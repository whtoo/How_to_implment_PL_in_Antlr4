package org.teachfx.antlr4.ep18r.stackvm.exception;

import org.teachfx.antlr4.ep18r.stackvm.ErrorCode;

/**
 * VMException - 虚拟机异常基类
 * 所有虚拟机运行时异常的父类
 */
public abstract class VMException extends RuntimeException {
    protected final int pc;
    protected final String instruction;
    protected final String details;
    protected final ErrorCode errorCode;

    public VMException(String message, int pc, String instruction) {
        this(message, pc, instruction, null);
    }

    public VMException(String message, int pc, String instruction, String details) {
        super(formatMessage(message, pc, instruction, details));
        this.pc = pc;
        this.instruction = instruction;
        this.details = details;
        // Legacy constructor without ErrorCode - use null for backwards compatibility
        this.errorCode = null;
    }

    protected VMException(ErrorCode code, int pc, String instruction, String details) {
        super(formatMessage(code.getDescription(), pc, instruction, details));
        this.pc = pc;
        this.instruction = instruction;
        this.details = details;
        this.errorCode = code;
    }

    protected VMException(ErrorCode code, int pc, String instruction) {
        super(formatMessage(code.getDescription(), pc, instruction, null));
        this.pc = pc;
        this.instruction = instruction;
        this.details = null;
        this.errorCode = code;
    }

    protected VMException(ErrorCode code, int pc) {
        super(formatMessage(code.getDescription(), pc, null, null));
        this.pc = pc;
        this.instruction = null;
        this.details = null;
        this.errorCode = code;
    }

    private static String formatMessage(String message, int pc, String instruction, String details) {
        StringBuilder sb = new StringBuilder();
        sb.append("VM Error: ").append(message);
        sb.append(" at PC=").append(pc);
        if (instruction != null) {
            sb.append(" (instruction=").append(instruction).append(")");
        }
        if (details != null) {
            sb.append("\n  Details: ").append(details);
        }
        return sb.toString();
    }

    public int getPC() {
        return pc;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getDetails() {
        return details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 获取简化的错误信息（不包含堆栈跟踪）
     */
    public String getSimpleMessage() {
        return String.format("%s at PC=%d (instruction=%s)",
            getMessage().split(" at PC=")[0], pc, instruction);
    }
}
