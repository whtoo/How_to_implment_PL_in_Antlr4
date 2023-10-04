package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;

import java.util.Objects;

public class Label extends Stmt {

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

    public String getRawLabel() {
        return rawLabel;
    }

    public void setRawLabel(String rawLabel) {
        this.rawLabel = rawLabel;
    }

    public Scope getScope() {
        return scope;
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
            return scope.getScopeName() + "_" + rawLabel + ":";
        }

        return scope.getScopeName() + "_" + seq + ":";
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.LABEL;
    }
}
