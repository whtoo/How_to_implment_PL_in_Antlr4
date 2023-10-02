package org.teachfx.antlr4.ep20.ir;

import org.teachfx.antlr4.ep20.ir.stmt.Stmt;

import java.util.List;

public class Prog extends IRNode {
    protected List<Stmt> stmts;

    public Prog(List<Stmt> stmts) {
        this.stmts = stmts;
    }


}
