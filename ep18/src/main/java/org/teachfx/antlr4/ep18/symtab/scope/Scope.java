package org.teachfx.antlr4.ep18.symtab.scope;

import org.teachfx.antlr4.ep18.symtab.symbol.Symbol;
import org.teachfx.antlr4.ep18.symtab.type.Type;

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

    public int setBaseVarSlotSeq(int baseVarSlotSeq);

    public int getVarSlots();

}