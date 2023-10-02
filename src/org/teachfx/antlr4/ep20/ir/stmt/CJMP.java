package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Expr;

public class CJMP extends Stmt {
    public Expr cond;
    public Stmt thenStmt;
    public Stmt elseStmt;

    public CJMP(Expr cond, Stmt thenStmt, Stmt elseStmt) {
        this.cond = cond;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
