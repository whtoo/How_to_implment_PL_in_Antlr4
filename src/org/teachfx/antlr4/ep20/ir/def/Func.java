package org.teachfx.antlr4.ep20.ir.def;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.stmt.Stmt;
import org.teachfx.antlr4.ep20.symtab.MethodSymbol;

import java.util.List;

public class Func extends Def {
    /// filed
    public String funcName;
    public int args;
    public int locals;
    public List<Stmt> body;

    public Func(String funcName, MethodSymbol symbol , List<Stmt> body) {
        // init
        this.funcName = funcName;
        this.args = symbol.getMembers().size();
        this.locals = locals;
        this.body = body;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
