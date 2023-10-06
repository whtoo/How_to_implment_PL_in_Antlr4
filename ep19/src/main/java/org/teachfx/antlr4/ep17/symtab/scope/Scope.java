package org.teachfx.antlr4.ep19.symtab.scope;

import org.teachfx.antlr4.ep19.symtab.Type;
import org.teachfx.antlr4.ep19.symtab.symbol.Symbol;

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