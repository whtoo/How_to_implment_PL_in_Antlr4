package org.teachfx.antlr4.ep18r.stackvm.exception;

import org.teachfx.antlr4.ep18r.stackvm.ErrorCode;

public class VMInvalidOpcodeException extends VMException {
    public VMInvalidOpcodeException(int pc, String instruction) {
        super(ErrorCode.INVALID_OPCODE.getDescription(), pc, instruction);
    }

    public VMInvalidOpcodeException(int pc, String instruction, String details) {
        super(ErrorCode.INVALID_OPCODE.getDescription(), pc, instruction, details);
    }
}
