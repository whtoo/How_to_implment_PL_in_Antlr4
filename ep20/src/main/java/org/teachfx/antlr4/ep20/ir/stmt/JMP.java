package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.JMPInstr;
import org.teachfx.antlr4.ep20.pass.cfg.LinearIRBlock;

public class JMP extends Stmt implements JMPInstr
{
    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public JMP(LinearIRBlock block)
    {
        this.next = block;
        block.refJMP(this);
    }
    public LinearIRBlock next;

    @Override
    public Label getTarget() {
        return next.getLabel();
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.JMP;
    }

    @Override
    public String toString() {
        return "jmp %s".formatted(next);
    }
}
