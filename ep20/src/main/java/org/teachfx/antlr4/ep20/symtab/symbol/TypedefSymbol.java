package org.teachfx.antlr4.ep20.symtab.symbol;

import org.teachfx.antlr4.ep20.symtab.type.Type;

public class TypedefSymbol extends Symbol {
    private Type originalType;

    public TypedefSymbol(String name, Type originalType) {
        super(name, originalType);
        this.originalType = originalType;
    }

    public Type getOriginalType() {
        return originalType;
    }

    public void setOriginalType(Type originalType) {
        this.originalType = originalType;
    }

    @Override
    public String toString() {
        return "TypedefSymbol{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", originalType=" + originalType +
                '}';
    }
}