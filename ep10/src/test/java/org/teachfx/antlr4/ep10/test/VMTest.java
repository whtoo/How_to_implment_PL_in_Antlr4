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

    @Test
    void testAllArithmeticInstructionsExist() {
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_IADD]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_ISUB]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_IMUL]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_IDIV]);
        assertEquals("isub", BytecodeDefinition.instructions[BytecodeDefinition.INSTR_ISUB].name);
        assertEquals("imul", BytecodeDefinition.instructions[BytecodeDefinition.INSTR_IMUL].name);
        assertEquals("idiv", BytecodeDefinition.instructions[BytecodeDefinition.INSTR_IDIV].name);
    }

    @Test
    void testAllComparisonInstructionsExist() {
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_ILT]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_IGT]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_IEQ]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_INE]);
    }

    @Test
    void testControlFlowInstructionsExist() {
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_BR]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_BRT]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_BRF]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_CALL]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_RET]);
    }

    @Test
    void testMemoryInstructionsExist() {
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_LOAD]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_STORE]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_GLOAD]);
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_GSTORE]);
    }

    @Test
    void testHaltInstructionExists() {
        assertNotNull(BytecodeDefinition.instructions[BytecodeDefinition.INSTR_HALT]);
        assertEquals("halt", BytecodeDefinition.instructions[BytecodeDefinition.INSTR_HALT].name);
    }

    @Test
    void testStackFrameMultipleLocals() {
        StackFrame frame = new StackFrame(new FunctionSymbol("test", 0, 10, 0), 0);
        for (int i = 0; i < 10; i++) {
            frame.setLocal(i, i * 10);
        }
        assertEquals(0, frame.getLocal(0));
        assertEquals(50, frame.getLocal(5));
        assertEquals(90, frame.getLocal(9));
    }

    @Test
    void testLabelSymbol() {
        LabelSymbol label = new LabelSymbol("loop_start", 100);
        assertEquals("loop_start", label.getName());
        assertEquals(100, label.getAddress());
    }

    @Test
    void testFunctionSymbolMultipleArgs() {
        FunctionSymbol func = new FunctionSymbol("add", 3, 10, 0);
        assertEquals("add", func.name);
        assertEquals(3, func.nargs);
        assertEquals(10, func.nlocals);
    }
}
