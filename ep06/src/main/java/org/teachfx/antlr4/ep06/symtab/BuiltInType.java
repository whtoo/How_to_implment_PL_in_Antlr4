package org.teachfx.antlr4.ep06.symtab;

public class BuiltInType implements Type {
    private final String name;

    public BuiltInType(String name) { this.name = name; }

    @Override public String getName() { return name; }
    @Override public boolean isPrimitive() { return true; }
    @Override public String toString() { return name; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuiltInType b)) return false;
        return name.equals(b.name);
    }
    @Override public int hashCode() { return name.hashCode(); }
}
