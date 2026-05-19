package org.teachfx.antlr4.ep07.symtab;

import java.util.*;

public class SymbolTable implements Scope {
    private final String scopeName;
    private final Scope enclosingScope;
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();
    private final List<Symbol> allSymbols = new ArrayList<>();

    public SymbolTable(String scopeName, Scope enclosingScope) {
        this.scopeName = scopeName;
        this.enclosingScope = enclosingScope;
    }

    @Override public String getScopeName() { return scopeName; }
    @Override public Scope getEnclosingScope() { return enclosingScope; }

    @Override
    public void define(String name, Symbol sym) {
        symbols.put(name, sym);
        allSymbols.add(sym);
    }

    @Override
    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if (s != null) return s;
        if (enclosingScope != null) return enclosingScope.resolve(name);
        return null;
    }

    public List<Symbol> getAllSymbols() { return allSymbols; }
    public Map<String, Symbol> getSymbols() { return symbols; }
}
