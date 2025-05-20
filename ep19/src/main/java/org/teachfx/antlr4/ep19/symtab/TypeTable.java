package org.teachfx.antlr4.ep19.symtab;

import org.teachfx.antlr4.ep19.symtab.symbol.PrimitiveType;

public class TypeTable {
    public static final Type VOID = new PrimitiveType("void");
    public static final Type INT = new PrimitiveType("int");
    public static final Type FLOAT = new PrimitiveType("float");
    public static final Type BOOLEAN = new PrimitiveType("bool");
    public static final Type CHAR = new PrimitiveType("char");
    public static final Type NULL = new PrimitiveType("null");
    public static final Type STRING = new PrimitiveType("String");
    public static final Type OBJECT = new PrimitiveType("Object");

    // Define true and false value;
    public static final Integer TRUE = 1;
    public static final Integer FALSE = 0;
    
    // 由名称获取类型
    public static Type getTypeByName(String name) {
        switch (name) {
            case "void": return VOID;
            case "int": return INT;
            case "float": return FLOAT;
            case "bool": return BOOLEAN;
            case "char": return CHAR;
            case "null": return NULL;
            case "String": return STRING;
            case "Object": return OBJECT;
            default: return null;
        }
    }
}
