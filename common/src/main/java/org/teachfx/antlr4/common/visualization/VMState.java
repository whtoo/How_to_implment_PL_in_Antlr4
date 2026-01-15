package org.teachfx.antlr4.common.visualization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 虚拟机统一状态模型
 * 
 * <p>该类提供虚拟机状态的统一表示，支持EP18栈式VM和EP18R寄存器VM
 * 的不同状态信息。使用泛型确保类型安全。</p>
 * 
 * @param <T> 虚拟机特定状态类型
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段一
 */
public class VMState<T> {
    
    // ==================== 共享状态 ====================
    
    /**
     * 程序计数器
     */
    private volatile int programCounter;
    
    /**
     * 执行状态
     */
    private volatile ExecutionState executionState;
    
    /**
     * 当前执行的指令
     */
    private volatile String currentInstruction;
    
    /**
     * 执行指令数
     */
    private volatile long instructionCount;
    
    /**
     * 执行开始时间
     */
    private final long startTime;
    
    /**
     * 最后更新时间
     */
    private volatile long lastUpdateTime;
    
    // ==================== 特定状态 ====================
    
    /**
     * VM特定状态信息
     */
    private final T vmSpecificState;
    
    /**
     * 额外的状态属性
     */
    private final Map<String, Object> additionalProperties;
    
    /**
     * 构造函数
     * 
     * @param programCounter 程序计数器
     * @param executionState 执行状态
     * @param vmSpecificState VM特定状态
     */
    public VMState(int programCounter, ExecutionState executionState, T vmSpecificState) {
        this.programCounter = programCounter;
        this.executionState = executionState;
        this.vmSpecificState = vmSpecificState;
        this.startTime = System.currentTimeMillis();
        this.lastUpdateTime = startTime;
        this.additionalProperties = new ConcurrentHashMap<>();
        this.currentInstruction = "";
        this.instructionCount = 0;
    }
    
    // ==================== Getters and Setters ====================
    
    /**
     * 获取程序计数器
     * 
     * @return 程序计数器值
     */
    public int getProgramCounter() {
        return programCounter;
    }
    
    /**
     * 设置程序计数器
     * 
     * @param programCounter 新的程序计数器值
     */
    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
        updateTimestamp();
    }
    
    /**
     * 获取执行状态
     * 
     * @return 当前执行状态
     */
    public ExecutionState getExecutionState() {
        return executionState;
    }
    
    /**
     * 设置执行状态
     * 
     * @param executionState 新的执行状态
     */
    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;
        updateTimestamp();
    }
    
    /**
     * 获取当前指令
     * 
     * @return 当前指令字符串
     */
    public String getCurrentInstruction() {
        return currentInstruction;
    }
    
    /**
     * 设置当前指令
     * 
     * @param currentInstruction 当前指令字符串
     */
    public void setCurrentInstruction(String currentInstruction) {
        this.currentInstruction = currentInstruction;
        updateTimestamp();
    }
    
    /**
     * 获取指令执行计数
     * 
     * @return 已执行的指令数
     */
    public long getInstructionCount() {
        return instructionCount;
    }
    
    /**
     * 增加指令执行计数
     * 
     * @param increment 增量值
     */
    public void incrementInstructionCount(long increment) {
        this.instructionCount += increment;
        updateTimestamp();
    }
    
    /**
     * 获取执行开始时间
     * 
     * @return 开始时间戳（毫秒）
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * 获取最后更新时间
     * 
     * @return 最后更新时间戳（毫秒）
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    /**
     * 获取VM特定状态
     * 
     * @return VM特定状态对象
     */
    public T getVmSpecificState() {
        return vmSpecificState;
    }
    
    /**
     * 获取额外属性
     * 
     * @return 额外属性映射
     */
    public Map<String, Object> getAdditionalProperties() {
        return new ConcurrentHashMap<>(additionalProperties);
    }
    
    /**
     * 设置额外属性
     * 
     * @param key 属性键
     * @param value 属性值
     */
    public void setAdditionalProperty(String key, Object value) {
        if (key != null) {
            additionalProperties.put(key, value);
            updateTimestamp();
        }
    }
    
    /**
     * 获取额外属性
     * 
     * @param key 属性键
     * @return 属性值，如果不存在则返回null
     */
    public Object getAdditionalProperty(String key) {
        return key != null ? additionalProperties.get(key) : null;
    }
    
    /**
     * 获取执行运行时间
     * 
     * @return 从开始到现在的时间差（毫秒）
     */
    public long getExecutionTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * 更新时间戳
     */
    private void updateTimestamp() {
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 创建状态的深拷贝
     * 
     * @return VMState的拷贝
     */
    public VMState<T> copy() {
        VMState<T> copy = new VMState<>(programCounter, executionState, vmSpecificState);
        copy.currentInstruction = this.currentInstruction;
        copy.instructionCount = this.instructionCount;
        copy.additionalProperties.putAll(this.additionalProperties);
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format(
            "VMState{pc=%d, state=%s, instruction='%s', count=%d, time=%dms}",
            programCounter, executionState, currentInstruction, instructionCount, getExecutionTime()
        );
    }
    
    /**
     * 执行状态枚举
     */
    public enum ExecutionState {
        /**
         * 未初始化
         */
        UNINITIALIZED,
        
        /**
         * 准备就绪
         */
        READY,
        
        /**
         * 运行中
         */
        RUNNING,
        
        /**
         * 暂停
         */
        PAUSED,
        
        /**
         * 已停止
         */
        STOPPED,
        
        /**
         * 错误状态
         */
        ERROR
    }
}