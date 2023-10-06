package org.teachfx.antlr4.ep20.ir.expr;

import org.teachfx.antlr4.ep20.ast.stmt.BlockStmtNode;
import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.StorageSlot;
import org.teachfx.antlr4.ep20.ir.expr.Expr;
import org.teachfx.antlr4.ep20.symtab.symbol.Symbol;

public class Var extends Expr {
    public Symbol symbol;

    private BlockStmtNode.ScopeType scopeType;

    public Var(Symbol varSymbol) {
        this.symbol = varSymbol;
        this.scopeType = BlockStmtNode.ScopeType.FuncScope;
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

    public BlockStmtNode.ScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(BlockStmtNode.ScopeType scopeType) {
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
