package org.teachfx.antlr4.ep18.stackvm;

/**
 * VMRuntimeException - 虚拟机运行时异常
 * 用于处理虚拟机运行时的各种错误，如栈溢出/下溢、类型错误等
 */
public class VMRuntimeException extends VMException {

    public VMRuntimeException(String message, int pc, String instruction) {
        super(message, pc, instruction);
    }

    public VMRuntimeException(String message, int pc, String instruction, String details) {
        super(message, pc, instruction, details);
    }

    public VMRuntimeException(String message) {
        super(message, -1, null);
    }
}