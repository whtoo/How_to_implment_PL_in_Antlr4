package org.teachfx.antlr4.ep20.ir.expr;


import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.addr.StackSlot;

import java.util.List;

public class CallFunc extends Expr {

    protected String funcName;
    protected int args;
    public CallFunc(String funcName,int args) {
        this.funcName = funcName;
        this.args = args;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public int getArgs() {
        return args;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "call " + funcName + "(args:" + args + ")";
    }
}
