package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Var;
import org.teachfx.antlr4.ep20.ir.expr.Expr;

public class Assign extends Stmt {
    protected Var lhs;
    protected Expr rhs;

    public Assign(Var lhs, Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    // Generate getter and setter for lhs and rhs
    public Var getLhs() {
        return lhs;
    }
    public Expr getRhs() {
        return rhs;
    }

    public void setLhs(Var lhs) {
        this.lhs = lhs;
    }

    public void setRhs(Expr rhs) {
        this.rhs = rhs;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.ASSIGN;
    }
}
