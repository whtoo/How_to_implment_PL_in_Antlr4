package org.teachfx.antlr4.ep19.symtab;

public class ReturnValue extends Error {
    public Object value;
    /**
     *
     */
    private static final long serialVersionUID = 5678639470613659555L;

    public ReturnValue(Object value) { 
        super("ReturnValue");
        this.value = value;
    } 
    
}
