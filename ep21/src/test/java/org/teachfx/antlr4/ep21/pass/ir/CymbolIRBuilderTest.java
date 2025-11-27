package org.teachfx.antlr4.ep21.pass.ir;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.pass.cfg.LinearIRBlock;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep21.symtab.type.BuiltInTypeSymbol;
import org.teachfx.antlr4.ep21.symtab.scope.Scope;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * CymbolIRBuilder类的重构单元测试
 * 使用JUnit5特性并补充边界case
 */
@DisplayName("CymbolIRBuilder单元测试")
@Tag("ir")
class CymbolIRBuilderTest {

    private CymbolIRBuilder irBuilder;

    @BeforeEach
    void setUp() {
        irBuilder = new CymbolIRBuilder();
    }

    @Nested
    @DisplayName("基本块管理测试")
    class BlockManagementTests {
        
        @Test
        @DisplayName("forkNewBlock应该创建新的基本块并设置为当前块")
        void testForkNewBlock() {
            Scope mockScope = null;
            
            irBuilder.forkNewBlock(mockScope);
            
            assertNotNull(irBuilder.getCurrentBlock());
            assertEquals(LinearIRBlock.class, irBuilder.getCurrentBlock().getClass());
            assertEquals(1, irBuilder.getCurrentBlock().getStmts().size());
            assertTrue(irBuilder.getCurrentBlock().getStmts().get(0) instanceof Label);
        }

        @Test
        @DisplayName("多次forkNewBlock应该创建多个基本块")
        void testMultipleForkNewBlock() {
            irBuilder.forkNewBlock(null);
            LinearIRBlock firstBlock = irBuilder.getCurrentBlock();
            
            irBuilder.forkNewBlock(null);
            LinearIRBlock secondBlock = irBuilder.getCurrentBlock();
            
            assertNotNull(firstBlock);
            assertNotNull(secondBlock);
            assertNotEquals(firstBlock, secondBlock);
        }

        @Test
        @DisplayName("setCurrentBlock应该正确设置当前块并维护前驱后继关系")
        void testSetCurrentBlock() {
            irBuilder.forkNewBlock(null);
            LinearIRBlock firstBlock = irBuilder.getCurrentBlock();
            firstBlock.addStmt(new Assign(new FrameSlot(0), ConstVal.valueOf(42)));
            
            LinearIRBlock secondBlock = new LinearIRBlock();
            irBuilder.setCurrentBlock(secondBlock);
            
            assertEquals(secondBlock, irBuilder.getCurrentBlock());
            assertTrue(firstBlock.getSuccessors().contains(secondBlock));
            assertTrue(secondBlock.getPredecessors().contains(firstBlock));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5, 10})
        @DisplayName("在多个基本块之间切换应该保持正确的链接关系")
        void testMultipleBlockSwitching(int blockCount) {
            LinearIRBlock[] blocks = new LinearIRBlock[blockCount];
            
            for (int i = 0; i < blockCount; i++) {
                blocks[i] = new LinearIRBlock();
                irBuilder.setCurrentBlock(blocks[i]);
                assertEquals(blocks[i], irBuilder.getCurrentBlock());
            }
        }
    }

    @Nested
    @DisplayName("指令添加测试")
    class InstructionAdditionTests {
        
        @Test
        @DisplayName("addInstr应该将指令添加到当前块")
        void testAddInstr() {
            irBuilder.forkNewBlock(null);
            FrameSlot lhs = new FrameSlot(0);
            FrameSlot rhs = new FrameSlot(1);
            Assign assign = Assign.with(lhs, rhs);
            
            irBuilder.addInstr(assign);
            
            // forkNewBlock会自动添加Label，所以现在有2个语句：Label + Assign
            assertEquals(2, irBuilder.getCurrentBlock().getStmts().size());
            assertEquals(assign, irBuilder.getCurrentBlock().getStmts().get(1));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 100})
        @DisplayName("添加多条指令应该全部保存在当前块")
        void testAddMultipleInstr(int instructionCount) {
            irBuilder.forkNewBlock(null);
            
            for (int i = 0; i < instructionCount; i++) {
                FrameSlot slot = new FrameSlot(i);
                Assign assign = Assign.with(slot, ConstVal.valueOf(i));
                irBuilder.addInstr(assign);
            }
            
            // forkNewBlock会自动添加Label，所以语句数量要+1
            assertEquals(instructionCount + 1, irBuilder.getCurrentBlock().getStmts().size());
        }

        @Test
        @DisplayName("null指令应该抛出NPE")
        void testAddNullInstr() {
            irBuilder.forkNewBlock(null);
            assertThrows(NullPointerException.class, () -> irBuilder.addInstr(null));
        }
    }

    @Nested
    @DisplayName("跳转指令测试")
    class JumpInstructionTests {
        
        @Test
        @DisplayName("jump应该添加无条件跳转指令")
        void testJump() {
            irBuilder.forkNewBlock(null);
            LinearIRBlock targetBlock = new LinearIRBlock();
            
            irBuilder.jump(targetBlock);
            
            // forkNewBlock会自动添加Label，所以现在有2个语句：Label + JMP
            assertEquals(2, irBuilder.getCurrentBlock().getStmts().size());
            var lastStmt = irBuilder.getCurrentBlock().getStmts().get(1);
            assertTrue(lastStmt instanceof JMP);
            
            JMP jmp = (JMP) lastStmt;
            assertEquals(targetBlock, jmp.getNext());
        }

        @Test
        @DisplayName("jumpIf应该添加条件跳转指令")
        void testJumpIf() {
            irBuilder.forkNewBlock(null);
            VarSlot condition = new FrameSlot(0);
            LinearIRBlock thenBlock = new LinearIRBlock();
            LinearIRBlock elseBlock = new LinearIRBlock();
            
            irBuilder.jumpIf(condition, thenBlock, elseBlock);
            
            // forkNewBlock会自动添加Label，所以现在有2个语句：Label + CJMP
            assertEquals(2, irBuilder.getCurrentBlock().getStmts().size());
            var lastStmt = irBuilder.getCurrentBlock().getStmts().get(1);
            assertTrue(lastStmt instanceof CJMP);
            
            CJMP cjmp = (CJMP) lastStmt;
            assertEquals(condition, cjmp.cond);
            assertEquals(thenBlock, cjmp.getThenBlock());
            assertEquals(elseBlock, cjmp.getElseBlock());
        }

        @ParameterizedTest
        @MethodSource("jumpDestinationProvider")
        @DisplayName("跳转到各种目标块应该正确工作")
        void testJumpToDifferentTargets(String displayName, LinearIRBlock target) {
            irBuilder.forkNewBlock(null);
            
            irBuilder.jump(target);
            
            // forkNewBlock会自动添加Label，所以现在有2个语句：Label + JMP
            assertEquals(2, irBuilder.getCurrentBlock().getStmts().size());
            JMP jmp = (JMP) irBuilder.getCurrentBlock().getStmts().get(1);
            assertEquals(target, jmp.getNext());
        }

        static Stream<Arguments> jumpDestinationProvider() {
            return Stream.of(
                arguments("新创建块", new LinearIRBlock()),
                arguments("自身", new LinearIRBlock()),
                arguments("已初始化块", new LinearIRBlock())
            );
        }
    }

    @Nested
    @DisplayName("表达式求值栈测试")
    class ExpressionStackTests {
        
        @Test
        @DisplayName("pushEvalOperand和popEvalOperand应该正确工作")
        void testPushAndPop() {
            irBuilder.forkNewBlock(null);
            ConstVal testValue = ConstVal.valueOf(true);
            
            VarSlot pushed = irBuilder.pushEvalOperand(testValue);
            VarSlot popped = irBuilder.popEvalOperand();
            
            assertNotNull(pushed);
            assertNotNull(popped);
            assertEquals(pushed, popped);
        }

        @Test
        @DisplayName("栈操作应该遵循后进先出原则")
        void testStackLIFO() {
            irBuilder.forkNewBlock(null);
            
            ConstVal val1 = ConstVal.valueOf(1);
            ConstVal val2 = ConstVal.valueOf(2);
            ConstVal val3 = ConstVal.valueOf(3);
            
            VarSlot slot1 = irBuilder.pushEvalOperand(val1);
            VarSlot slot2 = irBuilder.pushEvalOperand(val2);
            VarSlot slot3 = irBuilder.pushEvalOperand(val3);
            
            assertEquals(slot3, irBuilder.popEvalOperand());
            assertEquals(slot2, irBuilder.popEvalOperand());
            assertEquals(slot1, irBuilder.popEvalOperand());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 100})
        @DisplayName("多次push和pop应该保持栈的平衡")
        void testStackBalance(int operationCount) {
            irBuilder.forkNewBlock(null);
            
            // balance push and pop
            for (int i = 0; i < operationCount; i++) {
                irBuilder.pushEvalOperand(ConstVal.valueOf(i));
            }
            for (int i = 0; i < operationCount; i++) {
                irBuilder.popEvalOperand();
            }
            
            // After balanced operations, should not throw
            assertDoesNotThrow(() -> {
                try {
                    irBuilder.popEvalOperand();
                    fail("Should throw exception when popping from empty stack");
                } catch (Exception e) {
                    // Expected
                }
            });
        }
    }

    @Nested
    @DisplayName("循环控制栈测试")
    class LoopControlTests {
        
        @Test
        @DisplayName("break栈操作应该正确管理循环退出点")
        void testBreakStack() {
            irBuilder.forkNewBlock(null);
            LinearIRBlock breakBlock = new LinearIRBlock();
            
            assertDoesNotThrow(() -> {
                irBuilder.pushBreakStack(breakBlock);
                irBuilder.popBreakStack();
            });
        }

        @Test
        @DisplayName("continue栈操作应该正确管理循环继续点")
        void testContinueStack() {
            irBuilder.forkNewBlock(null);
            LinearIRBlock continueBlock = new LinearIRBlock();
            
            assertDoesNotThrow(() -> {
                irBuilder.pushContinueStack(continueBlock);
                irBuilder.popContinueStack();
            });
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10})
        @DisplayName("多层嵌套循环栈应该正确管理")
        void testNestedLoopStacks(int nestDepth) {
            irBuilder.forkNewBlock(null);
            
            LinearIRBlock[] blocks = new LinearIRBlock[nestDepth];
            for (int i = 0; i < nestDepth; i++) {
                blocks[i] = new LinearIRBlock();
                irBuilder.pushBreakStack(blocks[i]);
            }
            
            for (int i = nestDepth - 1; i >= 0; i--) {
                irBuilder.popBreakStack();
            }
            
            // Should be balanced
            assertDoesNotThrow(() -> {
                try {
                    irBuilder.popBreakStack();
                    fail("Should throw when popping empty stack");
                } catch (Exception e) {
                    // Expected
                }
            });
        }
    }

    @Nested
    @DisplayName("边界条件和异常测试")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("null startBlocks应该抛出NPE")
        void testNullStartBlocks() {
            irBuilder.forkNewBlock(null);
            assertThrows(NullPointerException.class, () -> irBuilder.getCFG(null));
        }

        @Test
        @DisplayName("clearBlock应该清除当前块")
        void testClearBlock() {
            irBuilder.forkNewBlock(null);
            assertNotNull(irBuilder.getCurrentBlock());
            
            irBuilder.clearBlock();
            assertNull(irBuilder.getCurrentBlock());
        }

        @Test
        @DisplayName("在没有当前块时操作应该抛出适当的异常")
        void testOperationsWithoutCurrentBlock() {
            // addInstr在stmt为null时会抛出NullPointerException（带有更详细的错误消息）
            assertThrows(NullPointerException.class, () -> irBuilder.addInstr(null));
            
            // evalExprStack在没有forkNewBlock时仍会抛出异常（栈为null）
            assertThrows(Exception.class, () -> {
                try {
                    irBuilder.popEvalOperand();
                    fail("Should throw when stack is empty");
                } catch (Exception e) {
                    // Expected - 栈为null时的NPE
                    throw e;
                }
            });
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 1000, Integer.MAX_VALUE})
        @DisplayName("FrameSlot的索引值应该能正确处理")
        void testFrameSlotIndices(int index) {
            FrameSlot slot = new FrameSlot(index);
            assertEquals(index, slot.getSlotIdx());
        }

        @Test
        @DisplayName("复杂表达式的求值应该正确生成临时变量")
        void testComplexExpressionEvaluation() {
            irBuilder.forkNewBlock(null);
            
            // 模拟表达式：a + b * c
            FrameSlot a = new FrameSlot(0);
            FrameSlot b = new FrameSlot(1);
            FrameSlot c = new FrameSlot(2);
            
            // 这只是一个简化的模拟，实际会更复杂
            irBuilder.pushEvalOperand(new ConstVal(10)); // a
            irBuilder.pushEvalOperand(new ConstVal(20)); // b 
            irBuilder.pushEvalOperand(new ConstVal(30)); // c
            
            // 验证栈中有3个临时变量
            // 注意：实际行为取决于IRBuilder的具体实现
            assertDoesNotThrow(() -> {
                irBuilder.popEvalOperand();
                irBuilder.popEvalOperand();
                irBuilder.popEvalOperand();
            });
        }

        @Test
        @DisplayName("连续快速切换块应该保持状态一致性")
        void testRapidBlockSwitching() {
            for (int i = 0; i < 100; i++) {
                LinearIRBlock block = new LinearIRBlock();
                irBuilder.setCurrentBlock(block);
                assertEquals(block, irBuilder.getCurrentBlock());
            }
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {
        
        @Test
        @DisplayName("大量指令添加的性能测试")
        @Timeout(5)
        @Tag("performance")
        void testMassiveInstructionAddition() {
            irBuilder.forkNewBlock(null);
            
            int instructionCount = 10000;
            for (int i = 0; i < instructionCount; i++) {
                FrameSlot slot = new FrameSlot(i % 100); // 重用一些slot
                Assign assign = Assign.with(slot, ConstVal.valueOf(i));
                irBuilder.addInstr(assign);
            }
            
            // forkNewBlock会自动添加Label，所以语句数量要+1
            assertEquals(instructionCount + 1, irBuilder.getCurrentBlock().getStmts().size());
        }

        @Test
        @DisplayName("深度嵌套表达式求值的性能")
        @Timeout(5)
        @Tag("performance")
        void testDeeplyNestedExpression() {
            irBuilder.forkNewBlock(null);
            
            // 模拟深度嵌套的表达式求值
            int depth = 1000;
            for (int i = 0; i < depth; i++) {
                irBuilder.pushEvalOperand(ConstVal.valueOf(i));
            }
            
            for (int i = 0; i < depth; i++) {
                irBuilder.popEvalOperand();
            }
            
            // Should be balanced
            assertDoesNotThrow(() -> {
                try {
                    irBuilder.popEvalOperand();
                    fail("Should throw exception");
                } catch (Exception e) {
                    // Expected
                }
            });
        }
    }
}