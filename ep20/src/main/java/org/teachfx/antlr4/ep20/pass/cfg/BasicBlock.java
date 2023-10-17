package org.teachfx.antlr4.ep20.pass.cfg;

import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.stmt.CJMP;
import org.teachfx.antlr4.ep20.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep20.ir.stmt.JMP;
import org.teachfx.antlr4.ep20.ir.stmt.Stmt;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    private static int LABEL_SEQ = 1;
    private ArrayList<IRNode> stmts;

    private List<BasicBlock> successors;

    private List<BasicBlock> predecessors;

    private int ord = 0;

    public BasicBlock() {
        stmts = new ArrayList<>();
        successors = new ArrayList<>();
        predecessors = new ArrayList<>();
        ord = LABEL_SEQ++;
    }

    public void addStmt(IRNode stmt) {
        stmts.add(stmt);
    }

    public static boolean isBasicBlock(Stmt stmt) {
        return !(stmt instanceof CJMP) && !(stmt instanceof JMP);
    }

    public List<IRNode> getStmts() {
        return stmts;
    }

    public void setStmts(ArrayList<IRNode> stmts) {
        this.stmts = stmts;
    }

    public List<BasicBlock> getSuccessors() {
        return successors;
    }

    public void setSuccessors(List<BasicBlock> successors) {
        this.successors = successors;
    }

    public List<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public void setPredecessors(List<BasicBlock> predecessors) {
        this.predecessors = predecessors;
    }

    public static void setLink(BasicBlock current,BasicBlock next) {
        current.successors.add(next);
        next.predecessors.add(current);
    }

    public void setLink(BasicBlock next) {
        BasicBlock.setLink(this,next);
    }

    @Override
    public String toString() {
//        var firstInstr = stmts.get(0);
//        if (firstInstr instanceof FuncEntryLabel) {
//            return firstInstr.toString();
//        }
        return ".L"+ord;
    }
}
