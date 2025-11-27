package org.teachfx.antlr4.ep21.pass.cfg;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.utils.Kind;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * BasicBlock类的重构单元测试
 * 使用JUnit5特性：@Nested, @ParameterizedTest, @DisplayName等
 */
@DisplayName("BasicBlock单元测试")
@Tag("cfg")
class BasicBlockTest {

    private List<BasicBlock<IRNode>> cachedNodes;

    @BeforeEach
    void setUp() {
        cachedNodes = new ArrayList<>();
    }

    @Nested
    @DisplayName("从LinearIRBlock构建BasicBlock")
    class BuildFromLinearBlockTests {
        
        @Test
        @DisplayName("空LinearIRBlock应该创建空BasicBlock")
        void testEmptyLinearBlock() {
            LinearIRBlock block = new LinearIRBlock();
            BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);
            
            assertTrue(basicBlock.isEmpty(), "BasicBlock应该为空");
            assertEquals(0, basicBlock.codes.size());
        }

        @Test
        @DisplayName("单指令LinearIRBlock应该创建单指令BasicBlock")
        void testSingleInstruction() {
            LinearIRBlock block = new LinearIRBlock();
            block.addStmt(new Label("L1", null));
            
            BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);
            
            assertEquals(1, basicBlock.codes.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {2, 5, 10, 100})
        @DisplayName("多指令LinearIRBlock应该创建相同指令数的BasicBlock")
        void testMultipleInstructions(int instructionCount) {
            LinearIRBlock block = new LinearIRBlock();
            for (int i = 0; i < instructionCount; i++) {
                block.addStmt(new Label("L" + i, null));
            }
            
            BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);
            
            assertEquals(instructionCount, basicBlock.codes.size(),
                       "BasicBlock应该有相同数量的指令");
        }

        @ParameterizedTest
        @MethodSource("instructionProvider")
        @DisplayName("不同类型的指令应该被正确识别和构建")
        void testDifferentInstructionTypes(String displayName, Stmt instruction, Class<?> expectedType) {
            LinearIRBlock block = new LinearIRBlock();
            block.addStmt(instruction);
            
            BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);
            
            assertEquals(expectedType, basicBlock.getLastInstr().getClass(),
                       displayName + " 应该被正确识别");
        }

        static Stream<Arguments> instructionProvider() {
            LinearIRBlock targetBlock = new LinearIRBlock();
            FrameSlot condSlot = new FrameSlot(0);
            
            return Stream.of(
                arguments("JMP指令", new JMP(targetBlock), JMP.class),
                arguments("CJMP指令", new CJMP(condSlot, targetBlock, new LinearIRBlock()), CJMP.class),
                arguments("FuncEntryLabel", new FuncEntryLabel("main", 0, 0, null), FuncEntryLabel.class),
                arguments("Label指令", new Label("L1", null), Label.class)
            );
        }

        @Test
        @DisplayName("混合指令类型应该被正确处理")
        void testMixedInstructions() {
            LinearIRBlock block = new LinearIRBlock();
            LinearIRBlock targetBlock = new LinearIRBlock();
            
            block.addStmt(new Label("MixedLabel", null));
            block.addStmt(new Label("AnotherLabel", null));
            block.addStmt(new JMP(targetBlock));
            
            BasicBlock<IRNode> basicBlock = BasicBlock.buildFromLinearBlock(block, cachedNodes);
            
            assertEquals(3, basicBlock.codes.size());
            assertTrue(basicBlock.getLastInstr() instanceof JMP);
        }
    }

    @Nested
    @DisplayName("基本块属性测试")
    class BlockPropertyTests {
        
        @Test
        @DisplayName("新建BasicBlock应该有正确的初始状态")
        void testInitialState() {
            List<Loc<IRNode>> codes = new ArrayList<>();
            Label label = new Label("L1", null);
            BasicBlock<IRNode> block = new BasicBlock<>(Kind.CONTINUOUS, codes, label, 1);
            
            assertEquals(1, block.id);
            assertNotNull(block.codes);
            assertTrue(block.codes.isEmpty());
            assertNotNull(block.liveOut);
            assertNotNull(block.def);
        }

        @Test
        @DisplayName("codes列表应该正确存储指令")
        void testCodesList() {
            List<Loc<IRNode>> codes = new ArrayList<>();
            Label label = new Label("L1", null);
            BasicBlock<IRNode> block = new BasicBlock<>(Kind.CONTINUOUS, codes, label, 1);
            
            block.codes.add(new Loc<>(new Label("L2", null)));
            block.codes.add(new Loc<>(Assign.with(new FrameSlot(0), ConstVal.valueOf(42))));
            
            assertEquals(2, block.codes.size());
        }

        @ParameterizedTest
        @CsvSource({
            "true, true, true",   // empty + empty = true (empty + empty = empty)
            "true, false, true",  // empty + non-empty = true (second block gets cleared)
            "false, true, true",  // non-empty + empty = true (empty + empty = empty)
            "false, false, true"  // non-empty + non-empty = true (second block gets cleared)
        })
        @DisplayName("合并后第二个块应该被清空")
        void testIsEmptyAfterMerge(boolean firstEmpty, boolean secondEmpty, boolean expectedEmpty) {
            List<Loc<IRNode>> codes1 = new ArrayList<>();
            List<Loc<IRNode>> codes2 = new ArrayList<>();
            Label label1 = new Label("L1", null);
            Label label2 = new Label("L2", null);
            
            BasicBlock<IRNode> block1 = new BasicBlock<>(Kind.CONTINUOUS, codes1, label1, 1);
            BasicBlock<IRNode> block2 = new BasicBlock<>(Kind.CONTINUOUS, codes2, label2, 2);
            
            if (!firstEmpty) {
                block1.codes.add(new Loc<>(new Label("L1", null)));
            }
            if (!secondEmpty) {
                block2.codes.add(new Loc<>(new Label("L2", null)));
            }
            
            block1.mergeNearBlock(block2);
            
            // mergeNearBlock should always clear the second block
            assertTrue(block2.isEmpty(), "第二个块合并后应该被清空");
        }
    }

    @Nested
    @DisplayName("基本块合并测试")
    class BlockMergeTests {
        
        @Test
        @DisplayName("合并相邻块应该移动指令并清空第二个块")
        void testMergeNearBlock() {
            List<Loc<IRNode>> codes1 = new ArrayList<>();
            List<Loc<IRNode>> codes2 = new ArrayList<>();
            Label label1 = new Label("L1", null);
            Label label2 = new Label("L2", null);
            
            BasicBlock<IRNode> block1 = new BasicBlock<>(Kind.CONTINUOUS, codes1, label1, 1);
            BasicBlock<IRNode> block2 = new BasicBlock<>(Kind.CONTINUOUS, codes2, label2, 2);
            
            block2.codes.add(new Loc<>(new Label("L2", null)));
            block2.codes.add(new Loc<>(Assign.with(new FrameSlot(0), ConstVal.valueOf(42))));
            
            int initialSize = block1.codes.size();
            int block2Size = block2.codes.size();
            
            block1.mergeNearBlock(block2);
            
            assertTrue(block2.isEmpty(), "第二个块应该被清空");
            assertEquals(initialSize + block2Size, block1.codes.size());
        }

        @Test
        @DisplayName("连续合并应该累积所有指令")
        void testChainedMerge() {
            List<Loc<IRNode>> codes1 = new ArrayList<>();
            List<Loc<IRNode>> codes2 = new ArrayList<>();
            List<Loc<IRNode>> codes3 = new ArrayList<>();
            Label label1 = new Label("L1", null);
            Label label2 = new Label("L2", null);
            Label label3 = new Label("L3", null);
            
            BasicBlock<IRNode> block1 = new BasicBlock<>(Kind.CONTINUOUS, codes1, label1, 1);
            BasicBlock<IRNode> block2 = new BasicBlock<>(Kind.CONTINUOUS, codes2, label2, 2);
            BasicBlock<IRNode> block3 = new BasicBlock<>(Kind.CONTINUOUS, codes3, label3, 3);
            
            block2.codes.add(new Loc<>(new Label("L2", null)));
            block3.codes.add(new Loc<>(new Label("L3", null)));
            
            block1.mergeNearBlock(block2);
            block1.mergeNearBlock(block3);
            
            assertEquals(2, block1.codes.size());
            assertTrue(block2.isEmpty());
            assertTrue(block3.isEmpty());
        }

        @Test
        @DisplayName("合并空块应该不改变第一个块")
        void testMergeEmptyBlock() {
            List<Loc<IRNode>> codes1 = new ArrayList<>();
            List<Loc<IRNode>> codes2 = new ArrayList<>();
            Label label1 = new Label("L1", null);
            Label label2 = new Label("L2", null);
            
            BasicBlock<IRNode> block1 = new BasicBlock<>(Kind.CONTINUOUS, codes1, label1, 1);
            block1.codes.add(new Loc<>(new Label("L1", null)));
            BasicBlock<IRNode> emptyBlock = new BasicBlock<>(Kind.CONTINUOUS, codes2, label2, 2);
            int initialSize = block1.codes.size();
            
            block1.mergeNearBlock(emptyBlock);
            
            assertEquals(initialSize, block1.codes.size());
            assertTrue(emptyBlock.isEmpty());
        }
    }

    @Nested
    @DisplayName("边界条件和异常测试")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("获取最后一个指令时应该返回正确的指令")
        void testGetLastInstr() {
            List<Loc<IRNode>> codes = new ArrayList<>();
            Label label = new Label("L1", null);
            BasicBlock<IRNode> block = new BasicBlock<>(Kind.CONTINUOUS, codes, label, 1);
            
            // 空块应该返回null
            assertNull(block.getLastInstr());
            
            // 添加指令后应该返回最后一个
            Label label1 = new Label("L1", null);
            Label label2 = new Label("L2", null);
            block.codes.add(new Loc<>(label1));
            block.codes.add(new Loc<>(label2));
            
            assertEquals(label2, block.getLastInstr());
        }

        @Test
        @DisplayName("大量指令的性能测试")
        @Timeout(2)
        void testPerformanceWithManyInstructions() {
            List<Loc<IRNode>> codes = new ArrayList<>();
            Label label = new Label("L1", null);
            BasicBlock<IRNode> block = new BasicBlock<>(Kind.CONTINUOUS, codes, label, 1);
            
            // 添加10000个指令
            for (int i = 0; i < 10000; i++) {
                block.codes.add(new Loc<>(new Label("L" + i, null)));
            }
            
            assertEquals(10000, block.codes.size());
            assertNotNull(block.getLastInstr());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100, Integer.MAX_VALUE})
        @DisplayName("各种块ID都应该被正确处理")
        void testBlockIdBoundaries(int blockId) {
            List<Loc<IRNode>> codes = new ArrayList<>();
            Label label = new Label("L1", null);
            BasicBlock<IRNode> block = new BasicBlock<>(Kind.CONTINUOUS, codes, label, blockId);
            assertEquals(blockId, block.id);
        }
    }

    @Nested
    @DisplayName("数据流分析相关测试")
    class DataFlowTests {
        
        @Test
        @DisplayName("活跃变量集合应该正确初始化")
        void testLiveVariableInitialization() {
            List<Loc<IRNode>> codes = new ArrayList<>();
            Label label = new Label("L1", null);
            BasicBlock<IRNode> block = new BasicBlock<>(Kind.CONTINUOUS, codes, label, 1);
            
            assertNotNull(block.liveOut);
            assertTrue(block.liveOut.isEmpty());
            assertNotNull(block.def);
            assertTrue(block.def.isEmpty());
        }

        @Test
        @DisplayName("def集合应该记录定义的变量")
        void testDefSet() {
            List<Loc<IRNode>> codes = new ArrayList<>();
            Label label = new Label("L1", null);
            BasicBlock<IRNode> block = new BasicBlock<>(Kind.CONTINUOUS, codes, label, 1);
            
            // 模拟变量定义
            FrameSlot var1 = new FrameSlot(0);
            FrameSlot var2 = new FrameSlot(1);
            
            block.def.add(var1);
            block.def.add(var2);
            
            assertEquals(2, block.def.size());
            assertTrue(block.def.contains(var1));
            assertTrue(block.def.contains(var2));
        }
    }
}
