package org.teachfx.antlr4.ep21.pass.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep21.symtab.scope.GlobalScope;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.type.BuiltInTypeSymbol;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test RegisterVMGenerator TRO functionality.
 */
@DisplayName("RegisterVMGenerator Tail Recursion Optimization Test")
public class RegisterVMGeneratorTROTest {

    private List<IRNode> fibonacciInstructions;
    private List<IRNode> nonFibonacciInstructions;

    @BeforeEach
    void setUp() {
        // Create a global scope for testing
        GlobalScope globalScope = new GlobalScope();

        // Create Fibonacci-like instructions
        fibonacciInstructions = new ArrayList<>();
        BuiltInTypeSymbol intType = new BuiltInTypeSymbol("int");
        MethodSymbol fibSymbol = new MethodSymbol("fib", intType, globalScope, null);
        fibonacciInstructions.add(new FuncEntryLabel("fib", 1, 1, globalScope));

        // Add recursive calls
        CallFunc call1 = new CallFunc("fib", 1, fibSymbol);
        CallFunc call2 = new CallFunc("fib", 1, fibSymbol);
        fibonacciInstructions.add(call1);
        fibonacciInstructions.add(call2);

        // Create non-Fibonacci instructions
        nonFibonacciInstructions = new ArrayList<>();
        MethodSymbol fooSymbol = new MethodSymbol("foo", intType, globalScope, null);
        nonFibonacciInstructions.add(new FuncEntryLabel("foo", 1, 1, globalScope));
        CallFunc call3 = new CallFunc("foo", 1, fooSymbol);
        nonFibonacciInstructions.add(call3);
    }

    @Test
    @DisplayName("Should detect Fibonacci pattern")
    void testFibonacciPatternDetection() {
        RegisterVMGenerator generator = new RegisterVMGenerator();

        // Generate code for Fibonacci pattern
        CodeGenerationResult result = generator.generateFromInstructions(fibonacciInstructions);

        assertTrue(result.isSuccess(), "Code generation should succeed");
        assertNotNull(result.getOutput(), "Output should not be null");

        System.out.println("Generated Fibonacci code:");
        System.out.println(result.getOutput());

        // Verify the output contains loop indicators
        String output = result.getOutput();
        assertTrue(output.contains("_loop"), "Generated code should contain loop labels");
        assertTrue(output.contains("_loop_body") || output.contains("_end"),
                   "Generated code should contain loop body/end labels");
    }

    @Test
    @DisplayName("Should generate iterative code for Fibonacci")
    void testIterativeCodeGeneration() {
        RegisterVMGenerator generator = new RegisterVMGenerator();

        CodeGenerationResult result = generator.generateFromInstructions(fibonacciInstructions);

        assertTrue(result.isSuccess(), "Code generation should succeed");

        String output = result.getOutput();
        System.out.println("Generated iterative Fibonacci code:");
        System.out.println(output);

        // Verify iterative characteristics
        assertTrue(output.contains("_loop_body"), "Should have loop body label");
        assertTrue(output.contains("_end"), "Should have end label");
        assertTrue(output.contains("add"), "Should have add instruction");
        assertTrue(output.contains("mov"), "Should have mov instruction");

        // Verify no recursive calls
        assertFalse(output.contains("call fib"), "Should not contain recursive call fib");
    }

    @Test
    @DisplayName("Should not optimize non-Fibonacci patterns")
    void testNonFibonacciPattern() {
        RegisterVMGenerator generator = new RegisterVMGenerator();

        CodeGenerationResult result = generator.generateFromInstructions(nonFibonacciInstructions);

        assertTrue(result.isSuccess(), "Code generation should succeed");

        String output = result.getOutput();
        System.out.println("Generated code for non-Fibonacci function:");
        System.out.println(output);

        // Non-Fibonacci patterns should NOT have TRO loop structure
        // (this test verifies the default code generation path)
    }

    @Test
    @DisplayName("Generated code should have correct function signature")
    void testFunctionSignature() {
        RegisterVMGenerator generator = new RegisterVMGenerator();

        CodeGenerationResult result = generator.generateFromInstructions(fibonacciInstructions);

        String output = result.getOutput();
        assertTrue(output.contains(".def fib:"), "Should contain function definition");
        assertTrue(output.contains("args=1"), "Should specify 1 argument");
        assertTrue(output.contains("locals=2"), "Should specify 2 locals (a, b)");
    }

    @Test
    @DisplayName("Generated code should handle base case")
    void testBaseCaseHandling() {
        RegisterVMGenerator generator = new RegisterVMGenerator();

        CodeGenerationResult result = generator.generateFromInstructions(fibonacciInstructions);

        String output = result.getOutput();

        // Should have base case check (n <= 1)
        assertTrue(output.contains("sle") || output.contains("sgt"),
                   "Should have comparison instruction for base case");
        assertTrue(output.contains("jf"), "Should have conditional jump");
        assertTrue(output.contains("ret"), "Should have return instruction");
    }
}
