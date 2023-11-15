package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Operand;
import org.teachfx.antlr4.ep20.ir.expr.VarSlot;

public class Assign extends Stmt {
    protected VarSlot lhs;
    protected Operand rhs;

    /**
     * Assign a value to a variable
     * @param lhs Variable to assign
     * @param rhs Value to assign
     * @return Assign object
     */
    @SuppressWarnings("unused") // Used in generated code.
    public static Assign with(VarSlot lhs,VarSlot rhs) {
        return new Assign(lhs,rhs);
    }
    /**
     * Assign a value to a variable
     * @param lhs Variable to assign
     * @param rhs Value to assign
     * @return Assign object
     */
    public static Assign with(VarSlot lhs, Operand rhs) {
        return new Assign(lhs,rhs);
    }
    public Assign(VarSlot lhs, Operand rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    // Generate getter and setter for lhs and rhs
    public VarSlot getLhs() {
        return lhs;
    }
    public Operand getRhs() {
        return rhs;
    }

    public void setLhs(VarSlot lhs) {
        this.lhs = lhs;
    }

    public void setRhs(VarSlot rhs) {
        this.rhs = rhs;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.ASSIGN;
    }

    @Override
    public String toString() {
        return "%s = %s".formatted(getLhs(),getRhs());
    }
}
