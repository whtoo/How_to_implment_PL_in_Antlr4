package org.teachfx.antlr4.ep14.cymbol.lang.symtab;

public class BuiltIntTypeSymbol extends Symbol implements Type {

    public BuiltIntTypeSymbol(String name) {
        super(name);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }
    
}
