package org.teachfx.antlr4.ep20.ir.expr.addr;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.VarSlot;

public class StackSlot extends VarSlot {
    private static int ordSeq = 0;

    public static StackSlot genTemp() {
        return new StackSlot();
    }

    private int ord = 0;
    private StackSlot() {
        this.ord = StackSlot.ordSeq++;
    }

    @Override
    public String toString() {
        return "t"+ ord ;
    }

    @Override
    public <S, E> E accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }


    public static StackSlot pushStack() { return new StackSlot(); }
    public static void popStack() { ordSeq--;}

    public static int getOrdSeq() {
        return ordSeq;
    }
}
