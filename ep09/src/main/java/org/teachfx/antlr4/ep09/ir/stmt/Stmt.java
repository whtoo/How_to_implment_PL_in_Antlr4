package org.teachfx.antlr4.ep09.ir.stmt;

import org.teachfx.antlr4.ep09.ir.IRNode;
import org.teachfx.antlr4.ep09.ir.IRVisitor;

public abstract class Stmt extends IRNode {
    public enum StmtType {
        JMP,
        CJMP,
        ASSIGN,
        ARRAY_ASSIGN,
        LABEL,
        RETURN,
        EXPR
    }
    public abstract <S,E> S accept(IRVisitor<S,E> visitor);

    public abstract StmtType getStmtType();

}
