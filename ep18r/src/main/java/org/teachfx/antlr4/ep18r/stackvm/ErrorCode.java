package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 错误代码枚举
 * 统一所有虚拟机错误的分类和标识
 */
public enum ErrorCode {
    // 算术错误
    DIVISION_BY_ZERO("DIV_ZERO", "Division by zero"),

    // 操作码错误
    INVALID_OPCODE("INV_OP", "Invalid opcode"),

    // 寄存器错误
    INVALID_REGISTER("INV_REG", "Invalid register number"),

    // 内存错误
    INVALID_ADDRESS("INV_ADDR", "Invalid memory address"),
    MEMORY_OUT_OF_BOUNDS("MEM_OOB", "Memory access out of bounds"),

    // 栈错误
    STACK_OVERFLOW("STK_OF", "Stack overflow"),
    STACK_UNDERFLOW("STK_UF", "Stack underflow"),

    // 控制流错误
    INVALID_JUMP_TARGET("JMP_INV", "Invalid jump target"),
    INFINITE_LOOP("INF_LOOP", "Infinite loop detected"),

    // 执行错误
    EXECUTION_STEPS_EXCEEDED("EXEC_LIMIT", "Maximum execution steps exceeded"),
    UNSUPPORTED_OPERATION("UNSUP_OP", "Unsupported operation"),

    // 类型错误
    TYPE_MISMATCH("TYPE_MISMATCH", "Type mismatch"),
    NULL_POINTER("NULL_PTR", "Null pointer access"),

    // 其他错误
    INTERNAL_ERROR("INT_ERR", "Internal error"),
    RUNTIME_ERROR("RUNTIME", "Runtime error");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取错误代码（简短标识）
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取错误描述
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
