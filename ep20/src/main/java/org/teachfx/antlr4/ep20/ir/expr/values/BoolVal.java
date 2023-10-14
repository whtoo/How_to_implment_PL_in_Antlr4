package org.teachfx.antlr4.ep20.ir.expr.values;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Expr;

public class BoolVal extends Expr {
    public boolean value;

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public BoolVal(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BoolVal(" + value + ")";
    }

}
