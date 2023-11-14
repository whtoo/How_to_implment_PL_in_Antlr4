package org.teachfx.antlr4.ep20.pass.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class CymbolAssemblerTest {
    private CymbolAssembler cymbolAssembler;

    @BeforeEach
    public void setUp() {
        cymbolAssembler = new CymbolAssembler();
    }

    @Test
    public void testEmitWhenCalledThenAssembleCmdBufferContainsCommand() {
        // Arrange
        String command = "iconst 5";

        // Act
        cymbolAssembler.emit(command);

        // Assert
        assertEquals( command + "\n", cymbolAssembler.getAsmInfo());
    }

    @Test
    public void testEmitWhenCalledMultipleTimesThenAssembleCmdBufferContainsCommands() {
        // Arrange
        String command1 = "iconst 5";
        String command2 = "istore 1";

        // Act
        cymbolAssembler.emit(command1);
        cymbolAssembler.emit(command2);

        // Assert
        assertEquals(command1 + "\n" + command2 + "\n", cymbolAssembler.getAsmInfo());
    }

    // Add more tests for other methods in the CymbolAssembler class
    // For example, you can test the 'visit' methods with different types of IR nodes
    // You can create mock IR nodes and pass them to the 'visit' methods
    // Then, you can check if the 'assembleCmdBuffer' list contains the correct assembly commands
}
