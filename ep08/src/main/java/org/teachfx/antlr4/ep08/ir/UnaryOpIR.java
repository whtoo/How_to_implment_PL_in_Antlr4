package org.teachfx.antlr4.ep08.ir;

/**
 * UnaryOpIR: a unary operation (negation, logical not).
 * TAC: result = op operand
 */
public class UnaryOpIR extends IRInstruction {
    public final String result;
    public final String op;
    public final String operand;

    public UnaryOpIR(String result, String op, String operand) {
        this.result = result;
        this.op = op;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return result + " = " + op + " " + operand;
    }
}
