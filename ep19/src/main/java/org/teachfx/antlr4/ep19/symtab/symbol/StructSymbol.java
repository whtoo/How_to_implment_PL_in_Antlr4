package org.teachfx.antlr4.ep19.symtab.symbol;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep19.symtab.Type;
import org.teachfx.antlr4.ep19.symtab.scope.Scope;
import org.teachfx.antlr4.ep19.symtab.scope.ScopedSymbol;

import java.util.LinkedHashMap;
import java.util.Map;

public class StructSymbol extends ScopedSymbol implements Type {

    Map<String, Symbol> fields = new LinkedHashMap<String, Symbol>();

    public StructSymbol(String name, Scope parent,
                        ParserRuleContext tree) {
        super(name, parent, tree);
    }

    /**
     * For a.b, only look in fields to resolve b, not up scope tree
     */
    public Symbol resolveMember(String name) {
        return fields.get(name);
    }

    @Override
    public Map<String, Symbol> getMembers() {
        return fields;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public String toString() {
        return "struct " + name + ":{"
                + stripBrackets(fields.keySet().toString()) + "}";
    }

}
