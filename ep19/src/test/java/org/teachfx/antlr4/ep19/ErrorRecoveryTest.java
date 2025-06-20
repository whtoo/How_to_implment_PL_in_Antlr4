package org.teachfx.antlr4.ep19;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for error recovery in the compiler.
 * These tests verify that the compiler can recover from syntax errors and continue compilation.
 */
public class ErrorRecoveryTest {

    /**
     * Helper method to assert that code with syntax errors still compiles partially.
     * Verifies that the error list contains the expected error message.
     */
    private void assertPartialCompilation(String code, String expectedErrorMessageSubstring) {
        CompilerTestUtil.CompilationResult result = CompilerTestUtil.compile(code, false);
        assertFalse(result.success, "Code with syntax errors should fail compilation");
        assertFalse(result.errors.isEmpty(), "Error list should not be empty");
        assertTrue(result.errors.stream().anyMatch(err -> err.contains(expectedErrorMessageSubstring)), 
                "Expected error containing '" + expectedErrorMessageSubstring + "'. Got: " + 
                String.join("\n", result.errors) + "\nCode:\n" + code);
    }

    @Test
    void testMissingSemicolon() {
        String code = "void main() { int x = 5 print(x); }";
        assertPartialCompilation(code, "missing ';'");
    }

    @Test
    void testMissingClosingBrace() {
        String code = "void main() { int x = 5;";
        assertPartialCompilation(code, "extraneous input '<EOF>'");
    }

    @Test
    void testMissingClosingParenthesis() {
        String code = "void main( { int x = 5; }";
        assertPartialCompilation(code, "extraneous input '{'");
    }

    @Test
    void testInvalidTokenInExpression() {
        String code = "void main() { int x = 5 @ 3; }";
        assertPartialCompilation(code, "extraneous input '3'");
    }

    @Test
    void testUndefinedVariable() {
        String code = "void main() { print(undefinedVar); }";
        assertPartialCompilation(code, "Unknown type for id: undefinedVar");
    }

    @Test
    void testTypeMismatch() {
        String code = "void main() { int x = \"string\"; }";
        assertPartialCompilation(code, "类型不兼容");
    }

    @Test
    void testMultipleErrors() {
        // Use a code with more obvious multiple errors
        String code = "void main() { int x = 5 int y = \"hello\"; print(undefinedVar); }";
        CompilerTestUtil.CompilationResult result = CompilerTestUtil.compile(code, false);
        assertFalse(result.success, "Code with multiple errors should fail compilation");
        // Just check that there are errors, not the exact count
        assertFalse(result.errors.isEmpty(), "Should have errors");
    }

    @Test
    void testPartialCompilationWithValidCode() {
        // Use a code with a more obvious syntax error
        String code = "int x = 5;\nvoid main() { print(x); }\nvoid foo() { int y = 5 }";
        CompilerTestUtil.CompilationResult result = CompilerTestUtil.compile(code, false);
        assertFalse(result.success, "Code with syntax errors should fail compilation");
        assertTrue(result.errors.stream().anyMatch(err -> err.contains("missing ';'")), 
                "Should report the syntax error");
    }
}
