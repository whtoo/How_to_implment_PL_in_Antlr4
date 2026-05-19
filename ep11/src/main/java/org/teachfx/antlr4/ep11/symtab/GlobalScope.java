package org.teachfx.antlr4.ep11.symtab;

public class GlobalScope extends BaseScope {
    public GlobalScope() {
        super(null);
        define(TypeTable.INT);
        define(TypeTable.FLOAT);
        define(TypeTable.VOID);
        define(TypeTable.BOOL);
        define(TypeTable.STRING);
    }

    @Override
    public String getScopeName() {
        return "global";
    }
}
