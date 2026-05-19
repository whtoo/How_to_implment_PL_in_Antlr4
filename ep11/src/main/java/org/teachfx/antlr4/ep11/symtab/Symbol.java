package org.teachfx.antlr4.ep11.symtab;

public class Symbol {
    public String name;
    public Type type;
    public Scope scope;

    public Symbol(String name) {
        this.name = name;
    }

    public Symbol(String name, Type type) {
        this(name);
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (type != null) return '<' + name + ":" + type.getName() + '>';
        return name;
    }
}
