package org.teachfx.antlr4.ep20.symtab.type;

public class ArrayType implements Type {
    private final Type elementType;
    private final int size;

    public ArrayType(Type elementType, int size) {
        this.elementType = elementType;
        this.size = size;
    }

    public Type getElementType() {
        return elementType;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String getName() {
        return elementType.getName() + "[" + size + "]";
    }

    @Override
    public boolean isPreDefined() {
        return false;
    }

    @Override
    public boolean isFunc() {
        return false;
    }

    @Override
    public Type getFuncType() {
        return null;
    }

    @Override
    public Type getPrimitiveType() {
        return elementType;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }
}