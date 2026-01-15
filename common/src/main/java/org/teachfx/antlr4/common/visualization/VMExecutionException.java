package org.teachfx.antlr4.common.visualization;

/**
 * 虚拟机执行异常类
 * 
 * <p>该异常表示虚拟机执行过程中发生的错误，包括指令执行错误、
 * 内存访问错误、栈溢出等各种运行时异常。</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段一
 */
public class VMExecutionException extends Exception {
    
    /**
     * 错误代码
     */
    private final ErrorCode errorCode;
    
    /**
     * 程序计数器位置
     */
    private final int programCounter;
    
    /**
     * 异常发生的指令
     */
    private final String currentInstruction;
    
    /**
     * 异常的严重级别
     */
    private final Severity severity;
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public VMExecutionException(String message) {
        this(message, ErrorCode.UNKNOWN, -1, null, Severity.ERROR);
    }
    
    /**
     * 完整构造函数
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param programCounter 程序计数器位置
     * @param currentInstruction 当前指令
     * @param severity 严重级别
     */
    public VMExecutionException(String message, ErrorCode errorCode, int programCounter,
                           String currentInstruction, Severity severity) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : ErrorCode.UNKNOWN;
        this.programCounter = programCounter;
        this.currentInstruction = currentInstruction;
        this.severity = severity != null ? severity : Severity.ERROR;
    }
    
    /**
     * 带原因的构造函数
     * 
     * @param message 错误消息
     * @param cause 原因异常
     */
    public VMExecutionException(String message, Throwable cause) {
        this(message, ErrorCode.UNKNOWN, -1, null, Severity.ERROR, cause);
    }
    
    /**
     * 完整构造函数（包含原因）
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param programCounter 程序计数器位置
     * @param currentInstruction 当前指令
     * @param severity 严重级别
     * @param cause 原因异常
     */
    public VMExecutionException(String message, ErrorCode errorCode, int programCounter,
                           String currentInstruction, Severity severity, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : ErrorCode.UNKNOWN;
        this.programCounter = programCounter;
        this.currentInstruction = currentInstruction;
        this.severity = severity != null ? severity : Severity.ERROR;
    }
    
    // ==================== Getters ====================
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取程序计数器位置
     * 
     * @return 程序计数器位置
     */
    public int getProgramCounter() {
        return programCounter;
    }
    
    /**
     * 获取当前指令
     * 
     * @return 当前指令
     */
    public String getCurrentInstruction() {
        return currentInstruction;
    }
    
    /**
     * 获取严重级别
     * 
     * @return 严重级别
     */
    public Severity getSeverity() {
        return severity;
    }
    
    /**
     * 检查是否有程序计数器信息
     * 
     * @return 如果有PC信息返回true
     */
    public boolean hasProgramCounter() {
        return programCounter >= 0;
    }
    
    /**
     * 检查是否有当前指令信息
     * 
     * @return 如果有指令信息返回true
     */
    public boolean hasCurrentInstruction() {
        return currentInstruction != null && !currentInstruction.isEmpty();
    }
    
    /**
     * 获取格式化的错误信息
     * 
     * @return 格式化的错误字符串
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(severity).append("] ");
        sb.append(errorCode).append(": ").append(getMessage());
        
        if (hasProgramCounter()) {
            sb.append(" (at PC=").append(programCounter).append(")");
        }
        
        if (hasCurrentInstruction()) {
            sb.append(" [instruction: ").append(currentInstruction).append("]");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getFormattedMessage();
    }
    
    /**
     * 创建栈溢出异常
     * 
     * @param programCounter 程序计数器
     * @param currentInstruction 当前指令
     * @return VM执行异常
     */
    public static VMExecutionException createStackOverflow(int programCounter, String currentInstruction) {
        return new VMExecutionException(
            "Stack overflow occurred",
            ErrorCode.STACK_OVERFLOW,
            programCounter,
            currentInstruction,
            Severity.FATAL
        );
    }
    
    /**
     * 创建栈下溢异常
     * 
     * @param programCounter 程序计数器
     * @param currentInstruction 当前指令
     * @return VM执行异常
     */
    public static VMExecutionException createStackUnderflow(int programCounter, String currentInstruction) {
        return new VMExecutionException(
            "Stack underflow occurred",
            ErrorCode.STACK_UNDERFLOW,
            programCounter,
            currentInstruction,
            Severity.ERROR
        );
    }
    
    /**
     * 创建除零异常
     * 
     * @param programCounter 程序计数器
     * @param currentInstruction 当前指令
     * @return VM执行异常
     */
    public static VMExecutionException createDivisionByZero(int programCounter, String currentInstruction) {
        return new VMExecutionException(
            "Division by zero",
            ErrorCode.DIVISION_BY_ZERO,
            programCounter,
            currentInstruction,
            Severity.ERROR
        );
    }
    
    /**
     * 创建无效指令异常
     * 
     * @param opcode 操作码
     * @param programCounter 程序计数器
     * @return VM执行异常
     */
    public static VMExecutionException createInvalidOpcode(int opcode, int programCounter) {
        return new VMExecutionException(
            "Invalid opcode: " + opcode,
            ErrorCode.INVALID_OPCODE,
            programCounter,
            "opcode=" + opcode,
            Severity.ERROR
        );
    }
    
    /**
     * 创建内存访问异常
     * 
     * @param address 访问地址
     * @param programCounter 程序计数器
     * @param currentInstruction 当前指令
     * @return VM执行异常
     */
    public static VMExecutionException createMemoryAccessViolation(int address, int programCounter, String currentInstruction) {
        return new VMExecutionException(
            "Memory access violation at address: " + address,
            ErrorCode.MEMORY_ACCESS_VIOLATION,
            programCounter,
            currentInstruction,
            Severity.ERROR
        );
    }
    
    /**
     * 创建类型不匹配异常
     * 
     * @param expectedType 期望类型
     * @param actualType 实际类型
     * @param programCounter 程序计数器
     * @param currentInstruction 当前指令
     * @return VM执行异常
     */
    public static VMExecutionException createTypeMismatch(String expectedType, String actualType,
                                                   int programCounter, String currentInstruction) {
        return new VMExecutionException(
            "Type mismatch: expected " + expectedType + ", got " + actualType,
            ErrorCode.TYPE_MISMATCH,
            programCounter,
            currentInstruction,
            Severity.ERROR
        );
    }
    
    /**
     * 错误代码枚举
     */
    public enum ErrorCode {
        /**
         * 未知错误
         */
        UNKNOWN,
        
        /**
         * 栈溢出
         */
        STACK_OVERFLOW,
        
        /**
         * 栈下溢
         */
        STACK_UNDERFLOW,
        
        /**
         * 除零错误
         */
        DIVISION_BY_ZERO,
        
        /**
         * 无效操作码
         */
        INVALID_OPCODE,
        
        /**
         * 内存访问违规
         */
        MEMORY_ACCESS_VIOLATION,
        
        /**
         * 类型不匹配
         */
        TYPE_MISMATCH,
        
        /**
         * 函数调用错误
         */
        FUNCTION_CALL_ERROR,
        
        /**
         * 变量未定义
         */
        UNDEFINED_VARIABLE,
        
        /**
         * 数组越界
         */
        ARRAY_INDEX_OUT_OF_BOUNDS,
        
        /**
         * 空指针访问
         */
        NULL_POINTER_ACCESS,
        
        /**
         * 算术溢出
         */
        ARITHMETIC_OVERFLOW,
        
        /**
         * 垃圾回收错误
         */
        GARBAGE_COLLECTION_ERROR,
        
        /**
         * 配置错误
         */
        CONFIGURATION_ERROR
    }
    
    /**
     * 严重级别枚举
     */
    public enum Severity {
        /**
         * 警告级别
         */
        WARNING,
        
        /**
         * 错误级别
         */
        ERROR,
        
        /**
         * 致命错误级别
         */
        FATAL
    }
}