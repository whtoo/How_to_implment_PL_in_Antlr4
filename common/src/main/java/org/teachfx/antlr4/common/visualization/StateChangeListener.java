package org.teachfx.antlr4.common.visualization;

import java.util.EventListener;

/**
 * 状态变化监听器接口
 * 
 * <p>该接口定义了虚拟机状态变化的监听方法，
 * 包括执行状态、程序计数器、内存状态等各种状态变化的回调。</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段一
 */
public interface StateChangeListener extends EventListener {
    
    /**
     * 虚拟机状态发生变化时调用
     * 
     * @param oldState 旧状态
     * @param newState 新状态
     */
    default void vmStateChanged(VMState.ExecutionState oldState, VMState.ExecutionState newState) {}
    
    /**
     * 程序计数器发生变化时调用
     * 
     * @param oldPC 旧的程序计数器值
     * @param newPC 新的程序计数器值
     */
    default void programCounterChanged(int oldPC, int newPC) {}
    
    /**
     * 栈状态发生变化时调用
     * 
     * @param stackType 栈类型（如操作数栈、调用栈等）
     * @param oldSize 旧的栈大小
     * @param newSize 新的栈大小
     */
    default void stackChanged(String stackType, int oldSize, int newSize) {}
    
    /**
     * 寄存器状态发生变化时调用
     * 
     * @param registerNumber 寄存器编号
     * @param oldValue 旧的寄存器值
     * @param newValue 新的寄存器值
     */
    default void registerChanged(int registerNumber, Object oldValue, Object newValue) {}
    
    /**
     * 内存状态发生变化时调用
     * 
     * @param address 内存地址
     * @param oldValue 旧的内存值
     * @param newValue 新的内存值
     */
    default void memoryChanged(int address, Object oldValue, Object newValue) {}
    
    /**
     * 变量值发生变化时调用
     * 
     * @param variableName 变量名
     * @param oldValue 旧的变量值
     * @param newValue 新的变量值
     * @param scope 变量作用域
     */
    default void variableChanged(String variableName, Object oldValue, Object newValue, String scope) {}
    
    /**
     * 函数调用栈发生变化时调用
     * 
     * @param action 变化类型（PUSH、POP、MODIFY）
     * @param stackFrame 栈帧信息
     */
    default void callStackChanged(StackChangeAction action, StackFrame<?> stackFrame) {}
    
    /**
     * 断点状态发生变化时调用
     * 
     * @param pc 断点位置
     * @param enabled 是否启用
     */
    default void breakpointChanged(int pc, boolean enabled) {}
    
    /**
     * 内存使用量发生变化时调用
     * 
     * @param oldUsage 旧的内存使用量
     * @param newUsage 新的内存使用量
     */
    default void memoryUsageChanged(long oldUsage, long newUsage) {}
    
    /**
     * 垃圾回收状态发生变化时调用
     * 
     * @param active 是否正在执行垃圾回收
     * @param phase 垃圾回收阶段
     */
    default void garbageCollectionStateChanged(boolean active, String phase) {}
    
    /**
     * 虚拟机重置时调用
     */
    default void vmReset() {}
    
    /**
     * 执行模式发生变化时调用
     * 
     * @param oldMode 旧的执行模式
     * @param newMode 新的执行模式
     */
    default void executionModeChanged(ExecutionMode oldMode, ExecutionMode newMode) {}
    
    /**
     * 性能指标状态发生变化时调用
     * 
     * @param metricType 指标类型
     * @param oldValue 旧值
     * @param newValue 新值
     */
    default void performanceMetricChanged(String metricType, Object oldValue, Object newValue) {}
    
    /**
     * 栈变化动作枚举
     */
    enum StackChangeAction {
        /**
         * 压栈
         */
        PUSH,
        
        /**
         * 弹栈
         */
        POP,
        
        /**
         * 修改
         */
        MODIFY
    }
    
    /**
     * 执行模式枚举
     */
    enum ExecutionMode {
        /**
         * 单步模式
         */
        STEP,
        
        /**
         * 连续执行模式
         */
        CONTINUOUS,
        
        /**
         * 调试模式
         */
        DEBUG,
        
        /**
         * 性能分析模式
         */
        PROFILING
    }
}