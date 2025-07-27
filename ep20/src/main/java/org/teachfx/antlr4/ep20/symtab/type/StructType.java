package org.teachfx.antlr4.ep20.symtab.type;

import org.teachfx.antlr4.ep20.symtab.symbol.Symbol;

import java.util.HashMap;
import java.util.Map;

public class StructType implements Type {
    private String name;
    private Map<String, Symbol> fields = new HashMap<>();

    public StructType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addField(String name, Symbol field) {
        fields.put(name, field);
    }

    public Symbol getField(String name) {
        return fields.get(name);
    }

    public Map<String, Symbol> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "StructType{" +
                "name='" + name + '\'' +
                ", fields=" + fields.keySet() +
                '}';
    }

    public boolean isEqual(Type other) {
        if (this == other) return true;
        if (!(other instanceof StructType)) return false;
        StructType structType = (StructType) other;
        return name.equals(structType.name);
    }

    @Override
    public boolean isPreDefined() {
        return false;
    }

    @Override
    public boolean isFunc() {
        return false;
    }

    @Override
    public Type getFuncType() {
        return null;
    }

    @Override
    public Type getPrimitiveType() {
        return this;
    }

    @Override
    public boolean isVoid() {
        return false;
    }
}