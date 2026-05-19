package org.teachfx.antlr4.ep08.ir;

/**
 * AssignIR: copies a value (variable, constant, or temp) into a variable.
 * TAC: lhs = rhs
 */
public class AssignIR extends IRInstruction {
    public final String lhs;
    public final String rhs;

    public AssignIR(String lhs, String rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return lhs + " = " + rhs;
    }
}
