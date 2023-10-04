package org.teachfx.antlr4.ep20.symtab.scope;

public class LocalScope extends BaseScope {
    // Generate seq code for local variables follow GlobalScope
    static int LABEL_SEQ = 0;
    static int VAR_SLOT_SEQ = 0;
    public LocalScope(Scope parent) {
        super(parent);
    }

    @Override
    public String getScopeName() {
        return "Local-"+getEnclosingScope().getScopeName();
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
