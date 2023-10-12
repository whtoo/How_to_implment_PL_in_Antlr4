package org.teachfx.antlr4.ep20.ir;

import org.teachfx.antlr4.ep20.ir.expr.Expr;
import org.teachfx.antlr4.ep20.ir.expr.Var;

public class LValue extends IRNode {
    protected Expr exprVal;
    protected Var varVal;

    protected String name;

    public LValue(Expr exprVal, Var varVal, String name) {
        this.exprVal = exprVal;
        this.varVal = varVal;
        this.name = name;
    }

}
