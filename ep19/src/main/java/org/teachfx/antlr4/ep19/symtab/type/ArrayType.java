package org.teachfx.antlr4.ep19.symtab.type;

import org.teachfx.antlr4.ep19.symtab.Type;

public class ArrayType implements Type {
    private Type elementType;
    private String name;

    public ArrayType(Type elementType) {
        if (elementType == null) {
            // Handle cases where element type might not be resolved, though ideally it should be.
            // For now, let's assign a default name or throw an error.
            // This case might indicate an issue upstream (e.g. in LocalResolver).
            this.elementType = null; // Or some placeholder 'unknown' type
            this.name = "unknown[]";
        } else {
            this.elementType = elementType;
            this.name = elementType.getName() + "[]";
        }
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isPrimitive() {
        return false; // Arrays are generally considered compound types, not primitive.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        if (elementType != null ? !elementType.equals(arrayType.elementType) : arrayType.elementType != null) return false;
        return name.equals(arrayType.name);
    }

    @Override
    public int hashCode() {
        int result = elementType != null ? elementType.hashCode() : 0;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }
}
