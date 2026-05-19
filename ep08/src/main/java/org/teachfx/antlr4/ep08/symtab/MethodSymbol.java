package org.teachfx.antlr4.ep08.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

public class MethodSymbol extends ScopedSymbol {
    Map<String, Symbol> orderedArgs = new LinkedHashMap<>();

    public MethodSymbol(String name, Type retType, Scope enclosingScope) {
        super(name, retType, enclosingScope);
    }

    @Override
    public Map<String, Symbol> getMembers() {
        return orderedArgs;
    }

    /** Define a parameter in this function scope. */
    public void defineParam(String paramName, Type paramType) {
        VariableSymbol vs = new VariableSymbol(paramName, paramType);
        define(vs);
    }
}
