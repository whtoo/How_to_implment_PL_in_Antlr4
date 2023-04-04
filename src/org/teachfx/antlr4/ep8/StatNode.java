package org.teachfx.antlr4.ep8;

public class StatNode extends ASTNode {
    ASTNode node;

    public StatNode(String rawText) {
        super(rawText);
        // TODO Auto-generated constructor stub
    }

    public StatNode(ASTNode node) {
        super("");
        this.node = node;
    }


}
