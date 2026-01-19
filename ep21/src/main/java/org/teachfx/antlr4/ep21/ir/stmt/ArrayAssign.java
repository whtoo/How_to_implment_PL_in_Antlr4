package org.teachfx.antlr4.ep21.ir.stmt;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.ArrayAccess;
import org.teachfx.antlr4.ep21.ir.expr.Expr;

/**
 * 数组赋值语句
 * 表示 arr[index] = value 形式的数组元素赋值
 */
public class ArrayAssign extends Stmt {
    private ArrayAccess arrayAccess;  // 数组访问表达式（左值）
    private Expr value;               // 要赋的值（右值）

    public static ArrayAssign with(ArrayAccess arrayAccess, Expr value) {
        return new ArrayAssign(arrayAccess, value);
    }

    public ArrayAssign(ArrayAccess arrayAccess, Expr value) {
        this.arrayAccess = arrayAccess;
        this.value = value;
    }

    public ArrayAccess getArrayAccess() {
        return arrayAccess;
    }

    public Expr getValue() {
        return value;
    }

    public void setArrayAccess(ArrayAccess arrayAccess) {
        this.arrayAccess = arrayAccess;
    }

    public void setValue(Expr value) {
        this.value = value;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.ARRAY_ASSIGN;
    }

    @Override
    public String toString() {
        return "%s = %s".formatted(arrayAccess, value);
    }
}