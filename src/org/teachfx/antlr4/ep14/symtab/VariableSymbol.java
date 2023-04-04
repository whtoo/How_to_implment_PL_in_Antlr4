package org.teachfx.antlr4.ep14.symtab;

public class VariableSymbol<T extends Type> extends Symbol {

    public VariableSymbol(String name) {
        super(name);
    }

    public VariableSymbol(String name, T type) {
        super(name, type);
    }

}
