package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.pass.cfg.LinearIRBlock;
import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.VarSlot;

public class CJMP extends Stmt {
    public VarSlot cond;
    private LinearIRBlock thenBlock;
    private LinearIRBlock elseBlock;


    public CJMP(VarSlot cond, LinearIRBlock thenLabel, LinearIRBlock elseLabel) {
        this.cond = cond;
        this.thenBlock = thenLabel;
        this.elseBlock = elseLabel;
        thenLabel.refJMP(this);
        elseBlock.refJMP(this);
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
        return "jmpIf %s,%s,%s".formatted(cond,thenBlock,elseBlock);
    }

    public void setElseBlock(LinearIRBlock elseBlock) {
        this.elseBlock = elseBlock;
        elseBlock.refJMP(this);
    }

    public void setThenBlock(LinearIRBlock thenBlock) {
        this.thenBlock = thenBlock;
        thenBlock.refJMP(this);
    }

    public LinearIRBlock getElseBlock() {
        return elseBlock;
    }

    public LinearIRBlock getThenBlock() {
        return thenBlock;
    }
}
