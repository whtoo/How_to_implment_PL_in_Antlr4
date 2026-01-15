package org.teachfx.antlr4.common.visualization;

import java.util.EventListener;
import java.util.List;

/**
 * 执行事件监听器接口
 * 
 * <p>该接口定义了虚拟机执行过程中的各种事件监听方法，
 * 包括指令执行、状态变化、错误处理等事件的回调。</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段一
 */
public interface ExecutionListener extends EventListener {
    
    /**
     * 指令执行前调用
     * 
     * @param pc 程序计数器位置
     * @param instruction 即将执行的指令
     */
    default void beforeInstructionExecute(int pc, String instruction) {}
    
    /**
     * 指令执行后调用
     * 
     * @param pc 程序计数器位置
     * @param instruction 已执行的指令
     * @param result 执行结果（如果有的话）
     */
    default void afterInstructionExecute(int pc, String instruction, Object result) {}
    
    /**
     * 虚拟机开始执行时调用
     */
    default void executionStarted() {}
    
    /**
     * 虚拟机停止执行时调用
     * 
     * @param reason 停止原因
     */
    default void executionStopped(String reason) {}
    
    /**
     * 虚拟机暂停执行时调用
     */
    default void executionPaused() {}
    
    /**
     * 虚拟机恢复执行时调用
     */
    default void executionResumed() {}
    
    /**
     * 函数调用开始时调用
     * 
     * @param functionName 函数名
     * @param arguments 参数列表
     */
    default void functionCallStarted(String functionName, List<Object> arguments) {}
    
    /**
     * 函数调用结束时调用
     * 
     * @param functionName 函数名
     * @param returnValue 返回值
     */
    default void functionCallFinished(String functionName, Object returnValue) {}
    
    /**
     * 发生执行错误时调用
     * 
     * @param exception 执行异常
     */
    default void executionError(VMExecutionException exception) {}
    
    /**
     * 发生执行警告时调用
     * 
     * @param message 警告消息
     * @param pc 程序计数器位置
     */
    default void executionWarning(String message, int pc) {}
    
    /**
     * 断点命中时调用
     * 
     * @param pc 断点位置
     * @param instruction 当前指令
     */
    default void breakpointHit(int pc, String instruction) {}
    
    /**
     * 内存分配时调用
     * 
     * @param address 分配的地址
     * @param size 分配的大小
     */
    default void memoryAllocated(int address, int size) {}
    
    /**
     * 内存释放时调用
     * 
     * @param address 释放的地址
     * @param size 释放的大小
     */
    default void memoryFreed(int address, int size) {}
    
    /**
     * 垃圾回收开始时调用
     * 
     * @param gcType 垃圾回收类型
     */
    default void garbageCollectionStarted(String gcType) {}
    
    /**
     * 垃圾回收结束时调用
     * 
     * @param gcType 垃圾回收类型
     * @param collectedObjects 回收的对象数量
     * @param freedMemory 释放的内存大小
     */
    default void garbageCollectionFinished(String gcType, int collectedObjects, long freedMemory) {}
    
    /**
     * 性能指标更新时调用
     * 
     * @param metrics 性能指标
     */
    default void performanceMetricsUpdated(PerformanceMetrics metrics) {}
}