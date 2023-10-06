package org.teachfx.antlr4.ep8;

public class IntNode extends ASTNode {

    Integer intValue;

    public IntNode(String text) {
        super(text);
        this.intValue = Integer.parseInt(text);

    }

    public Integer getIntValue() {
        return this.intValue;
    }

    public String toString() {
        return " ( INT " + this.getRawText() + " ) ";
    }
}
