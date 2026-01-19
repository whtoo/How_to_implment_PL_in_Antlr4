package org.teachfx.antlr4.ep21.ir.expr;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;

/**
 * 数组访问表达式
 * 表示 arr[index] 形式的数组元素访问
 */
public class ArrayAccess extends Expr {
    private Expr array;    // 数组表达式
    private Expr index;    // 索引表达式
    private FrameSlot baseSlot; // 数组基地址槽位

    public static ArrayAccess with(Expr array, Expr index, FrameSlot baseSlot) {
        return new ArrayAccess(array, index, baseSlot);
    }

    public ArrayAccess(Expr array, Expr index, FrameSlot baseSlot) {
        this.array = array;
        this.index = index;
        this.baseSlot = baseSlot;
    }

    public Expr getArray() {
        return array;
    }

    public Expr getIndex() {
        return index;
    }

    public FrameSlot getBaseSlot() {
        return baseSlot;
    }

    public void setArray(Expr array) {
        this.array = array;
    }

    public void setIndex(Expr index) {
        this.index = index;
    }

    public void setBaseSlot(FrameSlot baseSlot) {
        this.baseSlot = baseSlot;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "%s[%s]".formatted(array, index);
    }
}