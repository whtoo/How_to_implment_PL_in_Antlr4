package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep20.ir.IRVisitor;

public class JMP extends Stmt
{
    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public JMP(BasicBlock block)
    {
        this.next = block;
    }
    public BasicBlock next;

    @Override
    public StmtType getStmtType() {
        return StmtType.JMP;
    }

    @Override
    public String toString() {
        return "jmp %s".formatted(next);
    }
}
