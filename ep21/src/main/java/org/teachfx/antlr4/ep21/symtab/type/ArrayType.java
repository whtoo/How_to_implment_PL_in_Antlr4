package org.teachfx.antlr4.ep21.symtab.type;

/**
 * ArrayType represents an array type in the EP21 type system.
 * This class is ported from EP19 and adapted to the EP21 Type interface.
 * Currently supports single-dimensional arrays.
 *
 * Implementation notes:
 * - Stores element type (e.g., int for int[])
 * - Does NOT store array size (size is a runtime property, not type property)
 * - Single-dimensional only (multi-dimensional arrays would require dimension tracking)
 * - Implements EP21 Type interface with all required methods
 *
 * @author Ported from EP19 to EP21
 */
public class ArrayType implements Type {
    private final Type elementType;
    private final String name;

    /**
     * Creates a new ArrayType with the specified element type.
     *
     * @param elementType the type of array elements (must not be null)
     * @throws IllegalArgumentException if elementType is null
     */
    public ArrayType(Type elementType) {
        if (elementType == null) {
            throw new IllegalArgumentException("Array element type cannot be null");
        }
        this.elementType = elementType;
        this.name = elementType.getName() + "[]";
    }

    /**
     * Gets the element type of this array.
     * For example, for int[], this returns the INT type.
     *
     * @return the element type
     */
    public Type getElementType() {
        return elementType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isPreDefined() {
        // Arrays are compound types, not predefined primitive types
        return false;
    }

    @Override
    public boolean isFunc() {
        // Arrays are not function types
        return false;
    }

    @Override
    public Type getFuncType() {
        // Arrays don't have function types
        return null;
    }

    @Override
    public Type getPrimitiveType() {
        // For arrays, the primitive type is the element type
        return elementType.getPrimitiveType();
    }

    @Override
    public boolean isVoid() {
        // Arrays are never void
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        // Two array types are equal if they have the same element type
        if (elementType != null ? !elementType.equals(arrayType.elementType) : arrayType.elementType != null) {
            return false;
        }
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
