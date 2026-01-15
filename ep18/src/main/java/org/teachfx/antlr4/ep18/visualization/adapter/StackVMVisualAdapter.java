package org.teachfx.antlr4.ep18.visualization.adapter;

import org.teachfx.antlr4.common.visualization.*;
import org.teachfx.antlr4.common.visualization.event.*;
import org.teachfx.antlr4.common.visualization.event.events.*;
import org.teachfx.antlr4.ep18.VMInterpreter;
import org.teachfx.antlr4.ep18.stackvm.DisAssembler;
import org.teachfx.antlr4.ep18.stackvm.FunctionSymbol;
import org.teachfx.antlr4.ep18.stackvm.VMConfig;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EP18栈式虚拟机可视化适配器
 * 
 * <p>实现IVirtualMachineVisualization接口，为EP18栈式虚拟机提供统一的可视化支持。
 * 该适配器包装VMInterpreter实例，提供状态获取、执行控制、教育功能和事件通知。</p>
 * 
 * <p>注意：该适配器需要VMInterpreter支持暂停点检查和事件触发机制。
 * 当前实现假设VMInterpreter已相应修改。</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段二
 */
public class StackVMVisualAdapter implements IVirtualMachineVisualization, EventPublisher {
    
    // ==================== 核心依赖 ====================
    
    /**
     * 底层虚拟机解释器
     */
    private final VMInterpreter vm;
    
    /**
     * 事件总线
     */
    private EventBus eventBus;
    

    
    /**
     * 反汇编器
     */
    private final DisAssembler disAssembler;
    
    // ==================== 执行状态 ====================
    
    /**
     * 虚拟机状态
     */
    private final VMState<StackVMSpecificState> vmState;
    
    /**
     * 执行线程
     */
    private Thread executionThread;
    
    /**
     * 运行标志
     */
    private final AtomicBoolean running;
    
    /**
     * 暂停标志
     */
    private final AtomicBoolean paused;
    
    /**
     * 步进模式标志
     */
    private final AtomicBoolean stepMode;
    
    /**
     * 断点集合
     */
    private final Set<Integer> breakpoints;
    
    /**
     * 步进计数器
     */
    private final AtomicInteger stepCounter;
    
    // ==================== 监听器列表 ====================
    
    /**
     * 执行监听器列表
     */
    private final List<ExecutionListener> executionListeners;
    
    /**
     * 状态变化监听器列表
     */
    private final List<StateChangeListener> stateChangeListeners;
    
    /**
     * 教育提示监听器列表
     */
    private final List<EducationalHintListener> educationalHintListeners;
    
    // ==================== 构造函数 ====================
    
    /**
     * 构造函数
     * 
     * @param vm 虚拟机解释器实例
     * @param config VM配置
     */
    public StackVMVisualAdapter(VMInterpreter vm, VMConfig config) {
        if (vm == null) {
            throw new IllegalArgumentException("VM interpreter cannot be null");
        }
        
        this.vm = vm;
        this.eventBus = new EventBus();

        this.disAssembler = createDisAssembler(vm);
        
        // 初始化状态
        this.vmState = new VMState<>(
            0, 
            VMState.ExecutionState.UNINITIALIZED,
            new StackVMSpecificState()
        );
        
        // 初始化标志
        this.running = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);
        this.stepMode = new AtomicBoolean(false);
        this.breakpoints = Collections.synchronizedSet(new HashSet<>());
        this.stepCounter = new AtomicInteger(0);
        
        // 初始化监听器列表
        this.executionListeners = new CopyOnWriteArrayList<>();
        this.stateChangeListeners = new CopyOnWriteArrayList<>();
        this.educationalHintListeners = new CopyOnWriteArrayList<>();
        
        // 初始化事件系统
        initializeEventSystem();
    }
    
    /**
     * 创建反汇编器
     */
    private DisAssembler createDisAssembler(VMInterpreter vm) {
        try {
            byte[] code = vm.getCode();
            int codeSize = vm.getCodeSize();
            Object[] constPool = vm.getConstantPool();
            if (code != null && codeSize > 0) {
                return new DisAssembler(code, codeSize, constPool);
            }
        } catch (Exception e) {
            // 如果无法创建反汇编器，返回null（某些方法将回退到基本反汇编）
        }
        return null;
    }

    /**
     * 初始化事件系统
     */
    private void initializeEventSystem() {
        // 启动事件总线
        eventBus.start();
        
        // 注册默认事件订阅者（用于内部状态跟踪）
        eventBus.subscribe(new EventSubscriber<InstructionExecutedEvent>() {
            @Override
            public void onEvent(InstructionExecutedEvent event) {
                // 更新步进计数器
                stepCounter.incrementAndGet();
                
                // 更新VM状态
                updateVMState();
                
                // 通知执行监听器
                for (ExecutionListener listener : executionListeners) {
                    listener.afterInstructionExecute(
                        event.getPC(),
                        event.getMnemonic() + " " + event.getOperands(),
                        null // 结果暂不提供
                    );
                }
            }
            
            @Override
            public Class<InstructionExecutedEvent> getSubscribedEventType() {
                return InstructionExecutedEvent.class;
            }
            
            @Override
            public String getSubscriberId() {
                return "StackVMVisualAdapter";
            }
            
            @Override
            public String getSourceId() {
                return "StackVMVisualAdapter";
            }
        });
    }
    
    // ==================== IVirtualMachineVisualization 实现 ====================
    
    @Override
    public VMState<StackVMSpecificState> getCurrentState() {
        updateVMState();
        return vmState.copy();
    }
    
    @Override
    public String disassembleInstruction(int pc) {
        if (disAssembler != null) {
            try {
                // 使用反汇编器的disassembleInstructionToString方法（如果存在）
                // 否则回退到基本反汇编
                java.lang.reflect.Method method = DisAssembler.class.getMethod("disassembleInstructionToString", int.class);
                return (String) method.invoke(disAssembler, pc);
            } catch (Exception e) {
                // 回退到基本反汇编
            }
        }
        
        // 基本反汇编：显示操作码和PC
        byte[] code = vm.getCode();
        if (code != null && pc >= 0 && pc < code.length) {
            int opcode = code[pc] & 0xFF;
            return String.format("PC: %d, Opcode: 0x%02X", pc, opcode);
        }
        
        return "Invalid PC: " + pc;
    }
    
    @Override
    public List<StackFrame> getCallStack() {
        // 获取调用栈（需要VM支持）
        // 当前实现返回空列表，后续需要扩展VM以提供调用栈访问
        return Collections.emptyList();
    }
    
    @Override
    public String getEducationalHint() {
        // 根据当前状态生成教育提示
        int pc = vmState.getProgramCounter();
        String instruction = vmState.getCurrentInstruction();
        
        StringBuilder hint = new StringBuilder();
        hint.append("当前指令: ").append(instruction).append("\n");
        hint.append("程序计数器: ").append(pc).append("\n");
        hint.append("执行状态: ").append(vmState.getExecutionState()).append("\n");
        hint.append("已执行指令数: ").append(stepCounter.get()).append("\n");
        
        return hint.toString();
    }
    
    @Override
    public void step() {
        if (!running.get() || paused.get()) {
            // 确保虚拟机已加载代码
            if (vm.getCode() == null || vm.getCodeSize() == 0) {
                throw new IllegalStateException("VM没有加载代码");
            }
            
            // 设置步进模式
            stepMode.set(true);
            
            // 如果已暂停，恢复执行一条指令
            if (paused.get()) {
                resumeExecutionForStep();
            } else {
                // 开始执行单步
                startStepExecution();
            }
        }
    }
    
    @Override
    public void run() {
        if (running.get() && !paused.get()) {
            // 已经在运行中
            return;
        }
        
        // 确保虚拟机已加载代码
        if (vm.getCode() == null || vm.getCodeSize() == 0) {
            throw new IllegalStateException("VM没有加载代码");
        }
        
        // 重置步进模式
        stepMode.set(false);
        
        // 启动执行线程
        startExecutionThread();
    }
    
    @Override
    public void pause() {
        if (running.get() && !paused.get()) {
            paused.set(true);
            vmState.setExecutionState(VMState.ExecutionState.PAUSED);
            
            // 发布暂停事件
            this.publish(new ExecutionStateChangedEvent(
                this,
                stepCounter.get(),
                VMState.ExecutionState.RUNNING,
                VMState.ExecutionState.PAUSED,
                "用户请求暂停"
            ));
            
            // 通知监听器
            for (ExecutionListener listener : executionListeners) {
                listener.executionPaused();
            }
        }
    }
    
    @Override
    public void stop() {
        boolean wasRunning = running.getAndSet(false);
        paused.set(false);
        stepMode.set(false);
        
        if (wasRunning) {
            vmState.setExecutionState(VMState.ExecutionState.STOPPED);
            
            // 发布停止事件
            this.publish(new ExecutionStateChangedEvent(
                this,
                stepCounter.get(),
                VMState.ExecutionState.RUNNING,
                VMState.ExecutionState.STOPPED,
                "用户请求停止"
            ));
            
            // 通知监听器
            for (ExecutionListener listener : executionListeners) {
                listener.executionStopped("用户请求停止");
            }
            
            // 中断执行线程
            if (executionThread != null && executionThread.isAlive()) {
                executionThread.interrupt();
            }
        }
    }
    
    @Override
    public boolean isRunning() {
        return running.get() && !paused.get();
    }
    
    @Override
    public boolean isPaused() {
        return paused.get();
    }
    
    @Override
    public void highlightCurrentOperation(String description) {
        // 发布操作高亮事件
        this.publish(new OperationHighlightedEvent(
            this,
            stepCounter.get(),
            description,
            vmState.getProgramCounter(),
            vmState.getCurrentInstruction()
        ));
    }
    
    @Override
    public void showExpressionEvaluation(String expression, List<EvaluationStep> steps) {
        // 发布表达式求值事件
        this.publish(new ExpressionEvaluationEvent(
            this,
            stepCounter.get(),
            expression,
            steps
        ));
    }
    
    @Override
    public void visualizeRegisterAllocation(List<LiveInterval> intervals) {
        // 发布寄存器分配可视化事件
        this.publish(new RegisterAllocationVisualizationEvent(
            this,
            stepCounter.get(),
            intervals
        ));
    }
    
    @Override
    public void compareWithOtherVM(String code, PerformanceMetrics metrics) {
        // 发布性能比较事件
        this.publish(new PerformanceComparisonEvent(
            this,
            stepCounter.get(),
            code,
            metrics
        ));
    }
    
    @Override
    public void addExecutionListener(ExecutionListener listener) {
        if (listener != null) {
            executionListeners.add(listener);
        }
    }
    
    @Override
    public void addStateChangeListener(StateChangeListener listener) {
        if (listener != null) {
            stateChangeListeners.add(listener);
        }
    }
    
    @Override
    public void addEducationalListener(EducationalHintListener listener) {
        if (listener != null) {
            educationalHintListeners.add(listener);
        }
    }
    
    // ==================== 内部方法 ====================
    
    /**
     * 更新VM状态
     */
    private void updateVMState() {
        // 获取当前PC
        int pc = getProgramCounter();
        
        // 获取当前指令
        String instruction = disassembleInstruction(pc);
        
        // 更新VMState
        vmState.setProgramCounter(pc);
        vmState.setCurrentInstruction(instruction);
        vmState.setExecutionState(
            running.get() ? 
                (paused.get() ? VMState.ExecutionState.PAUSED : VMState.ExecutionState.RUNNING) :
                VMState.ExecutionState.STOPPED
        );
        
        // 更新特定状态
        StackVMSpecificState specificState = vmState.getVmSpecificState();
        specificState.updateOperandStack(getOperandStackSnapshot());
        specificState.updateCallStackDepth(getCallStackDepth());
        specificState.updateGlobalVariables(getGlobalVariables());
    }
    
    /**
     * 获取程序计数器
     */
    private int getProgramCounter() {
        return vm.getProgramCounter();
    }

    /**
     * 获取操作栈快照
     */
    private Object[] getOperandStackSnapshot() {
        return vm.getOperandStack();
    }

    /**
     * 获取调用栈深度
     */
    private int getCallStackDepth() {
        return vm.getFramePointer() + 1;
    }

    /**
     * 获取全局变量
     */
    private Object[] getGlobalVariables() {
        return vm.getGlobalVariables();
    }

    /**
     * 启动执行线程
     */
    private void startExecutionThread() {
        if (executionThread != null && executionThread.isAlive()) {
            executionThread.interrupt();
        }
        
        running.set(true);
        paused.set(false);
        vmState.setExecutionState(VMState.ExecutionState.RUNNING);
        
        executionThread = new Thread(() -> {
            try {
                // 发布执行开始事件
                this.publish(new ExecutionStartedEvent(this, stepCounter.get()));
                
                // 通知监听器
                for (ExecutionListener listener : executionListeners) {
                    listener.executionStarted();
                }
                
                // 执行虚拟机
                vm.exec();
                
                // 执行完成
                running.set(false);
                vmState.setExecutionState(VMState.ExecutionState.STOPPED);
                
                // 发布执行完成事件
                this.publish(new ExecutionFinishedEvent(this, stepCounter.get(), "正常结束"));
                
                // 通知监听器
                for (ExecutionListener listener : executionListeners) {
                    listener.executionStopped("正常结束");
                }
                
            } catch (Exception e) {
                running.set(false);
                vmState.setExecutionState(VMState.ExecutionState.ERROR);
                
                // 发布错误事件
                this.publish(new ExecutionErrorEvent(this, stepCounter.get(), e));
                
                // 通知监听器
                for (ExecutionListener listener : executionListeners) {
                    listener.executionError(new VMExecutionException("虚拟机执行错误", e));
                }
            }
        }, "VM-Visual-Execution-Thread");
        
        executionThread.start();
    }
    
    /**
     * 启动单步执行
     */
    private void startStepExecution() {
        // 单步执行需要VM支持单步模式
        // 当前实现通过暂停点检查实现
        // 这里简化处理：直接调用VM的exec()，但需要在指令间暂停
        
        // TODO: 实现真正的单步执行
        // 当前先启动执行线程，但立即暂停
        startExecutionThread();
        pause();
    }
    
    /**
     * 恢复执行单步
     */
    private void resumeExecutionForStep() {
        // 单步执行需要VM支持
        // 当前简化：清除暂停标志，让VM执行一条指令后再暂停
        paused.set(false);
        stepMode.set(true);
        
        // TODO: 实现真正的单步恢复
        // 当前只是清除暂停标志
    }
    
    // ==================== 内部类：栈式VM特定状态 ====================
    
    /**
     * 栈式虚拟机特定状态
     */
    public static class StackVMSpecificState {
        private Object[] operandStack;
        private int callStackDepth;
        private Object[] globalVariables;
        private Map<String, Object> additionalInfo;
        
        public StackVMSpecificState() {
            this.operandStack = new Object[0];
            this.callStackDepth = 0;
            this.globalVariables = new Object[0];
            this.additionalInfo = new HashMap<>();
        }
        
        public void updateOperandStack(Object[] operandStack) {
            this.operandStack = operandStack != null ? Arrays.copyOf(operandStack, operandStack.length) : new Object[0];
        }
        
        public void updateCallStackDepth(int callStackDepth) {
            this.callStackDepth = callStackDepth;
        }
        
        public void updateGlobalVariables(Object[] globalVariables) {
            this.globalVariables = globalVariables != null ? Arrays.copyOf(globalVariables, globalVariables.length) : new Object[0];
        }
        
        public void setAdditionalInfo(String key, Object value) {
            if (key != null) {
                additionalInfo.put(key, value);
            }
        }
        
        public Object[] getOperandStack() {
            return Arrays.copyOf(operandStack, operandStack.length);
        }
        
        public int getCallStackDepth() {
            return callStackDepth;
        }
        
        public Object[] getGlobalVariables() {
            return Arrays.copyOf(globalVariables, globalVariables.length);
        }
        
        public Map<String, Object> getAdditionalInfo() {
            return new HashMap<>(additionalInfo);
        }
        
        @Override
        public String toString() {
            return String.format(
                "StackVMSpecificState{operandStack=%d, callStackDepth=%d, globalVariables=%d}",
                operandStack.length, callStackDepth, globalVariables.length
            );
        }
    }
    
    // ==================== 事件类（简化，实际应使用common包中的事件） ====================
    
    /**
     * 执行状态变化事件
     */
    private static class ExecutionStateChangedEvent extends VMEvent {
        private final VMState.ExecutionState oldState;
        private final VMState.ExecutionState newState;
        private final String reason;
        
        public ExecutionStateChangedEvent(Object source, int stepNumber,
                                         VMState.ExecutionState oldState,
                                         VMState.ExecutionState newState,
                                         String reason) {
            super(source, EventType.EXECUTION_STATE_CHANGED, stepNumber);
            this.oldState = oldState;
            this.newState = newState;
            this.reason = reason;
        }
        
        @Override
        public String getDescription() {
            return String.format("执行状态变化: %s -> %s (%s)", oldState, newState, reason);
        }
    }
    
    /**
     * 执行开始事件
     */
    private static class ExecutionStartedEvent extends VMEvent {
        public ExecutionStartedEvent(Object source, int stepNumber) {
            super(source, EventType.EXECUTION_STARTED, stepNumber);
        }
        
        @Override
        public String getDescription() {
            return "执行开始";
        }
    }
    
    /**
     * 执行完成事件
     */
    private static class ExecutionFinishedEvent extends VMEvent {
        private final String reason;
        
        public ExecutionFinishedEvent(Object source, int stepNumber, String reason) {
            super(source, EventType.EXECUTION_FINISHED, stepNumber);
            this.reason = reason;
        }
        
        @Override
        public String getDescription() {
            return String.format("执行完成: %s", reason);
        }
    }
    
    /**
     * 执行错误事件
     */
    private static class ExecutionErrorEvent extends VMEvent {
        private final Exception error;
        
        public ExecutionErrorEvent(Object source, int stepNumber, Exception error) {
            super(source, EventType.EXECUTION_ERROR, stepNumber);
            this.error = error;
        }
        
        @Override
        public String getDescription() {
            return String.format("执行错误: %s", error.getMessage());
        }
    }
    
    /**
     * 操作高亮事件
     */
    private static class OperationHighlightedEvent extends VMEvent {
        private final String description;
        private final int pc;
        private final String instruction;
        
        public OperationHighlightedEvent(Object source, int stepNumber,
                                        String description, int pc, String instruction) {
            super(source, EventType.OPERATION_HIGHLIGHTED, stepNumber);
            this.description = description;
            this.pc = pc;
            this.instruction = instruction;
        }
        
        @Override
        public String getDescription() {
            return String.format("高亮操作: %s (PC: %d, 指令: %s)", description, pc, instruction);
        }
    }
    
    /**
     * 表达式求值事件
     */
    private static class ExpressionEvaluationEvent extends VMEvent {
        private final String expression;
        private final List<EvaluationStep> steps;
        
        public ExpressionEvaluationEvent(Object source, int stepNumber,
                                        String expression, List<EvaluationStep> steps) {
            super(source, EventType.EXPRESSION_EVALUATION, stepNumber);
            this.expression = expression;
            this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
        }
        
        @Override
        public String getDescription() {
            return String.format("表达式求值: %s (步骤数: %d)", expression, steps.size());
        }
    }
    
    /**
     * 寄存器分配可视化事件
     */
    private static class RegisterAllocationVisualizationEvent extends VMEvent {
        private final List<LiveInterval> intervals;
        
        public RegisterAllocationVisualizationEvent(Object source, int stepNumber,
                                                   List<LiveInterval> intervals) {
            super(source, EventType.REGISTER_ALLOCATION_VISUALIZATION, stepNumber);
            this.intervals = intervals != null ? new ArrayList<>(intervals) : new ArrayList<>();
        }
        
        @Override
        public String getDescription() {
            return String.format("寄存器分配可视化 (活跃区间数: %d)", intervals.size());
        }
    }
    
    /**
     * 性能比较事件
     */
    private static class PerformanceComparisonEvent extends VMEvent {
        private final String code;
        private final PerformanceMetrics metrics;
        
        public PerformanceComparisonEvent(Object source, int stepNumber,
                                        String code, PerformanceMetrics metrics) {
            super(source, EventType.PERFORMANCE_COMPARISON, stepNumber);
            this.code = code;
            this.metrics = metrics;
        }
        
        @Override
        public String getDescription() {
            return String.format("性能比较: %s", metrics.getVmType());
        }
    }
    
    // ==================== EventPublisher 实现 ====================
    
    @Override
    public <T extends VMEvent> void publish(T event) {
        eventBus.publish(event);
    }
    
    @Override
    public String getPublisherId() {
        return "StackVMVisualAdapter";
    }
    
    @Override
    public EventBus getEventBus() {
        return eventBus;
    }
    
    @Override
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}