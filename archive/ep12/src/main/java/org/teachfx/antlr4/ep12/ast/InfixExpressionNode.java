package org.teachfx.antlr4.ep12.ast;

public abstract class InfixExpressionNode extends ExpressionNode {
    public ExpressionNode left;
    public ExpressionNode right;

    public InfixExpressionNode(ExpressionNode left, ExpressionNode right) {
        this.left = left;
        this.right = right;
    }

    public InfixExpressionNode() {
        this.left = null;
        this.right = null;
    }
}
