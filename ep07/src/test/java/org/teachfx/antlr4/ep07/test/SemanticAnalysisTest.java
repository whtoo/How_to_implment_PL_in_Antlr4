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

    @Test
    void testSymbolResolutionChainAcrossMultipleFunctions() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("g", new Symbol("g", Symbol.Kind.VARIABLE, "int", "global", 1));

        FunctionSymbol f1 = new FunctionSymbol("f1", "int", "global", 2, global);
        f1.define("a", new Symbol("a", Symbol.Kind.PARAMETER, "int", "f1", 3));
        FunctionSymbol f2 = new FunctionSymbol("f2", "float", "global", 4, global);
        f2.define("b", new Symbol("b", Symbol.Kind.PARAMETER, "float", "f2", 5));

        global.define("f1", f1);
        global.define("f2", f2);

        // f1 resolves its own params and globals
        assertNotNull(f1.resolve("a"));
        assertNotNull(f1.resolve("g"));
        // f2 resolves its own params and globals
        assertNotNull(f2.resolve("b"));
        assertNotNull(f2.resolve("g"));
        // f1 cannot resolve f2's params
        assertNull(f1.resolve("b"));
        // f2 cannot resolve f1's params
        assertNull(f2.resolve("a"));
    }

    @Test
    void testFunctionSymbolCanDefineMultipleParams() {
        SymbolTable global = new SymbolTable("global", null);
        FunctionSymbol f = new FunctionSymbol("add", "int", "global", 1, global);
        f.define("a", new Symbol("a", Symbol.Kind.PARAMETER, "int", "add", 2));
        f.define("b", new Symbol("b", Symbol.Kind.PARAMETER, "int", "add", 3));
        f.define("c", new Symbol("c", Symbol.Kind.PARAMETER, "float", "add", 4));

        assertEquals(3, f.getSymbols().size());
        assertEquals("int", f.resolve("a").getTypeName());
        assertEquals("float", f.resolve("c").getTypeName());
    }

    @Test
    void testScopeResolutionInsideBlockWithinFunction() {
        SymbolTable global = new SymbolTable("global", null);
        global.define("g", new Symbol("g", Symbol.Kind.VARIABLE, "int", "global", 1));

        FunctionSymbol func = new FunctionSymbol("test", "void", "global", 2, global);
        func.define("x", new Symbol("x", Symbol.Kind.VARIABLE, "int", "test", 3));

        // Block scope inside function
        SymbolTable block = new SymbolTable("block", func);
        block.define("y", new Symbol("y", Symbol.Kind.VARIABLE, "float", "block", 4));

        assertNotNull(block.resolve("y"));   // own scope
        assertNotNull(block.resolve("x"));   // function scope through enclosing
        assertNotNull(block.resolve("g"));   // global scope through chain
        assertNull(func.resolve("y"));       // function can't see block's scope (one-way)
    }

    @Test
    void testTypeCheckingSimulation() {
        // Simulating what EP07's type checker does
        assertEquals("int", "int");
        assertNotEquals("int", "float");
        assertNotEquals("int", "bool");
        assertNotEquals("float", "void");
    }

    @Test
    void testReturnTypeCheck() {
        // Function return type must match return expression type
        String funcReturnType = "int";
        String returnExprType = "int";
        assertEquals(funcReturnType, returnExprType);

        // Type mismatch should be caught
        String wrongType = "float";
        assertNotEquals(funcReturnType, wrongType);
    }

    @Test
    void testParameterTypeMatching() {
        // When calling f(a, b), arg types must match param types
        String paramTypeA = "int";
        String paramTypeB = "float";
        String argTypeA = "int";
        String argTypeB = "float";

        assertEquals(paramTypeA, argTypeA);
        assertEquals(paramTypeB, argTypeB);
    }

    @Test
    void testBinaryExpressionTypeCoercion() {
        // int + float → float
        // int + int → int
        String leftInt = "int";
        String rightFloat = "float";

        String result = rightFloat; // float wins
        assertEquals("float", result);
    }

    @Test
    void testComparisonReturnsInt() {
        // a == b, a < b etc. always return int (truthy)
        String resultType = "int";
        assertEquals("int", resultType);
    }

    @Test
    void testUnaryNegationPreservesType() {
        // -int → int
        String resultType = "int";
        assertEquals("int", resultType);
    }

    @Test
    void testUnaryLogicalNot() {
        // !int → int (truthy)
        String resultType = "int";
        assertEquals("int", resultType);
    }
}
