package org.teachfx.antlr4.ep21.ir.expr.values;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;

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
