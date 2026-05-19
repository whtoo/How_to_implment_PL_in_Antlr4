package org.teachfx.antlr4.ep04.ast;

/** Represents a print statement: print expr; */
public class PrintNode extends ASTNode {
    public final ASTNode expr;

    public PrintNode(ASTNode expr) {
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "Print");
        expr.print(indent + "  ");
    }
}
