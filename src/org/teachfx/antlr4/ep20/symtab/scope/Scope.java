package org.teachfx.antlr4.ep20.symtab.scope;

import org.teachfx.antlr4.ep20.symtab.symbol.Symbol;
import org.teachfx.antlr4.ep20.symtab.type.Type;

/**
 * Scope
 */
public interface Scope {
    public String getScopeName();

    public Scope getEnclosingScope();

    public void define(Symbol sym);

    public Symbol resolve(String name);

    public Type lookup(String name);

    public void setParentScope(Scope currentScope);

    public int getLabelSeq();

    public int getVarSlotSeq();
}