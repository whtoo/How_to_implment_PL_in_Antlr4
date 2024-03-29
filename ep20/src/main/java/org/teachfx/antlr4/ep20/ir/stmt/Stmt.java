package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.IRVisitor;

public abstract class Stmt extends IRNode {
    public enum StmtType {
        JMP,
        CJMP,
        ASSIGN,
        LABEL,
        RETURN,
        EXPR
    }
    public abstract <S,E> S accept(IRVisitor<S,E> visitor);

    public abstract StmtType getStmtType();

}
