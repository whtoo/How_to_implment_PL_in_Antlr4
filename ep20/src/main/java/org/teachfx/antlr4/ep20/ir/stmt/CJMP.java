package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.VarSlot;

public class CJMP extends Stmt {
    public VarSlot cond;

    private BasicBlock thenBlock;
    private BasicBlock elseBlock;


    public CJMP(VarSlot cond, BasicBlock thenLabel, BasicBlock elseLabel) {
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

    public void setElseBlock(BasicBlock elseBlock) {
        this.elseBlock = elseBlock;
        elseBlock.refJMP(this);
    }

    public void setThenBlock(BasicBlock thenBlock) {
        this.thenBlock = thenBlock;
        thenBlock.refJMP(this);
    }

    public BasicBlock getElseBlock() {
        return elseBlock;
    }

    public BasicBlock getThenBlock() {
        return thenBlock;
    }
}
