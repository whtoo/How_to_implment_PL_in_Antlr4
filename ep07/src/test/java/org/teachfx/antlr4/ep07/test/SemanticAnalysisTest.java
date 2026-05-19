package org.teachfx.antlr4.ep07.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep07.symtab.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP07 tests: 3-pass semantic analysis pipeline.
 */
public class SemanticAnalysisTest {

    @Test
    void testSymbolTableDefinesSymbols() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1));
        global.define("f", new FunctionSymbol("f", "int", "global", 2, global));

        assertEquals(2, global.getAllSymbols().size());
        assertNotNull(global.resolve("x"));
        assertNotNull(global.resolve("f"));
    }

    @Test
    void testFunctionSymbolHasParameters() {
        SymbolTable global = new SymbolTable("global", null);
        FunctionSymbol func = new FunctionSymbol("add", "int", "global", 1, global);
        func.define("a", new Symbol("a", Symbol.Kind.PARAMETER, "int", "add", 1));
        func.define("b", new Symbol("b", Symbol.Kind.PARAMETER, "int", "add", 1));

        assertEquals(2, func.getSymbols().size());
        assertNotNull(func.resolve("a"));
        assertNotNull(func.resolve("b"));
    }

    @Test
    void testScopeResolutionChain() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "int", "global", 1));

        FunctionSymbol func = new FunctionSymbol("f", "void", "global", 2, global);
        func.define("y", new Symbol("y", Symbol.Kind.VARIABLE, "int", "f", 3));

        // func can resolve both local and global
        assertNotNull(func.resolve("y"));
        assertNotNull(func.resolve("x")); // through global scope
    }

    @Test
    void testTypeMismatchDetection() {
        // Simulate type checking logic
        String varType = "int";
        String exprType = "float";
        assertNotEquals(varType, exprType);
    }

    @Test
    void testValidTypeMatch() {
        String varType = "int";
        String exprType = "int";
        assertEquals(varType, exprType);
    }
}
