package org.teachfx.antlr4.ep21.ir.expr.addr;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

public class FrameSlot extends VarSlot {

    protected int slotIdx = 0;

    public static FrameSlot get(VariableSymbol variableSymbol) {
        return new FrameSlot(variableSymbol.getSlotIdx());
    }

    public FrameSlot(int idx) {
        this.slotIdx = idx;
    }
    public int getSlotIdx() {
        return slotIdx;
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
