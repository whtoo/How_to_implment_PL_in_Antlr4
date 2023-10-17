package org.teachfx.antlr4.ep20.ir.expr.addr;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.VarSlot;
import org.teachfx.antlr4.ep20.symtab.symbol.VariableSymbol;

public class FrameSlot extends VarSlot {

    protected int slotIdx = 0;

    public static FrameSlot get(VariableSymbol variableSymbol) {
        return new FrameSlot(variableSymbol.getSlotIdx());
    }

    public FrameSlot(int idx) {
        this.slotIdx = idx;
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
