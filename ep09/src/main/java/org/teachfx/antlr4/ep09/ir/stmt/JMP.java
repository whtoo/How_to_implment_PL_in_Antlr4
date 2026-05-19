package org.teachfx.antlr4.ep09.ir.stmt;

import org.teachfx.antlr4.ep09.ir.IRVisitor;
import org.teachfx.antlr4.ep09.ir.JMPInstr;

public class JMP extends Stmt implements JMPInstr
{
    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public JMP(String targetLabel)
    {
        this.targetLabel = targetLabel;
    }
    private String targetLabel;

    @Override
    public Label getTarget() {
        return new Label(targetLabel, null);
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.JMP;
    }

    @Override
    public String toString() {
        return "jmp %s".formatted(targetLabel);
    }

    public void setNext(String targetLabel) {
        this.targetLabel = targetLabel;
    }

    public String getNext() {
        return this.targetLabel;
    }
}
