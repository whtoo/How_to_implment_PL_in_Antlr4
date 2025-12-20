package org.teachfx.antlr4.ep18;

import java.util.Arrays;
import java.util.List;

/**
 * Test Fixtures for EP18 Stack VM Testing
 *
 * This class provides common test programs and test utilities for TDD refactoring.
 * It includes simple programs, complex programs, and various test scenarios.
 *
 * @author EP18 TDD Refactoring Team
 * @version 1.0.0
 */
public class VMTestFixtures {

    /**
     * Simple addition program
     * Tests: iconst, iadd, halt
     */
    public static final String SIMPLE_ADD = """
        iconst 10
        iconst 20
        iadd
        halt
        """;

    /**
     * Simple subtraction program
     * Tests: iconst, isub, halt
     */
    public static final String SIMPLE_SUB = """
        iconst 30
        iconst 10
        isub
        halt
        """;

    /**
     * Simple multiplication program
     * Tests: iconst, imul, halt
     */
    public static final String SIMPLE_MUL = """
        iconst 7
        iconst 8
        imul
        halt
        """;

    /**
     * Simple division program
     * Tests: iconst, idiv, halt
     */
    public static final String SIMPLE_DIV = """
        iconst 100
        iconst 5
        idiv
        halt
        """;

    /**
     * Fibonacci program with recursion
     * Tests: function calls, local variables, branches
     */
    public static final String FIBONACCI = """
        .def fib: args=1, locals=3, stack=4
            load 0
            iconst 2
            ilt
            brf recursive
            iconst 1
            ret

        recursive:
            load 0
            iconst 1
            isub
            call fib
            load 0
            iconst 2
            isub
            call fib
            iadd
            ret

        .def main: args=0, locals=1, stack=4
            iconst 10
            call fib
            print
            halt
        """;

    /**
     * Factorial program with recursion
     * Tests: function calls, branches, arithmetic
     */
    public static final String FACTORIAL = """
        .def fact: args=1, locals=1, stack=3
            load 0
            iconst 1
            ilt
            brf compute
            iconst 1
            ret

        compute:
            load 0
            iconst 1
            isub
            call fact
            load 0
            imul
            ret

        .def main: args=0, locals=1, stack=3
            iconst 5
            call fact
            print
            halt
        """;

    /**
     * Array operations program
     * Tests: array allocation, indexing, assignment
     */
    public static final String ARRAY_OPS = """
        .def main: args=0, locals=3, stack=4
            iconst 5
            anewarray
            astore 0

            iconst 0
            iconst 10
            iastore

            iconst 1
            iconst 20
            iastore

            iconst 2
            iconst 30
            iastore

            aload 0
            iconst 0
            iaload
            print

            aload 0
            iconst 1
            iaload
            print

            aload 0
            iconst 2
            iaload
            print

            halt
        """;

    /**
     * Loop program
     * Tests: branches, local variables, comparison
     */
    public static final String LOOP_TEST = """
        .def main: args=0, locals=2, stack=3
            iconst 0
            istore 0

        loop_start:
            load 0
            iconst 10
            igt
            brf loop_end

            load 0
            print

            load 0
            iconst 1
            iadd
            istore 0
            goto loop_start

        loop_end:
            halt
        """;

    /**
     * If-else program
     * Tests: conditional branches
     */
    public static final String IF_ELSE = """
        .def main: args=0, locals=1, stack=2
            iconst 15
            istore 0

            load 0
            iconst 10
            igt
            brf else_part

            iconst 1
            print
            goto end_if

        else_part:
            iconst 0
            print

        end_if:
            halt
        """;

    /**
     * Nested function calls
     * Tests: function calls, parameter passing
     */
    public static final String NESTED_CALLS = """
        .def add: args=2, locals=0, stack=3
            load 0
            load 1
            iadd
            ret

        .def mult: args=2, locals=0, stack=3
            load 0
            load 1
            imul
            ret

        .def main: args=0, locals=0, stack=4
            iconst 5
            iconst 3
            call add
            iconst 2
            iconst 4
            call mult
            imul
            print
            halt
        """;

    /**
     * String literal test
     * Tests: string constants, print
     */
    public static final String STRING_TEST = """
        .def main: args=0, locals=0, stack=2
            ldc "Hello, World!"
            print
            halt
        """;

    /**
     * Boolean operations
     * Tests: boolean literals, comparisons
     */
    public static final String BOOL_TEST = """
        .def main: args=0, locals=2, stack=2
            iconst 5
            iconst 3
            igt
            istore 0

            iconst 10
            iconst 20
            ilt
            istore 1

            load 0
            print

            load 1
            print

            halt
        """;

    /**
     * Stack operations test
     * Tests: dup, swap, pop
     */
    public static final String STACK_OPS = """
        .def main: args=0, locals=0, stack=4
            iconst 10
            iconst 20
            dup
            print
            print

            iconst 30
            swap
            print
            print

            pop
            print
            halt
        """;

    /**
     * Comparison operations
     * Tests: ilt, igt, ieq
     */
    public static final String COMPARE_TEST = """
        .def main: args=0, locals=0, stack=3
            iconst 10
            iconst 20
            ilt
            print

            iconst 30
            iconst 30
            ieq
            print

            iconst 100
            iconst 50
            igt
            print

            halt
        """;

    /**
     * Load/Store operations
     * Tests: load, store, iload, istore
     */
    public static final String LOAD_STORE_TEST = """
        .def main: args=0, locals=4, stack=2
            iconst 42
            istore 0

            iconst 100
            istore 1

            load 0
            load 1
            iadd
            print

            iconst 7
            istore 2

            load 2
            print

            halt
        """;

    /**
     * Null test
     * Tests: aconst_null, ifnull, ifnonnull
     */
    public static final String NULL_TEST = """
        .def main: args=0, locals=1, stack=2
            aconst_null
            istore 0

            load 0
            ifnull is_null

            iconst 0
            print
            goto end

        is_null:
            iconst 1
            print

        end:
            halt
        """;

    /**
     * Bitwise operations
     * Tests: iand, ior, ixor, ishl, ishr
     */
    public static final String BITWISE_TEST = """
        .def main: args=0, locals=0, stack=2
            iconst 15
            iconst 7
            iand
            print

            iconst 15
            iconst 7
            ior
            print

            iconst 12
            iconst 8
            ixor
            print

            halt
        """;

    /**
     * Return from function
     * Tests: ret, return values
     */
    public static final String RETURN_TEST = """
        .def getValue: args=0, locals=0, stack=1
            iconst 99
            ret

        .def main: args=0, locals=0, stack=2
            call getValue
            print
            halt
        """;

    // ====================
    // Utility Methods
    // ====================

    /**
     * Parse program string into lines
     *
     * @param program the program string
     * @return list of instructions
     */
    public static List<String> parseProgram(String program) {
        return Arrays.stream(program.trim().split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .toList();
    }

    /**
     * Get a simple test program by name
     *
     * @param name the program name
     * @return the program string
     * @throws IllegalArgumentException if program not found
     */
    public static String getProgram(String name) {
        return switch (name.toLowerCase()) {
            case "simple_add" -> SIMPLE_ADD;
            case "simple_sub" -> SIMPLE_SUB;
            case "simple_mul" -> SIMPLE_MUL;
            case "simple_div" -> SIMPLE_DIV;
            case "fibonacci" -> FIBONACCI;
            case "factorial" -> FACTORIAL;
            case "array_ops" -> ARRAY_OPS;
            case "loop" -> LOOP_TEST;
            case "if_else" -> IF_ELSE;
            case "nested_calls" -> NESTED_CALLS;
            case "string_test" -> STRING_TEST;
            case "bool_test" -> BOOL_TEST;
            case "stack_ops" -> STACK_OPS;
            case "compare_test" -> COMPARE_TEST;
            case "load_store" -> LOAD_STORE_TEST;
            case "null_test" -> NULL_TEST;
            case "bitwise" -> BITWISE_TEST;
            case "return_test" -> RETURN_TEST;
            default -> throw new IllegalArgumentException("Unknown test program: " + name);
        };
    }

    /**
     * Get all available test program names
     *
     * @return list of program names
     */
    public static List<String> getAllProgramNames() {
        return Arrays.asList(
                "simple_add", "simple_sub", "simple_mul", "simple_div",
                "fibonacci", "factorial", "array_ops", "loop",
                "if_else", "nested_calls", "string_test", "bool_test",
                "stack_ops", "compare_test", "load_store", "null_test",
                "bitwise", "return_test"
        );
    }

    /**
     * Check if program is expected to complete normally
     *
     * @param name the program name
     * @return true if program should complete normally
     */
    public static boolean isExpectedToComplete(String name) {
        // All test programs are expected to complete normally
        // except programs that intentionally test error conditions
        return true;
    }

    /**
     * Get expected output for a program
     * This is a placeholder - actual implementation would compare results
     *
     * @param name the program name
     * @return expected output description
     */
    public static String getExpectedOutputDescription(String name) {
        return switch (name.toLowerCase()) {
            case "simple_add" -> "Result of 10 + 20 = 30";
            case "simple_sub" -> "Result of 30 - 10 = 20";
            case "simple_mul" -> "Result of 7 * 8 = 56";
            case "simple_div" -> "Result of 100 / 5 = 20";
            case "fibonacci" -> "Fibonacci(10) result";
            case "factorial" -> "Factorial(5) = 120";
            case "array_ops" -> "Array values: 10, 20, 30";
            case "loop" -> "Numbers 0 through 9";
            case "if_else" -> "Boolean value (1 or 0)";
            case "nested_calls" -> "Complex arithmetic result";
            case "string_test" -> "String: 'Hello, World!'";
            case "bool_test" -> "Boolean values";
            case "stack_ops" -> "Stack manipulation results";
            case "compare_test" -> "Comparison results";
            case "load_store" -> "Variable operations result";
            case "null_test" -> "Null check result";
            case "bitwise" -> "Bitwise operation results";
            case "return_test" -> "Function return value";
            default -> "Program output";
        };
    }
}
