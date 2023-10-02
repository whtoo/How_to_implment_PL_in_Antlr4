package org.teachfx.antlr4.ep20.symtab;

import org.teachfx.antlr4.ep20.misc.MemorySpace;

public class Symbol {
    static Type UNDEFINED;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    // Mark which type
    protected Type type;
    // Locate where I am.
    public Scope scope;

    String name;

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
