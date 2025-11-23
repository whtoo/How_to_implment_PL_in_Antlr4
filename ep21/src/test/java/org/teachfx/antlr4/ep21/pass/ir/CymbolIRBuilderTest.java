package org.teachfx.antlr4.ep21.pass.ir;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.stmt.ReturnVal;
import org.teachfx.antlr4.ep21.pass.cfg.LinearIRBlock;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep21.symtab.type.BuiltInTypeSymbol;
import org.teachfx.antlr4.ep21.symtab.scope.Scope;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CymbolIRBuilder类的单元测试
 * 测试IR构建器的核心功能，包括基本块管理、指令添加和表达式求值栈操作
 */
class CymbolIRBuilderTest {

    private CymbolIRBuilder irBuilder;

    @BeforeEach
    void setUp() {
        irBuilder = new CymbolIRBuilder();
    }

    @Test
    void testForkNewBlockThenCurrentBlockIsSet() {
        // Arrange
        Scope mockScope = null; // 使用null作为简单作用域

        // Act
        irBuilder.forkNewBlock(mockScope);

        // Assert
        assertNotNull(irBuilder.getCurrentBlock(), "Current block should be set after forking new block");
        assertEquals(LinearIRBlock.class, irBuilder.getCurrentBlock().getClass(), 
                     "Current block should be LinearIRBlock type");
    }

    @Test
    void testAddInstrWithAssignStatement() {
        // Arrange
        irBuilder.forkNewBlock(null);
        VarSlot lhs = new FrameSlot(0);
        VarSlot rhs = new FrameSlot(1);
        Assign assign = Assign.with(lhs, rhs);

        // Act
        irBuilder.addInstr(assign);

        // Assert
        assertEquals(1, irBuilder.getCurrentBlock().getStmts().size(), 
                     "Should have one statement in current block after adding instruction");
        assertEquals(assign, irBuilder.getCurrentBlock().getStmts().get(0), 
                     "Added statement should be the same as the instruction");
    }

    @Test
    void testPushEvalOperandAndPopEvalOperand() {
        // Arrange
        irBuilder.forkNewBlock(null);
        ConstVal testValue = ConstVal.valueOf(true);

        // Act
        VarSlot pushedSlot = irBuilder.pushEvalOperand(testValue);
        VarSlot poppedSlot = irBuilder.popEvalOperand();

        // Assert
        assertNotNull(pushedSlot, "pushEvalOperand should return a VarSlot");
        assertNotNull(poppedSlot, "popEvalOperand should return a VarSlot");
        assertEquals(pushedSlot, poppedSlot, "Popped slot should be the same as pushed slot");
    }

    @Test
    void testSetCurrentBlockThenBlocksAreLinked() {
        // Arrange
        irBuilder.forkNewBlock(null);
        LinearIRBlock firstBlock = irBuilder.getCurrentBlock();
        LinearIRBlock secondBlock = new LinearIRBlock();

        // Act
        irBuilder.setCurrentBlock(secondBlock);

        // Assert
        assertEquals(secondBlock, irBuilder.getCurrentBlock(), "Current block should be updated");
        assertTrue(firstBlock.getSuccessors().contains(secondBlock), 
                   "First block should have second block as successor");
        assertTrue(secondBlock.getPredecessors().contains(firstBlock), 
                   "Second block should have first block as predecessor");
    }

    @Test
    void testJumpThenJumpInstructionAdded() {
        // Arrange
        irBuilder.forkNewBlock(null);
        LinearIRBlock targetBlock = new LinearIRBlock();

        // Act
        irBuilder.jump(targetBlock);

        // Assert
        assertEquals(1, irBuilder.getCurrentBlock().getStmts().size(), 
                     "Should have one jump instruction in current block");
        var lastStmt = irBuilder.getCurrentBlock().getStmts().get(0);
        assertTrue(lastStmt instanceof org.teachfx.antlr4.ep21.ir.stmt.JMP, 
                   "Last statement should be a jump instruction");
    }

    @Test
    void testGetCFGWithNullStartBlocks() {
        // Arrange
        irBuilder.forkNewBlock(null);
        
        // Act & Assert - 这应该抛出异常或返回空CFG
        assertThrows(NullPointerException.class, () -> {
            irBuilder.getCFG(null);
        }, "getCFG should throw NullPointerException when startBlocks is null");
    }

    @Test
    void testClearBlockThenCurrentBlockIsNull() {
        // Arrange
        irBuilder.forkNewBlock(null);
        assertNotNull(irBuilder.getCurrentBlock(), "Current block should exist before clearing");

        // Act
        irBuilder.clearBlock();

        // Assert
        assertNull(irBuilder.getCurrentBlock(), "Current block should be null after clearing");
    }

    @Test
    void testPushBreakStackAndPopBreakStack() {
        // Arrange
        irBuilder.forkNewBlock(null);
        LinearIRBlock breakBlock = new LinearIRBlock();

        // Act
        irBuilder.pushBreakStack(breakBlock);
        irBuilder.popBreakStack();

        // Assert - 这个测试主要验证栈操作不会抛出异常
        assertTrue(true, "Stack operations should complete without exception");
    }

    @Test
    void testJumpIfThenConditionalJumpInstructionAdded() {
        // Arrange
        irBuilder.forkNewBlock(null);
        VarSlot condition = new FrameSlot(0);
        LinearIRBlock thenBlock = new LinearIRBlock();
        LinearIRBlock elseBlock = new LinearIRBlock();

        // Act
        irBuilder.jumpIf(condition, thenBlock, elseBlock);

        // Assert
        assertEquals(1, irBuilder.getCurrentBlock().getStmts().size(), 
                     "Should have one conditional jump instruction in current block");
        var lastStmt = irBuilder.getCurrentBlock().getStmts().get(0);
        assertTrue(lastStmt instanceof org.teachfx.antlr4.ep21.ir.stmt.CJMP, 
                   "Last statement should be a conditional jump instruction");
    }
}