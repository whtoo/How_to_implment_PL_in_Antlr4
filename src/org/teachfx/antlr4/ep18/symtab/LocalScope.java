package org.teachfx.antlr4.ep18.symtab;

public class LocalScope extends BaseScope {

    public LocalScope(Scope parent) {
        super(parent);
    }

    @Override
    public String getScopeName() {
        return "Local";
    }
}
