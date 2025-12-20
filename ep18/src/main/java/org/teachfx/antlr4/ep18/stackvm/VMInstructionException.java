package org.teachfx.antlr4.ep18.stackvm;

/**
 * VMInstructionException - 指令执行异常
 * 用于处理指令执行过程中的各种错误
 */
public class VMInstructionException extends VMException {
    protected final int opcode;
    protected final Object[] operands;

    public VMInstructionException(String message, int pc, String instruction, int opcode, Object... operands) {
        super(message, pc, instruction, formatInstructionDetails(opcode, operands));
        this.opcode = opcode;
        this.operands = operands != null ? operands.clone() : new Object[0];
    }

    private static String formatInstructionDetails(int opcode, Object[] operands) {
        StringBuilder sb = new StringBuilder();
        sb.append("Instruction execution failed\n");
        sb.append("  Opcode: ").append(opcode);
        if (operands != null && operands.length > 0) {
            sb.append("\n  Operands: ");
            for (int i = 0; i < operands.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(operands[i]);
            }
        }
        return sb.toString();
    }

    public int getOpcode() {
        return opcode;
    }

    public Object[] getOperands() {
        return operands != null ? operands.clone() : new Object[0];
    }
}