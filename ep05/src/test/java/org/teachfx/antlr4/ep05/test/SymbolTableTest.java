package org.teachfx.antlr4.ep05.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep05.symtab.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP05 tests: Symbol table — scopes and symbol resolution.
 */
public class SymbolTableTest {

    @Test
    void testDefineAndResolve() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1));

        Symbol s = global.resolve("x");
        assertNotNull(s);
        assertEquals("x", s.getName());
        assertEquals("int", s.getTypeName());
    }

    @Test
    void testResolveUndefinedReturnsNull() {
        SymbolTable global = new SymbolTable("global", null);
        assertNull(global.resolve("y"));
    }

    @Test
    void testNestedScopeResolution() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1));

        SymbolTable local = new SymbolTable("local", global);
        local.define("y", new Symbol("y", Symbol.Kind.VARIABLE, "int", "local", 2));

        // Can resolve local symbol in local scope
        assertNotNull(local.resolve("y"));
        // Can resolve global symbol from local scope
        assertNotNull(local.resolve("x"));
        // Cannot resolve local symbol from global scope
        assertNull(global.resolve("y"));
    }

    @Test
    void testScopeShadowing() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1));

        SymbolTable local = new SymbolTable("local", global);
        local.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "float", "local", 2));

        Symbol s = local.resolve("x");
        assertEquals("float", s.getTypeName()); // local shadows global
    }

    @Test
    void testFunctionSymbolScope() {
        SymbolTable global = new SymbolTable("global", null);
        FunctionSymbol func = new FunctionSymbol("f", "int", "global", 1, global);
        func.define("a", new Symbol("a", Symbol.Kind.PARAMETER, "int", "f", 1));

        global.define("f", func);

        assertNotNull(global.resolve("f"));
        assertNotNull(func.resolve("a"));
    }
}
