package org.teachfx.antlr4.ep21.ir.def;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.stmt.ReturnVal;
import org.teachfx.antlr4.ep21.ir.stmt.Stmt;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;

import java.util.List;

public class Func extends Define {
    /// filed
    public String funcName;
    private int locals;
    public List<Stmt> body;
    public ReturnVal retHook;

    public Func(String funcName, MethodSymbol symbol , List<Stmt> body) {
        // init
        this.funcName = funcName;
        this.symbol = symbol;
        this.body = body;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public int getArgs() {
        return getFuncSymbol().getMembers().size();
    }
    protected MethodSymbol getFuncSymbol() {
        return (MethodSymbol)symbol;
    }
    public int getLocals() {
        return locals;
    }

    public void setLocals(int locals) {
        this.locals = locals;
    }

    public List<Stmt> getBody() {
        return body;
    }

    public void setBody(List<Stmt> body) {
        this.body = body;
    }

    @Override
    public String getDeclName() {
        return getFuncName();
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public String toSource() {
        return ".def %s: args=%d ,locals=%d".formatted(getFuncName(), getFuncSymbol().getArgs(),getFuncSymbol().getMembers().size() -  getFuncSymbol().getArgs());
    }
}
