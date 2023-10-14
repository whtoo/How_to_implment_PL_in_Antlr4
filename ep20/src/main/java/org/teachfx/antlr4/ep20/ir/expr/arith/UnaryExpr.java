package org.teachfx.antlr4.ep20.ir.expr.arith;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Expr;
import org.teachfx.antlr4.ep20.symtab.type.OperatorType.UnaryOpType;

public class UnaryExpr extends Expr
 {
    public UnaryOpType op;
    public Expr expr;
    public UnaryExpr(UnaryOpType op, Expr expr) {
        this.op = op;
        this.expr = expr;
    }

     @Override
     public <S, E> E accept(IRVisitor<S, E> visitor) {
         return visitor.visit(this);
     }
 }
