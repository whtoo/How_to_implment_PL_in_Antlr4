package org.teachfx.antlr4.ep14.cymbol.lang.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

import org.teachfx.antlr4.ep14.cymbol.lang.ast.Ast;

public class BlockSymbol extends ScopedSymbol {
    Map<String,Symbol> orderedArgs = new LinkedHashMap<>();
    public boolean builtin = false;

    public BlockSymbol(String name, Type type, Scope enclosingScope) {
        super(name, type, enclosingScope);
    }

    public BlockSymbol(String name, Scope parent,Ast tree) {
        super(name, parent, tree);
    }

    @Override
    public Map<String, Symbol> getMemebers() {
       return orderedArgs;
    }
    
    @Override
    public String getName() {
        return name + "(" + stripBrackets(orderedArgs.keySet().toString()) + ")";
    }
}
