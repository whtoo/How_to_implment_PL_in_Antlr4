package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;

import java.util.List;

/**
 * 数组初始化LIR指令
 * 表示数组初始化操作，类似于{expr, expr, ...}语法
 *
 * <p>这个指令包含：</p>
 * <ul>
 *   <li>elementType: 数组元素的类型</li>
 *   <li>size: 数组的大小</li>
 *   <li>elements: 初始化元素列表</li>
 * </ul>
 *
 * @author EP21数组功能实现
 */
public class LIRArrayInit extends LIRNode {
    private final VarSlot arraySlot;
    private final int size;
    private final List<Expr> elements;
    private final String elementTypeName;

    /**
     * 创建数组初始化指令
     *
     * @param arraySlot 数组变量在局部变量表中的槽位
     * @param size 数组大小
     * @param elements 初始化元素列表
     * @param elementTypeName 元素类型名称（用于调试）
     */
    public LIRArrayInit(VarSlot arraySlot, int size, List<Expr> elements, String elementTypeName) {
        this.arraySlot = arraySlot;
        this.size = size;
        this.elements = elements;
        this.elementTypeName = elementTypeName;
    }

    public VarSlot getArraySlot() {
        return arraySlot;
    }

    public int getSize() {
        return size;
    }

    public List<Expr> getElements() {
        return elements;
    }

    public String getElementTypeName() {
        return elementTypeName;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.DATA_TRANSFER;
    }

    @Override
    public boolean hasMemoryAccess() {
        return true;  // 数组初始化涉及内存访问
    }

    @Override
    public int getCost() {
        // 每个元素存储指令的成本为1
        return size;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("array_init %s[%d] = {%s}",
                elementTypeName, size, elements);
    }
}
