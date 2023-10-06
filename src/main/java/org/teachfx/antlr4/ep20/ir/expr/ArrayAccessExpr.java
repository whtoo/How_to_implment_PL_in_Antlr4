package org.teachfx.antlr4.ep20.ir.expr;


import org.teachfx.antlr4.ep20.ir.IRVisitor;

public class ArrayAccessExpr extends Expr {

    private Expr arrayExpr;
    private Expr indexExpr;

    public Expr getArrayExpr() {
        return arrayExpr;

    }

    public Expr getIndexExpr() {
        return indexExpr;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
