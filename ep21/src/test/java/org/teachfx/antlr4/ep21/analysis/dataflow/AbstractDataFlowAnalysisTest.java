package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.utils.Kind;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试抽象数据流分析框架
 */
@DisplayName("抽象数据流分析测试")
@Tag("dataflow")
class AbstractDataFlowAnalysisTest {

    private CFG<IRNode> testCFG;

    @BeforeEach
    void setUp() {
        // 创建简单的测试CFG
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            nodes.add(new BasicBlock<>(
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("L" + i, null),
                i
            ));
        }

        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, 1));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 2, 1));

        testCFG = new CFG<>(nodes, edges);
    }

    @Nested
    @DisplayName("数据流分析框架基础测试")
    class FrameworkTests {

        @Test
        @DisplayName("应该能够创建数据流分析实例")
        void testCanCreateDataFlowAnalysis() {
            // Arrange & Act & Assert
            assertDoesNotThrow(() -> {
                TestAnalysis analysis = new TestAnalysis(testCFG);
                assertNotNull(analysis);
            });
        }

        @Test
        @DisplayName("空CFG应该被正确处理")
        void testEmptyCFG() {
            // Arrange
            CFG<IRNode> emptyCFG = new CFG<>(new ArrayList<>(), new ArrayList<>());

            // Act & Assert
            assertDoesNotThrow(() -> new TestAnalysis(emptyCFG));
        }

        @Test
        @DisplayName("单节点CFG应该被正确处理")
        void testSingleNodeCFG() {
            // Arrange
            List<BasicBlock<IRNode>> singleNode = new ArrayList<>();
            singleNode.add(new BasicBlock<>(Kind.CONTINUOUS, new ArrayList<>(), new Label("L0", null), 0));
            CFG<IRNode> singleNodeCFG = new CFG<>(singleNode, new ArrayList<>());

            // Act & Assert
            assertDoesNotThrow(() -> new TestAnalysis(singleNodeCFG));
        }

        @Test
        @DisplayName("前向数据流分析应该正确传播")
        void testForwardFlow() {
            // Arrange
            TestAnalysis analysis = new TestAnalysis(testCFG);

            // Act & Assert
            assertDoesNotThrow(() -> analysis.analyze());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("循环CFG应该被正确处理")
        void testLoopCFG() {
            // Arrange - 创建自环CFG
            var loopNodes = new ArrayList<BasicBlock<IRNode>>();
            loopNodes.add(new BasicBlock<>(Kind.CONTINUOUS, new ArrayList<>(), new Label("L0", null), 0));

            var edges = new ArrayList<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>>();
            edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 0, 1));

            CFG<IRNode> loopCFG = new CFG<>(loopNodes, edges);

            // Act & Assert
            assertDoesNotThrow(() -> {
                TestAnalysis analysis = new TestAnalysis(loopCFG);
                analysis.analyze();
            });
        }

        @Test
        @DisplayName("复杂CFG应该被正确处理")
        @Timeout(5)
        void testComplexCFG() {
            // Arrange - 创建大型CFG
            var complexNodes = new ArrayList<BasicBlock<IRNode>>();
            var edges = new ArrayList<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>>();

            for (int i = 0; i < 50; i++) {
                complexNodes.add(new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                ));
                if (i > 0) {
                    edges.add(org.apache.commons.lang3.tuple.Triple.of(i - 1, i, 1));
                }
            }

            CFG<IRNode> complexCFG = new CFG<>(complexNodes, edges);

            // Act & Assert
            assertDoesNotThrow(() -> {
                TestAnalysis analysis = new TestAnalysis(complexCFG);
                analysis.analyze();
            });
        }
    }

    /**
     * 简单的测试用数据流分析实现
     */
    private static class TestAnalysis extends AbstractDataFlowAnalysis<Set<Object>, IRNode> {
        public TestAnalysis(CFG<IRNode> cfg) {
            super(cfg);
        }

        @Override
        public Set<Object> meet(Set<Object> input1, Set<Object> input2) {
            Set<Object> result = new HashSet<>(input1);
            result.addAll(input2);
            return result;
        }

        @Override
        public Set<Object> transfer(IRNode instr, Set<Object> input) {
            return new HashSet<>(input);
        }

        @Override
        public Set<Object> getInitialValue() {
            return new HashSet<>();
        }

        @Override
        public boolean isForward() {
            return true;
        }
    }
}
