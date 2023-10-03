package org.teachfx.antlr4.ep20.symtab.scope;

import org.teachfx.antlr4.ep20.symtab.symbol.Symbol;
import org.teachfx.antlr4.ep20.symtab.type.Type;

/**
 * Scope
 */
public interface Scope {
    String getScopeName();

    Scope getEnclosingScope();

    void define(Symbol sym);

    Symbol resolve(String name);

    Type lookup(String name);

    void setParentScope(Scope currentScope);
}