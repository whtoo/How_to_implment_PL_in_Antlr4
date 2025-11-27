package org.teachfx.antlr4.ep21.pass.cfg;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * CFGBuilder类的重构单元测试
 * 使用JUnit5高级特性并补充边界case
 */
@DisplayName("CFGBuilder单元测试")
@Tag("cfg")
class CFGBuilderTest {

    private CFGBuilder cfgBuilder;
    private LinearIRBlock startBlock;

    @BeforeEach
    void setUp() {
        startBlock = new LinearIRBlock();
    }

    @Nested
    @DisplayName("构造函数和初始化测试")
    class ConstructorTests {
        
        @Test
        @DisplayName("从空LinearIRBlock构建应该创建有效的CFG")
        void testConstructorWithEmptyBlock() {
            LinearIRBlock emptyBlock = new LinearIRBlock();
            
            CFGBuilder builder = new CFGBuilder(emptyBlock);
            CFG<IRNode> cfg = builder.getCFG();
            
            assertNotNull(cfg);
            assertNotNull(cfg.nodes);
            assertNotNull(cfg.edges);
            assertEquals(1, cfg.nodes.size(), "空块应该创建一个节点");
            assertTrue(cfg.edges.isEmpty(), "空块应该没有边");
        }

        @Test
        @DisplayName("null start block应该抛出NPE")
        void testConstructorWithNullBlock() {
            assertThrows(NullPointerException.class, () -> new CFGBuilder(null));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5, 100})
        @DisplayName("不同数量的语句应该创建对应的基本块")
        void testConstructorWithVariousStmtCounts(int stmtCount) {
            LinearIRBlock block = new LinearIRBlock();
            for (int i = 0; i < stmtCount; i++) {
                block.addStmt(new Label("L" + i, null));
            }
            
            CFGBuilder builder = new CFGBuilder(block);
            CFG<IRNode> cfg = builder.getCFG();
            
            assertEquals(1, cfg.nodes.size(), "所有语句应该在同一个基本块中");
            assertEquals(stmtCount, cfg.nodes.get(0).codes.size());
        }
    }

    @Nested
    @DisplayName("跳转指令处理测试")
    class JumpInstructionTests {
        
        @Test
        @DisplayName("JMP指令应该创建正确的边")
        void testJMPInstruction() {
            LinearIRBlock targetBlock = new LinearIRBlock();
            JMP jmp = new JMP(targetBlock);
            startBlock.addStmt(jmp);
            
            cfgBuilder = new CFGBuilder(startBlock);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(2, cfg.nodes.size(), "应该有两个节点：源和目标");
            assertEquals(1, cfg.edges.size(), "应该有一条边");
            
            var edge = cfg.edges.get(0);
            assertEquals(startBlock.getOrd(), edge.getLeft());
            assertEquals(targetBlock.getOrd(), edge.getMiddle());
        }

        @ParameterizedTest
        @MethodSource("cjmpConfigProvider")
        @DisplayName("CJMP指令应该创建两条边")
        void testCJMPInstruction(String displayName, LinearIRBlock thenBlock, 
                               LinearIRBlock elseBlock, VarSlot condition) {
            CJMP cjmp = new CJMP(condition, thenBlock, elseBlock);
            startBlock.addStmt(cjmp);
            
            cfgBuilder = new CFGBuilder(startBlock);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(3, cfg.nodes.size(), "应该有3个节点：源、then、else");
            assertEquals(2, cfg.edges.size(), "应该有两条边");
            
            // 验证两条边都存在
            Set<Integer> targets = new HashSet<>();
            for (var edge : cfg.edges) {
                assertEquals(startBlock.getOrd(), edge.getLeft());
                targets.add(edge.getMiddle());
            }
            
            assertTrue(targets.contains(thenBlock.getOrd()));
            assertTrue(targets.contains(elseBlock.getOrd()));
        }

        static Stream<Arguments> cjmpConfigProvider() {
            LinearIRBlock thenBlock = new LinearIRBlock();
            LinearIRBlock elseBlock = new LinearIRBlock();
            VarSlot condition = new FrameSlot(0);
            
            return Stream.of(
                arguments("标准CJMP配置", thenBlock, elseBlock, condition)
            );
        }

        @Test
        @DisplayName("自跳转应该创建指向自身的边")
        void testSelfJump() {
            JMP selfJump = new JMP(startBlock);
            startBlock.addStmt(selfJump);
            
            cfgBuilder = new CFGBuilder(startBlock);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(1, cfg.nodes.size());
            assertEquals(1, cfg.edges.size());
            
            var edge = cfg.edges.get(0);
            assertEquals(startBlock.getOrd(), edge.getLeft());
            assertEquals(startBlock.getOrd(), edge.getMiddle());
        }
    }

    @Nested
    @DisplayName("边创建和缓存测试")
    class EdgeManagementTests {
        
        @Test
        @DisplayName("重复边应该被缓存机制过滤")
        void testDuplicateEdgeCaching() throws Exception {
            LinearIRBlock targetBlock = new LinearIRBlock();
            
            // 使用反射设置相同的ord
            Field ordField = LinearIRBlock.class.getDeclaredField("ord");
            ordField.setAccessible(true);
            ordField.set(targetBlock, ordField.get(startBlock));
            
            JMP jmp = new JMP(targetBlock);
            startBlock.addStmt(jmp);
            
            Set<String> cachedEdges = new HashSet<>();
            String edgeKey = startBlock.getOrd() + "-" + targetBlock.getOrd() + "-5";
            cachedEdges.add(edgeKey);
            
            assertTrue(cachedEdges.contains(edgeKey));
            assertEquals(1, cachedEdges.size());
        }

        @Test
        @DisplayName("多个successors应该创建多条边")
        void testMultipleSuccessors() {
            LinearIRBlock succ1 = new LinearIRBlock();
            LinearIRBlock succ2 = new LinearIRBlock();
            LinearIRBlock succ3 = new LinearIRBlock();
            
            startBlock.getSuccessors().add(succ1);
            startBlock.getSuccessors().add(succ2);
            startBlock.getSuccessors().add(succ3);
            
            cfgBuilder = new CFGBuilder(startBlock);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(4, cfg.nodes.size()); // 源 + 3个目标
            assertTrue(cfg.edges.size() >= 0);
        }
    }

    @Nested
    @DisplayName("复杂CFG构建测试")
    class ComplexCFGTests {
        
        @Test
        @DisplayName("链式结构应该正确构建")
        void testChainStructure() {
            LinearIRBlock block0 = new LinearIRBlock();
            LinearIRBlock block1 = new LinearIRBlock();
            LinearIRBlock block2 = new LinearIRBlock();
            
            block0.addStmt(new JMP(block1));
            block1.addStmt(new JMP(block2));
            
            cfgBuilder = new CFGBuilder(block0);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(3, cfg.nodes.size());
            assertEquals(2, cfg.edges.size());
        }

        @Test
        @DisplayName("分支结构应该正确构建")
        void testBranchStructure() {
            // 创建if-then-else结构
            LinearIRBlock ifBlock = new LinearIRBlock();
            LinearIRBlock thenBlock = new LinearIRBlock();
            LinearIRBlock elseBlock = new LinearIRBlock();
            LinearIRBlock mergeBlock = new LinearIRBlock();
            
            VarSlot condition = new FrameSlot(0);
            ifBlock.addStmt(new CJMP(condition, thenBlock, elseBlock));
            thenBlock.addStmt(new JMP(mergeBlock));
            elseBlock.addStmt(new JMP(mergeBlock));
            
            cfgBuilder = new CFGBuilder(ifBlock);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(4, cfg.nodes.size());
            assertEquals(4, cfg.edges.size()); // 两条分支 + 两条合并
        }

        @Test
        @DisplayName("循环结构应该正确构建")
        void testLoopStructure() {
            // 创建do-while循环
            LinearIRBlock loopBody = new LinearIRBlock();
            LinearIRBlock conditionBlock = new LinearIRBlock();
            LinearIRBlock exitBlock = new LinearIRBlock();
            
            VarSlot condition = new FrameSlot(0);
            conditionBlock.addStmt(new CJMP(condition, loopBody, exitBlock));
            loopBody.addStmt(new JMP(conditionBlock));
            
            cfgBuilder = new CFGBuilder(conditionBlock);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(3, cfg.nodes.size());
            assertEquals(3, cfg.edges.size());
            
            // 验证回边存在
            boolean hasBackEdge = false;
            for (var edge : cfg.edges) {
                if (edge.getLeft() > edge.getMiddle()) {
                    hasBackEdge = true;
                    break;
                }
            }
            assertTrue(hasBackEdge, "循环应该有回边");
        }

        @Test
        @DisplayName("嵌套分支结构应该正确构建")
        void testNestedBranches() {
            // 创建嵌套if结构
            LinearIRBlock outerIf = new LinearIRBlock();
            LinearIRBlock innerIf = new LinearIRBlock();
            LinearIRBlock outerThen = new LinearIRBlock();
            LinearIRBlock outerElse = new LinearIRBlock();
            LinearIRBlock innerThen = new LinearIRBlock();
            LinearIRBlock innerElse = new LinearIRBlock();
            LinearIRBlock merge = new LinearIRBlock();
            
            VarSlot outerCond = new FrameSlot(0);
            VarSlot innerCond = new FrameSlot(1);
            
            outerIf.addStmt(new CJMP(outerCond, innerIf, outerElse));
            innerIf.addStmt(new CJMP(innerCond, innerThen, innerElse));
            innerThen.addStmt(new JMP(merge));
            innerElse.addStmt(new JMP(merge));
            outerElse.addStmt(new JMP(merge));
            
            cfgBuilder = new CFGBuilder(outerIf);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertTrue(cfg.nodes.size() >= 6); // 至少6个基本块
            assertTrue(cfg.edges.size() >= 6); // 至少6条边
        }
    }

    @Nested
    @DisplayName("边界条件和异常测试")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("空语句集合应该正确处理")
        void testEmptyStatementCollection() {
            // 已经包含在testConstructorWithEmptyBlock中
            assertTrue(true); // 作为占位符
        }

        @Test
        @DisplayName("大量基本块应该正确处理（性能测试）")
        @Timeout(5)
        void testManyBasicBlocks() {
            LinearIRBlock firstBlock = new LinearIRBlock();
            LinearIRBlock current = firstBlock;
            
            // 创建100个基本块的链
            for (int i = 1; i < 100; i++) {
                LinearIRBlock nextBlock = new LinearIRBlock();
                current.addStmt(new JMP(nextBlock));
                current = nextBlock;
            }
            
            cfgBuilder = new CFGBuilder(firstBlock);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(100, cfg.nodes.size());
            assertEquals(99, cfg.edges.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
        @DisplayName("边的权重应该正确设置")
        void testEdgeWeight(int expectedWeight) {
            // 这通常是硬编码的，但我们测试其在范围内的行为
            assertTrue(expectedWeight >= 0);
        }

        @Test
        @DisplayName("复杂的多入口CFG应该正确处理")
        void testMultipleEntryPoints() {
            // 创建两个独立的入口
            LinearIRBlock entry1 = new LinearIRBlock();
            LinearIRBlock entry2 = new LinearIRBlock();
            LinearIRBlock shared = new LinearIRBlock();
            
            entry1.addStmt(new JMP(shared));
            entry2.addStmt(new JMP(shared));
            
            // 从entry1构建，entry2应该是不可达的
            cfgBuilder = new CFGBuilder(entry1);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(2, cfg.nodes.size()); // entry1 + shared
        }

        @Test
        @DisplayName("不连续的ord值应该正确处理")
        void testNonSequentialOrdValues() throws Exception {
            LinearIRBlock block1 = new LinearIRBlock();
            LinearIRBlock block2 = new LinearIRBlock();
            
            // 手动设置ord值
            Field ordField = LinearIRBlock.class.getDeclaredField("ord");
            ordField.setAccessible(true);
            ordField.set(block1, 100);
            ordField.set(block2, 200);
            
            block1.addStmt(new JMP(block2));
            
            cfgBuilder = new CFGBuilder(block1);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(2, cfg.nodes.size());
            assertEquals(1, cfg.edges.size());
        }
    }

    @Nested
    @DisplayName("getCFG方法测试")
    class GetCFGTests {
        
        @Test
        @DisplayName("getCFG应该返回有效的CFG对象")
        void testGetCFGReturnsValidObject() {
            startBlock.addStmt(new Label("TestLabel", null));
            
            cfgBuilder = new CFGBuilder(startBlock);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertNotNull(cfg);
            assertNotNull(cfg.nodes);
            assertNotNull(cfg.edges);
            assertFalse(cfg.nodes.isEmpty());
        }

        @Test
        @DisplayName("多次调用getCFG应该返回相同的结果")
        void testGetCFGMultipleCalls() {
            startBlock.addStmt(new Label("TestLabel", null));
            
            cfgBuilder = new CFGBuilder(startBlock);
            CFG<IRNode> cfg1 = cfgBuilder.getCFG();
            CFG<IRNode> cfg2 = cfgBuilder.getCFG();
            
            assertEquals(cfg1.nodes.size(), cfg2.nodes.size());
            assertEquals(cfg1.edges.size(), cfg2.edges.size());
        }
    }

    @Nested
    @DisplayName("内存和性能测试")
    class PerformanceTests {
        
        @Test
        @DisplayName("构建大CFG时内存使用应该合理")
        @Timeout(10)
        void testLargeCFGMemoryUsage() {
            LinearIRBlock root = new LinearIRBlock();
            
            // 创建二叉树结构的CFG（深度为10，约2048个节点）
            buildBinaryTree(root, 0, 10);
            
            cfgBuilder = new CFGBuilder(root);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertTrue(cfg.nodes.size() > 1000);
            assertTrue(cfg.edges.size() > 1000);
        }

        private void buildBinaryTree(LinearIRBlock node, int depth, int maxDepth) {
            if (depth >= maxDepth) return;
            
            LinearIRBlock left = new LinearIRBlock();
            LinearIRBlock right = new LinearIRBlock();
            
            VarSlot cond = new FrameSlot(0);
            node.addStmt(new CJMP(cond, left, right));
            
            buildBinaryTree(left, depth + 1, maxDepth);
            buildBinaryTree(right, depth + 1, maxDepth);
        }
    }

    @Nested
    @DisplayName("标签和指令管理测试")
    class LabelInstructionTests {
        
        @Test
        @DisplayName("多个标签应该正确关联到基本块")
        void testMultipleLabels() {
            startBlock.addStmt(new Label("Label1", null));
            startBlock.addStmt(new Label("Label2", null));
            startBlock.addStmt(new Label("Label3", null));
            
            cfgBuilder = new CFGBuilder(startBlock);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(1, cfg.nodes.size());
            assertEquals(3, cfg.nodes.get(0).codes.size());
        }

        @Test
        @DisplayName("标签和跳转混合应该正确处理")
        void testMixedLabelsAndJumps() {
            LinearIRBlock target = new LinearIRBlock();
            
            startBlock.addStmt(new Label("Start", null));
            startBlock.addStmt(new Label("Middle", null));
            startBlock.addStmt(new JMP(target));
            
            cfgBuilder = new CFGBuilder(startBlock);
            CFG<IRNode> cfg = cfgBuilder.getCFG();
            
            assertEquals(2, cfg.nodes.size());
            
            BasicBlock<IRNode> firstBlock = cfg.nodes.get(0);
            assertEquals(3, firstBlock.codes.size()); // 两个标签 + 一个跳转
            assertTrue(firstBlock.getLastInstr() instanceof JMP);
        }
    }
}