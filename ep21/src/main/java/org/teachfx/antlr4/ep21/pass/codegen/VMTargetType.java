package org.teachfx.antlr4.ep21.pass.codegen;

public enum VMTargetType {
    STACK_VM("EP18"),
    REGISTER_VM("EP18R");

    private final String identifier;

    VMTargetType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
