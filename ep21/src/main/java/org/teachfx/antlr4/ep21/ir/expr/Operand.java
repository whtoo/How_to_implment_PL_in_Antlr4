package org.teachfx.antlr4.ep21.ir.expr;

import org.teachfx.antlr4.ep21.ir.IRVisitor;

public class Operand extends Expr {
    /**
     * 默认的accept实现，返回null
     * 子类可以覆盖此方法提供更具体的实现
     */
    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        // 默认实现返回null，子类可以覆盖
        return null;
    }
}
