package org.teachfx.antlr4.ep20.ir.expr;

import org.teachfx.antlr4.ep20.ir.IRVisitor;

public class ClassAccessExpr extends Expr
{
    private Expr expr;
    private String name;

    public ClassAccessExpr(Expr expr, String name) {
        this.expr = expr;
        this.name = name;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return "ClassAccessExpr [expr=" + expr + ", name=" + name + "]";
    }
}
