package org.teachfx.antlr4.ep19.runtime;

import org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol;

public class FunctionSpace extends MemorySpace {
    MethodSymbol def;

    public FunctionSpace(String name, MethodSymbol def) {
        super(name);
        this.def = def;
    }

    public FunctionSpace(String name, MethodSymbol def, MemorySpace space) {
        super(name, space);
        this.def = def;
    }


}
