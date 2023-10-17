package org.teachfx.antlr4.ep20.ir;


import org.teachfx.antlr4.ep20.pass.cfg.BasicBlock;

import java.util.ArrayList;
import java.util.List;


public class Prog extends IRNode {
    public List<BasicBlock> blockList;

    public Prog() {
        this.blockList = new ArrayList<>() ;
    }

    public <S,E> S accept(IRVisitor<S,E> visitor){

        return visitor.visit(this);
    }

    public void addBlock(BasicBlock basicBlock) {
        blockList.add(basicBlock);
    }
}
