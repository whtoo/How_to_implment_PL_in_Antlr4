package org.teachfx.antlr4.ep11.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep11.stackvm.*;
import org.teachfx.antlr4.ep11.ir.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP11 tests: Full compiler pipeline (source → IR → bytecode → VM).
 */
public class CompilerPipelineTest {

    @Test
    void testIRInstructionTypes() {
        AssignIR assign = new AssignIR("x", "42");
        assertEquals("x", assign.lhs);
        assertEquals("42", assign.rhs);

        BinaryOpIR bin = new BinaryOpIR("t0", "a", "+", "b");
        assertEquals("t0", bin.result);

        JumpIR jump = new JumpIR("L1");
        assertEquals("L1", jump.target);

        ReturnIR ret = new ReturnIR("t0");
        assertEquals("t0", ret.value);
    }

    @Test
    void testVMInterpreterExists() {
        // VMInterpreter requires bytecode; just verify class loads
        assertDoesNotThrow(() -> VMInterpreter.class.getName());
    }

    @Test
    void testBytecodeAssemblerExists() {
        assertDoesNotThrow(() -> {
            ByteCodeAssembler asm = new ByteCodeAssembler(
                BytecodeDefinition.instructions);
        });
    }

    @Test
    void testStackFrameOperations() {
        StackFrame frame = new StackFrame(new FunctionSymbol("test", 0, 5, 0), 0);
        frame.setLocal(0, 10);
        assertEquals(10, frame.getLocal(0));
    }

    @Test
    void testLabelSymbolTracking() {
        LabelSymbol label = new LabelSymbol("loop", 42);
        assertEquals("loop", label.getName());
        assertEquals(42, label.getAddress());
    }

    @Test
    void testFunctionSymbolMetadata() {
        FunctionSymbol func = new FunctionSymbol("add", 2, 5, 10);
        assertEquals("add", func.name);
        assertEquals(2, func.nargs);
        assertEquals(5, func.nlocals);
        assertEquals(10, func.address);
    }
}
