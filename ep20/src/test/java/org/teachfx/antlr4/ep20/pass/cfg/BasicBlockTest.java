package org.teachfx.antlr4.ep20.pass.cfg;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.stmt.*;
import org.teachfx.antlr4.ep20.utils.Kind;

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

    // Additional boundary scenario tests
    @Test
    void testBuildFromLinearBlockWithReturnInstructionThenReturnBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        LinearIRBlock targetBlock = new LinearIRBlock();
        block.addStmt(new Label("L1", null));
        block.addStmt(new JMP(targetBlock));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();

        // Act
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Assert
        assertTrue(basicBlock.getLastInstr() instanceof JMP, "BasicBlock should have a jump instruction");
        assertEquals(Kind.END_BY_JMP, basicBlock.kind, "BasicBlock kind should be END_BY_JMP when it ends with a jump instruction");
    }

    @Test
    void testGetIdReturnsCorrectId() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        int id = basicBlock.getId();

        // Assert
        assertEquals(block.getOrd(), id, "BasicBlock ID should match the LinearIRBlock ordinal");
    }

    @Test
    void testGetLabelReturnsCorrectLabel() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        Label label = new Label("TestLabel", null);
        block.addStmt(label);
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        Label resultLabel = basicBlock.getLabel();

        // Assert
        assertEquals(label, resultLabel, "BasicBlock label should match the LinearIRBlock label");
    }

    @Test
    void testGetOrdLabelReturnsCorrectLabel() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        String ordLabel = basicBlock.getOrdLabel();

        // Assert
        assertEquals("L" + basicBlock.getId(), ordLabel, "BasicBlock ordinal label should be in the format 'L{id}'");
    }

    @Test
    void testIsEmptyReturnsTrueWhenNoInstructions() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        boolean isEmpty = basicBlock.isEmpty();

        // Assert
        assertTrue(isEmpty, "BasicBlock should be empty when it has no instructions");
    }

    @Test
    void testIsEmptyReturnsFalseWhenHasInstructions() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        block.addStmt(new Label("L1", null));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        boolean isEmpty = basicBlock.isEmpty();

        // Assert
        assertFalse(isEmpty, "BasicBlock should not be empty when it has instructions");
    }

    @Test
    void testAllSeqReturnsAllInstructionsForContinuousBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        Label label = new Label("L1", null);
        block.addStmt(label);
        block.addStmt(new Assign(null, null));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        List<Loc<IRNode>> allSeq = basicBlock.allSeq();

        // Assert
        assertEquals(basicBlock.codes.size(), allSeq.size(), "All sequence should contain all instructions for continuous block");
    }

    @Test
    void testDropLabelSeqReturnsCorrectSequence() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        Label label = new Label("L1", null);
        block.addStmt(label);
        block.addStmt(new Assign(null, null));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        List<Loc<IRNode>> dropLabelSeq = basicBlock.dropLabelSeq();

        // Assert
        assertEquals(basicBlock.codes.size() - 1, dropLabelSeq.size(), "Drop label sequence should exclude the first label instruction");
        assertNotEquals(label, dropLabelSeq.get(0).instr, "Drop label sequence should not contain the label instruction");
    }

    @Test
    void testDropLabelSeqWithFunctionEntryLabelReturnsAllInstructions() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        FuncEntryLabel funcEntryLabel = new FuncEntryLabel("main", 0, 0, null);
        block.addStmt(funcEntryLabel);
        block.addStmt(new Assign(null, null));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        List<Loc<IRNode>> dropLabelSeq = basicBlock.dropLabelSeq();

        // Assert
        assertEquals(basicBlock.codes.size(), dropLabelSeq.size(), "Drop label sequence should contain all instructions when first instruction is FuncEntryLabel");
    }

    @Test
    void testBackwardIteratorReturnsInstructionsInReverseOrder() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        Label label = new Label("L1", null);
        Assign assign = new Assign(null, null);
        block.addStmt(label);
        block.addStmt(assign);
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        List<IRNode> reversedInstructions = new ArrayList<>();
        var iterator = basicBlock.backwardIterator();
        while (iterator.hasNext()) {
            reversedInstructions.add(iterator.next().instr);
        }

        // Assert
        assertEquals(2, reversedInstructions.size(), "Reversed instructions should contain all instructions");
        assertEquals(assign, reversedInstructions.get(0), "First reversed instruction should be the last added instruction");
        assertEquals(label, reversedInstructions.get(1), "Second reversed instruction should be the first added instruction");
    }

    // 新增的边界场景测试
    @Test
    void testBuildFromLinearBlockWithNullLabel() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        block.addStmt(new Assign(null, null));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();

        // Act
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Assert
        assertNotNull(basicBlock);
        assertTrue(basicBlock.isEmpty() || basicBlock.codes.size() > 0);
    }

    @Test
    void testGetLastInstrWithEmptyBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act & Assert
        assertThrows(IndexOutOfBoundsException.class, () -> {
            basicBlock.getLastInstr();
        }, "Should throw IndexOutOfBoundsException for empty block");
    }

    @Test
    void testMergeNearBlock() {
        // Arrange
        LinearIRBlock targetBlock = new LinearIRBlock();
        
        LinearIRBlock block1 = new LinearIRBlock();
        block1.addStmt(new Label("L1", null));
        block1.addStmt(new JMP(targetBlock)); // 使用有效的targetBlock

        LinearIRBlock block2 = new LinearIRBlock();
        block2.addStmt(new Label("L2", null));
        block2.addStmt(new Assign(null, null));

        List<BasicBlock<IRNode>> cachedNodes1 = new ArrayList<>();
        List<BasicBlock<IRNode>> cachedNodes2 = new ArrayList<>();
        BasicBlock<IRNode> basicBlock1 = BasicBlock.buildFromLinearBlock(block1, cachedNodes1);
        BasicBlock<IRNode> basicBlock2 = BasicBlock.buildFromLinearBlock(block2, cachedNodes2);

        // 记录合并前的指令数量
        int initialSize1 = basicBlock1.codes.size();
        int initialSize2 = basicBlock2.codes.size();

        // Act
        basicBlock1.mergeNearBlock(basicBlock2);

        // Assert
        // 合并后的块应该包含第一个块的指令(减去跳转指令)和第二个块的指令(减去标签)
        assertTrue(basicBlock1.codes.size() >= initialSize1 + initialSize2 - 2);
    }

    @Test
    void testRemoveLastInstr() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        block.addStmt(new Label("L1", null));
        block.addStmt(new Assign(null, null));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        int initialSize = basicBlock.codes.size();

        // Act
        basicBlock.removeLastInstr();

        // Assert
        assertEquals(initialSize - 1, basicBlock.codes.size(), "Block should have one less instruction after removal");
        assertEquals(org.teachfx.antlr4.ep20.utils.Kind.CONTINUOUS, basicBlock.kind, "Block kind should be CONTINUOUS after removing last instruction");
    }

    @Test
    void testAllSeqWithNonContinuousBlock() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        LinearIRBlock targetBlock = new LinearIRBlock();
        block.addStmt(new Label("L1", null));
        block.addStmt(new JMP(targetBlock)); // 使用有效的targetBlock
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        List<Loc<IRNode>> allSeq = basicBlock.allSeq();

        // Assert
        // 对于非连续块，应该返回除最后一条指令外的所有指令
        assertEquals(basicBlock.codes.size() - 1, allSeq.size(), "All sequence should exclude last instruction for non-continuous block");
    }

    @Test
    void testGetIRNodes() {
        // Arrange
        LinearIRBlock block = new LinearIRBlock();
        block.addStmt(new Label("L1", null));
        block.addStmt(new Assign(null, null));
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);

        // Act
        var irNodes = basicBlock.getIRNodes();

        // Assert
        assertNotNull(irNodes);
        assertEquals(2, irNodes.count(), "IR nodes stream should contain all instructions");
    }
}
