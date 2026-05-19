package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;

/**
 * 数组分配LIR指令
 * 表示分配新数组的操作（对应VM的NEWARRAY指令）
 *
 * <p>这个指令包含：</p>
 * <ul>
 *   <li>size: 数组大小表达式</li>
 *   <li>resultSlot: 分配的数组引用存储的槽位</li>
 *   <li>elementTypeName: 数组元素类型名称（用于调试）</li>
 * </ul>
 *
 * <p>示例：int[5] arr 生成 LIRNewArray(size=5, resultSlot=arr)</p>
 *
 * @author EP21数组功能实现
 */
public class LIRNewArray extends LIRNode {
    private final Expr size;
    private final VarSlot resultSlot;
    private final String elementTypeName;

    /**
     * 创建数组分配指令
     *
     * @param size 数组大小表达式
     * @param resultSlot 分配的数组引用存储的槽位
     * @param elementTypeName 数组元素类型名称（用于调试）
     */
    public LIRNewArray(Expr size, VarSlot resultSlot, String elementTypeName) {
        this.size = size;
        this.resultSlot = resultSlot;
        this.elementTypeName = elementTypeName;
    }

    public Expr getSize() {
        return size;
    }

    public VarSlot getResultSlot() {
        return resultSlot;
    }

    public String getElementTypeName() {
        return elementTypeName;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.MEMORY_ACCESS;
    }

    @Override
    public boolean hasMemoryAccess() {
        return true;  // 数组分配涉及内存访问
    }

    @Override
    public int getCost() {
        return 3;  // 数组分配成本：计算大小 + 分配内存 + 存储引用
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("%s = new %s[%s]", resultSlot, elementTypeName, size);
    }
}