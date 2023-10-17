package org.teachfx.antlr4.ep20.ir.stmt;


import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.VarSlot;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;

import java.util.Objects;

public class ReturnVal extends Stmt {
    public Label retFuncLabel;
    private VarSlot retVal;
    private boolean isMainEntry = false;
    public ReturnVal(VarSlot retVal, Scope scope) {
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

    public VarSlot getRetVal() {
        return retVal;
    }

    public void setRetVal(VarSlot retVal) {
        this.retVal = retVal;
    }

    public boolean isMainEntry() {
        return isMainEntry;
    }

    public void setMainEntry(boolean mainEntry) {
        isMainEntry = mainEntry;
    }

    @Override
    public String toString() {
        var retText = isMainEntry()?"halt":"ret";
        var retValText = "";
        if (Objects.nonNull(retVal)){
            retValText = "load" + retVal.toString() + "\n";
        }
        return  retValText + retText;
    }
}
