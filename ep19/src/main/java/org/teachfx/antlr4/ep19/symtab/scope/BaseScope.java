package org.teachfx.antlr4.ep19.symtab.scope;

import org.teachfx.antlr4.ep19.symtab.Type;
import org.teachfx.antlr4.ep19.symtab.TypeTable;
import org.teachfx.antlr4.ep19.symtab.symbol.Symbol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseScope implements Scope {
    Scope enclosingScope;
    Map<String, Symbol> symbols = new LinkedHashMap<>();
    private final static Map<String, Symbol> builtSymbols = new LinkedHashMap<>();

    public void  preDefineSymbol() {
        if (builtSymbols.isEmpty()) {
            for (var sym : List.of(TypeTable.INT,TypeTable.FLOAT,TypeTable.VOID,TypeTable.BOOLEAN)) {
                builtSymbols.put(sym.getName(), new Symbol(sym.getName(), sym));
            }
        }
    }

    public BaseScope(Scope parent) {
        preDefineSymbol();
        this.enclosingScope = parent;
    }

    @Override
    public Type lookup(String name) {
        Symbol symbol = resolve(name);
        if (symbol != null && symbol instanceof Type) {
            return (Type) symbol;
        } else if (symbol != null && symbol.type instanceof Type) {
            return symbol.type;
        }
        return null;
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
        symbols.put(sym.getName(), sym);
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
