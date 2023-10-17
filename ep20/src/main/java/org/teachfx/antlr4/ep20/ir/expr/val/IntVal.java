package org.teachfx.antlr4.ep20.ir.expr.val;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.ImmValue;

public class IntVal<T> extends ImmValue {
    private T val;

    public IntVal(T val) {
        this.val = val;
    }

    public static <T> IntVal<T> valueOf(T val) {
        return new IntVal<>(val);
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public T getVal() {
        return val;
    }

    public void setVal(T val) {
        this.val = val;
    }

    @Override
    public String toString() {
        if(val instanceof String) {
            return " %s ".formatted(val);
        } else if (val instanceof Integer) {
            return " %d ".formatted(val);
        } else if (val instanceof Boolean) {
            return " %b ".formatted(val);
        }

        return "val(%s)".formatted(val.toString());
    }
}
