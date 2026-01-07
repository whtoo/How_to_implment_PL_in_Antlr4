package org.teachfx.antlr4.ep21.ir.stmt;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;

/**
 * 赋值语句节点
 *
 * 类型层次修复说明：
 * - Operand extends Expr，因此所有Operand都是Expr
 * - rhs字段声明为Expr类型，可以接受任何Expr子类（包括Operand、BinExpr等）
 * - 无需使用反射绕过类型系统
 */
public class Assign extends Stmt {
    protected VarSlot lhs;
    protected Expr rhs;

    /**
     * 创建赋值语句
     * @param lhs 左值（变量）
     * @param rhs 右值（表达式，可以是Operand、BinExpr等任何Expr子类）
     * @return Assign对象
     */
    public static Assign with(VarSlot lhs, Expr rhs) {
        return new Assign(lhs, rhs);
    }

    public Assign(VarSlot lhs, Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public VarSlot getLhs() {
        return lhs;
    }

    public Expr getRhs() {
        return rhs;
    }

    public void setLhs(VarSlot lhs) {
        this.lhs = lhs;
    }

    public void setRhs(Expr rhs) {
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
