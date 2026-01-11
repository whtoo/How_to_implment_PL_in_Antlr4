package org.teachfx.antlr4.ep18r.stackvm.exception;

import org.teachfx.antlr4.ep18r.stackvm.ErrorCode;

/**
 * VMDivisionByZeroException - 除零异常
 */
public class VMDivisionByZeroException extends VMException {
    public VMDivisionByZeroException(int pc, String instruction) {
        super(ErrorCode.DIVISION_BY_ZERO, pc, instruction);
    }

    public VMDivisionByZeroException(int pc, String instruction, String details) {
        super(ErrorCode.DIVISION_BY_ZERO, pc, instruction, details);
    }
}