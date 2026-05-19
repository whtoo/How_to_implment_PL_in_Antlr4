package org.teachfx.antlr4.ep04.ast;

/** Represents an integer literal, e.g. 42. */
public class IntNode extends ASTNode {
    public final int value;

    public IntNode(int value) {
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "Int(" + value + ")");
    }
}
