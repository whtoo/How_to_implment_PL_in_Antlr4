package org.teachfx.antlr4.ep21.ir.expr;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.IRVisitor;

public abstract class Expr extends IRNode {
    public abstract <S,E> E accept(IRVisitor<S,E> visitor);
}
