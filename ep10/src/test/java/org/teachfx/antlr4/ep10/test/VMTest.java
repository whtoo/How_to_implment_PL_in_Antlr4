package org.teachfx.antlr4.ep10.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep10.stackvm.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP10 tests: Stack VM assembler and interpreter.
 */
public class VMTest {

    @Test
    void testBytecodeDefinitionHasOpcodes() {
        assertTrue(BytecodeDefinition.instructions.length > 0);
        assertNotNull(BytecodeDefinition.instructions[1]); // iadd at index 1
    }

    @Test
    void testAssemblerCreatesValidBytecode() {
        ByteCodeAssembler assembler = new ByteCodeAssembler(
            BytecodeDefinition.instructions);
        assertNotNull(assembler);
    }

    @Test
    void testStackFrameLocals() {
        StackFrame frame = new StackFrame(new FunctionSymbol("main", 0, 10, 10), 0);
        frame.setLocal(0, 100);
        assertEquals(100, frame.getLocal(0));
    }

    @Test
    void testFunctionSymbolMetadata() {
        FunctionSymbol func = new FunctionSymbol("add", 5, 10, 0);
        assertEquals("add", func.name);
        assertEquals(5, func.nargs);
        assertEquals(10, func.nlocals);
        assertEquals(0, func.address);
    }

    @Test
    void testInstructionMetadata() {
        BytecodeDefinition.Instruction iadd = BytecodeDefinition.instructions[BytecodeDefinition.INSTR_IADD];
        assertEquals("iadd", iadd.name);
        assertEquals(0, iadd.n);

        BytecodeDefinition.Instruction load = BytecodeDefinition.instructions[BytecodeDefinition.INSTR_LOAD];
        assertEquals("load", load.name);
        assertEquals(1, load.n);
        assertEquals(BytecodeDefinition.INT, load.type[0]);
    }
}
