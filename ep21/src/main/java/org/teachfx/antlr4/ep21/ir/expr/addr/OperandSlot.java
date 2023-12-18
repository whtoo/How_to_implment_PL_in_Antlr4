package org.teachfx.antlr4.ep21.ir.expr.addr;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;

public class OperandSlot extends VarSlot {
    private static int ordSeq = 0;

    public static OperandSlot genTemp() {
        return new OperandSlot();
    }

    private int ord = 0;
    private OperandSlot() {
        this.ord = OperandSlot.ordSeq++;
    }
    public int getOrd() {
        return ord;
    }

    @Override
    public String toString() {
        return "t"+ ord ;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }


    public static OperandSlot pushStack() { return OperandSlot.genTemp(); }
    public static void popStack() { ordSeq--;}

    public static int getOrdSeq() {
        return ordSeq;
    }
}
