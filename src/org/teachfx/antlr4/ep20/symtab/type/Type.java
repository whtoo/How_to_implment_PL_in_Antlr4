package org.teachfx.antlr4.ep20.symtab.type;

public interface Type {
    String getName();

    boolean isPrimitive();

    boolean isFunc();

    public Type getFuncType();
    public Type getPrimitiveType();
}
