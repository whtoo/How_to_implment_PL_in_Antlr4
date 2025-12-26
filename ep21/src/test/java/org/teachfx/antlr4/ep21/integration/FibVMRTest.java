package org.teachfx.antlr4.ep21.integration;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep21.CymbolLexer;
import org.teachfx.antlr4.ep21.CymbolParser;
import org.teachfx.antlr4.ep21.ast.ASTNode;
import org.teachfx.antlr4.ep21.ir.Prog;
import org.teachfx.antlr4.ep21.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep21.pass.codegen.CodeGenerationResult;
import org.teachfx.antlr4.ep21.pass.codegen.RegisterVMGenerator;
import org.teachfx.antlr4.ep21.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep21.pass.symtab.LocalDefine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Fibonacci fib(10) code generation for EP18R Register VM
 */
@DisplayName("Fib(10) VMR Code Generation Test")
public class FibVMRTest {

    private static final String FIB_PROGRAM = """
            int fib(int n) {
                if (n <= 1) {
                    return n;
                }
                return fib(n - 1) + fib(n - 2);
            }

            int main() {
                int result = fib(10);
                return result;
            }
            """;

    private RegisterVMGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new RegisterVMGenerator();
    }

    private ASTNode parseToAST(String sourceCode) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(sourceCode.getBytes(StandardCharsets.UTF_8));
        CharStream charStream = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();

        CymbolASTBuilder astBuilder = new CymbolASTBuilder();
        return parseTree.accept(astBuilder);
    }

    @Test
    @DisplayName("Should generate VMR code for fib(10)")
    void testFib10VMRCodeGeneration() throws IOException {
        // Parse and build IR
        ASTNode astRoot = parseToAST(FIB_PROGRAM);
        assertNotNull(astRoot);

        astRoot.accept(new LocalDefine());

        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);
        Prog prog = irBuilder.prog;
        assertNotNull(prog);

        prog.optimizeBasicBlock();

        // Generate VMR code
        CodeGenerationResult result = generator.generateFromInstructions(prog.linearInstrs());

        // Print generated VMR code
        System.out.println("=== Fib(10) EP18R Register VM Code ===");
        System.out.println(result.getOutput());
        System.out.println("========================================");

        // Verify generation succeeded
        assertTrue(result.isSuccess(), "Generation should succeed. Errors: " + result.getErrors());
        assertFalse(result.getOutput().isEmpty(), "Output should not be empty");
        assertTrue(result.getOutput().contains("halt"), "Code should contain halt instruction");
        assertTrue(result.getOutput().contains("fib"), "Code should contain fib function");
        assertTrue(result.getOutput().contains("main"), "Code should contain main function");

        // Print statistics
        System.out.println("Generated VMR Statistics:");
        System.out.println("  Success: " + result.isSuccess());
        System.out.println("  Instructions: " + result.getInstructionCount());
        System.out.println("  Generation Time: " + result.getGenerationTimeMs() + "ms");
        System.out.println("  Target VM: " + result.getTargetVM());
    }

    @Test
    @DisplayName("Should generate valid VMR with function definitions")
    void testVMRFunctionDefinitions() throws IOException {
        ASTNode astRoot = parseToAST(FIB_PROGRAM);
        astRoot.accept(new LocalDefine());

        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);
        Prog prog = irBuilder.prog;
        prog.optimizeBasicBlock();

        CodeGenerationResult result = generator.generateFromInstructions(prog.linearInstrs());

        String vmrCode = result.getOutput();

        // Verify function definition format
        assertTrue(vmrCode.contains(".def"), "Should contain .def directive");
        assertTrue(vmrCode.contains("args="), "Should specify args");
        assertTrue(vmrCode.contains("locals="), "Should specify locals");

        System.out.println("Function definitions found:");
        vmrCode.lines().filter(line -> line.contains(".def"))
            .forEach(line -> System.out.println("  " + line.trim()));
    }

    /**
     * Debug test: Analyze IR structure from full compilation
     */
    @Test
    @DisplayName("Debug: Analyze IR structure for TRO detection")
    void debugIRStructure() throws IOException {
        ASTNode astRoot = parseToAST(FIB_PROGRAM);
        astRoot.accept(new LocalDefine());

        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);
        Prog prog = irBuilder.prog;
        prog.optimizeBasicBlock();

        // Generate code and check if TRO was applied
        CodeGenerationResult result = generator.generateFromInstructions(prog.linearInstrs());

        String output = result.getOutput();
        System.out.println("=== Checking for TRO in generated code ===");

        // Count recursive calls
        long callFibCount = output.lines()
            .filter(line -> line.trim().startsWith("call fib"))
            .count();

        System.out.println("Recursive 'call fib' count: " + callFibCount);
        System.out.println("Contains loop labels: " + output.contains("_loop"));
        System.out.println("Contains _end label: " + output.contains("_end"));

        // Print the generated fib function for inspection
        System.out.println("\n=== Generated fib function ===");
        boolean inFib = false;
        for (String line : output.lines().toList()) {
            if (line.contains(".def fib:")) {
                inFib = true;
            }
            if (inFib) {
                System.out.println(line);
                if (inFib && line.trim().startsWith(".def ") && !line.contains(".def fib:")) {
                    break;
                }
            }
        }
    }
}
