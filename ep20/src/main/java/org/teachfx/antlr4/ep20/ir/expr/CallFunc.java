package org.teachfx.antlr4.ep20.ir.expr;


import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.values.Var;

import java.util.List;

public class CallFunc extends Expr{
    protected List<Expr> args;
    protected Var funcExpr;

    public CallFunc(Var funcExpr,List<Expr> args) {
        this.funcExpr = funcExpr;
        this.args = args;
    }

    public Var getFuncExpr() {
        return funcExpr;
    }

    public void setFuncExpr(Var funcExpr) {
        this.funcExpr = funcExpr;
    }

    public List<Expr> getArgs() {
        return args;
    }

    public void setArgs(List<Expr> args) {
        this.args = args;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
