package org.teachfx.antlr4.ep20.symtab;

/**
 * Scope
 */
public interface Scope {
    String getScopeName();

    Scope getEnclosingScope();

    void define(Symbol sym);

    Symbol resolve(String name);

    Type lookup(String name);

}