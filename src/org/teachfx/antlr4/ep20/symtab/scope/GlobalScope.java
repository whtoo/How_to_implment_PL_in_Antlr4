package org.teachfx.antlr4.ep20.symtab.scope;

public class GlobalScope extends BaseScope {
    static int LABEL_SEQ = 0;
    static int VAR_SLOT_SEQ = 0;
    public GlobalScope() {
        super(null);

    }

    @Override
    public String getScopeName() {
        return "gloabl";
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
