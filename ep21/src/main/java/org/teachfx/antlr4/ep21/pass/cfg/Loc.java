package org.teachfx.antlr4.ep21.pass.cfg;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Loc<I extends IRNode> {
    // arrange properties
    protected final I instr;
    public Set<Operand> liveIn;
    public Set<Operand> liveOut;

    public Loc(I instr) {
        this.instr = instr;
    }

    public Stream<I> getInstr() {
        return Stream.of(instr);
    }
    
    /**
     * 获取指令
     */
    public I getInstruction() {
        return instr;
    }

    @Override
    public String toString() {
        return "Loc{" +
                "instr=" + instr +
                '}';
    }
}
