package org.teachfx.antlr4.ep18r.symtab.symbol;

import org.teachfx.antlr4.ep18r.symtab.type.Type;

import java.util.HashMap;
import java.util.Map;

public class StructSymbol extends Symbol {
    private Map<String, Symbol> fields = new HashMap<>();

    public StructSymbol(String name) {
        super(name);
    }

    public StructSymbol(String name, Type type) {
        super(name, type);
    }

    public void defineField(Symbol symbol) {
        fields.put(symbol.getName(), symbol);
    }

    public Symbol resolveField(String name) {
        return fields.get(name);
    }

    public Map<String, Symbol> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "StructSymbol{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", fields=" + fields +
                '}';
    }
}