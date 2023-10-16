package org.teachfx.antlr4.ep21.ir.expr.arith;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

public class BinExpr extends Expr {

    protected Expr lhs;
    protected Expr rhs;
    protected OperatorType.BinaryOpType opType;

    public BinExpr(OperatorType.BinaryOpType op, Expr left, Expr right) {
        this.opType = op;
        this.lhs = left;
        this.rhs = right;
    }

    public Expr getLhs() {
        return lhs;
    }

    public void setLhs(Expr lhs) {
        this.lhs = lhs;
    }

    public Expr getRhs() {
        return rhs;
    }

    public void setRhs(Expr rhs) {
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
        return "BinaryExpr";
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}