package org.teachfx.antlr4.ep13;

public class AssignNode extends ExpressionNode {
    protected String varName;
    protected ExpressionNode value;
    public AssignNode(String varName,ExpressionNode value) {
        super();
        this.varName = varName;
        this.value = value;
    }

}
