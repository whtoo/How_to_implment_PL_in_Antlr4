package org.teachfx.antlr4.ep19.symtab;

import org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol;

public class TypeTable {
    // Define builtin types
    public static org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol INT = new org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol("int");
    public static org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol FLOAT = new org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol("float");
    public static org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol DOUBLE = new org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol("double");
    public static org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol CHAR = new org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol("char");
    public static org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol VOID = new org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol("void");
    public static org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol NULL = new org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol("null");
    public static org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol BOOLEAN = new org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol("bool");
    public static org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol OBJECT = new org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol("Object");
    public static org.teachfx.antlr4.ep19.symtab.symbol.BuiltInTypeSymbol STRING = new BuiltInTypeSymbol("String");

    // Define true and false value;
    public static Integer TRUE = 1;
    public static Integer FALSE = 0;


}
