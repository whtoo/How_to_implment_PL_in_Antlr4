package org.teachfx.antlr4.common.visualization;

import io.reactivex.rxjava3.core.Observable;
import org.teachfx.antlr4.common.visualization.event.VMEvent;
import org.teachfx.antlr4.common.visualization.event.events.InstructionExecutedEvent;
import org.teachfx.antlr4.common.visualization.event.events.RegisterChangedEvent;
import org.teachfx.antlr4.common.visualization.event.events.MemoryChangedEvent;
import org.teachfx.antlr4.common.visualization.event.events.ProgramCounterChangedEvent;
import org.teachfx.antlr4.common.visualization.event.events.ExecutionStateChangedEvent;
import org.teachfx.antlr4.common.visualization.event.events.ExecutionStartedEvent;
import org.teachfx.antlr4.common.visualization.event.events.ExecutionFinishedEvent;
import org.teachfx.antlr4.common.visualization.event.events.BreakpointHitEvent;
import org.teachfx.antlr4.common.visualization.event.events.EducationalHintEvent;

/**
 * 统一的虚拟机可视化接口
 * 
 * <p>该接口为EP18栈式VM和EP18R寄存器VM提供统一的可视化抽象，
 * 支持状态获取、执行控制、教育功能和事件监听等功能。</p>
 * 
 * <p>实现类应确保线程安全，支持多线程环境下的并发访问。</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段一
 */
public interface IVirtualMachineVisualization {
    
    // ==================== 状态获取 ====================
    
    /**
     * 获取虚拟机当前状态
     * 
     * @return 包含完整状态信息的VMState对象
     * @throws IllegalStateException 如果虚拟机处于无效状态
     */
    VMState getCurrentState() throws IllegalStateException;
    
    /**
     * 反汇编指定PC位置的指令
     * 
     * @param pc 程序计数器位置
     * @return 指令的反汇编字符串表示
     * @throws IllegalArgumentException 如果PC位置无效
     * @throws IndexOutOfBoundsException 如果PC超出代码范围
     */
    String disassembleInstruction(int pc) throws IllegalArgumentException, IndexOutOfBoundsException;
    
    /**
     * 获取当前调用栈
     * 
     * @return 调用栈帧列表，从主函数到当前函数的调用链
     */
    java.util.List<StackFrame> getCallStack();
    
    /**
     * 获取教育提示信息
     * 
     * @return 当前状态下的教育提示文本
     */
    String getEducationalHint();
    
    // ==================== 执行控制 ====================
    
    /**
     * 单步执行一条指令
     * 
     * @throws VMExecutionException 如果执行过程中发生错误
     * @throws IllegalStateException 如果虚拟机未准备就绪
     */
    void step() throws VMExecutionException, IllegalStateException;
    
    /**
     * 开始连续执行
     * 
     * @throws VMExecutionException 如果执行过程中发生错误
     * @throws IllegalStateException 如果虚拟机未准备就绪
     */
    void run() throws VMExecutionException, IllegalStateException;
    
    /**
     * 暂停执行
     *
     * @throws IllegalStateException 如果虚拟机未在运行
     */
    void pause() throws IllegalStateException;

    /**
     * 停止执行并重置状态
     *
     * @throws IllegalStateException 如果虚拟机已经停止
     */
    void stop() throws IllegalStateException;

    /**
     * 设置自动步进模式
     *
     * @param autoStepMode true为启用自动步进，false为禁用
     */
    void setAutoStepMode(boolean autoStepMode);

    /**
     * 设置自动步进延迟
     *
     * @param delayMs 每条指令执行后的延迟时间（毫秒）
     */
    void setAutoStepDelay(int delayMs);

    /**
     * 检查是否为自动步进模式
     *
     * @return true表示已启用自动步进，false表示未启用
     */
    boolean isAutoStepMode();

    /**
     * 获取自动步进延迟
     *
     * @return 每条指令执行后的延迟时间（毫秒）
     */
    int getAutoStepDelay();
    
    /**
     * 检查虚拟机是否正在运行
     * 
     * @return true表示正在运行，false表示已停止或暂停
     */
    boolean isRunning();
    
    /**
     * 检查虚拟机是否处于暂停状态
     * 
     * @return true表示已暂停，false表示运行中或已停止
     */
    boolean isPaused();
    
    // ==================== 教育功能 ====================
    
    /**
     * 高亮当前操作
     * 
     * @param description 操作描述文本
     */
    void highlightCurrentOperation(String description);
    
    /**
     * 显示表达式求值过程
     * 
     * @param expression 要求值的表达式
     * @param steps 求值步骤列表
     * @throws IllegalArgumentException 如果表达式或步骤无效
     */
    void showExpressionEvaluation(String expression, java.util.List<EvaluationStep> steps) 
            throws IllegalArgumentException;
    
    /**
     * 可视化寄存器分配
     * 
     * @param intervals 活跃区间列表
     * @throws IllegalArgumentException 如果活跃区间列表无效
     */
    void visualizeRegisterAllocation(java.util.List<LiveInterval> intervals) 
            throws IllegalArgumentException;
    
    /**
     * 与其他VM进行性能比较
     * 
     * @param code 要执行的代码
     * @param metrics 性能指标对象
     * @throws IllegalArgumentException 如果代码或指标无效
     */
    void compareWithOtherVM(String code, PerformanceMetrics metrics) 
            throws IllegalArgumentException;
    
    // ==================== 事件监听 ====================
    
    /**
     * 添加执行监听器
     * 
     * @param listener 执行事件监听器
     * @throws IllegalArgumentException 如果监听器为null
     */
    void addExecutionListener(ExecutionListener listener) throws IllegalArgumentException;
    
    /**
     * 添加状态变化监听器
     * 
     * @param listener 状态变化监听器
     * @throws IllegalArgumentException 如果监听器为null
     */
    void addStateChangeListener(StateChangeListener listener) throws IllegalArgumentException;
    
    /**
     * 添加教育提示监听器
     * 
     * @param listener 教育提示监听器
     * @throws IllegalArgumentException 如果监听器为null
     */
    void addEducationalListener(EducationalHintListener listener) throws IllegalArgumentException;
    
    // ==================== RxJava事件流 ====================
    
    /**
     * 获取所有事件的通用流
     * 
     * @return 所有VMEvent事件的Observable流
     */
    Observable<VMEvent> getEventStream();
    
    /**
     * 获取指令执行事件流
     * 
     * @return InstructionExecutedEvent事件的Observable流
     */
    Observable<InstructionExecutedEvent> getInstructionExecutedStream();
    
    /**
     * 获取寄存器变化事件流
     * 
     * @return RegisterChangedEvent事件的Observable流
     */
    Observable<RegisterChangedEvent> getRegisterChangedStream();
    
    /**
     * 获取内存变化事件流
     * 
     * @return MemoryChangedEvent事件的Observable流
     */
    Observable<MemoryChangedEvent> getMemoryChangedStream();
    
    /**
     * 获取程序计数器变化事件流
     * 
     * @return ProgramCounterChangedEvent事件的Observable流
     */
    Observable<ProgramCounterChangedEvent> getProgramCounterChangedStream();
    
    /**
     * 获取执行状态变化事件流
     * 
     * @return ExecutionStateChangedEvent事件的Observable流
     */
    Observable<ExecutionStateChangedEvent> getExecutionStateChangedStream();
    
    /**
     * 获取执行开始事件流
     * 
     * @return ExecutionStartedEvent事件的Observable流
     */
    Observable<ExecutionStartedEvent> getExecutionStartedStream();
    
    /**
     * 获取执行完成事件流
     * 
     * @return ExecutionFinishedEvent事件的Observable流
     */
    Observable<ExecutionFinishedEvent> getExecutionFinishedStream();
    
    /**
     * 获取断点命中事件流
     * 
     * @return BreakpointHitEvent事件的Observable流
     */
    Observable<BreakpointHitEvent> getBreakpointHitStream();
    
    /**
     * 获取教育提示事件流
     * 
     * @return EducationalHintEvent事件的Observable流
     */
    Observable<EducationalHintEvent> getEducationalHintStream();
}