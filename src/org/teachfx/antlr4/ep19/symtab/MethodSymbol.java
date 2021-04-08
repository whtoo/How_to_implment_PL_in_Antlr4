package org.teachfx.antlr4.ep19.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.v4.runtime.ParserRuleContext;

public class MethodSymbol extends ScopedSymbol {
    Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();
    public ParserRuleContext blockStmt;
    public boolean builtin = false;
    public ParserRuleContext callee = null;
    // Language func
    public MethodSymbol(String name, Type retType, Scope parent,
            ParserRuleContext tree) {
        super(name, retType, parent);
    }
    // Native func
    public MethodSymbol(String name, Scope parent,
            ParserRuleContext tree) {
        super(name, parent, tree);
    }

    @Override
    public Map<String, Symbol> getMemebers() {       
        return orderedArgs;
    }
    
}
