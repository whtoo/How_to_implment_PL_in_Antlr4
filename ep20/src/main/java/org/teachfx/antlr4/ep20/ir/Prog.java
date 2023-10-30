package org.teachfx.antlr4.ep20.ir;

import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.pass.cfg.BasicBlock;

import java.util.ArrayList;
import java.util.List;


public class Prog extends IRNode {
    public List<BasicBlock> blockList;

    public List<IRNode> instrs = new ArrayList<>();

    public Prog() {
        this.blockList = new ArrayList<>() ;
    }

    public <S,E> S accept(IRVisitor<S,E> visitor){

        return visitor.visit(this);
    }

    public void addBlock(BasicBlock basicBlock) {
        blockList.add(basicBlock);
    }

    private void linearInstrsImpl(BasicBlock basicBlock) {
        if (!basicBlock.getStmts().isEmpty()) {
            instrs.add(new Label(basicBlock.toString(),null));
            instrs.addAll(basicBlock.getStmts());
        }

        if (basicBlock.getSuccessors().isEmpty()) {
            return;
        }

        for(var successor : basicBlock.getSuccessors()){
            linearInstrsImpl(successor);
        }
    }

    public List<IRNode> linearInstrs() {
        for(var block : blockList) {
            linearInstrsImpl(block);
        }

        return instrs;
    }
}
