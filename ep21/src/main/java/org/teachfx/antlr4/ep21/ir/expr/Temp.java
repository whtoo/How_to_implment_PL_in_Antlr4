package org.teachfx.antlr4.ep21.ir.expr;

import org.teachfx.antlr4.ep21.ir.IRVisitor;

public class Temp extends Expr{

    public Temp() {
        super();
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
