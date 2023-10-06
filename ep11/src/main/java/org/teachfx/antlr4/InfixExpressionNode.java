package org.teachfx.antlr4.ep11;

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
