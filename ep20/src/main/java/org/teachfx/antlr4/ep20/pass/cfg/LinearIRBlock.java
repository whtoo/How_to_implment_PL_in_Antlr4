package org.teachfx.antlr4.ep20.pass.cfg;

import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.stmt.*;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;
import org.teachfx.antlr4.ep20.utils.Kind;

import java.util.ArrayList;
import java.util.List;

public class LinearIRBlock {

    // Fields
    private static int LABEL_SEQ = 1;
    private Kind kind = Kind.CONTINUOUS;
    private int ord = 1;
    private ArrayList<IRNode> stmts;
    private List<LinearIRBlock> successors;
    private List<LinearIRBlock> predecessors;
    private Scope scope = null;
    private List<IRNode> jmpRefMap = new ArrayList<>();

    // Constructor
    public LinearIRBlock() {
        stmts = new ArrayList<>();
        successors = new ArrayList<>();
        predecessors = new ArrayList<>();
        ord = LABEL_SEQ++;
    }

    // Methods
    // Statement Operations
    public void addStmt(IRNode stmt) {
        stmts.add(stmt);
        updateKindByLastInstr(stmt);
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

    public static boolean isBasicBlock(Stmt stmt) {
        return !(stmt instanceof CJMP) && !(stmt instanceof JMP);
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

    public List<IRNode> getJmpRefMap() {
        return jmpRefMap;
    }

    public void setJmpRefMap(List<IRNode> jmpRefMap) {
        this.jmpRefMap = jmpRefMap;
    }

    public int getOrd() {
        return ord;
    }

    // Link Operations
    public static void setLink(LinearIRBlock current, LinearIRBlock next) {
        current.successors.add(next);
        next.predecessors.add(current);
    }

    public void setLink(LinearIRBlock next) {
        LinearIRBlock.setLink(this, next);
    }

    public Kind getKind() {
        return kind;
    }

    // Jump Operations
    public void refJMP(IRNode node) {
        jmpRefMap.add(node);
    }

    // Utility Methods
    @Override
    public String toString() {
        var firstInstr = stmts.get(0);

        if (firstInstr instanceof FuncEntryLabel) {
            return firstInstr.toString();
        }
        return "L" + ord;
    }

    public String toSource(){
        var firstInstr = stmts.get(0);

        if (firstInstr instanceof FuncEntryLabel) {
            return ((FuncEntryLabel) firstInstr).toSource();
        }

        return "L" + ord;
    }

    public Label getLabel() {
        var firstInstr = stmts.get(0);

        if (firstInstr instanceof FuncEntryLabel funcEntryLabel) {
            return funcEntryLabel;
        }

        return new Label(toString(),scope);
    }
}