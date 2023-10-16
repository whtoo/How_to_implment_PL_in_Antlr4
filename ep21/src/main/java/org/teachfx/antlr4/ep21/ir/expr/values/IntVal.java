package org.teachfx.antlr4.ep21.ir.expr.values;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;

public class IntVal extends Expr {

    public int value;

    public IntVal(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "IntVal [value=" + value + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntVal other = (IntVal) obj;
        if (value != other.value)
            return false;
        return true;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
