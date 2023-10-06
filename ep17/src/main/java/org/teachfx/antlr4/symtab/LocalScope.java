package org.teachfx.antlr4.ep17.symtab;

public class LocalScope extends BaseScope {

    public LocalScope(Scope parent) {
        super(parent);
    }

    @Override
    public String getScopeName() {
        return "Local";
    }
}
