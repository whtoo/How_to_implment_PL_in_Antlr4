package org.teachfx.antlr4.ep20.symtab;

import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ast.expr.ExprNode;
import org.teachfx.antlr4.ep20.ast.stmt.StmtNode;

import java.util.LinkedHashMap;
import java.util.Map;

public class MethodSymbol extends ScopedSymbol implements Type {
    public StmtNode blockStmt = null;
    public boolean builtin = false;
    public ExprNode callee = null;
    Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();

    // Language func
    public MethodSymbol(String name, Type retType, Scope parent,
                        ASTNode tree) {
        super(name, retType, parent);
        this.tree = tree;
    }

    // Native func
    public MethodSymbol(String name, Scope parent,
                        ASTNode tree) {
        super(name, parent, tree);
    }


    public void defineMember(Symbol symbol) {
        orderedArgs.put(symbol.getName(),symbol);
    }
    @Override
    public Map<String, Symbol> getMembers() {
        return orderedArgs;
    }


    @Override
    public boolean isPrimitive() {
        return builtin;
    }

    @Override
    public boolean isFunc() {
        return true;
    }

    @Override
    public Type getFuncType() {
        return this;
    }

    @Override
    public Type getPrimitiveType() {
        return null;
    }

    @Override
    public void setParentScope(Scope currentScope) {
        this.enclosingScope = currentScope;
    }
}
