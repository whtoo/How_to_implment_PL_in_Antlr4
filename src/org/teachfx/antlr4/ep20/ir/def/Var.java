package org.teachfx.antlr4.ep20.ir.def;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.symtab.symbol.Symbol;

public class Var extends Define {

    public Var(Symbol varSymbol) {
        this.symbol = varSymbol;
    }


    @Override
    public String getDeclName() {
        return symbol.getName();
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
