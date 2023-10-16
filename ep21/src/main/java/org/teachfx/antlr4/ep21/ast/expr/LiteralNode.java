package org.teachfx.antlr4.ep21.ast.expr;

import java.util.Objects;

abstract public class LiteralNode<T> extends ExprNode {

    protected T rawValue;

    public T getRawValue() {
        return rawValue;
    }

    public void setRawValue(T rawValue) {
        this.rawValue = rawValue;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiteralNode<?> that)) return false;
        return Objects.equals(getRawValue(), that.getRawValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRawValue());
    }
}
