package org.teachfx.antlr4.ep18r.vizvmr.core.ReactiveVMRStateModel;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;

import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;

/**
 * 响应式虚拟机状态模型
 * 使用RxJava实现单一数据源和响应式绑定
 */
public class ReactiveVMRStateModel {

    private final RegisterVMInterpreter vm;
    private final int maxCallStackDepth;

    // ==================== 状态Subjects ====================
    private final BehaviorSubject<int[]> registersSubject;
    private final BehaviorSubject<Integer> pcSubject;
    private final BehaviorSubject<int[]> heapSubject;
    private final BehaviorSubject<VMStateChangeEvent.State> vmStateSubject;
    private final BehaviorSubject<Long> executionStepsSubject;
    private final BehaviorSubject<InstructionInfo> instructionSubject;

    public ReactiveVMRStateModel(RegisterVMInterpreter vm) {
        this.vm = vm;
        int heapSize = vm.getHeapSize();
        
        registersSubject = BehaviorSubject.createDefault(new int[16]);
        pcSubject = BehaviorSubject.createDefault(0);
        heapSubject = BehaviorSubject.createDefault(new int[heapSize]);
        vmStateSubject = BehaviorSubject.createDefault(VMStateChangeEvent.State.CREATED);
        executionStepsSubject = BehaviorSubject.createDefault(0L);
        instructionSubject = BehaviorSubject.create();

        // ==================== 公共Observable ====================
        public Observable<int[]> getRegisters() { return registersSubject.hide(); }

        public Observable<Integer> getPC() { return pcSubject.hide(); }

        public Observable<int[]> getHeap() { return heapSubject.hide(); }

        public Observable<VMStateChangeEvent.State> getVMStateObs() { return vmStateSubject.hide(); }

        public Observable<Long> getStepsObs() { return executionStepsSubject.hide(); }

        public Observable<InstructionInfo> getInstruction() { return instructionSubject.hide(); }

        // ==================== 委器管理 ====================
        private volatile VMStateChangeEvent.State vmState = VMStateChangeEvent.State.CREATED;

        public void setVMState(VMStateChangeEvent.State state) {
            this.vmState = state;
            vmStateSubject.onNext(state);
        }

        public VMStateChangeEvent.State getVMState() {
            return vmState;
        }

        public void start() {
            vmStateSubject.onNext(VMStateChangeEvent.State.RUNNING);
            vmStateSubject.onNext(VMStateChangeEvent.State.RUNNING);
        }

        public void pause() {
            vmStateSubject.onNext(VMStateChangeEvent.State.PAUSED);
            vmStateSubject.onNext(VMStateChangeEvent.State.PAUSED);
        }

        public void stop() {
            vmStateSubject.onNext(VMStateChangeEvent.State.HALTED);
        vmStateSubject.onNext(VMStateChangeEvent.State.HALTED);
            syncFromVM();
        }

        public void step() {
            vmStateSubject.onNext(VMStateChangeEvent.State.STEPPING);
            vm.setPaused(true);
            vmStateSubject.onNext(VMStateChangeEvent.State.STEPPING);
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
            int[] currentRegs = new int[16];
            for (int i = 0; i < 16; i++) {
                currentRegs[i] = vm.getRegister(i);
            }
            registersSubject.onNext(currentRegs.clone());
        }

        private void syncMemory() {
            int[] currentHeap = new int[heapSize];
            for (int i = 0; i < currentHeap.length; i++) {
                currentHeap[i] = vm.readHeap(i);
            }
            heapSubject.onNext(currentHeap.clone());
        }

        private void syncPC() {
            int pc = vm.getProgramCounter();
            pcSubject.onNext(pc);
        }

        private void syncHeap() {
            int heapAllocPointer = vm.getHeapAllocPointer();
            heapSubject.onNext(heapAllocPointer);
        }

        private void syncState() {
            VMStateChangeEvent.State state = vmStateSubject.getValue();
            vmStateSubject.onNext(state);
        }

        // ==================== 栈帧同步 ====================
        private volatile VMStateChangeEvent.State lastStackEvent;
        private final BehaviorSubject<StackFrameEvent> stackFrameEventSubject;

        public ReactiveVMRStateModel(RegisterVMInterpreter vm) {
            this.vm = vm;
            this.lastStackEvent = VMStateChangeEvent.State.CREATED;
            this.stackFrameEventSubject = PublishSubject.create();
        }

        public void pushStackFrame(StackFrame frame) {
            StackFrameEvent event = new StackFrameEvent(
                    this,
                    frame,
                    StackFrameEvent.EventType.PUSH,
                    lastStackEvent.stepNumber
            );
            stackFrameEventSubject.onNext(event);
        }

        public void popStackFrame() {
            StackFrameEvent event = new StackFrameEvent(
                    this,
                    null,
                    StackFrameEvent.EventType.POP,
                    lastStackEvent.stepNumber
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
}
