package org.teachfx.antlr4.ep14.cymbol.lang.symtab;

/**
 * Scope
 */
public interface Scope {
    public String getScopeName();

    public Scope getEnclosingScope();

    public void define(Symbol sym);

    public Symbol resolve(String name);

    public Type lookup(String name);
    
}