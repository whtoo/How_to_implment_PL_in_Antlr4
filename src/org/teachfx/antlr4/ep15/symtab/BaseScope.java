package org.teachfx.antlr4.ep15.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseScope implements Scope {
    Scope enclosingScope;
    Map<String, Symbol> symbols = new LinkedHashMap<>();

    public BaseScope(Scope parent) {
        this.enclosingScope = parent;
        define(new BuiltInTypeSymbol("int"));
        define(new BuiltInTypeSymbol("float"));
        define(new BuiltInTypeSymbol("void"));
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
}
