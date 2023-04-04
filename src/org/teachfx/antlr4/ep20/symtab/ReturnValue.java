package org.teachfx.antlr4.ep20.symtab;

public class ReturnValue extends Error {
    /**
     *
     */
    private static final long serialVersionUID = 5678639470613659555L;
    public Object value;

    public ReturnValue(Object value) {
        super("ReturnValue");
        this.value = value;
    }

}
