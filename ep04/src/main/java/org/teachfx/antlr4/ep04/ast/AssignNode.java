package org.teachfx.antlr4.ep04.ast;

/** Represents assignment: name = value. */
public class AssignNode extends ASTNode {
    public final String name;
    public final ASTNode value;

    public AssignNode(String name, ASTNode value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "Assign(" + name + ")");
        value.print(indent + "  ");
    }
}
