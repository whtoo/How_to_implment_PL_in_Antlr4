package org.teachfx.antlr4.ep18.stackvm;

/**
 * VMInvalidOpcodeException - 无效操作码异常
 * 用于处理遇到未定义或不支持的操作码
 */
public class VMInvalidOpcodeException extends VMInstructionException {

    public VMInvalidOpcodeException(String message, int pc, int opcode) {
        super(message, pc, "unknown", opcode);
    }

    public VMInvalidOpcodeException(int pc, int opcode) {
        this(String.format("Invalid or unsupported opcode: %d (0x%02X)", opcode, opcode), pc, opcode);
    }

    public VMInvalidOpcodeException(int pc, int opcode, int maxValidOpcode) {
        this(String.format("Invalid opcode: %d (0x%02X). Valid range: [0, %d]", opcode, opcode, maxValidOpcode), pc, opcode);
    }
}