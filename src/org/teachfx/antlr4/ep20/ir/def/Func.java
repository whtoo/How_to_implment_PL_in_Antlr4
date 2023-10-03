package org.teachfx.antlr4.ep20.ir.def;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.stmt.Stmt;
import org.teachfx.antlr4.ep20.symtab.MethodSymbol;

import java.util.List;

public class Func extends Define {
    /// filed
    public String funcName;
    private int args;
    private int locals;
    public List<Stmt> body;

    public Func(String funcName, MethodSymbol symbol , List<Stmt> body) {
        // init
        this.funcName = funcName;
        this.args = symbol.getMembers().size();
        this.body = body;
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

    public void setArgs(int args) {
        this.args = args;
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
}
