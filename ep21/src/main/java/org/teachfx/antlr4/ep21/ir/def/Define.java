package org.teachfx.antlr4.ep21.ir.def;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.symtab.symbol.Symbol;

public abstract class Define extends IRNode {
    public Symbol symbol;

    public abstract String getDeclName();

    //IR Visitor
    public abstract <S,E> S accept(IRVisitor<S,E> visitor);



}
