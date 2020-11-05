package org.teachfx.antlr4.ep13.ast;

public class VarNode extends ExpressionNode {
    public String name;
    public VarNode(String name) {
        super();
        this.name = name;
    }    
}
