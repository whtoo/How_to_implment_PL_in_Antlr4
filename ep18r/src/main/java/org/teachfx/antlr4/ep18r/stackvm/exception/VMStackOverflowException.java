package org.teachfx.antlr4.ep18r.stackvm.exception;

import org.teachfx.antlr4.ep18r.stackvm.ErrorCode;

/**
 * VMStackOverflowException - 栈溢出异常
 */
public class VMStackOverflowException extends VMException {
    public VMStackOverflowException(String message, int pc, String instruction) {
        super(message, pc, instruction);
    }

    public VMStackOverflowException(String message) {
        this(message, -1, null);
    }

    public VMStackOverflowException(ErrorCode code, int pc, String instruction) {
        super(code, pc, instruction);
    }
}