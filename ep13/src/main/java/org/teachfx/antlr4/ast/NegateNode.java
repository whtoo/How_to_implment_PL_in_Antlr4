package org.teachfx.antlr4.ep13.ast;

public class NegateNode extends ExpressionNode {
    public ExpressionNode innerNode;

    public NegateNode() {
        super();
    }

    public NegateNode(ExpressionNode innerNode) {
        super();
        this.innerNode = innerNode;
    }
}
           