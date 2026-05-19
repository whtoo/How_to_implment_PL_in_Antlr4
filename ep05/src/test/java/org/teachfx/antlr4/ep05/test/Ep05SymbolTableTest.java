package org.teachfx.antlr4.ep05.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep05.symtab.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP05 — Symbols: What Names Mean
 *
 * Tests symbol table construction, scope nesting, and name resolution.
 */
class Ep05SymbolTableTest {

    @Test
    void defineAndResolveInSameScope() {
        SymbolTable global = new SymbolTable("global", null);
        Symbol x = new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1);
        global.define("x", x);

        Symbol found = global.resolve("x");
        assertNotNull(found);
        assertEquals("x", found.getName());
        assertEquals(Symbol.Kind.VARIABLE, found.getKind());
        assertEquals("int", found.getTypeName());
    }

    @Test
    void resolveFromNestedScope() {
        SymbolTable global = new SymbolTable("global", null);
        Symbol x = new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1);
        global.define("x", x);

        SymbolTable local = new SymbolTable("local", global);
        Symbol y = new Symbol("y", Symbol.Kind.VARIABLE, "float", "local", 2);
        local.define("y", y);

        // Local scope can resolve its own symbols
        assertNotNull(local.resolve("y"));
        assertEquals("y", local.resolve("y").getName());

        // Local scope can resolve symbols from enclosing scope
        assertNotNull(local.resolve("x"));
        assertEquals("x", local.resolve("x").getName());

        // Global scope cannot resolve local symbols
        assertNull(global.resolve("y"));
    }

    @Test
    void functionSymbolHasItsOwnScope() {
        SymbolTable global = new SymbolTable("global", null);
        FunctionSymbol func = new FunctionSymbol("add", "int", "global", 1, global);
        global.define("add", func);

        Symbol paramA = new Symbol("a", Symbol.Kind.PARAMETER, "int", "add", 1);
        Symbol paramB = new Symbol("b", Symbol.Kind.PARAMETER, "int", "add", 1);
        func.define("a", paramA);
        func.define("b", paramB);

        // Function resolves its own parameters
        assertNotNull(func.resolve("a"));
        assertNotNull(func.resolve("b"));

        // Function resolves global symbols through enclosing scope
        Symbol globalVar = new Symbol("g", Symbol.Kind.VARIABLE, "int", "global", 1);
        global.define("g", globalVar);
        assertNotNull(func.resolve("g"));

        // Global scope does not resolve function-local parameters
        assertNull(global.resolve("a"));
    }

    @Test
    void shadowingInNestedScope() {
        SymbolTable global = new SymbolTable("global", null);
        Symbol globalX = new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1);
        global.define("x", globalX);

        SymbolTable local = new SymbolTable("local", global);
        Symbol localX = new Symbol("x", Symbol.Kind.VARIABLE, "float", "local", 2);
        local.define("x", localX);

        // Local scope finds its own x first (shadowing)
        Symbol found = local.resolve("x");
        assertEquals("float", found.getTypeName());
        assertEquals("local", found.getScopeName());
    }

    @Test
    void resolveLocalOnly() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1));

        SymbolTable local = new SymbolTable("local", global);
        local.define("y", new Symbol("y", Symbol.Kind.VARIABLE, "float", "local", 2));

        assertNotNull(local.resolveLocal("y"));
        assertNull(local.resolveLocal("x")); // not found locally
    }
}
