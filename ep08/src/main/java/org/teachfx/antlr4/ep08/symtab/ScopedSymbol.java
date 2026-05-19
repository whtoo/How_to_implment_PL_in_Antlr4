package org.teachfx.antlr4.ep08.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ScopedSymbol extends Symbol implements Scope {
    Scope enclosingScope;

    public ScopedSymbol(String name, Type type, Scope enclosingScope) {
        super(name, type);
        this.enclosingScope = enclosingScope;
    }

    public ScopedSymbol(String name, Scope enclosingScope) {
        super(name);
        this.enclosingScope = enclosingScope;
    }

    @Override
    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public String getScopeName() {
        return name;
    }

    public abstract Map<String, Symbol> getMembers();

    @Override
    public Symbol resolve(String name) {
        Symbol s = getMembers().get(name);
        if (s != null) return s;
        if (enclosingScope != null) return enclosingScope.resolve(name);
        return null;
    }

    @Override
    public void define(Symbol sym) {
        getMembers().put(sym.name, sym);
        sym.scope = this;
    }
}
