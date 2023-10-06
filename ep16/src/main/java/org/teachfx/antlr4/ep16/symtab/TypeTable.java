package org.teachfx.antlr4.ep16.symtab;

public class TypeTable {
    // Define builtin types
    public static BuiltInTypeSymbol INT = new BuiltInTypeSymbol("int");
    public static BuiltInTypeSymbol FLOAT = new BuiltInTypeSymbol("float");
    public static BuiltInTypeSymbol DOUBLE = new BuiltInTypeSymbol("double");
    public static BuiltInTypeSymbol CHAR = new BuiltInTypeSymbol("char");
    public static BuiltInTypeSymbol VOID = new BuiltInTypeSymbol("void");
    public static BuiltInTypeSymbol NULL = new BuiltInTypeSymbol("null");
    public static BuiltInTypeSymbol BOOLEAN = new BuiltInTypeSymbol("bool");
    public static BuiltInTypeSymbol OBJECT = new BuiltInTypeSymbol("object");
    // Define true and false value;
    public static Integer TRUE = 1;
    public static Integer FALSE = 0;


}
