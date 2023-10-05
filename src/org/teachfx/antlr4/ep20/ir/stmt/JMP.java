package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;

// TODO: Impletement JMP in code generation phase
public class JMP extends Stmt
{
    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public JMP(String label)
    {
        this.label = label;
    }
    public String label;

    @Override
    public StmtType getStmtType() {
        return StmtType.JMP;
    }
}
