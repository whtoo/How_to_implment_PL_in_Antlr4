package org.teachfx.antlr4.ep11.ir;

/**
 * LabelIR: a named label marking a position in the instruction stream.
 * Labels are targets for jumps and branches.
 */
public class LabelIR extends IRInstruction {
    public final String label;

    public LabelIR(String label) {
        this.label = label;
    }

    @Override
    public boolean isLabel() {
        return true;
    }

    @Override
    public String toString() {
        return label + ":";
    }
}
