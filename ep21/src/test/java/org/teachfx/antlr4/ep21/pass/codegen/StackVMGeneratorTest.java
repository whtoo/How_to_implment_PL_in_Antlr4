package org.teachfx.antlr4.ep21.pass.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.Prog;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StackVMGenerator.
 */
public class StackVMGeneratorTest {

    private StackVMGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new StackVMGenerator();
    }

    @Test
    public void testGetTargetVM() {
        assertEquals(StackVMGenerator.TARGET_VM, generator.getTargetVM());
    }

    @Test
    public void testGenerateEmptyProgram() {
        Prog program = new Prog();
        CodeGenerationResult result = generator.generate(program);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("", result.getOutput());
        assertEquals(StackVMGenerator.TARGET_VM, result.getTargetVM());
    }

    @Test
    public void testConfigure() {
        Map<String, Object> config = new HashMap<>();
        IEmitter customEmitter = new AssemblyEmitterImpl();
        config.put("emitter", customEmitter);

        generator.configure(config);

        // Should not throw any exception
        assertNotNull(generator);
    }

    @Test
    public void testGenerateFromWithEmptyList() {
        generator.generateFrom(new java.util.ArrayList<>());
        String output = generator.getAssemblyOutput();
        assertEquals("", output);
    }

    @Test
    public void testResultContainsNoErrorsForValidProgram() {
        Prog program = new Prog();
        CodeGenerationResult result = generator.generate(program);

        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    public void testResultInstructionCountForEmptyProgram() {
        Prog program = new Prog();
        CodeGenerationResult result = generator.generate(program);

        assertEquals(0, result.getInstructionCount());
    }

    @Test
    public void testResultContainsTargetVM() {
        Prog program = new Prog();
        CodeGenerationResult result = generator.generate(program);

        assertEquals(StackVMGenerator.TARGET_VM, result.getTargetVM());
    }

    @Test
    public void testGenerationTimeMsIsRecorded() {
        Prog program = new Prog();
        CodeGenerationResult result = generator.generate(program);

        assertTrue(result.getGenerationTimeMs() >= 0);
    }

    @Test
    public void testWithCustomEmitterAndOperatorEmitter() {
        IEmitter customEmitter = new AssemblyEmitterImpl();
        IOperatorEmitter customOperatorEmitter = new IOperatorEmitter() {
            @Override
            public String emitBinaryOp(org.teachfx.antlr4.ep21.symtab.type.OperatorType.BinaryOpType binaryOpType) {
                return "custom_binop";
            }

            @Override
            public String emitUnaryOp(org.teachfx.antlr4.ep21.symtab.type.OperatorType.UnaryOpType unaryOpType) {
                return "custom_unop";
            }
        };

        StackVMGenerator customGenerator = new StackVMGenerator(customEmitter, customOperatorEmitter);
        assertNotNull(customGenerator);
    }

    @Test
    public void testAssemblyOutputIsFormattedCorrectly() {
        Prog program = new Prog();
        CodeGenerationResult result = generator.generate(program);

        // Check that the output ends with a newline (when not empty)
        String output = result.getOutput();
        if (!output.isEmpty()) {
            assertTrue(output.endsWith("\n"));
        }
    }

    @Test
    public void testEqualsAndHashCodeForSameResults() {
        Prog program1 = new Prog();
        Prog program2 = new Prog();

        CodeGenerationResult result1 = generator.generate(program1);
        CodeGenerationResult result2 = generator.generate(program2);

        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    public void testGetAssemblyOutputAfterGenerate() {
        Prog program = new Prog();
        generator.generate(program);
        String output = generator.getAssemblyOutput();

        assertNotNull(output);
        assertEquals("", output);
    }

    @Test
    public void testConfigureWithOperatorEmitter() {
        Map<String, Object> config = new HashMap<>();
        IOperatorEmitter customOperatorEmitter = new IOperatorEmitter() {
            @Override
            public String emitBinaryOp(org.teachfx.antlr4.ep21.symtab.type.OperatorType.BinaryOpType binaryOpType) {
                return "custom";
            }

            @Override
            public String emitUnaryOp(org.teachfx.antlr4.ep21.symtab.type.OperatorType.UnaryOpType unaryOpType) {
                return "custom";
            }
        };
        config.put("operatorEmitter", customOperatorEmitter);

        generator.configure(config);
        assertNotNull(generator);
    }

    /**
     * Simple implementation of IEmitter for testing.
     */
    private static class AssemblyEmitterImpl implements IEmitter {
        private final java.util.List<String> instructions = new java.util.ArrayList<>();
        private int indentLevel = 0;

        @Override
        public void emit(String instruction) {
            instructions.add("    ".repeat(indentLevel) + instruction);
        }

        @Override
        public void emitLabel(String label) {
            instructions.add(label + ":");
        }

        @Override
        public void emitComment(String comment) {
            instructions.add("    # " + comment);
        }

        @Override
        public void emitAll(java.util.List<String> instructions) {
            for (String instruction : instructions) {
                emit(instruction);
            }
        }

        @Override
        public void beginScope(String scopeName) {
            instructions.add("# Begin scope: " + scopeName);
        }

        @Override
        public void endScope() {
            instructions.add("# End scope");
        }

        @Override
        public String flush() {
            String result = String.join("\n", instructions);
            instructions.clear();
            return result.isEmpty() ? result : result + "\n";
        }

        @Override
        public void clear() {
            instructions.clear();
            indentLevel = 0;
        }

        @Override
        public int getIndentLevel() {
            return indentLevel;
        }

        @Override
        public void setIndentLevel(int level) {
            this.indentLevel = level;
        }
    }
}
