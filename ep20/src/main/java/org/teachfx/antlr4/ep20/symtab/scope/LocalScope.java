package org.teachfx.antlr4.ep20.symtab.scope;

import org.teachfx.antlr4.ep20.ast.stmt.ScopeType;
import org.teachfx.antlr4.ep20.symtab.symbol.Symbol;

public class LocalScope extends BaseScope {
    // Generate seq code for local variables follow GlobalScope
    static int LABEL_SEQ = 0;
    static int VAR_SLOT_SEQ = 0;
    public LocalScope(Scope parent) {
        this(parent, ScopeType.BlockScope);
    }

    public LocalScope(Scope parent, ScopeType scopeType) {
        super(parent);
        setBaseVarSlotSeq(parent.getVarSlots());
        setScopeType(scopeType);
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
    public void define(Symbol sym) {
        super.define(sym);
        if (sym.isBuiltIn()) return;
        sym.setSlotIdx(getVarSlotSeq());
    }

    @Override
    public int getVarSlotSeq() {
        return VAR_SLOT_SEQ++;
    }


    @Override
    public int getVarSlots() {
        return VAR_SLOT_SEQ;
    }

    @Override
    public int setBaseVarSlotSeq(int baseVarSlotSeq) {
        return (VAR_SLOT_SEQ = baseVarSlotSeq);
    }
}
