package org.teachfx.antlr4.ep15.symtab;

public class BuiltInTypeSymbol extends Symbol implements Type {

    public BuiltInTypeSymbol(String name) {
        super(name);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

}
