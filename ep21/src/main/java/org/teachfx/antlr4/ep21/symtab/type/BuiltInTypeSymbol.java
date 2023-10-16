package org.teachfx.antlr4.ep21.symtab.type;

import org.teachfx.antlr4.ep21.symtab.symbol.Symbol;

public class BuiltInTypeSymbol extends Symbol implements Type {

    public BuiltInTypeSymbol(String name) {
        super(name);
    }

    @Override
    public boolean isPreDefined() {
        return true;
    }

    @Override
    public boolean isBuiltIn() {
        return true;
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

}
