package org.teachfx.antlr4.ep19.symtab.symbol;

import org.teachfx.antlr4.ep19.symtab.Type;

public class BuiltInTypeSymbol extends Symbol implements Type {

    public BuiltInTypeSymbol(String name) {
        super(name);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

}
