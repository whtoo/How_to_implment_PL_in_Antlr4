package org.teachfx.antlr4.common.visualization.event;

/**
 * 事件类型枚举
 * 定义统一可视化框架支持的所有事件类型
 */
public enum EventType {
    
    INSTRUCTION_EXECUTED("指令执行事件"),
    EXECUTION_STARTED("执行开始事件"),
    EXECUTION_PAUSED("执行暂停事件"),
    EXECUTION_RESUMED("执行恢复事件"),
    EXECUTION_FINISHED("执行完成事件"),
    EXECUTION_ERROR("执行错误事件"),
    
    REGISTER_CHANGED("寄存器变化事件"),
    MEMORY_CHANGED("内存变化事件"),
    PROGRAM_COUNTER_CHANGED("程序计数器变化事件"),
    STACK_CHANGED("栈变化事件"),
    CALL_STACK_CHANGED("调用栈变化事件"),
    EXECUTION_STATE_CHANGED("执行状态变化事件"),
    
    EDUCATIONAL_HINT("教育提示事件"),
    OPERATION_HIGHLIGHTED("操作高亮事件"),
    EXPRESSION_EVALUATION("表达式求值事件"),
    REGISTER_ALLOCATION_VISUALIZATION("寄存器分配可视化事件"),
    PERFORMANCE_COMPARISON("性能比较事件"),
    
    BREAKPOINT_HIT("断点命中事件"),
    STEP_COMPLETED("单步完成事件"),
    WATCH_TRIGGERED("监视点触发事件");
    
    private final String description;
    
    EventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 检查是否为执行事件
     */
    public boolean isExecutionEvent() {
        return this == INSTRUCTION_EXECUTED || 
               this == EXECUTION_STARTED ||
               this == EXECUTION_PAUSED ||
               this == EXECUTION_RESUMED ||
               this == EXECUTION_FINISHED ||
               this == EXECUTION_ERROR;
    }
    
    /**
     * 检查是否为状态变化事件
     */
    public boolean isStateChangeEvent() {
        return this == REGISTER_CHANGED ||
               this == MEMORY_CHANGED ||
               this == PROGRAM_COUNTER_CHANGED ||
               this == STACK_CHANGED ||
               this == CALL_STACK_CHANGED ||
               this == EXECUTION_STATE_CHANGED;
    }
    
    /**
     * 检查是否为教育功能事件
     */
    public boolean isEducationalEvent() {
        return this == EDUCATIONAL_HINT ||
               this == OPERATION_HIGHLIGHTED ||
               this == EXPRESSION_EVALUATION ||
               this == REGISTER_ALLOCATION_VISUALIZATION ||
               this == PERFORMANCE_COMPARISON;
    }
    
    /**
     * 检查是否为系统事件
     */
    public boolean isSystemEvent() {
        return this == BREAKPOINT_HIT ||
               this == STEP_COMPLETED ||
               this == WATCH_TRIGGERED;
    }
}