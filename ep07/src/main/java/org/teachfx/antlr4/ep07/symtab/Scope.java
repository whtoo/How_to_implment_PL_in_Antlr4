package org.teachfx.antlr4.ep07.symtab;

public interface Scope {
    String getScopeName();
    Scope getEnclosingScope();
    void define(String name, Symbol sym);
    Symbol resolve(String name);
}
