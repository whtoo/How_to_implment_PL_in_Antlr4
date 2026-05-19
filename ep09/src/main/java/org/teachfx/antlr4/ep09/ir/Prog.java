package org.teachfx.antlr4.ep09.ir;

import java.util.*;

public class Prog extends IRNode {
    public List<IRNode> instrs = new ArrayList<>();

    public Prog() {
    }

    public <S,E> S accept(IRVisitor<S,E> visitor){
        return visitor.visit(this);
    }

    public void addInstr(IRNode instr) {
        instrs.add(instr);
    }

    public List<IRNode> getInstrs() {
        return instrs;
    }
}
