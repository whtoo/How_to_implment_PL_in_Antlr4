package org.teachfx.antlr4.ep15.symtab;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class MethodSymbol extends ScopedSymbol {
    public boolean builtin = false;
    Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();

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
