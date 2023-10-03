package org.teachfx.antlr4.ep20.symtab.symbol;

import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.symtab.type.Type;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;

import java.util.Map;


public abstract class ScopedSymbol extends Symbol implements org.teachfx.antlr4.ep20.symtab.scope.Scope {
    public ASTNode tree;
    org.teachfx.antlr4.ep20.symtab.scope.Scope enclosingScope;

    public ScopedSymbol(String name, Type type, org.teachfx.antlr4.ep20.symtab.scope.Scope enclosingScope) {
        super(name, type);
        this.enclosingScope = enclosingScope;
    }

    public ScopedSymbol(String name, org.teachfx.antlr4.ep20.symtab.scope.Scope enclosingScope, ASTNode tree) {
        super(name);
        this.enclosingScope = enclosingScope;
        this.tree = tree;
    }

    @Override
    public Type lookup(String name) {
        return (Type) resolve(name);
    }

    @Override
    public Symbol resolve(String name) {
        Symbol s = getMembers().get(name);
        if (s != null) return s;
        if (getEnclosingScope() != null) {
            return getEnclosingScope().resolve(name);
        }

        return null;
    }

    public Symbol resolveType(String name) {
        return resolve(name);
    }

    @Override
    public org.teachfx.antlr4.ep20.symtab.scope.Scope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public String getScopeName() {
        return name;
    }

    @Override
    public void define(Symbol sym) {
        defineMember(sym);
        sym.scope = this;
    }

    public abstract Map<String, Symbol> getMembers();
    public abstract void defineMember(Symbol sym);

    @Override
    public void setParentScope(Scope currentScope) {
        this.enclosingScope = scope;
    }
}
