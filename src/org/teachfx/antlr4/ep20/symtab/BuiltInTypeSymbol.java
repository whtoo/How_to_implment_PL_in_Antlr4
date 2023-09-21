package org.teachfx.antlr4.ep20.symtab;

public class BuiltInTypeSymbol extends Symbol implements Type {

    public BuiltInTypeSymbol(String name) {
        super(name);
    }

    @Override
    public boolean isPrimitive() {
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
