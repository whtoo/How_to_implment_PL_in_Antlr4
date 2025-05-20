package org.teachfx.antlr4.ep19.symtab.scope;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep19.symtab.Type;
import org.teachfx.antlr4.ep19.symtab.symbol.Symbol;

import java.util.Map;


public abstract class ScopedSymbol extends Symbol implements Scope {
    public ParserRuleContext tree;
    Scope enclosingScope;

    public ScopedSymbol(String name, Type type, Scope enclosingScope) {
        super(name, type);
        this.enclosingScope = enclosingScope;
    }

    public ScopedSymbol(String name, Scope enclosingScope, ParserRuleContext tree) {
        super(name);
        this.enclosingScope = enclosingScope;
        this.tree = tree;
    }

    @Override
    public Type lookup(String name) {
        Symbol symbol = resolve(name);
        if (symbol != null && symbol.type instanceof Type) {
            return symbol.type;
        }
        return null;
    }

    @Override
    public Symbol resolve(String name) {
        Symbol s = getMembers().get(name);
        if (s != null) return s;
        if (getEnclosingScope() != null) {
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
        getMembers().put(sym.getName(), sym);
        sym.scope = this;
    }

    public abstract Map<String, Symbol> getMembers();
}
