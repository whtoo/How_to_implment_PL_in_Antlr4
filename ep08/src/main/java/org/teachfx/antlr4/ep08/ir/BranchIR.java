package org.teachfx.antlr4.ep08.ir;

/**
 * BranchIR: conditional branch on a boolean value.
 * TAC: if cond goto trueLabel else falseLabel
 */
public class BranchIR extends IRInstruction {
    public final String cond;
    public final String trueLabel;
    public final String falseLabel;

    public BranchIR(String cond, String trueLabel, String falseLabel) {
        this.cond = cond;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    @Override
    public boolean isControlTransfer() {
        return true;
    }

    @Override
    public String toString() {
        return "if " + cond + " goto " + trueLabel + " else " + falseLabel;
    }
}
