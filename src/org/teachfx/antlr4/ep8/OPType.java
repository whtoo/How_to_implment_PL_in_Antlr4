package org.teachfx.antlr4.ep8;

public enum OPType {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/");

    String text;

    private OPType(String str) {
        this.text = str;
    }
    
    public String getText() {
        return this.text;
    }
}
