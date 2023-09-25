package org.teachfx.antlr4.ep20.ast.expr;

import org.teachfx.antlr4.ep20.debugger.Dumper;

abstract public class LiteralNode<T> extends ExprNode {

    protected T rawValue;

    public T getRawValue() {
        return rawValue;
    }

    public void setRawValue(T rawValue) {
        this.rawValue = rawValue;
    }


}
