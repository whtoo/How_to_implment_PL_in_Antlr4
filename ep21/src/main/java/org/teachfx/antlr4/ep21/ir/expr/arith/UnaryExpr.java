package org.teachfx.antlr4.ep21.ir.expr.arith;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType.UnaryOpType;

public class UnaryExpr extends Expr
 {
    public UnaryOpType op;
    public VarSlot expr;
    public UnaryExpr(UnaryOpType op, VarSlot expr) {
        this.op = op;
        this.expr = expr;
    }

     @Override
     public <S, E> E accept(IRVisitor<S, E> visitor) {
         return visitor.visit(this);
     }

     public static UnaryExpr with(OperatorType.UnaryOpType opType, VarSlot operand) {
         return new UnaryExpr(opType,operand);
     }

     @Override
     public String toString() {
         return "%s %s".formatted(op,expr);
     }
 }
