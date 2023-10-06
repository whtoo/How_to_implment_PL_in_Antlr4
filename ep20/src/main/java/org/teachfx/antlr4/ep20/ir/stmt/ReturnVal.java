package org.teachfx.antlr4.ep20.ir.stmt;


import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Expr;

public class ReturnVal extends Stmt {
    private Expr retVal;
    private boolean isMainEntry = false;
    public ReturnVal(Expr retVal) {
        this.retVal = retVal;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return null;
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.RETURN;
    }

    public Expr getRetVal() {
        return retVal;
    }

    public void setRetVal(Expr retVal) {
        this.retVal = retVal;
    }

    public boolean isMainEntry() {
        return isMainEntry;
    }

    public void setMainEntry(boolean mainEntry) {
        isMainEntry = mainEntry;
    }
}
