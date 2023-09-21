package org.teachfx.antlr4.ep20.symtab;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class MethodSymbol extends ScopedSymbol implements Type {
    public ParserRuleContext blockStmt;
    public boolean builtin = false;
    public ParserRuleContext callee = null;
    Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();

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


    public void defineMember(Symbol symbol) {
        orderedArgs.put(symbol.getName(),symbol);
    }
    @Override
    public Map<String, Symbol> getMembers() {
        return orderedArgs;
    }


    @Override
    public boolean isPrimitive() {
        return builtin;
    }

    @Override
    public boolean isFunc() {
        return true;
    }

    @Override
    public Type getFuncType() {
        return this;
    }

    @Override
    public Type getPrimitiveType() {
        return null;
    }
}
