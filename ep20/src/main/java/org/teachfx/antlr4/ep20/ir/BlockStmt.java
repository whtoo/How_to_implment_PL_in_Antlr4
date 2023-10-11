package org.teachfx.antlr4.ep20.ir;

import org.teachfx.antlr4.ep20.ir.stmt.Stmt;

import java.util.LinkedList;
import java.util.List;

public class BlockStmt {

    private List<Stmt> stmts = new LinkedList<>();

    public List<Stmt> getStmts() {
        return stmts;
    }

    public void setStmts(List<Stmt> stmts) {
        this.stmts = stmts;
    }

}
