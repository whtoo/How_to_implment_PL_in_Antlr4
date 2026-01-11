package org.teachfx.antlr4.ep18r.stackvm.exception;

/**
 * VMException - 虚拟机异常基类
 * 所有虚拟机运行时异常的父类
 */
public abstract class VMException extends RuntimeException {
    protected final int pc;
    protected final String instruction;
    protected final String details;

    public VMException(String message, int pc, String instruction) {
        this(message, pc, instruction, null);
    }

    public VMException(String message, int pc, String instruction, String details) {
        super(formatMessage(message, pc, instruction, details));
        this.pc = pc;
        this.instruction = instruction;
        this.details = details;
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

    /**
     * 获取简化的错误信息（不包含堆栈跟踪）
     */
    public String getSimpleMessage() {
        return String.format("%s at PC=%d (instruction=%s)", 
            getMessage().split(" at PC=")[0], pc, instruction);
    }
}