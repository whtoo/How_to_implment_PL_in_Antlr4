package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.VarSlot;

public class ExprStmt extends Stmt {
    private VarSlot varSlot;
    public ExprStmt(VarSlot varSlot)
    {
        this.varSlot = varSlot;
    }

    @Override
    public String toString()
    {
        return varSlot.toString();
    }

    public VarSlot getExpr() {
        return varSlot;
    }

    public void setExpr(VarSlot varSlot) {
        this.varSlot = varSlot;
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
