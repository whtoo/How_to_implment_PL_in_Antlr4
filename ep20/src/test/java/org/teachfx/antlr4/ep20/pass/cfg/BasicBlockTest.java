package org.teachfx.antlr4.ep20.pass.cfg;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.stmt.CJMP;
import org.teachfx.antlr4.ep20.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep20.ir.stmt.JMP;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BasicBlockTest {

    @Test
    void testBuildFromLinearBlockWhenNoInstructionsThenEmptyBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();

        // Act
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Assert
        assertTrue(basicBlock.isEmpty(), "BasicBlock should be empty when created from an empty LinearIRBlock");
    }

    @Test
    void testBuildFromLinearBlockWhenOneInstructionThenOneInstructionBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        block.addStmt(new Label("L1", null));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();

        // Act
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Assert
        assertEquals(1, basicBlock.codes.size(), "BasicBlock should have one instruction when created from a LinearIRBlock with one instruction");
    }

    @Test
    void testBuildFromLinearBlockWhenMultipleInstructionsThenSameNumberOfInstructionsBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        LinearIRBlock thenBlock = new LinearIRBlock();
        block.addStmt(new Label("L1", null));
        block.addStmt(new JMP(thenBlock));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();

        // Act
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Assert
        assertEquals(2, basicBlock.codes.size(), "BasicBlock should have the same number of instructions as the LinearIRBlock");
    }

    @Test
    void testBuildFromLinearBlockWhenJumpInstructionThenJumpInstructionBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        LinearIRBlock thenBlock = new LinearIRBlock();

        JMP jumpInstruction = new JMP(thenBlock);
        block.addStmt(jumpInstruction);
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();

        // Act
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Assert
        assertTrue(basicBlock.getLastInstr() instanceof JMP, "BasicBlock should have a jump instruction when created from a LinearIRBlock with a jump instruction");
    }

    @Test
    void testBuildFromLinearBlockWhenConditionalJumpInstructionThenConditionalJumpInstructionBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        LinearIRBlock then = new LinearIRBlock();
        LinearIRBlock other = new LinearIRBlock();

        var condSlot = new FrameSlot(0);

        CJMP cjumpInstruction = new CJMP(condSlot, then, other);
        block.addStmt(cjumpInstruction);
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();

        // Act
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Assert
        assertTrue(basicBlock.getLastInstr() instanceof CJMP, "BasicBlock should have a conditional jump instruction when created from a LinearIRBlock with a conditional jump instruction");
    }

    @Test
    void testBuildFromLinearBlockWhenFunctionEntryLabelThenFunctionEntryLabelBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        FuncEntryLabel funcEntryLabel = new FuncEntryLabel("main", 0, 0, null);
        block.addStmt(funcEntryLabel);
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();

        // Act
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Assert
        assertTrue(basicBlock.getLastInstr() instanceof FuncEntryLabel, "BasicBlock should have a function entry label when created from a LinearIRBlock with a function entry label");
    }

    @Test
    void testBuildFromLinearBlockWhenLabelThenLabelBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        Label label = new Label("L1", null);
        block.addStmt(label);
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();

        // Act
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Assert
        assertTrue(basicBlock.getLastInstr() instanceof Label, "BasicBlock should have a label when created from a LinearIRBlock with a label");
    }

    // Additional tests for other methods can be added here following the same pattern.
}
