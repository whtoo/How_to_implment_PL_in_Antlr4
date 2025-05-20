package org.teachfx.antlr4.ep19;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FunctionAndMethodTest {

    private void assertCompilesNoError(String code) {
        CompilerTestUtil.CompilationResult result = CompilerTestUtil.compile(code);
        assertTrue(result.success, "Code should compile without errors. Errors: " + String.join("\n", result.errors) + "\nCode:\n" + code);
        assertTrue(result.errors.isEmpty(), "Error list should be empty. Errors: " + String.join("\n", result.errors) + "\nCode:\n" + code);
    }

    private void assertCompilationError(String code, String expectedErrorMessageSubstring) {
        CompilerTestUtil.CompilationResult result = CompilerTestUtil.compile(code);
        assertFalse(result.success, "Code should fail compilation for: \n" + code);
        assertFalse(result.errors.isEmpty(), "Error list should not be empty for: \n" + code);
        assertTrue(result.errors.stream().anyMatch(err -> err.contains(expectedErrorMessageSubstring)), 
            "Expected error containing '" + expectedErrorMessageSubstring + "'. Got: " + String.join("\n", result.errors) + "\nCode:\n" + code);
    }

    @Test
    void testValidFunctionCall() {
        String code = "int add(int a, int b) { return a+b; } int x; x = add(1,2);";
        assertCompilesNoError(code);
    }

    @Test
    void testFunctionCallWrongArgCountError() {
        String code = "int add(int a, int b) { return a+b; } int x; x = add(1);";
        assertCompilationError(code, "参数数量不匹配"); // More specific: "期望 2 个参数，实际 1 个参数"
    }

    @Test
    void testFunctionCallWrongArgTypeError() {
        String code = "int add(int a, int b) { return a+b; } float f; f=1.0; int x; x = add(1,f);";
        assertCompilationError(code, "参数类型不匹配"); // More specific: "期望 int，实际 float"
    }

    @Test
    void testUndefinedFunctionCallError() {
        String code = "int x; x = undefinedFunc(1,2);";
        // This error might be caught by LocalResolver (Unknown type for id: undefinedFunc) or TypeCheckVisitor (表达式不是一个函数)
        // Let's target the TypeCheckVisitor message for now, as it's more specific to the call.
        assertCompilationError(code, "表达式不是一个函数"); 
    }
    
    @Test
    void testCallNonFunctionError() {
        String code = "int x; x=1; int y; y = x();";
        assertCompilationError(code, "表达式不是一个函数: x");
    }
} 