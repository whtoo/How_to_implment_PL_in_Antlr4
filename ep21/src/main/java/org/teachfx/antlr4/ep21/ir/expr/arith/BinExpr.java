package org.teachfx.antlr4.ep21.ir.expr.arith;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

public class BinExpr extends Expr {

    protected VarSlot lhs;
    protected VarSlot rhs;
    protected OperatorType.BinaryOpType opType;

    public BinExpr(OperatorType.BinaryOpType op, VarSlot left, VarSlot right) {
        this.opType = op;
        this.lhs = left;
        this.rhs = right;
    }

    public VarSlot getLhs() {
        return lhs;
    }

    public void setLhs(VarSlot lhs) {
        this.lhs = lhs;
    }

    public VarSlot getRhs() {
        return rhs;
    }

    public void setRhs(VarSlot rhs) {
        this.rhs = rhs;
    }

    public OperatorType.BinaryOpType getOpType() {
        return opType;
    }

    public void setOpType(OperatorType.BinaryOpType opType) {
        this.opType = opType;
    }

    @Override
    public String toString() {
        return "%s %s %s".formatted(lhs,opType,rhs);
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public static BinExpr with(OperatorType.BinaryOpType opType,VarSlot lhs,VarSlot rhs) {
        return new BinExpr(opType,lhs,rhs);
    }


}