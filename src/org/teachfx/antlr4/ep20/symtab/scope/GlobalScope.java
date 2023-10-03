package org.teachfx.antlr4.ep20.symtab.scope;

public class GlobalScope extends BaseScope {

    public GlobalScope() {
        super(null);

    }

    @Override
    public String getScopeName() {
        return "gloabl";
    }

}
