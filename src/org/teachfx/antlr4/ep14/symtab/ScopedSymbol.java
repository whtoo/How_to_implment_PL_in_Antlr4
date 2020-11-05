package org.teachfx.antlr4.ep14.symtab;

import java.util.Map;

import org.teachfx.antlr4.ep14.ast.*;


public abstract class ScopedSymbol extends Symbol implements Scope {
    Scope enclosingScope;
    public Ast tree;
    public ScopedSymbol(String name, Type type,Scope enclosingScope) {
        super(name, type);
        this.enclosingScope = enclosingScope;
    }

    public ScopedSymbol(String name, Scope enclosingScope,Ast tree) {
        super(name);
        this.enclosingScope = enclosingScope;
        this.tree = tree;
    }
    @Override
    public Type lookup(String name) {
        return (Type) resolve(name);
    }

    @Override
    public Symbol resolve(String name) {
        Symbol s = getMemebers().get(name);
        if(s != null) return s;
        if(getEnclosingScope() != null) {
            return getEnclosingScope().resolve(name);
        }

        return null;
    }

    public Symbol resolveType(String name) {
        return resolve(name);
    }

    @Override
    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public String getScopeName() {
        return name;
    }
    @Override
    public void define(Symbol sym) {
        getMemebers().put(sym.name, sym);
        sym.scope = this;
    }
    public abstract Map<String,Symbol> getMemebers();
}
