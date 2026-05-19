package org.teachfx.antlr4.ep04.ast;

/** Represents a binary operation: left OP right. */
public class BinaryOpNode extends ASTNode {
    public final ASTNode left;
    public final ASTNode right;
    public final String op;  // "+", "-", "*", "/"

    public BinaryOpNode(ASTNode left, String op, ASTNode right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "BinaryOp(" + op + ")");
        left.print(indent + "  ");
        right.print(indent + "  ");
    }
}
