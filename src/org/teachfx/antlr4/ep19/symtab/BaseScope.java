package org.teachfx.antlr4.ep19.symtab;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.List.*;

public abstract class BaseScope implements Scope {
    Scope enclosingScope;
    Map<String, Symbol> symbols = new LinkedHashMap<>();
    private final static Map<String, Symbol> builtSymbols = new LinkedHashMap<>();

    public void  preDefineSymbol() {
        if (builtSymbols.isEmpty()) {
            for (var sym : List.of(TypeTable.INT,TypeTable.FLOAT,TypeTable.VOID,TypeTable.BOOLEAN)) {
                builtSymbols.put(sym.getName(),sym);
            }
        }
    }


    public BaseScope(Scope parent) {
        preDefineSymbol();
        this.enclosingScope = parent;
    }

    @Override
    public Type lookup(String name) {
        return (Type) resolve(name);
    }

    @Override
    public Symbol resolve(String name) {
        Symbol s = builtSymbols.get(name);
        if (s != null) return s;

        s = symbols.get(name);
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
