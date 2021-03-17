package org.teachfx.antlr4.ep16.symtab;

public class Symbol {
    String name;
    static Type UNDEFINED;
    public Type type;
    public Scope scope;
    public Object value;
    
    public Symbol(String name) {
        this.name = name;
        this.type = UNDEFINED;
    }
    public Symbol(String name,Type type) {
        this(name);
        this.type = type != null ? type : UNDEFINED;
    }  
    
    public String getName() {
        return name;
    }

    public String toString() {
        String s = "";
        if(scope != null) s = scope.getScopeName() + ".";
        if(type != null) return '<' + s + getName() + ":" + type + ">";
        return s + getName();
    }

    public static String stripBrackets(String s) {
        return s.substring(1,s.length() - 1);
    }
}
