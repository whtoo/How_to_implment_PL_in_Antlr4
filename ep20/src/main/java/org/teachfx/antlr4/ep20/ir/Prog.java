package org.teachfx.antlr4.ep20.ir;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep20.ir.stmt.CJMP;
import org.teachfx.antlr4.ep20.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep20.ir.stmt.JMP;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.pass.cfg.LinearIRBlock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class Prog extends IRNode {
    public List<LinearIRBlock> blockList;
    protected static Logger logger = LogManager.getLogger(Prog.class);
    public List<IRNode> instrs = new ArrayList<>();

    private final List<IRNode> truncateInstrList = new LinkedList<>();
    public Prog() {
        this.blockList = new ArrayList<>() ;
    }

    public <S,E> S accept(IRVisitor<S,E> visitor){

        return visitor.visit(this);
    }

    public void addBlock(LinearIRBlock linearIRBlock) {
        blockList.add(linearIRBlock);
    }

    private void linearInstrsImpl(LinearIRBlock linearIRBlock) {
        if (!linearIRBlock.getStmts().isEmpty()) {
            if (!linearIRBlock.getJmpRefMap().isEmpty()){
                instrs.add(new Label(linearIRBlock.toString(),null));
            }
            instrs.addAll(linearIRBlock.getStmts());
        } else {
            if (linearIRBlock.getSuccessors().isEmpty()) {
                return;
            }

            var nextBlock = linearIRBlock.getSuccessors().get(0);
            for (var ref : linearIRBlock.getJmpRefMap()){
                if (ref instanceof JMP jmp) {
                    jmp.next = nextBlock;
                } else if (ref instanceof CJMP cjmp) {
                    cjmp.setElseBlock(nextBlock);
                }
            }

        }

        for(var successor : linearIRBlock.getSuccessors()){
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

        IRNode prev;
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
