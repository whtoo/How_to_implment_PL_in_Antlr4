package org.teachfx.antlr4.ep20.ir.expr.values;

import org.teachfx.antlr4.ep20.ast.stmt.ScopeType;
import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Expr;
import org.teachfx.antlr4.ep20.symtab.symbol.Symbol;

public class Var extends Expr {
    public Symbol symbol;

    private ScopeType scopeType;

    public Var(Symbol varSymbol) {
        this.symbol = varSymbol;
        this.scopeType = ScopeType.FuncScope;
    }

    public String getDeclName() {
        return symbol.getName();
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }
    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
    public String toSource(boolean isWriting) {
        int varSlotIdx = symbol.getSlotIdx();
        String operator = "";
        switch ( scopeType) {
            /// Generate all cases for scopeType
            case ClassScope -> {
                operator =isWriting ? "xload" : "xstore";
            }
            case GlobalScope -> {
                operator = isWriting? "gload": "gstore";
            }
            default -> {
                operator = isWriting? "load" : "store";
            }
        }
        return String.format("%s %d", operator, varSlotIdx);
    }
}
