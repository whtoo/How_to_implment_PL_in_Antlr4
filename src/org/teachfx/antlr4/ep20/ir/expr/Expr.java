package org.teachfx.antlr4.ep20.ir.expr;

import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.IRVisitor;

public abstract class Expr extends IRNode {

    abstract public <S,E> E accept(IRVisitor<S,E> visitor);
}
