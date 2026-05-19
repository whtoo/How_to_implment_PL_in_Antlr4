package org.teachfx.antlr4.ep08.ir;

/**
 * ReturnIR: return from a function, optionally with a value.
 * TAC: return val  (or just "return" if val is null)
 */
public class ReturnIR extends IRInstruction {
    public final String value;  // null if returning void

    public ReturnIR(String value) {
        this.value = value;
    }

    @Override
    public boolean isControlTransfer() {
        return true;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "return";
        }
        return "return " + value;
    }
}
