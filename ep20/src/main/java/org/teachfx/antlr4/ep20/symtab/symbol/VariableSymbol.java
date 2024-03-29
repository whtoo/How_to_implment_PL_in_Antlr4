package org.teachfx.antlr4.ep20.symtab.symbol;

import org.teachfx.antlr4.ep20.symtab.type.Type;

public class VariableSymbol extends Symbol {

    public VariableSymbol(String name) {
        super(name);
    }

    public VariableSymbol(String name, Type type) {
        super(name, type);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
