package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;

/**
 * 数组存储LIR指令
 * 表示向数组中存储元素的操作（对应VM的IASTORE指令）
 *
 * <p>这个指令包含：</p>
 * <ul>
 *   <li>arraySlot: 数组变量在局部变量表中的槽位</li>
 *   <li>index: 数组索引表达式</li>
 *   <li>value: 要存储的值表达式</li>
 * </ul>
 *
 * <p>示例：arr[i] = value 生成 LIRArrayStore</p>
 *
 * @author EP21数组功能实现
 */
public class LIRArrayStore extends LIRNode {
    private final VarSlot arraySlot;
    private final Expr index;
    private final Expr value;

    /**
     * 创建数组存储指令
     *
     * @param arraySlot 数组变量在局部变量表中的槽位
     * @param index 数组索引表达式
     * @param value 要存储的值表达式
     */
    public LIRArrayStore(VarSlot arraySlot, Expr index, Expr value) {
        this.arraySlot = arraySlot;
        this.index = index;
        this.value = value;
    }

    public VarSlot getArraySlot() {
        return arraySlot;
    }

    public Expr getIndex() {
        return index;
    }

    public Expr getValue() {
        return value;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.MEMORY_ACCESS;
    }

    @Override
    public boolean hasMemoryAccess() {
        return true;  // 数组存储涉及内存访问
    }

    @Override
    public int getCost() {
        return 2;  // 数组访问成本：加载地址 + 存储数据
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("%s[%s] = %s", arraySlot, index, value);
    }
}
