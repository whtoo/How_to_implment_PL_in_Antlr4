package org.teachfx.antlr4.ep08.symtab;

public class LocalScope extends BaseScope {
    public LocalScope(Scope parent) {
        super(parent);
    }

    @Override
    public String getScopeName() {
        return "local";
    }
}
