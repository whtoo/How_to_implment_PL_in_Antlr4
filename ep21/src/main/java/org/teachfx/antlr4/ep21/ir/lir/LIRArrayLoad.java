package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;

/**
 * 数组加载LIR指令
 * 表示从数组中加载元素的操作（对应VM的IALOAD指令）
 *
 * <p>这个指令包含：</p>
 * <ul>
 *   <li>arraySlot: 数组变量在局部变量表中的槽位</li>
 *   <li>index: 数组索引表达式</li>
 *   <li>resultSlot: 加载结果存储的槽位</li>
 * </ul>
 *
 * <p>示例：arr[i] 生成 LIRArrayLoad</p>
 *
 * @author EP21数组功能实现
 */
public class LIRArrayLoad extends LIRNode {
    private final VarSlot arraySlot;
    private final Expr index;
    private final VarSlot resultSlot;

    /**
     * 创建数组加载指令
     *
     * @param arraySlot 数组变量在局部变量表中的槽位
     * @param index 数组索引表达式
     * @param resultSlot 加载结果存储的槽位
     */
    public LIRArrayLoad(VarSlot arraySlot, Expr index, VarSlot resultSlot) {
        this.arraySlot = arraySlot;
        this.index = index;
        this.resultSlot = resultSlot;
    }

    public VarSlot getArraySlot() {
        return arraySlot;
    }

    public Expr getIndex() {
        return index;
    }

    public VarSlot getResultSlot() {
        return resultSlot;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.MEMORY_ACCESS;
    }

    @Override
    public boolean hasMemoryAccess() {
        return true;  // 数组加载涉及内存访问
    }

    @Override
    public int getCost() {
        return 2;  // 数组访问成本：加载地址 + 加载数据
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("%s = %s[%s]", resultSlot, arraySlot, index);
    }
}
