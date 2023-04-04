package org.teachfx.antlr4.ep19.symtab;

public class GlobalScope extends BaseScope {

    public GlobalScope() {
        super(null);

    }

    @Override
    public String getScopeName() {
        return "gloabl";
    }

}
