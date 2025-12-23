package org.teachfx.antlr4.ep21.pass.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep21.ir.Prog;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RegisterVMGenerator.
 */
@DisplayName("RegisterVMGenerator Tests")
public class RegisterVMGeneratorTest {

    private RegisterVMGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new RegisterVMGenerator();
    }

    @Test
    @DisplayName("Should return correct target VM")
    void testGetTargetVM() {
        assertEquals(RegisterVMGenerator.TARGET_VM, generator.getTargetVM());
    }

    @Test
    @DisplayName("Should generate empty output for empty program")
    void testEmptyProgram() {
        Prog program = new Prog();
        CodeGenerationResult result = generator.generate(program);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getInstructionCount());
    }

    @Test
    @DisplayName("Should generate output with correct format")
    void testOutputFormat() {
        Prog program = new Prog();
        CodeGenerationResult result = generator.generate(program);

        assertNotNull(result);
        // Empty program should produce empty string or just whitespace
        String output = result.getOutput();
        assertTrue(output == null || output.isEmpty() || output.trim().isEmpty());
    }

    @Test
    @DisplayName("Should track instruction count")
    void testInstructionCount() {
        Prog program = new Prog();
        CodeGenerationResult result = generator.generate(program);

        assertEquals(0, result.getInstructionCount());
    }
}
