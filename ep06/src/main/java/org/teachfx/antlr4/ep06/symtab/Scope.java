package org.teachfx.antlr4.ep06.symtab;

public interface Scope {
    String getScopeName();
    Scope getEnclosingScope();
    void define(String name, Symbol sym);
    Symbol resolve(String name);
}
