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
     *
     * ⚠️ 技术债务: 此方法使用反射绕过类型系统
     *
     * 原因: Expr和Operand是独立的类型层次，无法直接转换
     * - Expr extends IRNode
     * - Operand extends IRNode
     * - 但它们之间没有继承关系
     *
     * TODO: 修复类型层次结构，使Expr成为Operand的子类，或使用统一接口
     * 当前方案: 使用反射作为临时解决方案
     */
    private void setRhsFromExpr(org.teachfx.antlr4.ep21.ir.expr.Expr expr) {
        // 由于架构限制，需要通过反射直接访问rhs字段
        // Expr和Operand是独立的类型层次，无法直接赋值
        try {
            java.lang.reflect.Field rhsField = Assign.class.getDeclaredField("rhs");
            rhsField.setAccessible(true);
            rhsField.set(this, expr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set rhs from Expr (technical debt - using reflection)", e);
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

    /**
     * 设置右值
     *
     * @param rhs 要设置的右值（仅支持VarSlot类型）
     * @deprecated 此方法存在类型问题，参数应为Operand而非VarSlot
     *             使用withExpr()方法支持Expr类型的右值
     */
    @Deprecated
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
