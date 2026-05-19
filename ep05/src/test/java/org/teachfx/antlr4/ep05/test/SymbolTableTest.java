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

    @Test
    void testFunctionSymbolHasOwnScope() {
        SymbolTable global = new SymbolTable("global", null);
        FunctionSymbol func = new FunctionSymbol("add", "int", "global", 1, global);
        global.define("add", func);

        Symbol paramA = new Symbol("a", Symbol.Kind.PARAMETER, "int", "add", 1);
        Symbol paramB = new Symbol("b", Symbol.Kind.PARAMETER, "int", "add", 1);
        func.define("a", paramA);
        func.define("b", paramB);

        assertNotNull(func.resolve("a"));
        assertNotNull(func.resolve("b"));
        assertNull(global.resolve("a"));
    }

    @Test
    void testResolveLocalOnly() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1));

        SymbolTable local = new SymbolTable("local", global);
        local.define("y", new Symbol("y", Symbol.Kind.VARIABLE, "float", "local", 2));

        assertNotNull(local.resolveLocal("y"));
        assertNull(local.resolveLocal("x"));
    }

    @Test
    void testMultipleNestedScopes() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("g", new Symbol("g", Symbol.Kind.VARIABLE, "int", "global", 1));

        SymbolTable outer = new SymbolTable("outer", global);
        outer.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "int", "outer", 2));

        SymbolTable inner = new SymbolTable("inner", outer);
        inner.define("y", new Symbol("y", Symbol.Kind.VARIABLE, "int", "inner", 3));

        // inner can resolve all
        assertNotNull(inner.resolve("y"));
        assertNotNull(inner.resolve("x"));
        assertNotNull(inner.resolve("g"));

        // outer can resolve outer+global but not inner
        assertNotNull(outer.resolve("x"));
        assertNotNull(outer.resolve("g"));
        assertNull(outer.resolve("y"));

        // global can only resolve global
        assertNotNull(global.resolve("g"));
        assertNull(global.resolve("x"));
        assertNull(global.resolve("y"));
    }

    @Test
    void testSymbolKindEnum() {
        assertEquals(Symbol.Kind.VARIABLE, Symbol.Kind.valueOf("VARIABLE"));
        assertEquals(Symbol.Kind.PARAMETER, Symbol.Kind.valueOf("PARAMETER"));
        assertEquals(Symbol.Kind.FUNCTION, Symbol.Kind.valueOf("FUNCTION"));
    }

    @Test
    void testScopeGetName() {
        SymbolTable global = new SymbolTable("myProgram", null);
        assertEquals("myProgram", global.getScopeName());
    }

    @Test
    void testGetAllSymbols() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("a", new Symbol("a", Symbol.Kind.VARIABLE, "int", "global", 1));
        global.define("b", new Symbol("b", Symbol.Kind.VARIABLE, "float", "global", 2));

        var all = global.getAllSymbols();
        assertEquals(2, all.size());
    }

    @Test
    void testRedeclarationOverwrites() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1));
        global.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "float", "global", 2));

        assertEquals("float", global.resolve("x").getTypeName());
    }
}
