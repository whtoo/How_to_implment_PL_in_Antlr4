package org.teachfx.antlr4.ep18r.vizvmr.unified.core;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.teachfx.antlr4.ep18r.vizvmr.unified.bridge.UnifiedEventAdapter;
import org.teachfx.antlr4.ep18r.vizvmr.unified.core.IRxVMStateManager;
import org.teachfx.antlr4.ep18r.vizvmr.unified.core.IVM;
import org.teachfx.antlr4.ep18r.vizvmr.unified.core.VMTypes;
import org.teachfx.antlr4.ep18r.vizvmr.unified.event.IVMEventBus;
import org.teachfx.antlr4.ep18r.vizvmr.unified.event.VMEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 统一响应式虚拟机状态管理器实现
 *
 * <p>整合原有三重状态管理，提供单一响应式状态管理</p>
 */
public class RxVMStateManagerImpl implements IRxVMStateManager {

    private static final Logger logger = LogManager.getLogger(RxVMStateManagerImpl.class);
    private static final int NUM_REGISTERS = 16;

    private final IVM vm;
    private final IVMEventBus eventBus;
    private final UnifiedEventAdapter eventAdapter;
    private final TerminalObservabilityImpl observability;
    private final VMBreakpointManager breakpointManager;

    private final int heapSize;
    private final int globalsSize;

    // ==================== RxJava状态流 ====================

    private BehaviorSubject<int[]> registersSubject;
    private BehaviorSubject<Integer> pcSubject;
    private BehaviorSubject<int[]> heapSubject;
    private BehaviorSubject<int[]> globalsSubject;
    private BehaviorSubject<int[]> callStackSubject;
    private BehaviorSubject<VMTypes.VMState> stateSubject;
    private BehaviorSubject<Long> executionStepsSubject;

    // ==================== 执行控制 ====================

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean stepping = new AtomicBoolean(false);

    private final PublishSubject<VMTypes.Command> commandSubject = PublishSubject.create();

    // ==================== 配置 ====================

    private volatile int autoStepDelay = 200;
    private volatile boolean autoStepMode = false;

    // ==================== 性能统计 ====================

    private final AtomicLong startTime = new AtomicLong(0);
    private final AtomicInteger instructionCount = new AtomicInteger(0);
    private final AtomicInteger breakpointHitCount = new AtomicInteger(0);

    public RxVMStateManagerImpl(IVM vm, int heapSize, int globalsSize) {
        this.vm = vm;
        this.heapSize = heapSize;
        this.globalsSize = globalsSize;

        this.eventBus = new org.teachfx.antlr4.ep18r.vizvmr.unified.event.VMEventBusImpl();
        this.eventAdapter = new UnifiedEventAdapter(eventBus);
        this.breakpointManager = new VMBreakpointManager();
        this.observability = new TerminalObservabilityImpl(eventBus, this);

        initializeSubjects();
        registerEventListeners();
        setupCommandPipeline();

        logger.info("RxVMStateManager初始化完成，堆大小={}, 全局变量大小={}", heapSize, globalsSize);
    }

    private void initializeSubjects() {
        this.registersSubject = BehaviorSubject.createDefault(new int[NUM_REGISTERS]);
        this.pcSubject = BehaviorSubject.createDefault(0);
        this.heapSubject = BehaviorSubject.createDefault(new int[heapSize]);
        this.globalsSubject = BehaviorSubject.createDefault(new int[globalsSize]);
        this.callStackSubject = BehaviorSubject.createDefault(new int[0]);
        this.stateSubject = BehaviorSubject.createDefault(VMTypes.VMState.CREATED);
        this.executionStepsSubject = BehaviorSubject.createDefault(0L);
    }

    private void registerEventListeners() {
    vm.addVisualizationListener(eventAdapter);
        eventBus.registerListener(this);
    }

    private void setupCommandPipeline() {
        commandSubject
            .observeOn(Schedulers.io())
            .subscribe(command -> {
                logger.info("执行命令: {}", command);
                try {
                    executeCommandInternal(command);
                } catch (Exception e) {
                    logger.error("命令执行失败: {}", command, e);
                    handleExecutionError(e);
                }
            }, error -> logger.error("命令流错误", error));
    }

    // ==================== RxJava状态流 ====================

    @Override
    public Observable<int[]> registers() {
        return registersSubject.hide();
    }

    @Override
    public Observable<Integer> register(int regNum) {
        return registersSubject.map(regs -> regs[regNum]).hide();
    }

    @Override
    public Observable<Integer> pc() {
        return pcSubject.hide();
    }

    @Override
    public Observable<int[]> heap() {
        return heapSubject.hide();
    }

    @Override
    public Observable<int[]> globals() {
        return globalsSubject.hide();
    }

    @Override
    public Observable<int[]> callStack() {
        return callStackSubject.hide();
    }

    @Override
    public Observable<VMTypes.VMState> state() {
        return stateSubject.hide();
    }

    @Override
    public Observable<Long> executionSteps() {
        return executionStepsSubject.hide();
    }

    // ==================== 命令执行 ====================

    @Override
    public CompletableFuture<VMTypes.CommandResult> executeAsync(VMTypes.Command command) {
        CompletableFuture<VMTypes.CommandResult> future = new CompletableFuture<>();
        commandSubject.onNext(command);
        return future;
    }

    @Override
    public VMTypes.CommandResult execute(VMTypes.Command command) {
        try {
            VMTypes.CommandResult result = executeCommandInternal(command);
            return result;
        } catch (Exception e) {
            logger.error("同步命令执行失败: {}", command, e);
            return VMTypes.CommandResult.failure(command, e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<VMTypes.CommandResult> loadCode(InputStream codeStream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (codeStream == null) {
                    logger.warn("代码流为null，跳过加载");
                    return VMTypes.CommandResult.success(
                        VMTypes.Command.LOAD_CODE,
                        "跳过代码加载",
                        VMTypes.VMState.LOADED
                    );
                }

                boolean hasErrors = IVMAdapter.load(vm, codeStream);
                if (hasErrors) {
                    return VMTypes.CommandResult.failure(
                        VMTypes.Command.LOAD_CODE,
                        "代码加载失败",
                        new RuntimeException("代码加载返回错误状态")
                    );
                }

                syncStateFromVM();
                setState(VMTypes.VMState.LOADED);

                return VMTypes.CommandResult.success(
                    VMTypes.Command.LOAD_CODE,
                    "代码加载成功",
                    VMTypes.VMState.LOADED
                );
            } catch (Exception e) {
                logger.error("加载代码异常", e);
                return VMTypes.CommandResult.failure(
                    VMTypes.Command.LOAD_CODE,
                    "代码加载异常: " + e.getMessage(),
                    e
                );
            }
        }, java.util.concurrent.ForkJoinPool.commonPool());
    }

    @Override
    public void start() {
        logger.info("启动VM执行");
        if (running.get() && !paused.get()) {
            logger.warn("VM已经在运行中");
            return;
        }

        vm.setPaused(false);
        vm.setStepMode(false);
        running.set(true);
        paused.set(false);
        stepping.set(false);

        setState(VMTypes.VMState.RUNNING);
        startTime.set(System.currentTimeMillis());

        try {
            vm.exec();
            eventAdapter.publishExecutionStopped();
        } catch (Exception e) {
            logger.error("VM执行失败", e);
            handleExecutionError(e);
        }
    }

    @Override
    public void pause() {
        logger.info("暂停VM执行");
        if (!running.get() || paused.get()) {
            logger.warn("VM未运行或已暂停");
            return;
        }

        vm.setPaused(true);
        paused.set(true);

        eventAdapter.publishStateChange(VMTypes.VMState.RUNNING, VMTypes.VMState.PAUSED);
        setState(VMTypes.VMState.PAUSED);
    }

    @Override
    public void stop() {
        logger.info("停止VM执行");
        vm.setPaused(true);
        vm.setStepMode(false);

        running.set(false);
        paused.set(false);
        stepping.set(false);

        eventAdapter.publishExecutionStopped();
        setState(VMTypes.VMState.HALTED);

        syncStateFromVM();
    }

    @Override
    public void step() {
        logger.info("单步执行VM指令");
        if (!running.get() || paused.get()) {
            logger.debug("设置步进模式");
            vm.setStepMode(true);
            vm.setPaused(false);
            running.set(true);
        }

        stepping.set(true);

        try {
            vm.step();
            instructionCount.incrementAndGet();
            executionStepsSubject.onNext((long) instructionCount.get());
        } catch (Exception e) {
            logger.error("单步执行失败", e);
            handleExecutionError(e);
        }

        syncStateFromVM();
    }

    @Override
    public void resume() {
        logger.info("恢复VM执行");
        if (!paused.get()) {
            logger.warn("VM未暂停");
            return;
        }

        vm.setPaused(false);
        paused.set(false);

        eventAdapter.publishStateChange(VMTypes.VMState.PAUSED, VMTypes.VMState.RUNNING);
        setState(VMTypes.VMState.RUNNING);
    }

    // ==================== 断点管理 ====================

    @Override
    public void setBreakpoint(int pc) {
        breakpointManager.setBreakpoint(pc);
        eventBus.publish(new VMEvent.StateChangedEvent(stateSubject.getValue(), stateSubject.getValue()));
    }

    @Override
    public void clearBreakpoint(int pc) {
        breakpointManager.clearBreakpoint(pc);
    }

    @Override
    public void toggleBreakpoint(int pc) {
        breakpointManager.toggleBreakpoint(pc);
    }

    @Override
    public void clearAllBreakpoints() {
        breakpointManager.clearAllBreakpoints();
    }

    @Override
    public Set<Integer> getBreakpoints() {
        return breakpointManager.getBreakpoints();
    }

    @Override
    public boolean hasBreakpoints() {
        return breakpointManager.hasBreakpoints();
    }

    // ==================== 终端可观测性 ====================

    @Override
    public ITerminalObservability getObservability() {
        return observability;
    }

    // ==================== 状态查询 ====================

    @Override
    public int[] getRegistersSnapshot() {
        return registersSubject.getValue().clone();
    }

    @Override
    public int getRegisterValue(int regNum) {
        int[] regs = registersSubject.getValue();
        if (regNum < 0 || regNum >= NUM_REGISTERS) {
            throw new IllegalArgumentException("Invalid register number: " + regNum);
        }
        return regs[regNum];
    }

    @Override
    public int getPC() {
        return pcSubject.getValue();
    }

    @Override
    public int[] getHeapSnapshot() {
        return heapSubject.getValue().clone();
    }

    @Override
    public int[] getGlobalsSnapshot() {
        return globalsSubject.getValue().clone();
    }

    @Override
    public VMTypes.VMState getCurrentState() {
        return stateSubject.getValue();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean isPaused() {
        return paused.get();
    }

    // ==================== 配置 ====================

    @Override
    public void setAutoStepMode(boolean autoStepMode) {
        this.autoStepMode = autoStepMode;
    }

    @Override
    public void setAutoStepDelay(int delayMs) {
        this.autoStepDelay = delayMs;
    }

    @Override
    public int getAutoStepDelay() {
        return autoStepDelay;
    }

    // ==================== 内部方法 ====================

    private VMTypes.CommandResult executeCommandInternal(VMTypes.Command command) {
        switch (command) {
            case LOAD_CODE:
                throw new UnsupportedOperationException("使用loadCode()方法加载代码");

            case START:
                start();
                return VMTypes.CommandResult.success(command, "VM已启动", VMTypes.VMState.RUNNING);

            case PAUSE:
                pause();
                return VMTypes.CommandResult.success(command, "VM已暂停", VMTypes.VMState.PAUSED);

            case STOP:
                stop();
                return VMTypes.CommandResult.success(command, "VM已停止", VMTypes.VMState.HALTED);

            case STEP:
                step();
                return VMTypes.CommandResult.success(command, "单步执行完成", VMTypes.VMState.STEPPING);

            case RESUME:
                resume();
                return VMTypes.CommandResult.success(command, "VM已恢复", VMTypes.VMState.RUNNING);

            default:
                return VMTypes.CommandResult.failure(
                    command,
                    "未知命令: " + command,
                    new IllegalArgumentException("Unknown command: " + command)
                );
        }
    }

    private void setState(VMTypes.VMState newState) {
        VMTypes.VMState oldState = stateSubject.getValue();
        stateSubject.onNext(newState);
        eventAdapter.publishStateChange(oldState, newState);
    }

    private void syncStateFromVM() {
        try {
            int[] newRegisters = new int[NUM_REGISTERS];
            for (int i = 0; i < NUM_REGISTERS; i++) {
                newRegisters[i] = vm.getRegister(i);
            }
            registersSubject.onNext(newRegisters);

            int newPC = vm.getProgramCounter();
            int oldPC = pcSubject.getValue();
            pcSubject.onNext(newPC);
            eventAdapter.publishPCChange(oldPC, newPC);

            int[] newHeap = new int[heapSize];
            for (int i = 0; i < heapSize; i++) {
                newHeap[i] = vm.readHeap(i);
            }
            heapSubject.onNext(newHeap);

            int[] newGlobals = new int[globalsSize];
            for (int i = 0; i < globalsSize; i++) {
                newGlobals[i] = ((Number) vm.readGlobal(i)).intValue();
            }
            globalsSubject.onNext(newGlobals);

            int[] callStack = new int[0];
            callStackSubject.onNext(callStack);

        } catch (Exception e) {
            logger.error("状态同步失败", e);
        }
    }

    private void handleExecutionError(Throwable error) {
        logger.error("执行错误", error);
        setState(VMTypes.VMState.ERROR);
        eventBus.publish(new VMEvent.StateChangedEvent(
            stateSubject.getValue(),
            VMTypes.VMState.ERROR
        ));
    }

    /**
     * 内部断点管理器
     */
    private static class VMBreakpointManager {
        private final Set<Integer> breakpoints = new HashSet<>();

        public void setBreakpoint(int pc) {
            breakpoints.add(pc);
        }

        public void clearBreakpoint(int pc) {
            breakpoints.remove(pc);
        }

        public void toggleBreakpoint(int pc) {
            if (breakpoints.contains(pc)) {
                clearBreakpoint(pc);
            } else {
                setBreakpoint(pc);
            }
        }

        public void clearAllBreakpoints() {
            breakpoints.clear();
        }

        public Set<Integer> getBreakpoints() {
            return new HashSet<>(breakpoints);
        }

        public boolean hasBreakpoints() {
            return !breakpoints.isEmpty();
        }
    }

    /**
     * 终端可观测性实现
     */
    private static class TerminalObservabilityImpl implements ITerminalObservability {

        private final IVMEventBus eventBus;
        private final RxVMStateManagerImpl stateManager;
        private final List<VMEvent> eventLog = Collections.synchronizedList(new ArrayList<>());
        private final List<StateSnapshot> snapshots = Collections.synchronizedList(new ArrayList<>());

        public TerminalObservabilityImpl(IVMEventBus eventBus, RxVMStateManagerImpl stateManager) {
            this.eventBus = eventBus;
            this.stateManager = stateManager;
            setupEventLogging();
        }

        private void setupEventLogging() {
            eventBus.events().subscribe(event -> {
                eventLog.add(event);
            });
        }

        @Override
        public List<VMEvent> getEventLog() {
            return new ArrayList<>(eventLog);
        }

        @Override
        public Observable<VMEvent> eventStream() {
            return eventBus.events();
        }

        @Override
        public void clearEventLog() {
            eventLog.clear();
        }

        @Override
        public int getEventCount() {
            return eventLog.size();
        }

        @Override
        public StateSnapshot captureSnapshot() {
            return new StateSnapshot(
                stateManager.getRegistersSnapshot(),
                stateManager.getHeapSnapshot(),
                stateManager.getGlobalsSnapshot(),
                stateManager.getPC(),
                stateManager.callStackSubject.getValue().length,
                stateManager.getCurrentState(),
                System.currentTimeMillis(),
                stateManager.executionStepsSubject.getValue()
            );
        }

        @Override
        public List<StateSnapshot> getAllSnapshots() {
            return new ArrayList<>(snapshots);
        }

        @Override
        public void clearSnapshots() {
            snapshots.clear();
        }

        @Override
        public void restoreSnapshot(StateSnapshot snapshot) {
            throw new UnsupportedOperationException("状态恢复功能待实现");
        }

        @Override
        public long getInstructionCount() {
            return stateManager.instructionCount.get();
        }

        @Override
        public long getExecutionDuration() {
            long start = stateManager.startTime.get();
            return start > 0 ? System.currentTimeMillis() - start : 0;
        }

        @Override
        public double getAverageInstructionTime() {
            long duration = getExecutionDuration();
            long count = getInstructionCount();
            return count > 0 ? (double) duration / count : 0;
        }

        @Override
        public int getBreakpointHitCount() {
            return stateManager.breakpointHitCount.get();
        }

        @Override
        public AssertionHelper assertions() {
            return new AssertionHelper(eventLog, this);
        }

        @Override
        public boolean isStateReached(VMTypes.VMState expectedState) {
            return eventLog.stream()
                .filter(e -> e instanceof VMEvent.StateChangedEvent)
                .map(e -> (VMEvent.StateChangedEvent) e)
                .anyMatch(e -> e.getNewState() == expectedState);
        }

        @Override
        public boolean isPCReached(int expectedPC) {
            return eventLog.stream()
                .filter(e -> e instanceof VMEvent.PCChangedEvent)
                .map(e -> (VMEvent.PCChangedEvent) e)
                .anyMatch(e -> e.getNewPC() == expectedPC);
        }

        @Override
        public boolean hasEvent(Class<? extends VMEvent> eventType) {
            return eventLog.stream().anyMatch(e -> eventType.isInstance(e));
        }

        @Override
        public String getDiagnosticReport() {
            StringBuilder report = new StringBuilder();
            report.append("VM诊断报告\n");
            report.append("=" .repeat(40)).append("\n\n");
            report.append("当前状态: ").append(stateManager.getCurrentState()).append("\n");
            report.append("程序计数器: ").append(stateManager.getPC()).append("\n");
            report.append("已执行指令数: ").append(getInstructionCount()).append("\n");
            report.append("事件总数: ").append(getEventCount()).append("\n");
            report.append("断点命中次数: ").append(getBreakpointHitCount()).append("\n");
            report.append("执行时长: ").append(getExecutionDuration()).append("ms\n");
            return report.toString();
        }

        @Override
        public void printExecutionHistory() {
            System.out.println("执行历史:");
            for (VMEvent event : eventLog) {
                System.out.println("  " + event);
            }
        }

        @Override
        public void printStateSummary() {
            StateSnapshot snapshot = captureSnapshot();
            System.out.println("状态摘要:");
            System.out.println("  PC: " + snapshot.getPC());
            System.out.println("  状态: " + snapshot.getState());
            System.out.println("  执行步数: " + snapshot.getExecutionSteps());
        }

        @Override
        public String getStateDetails() {
            return String.format(
                "{\"pc\": %d, \"state\": \"%s\", \"steps\": %d}",
                stateManager.getPC(),
                stateManager.getCurrentState(),
                getInstructionCount()
            );
        }
    }
}
