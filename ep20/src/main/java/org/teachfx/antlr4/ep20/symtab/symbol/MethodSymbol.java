package org.teachfx.antlr4.ep20.symtab.symbol;

import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ast.expr.ExprNode;
import org.teachfx.antlr4.ep20.ast.stmt.ScopeType;
import org.teachfx.antlr4.ep20.ast.stmt.StmtNode;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;
import org.teachfx.antlr4.ep20.symtab.type.Type;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class MethodSymbol extends ScopedSymbol implements Type {
    private int LABEL_SEQ = 0;
    private int VAR_SLOT_SEQ = 0;

    public boolean builtin = false;
    private int args = 0;

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
        if (symbol instanceof VariableSymbol variableSymbol) {
            variableSymbol.setSlotIdx(getVarSlotSeq());
        }
        orderedArgs.put(symbol.getName(), symbol);
    }

    @Override
    public Map<String, Symbol> getMembers() {
        return orderedArgs;
    }


    @Override
    public boolean isPreDefined() {
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

    @Override
    public int getLabelSeq() {
        return LABEL_SEQ++;
    }


    @Override
    public int getVarSlotSeq() {
        return VAR_SLOT_SEQ++;
    }

    public int getArgs() {
        return args;
    }

    public void setArgs(int args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "%s<%s:%s>".formatted(getName(),getScopeName(),getType());
    }
    public Type getReturnType() {
        return getType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodSymbol that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getReturnType(),((MethodSymbol) o).getReturnType()) && Objects.equals(orderedArgs, that.orderedArgs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), orderedArgs);
    }

    private ScopeType scopeType = ScopeType.FuncScope;
    @Override
    public ScopeType getScopeType() {
        return scopeType;
    }

    @Override
    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }
}
