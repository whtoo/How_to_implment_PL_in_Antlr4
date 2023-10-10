package org.teachfx.antlr4.ep20.symtab.scope;

import org.teachfx.antlr4.ep20.ast.stmt.ScopeType;

public class LocalScope extends BaseScope {
    // Generate seq code for local variables follow GlobalScope
    static int LABEL_SEQ = 0;
    static int VAR_SLOT_SEQ = 0;
    public LocalScope(Scope parent) {
        super(parent);
        this.setScopeType(ScopeType.BlockScope);
    }

    public LocalScope(Scope parent, ScopeType scopeType) {
        super(parent);
        this.setScopeType(scopeType);
    }

    @Override
    public String getScopeName() {
        return "Local_"+getEnclosingScope().getScopeName();
    }

    @Override
    public int getLabelSeq() {
        return LABEL_SEQ++;
   }


    @Override
    public int getVarSlotSeq() {
        return VAR_SLOT_SEQ++;
    }



}
