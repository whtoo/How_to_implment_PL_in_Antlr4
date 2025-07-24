package org.teachfx.antlr4.ep20.ir.expr.addr;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.symtab.symbol.VariableSymbol;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddressingTest {

    @Test
    void testFrameSlotCreation() {
        // Arrange
        int slotIdx = 5;

        // Act
        FrameSlot frameSlot = new FrameSlot(slotIdx);

        // Assert
        assertThat(frameSlot).isNotNull();
        assertThat(frameSlot.getSlotIdx()).isEqualTo(slotIdx);
        assertThat(frameSlot.toString()).isEqualTo("@5");
    }

    @Test
    void testFrameSlotFromVariableSymbol() {
        // Arrange
        VariableSymbol variableSymbol = mock(VariableSymbol.class);
        when(variableSymbol.getSlotIdx()).thenReturn(3);

        // Act
        FrameSlot frameSlot = FrameSlot.get(variableSymbol);

        // Assert
        assertThat(frameSlot).isNotNull();
        assertThat(frameSlot.getSlotIdx()).isEqualTo(3);
        assertThat(frameSlot.toString()).isEqualTo("@3");
    }

    @Test
    void testOperandSlotCreation() {
        // Arrange
        int initialOrdSeq = OperandSlot.getOrdSeq();

        // Act
        OperandSlot operandSlot = OperandSlot.genTemp();

        // Assert
        assertThat(operandSlot).isNotNull();
        assertThat(operandSlot.getOrd()).isEqualTo(initialOrdSeq);
        assertThat(operandSlot.toString()).isEqualTo("t" + initialOrdSeq);
    }

    @Test
    void testMultipleOperandSlotCreation() {
        // Arrange
        int initialOrdSeq = OperandSlot.getOrdSeq();

        // Act
        OperandSlot slot1 = OperandSlot.genTemp();
        OperandSlot slot2 = OperandSlot.genTemp();
        OperandSlot slot3 = OperandSlot.genTemp();

        // Assert
        assertThat(slot1.getOrd()).isEqualTo(initialOrdSeq);
        assertThat(slot2.getOrd()).isEqualTo(initialOrdSeq + 1);
        assertThat(slot3.getOrd()).isEqualTo(initialOrdSeq + 2);
        assertThat(slot1.toString()).isEqualTo("t" + initialOrdSeq);
        assertThat(slot2.toString()).isEqualTo("t" + (initialOrdSeq + 1));
        assertThat(slot3.toString()).isEqualTo("t" + (initialOrdSeq + 2));
    }

    @Test
    void testOperandSlotPushPop() {
        // Arrange
        int initialOrdSeq = OperandSlot.getOrdSeq();

        // Act
        OperandSlot slot1 = OperandSlot.pushStack();
        OperandSlot slot2 = OperandSlot.pushStack();
        OperandSlot.pushStack();
        OperandSlot.popStack();

        OperandSlot slot3 = OperandSlot.pushStack();

        // Assert
        assertThat(slot1.getOrd()).isEqualTo(initialOrdSeq);
        assertThat(slot2.getOrd()).isEqualTo(initialOrdSeq + 1);
        assertThat(slot3.getOrd()).isEqualTo(initialOrdSeq + 2);
    }
}