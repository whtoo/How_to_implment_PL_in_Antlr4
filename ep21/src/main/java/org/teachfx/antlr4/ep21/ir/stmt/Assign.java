package org.teachfx.antlr4.ep21.ir.stmt;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;

public class Assign extends Stmt {
    protected VarSlot lhs;
    protected Operand rhs;

    /**
     * Assign a value to a variable
     * @param lhs Variable to assign
     * @param rhs Value to assign
     * @return Assign object
     */
    public static Assign with(VarSlot lhs, VarSlot rhs) {
        return new Assign(lhs, rhs);
    }

    /**
     * Assign a value to a variable
     * @param lhs Variable to assign
     * @param rhs Value to assign (Operand type: VarSlot, ConstVal, etc.)
     * @return Assign object
     */
    public static Assign with(VarSlot lhs, Operand rhs) {
        return new Assign(lhs, rhs);
    }

    /**
     * Assign an expression to a variable
     * @param lhs Variable to assign
     * @param rhs Expression to assign (Expr type: BinExpr, UnaryExpr, etc.)
     * @return Assign object
     */
    public static Assign withExpr(VarSlot lhs, org.teachfx.antlr4.ep21.ir.expr.Expr rhs) {
        // 需要包装Expr为Operand，使用setRhs方法
        Assign assign = new Assign(lhs, null);
        assign.setRhsFromExpr(rhs);
        return assign;
    }

    public Assign(VarSlot lhs, Operand rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * 从Expr设置rhs（用于BinExpr等非Operand类型）
     */
    private void setRhsFromExpr(org.teachfx.antlr4.ep21.ir.expr.Expr expr) {
        // 由于架构限制，需要通过Field直接访问或使用setRhs
        // 这里使用反射或直接字段访问
        try {
            java.lang.reflect.Field rhsField = Assign.class.getDeclaredField("rhs");
            rhsField.setAccessible(true);
            rhsField.set(this, expr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set rhs from Expr", e);
        }
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
