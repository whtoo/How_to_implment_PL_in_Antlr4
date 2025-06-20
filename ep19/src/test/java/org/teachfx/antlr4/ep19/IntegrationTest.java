package org.teachfx.antlr4.ep19;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the full compiler pipeline.
 * These tests verify the entire compilation process from source to execution.
 */
public class IntegrationTest {

    /**
     * Helper method to assert that code compiles and executes without errors.
     * Also verifies that the output contains the expected text.
     */
    private void assertCompilesAndExecutes(String code, String expectedOutput) {
        CompilerTestUtil.CompilationResult result = CompilerTestUtil.compile(code, true);
        assertTrue(result.success, "Code should compile and execute without errors. Errors: " + 
                String.join("\n", result.errors) + "\nCode:\n" + code);
        assertTrue(result.errors.isEmpty(), "Error list should be empty. Errors: " + 
                String.join("\n", result.errors) + "\nCode:\n" + code);
        assertTrue(result.output.contains(expectedOutput), 
                "Output should contain '" + expectedOutput + "'. Got: " + result.output + "\nCode:\n" + code);
    }

    /**
     * Helper method to assert that code fails to compile or execute.
     * Also verifies that the error list contains the expected error message.
     */
    private void assertCompilationOrExecutionError(String code, String expectedErrorMessageSubstring) {
        CompilerTestUtil.CompilationResult result = CompilerTestUtil.compile(code, true);
        assertFalse(result.success, "Code should fail compilation or execution for: \n" + code);
        assertFalse(result.errors.isEmpty(), "Error list should not be empty for: \n" + code);
        assertTrue(result.errors.stream().anyMatch(err -> err.contains(expectedErrorMessageSubstring)), 
                "Expected error containing '" + expectedErrorMessageSubstring + "'. Got: " + 
                String.join("\n", result.errors) + "\nCode:\n" + code);
    }

    @Test
    void testBasicArithmetic() {
        String code = "void main() { int a = 5; int b = 3; int c = a + b; print(c); }";
        assertCompilesAndExecutes(code, "8");
    }

    @Test
    void testVariableDeclarationAndAssignment() {
        String code = "void main() { int x; x = 42; print(x); }";
        assertCompilesAndExecutes(code, "42");
    }

    @Test
    void testFunctionCallAndReturn() {
        String code = "int add(int a, int b) { return a + b; } void main() { int result = add(5, 7); print(result); }";
        assertCompilesAndExecutes(code, "12");
    }

    @Test
    void testIfStatement() {
        String code = "void main() { int x = 10; if (x > 5) { print(\"Greater\"); } else { print(\"Lesser\"); } }";
        assertCompilesAndExecutes(code, "Greater");
    }

    @Test
    void testWhileLoop() {
        String code = "void main() { int i = 0; int sum = 0; while (i < 5) { sum = sum + i; i = i + 1; } print(sum); }";
        assertCompilesAndExecutes(code, "10");
    }

    @Test
    void testStructDeclarationAndUsage() {
        String code = "struct Point { int x; int y; } void main() { Point p; p.x = 3; p.y = 4; print(p.x); print(p.y); }";
        assertCompilesAndExecutes(code, "3");
    }

    @Test
    void testStructMethodCall() {
        String code = "struct Calculator { int add(int a, int b) { return a + b; } } void main() { Calculator calc; int result = calc.add(5, 7); print(result); }";
        assertCompilesAndExecutes(code, "12");
    }

    @Test
    void testNestedStructs() {
        String code = "struct Inner { int value; } struct Outer { Inner inner; } void main() { Outer o; o.inner.value = 42; print(o.inner.value); }";
        assertCompilesAndExecutes(code, "42");
    }

    @Test
    void testTypedef() {
        String code = "typedef int Integer; void main() { Integer x = 100; print(x); }";
        assertCompilesAndExecutes(code, "100");
    }

    @Test
    void testSimpleProgram() {
        String code = "void main() { print(\"Hello, World!\"); }";
        assertCompilesAndExecutes(code, "Hello, World!");
    }

    @Test
    void testComplexProgram() {
        // Test each part separately to isolate issues

        // Test 1: Basic struct with method
        String structTest = 
            "struct Point {\n" +
            "    int x;\n" +
            "    int y;\n" +
            "    float distance() {\n" +
            "        return (x * x + y * y) * 1.0;\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    Point p;\n" +
            "    p.x = 3;\n" +
            "    p.y = 4;\n" +
            "    float dist = p.distance();\n" +
            "    print(\"Distance: \");\n" +
            "    print(dist);\n" +
            "}";

        assertCompilesAndExecutes(structTest, "Distance:");

        // Test 2: Recursive function
        String recursiveTest = 
            "int factorial(int n) {\n" +
            "    if (n <= 1) {\n" +
            "        return 1;\n" +
            "    } else {\n" +
            "        return n * factorial(n - 1);\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    int fact5 = factorial(5);\n" +
            "    print(\"Factorial of 5: \");\n" +
            "    print(fact5);\n" +
            "}";

        assertCompilesAndExecutes(recursiveTest, "Factorial of 5:");

        // Test 3: Control flow
        String controlFlowTest = 
            "void main() {\n" +
            "    int i = 0;\n" +
            "    int sum = 0;\n" +
            "    while (i < 10) {\n" +
            "        if (i % 2 == 0) {\n" +
            "            sum = sum + i;\n" +
            "        }\n" +
            "        i = i + 1;\n" +
            "    }\n" +
            "    print(\"Sum of even numbers from 0 to 9: \");\n" +
            "    print(sum);\n" +
            "}";

        assertCompilesAndExecutes(controlFlowTest, "Sum of even numbers from 0 to 9:");
    }

    @Test
    void testRuntimeError() {
        String code = "void main() { int x = 5; int y = 0; int z = x / y; print(z); }";
        assertCompilationOrExecutionError(code, "整数除零错误");
    }
}
