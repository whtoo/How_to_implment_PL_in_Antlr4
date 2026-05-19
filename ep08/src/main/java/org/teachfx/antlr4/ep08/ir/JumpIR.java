package org.teachfx.antlr4.ep08.ir;

/**
 * JumpIR: unconditional jump to a label.
 * TAC: goto target
 */
public class JumpIR extends IRInstruction {
    public final String target;

    public JumpIR(String target) {
        this.target = target;
    }

    @Override
    public boolean isControlTransfer() {
        return true;
    }

    @Override
    public String toString() {
        return "goto " + target;
    }
}
