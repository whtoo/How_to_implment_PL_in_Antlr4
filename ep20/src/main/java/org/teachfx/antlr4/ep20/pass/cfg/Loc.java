package org.teachfx.antlr4.ep20.pass.cfg;

import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.expr.Operand;

import java.util.Set;

public class Loc<I extends IRNode> {
    // arrange properties
    public final I instr;
    public Set<Operand> liveIn;
    public Set<Operand> liveOut;

    public Loc(I instr) {
        this.instr = instr;
    }

    @Override
    public String toString() {
        return "Loc{" +
                "instr=" + instr +
                '}';
    }
}
