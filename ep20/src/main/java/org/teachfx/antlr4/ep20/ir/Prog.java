package org.teachfx.antlr4.ep20.ir;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep20.ir.stmt.CJMP;
import org.teachfx.antlr4.ep20.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep20.ir.stmt.JMP;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.pass.cfg.BasicBlock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class Prog extends IRNode {
    public List<BasicBlock> blockList;
    protected static Logger logger = LogManager.getLogger(Prog.class);
    public List<IRNode> instrs = new ArrayList<>();

    private List<IRNode> truncateInstrList = new LinkedList<>();
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
            if (!basicBlock.getJmpRefMap().isEmpty()){
                instrs.add(new Label(basicBlock.toString(),null));
            }
            instrs.addAll(basicBlock.getStmts());
        } else {
            if (basicBlock.getSuccessors().isEmpty()) {
                return;
            }

            var nextBlock = basicBlock.getSuccessors().get(0);
            for (var ref : basicBlock.getJmpRefMap()){
                if (ref instanceof JMP jmp) {
                    jmp.next = nextBlock;
                } else if (ref instanceof CJMP cjmp) {
                    cjmp.setElseBlock(nextBlock);
                }
            }

        }

        for(var successor : basicBlock.getSuccessors()){
            linearInstrsImpl(successor);
        }
    }

    public List<IRNode> linearInstrs() {

        if (!truncateInstrList.isEmpty()){
            return truncateInstrList;
        }

        for(var block : blockList) {
            linearInstrsImpl(block);
        }

        IRNode prev = null;
        IRNode cur = null;

        for (IRNode instr : instrs) {
            prev = cur;
            cur = instr;
            if (Objects.nonNull(prev) && prev instanceof Label) {
                if (cur instanceof FuncEntryLabel) {
                    truncateInstrList.remove(prev);
                }
            }

            truncateInstrList.add(cur);
        }

        return truncateInstrList;
    }


}
