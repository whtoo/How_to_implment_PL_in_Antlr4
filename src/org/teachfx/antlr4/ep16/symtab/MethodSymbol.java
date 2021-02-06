package org.teachfx.antlr4.ep16.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

public class MethodSymbol extends ScopedSymbol implements Scope {
    Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();
    public boolean builtin = false;

    public MethodSymbol(String name, Type retType, Scope parent,
            ParserRuleContext token) {
        super(name, retType, parent);
    }

    public MethodSymbol(String name, Scope parent,
            ParserRuleContext tree) {
        super(name, parent, tree);
    }

    @Override
    public Map<String, Symbol> getMemebers() {       
        return orderedArgs;
    }
    
}
