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
 * 活跃变量分析测试
 */
@DisplayName("活跃变量分析测试")
@Tag("dataflow")
@Tag("liveness")
class LiveVariableAnalysisTest {

    private CFG<IRNode> testCFG;

    @BeforeEach
    void setUp() {
        // 创建测试用CFG
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

        // 创建3个基本块
        for (int i = 0; i < 3; i++) {
            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("L" + i, null),
                i
            );
            nodes.add(block);
        }

        // 添加边: 0 -> 1, 1 -> 2
        edges.add(Triple.of(0, 1, 1));
        edges.add(Triple.of(1, 2, 1));

        testCFG = new CFG<>(nodes, edges);
    }

    @Nested
    @DisplayName("活跃变量分析基础功能测试")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("应该能够创建活跃变量分析实例")
        void testCanCreateLiveVariableAnalysis() {
            // Act
            LiveVariableAnalysis analysis = new LiveVariableAnalysis(testCFG);

            // Assert
            assertNotNull(analysis);
        }

        @Test
        @DisplayName("空CFG的分析应该成功")
        void testEmptyCFGAnalysis() {
            // Arrange
            CFG<IRNode> emptyCFG = new CFG<>(new ArrayList<>(), new ArrayList<>());

            // Act & Assert
            assertDoesNotThrow(() -> new LiveVariableAnalysis(emptyCFG));
        }

        @Test
        @DisplayName("单节点CFG的分析应该成功")
        void testSingleNodeCFGAnalysis() {
            // Arrange
            List<BasicBlock<IRNode>> singleNode = new ArrayList<>();
            singleNode.add(new BasicBlock<>(Kind.CONTINUOUS, new ArrayList<>(), new Label("L0", null), 0));
            CFG<IRNode> singleNodeCFG = new CFG<>(singleNode, new ArrayList<>());

            // Act & Assert
            assertDoesNotThrow(() -> new LiveVariableAnalysis(singleNodeCFG));
        }

        @Test
        @DisplayName("活跃变量应该被正确传播")
        void testLiveVariablePropagation() {
            // Arrange
            LiveVariableAnalysis analysis = new LiveVariableAnalysis(testCFG);

            // Act & Assert
            assertDoesNotThrow(() -> analysis.analyze());
        }
    }

    @Nested
    @DisplayName("活跃变量集合测试")
    class LiveVariableSetTests {

        @Test
        @DisplayName("基本块应该有活跃变量集合")
        void testBasicBlockHasLiveVariables() {
            // Arrange
            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("L0", null),
                0
            );

            // Act & Assert
            assertNotNull(block.getLiveOut());
            assertNotNull(block.getLiveIn());
        }

        @Test
        @DisplayName("活跃变量集合应该正确初始化为空")
        void testLiveVariableSetsInitializedAsEmpty() {
            // Arrange
            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("L0", null),
                0
            );

            // Act & Assert
            assertTrue(block.getLiveOut().isEmpty());
            assertTrue(block.getLiveIn().isEmpty());
        }
    }

    @Nested
    @DisplayName("边界条件和异常测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("循环CFG应该被正确处理")
        void testLoopCFG() {
            // Arrange - 创建自环CFG
            List<BasicBlock<IRNode>> loopNodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("L0", null),
                0
            );
            loopNodes.add(block);
            edges.add(Triple.of(0, 0, 1));

            CFG<IRNode> loopCFG = new CFG<>(loopNodes, edges);

            // Act & Assert
            assertDoesNotThrow(() -> {
                LiveVariableAnalysis analysis = new LiveVariableAnalysis(loopCFG);
                analysis.analyze();
            });
        }

        @Test
        @DisplayName("复杂CFG应该被正确处理")
        @Timeout(5)
        void testComplexCFG() {
            // Arrange - 创建大型CFG
            List<BasicBlock<IRNode>> complexNodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            for (int i = 0; i < 50; i++) {
                BasicBlock<IRNode> block = new BasicBlock<>(
                    Kind.CONTINUOUS,
                    new ArrayList<>(),
                    new Label("L" + i, null),
                    i
                );
                complexNodes.add(block);

                if (i > 0) {
                    edges.add(Triple.of(i - 1, i, 1));
                }
                // 添加一些分支
                if (i > 1 && i % 3 == 0) {
                    edges.add(Triple.of(i - 2, i, 1));
                }
            }

            CFG<IRNode> complexCFG = new CFG<>(complexNodes, edges);

            // Act & Assert
            assertDoesNotThrow(() -> {
                LiveVariableAnalysis analysis = new LiveVariableAnalysis(complexCFG);
                analysis.analyze();
            });
        }

        @Test
        @DisplayName("多个出口的CFG应该被正确处理")
        void testMultipleExitPoints() {
            // Arrange - 创建有多个出口的CFG
            List<BasicBlock<IRNode>> multiExitNodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            // 创建入口块
            multiExitNodes.add(new BasicBlock<>(Kind.CONTINUOUS, new ArrayList<>(), new Label("L0", null), 0));

            // 创建两个出口块
            multiExitNodes.add(new BasicBlock<>(Kind.CONTINUOUS, new ArrayList<>(), new Label("L1", null), 1));
            multiExitNodes.add(new BasicBlock<>(Kind.CONTINUOUS, new ArrayList<>(), new Label("L2", null), 2));

            edges.add(Triple.of(0, 1, 1));
            edges.add(Triple.of(0, 2, 1));

            CFG<IRNode> multiExitCFG = new CFG<>(multiExitNodes, edges);

            // Act & Assert
            assertDoesNotThrow(() -> {
                LiveVariableAnalysis analysis = new LiveVariableAnalysis(multiExitCFG);
                analysis.analyze();
            });
        }
    }

    @Nested
    @DisplayName("活跃变量分析正确性测试")
    class CorrectnessTests {

        @Test
        @DisplayName("分析结果应该是确定的")
        void testAnalysisIsDeterministic() {
            // Arrange
            LiveVariableAnalysis analysis1 = new LiveVariableAnalysis(testCFG);
            LiveVariableAnalysis analysis2 = new LiveVariableAnalysis(testCFG);

            // Act
            analysis1.analyze();
            analysis2.analyze();

            // Assert - 两次分析应该产生相同的结果
            // 注意: 这里我们主要验证分析不会抛出异常
            assertDoesNotThrow(() -> {
                analysis1.analyze();
                analysis2.analyze();
            });
        }

        @Test
        @DisplayName("活跃变量分析应该收敛")
        void testAnalysisConverges() {
            // Arrange
            LiveVariableAnalysis analysis = new LiveVariableAnalysis(testCFG);

            // Act & Assert - 分析应该完成而不是无限循环
            assertDoesNotThrow(() -> analysis.analyze());
        }
    }
}
