package org.teachfx.antlr4.ep19.symtab.symbol;

import org.teachfx.antlr4.ep19.runtime.MemorySpace;
import org.teachfx.antlr4.ep19.symtab.Type;
import org.teachfx.antlr4.ep19.symtab.scope.Scope;

public class Symbol {
    static Type UNDEFINED;
    // Mark which type
    public Type type;
    // Locate where I am.
    public Scope scope;
    public MemorySpace space;
    protected String name;

    public Symbol(String name) {
        this.name = name;
        this.type = UNDEFINED;
    }

    public Symbol(String name, Type type) {
        this(name);
        this.type = type != null ? type : UNDEFINED;
    }

    public static String stripBrackets(String s) {
        return s.substring(1, s.length() - 1);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        String s = "";
        if (scope != null) s = scope.getScopeName() + ".";
        if (type != null) return '<' + s + getName() + ":" + type + ">";
        return s + getName();
    }
}
