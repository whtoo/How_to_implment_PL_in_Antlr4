package org.teachfx.antlr4.ep17.symtab;

public class VariableSymbol extends Symbol {

    public VariableSymbol(String name) {
        super(name);
    }

    public VariableSymbol(String name, Type type) {
        super(name, type);
    }

}
