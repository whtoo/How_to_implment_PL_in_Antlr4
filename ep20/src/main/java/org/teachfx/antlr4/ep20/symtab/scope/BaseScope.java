package org.teachfx.antlr4.ep20.symtab.scope;

import org.teachfx.antlr4.ep20.ast.stmt.ScopeType;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep20.symtab.symbol.Symbol;
import org.teachfx.antlr4.ep20.symtab.type.Type;
import org.teachfx.antlr4.ep20.symtab.type.TypeTable;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseScope implements Scope {
    Scope enclosingScope;
    Map<String, Symbol> symbols = new LinkedHashMap<>();

    public BaseScope(Scope parent) {
        this.enclosingScope = parent;
        define(TypeTable.INT);
        define(TypeTable.FLOAT);
        define(TypeTable.VOID);
        define(TypeTable.BOOLEAN);
        var printFn = new MethodSymbol("print", TypeTable.VOID, this, null);
        printFn.builtin = true;
        define(printFn);
    }

    @Override
    public Type lookup(String name) {
        return (Type) resolve(name);
    }

    @Override
    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if (s != null) return s;
        if (enclosingScope != null) return enclosingScope.resolve(name);
        return null;
    }

    @Override
    public void define(Symbol sym) {
        symbols.put(sym.name, sym);
        sym.scope = this;
    }

    @Override
    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public String toString() {
        return getScopeName() + symbols.keySet();
    }

    @Override
    public void setParentScope(Scope currentScope) {
        this.enclosingScope = currentScope;
    }

    protected ScopeType scopeType;
    @Override
    public ScopeType getScopeType() {
        return scopeType;
    }

    @Override
    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }
}
