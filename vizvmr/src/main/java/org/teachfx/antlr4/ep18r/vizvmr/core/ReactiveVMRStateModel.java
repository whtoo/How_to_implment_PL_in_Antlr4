package org.teachfx.antlr4.ep18r.vizvmr.core;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;

import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.event.StackFrameEvent;
import org.teachfx.antlr4.ep18r.vizvmr.event.InstructionExecutionEvent;
import org.teachfx.antlr4.ep18r.vizvmr.event.RegisterChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.event.MemoryChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.controller.VMCommandController;
import org.teachfx.antlr4.ep18r.vizvmr.controller.VMCommandResult;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.Arrays;

/**
 * 响应式虚拟机状态模型
 * 使用RxJava实现单一数据源和响应式绑定
 */
public class ReactiveVMRStateModel {
    
    private static final Logger logger = LogManager.getLogger(ReactiveVMRStateModel.class);

    private final RegisterVMInterpreter vm;
    private final int maxCallStackDepth;
    private final int heapSize;
    
    // ==================== 可视化桥接 ====================
    private final org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel delegateStateModel;
    private final org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge visualBridge;
    private final VMCommandController commandController;

    // ==================== 状态Subjects ====================
    private final BehaviorSubject<int[]> registersSubject;
    private final BehaviorSubject<Integer> pcSubject;
    private final BehaviorSubject<int[]> heapSubject;
    private final BehaviorSubject<VMStateChangeEvent.State> vmStateSubject;
    private final BehaviorSubject<Long> executionStepsSubject;
    private final BehaviorSubject<InstructionExecutionEvent> instructionSubject;

    // ==================== 栈帧管理 ====================
    private volatile VMStateChangeEvent.State vmState = VMStateChangeEvent.State.CREATED;
    private volatile StackFrameEvent lastStackEvent;
    private final BehaviorSubject<StackFrameEvent> stackFrameEventSubject;

    private final io.reactivex.rxjava3.subjects.PublishSubject<RegisterChangeEvent> registerChangesSubject;
    private final io.reactivex.rxjava3.subjects.PublishSubject<MemoryChangeEvent> memoryChangesSubject;

    private volatile int autoStepDelay = 200;

    public ReactiveVMRStateModel(RegisterVMInterpreter vm) {
        logger.info("创建ReactiveVMRStateModel，VM堆大小: {}", vm.getHeapSize());
        this.vm = vm;
        this.heapSize = vm.getHeapSize();
        this.maxCallStackDepth = 100;

        // 初始化委托状态模型和可视化桥接
        logger.debug("初始化委托状态模型，堆大小: {}，最大调用栈深度: {}", heapSize, maxCallStackDepth);
        this.delegateStateModel = new org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel(
            heapSize, 256, maxCallStackDepth);
        this.visualBridge = new org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge(vm, delegateStateModel);
        this.commandController = new VMCommandController(vm);
        visualBridge.setAutoStepMode(true);
        visualBridge.setAutoStepDelay(autoStepDelay);
        logger.debug("设置自动步进模式: true，延迟: {}ms", autoStepDelay);
        setupVisualBridgeCallback();

        registersSubject = BehaviorSubject.createDefault(new int[16]);
        pcSubject = BehaviorSubject.createDefault(0);
        heapSubject = BehaviorSubject.createDefault(new int[heapSize]);
        vmStateSubject = BehaviorSubject.createDefault(VMStateChangeEvent.State.CREATED);
        executionStepsSubject = BehaviorSubject.createDefault(0L);
        instructionSubject = BehaviorSubject.create();
        this.lastStackEvent = null;
        this.stackFrameEventSubject = BehaviorSubject.create();
        this.registerChangesSubject = io.reactivex.rxjava3.subjects.PublishSubject.create();
        this.memoryChangesSubject = io.reactivex.rxjava3.subjects.PublishSubject.create();
        
        logger.info("ReactiveVMRStateModel初始化完成，Subjects已创建");
    }

    // ==================== 公共Observable ====================
    public Observable<int[]> getRegisters() { return registersSubject.hide(); }

    public Observable<Integer> getPC() { return pcSubject.hide(); }

    public Observable<int[]> getHeap() { return heapSubject.hide(); }

    public Observable<VMStateChangeEvent.State> getVMStateObs() { return vmStateSubject.hide(); }

    public Observable<VMStateChangeEvent.State> getExecutionStatus() { return vmStateSubject.hide(); }

    public Observable<Long> getStepsObs() { return executionStepsSubject.hide(); }

    public Observable<InstructionExecutionEvent> getInstruction() { return instructionSubject.hide(); }

    public Observable<RegisterChangeEvent> getRegisterChanges() { return registerChangesSubject.hide(); }

    public Observable<MemoryChangeEvent> getMemoryChanges() { return memoryChangesSubject.hide(); }

    public int[] getHeapSnapshot() {
        return heapSubject.getValue();
    }

    public void setAutoStepDelay(int delayMs) {
        this.autoStepDelay = delayMs;
        visualBridge.setAutoStepDelay(delayMs);
    }

    public int getAutoStepDelay() {
        return autoStepDelay;
    }

    // ==================== 委器管理 ====================
    public void setVMState(VMStateChangeEvent.State state) {
        this.vmState = state;
        vmStateSubject.onNext(state);
    }

    public VMStateChangeEvent.State getVMState() {
        return vmState;
    }

    public void start() {
        logger.info("开始VM执行");
        VMCommandResult result = commandController.start();
        if (!result.isSuccess()) {
            logger.error("启动失败: {}", result.getMessage());
        } else {
            logger.info("启动成功: {}", result.getMessage());
        }
    }

    public void pause() {
        logger.info("暂停VM执行");
        VMCommandResult result = commandController.pause();
        if (!result.isSuccess()) {
            logger.error("暂停失败: {}", result.getMessage());
        }
    }

    public void stop() {
        logger.info("停止VM执行");
        VMCommandResult result = commandController.stop();
        if (!result.isSuccess()) {
            logger.error("停止失败: {}", result.getMessage());
        }
    }

    public void step() {
        logger.info("单步执行VM指令");
        VMCommandResult result = commandController.step();
        if (!result.isSuccess()) {
            logger.error("单步执行失败: {}", result.getMessage());
        }
    }

    public void syncFromVM() {
        syncRegisters();
        syncMemory();
        syncPC();
        syncHeap();
        syncState();
    }

    // ==================== 状态同步 ====================
    private void syncRegisters() {
        logger.debug("同步寄存器状态");
        int[] currentRegs = new int[16];
        for (int i = 0; i < 16; i++) {
            currentRegs[i] = vm.getRegister(i);
        }
        registersSubject.onNext(currentRegs.clone());
        logger.trace("寄存器状态已更新: {}", Arrays.toString(currentRegs));
    }

    private void syncMemory() {
        logger.debug("同步内存状态，堆大小: {}", heapSize);
        int[] currentHeap = new int[heapSize];
        for (int i = 0; i < currentHeap.length; i++) {
            currentHeap[i] = vm.readHeap(i);
        }
        heapSubject.onNext(currentHeap.clone());
        logger.trace("内存状态已更新，前10个值: {}", Arrays.toString(Arrays.copyOf(currentHeap, Math.min(10, currentHeap.length))));
    }

    private void syncPC() {
        int pc = vm.getProgramCounter();
        logger.debug("同步程序计数器: {}", pc);
        pcSubject.onNext(pc);
    }

    private void syncHeap() {
        int heapAllocPointer = vm.getHeapAllocPointer();
        logger.debug("同步堆分配指针: {}", heapAllocPointer);
        // Publish heap alloc pointer as special marker value in heap array
        int[] heapWithPointer = new int[heapSize];
        heapWithPointer[0] = heapAllocPointer;
        heapSubject.onNext(heapWithPointer);
    }

    private void syncState() {
        VMStateChangeEvent.State state = vmStateSubject.getValue();
        vmStateSubject.onNext(state);
    }

    // ==================== 栈帧同步 ====================
    public void pushStackFrame(StackFrame frame) {
        String funcName = (frame.getFunctionSymbol() != null) ? frame.getFunctionSymbol().name : "anonymous";
        logger.info("压入栈帧: {}，返回地址: {}，帧基址: {}",
            funcName, frame.getReturnAddress(), frame.getFrameBasePointer());
        int stepNumber = lastStackEvent != null ? lastStackEvent.getStepNumber() : 0;
        StackFrameEvent event = new StackFrameEvent(
                this,
                frame,
                StackFrameEvent.EventType.PUSH,
                stepNumber
        );
        stackFrameEventSubject.onNext(event);
        this.lastStackEvent = event;
    }

    public void popStackFrame() {
        logger.info("弹出栈帧");
        int stepNumber = lastStackEvent != null ? lastStackEvent.getStepNumber() : 0;
        StackFrameEvent event = new StackFrameEvent(
                this,
                null,
                StackFrameEvent.EventType.POP,
                stepNumber
        );
        stackFrameEventSubject.onNext(event);
        this.lastStackEvent = event;
    }

    public StackFrameEvent getLastStackEvent() {
        return lastStackEvent;
    }

    public Observable<StackFrameEvent> getStackFrameEvents() {
        return stackFrameEventSubject.hide();
    }

    // ==================== 公共方法 ====================
    public StackFrame[] getCallStack() {
            try {
                java.lang.reflect.Field callStackField = vm.getClass().getDeclaredField("callStack");
                return (StackFrame[]) callStackField.get(vm);
            } catch (Exception e) {
                return new StackFrame[0];
            }
        }

        public int getCallStackDepth() {
            try {
                java.lang.reflect.Field callStackField = vm.getClass().getDeclaredField("callStack");
                StackFrame[] callStack = (StackFrame[]) callStackField.get(vm);
                return callStack.length;
            } catch (Exception e) {
                return 0;
            }
        }

    private void setupVisualBridgeCallback() {
        visualBridge.setExecutionCallback(new org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge.ExecutionCallback() {
            @Override
            public void onRegisterChanged(int regNum, int oldValue, int newValue) {
                logger.debug("寄存器 {} 改变: {} -> {}", regNum, oldValue, newValue);
                syncRegisters();
            }

            @Override
            public void onMemoryChanged(org.teachfx.antlr4.ep18r.vizvmr.event.MemoryChangeEvent.MemoryType type, int address, int oldValue, int newValue) {
                logger.debug("内存改变 - 类型: {}, 地址: {}, 值: {} -> {}", type, address, oldValue, newValue);
                syncMemory();
            }

            @Override
            public void onPCChanged(int oldPC, int newPC) {
                logger.debug("程序计数器改变: {} -> {}", oldPC, newPC);
                syncPC();
            }

            @Override
            public void onStateChanged(org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State oldState, org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State newState) {
                logger.info("VM状态改变: {} -> {}", oldState, newState);
                vmStateSubject.onNext(newState);
            }

            @Override
            public void onInstructionExecuted(int pc, int opcode, String mnemonic, String operands) {
                logger.info("指令执行: PC={}, 操作码={}, 助记符={}, 操作数={}", pc, opcode, mnemonic, operands);
                instructionSubject.onNext(new InstructionExecutionEvent(visualBridge, 0, pc, opcode, mnemonic, operands));
                syncPC();
                syncRegisters();
                syncMemory();
            }

            @Override
            public void onExecutionStarted() {
                logger.info("VM执行开始");
                vmStateSubject.onNext(VMStateChangeEvent.State.RUNNING);
            }

            @Override
            public void onExecutionFinished() {
                logger.info("VM执行完成");
                vmStateSubject.onNext(VMStateChangeEvent.State.HALTED);
                syncFromVM();
            }

            @Override
            public void onExecutionPaused() {
                logger.info("VM执行暂停");
                vmStateSubject.onNext(VMStateChangeEvent.State.PAUSED);
            }

            @Override
            public void onError(Throwable error) {
                logger.error("VM执行错误", error);
                error.printStackTrace();
            }
        });
    }
}
