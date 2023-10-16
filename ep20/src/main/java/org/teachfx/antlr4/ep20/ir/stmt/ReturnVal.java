package org.teachfx.antlr4.ep20.ir.stmt;


import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Expr;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;

public class ReturnVal extends Stmt {
    public Label retFuncLabel;
    private Expr retVal;
    private boolean isMainEntry = false;
    public ReturnVal(Expr retVal, Scope scope) {
        retFuncLabel = new Label(null,scope);
        retFuncLabel.setNextEntry(this);
        this.retVal = retVal;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {

        return visitor.visit(this);
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
