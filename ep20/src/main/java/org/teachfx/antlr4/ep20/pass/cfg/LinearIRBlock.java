package org.teachfx.antlr4.ep20.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.JMPInstr;
import org.teachfx.antlr4.ep20.ir.stmt.*;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;
import org.teachfx.antlr4.ep20.utils.Kind;

import java.util.*;

public class LinearIRBlock implements Comparable<LinearIRBlock> {

    // Fields
    private static int LABEL_SEQ = 0;
    private static final Logger logger = LogManager.getLogger(LinearIRBlock.class);
    private Kind kind = Kind.CONTINUOUS;
    private int ord = 0;
    private ArrayList<IRNode> stmts;
    private List<LinearIRBlock> successors;
    private List<LinearIRBlock> predecessors;

    private Scope scope = null;
    private List<JMPInstr> jmpRefMap = new ArrayList<>();

    // Constructor
    public LinearIRBlock(Scope scope) {
        stmts = new ArrayList<>();
        successors = new ArrayList<>();
        predecessors = new ArrayList<>();
        ord = LABEL_SEQ++;
        this.scope = scope;
        logger.debug(ord);
    }

    public LinearIRBlock() {
        stmts = new ArrayList<>();
        successors = new ArrayList<>();
        predecessors = new ArrayList<>();
        ord = LABEL_SEQ++;
        this.scope = null;
        logger.debug(ord);
    }
    public static boolean isBasicBlock(Stmt stmt) {
        return !(stmt instanceof CJMP) && !(stmt instanceof JMP);
    }

    // Link Operations
    public static void setLink(LinearIRBlock current, LinearIRBlock next) {
        current.successors.add(next);
        next.predecessors.add(current);
    }

    // Methods
    // Statement Operations
    public void addStmt(IRNode stmt) {
        stmts.add(stmt);
        updateKindByLastInstr(stmt);
    }

    public void insertStmt(IRNode stmt,int idx) {
        stmts.add(idx, stmt);
    }

    private void updateKindByLastInstr(IRNode stmt) {
        if (stmt instanceof CJMP) {
            kind = Kind.END_BY_CJMP;
        } else if (stmt instanceof JMP) {
            kind = Kind.END_BY_JMP;
        } else if (stmt instanceof ReturnVal) {
            kind = Kind.END_BY_RETURN;
        } else {
            kind = Kind.CONTINUOUS;
        }
    }

    // Getters and Setters
    public List<IRNode> getStmts() {
        return stmts;
    }

    public void setStmts(ArrayList<IRNode> stmts) {
        this.stmts = stmts;
        var lastInstr = stmts.get(stmts.size() - 1);
        updateKindByLastInstr(lastInstr);
    }

    public List<LinearIRBlock> getSuccessors() {
        return successors;
    }

    public void setSuccessors(List<LinearIRBlock> successors) {
        this.successors = successors;
    }

    public List<LinearIRBlock> getPredecessors() {
        return predecessors;
    }

    public void setPredecessors(List<LinearIRBlock> predecessors) {
        this.predecessors = predecessors;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public List<JMPInstr> getJmpRefMap() {
        return jmpRefMap;
    }

    public void setJmpRefMap(List<JMPInstr> jmpRefMap) {
        this.jmpRefMap = jmpRefMap;
    }

    public int getOrd() {
        return ord;
    }

    public void setLink(LinearIRBlock next) {
        LinearIRBlock.setLink(this, next);
    }

    public Kind getKind() {
        return kind;
    }

    // Jump Operations
    public void refJMP(JMPInstr node) {
        jmpRefMap.add(node);
    }

    // Utility Methods
    @Override
    public String toString() {
        if (stmts.isEmpty()) {
            return "L" + ord;
        }
        if (stmts.get(0) instanceof FuncEntryLabel funcEntryLabel) {
            return funcEntryLabel.toSource();
        }
        return "L" + ord;
    }

    public String toSource() {
        var firstInstr = stmts.get(0);

        if (firstInstr instanceof FuncEntryLabel) {
            return ((FuncEntryLabel) firstInstr).toSource();
        }

        return "L" + ord;
    }

    public Label getLabel() {
        if (stmts.isEmpty()) {
            return new Label(toString(), scope);
        }

        var firstInstr = stmts.get(0);
        if (firstInstr instanceof Label label) {
            return label;
        }

        return null;
    }

    public Optional<TreeSet<LinearIRBlock>> getJumpEntries() {
        var entries = new TreeSet<LinearIRBlock>();
        switch (kind) {
            case END_BY_CJMP -> {
                CJMP cjmp = (CJMP) stmts.get(stmts.size() - 1);
                var targetLb = cjmp.getThenBlock();
                var elseLb = cjmp.getElseBlock();
                entries.add(targetLb);
                entries.add(elseLb);
                return Optional.of(entries);
            }
            case END_BY_JMP -> {
                JMP cjmp = (JMP) stmts.get(stmts.size() - 1);
                var tid = cjmp.getNext();
                entries.add(tid);
                return Optional.of(entries);
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    @Override
    public int compareTo(@NotNull LinearIRBlock o) {
        return  ord - o.getOrd();
    }

    @Override
    public int hashCode() {
        return (ord * 177) % 71;
    }

    /**
     *
     * @param otherBlock 必须是相邻的基本块才能进行合并
     */
    public void mergeBlock(LinearIRBlock otherBlock) {
        stmts.addAll(otherBlock.getStmts());

        updateKindByLastInstr(stmts.get(stmts.size() - 1));

        setSuccessors(otherBlock.getSuccessors());

        for(var s : otherBlock.getSuccessors()) {
            s.predecessors.remove(otherBlock);
            s.predecessors.add(this);
        }

        for (var jmp : otherBlock.getJmpRefMap()) {
            if (jmp instanceof JMP jmpInstr) {
                jmpInstr.setNext(this);
            } else {
                var jmpInstr = (CJMP) jmp;
                jmpInstr.setElseBlock(this);
                jmpInstr.setThenBlock(this);
            }
        }
    }

    public void removeSuccessor(LinearIRBlock block) {
        successors.remove(block);
    }
}