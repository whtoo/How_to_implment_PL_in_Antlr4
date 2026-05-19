package org.teachfx.antlr4.ep11.ir;

/**
 * Abstract base class for all IR (Intermediate Representation) instructions.
 * Each subclass represents one three-address-code (TAC) instruction.
 */
public abstract class IRInstruction {

    /** Print this instruction in human-readable TAC form. */
    public abstract String toString();

    /**
     * Returns true if this instruction transfers control flow
     * (jump, branch, return), marking the end of a basic block.
     */
    public boolean isControlTransfer() {
        return false;
    }

    /**
     * Returns true if this instruction is a label,
     * marking the start of a basic block.
     */
    public boolean isLabel() {
        return false;
    }
}
