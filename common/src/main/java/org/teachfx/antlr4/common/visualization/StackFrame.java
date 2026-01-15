package org.teachfx.antlr4.common.visualization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 栈帧抽象类（两种虚拟机通用）
 * 
 * <p>该类提供函数调用栈帧的统一抽象表示，支持EP18栈式VM和EP18R寄存器VM。
 * 使用泛型确保类型安全和扩展性。</p>
 * 
 * @param <T> 局部变量类型（栈式VM使用Object[]，寄存器VM使用Map）
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段一
 */
public abstract class StackFrame<T> {
    
    // ==================== 基本信息 ====================
    
    /**
     * 函数名称
     */
    protected final String functionName;
    
    /**
     * 返回地址（程序计数器位置）
     */
    protected final int returnAddress;
    
    /**
     * 栈帧基地址
     */
    protected final int frameBasePointer;
    
    /**
     * 函数参数数量
     */
    protected final int parameterCount;
    
    /**
     * 局部变量数量
     */
    protected final int localVariableCount;
    
    // ==================== 局部数据 ====================
    
    /**
     * 局部变量存储
     */
    protected T localVariables;
    
    /**
     * 额外的帧信息
     */
    protected final Map<String, Object> frameInfo;
    
    /**
     * 构造函数
     * 
     * @param functionName 函数名称
     * @param returnAddress 返回地址
     * @param frameBasePointer 栈帧基地址
     * @param parameterCount 参数数量
     * @param localVariableCount 局部变量数量
     */
    protected StackFrame(String functionName, int returnAddress, int frameBasePointer,
                       int parameterCount, int localVariableCount) {
        this.functionName = functionName != null ? functionName : "<anonymous>";
        this.returnAddress = returnAddress;
        this.frameBasePointer = frameBasePointer;
        this.parameterCount = parameterCount;
        this.localVariableCount = localVariableCount;
        this.frameInfo = new ConcurrentHashMap<>();
    }
    
    // ==================== Getters ====================
    
    /**
     * 获取函数名称
     * 
     * @return 函数名称
     */
    public String getFunctionName() {
        return functionName;
    }
    
    /**
     * 获取返回地址
     * 
     * @return 返回地址
     */
    public int getReturnAddress() {
        return returnAddress;
    }
    
    /**
     * 获取栈帧基地址
     * 
     * @return 栈帧基地址
     */
    public int getFrameBasePointer() {
        return frameBasePointer;
    }
    
    /**
     * 获取参数数量
     * 
     * @return 参数数量
     */
    public int getParameterCount() {
        return parameterCount;
    }
    
    /**
     * 获取局部变量数量
     * 
     * @return 局部变量数量
     */
    public int getLocalVariableCount() {
        return localVariableCount;
    }
    
    /**
     * 获取局部变量存储
     * 
     * @return 局部变量存储对象
     */
    public T getLocalVariables() {
        return localVariables;
    }
    
    /**
     * 获取帧信息
     * 
     * @return 帧信息映射
     */
    public Map<String, Object> getFrameInfo() {
        return new ConcurrentHashMap<>(frameInfo);
    }
    
    // ==================== 抽象方法 ====================
    
    /**
     * 设置局部变量值
     * 
     * @param index 变量索引
     * @param value 变量值
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public abstract void setLocalVariable(int index, Object value) throws IndexOutOfBoundsException;
    
    /**
     * 获取局部变量值
     * 
     * @param index 变量索引
     * @return 变量值
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public abstract Object getLocalVariable(int index) throws IndexOutOfBoundsException;
    
    /**
     * 获取参数值
     * 
     * @param index 参数索引
     * @return 参数值
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public abstract Object getParameter(int index) throws IndexOutOfBoundsException;
    
    /**
     * 设置参数值
     * 
     * @param index 参数索引
     * @param value 参数值
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public abstract void setParameter(int index, Object value) throws IndexOutOfBoundsException;
    
    /**
     * 获取栈帧的字符串表示
     * 
     * @return 栈帧描述字符串
     */
    public abstract String getStackFrameDescription();
    
    // ==================== 实用方法 ====================
    
    /**
     * 设置帧信息
     * 
     * @param key 信息键
     * @param value 信息值
     */
    public void setFrameInfo(String key, Object value) {
        if (key != null) {
            frameInfo.put(key, value);
        }
    }
    
    /**
     * 获取帧信息
     * 
     * @param key 信息键
     * @return 信息值，如果不存在则返回null
     */
    public Object getFrameInfo(String key) {
        return key != null ? frameInfo.get(key) : null;
    }
    
    /**
     * 验证索引有效性
     * 
     * @param index 要验证的索引
     * @param maxValue 最大有效值
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    protected void validateIndex(int index, int maxValue) throws IndexOutOfBoundsException {
        if (index < 0 || index >= maxValue) {
            throw new IndexOutOfBoundsException(
                String.format("Index %d is out of bounds [0, %d)", index, maxValue)
            );
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "StackFrame{function='%s', returnAddr=%d, basePtr=%d, params=%d, locals=%d}",
            functionName, returnAddress, frameBasePointer, parameterCount, localVariableCount
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        StackFrame<?> that = (StackFrame<?>) obj;
        return returnAddress == that.returnAddress &&
               frameBasePointer == that.frameBasePointer &&
               functionName.equals(that.functionName);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(functionName, returnAddress, frameBasePointer);
    }
}