package org.teachfx.antlr4.ep12.ast;

public class NumberNode extends ExpressionNode {
    public double value;

    public NumberNode(double value) {
        this.value = value;
    }
}
           