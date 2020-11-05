package org.teachfx.antlr4.ep14.symtab;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable implements Scope {
    Map<String,Symbol> symbols; 
    static Type UNDEFINED;

    public SymbolTable() {
        symbols = new HashMap<>();
        initTypeSystem();
    }
    private void initTypeSystem() {
        symbols.put("int", new BuiltIntTypeSymbol("int"));
        symbols.put("float", new BuiltIntTypeSymbol("float"));
    }

    @Override
    public String getScopeName() {
        return "global";
    }

    @Override
    public Scope getEnclosingScope() {
        return null;
    }

    @Override
    public void define(Symbol sym) {
        symbols.put(sym.name,sym);
    }

    @Override
    public Symbol resolve(String name) {
        return symbols.get(name);
    }

    @Override
    public Type lookup(String name) {
        return resolve(name).type;
    }

    @Override
    public String toString() {
        return getScopeName() + ":" + symbols;
    }
}
