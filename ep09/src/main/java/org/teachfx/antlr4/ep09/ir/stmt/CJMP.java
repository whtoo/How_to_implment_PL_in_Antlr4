package org.teachfx.antlr4.ep09.ir.stmt;

import org.teachfx.antlr4.ep09.ir.IRVisitor;
import org.teachfx.antlr4.ep09.ir.JMPInstr;
import org.teachfx.antlr4.ep09.ir.expr.VarSlot;

public class CJMP extends Stmt implements JMPInstr {
    public VarSlot cond;
    private String thenLabel;
    private String elseLabel;


    public CJMP(VarSlot cond, String thenLabel, String elseLabel) {
        this.cond = cond;
        this.thenLabel = thenLabel;
        this.elseLabel = elseLabel;
    }

    @Override
    public Label getTarget() {
        return new Label(elseLabel, null);
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.CJMP;
    }

    @Override
    public String toString() {
        return "jmpIf %s,%s,%s".formatted(cond,thenLabel,elseLabel);
    }

    public void setElseBlock(String elseLabel) {
        this.elseLabel = elseLabel;
    }

    public void setThenBlock(String thenLabel) {
        this.thenLabel = thenLabel;
    }

    public String getElseBlock() {
        return elseLabel;
    }

    public String getThenBlock() {
        return thenLabel;
    }
}
