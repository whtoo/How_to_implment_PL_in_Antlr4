package org.teachfx.antlr4.ep19.runtime;

import org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol;


public class StructInstance extends MemorySpace {
    StructSymbol symbol;

    public StructInstance(String name, StructSymbol symbol) {
        super(name);
        this.symbol = symbol;
    }

    public StructInstance(String name, MemorySpace enclosingSpace, StructSymbol symbol) {
        super(name, enclosingSpace);
        this.symbol = symbol;
        for (String key : symbol.getMembers().keySet()) {
            define(key, 0);
        }
    }

    @Override
    public Object get(String name) {

        return super.get(name);
    }
}
