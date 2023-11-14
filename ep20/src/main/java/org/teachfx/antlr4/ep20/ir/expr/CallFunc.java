package org.teachfx.antlr4.ep20.ir.expr;


import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;

public class CallFunc extends Expr {
    protected MethodSymbol funcType;
    protected String funcName;
    protected int args;
    public CallFunc(String funcName,int args,MethodSymbol funcType) {
        this.funcName = funcName;
        this.args = args;
        this.funcType = funcType;
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

    public MethodSymbol getFuncType() {
        return funcType;
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
