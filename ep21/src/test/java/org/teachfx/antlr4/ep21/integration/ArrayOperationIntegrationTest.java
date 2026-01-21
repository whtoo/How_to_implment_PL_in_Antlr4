package org.teachfx.antlr4.ep21.integration;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.CymbolLexer;
import org.teachfx.antlr4.ep21.CymbolParser;
import org.teachfx.antlr4.ep21.ast.ASTNode;
import org.teachfx.antlr4.ep21.ast.CompileUnit;
import org.teachfx.antlr4.ep21.ir.Prog;
import org.teachfx.antlr4.ep21.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep21.pass.codegen.CodeGenerationResult;
import org.teachfx.antlr4.ep21.pass.codegen.ICodeGenerator;
import org.teachfx.antlr4.ep21.pass.codegen.StackVMGenerator;
import org.teachfx.antlr4.ep21.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep21.pass.symtab.LocalDefine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Array operation integration tests for EP21.
 * <p>
 * This test class verifies that:
 * 1. EP21 can compile Cymbol array operations to IR
 * 2. Code generators can generate array bytecode for EP18
 * 3. Array operations (NEWARRAY, IALOAD, IASTORE) work correctly
 * </p>
 */
@DisplayName("Array Operation Integration Tests")
public class ArrayOperationIntegrationTest {

    private static final String ARRAY_DECLARATION_PROGRAM = """
            int main() {
                int arr[5];
                return 0;
            }
            """;

    private static final String ARRAY_INITIALIZATION_PROGRAM = """
            int main() {
                int arr[3] = {1, 2, 3};
                return 0;
            }
            """;

    private static final String ARRAY_ACCESS_PROGRAM = """
            int main() {
                int arr[3] = {10, 20, 30};
                int x = arr[1];
                print(x);
                return 0;
            }
            """;

    private static final String ARRAY_ASSIGN_PROGRAM = """
            int main() {
                int arr[3] = {1, 2, 3};
                arr[1] = 42;
                print(arr[1]);
                return 0;
            }
            """;

    private static final String ARRAY_LOOP_PROGRAM = """
            int main() {
                int arr[5] = {1, 2, 3, 4, 5};
                int sum = 0;
                int i = 0;
                while (i < 5) {
                    sum = sum + arr[i];
                    i = i + 1;
                }
                print(sum);
                return 0;
            }
            """;

    private static final String ARRAY_BOUNDARIES_PROGRAM = """
            int main() {
                int arr[3];
                arr[0] = 10;
                arr[2] = 30;
                print(arr[0]);
                print(arr[2]);
                return 0;
            }
            """;

    private CymbolLexer lexer;
    private CymbolParser parser;
    private Prog irProgram;

    @BeforeEach
    public void setUp() {
        // Reset for each test
        lexer = null;
        parser = null;
        irProgram = null;
    }

    private void compileToIR(String sourceCode) throws Exception {
        // Step 1: Parse Cymbol source to AST
        ASTNode astRoot = parseToAST(sourceCode);
        assertThat(astRoot).isNotNull();

        var compileUnit = (CompileUnit) astRoot;

        // Step 2: Build symbol table
        LocalDefine symbolTableBuilder = new LocalDefine();
        compileUnit.accept(symbolTableBuilder);

        // Step 3: Build IR
        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        compileUnit.accept(irBuilder);
        irProgram = irBuilder.prog;
    }

    private ASTNode parseToAST(String sourceCode) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(sourceCode.getBytes(StandardCharsets.UTF_8));
        CharStream charStream = CharStreams.fromStream(inputStream);
        lexer = new CymbolLexer(charStream);
        parser = new CymbolParser(new CommonTokenStream(lexer));
        ParseTree compileUnitTree = parser.file();
        CymbolASTBuilder astBuilder = new CymbolASTBuilder();
        return (ASTNode) compileUnitTree.accept(astBuilder);
    }

    private String generateBytecode() {
        ICodeGenerator codeGenerator = new StackVMGenerator();
        CodeGenerationResult result = codeGenerator.generate(irProgram);

        System.err.println("Code generation result: success=" + result.isSuccess() + ", errors=" + result.getErrors() + ", output length=" + (result.getOutput() != null ? result.getOutput().length() : 0));
        if (!result.isSuccess()) {
            System.err.println("=== CODE GENERATION ERRORS ===");
            for (String error : result.getErrors()) {
                System.err.println("  - " + error);
            }
            System.err.println("=== END ERRORS ===");
        }

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getErrors()).isEmpty();

        return result.getOutput();
    }

    @Test
    @DisplayName("Should compile array declaration to IR and bytecode")
    public void testArrayDeclaration() throws Exception {
        compileToIR(ARRAY_DECLARATION_PROGRAM);

        assertThat(irProgram).isNotNull();
        assertThat(irProgram.instrs).isNotNull();

        String bytecode = generateBytecode();
        assertThat(bytecode).contains("newarray");
    }

    @Test
    @DisplayName("Should compile array initialization to IR and bytecode")
    public void testArrayInitialization() throws Exception {
        compileToIR(ARRAY_INITIALIZATION_PROGRAM);

        assertThat(irProgram).isNotNull();
        assertThat(irProgram.instrs).isNotNull();

        String bytecode = generateBytecode();
        // Should contain newarray and initialization
        assertThat(bytecode).contains("newarray");
    }

    @Test
    @DisplayName("Should compile array access to IR and bytecode")
    public void testArrayAccess() throws Exception {
        compileToIR(ARRAY_ACCESS_PROGRAM);

        assertThat(irProgram).isNotNull();
        assertThat(irProgram.instrs).isNotNull();

        String bytecode = generateBytecode();
        // Should contain iaload instruction
        assertThat(bytecode).contains("iaload");
    }

    @Test
    @DisplayName("Should compile array assignment to IR and bytecode")
    public void testArrayAssignment() throws Exception {
        compileToIR(ARRAY_ASSIGN_PROGRAM);

        assertThat(irProgram).isNotNull();
        assertThat(irProgram.instrs).isNotNull();

        String bytecode = generateBytecode();
        // Should contain iastore instruction
        assertThat(bytecode).contains("iastore");
    }

    @Test
    @DisplayName("Should compile array loop to IR and bytecode")
    public void testArrayLoop() throws Exception {
        compileToIR(ARRAY_LOOP_PROGRAM);

        assertThat(irProgram).isNotNull();
        assertThat(irProgram.instrs).isNotNull();

        String bytecode = generateBytecode();
        // Should contain both iaload and iastore
        assertThat(bytecode).contains("iaload");
        assertThat(bytecode).contains("iastore");
    }

    @Test
    @DisplayName("Should compile array boundary access to IR and bytecode")
    public void testArrayBoundaries() throws Exception {
        compileToIR(ARRAY_BOUNDARIES_PROGRAM);

        assertThat(irProgram).isNotNull();
        assertThat(irProgram.instrs).isNotNull();

        String bytecode = generateBytecode();
        // Should contain both iaload and iastore
        assertThat(bytecode).contains("iaload");
        assertThat(bytecode).contains("iastore");
    }
}
