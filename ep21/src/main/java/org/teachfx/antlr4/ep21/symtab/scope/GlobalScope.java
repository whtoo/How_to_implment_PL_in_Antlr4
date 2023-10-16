package org.teachfx.antlr4.ep21.symtab.scope;

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

    @Override
    public int setBaseVarSlotSeq(int baseVarSlotSeq) {
        return 0;
    }

    @Override
    public int getVarSlots() {
        return VAR_SLOT_SEQ;
    }

}
