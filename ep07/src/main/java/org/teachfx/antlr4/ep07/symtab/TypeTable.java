package org.teachfx.antlr4.ep07.symtab;

public class TypeTable {
    public static final Type VOID = new BuiltInType("void");
    public static final Type INT = new BuiltInType("int");
    public static final Type FLOAT = new BuiltInType("float");
    public static final Type BOOL = new BuiltInType("bool");
    public static final Type ERROR = new BuiltInType("<error>");

    public static Type fromName(String name) {
        return switch (name) {
            case "void" -> VOID;
            case "int" -> INT;
            case "float" -> FLOAT;
            case "bool" -> BOOL;
            default -> null;
        };
    }
}
