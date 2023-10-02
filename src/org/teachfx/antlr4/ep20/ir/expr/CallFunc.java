package org.teachfx.antlr4.ep20.ir.expr;

import org.teachfx.antlr4.ep20.ir.IRVisitor;

public class CallFunc extends Expr{
    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
