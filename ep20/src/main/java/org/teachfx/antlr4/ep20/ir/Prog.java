package org.teachfx.antlr4.ep20.ir;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep20.ir.stmt.CJMP;
import org.teachfx.antlr4.ep20.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep20.ir.stmt.JMP;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.pass.cfg.LinearIRBlock;

import java.util.*;


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
    protected TreeSet<LinearIRBlock> needRemovedBlocks = new TreeSet<>();

    private void optimizeEmptyBlock(@NotNull LinearIRBlock linearIRBlock) {
        // replace empty block within non-empty first successor
        if (linearIRBlock.getStmts().isEmpty()){
            // Drop empty block
            if (linearIRBlock.getSuccessors().isEmpty()) {
                needRemovedBlocks.add(linearIRBlock);
                return;
            }
            // Auto-fill next block for jmp/cjmp
            var nextBlock = linearIRBlock.getSuccessors().get(0);
            for (var ref : linearIRBlock.getJmpRefMap()){
                if (ref instanceof JMP jmp) {
                    jmp.setNext(nextBlock);

                } else if (ref instanceof CJMP cjmp) {
                    cjmp.setElseBlock(nextBlock);
                }
            }

            linearIRBlock.getPredecessors().forEach(prev -> {
                prev.removeSuccessor(linearIRBlock);
                prev.getSuccessors().add(nextBlock);
            });

            needRemovedBlocks.add(linearIRBlock);
        }

        // recursive call
        for(var successor : linearIRBlock.getSuccessors()){
            optimizeEmptyBlock(successor);
        }
    }

    private void insertLabelForBlock(LinearIRBlock startBlock) {
        for (var stmt : startBlock.getStmts()) {
            if (stmt instanceof Label) {
                break;
            }

            startBlock.insertStmt(new Label(startBlock.getScope(), startBlock.getOrd()),0);
            break; // only insert one label for each block which is not func-entry block.
        }

        for (var successor : startBlock.getSuccessors()) {
            insertLabelForBlock(successor);
        }
    }
    protected void buildInstrs(LinearIRBlock block) {
        instrs.addAll(block.getStmts());

        for (var successor : block.getSuccessors()) {
            buildInstrs(successor);
        }

    }

    public void optimizeBasicBlock() {
        for(var func : blockList) {
            optimizeEmptyBlock(func);
        }

        for( var emptyBlock : needRemovedBlocks) {
            emptyBlock.getPredecessors().forEach(p -> p.removeSuccessor(emptyBlock));
        }

        for(var func : blockList) {
            insertLabelForBlock(func);
        }
    }

    public List<IRNode> linearInstrs() {

        if (!truncateInstrList.isEmpty()){
            return truncateInstrList;
        }

        for(var func : blockList) {
            buildInstrs(func);
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
