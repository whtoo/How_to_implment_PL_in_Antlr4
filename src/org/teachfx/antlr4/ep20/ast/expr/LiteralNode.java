package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;

abstract public class LiteralNode<T> extends ExprNode {

    protected T rawValue;

    public T getRawValue() {
        return rawValue;
    }

    public void setRawValue(T rawValue) {
        this.rawValue = rawValue;
    }


    @Override
    public void setCtx(ParserRuleContext ctx) {
        this.ctx = ctx;
    }
}
