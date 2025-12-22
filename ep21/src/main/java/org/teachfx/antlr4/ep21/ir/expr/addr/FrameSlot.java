package org.teachfx.antlr4.ep21.ir.expr.addr;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

public class FrameSlot extends VarSlot {

    protected int slotIdx = 0;
    private final VariableSymbol symbol; // 保存符号引用，用于SSA转换

    public static FrameSlot get(VariableSymbol variableSymbol) {
        return new FrameSlot(variableSymbol.getSlotIdx(), variableSymbol);
    }

    public FrameSlot(int idx) {
        this(idx, null);
    }

    public FrameSlot(int idx, VariableSymbol symbol) {
        this.slotIdx = idx;
        this.symbol = symbol;
    }
    public int getSlotIdx() {
        return slotIdx;
    }

    /**
     * 获取关联的变量符号
     * @return 变量符号，如果不存在则返回null
     */
    public VariableSymbol getSymbol() {
        return symbol;
    }

    /**
     * 获取变量名
     * @return 变量名，如果无法获取则返回null
     */
    public String getVariableName() {
        return symbol != null ? symbol.getName() : null;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "@%d".formatted(slotIdx);
    }
}
