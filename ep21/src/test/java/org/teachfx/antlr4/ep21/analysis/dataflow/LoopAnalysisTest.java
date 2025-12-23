package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.utils.Kind;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 循环分析测试套件
 *
 * 测试内容：
 * - 循环识别（自然循环、回边）
 * - 循环入口/出口分析
 * - 循环不变式检测基础
 */
@DisplayName("循环分析测试")
@Tag("dataflow")
@Tag("loop-analysis")
class LoopAnalysisTest {

    private LoopAnalysis<IRNode> loopAnalysis;

    @BeforeEach
    void setUp() {
        loopAnalysis = new LoopAnalysis<>();
    }

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("应该能够创建循环分析器")
        void testCanCreateLoopAnalysis() {
            assertNotNull(loopAnalysis);
        }

        @Test
        @DisplayName("初始状态应该为空")
        void testInitialState() {
            assertTrue(loopAnalysis.getLoops().isEmpty());
            assertTrue(loopAnalysis.getBackEdges().isEmpty());
        }
    }

    @Nested
    @DisplayName("简单循环测试")
    class SimpleLoopTests {

        @Test
        @DisplayName("应该能够识别简单while循环")
        void testWhileLoopDetection() {
            // L0: entry
            // L1: loop_header (回边 L3 -> L1)
            // L2: loop_body
            // L3: loop_end -> L1 (if condition true)

            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                blocks.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
            }

            // 边: 0->1, 1->2, 2->3, 3->1 (回边)
            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(Triple.of(0, 1, 1));
            edges.add(Triple.of(1, 2, 1));
            edges.add(Triple.of(2, 3, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(3, 1, 1)); // 回边

            CFG<IRNode> cfg = new CFG<>(blocks, edges);
            loopAnalysis.analyze(cfg);

            // 应该检测到1个自然循环
            assertEquals(1, loopAnalysis.getLoops().size());
        }

        @Test
        @DisplayName("应该能够识别嵌套循环")
        void testNestedLoopDetection() {
            // L0: entry
            // L1: outer_header (回边 L4 -> L1)
            // L2: outer_body
            // L3: inner_header (回边 L4 -> L3)
            // L4: inner_body -> L3/L1

            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                blocks.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
            }

            // 边: 0->1, 1->2, 2->3, 3->4, 4->3 (内层回边), 4->1 (外层回边)
            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(Triple.of(0, 1, 1));
            edges.add(Triple.of(1, 2, 1));
            edges.add(Triple.of(2, 3, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(3, 4, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(4, 3, 1)); // 内层回边
            edges.add(org.apache.commons.lang3.tuple.Triple.of(4, 1, 1)); // 外层回边

            CFG<IRNode> cfg = new CFG<>(blocks, edges);
            loopAnalysis.analyze(cfg);

            // 应该检测到2个自然循环
            assertEquals(2, loopAnalysis.getLoops().size());
        }

        @Test
        @DisplayName("应该能够识别for循环")
        void testForLoopDetection() {
            // L0: init
            // L1: header (回边 L3 -> L1)
            // L2: body
            // L3: update -> L1

            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                blocks.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
            }

            // 边: 0->1, 1->2, 2->3, 3->1 (回边), 1->exit
            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(Triple.of(0, 1, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(2, 3, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(3, 1, 1)); // 回边

            CFG<IRNode> cfg = new CFG<>(blocks, edges);
            loopAnalysis.analyze(cfg);

            assertEquals(1, loopAnalysis.getLoops().size());
        }
    }

    @Nested
    @DisplayName("循环属性测试")
    class LoopPropertyTests {

        @Test
        @DisplayName("应该能够获取循环入口节点")
        void testLoopHeaderDetection() {
            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                blocks.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
            }

            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(2, 3, 1));
            edges.add(Triple.of(3, 1, 1));

            CFG<IRNode> cfg = new CFG<>(blocks, edges);
            loopAnalysis.analyze(cfg);

            List<NaturalLoop<IRNode>> loops = loopAnalysis.getLoops();
            assertFalse(loops.isEmpty());
            assertEquals(1, loops.get(0).getHeader().getId());
        }

        @Test
        @DisplayName("应该能够获取循环的所有节点")
        void testLoopNodes() {
            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                blocks.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
            }

            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(2, 3, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(3, 1, 1));

            CFG<IRNode> cfg = new CFG<>(blocks, edges);
            loopAnalysis.analyze(cfg);

            List<NaturalLoop<IRNode>> loops = loopAnalysis.getLoops();
            assertFalse(loops.isEmpty());

            Set<Integer> loopNodes = loops.get(0).getLoopNodes();
            System.out.println("DEBUG: Loop nodes = " + loopNodes);
            System.out.println("DEBUG: Back edges = " + loopAnalysis.getBackEdges());
            System.out.println("DEBUG: Dominance = " + loopAnalysis.getDominance());

            // 循环应该包含节点1, 2, 3
            assertTrue(loopNodes.contains(1), "Header node 1 should be in loop");
            assertTrue(loopNodes.contains(2), "Node 2 should be in loop");
            assertTrue(loopNodes.contains(3), "Back edge source node 3 should be in loop");
        }

        @Test
        @DisplayName("应该能够检测回边")
        void testBackEdgeDetection() {
            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                blocks.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
            }

            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(2, 3, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(3, 1, 1));

            CFG<IRNode> cfg = new CFG<>(blocks, edges);
            loopAnalysis.analyze(cfg);

            assertFalse(loopAnalysis.getBackEdges().isEmpty());
            // 回边应该是 3 -> 1
            assertTrue(loopAnalysis.getBackEdges().containsKey(3));
            assertTrue(loopAnalysis.getBackEdges().get(3).contains(1));
        }
    }

    @Nested
    @DisplayName("非循环代码测试")
    class NonLoopTests {

        @Test
        @DisplayName("直线代码应该没有循环")
        void testLinearCodeNoLoop() {
            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                blocks.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
            }

            // 直线代码：0->1->2->3，没有回边
            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(2, 3, 1));

            CFG<IRNode> cfg = new CFG<>(blocks, edges);
            loopAnalysis.analyze(cfg);

            assertTrue(loopAnalysis.getLoops().isEmpty());
        }

        @Test
        @DisplayName("空CFG应该没有循环")
        void testEmptyCFGNoLoop() {
            CFG<IRNode> cfg = new CFG<>(new ArrayList<>(), new ArrayList<>());
            loopAnalysis.analyze(cfg);

            assertTrue(loopAnalysis.getLoops().isEmpty());
        }

        @Test
        @DisplayName("if-else代码应该没有循环")
        void testIfElseNoLoop() {
            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                blocks.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
            }

            // if-else: 0->1, 0->2, 1->3, 2->3
            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 2, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 3, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(2, 3, 1));

            CFG<IRNode> cfg = new CFG<>(blocks, edges);
            loopAnalysis.analyze(cfg);

            assertTrue(loopAnalysis.getLoops().isEmpty());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("单个基本块应该没有循环")
        void testSingleBlockNoLoop() {
            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            blocks.add(new BasicBlock<>(
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("L0", null),
                0
            ));

            CFG<IRNode> cfg = new CFG<>(blocks, new ArrayList<>());
            loopAnalysis.analyze(cfg);

            assertTrue(loopAnalysis.getLoops().isEmpty());
        }

        @Test
        @DisplayName("自环应该被识别为循环")
        void testSelfLoop() {
            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            blocks.add(new BasicBlock<>(
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("L0", null),
                0
            ));

            // 自环：0->0
            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 0, 1));

            CFG<IRNode> cfg = new CFG<>(blocks, edges);
            loopAnalysis.analyze(cfg);

            assertFalse(loopAnalysis.getLoops().isEmpty());
        }

        @Test
        @DisplayName("循环应该支持多次调用")
        void testMultipleAnalysisCalls() {
            List<BasicBlock<IRNode>> blocks = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                blocks.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
            }

            List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(2, 3, 1));
            edges.add(org.apache.commons.lang3.tuple.Triple.of(3, 1, 1));

            CFG<IRNode> cfg = new CFG<>(blocks, edges);

            assertDoesNotThrow(() -> {
                loopAnalysis.analyze(cfg);
                loopAnalysis.analyze(cfg);
                loopAnalysis.analyze(cfg);
            });
        }
    }
}
