package org.teachfx.antlr4.ep11.symtab;

public interface Scope {
    String getScopeName();
    Scope getEnclosingScope();
    void define(Symbol sym);
    Symbol resolve(String name);
}
