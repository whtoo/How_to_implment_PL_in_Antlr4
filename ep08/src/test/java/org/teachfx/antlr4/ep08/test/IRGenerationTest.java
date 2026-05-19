package org.teachfx.antlr4.ep08.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep08.ir.*;

import java.util.List;

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

    @Test
    void testBranchIR() {
        BranchIR ir = new BranchIR("cond", "L_true", "L_false");
        assertEquals("cond", ir.cond);
        assertEquals("L_true", ir.trueLabel);
        assertEquals("L_false", ir.falseLabel);
        assertTrue(ir.isControlTransfer());
    }

    @Test
    void testJumpIR() {
        JumpIR ir = new JumpIR("L_end");
        assertEquals("L_end", ir.target);
        assertTrue(ir.isControlTransfer());
    }

    @Test
    void testLabelIR() {
        LabelIR ir = new LabelIR("L_entry");
        assertEquals("L_entry", ir.label);
        assertTrue(ir.isLabel());
        assertFalse(ir.isControlTransfer());
    }

    @Test
    void testCallIR() {
        List<String> args = List.of("a", "b");
        CallIR ir = new CallIR("result", "add", args);
        assertEquals("result", ir.result);
        assertEquals("add", ir.funcName);
        assertEquals(2, ir.args.size());
        assertEquals("a", ir.args.get(0));
        assertEquals("b", ir.args.get(1));
    }

    @Test
    void testCallIRVoidReturn() {
        CallIR ir = new CallIR(null, "print", List.of("x"));
        assertNull(ir.result);
        assertEquals("print", ir.funcName);
    }

    @Test
    void testUnaryOpIR() {
        UnaryOpIR neg = new UnaryOpIR("t0", "-", "x");
        assertEquals("t0", neg.result);
        assertEquals("-", neg.op);
        assertEquals("x", neg.operand);

        UnaryOpIR notOp = new UnaryOpIR("t1", "!", "flag");
        assertEquals("!", notOp.op);
    }

    @Test
    void testReturnIRVoid() {
        ReturnIR ir = new ReturnIR(null);
        assertNull(ir.value);
        assertTrue(ir.isControlTransfer());
    }

    @Test
    void testBinaryOpIRComparisonOps() {
        BinaryOpIR eq = new BinaryOpIR("t0", "a", "==", "b");
        assertEquals("==", eq.op);
        BinaryOpIR ne = new BinaryOpIR("t1", "a", "!=", "b");
        assertEquals("!=", ne.op);
        BinaryOpIR lt = new BinaryOpIR("t2", "a", "<", "b");
        assertEquals("<", lt.op);
        BinaryOpIR gt = new BinaryOpIR("t3", "a", ">", "b");
        assertEquals(">", gt.op);
    }

    @Test
    void testIRFunctionToStrings() {
        assertTrue(new AssignIR("x", "42").toString().contains("="));
        assertTrue(new JumpIR("L").toString().contains("goto"));
        assertTrue(new ReturnIR("x").toString().contains("return"));
        assertTrue(new LabelIR("L").toString().contains("L"));
    }

    @Test
    void testFullFunctionIR() {
        IRFunction func = new IRFunction("factorial", "int");
        // entry
        func.emit(new LabelIR("entry"));
        func.emit(new AssignIR("result", "1"));
        // branch
        func.emit(new BranchIR("n <= 1", "done", "recurse"));
        // recurse
        func.emit(new LabelIR("recurse"));
        func.emit(new BinaryOpIR("t0", "n", "-", "1"));
        func.emit(new CallIR("t1", "factorial", List.of("t0")));
        func.emit(new BinaryOpIR("result", "n", "*", "t1"));
        // done
        func.emit(new LabelIR("done"));
        func.emit(new ReturnIR("result"));

        assertEquals(9, func.instructions.size());
        assertEquals("factorial", func.name);
        assertEquals("int", func.returnType);
    }

    @Test
    void testIRInstructionBaseClassDefaults() {
        // Regular instruction should not be control transfer
        IRInstruction assign = new AssignIR("x", "1");
        assertFalse(assign.isControlTransfer());
        assertFalse(assign.isLabel());
    }
}
