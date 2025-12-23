package org.teachfx.antlr4.ep21.pass.codegen;

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
import org.teachfx.antlr4.ep21.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep21.pass.symtab.LocalDefine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RegisterVMGenerator with real IR.
 */
@DisplayName("RegisterVMGenerator Integration Tests")
public class RegisterVMGeneratorIntegrationTest {

    private static final String SIMPLE_PROGRAM = """
            int main() {
                int x = 10;
                int y = 20;
                int z = x + y;
                print(z);
                return 0;
            }
            """;

    private RegisterVMGenerator generator;
    private CymbolLexer lexer;
    private CymbolParser parser;

    @BeforeEach
    public void setUp() {
        generator = new RegisterVMGenerator();
    }

    private ASTNode parseToAST(String sourceCode) throws IOException {
        InputStream is = new ByteArrayInputStream(sourceCode.getBytes(StandardCharsets.UTF_8));
        CharStream charStream = CharStreams.fromStream(is);
        lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();

        CymbolASTBuilder astBuilder = new CymbolASTBuilder();
        return parseTree.accept(astBuilder);
    }

    @Test
    @DisplayName("Should generate bytecode for simple program")
    void testSimpleProgramCodeGeneration() throws IOException {
        // Parse and build IR
        ASTNode astRoot = parseToAST(SIMPLE_PROGRAM);
        assertNotNull(astRoot);

        astRoot.accept(new LocalDefine());

        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);
        Prog prog = irBuilder.prog;
        assertNotNull(prog);

        prog.optimizeBasicBlock();

        // Generate bytecode
        CodeGenerationResult result = generator.generateFromInstructions(prog.linearInstrs());

        // Debug output
        System.out.println("=== EP18R Generation Result ===");
        System.out.println("Success: " + result.isSuccess());
        System.out.println("Errors: " + result.getErrors());
        System.out.println("Output length: " + result.getOutput().length());
        System.out.println("Output:\n" + result.getOutput());
        System.out.println("================================");

        assertTrue(result.isSuccess());
        assertFalse(result.getOutput().isEmpty());
        assertTrue(result.getOutput().contains("halt"));
    }
}
