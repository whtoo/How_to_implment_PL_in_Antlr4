package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;

import java.util.Objects;

public class Label extends Stmt {

    private Stmt nextEntry = null;

    private String rawLabel;


    private Scope scope;
    protected int seq;

    public Label(String rawLabel, Scope scope) {
        this.rawLabel = rawLabel;
        this.scope = scope;
        if (Objects.isNull(rawLabel)) {
            this.seq = scope.getLabelSeq();
        }
    }

    public Label(Scope scope) {
        this.scope = scope;
        this.seq = scope.getLabelSeq();
        this.rawLabel = null;
    }

    public void setRawLabel(String rawLabel) {
        this.rawLabel = rawLabel;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }

    public String toSource() {
        if (Objects.nonNull(rawLabel)) {
            return rawLabel ;
        }

        return scope.getScopeName() + "_" + seq ;
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.LABEL;
    }

    @Override
    public String toString() {
        return toSource();
    }

    public void setNextEntry(Stmt nextEntry) {
        this.nextEntry = nextEntry;
    }

    public boolean hasNextEntry() {
        return Objects.nonNull(nextEntry) && nextEntry.getStmtType() != StmtType.LABEL;
    }

    public Stmt fetchNextJumpEntry() {
        var item = getNextEntry();
        Stmt stmtEntry = item;
        while (item.getStmtType() == StmtType.LABEL) {
            item = ((Label) item).getNextEntry();
        }

        return item;
    }

    public Stmt getNextEntry() {
        return nextEntry;
    }

    public String getRawLabel() {
        return rawLabel;
    }

    public Scope getScope() {
        return scope;
    }

    public int getSeq() {
        return seq;
    }
}
