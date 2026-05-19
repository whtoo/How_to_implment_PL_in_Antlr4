package org.teachfx.antlr4.ep06.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep06.symtab.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP06 — Type System
 *
 * Tests type table lookup, type equality, and basic type properties.
 */
class Ep06TypeTest {

    @Test
    void builtInTypesExist() {
        assertNotNull(TypeTable.INT);
        assertNotNull(TypeTable.FLOAT);
        assertNotNull(TypeTable.BOOL);
        assertNotNull(TypeTable.VOID);
        assertNotNull(TypeTable.ERROR);
    }

    @Test
    void typeNamesAreCorrect() {
        assertEquals("int", TypeTable.INT.getName());
        assertEquals("float", TypeTable.FLOAT.getName());
        assertEquals("bool", TypeTable.BOOL.getName());
        assertEquals("void", TypeTable.VOID.getName());
        assertEquals("<error>", TypeTable.ERROR.getName());
    }

    @Test
    void allBuiltInTypesArePrimitive() {
        assertTrue(TypeTable.INT.isPrimitive());
        assertTrue(TypeTable.FLOAT.isPrimitive());
        assertTrue(TypeTable.BOOL.isPrimitive());
        assertTrue(TypeTable.VOID.isPrimitive());
        assertTrue(TypeTable.ERROR.isPrimitive());
    }

    @Test
    void typeEquality() {
        assertEquals(TypeTable.INT, TypeTable.INT);
        assertNotEquals(TypeTable.INT, TypeTable.FLOAT);
        assertNotEquals(TypeTable.INT, TypeTable.BOOL);
    }

    @Test
    void lookupByName() {
        assertEquals(TypeTable.INT, TypeTable.fromName("int"));
        assertEquals(TypeTable.FLOAT, TypeTable.fromName("float"));
        assertEquals(TypeTable.BOOL, TypeTable.fromName("bool"));
        assertEquals(TypeTable.VOID, TypeTable.fromName("void"));
        assertNull(TypeTable.fromName("unknown"));
    }

    @Test
    void symbolCarriesTypeName() {
        Symbol x = new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1);
        assertEquals("int", x.getTypeName());
        assertEquals("x", x.getName());
        assertEquals(Symbol.Kind.VARIABLE, x.getKind());
    }

    @Test
    void functionSymbolHasReturnType() {
        SymbolTable global = new SymbolTable("global", null);
        FunctionSymbol func = new FunctionSymbol("add", "int", "global", 1, global);
        assertEquals("int", func.getTypeName());
        assertEquals("add", func.getScopeName());
    }
}
