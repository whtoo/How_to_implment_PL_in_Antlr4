package org.teachfx.antlr4.ep19.misc;

import org.teachfx.antlr4.ep19.symtab.StructSymbol;


public class StructInstance extends MemorySpace {
    StructSymbol symbol;

    public StructInstance(String name,StructSymbol symbol) {
        super(name);
        this.symbol = symbol;
    }

    public StructInstance(String name, MemorySpace enclosingSpace, StructSymbol symbol) {
        super(name, enclosingSpace);
        this.symbol = symbol;
    }
  
}
