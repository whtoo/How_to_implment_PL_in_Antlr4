package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;

public class JMP extends Stmt
{
    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public JMP(Label label)
    {
        this.label = label;
    }
    public Label label;

    @Override
    public StmtType getStmtType() {
        return StmtType.JMP;
    }
}
