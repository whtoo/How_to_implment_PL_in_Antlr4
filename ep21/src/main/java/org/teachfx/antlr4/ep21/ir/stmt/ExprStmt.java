package org.teachfx.antlr4.ep21.ir.stmt;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;

public class ExprStmt extends Stmt {
    private Expr expr;
    public ExprStmt(Expr expr)
    {
        this.expr = expr;
    }

    @Override
    public String toString()
    {
        return expr.toString();
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.EXPR;
    }
}
