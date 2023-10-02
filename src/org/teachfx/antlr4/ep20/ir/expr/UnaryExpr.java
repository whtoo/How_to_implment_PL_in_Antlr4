package org.teachfx.antlr4.ep20.ir.expr;

import org.teachfx.antlr4.ep20.ir.IRVisitor;

public class UnaryExpr extends Expr
 {
    public String op;
    public Expr expr;
    public UnaryExpr(String op, Expr expr) {
        this.op = op;
        this.expr = expr;
    }

     @Override
     public <S, E> E accept(IRVisitor<S, E> visitor) {
         return visitor.visit(this);
     }
 }
