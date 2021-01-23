package org.teachfx.antlr4.ep12.ast;

public class AssignNode extends ExpressionNode {
    public String varName;
    public ExpressionNode value;
    public AssignNode(String varName,ExpressionNode value) {
        super();
        this.varName = varName;
        this.value = value;
    }

}
