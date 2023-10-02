package org.teachfx.antlr4.ep20.ir.def;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.symtab.Symbol;

public abstract class Def {
    public Symbol symbol;

    //IR Visitor
    public abstract <S,E> S accept(IRVisitor<S,E> visitor);

}
