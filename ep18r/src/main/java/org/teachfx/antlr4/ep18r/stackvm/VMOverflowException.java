package org.teachfx.antlr4.ep18r.stackvm;

/**
 * VMOverflowException - 溢出异常
 */
public class VMOverflowException extends VMException {
    public VMOverflowException(String message, int pc, String instruction) {
        super(message, pc, instruction);
    }

    public VMOverflowException(int pc, String instruction) {
        this("Arithmetic overflow", pc, instruction);
    }
}