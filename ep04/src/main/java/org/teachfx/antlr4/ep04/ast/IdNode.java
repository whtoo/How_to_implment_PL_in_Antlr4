package org.teachfx.antlr4.ep04.ast;

/** Represents a variable reference, e.g. x. */
public class IdNode extends ASTNode {
    public final String name;

    public IdNode(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "Id(" + name + ")");
    }
}
