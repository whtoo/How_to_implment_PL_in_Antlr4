package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Expr;

public class Assign extends Stmt {
    protected Expr lhs;
    protected Expr rhs;

    public Assign(Expr lhs, Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }


    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
