package org.teachfx.antlr4.ep20.ir;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.expr.Operand;
import org.teachfx.antlr4.ep20.ir.expr.VarSlot;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep20.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep20.ir.stmt.Assign;
import org.teachfx.antlr4.ep20.symtab.type.OperatorType;

import static org.assertj.core.api.Assertions.assertThat;

class ThreeAddressCodeTest {

    @BeforeEach
    void resetOperandSlotSequence() {
        // Reset the OperandSlot sequence to ensure predictable test results
        resetOperandSlotOrdSeq();
    }

    private void resetOperandSlotOrdSeq() {
        // Use reflection to reset the private static ordSeq field in OperandSlot
        try {
            java.lang.reflect.Field ordSeqField = OperandSlot.class.getDeclaredField("ordSeq");
            ordSeqField.setAccessible(true);
            ordSeqField.set(null, 0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset OperandSlot ordSeq", e);
        }
    }

    @Test
    void testAssignStmtCreation() {
        // Arrange
        FrameSlot lhs = new FrameSlot(1);
        OperandSlot rhs = OperandSlot.genTemp();

        // Act
        Assign assignStmt = Assign.with(lhs, rhs);

        // Assert
        assertThat(assignStmt).isNotNull();
        assertThat(assignStmt.getLhs()).isEqualTo(lhs);
        assertThat(assignStmt.getRhs()).isEqualTo(rhs);
        assertThat(assignStmt.toString()).isEqualTo("@1 = t0");
    }

    @Test
    void testBinExprCreation() {
        // Arrange
        OperandSlot lhs = OperandSlot.genTemp();
        OperandSlot rhs = OperandSlot.genTemp();
        OperatorType.BinaryOpType opType = OperatorType.BinaryOpType.ADD;

        // Act
        BinExpr binExpr = BinExpr.with(opType, lhs, rhs);

        // Assert
        assertThat(binExpr).isNotNull();
        assertThat(binExpr.getLhs()).isEqualTo(lhs);
        assertThat(binExpr.getRhs()).isEqualTo(rhs);
        assertThat(binExpr.getOpType()).isEqualTo(opType);
        assertThat(binExpr.toString()).isEqualTo("t0 ADD t1");
    }

    @Test
    void testThreeAddressCodeGeneration() {
        // Arrange
        FrameSlot var1 = new FrameSlot(1);
        FrameSlot var2 = new FrameSlot(2);
        
        // Reset sequence to ensure predictable slot numbers
        resetOperandSlotOrdSeq();
        OperandSlot temp1 = OperandSlot.genTemp();
        OperandSlot temp2 = OperandSlot.genTemp();

        // Act - Simulate the IRBuilder process
        // Load var1 into temp1
        Assign assign1 = Assign.with(temp1, var1);
        // Create expression: temp1 + var2
        BinExpr addExpr = BinExpr.with(OperatorType.BinaryOpType.ADD, temp1, var2);
        // Simulate the result of addInstr which would generate a new temp slot
        resetOperandSlotOrdSeq();
        OperandSlot resultSlot = OperandSlot.genTemp(); // This will be t0
        Assign assign2 = Assign.with(temp2, resultSlot);

        // Assert
        assertThat(assign1.toString()).isEqualTo("t0 = @1");
        assertThat(addExpr.toString()).isEqualTo("t0 ADD @2");
        assertThat(assign2.toString()).isEqualTo("t1 = t0");
    }

    @Test
    void testComplexThreeAddressCode() {
        // Arrange
        FrameSlot a = new FrameSlot(0);
        FrameSlot b = new FrameSlot(1);
        FrameSlot c = new FrameSlot(2);
        
        // Reset sequence to ensure predictable slot numbers
        resetOperandSlotOrdSeq();
        OperandSlot t0 = OperandSlot.genTemp(); // t0
        OperandSlot t1 = OperandSlot.genTemp(); // t1
        OperandSlot t2 = OperandSlot.genTemp(); // t2
        OperandSlot t3 = OperandSlot.genTemp(); // t3
        OperandSlot t4 = OperandSlot.genTemp(); // t4

        // Act - Generate TAC for: d = a + b * c
        // Load a into t0
        Assign loadA = Assign.with(t0, a);
        // Load b into t1
        Assign loadB = Assign.with(t1, b);
        // Create expression: b * c (t1 * c)
        BinExpr mulExpr = BinExpr.with(OperatorType.BinaryOpType.MUL, t1, c);
        // Simulate the result of addInstr for multiplication
        Assign assignMul = Assign.with(t2, t3); // t3 represents the result of mulExpr
        // Create expression: a + (b * c) (t0 + t2)
        BinExpr addExpr = BinExpr.with(OperatorType.BinaryOpType.ADD, t0, t2);
        // Simulate the result of addInstr for addition
        Assign assignResult = Assign.with(new FrameSlot(3), t4); // t4 represents the result of addExpr

        // Assert
        assertThat(loadA.toString()).isEqualTo("t0 = @0");
        assertThat(loadB.toString()).isEqualTo("t1 = @1");
        assertThat(mulExpr.toString()).isEqualTo("t1 MUL @2");
        assertThat(assignMul.toString()).isEqualTo("t2 = t3");
        assertThat(addExpr.toString()).isEqualTo("t0 ADD t2");
        assertThat(assignResult.toString()).isEqualTo("@3 = t4");
    }
}