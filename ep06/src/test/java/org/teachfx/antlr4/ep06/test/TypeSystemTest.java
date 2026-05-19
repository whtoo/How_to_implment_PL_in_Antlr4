package org.teachfx.antlr4.ep06.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep06.symtab.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP06 tests: Type system — type checking and compatibility.
 */
public class TypeSystemTest {

    @Test
    void testBuiltInTypeEquality() {
        Type intType1 = new BuiltInType("int");
        Type intType2 = new BuiltInType("int");
        Type floatType = new BuiltInType("float");

        assertEquals(intType1, intType2);
        assertNotEquals(intType1, floatType);
    }

    @Test
    void testTypeTableLookup() {
        assertNotNull(TypeTable.fromName("int"));
        assertNotNull(TypeTable.fromName("float"));
        assertNull(TypeTable.fromName("unknown"));
    }

    @Test
    void testPrimitiveTypesArePrimitive() {
        Type intType = new BuiltInType("int");
        assertTrue(intType.isPrimitive());
    }

    @Test
    void testSymbolHasType() {
        Symbol s = new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1);
        assertEquals("int", s.getTypeName());
    }

    @Test
    void testTypeNameFormatting() {
        Type t = new BuiltInType("float");
        assertEquals("float", t.getName());
        assertEquals("float", t.toString());
    }
}
