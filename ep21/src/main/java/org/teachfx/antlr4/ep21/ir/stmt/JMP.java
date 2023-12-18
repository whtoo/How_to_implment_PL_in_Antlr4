package org.teachfx.antlr4.ep21.ir.stmt;

import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.JMPInstr;
import org.teachfx.antlr4.ep21.pass.cfg.LinearIRBlock;

public class JMP extends Stmt implements JMPInstr
{
    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public JMP(@NotNull LinearIRBlock block)
    {
        this.next = block;
        block.refJMP(this);
    }
    private LinearIRBlock next;

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

    public void setNext(LinearIRBlock next) {
        this.next = next;
        next.refJMP(this);
    }

    public LinearIRBlock getNext() {
        return this.next;
    }
}
