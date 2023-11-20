package org.teachfx.antlr4.ep21.pass.cfg;

import org.teachfx.antlr4.ep21.ir.IRNode;

import java.util.List;

public class BasicBlock {

    List<IRNode> stmts = List.of();

    public boolean isEmpty() {
        return stmts.isEmpty();
    }

    public void addStmt(IRNode stm) {
        stmts.add(stm);
    }

    public List<IRNode> getStmts() {
        return stmts;
    }

    // Get size of `stmts`
    public int size() {
        return stmts.size();
    }
}
