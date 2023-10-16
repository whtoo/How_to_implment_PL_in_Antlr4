package org.teachfx.antlr4.ep21.symtab.type;

public interface Type {
    String getName();

    boolean isPreDefined();

    boolean isFunc();

    public Type getFuncType();
    public Type getPrimitiveType();


}
