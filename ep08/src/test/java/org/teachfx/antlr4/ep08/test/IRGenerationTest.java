package org.teachfx.antlr4.ep08.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep08.ir.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP08 tests: IR generation (Three-Address Code).
 */
public class IRGenerationTest {

    @Test
    void testIRFunctionCreation() {
        IRFunction func = new IRFunction("main", "int");
        assertEquals("main", func.name);
        assertEquals("int", func.returnType);
    }

    @Test
    void testAssignIR() {
        AssignIR ir = new AssignIR("x", "42");
        assertEquals("x", ir.lhs);
        assertEquals("42", ir.rhs);
    }

    @Test
    void testBinaryOpIR() {
        BinaryOpIR ir = new BinaryOpIR("t0", "a", "+", "b");
        assertEquals("t0", ir.result);
        assertEquals("a", ir.lhs);
        assertEquals("+", ir.op);
        assertEquals("b", ir.rhs);
    }

    @Test
    void testReturnIR() {
        ReturnIR ir = new ReturnIR("t0");
        assertEquals("t0", ir.value);
    }

    @Test
    void testIRFunctionAccumulatesInstructions() {
        IRFunction func = new IRFunction("test", "void");
        func.emit(new AssignIR("x", "10"));
        func.emit(new BinaryOpIR("t0", "x", "+", "5"));
        func.emit(new ReturnIR("t0"));

        assertEquals(3, func.instructions.size());
        assertTrue(func.instructions.get(0) instanceof AssignIR);
        assertTrue(func.instructions.get(1) instanceof BinaryOpIR);
        assertTrue(func.instructions.get(2) instanceof ReturnIR);
    }
}
