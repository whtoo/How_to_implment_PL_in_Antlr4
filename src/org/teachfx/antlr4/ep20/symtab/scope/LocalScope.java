package org.teachfx.antlr4.ep20.symtab.scope;

public class LocalScope extends BaseScope {

    public LocalScope(Scope parent) {
        super(parent);
    }

    @Override
    public String getScopeName() {
        return "Local-"+getEnclosingScope().getScopeName();
    }
}
