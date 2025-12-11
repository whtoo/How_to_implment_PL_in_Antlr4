package org.teachfx.antlr4.ep18.stackvm;

/**
 * VMDivisionByZeroException - 除零异常
 */
public class VMDivisionByZeroException extends VMException {
    public VMDivisionByZeroException(String message, int pc, String instruction) {
        super(message, pc, instruction);
    }

    public VMDivisionByZeroException(int pc, String instruction) {
        this("Division by zero", pc, instruction);
    }
}