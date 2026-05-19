package org.teachfx.antlr4.ep05.symtab;

import java.util.*;

public class FunctionSymbol extends Symbol implements Scope {
    private final Scope enclosingScope;
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

    public FunctionSymbol(String name, String typeName, String scopeName, int line,
                          Scope enclosingScope) {
        super(name, Kind.FUNCTION, typeName, scopeName, line);
        this.enclosingScope = enclosingScope;
    }

    @Override public String getScopeName() { return name; }
    @Override public Scope getEnclosingScope() { return enclosingScope; }

    @Override
    public void define(String name, Symbol sym) {
        symbols.put(name, sym);
    }

    @Override
    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if (s != null) return s;
        if (enclosingScope != null) return enclosingScope.resolve(name);
        return null;
    }

    public Map<String, Symbol> getSymbols() { return symbols; }
}
