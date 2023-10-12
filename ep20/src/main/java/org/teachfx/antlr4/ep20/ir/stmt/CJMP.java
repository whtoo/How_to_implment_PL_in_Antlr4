package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Expr;

public class CJMP extends Stmt {
    public Expr cond;

    public Label thenLabel;
    public Label elseLabel;


    public CJMP(Expr cond,Label thenLabel,Label elseLabel) {
        this.cond = cond;
        this.thenLabel = thenLabel;
        this.elseLabel = elseLabel;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.CJMP;
    }
}
