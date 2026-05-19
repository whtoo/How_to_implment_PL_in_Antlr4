package org.teachfx.antlr4.ep11.ir;

/**
 * BinaryOpIR: a binary arithmetic or comparison operation.
 * TAC: result = lhs op rhs
 *
 * op can be +, -, *, /, %, ==, !=, <, >, <=, >=, &&
 */
public class BinaryOpIR extends IRInstruction {
    public final String result;
    public final String lhs;
    public final String rhs;
    public final String op;

    public BinaryOpIR(String result, String lhs, String op, String rhs) {
        this.result = result;
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return result + " = " + lhs + " " + op + " " + rhs;
    }
}
