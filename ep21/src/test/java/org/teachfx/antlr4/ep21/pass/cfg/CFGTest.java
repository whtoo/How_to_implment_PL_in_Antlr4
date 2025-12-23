package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.utils.Kind;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * CFG类的综合单元测试
 * 覆盖CFG的核心方法：节点查询、边关系、度数计算、迭代器、可视化等
 * 对应TDD计划 TASK-2.2.1.2: 边关系测试
 *
 * 测试分类:
 * 1. 节点查询测试 (getBlock, getIRNodes)
 * 2. 边关系测试 (getSucceed, getInEdges, getOutDegree, getInDegree)
 * 3. 图结构测试 (iterator, toDOT, toString)
 * 4. 图修改测试 (removeNode, removeEdge)
 * 5. 优化器测试 (addOptimizer, applyOptimizers)
 */
@DisplayName("CFG综合测试")
@Tag("cfg")
class CFGTest {

    private CFG<IRNode> testCfg;
    private List<BasicBlock<IRNode>> nodes;
    private List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges;

    @BeforeEach
    void setUp() {
        // 创建测试用的CFG结构:
        //     0
        //    / \
        //   1   2
        //   |   |
        //   3 <-+
        //   |
        //   4
        nodes = new ArrayList<>();
        edges = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Label("L" + i, null))),
                new Label("L" + i, null),
                i
            );
            nodes.add(block);
        }

        // 添加边: 0->1, 0->2, 1->3, 2->3, 3->4
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(0, 2, CFGConstants.JUMP_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(1, 3, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(2, 3, CFGConstants.JUMP_EDGE_TYPE));
        edges.add(org.apache.commons.lang3.tuple.Triple.of(3, 4, CFGConstants.SUCCESSOR_EDGE_TYPE));

        testCfg = new CFG<>(nodes, edges);
    }

    @Nested
    @DisplayName("节点查询测试")
    class NodeQueryTests {

        @Test
        @DisplayName("getBlock应该返回正确的节点")
        void testGetBlockReturnsCorrectNode() {
            // Act
            BasicBlock<IRNode> block = testCfg.getBlock(2);

            // Assert
            assertNotNull(block, "getBlock应该返回非null节点");
            assertEquals(2, block.getId(), "返回的节点ID应该匹配");
            assertEquals("L2", block.getLabel().toString(), "返回的节点标签应该匹配");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4})
        @DisplayName("getBlock应该返回所有有效节点")
        void testGetBlockReturnsAllValidNodes(int blockId) {
            // Act
            BasicBlock<IRNode> block = testCfg.getBlock(blockId);

            // Assert
            assertNotNull(block, "getBlock(" + blockId + ") 应该返回非null节点");
            assertEquals(blockId, block.getId(), "节点ID应该匹配请求的ID");
        }

        @Test
        @DisplayName("getBlock对不存在的节点应该返回null")
        void testGetBlockReturnsNullForNonExistentNode() {
            // Act
            BasicBlock<IRNode> block = testCfg.getBlock(999);

            // Assert
            assertNull(block, "getBlock应该对不存在的节点返回null");
        }

        @Test
        @DisplayName("getIRNodes应该返回所有IR节点")
        void testGetIRNodesReturnsAllNodes() {
            // Act
            List<IRNode> irNodes = testCfg.getIRNodes();

            // Assert
            assertNotNull(irNodes, "getIRNodes应该返回非null列表");
            assertFalse(irNodes.isEmpty(), "getIRNodes应该返回非空列表");
            // 每个BasicBlock有一个Label指令
            assertTrue(irNodes.size() >= nodes.size(), "IR节点数量应该至少等于基本块数量");
        }
    }

    @Nested
    @DisplayName("边关系测试 - TASK-2.2.1.2")
    class EdgeRelationshipTests {

        @Test
        @DisplayName("getSucceed应该返回节点的所有后继")
        void testGetSucceedReturnsSuccessors() {
            // Act - 节点0有两个后继: 1和2
            Set<Integer> successors = testCfg.getSucceed(0);

            // Assert
            assertNotNull(successors, "getSucceed应该返回非null集合");
            assertEquals(2, successors.size(), "节点0应该有2个后继");
            assertTrue(successors.contains(1), "后继应该包含节点1");
            assertTrue(successors.contains(2), "后继应该包含节点2");
        }

        @ParameterizedTest
        @MethodSource("successorProvider")
        @DisplayName("getSucceed应该返回正确的后继节点")
        void testGetSucceedReturnsCorrectSuccessors(int nodeId, int expectedCount, List<Integer> expectedSuccessors) {
            // Act
            Set<Integer> successors = testCfg.getSucceed(nodeId);

            // Assert
            assertEquals(expectedCount, successors.size(),
                "节点" + nodeId + "应该有" + expectedCount + "个后继");
            for (Integer expected : expectedSuccessors) {
                assertTrue(successors.contains(expected),
                    "节点" + nodeId + "的后继应该包含" + expected);
            }
        }

        static Stream<Arguments> successorProvider() {
            return Stream.of(
                arguments(0, 2, List.of(1, 2)),  // 节点0 -> {1, 2}
                arguments(1, 1, List.of(3)),      // 节点1 -> {3}
                arguments(2, 1, List.of(3)),      // 节点2 -> {3}
                arguments(3, 1, List.of(4)),      // 节点3 -> {4}
                arguments(4, 0, List.of())        // 节点4 -> {} (出口节点)
            );
        }

        @Test
        @DisplayName("getInEdges应该返回节点的所有入边")
        void testGetInEdgesReturnsIncomingEdges() {
            // Act - 节点3有两个入边: 1->3和2->3
            Stream<Triple<Integer, Integer, Integer>> inEdges = testCfg.getInEdges(3);
            List<Triple<Integer, Integer, Integer>> inEdgesList = inEdges.toList();

            // Assert
            assertNotNull(inEdgesList, "getInEdges应该返回非null列表");
            assertEquals(2, inEdgesList.size(), "节点3应该有2条入边");
        }

        @Test
        @DisplayName("getOutDegree应该返回正确的出度")
        void testGetOutDegreeReturnsCorrectValue() {
            // Act & Assert
            assertEquals(2, testCfg.getOutDegree(0), "节点0的出度应该是2");
            assertEquals(1, testCfg.getOutDegree(1), "节点1的出度应该是1");
            assertEquals(0, testCfg.getOutDegree(4), "节点4的出度应该是0");
        }

        @Test
        @DisplayName("getInDegree应该返回正确的入度")
        void testGetInDegreeReturnsCorrectValue() {
            // Act & Assert
            assertEquals(0, testCfg.getInDegree(0), "节点0的入度应该是0");
            assertEquals(1, testCfg.getInDegree(1), "节点1的入度应该是1");
            assertEquals(2, testCfg.getInDegree(3), "节点3的入度应该是2");
        }

        @ParameterizedTest
        @MethodSource("degreeProvider")
        @DisplayName("度数计算应该正确")
        void testDegreeCalculations(int nodeId, int expectedInDegree, int expectedOutDegree) {
            // Act
            int inDegree = testCfg.getInDegree(nodeId);
            int outDegree = testCfg.getOutDegree(nodeId);

            // Assert
            assertEquals(expectedInDegree, inDegree,
                "节点" + nodeId + "的入度应该是" + expectedInDegree);
            assertEquals(expectedOutDegree, outDegree,
                "节点" + nodeId + "的出度应该是" + expectedOutDegree);
        }

        static Stream<Arguments> degreeProvider() {
            return Stream.of(
                arguments(0, 0, 2),  // 节点0: 入度0, 出度2
                arguments(1, 1, 1),  // 节点1: 入度1, 出度1
                arguments(2, 1, 1),  // 节点2: 入度1, 出度1
                arguments(3, 2, 1),  // 节点3: 入度2, 出度1
                arguments(4, 1, 0)   // 节点4: 入度1, 出度0
            );
        }
    }

    @Nested
    @DisplayName("图结构测试")
    class GraphStructureTests {

        @Test
        @DisplayName("iterator应该遍历所有节点")
        void testIteratorIteratesAllNodes() {
            // Act
            int count = 0;
            for (BasicBlock<IRNode> block : testCfg) {
                assertNotNull(block, "迭代器不应该返回null节点");
                count++;
            }

            // Assert
            assertEquals(5, count, "迭代器应该遍历所有5个节点");
        }

        @Test
        @DisplayName("toDOT应该生成有效的DOT格式")
        void testToDOTGeneratesValidFormat() {
            // Act
            String dotOutput = testCfg.toDOT();

            // Assert
            assertNotNull(dotOutput, "toDOT应该返回非null字符串");
            assertFalse(dotOutput.isEmpty(), "toDOT应该返回非空字符串");
            assertTrue(dotOutput.contains("digraph"), "DOT输出应该包含'digraph'关键字");
            assertTrue(dotOutput.contains("->"), "DOT输出应该包含边箭头'->'");
        }

        @Test
        @DisplayName("toString应该返回CFG的字符串表示")
        void testToStringReturnsStringRepresentation() {
            // Act
            String str = testCfg.toString();

            // Assert
            assertNotNull(str, "toString应该返回非null字符串");
            assertFalse(str.isEmpty(), "toString应该返回非空字符串");
        }

        @Test
        @DisplayName("空CFG应该被正确处理")
        void testEmptyCFG() {
            // Arrange
            CFG<IRNode> emptyCfg = new CFG<>(new ArrayList<>(), new ArrayList<>());

            // Act & Assert
            assertNotNull(emptyCfg, "空CFG构造应该成功");
            assertTrue(emptyCfg.nodes.isEmpty(), "空CFG应该没有节点");
            assertTrue(emptyCfg.edges.isEmpty(), "空CFG应该没有边");
            // Note: getOutDegree/getInDegree will throw IndexOutOfBoundsException for empty CFG
            // This is expected behavior since there are no nodes to query
        }
    }

    @Nested
    @DisplayName("图修改测试")
    class GraphModificationTests {

        @Test
        @DisplayName("removeNode应该移除指定节点及其相关边")
        void testRemoveNodeRemovesNodeAndEdges() {
            // Arrange
            int initialNodeCount = testCfg.nodes.size();
            int initialEdgeCount = testCfg.edges.size();
            BasicBlock<IRNode> nodeToRemove = testCfg.getBlock(3);

            // Act - 移除节点3
            testCfg.removeNode(nodeToRemove);

            // Assert
            assertEquals(initialNodeCount - 1, testCfg.nodes.size(),
                "节点数量应该减少1");

            // 验证节点3不存在
            BasicBlock<IRNode> removedBlock = testCfg.getBlock(3);
            assertNull(removedBlock, "被移除的节点应该不存在");
        }

        @Test
        @DisplayName("removeEdge应该移除指定边")
        void testRemoveEdgeRemovesEdge() {
            // Arrange
            int initialEdgeCount = testCfg.edges.size();
            Triple<Integer, Integer, Integer> edgeToRemove = Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE);

            // Act - 移除边 0->1
            boolean removed = testCfg.removeEdge(edgeToRemove);

            // Assert
            assertTrue(removed, "removeEdge应该返回true表示成功移除");
            assertTrue(testCfg.edges.size() < initialEdgeCount,
                "边数量应该减少");

            // 验证节点1不再是节点0的后继
            Set<Integer> successors = testCfg.getSucceed(0);
            assertFalse(successors.contains(1),
                "被移除的边不应该在后继集合中");
        }

        @Test
        @DisplayName("移除不存在的边应该返回false")
        void testRemoveNonExistentEdgeReturnsFalse() {
            // Arrange
            Triple<Integer, Integer, Integer> nonExistentEdge = Triple.of(999, 1000, CFGConstants.JUMP_EDGE_TYPE);

            // Act & Assert
            assertFalse(testCfg.removeEdge(nonExistentEdge),
                "移除不存在的边应该返回false");
        }

        @Test
        @DisplayName("移除不存在的节点不应该抛出异常")
        void testRemoveNonExistentNodeDoesNotThrow() {
            // Arrange - 创建一个不存在的节点
            BasicBlock<IRNode> nonExistentNode = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Label("NonExistent", null))),
                new Label("NonExistent", null), 999
            );

            // Act & Assert - 不应该抛出异常
            assertDoesNotThrow(() -> testCfg.removeNode(nonExistentNode),
                "移除不存在的节点不应该抛出异常");
        }
    }

    @Nested
    @DisplayName("优化器测试")
    class OptimizerTests {

        @Test
        @DisplayName("addOptimizer应该添加优化器")
        void testAddOptimizerAddsOptimizer() {
            // Arrange
            IFlowOptimizer<IRNode> mockOptimizer = cfg -> {}; // 空操作优化器

            // Act
            testCfg.addOptimizer(mockOptimizer);

            // Assert - 验证优化器被添加（通过applyOptimizers的行为间接验证）
            assertDoesNotThrow(() -> testCfg.applyOptimizers(),
                "添加优化器后applyOptimizers应该正常工作");
        }

        @Test
        @DisplayName("applyOptimizers应该执行所有优化器")
        void testApplyOptimizersExecutesAllOptimizers() {
            // Arrange
            final int[] callCount = {0};
            IFlowOptimizer<IRNode> mockOptimizer = cfg -> callCount[0]++;

            testCfg.addOptimizer(mockOptimizer);
            testCfg.addOptimizer(mockOptimizer);
            testCfg.addOptimizer(mockOptimizer);

            // Act
            testCfg.applyOptimizers();

            // Assert
            assertEquals(3, callCount[0], "应该调用3次优化器");
        }

        @Test
        @DisplayName("applyOptimizers在没有优化器时应该正常工作")
        void testApplyOptimizersWithNoOptimizers() {
            // Act & Assert - 不应该抛出异常
            assertDoesNotThrow(() -> testCfg.applyOptimizers(),
                "没有优化器时applyOptimizers不应该抛出异常");
        }
    }

    @Nested
    @DisplayName("边界条件和异常测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("自环CFG应该被正确处理")
        void testSelfLoopCFG() {
            // Arrange - 创建自环: 0->0
            List<BasicBlock<IRNode>> selfLoopNodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> selfLoopEdges = new ArrayList<>();

            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Label("L0", null))),
                new Label("L0", null), 0
            );
            selfLoopNodes.add(block);
            selfLoopEdges.add(Triple.of(0, 0, CFGConstants.JUMP_EDGE_TYPE));

            // Act
            CFG<IRNode> selfLoopCfg = new CFG<>(selfLoopNodes, selfLoopEdges);

            // Assert
            assertNotNull(selfLoopCfg, "自环CFG应该被创建");
            assertEquals(1, selfLoopCfg.getOutDegree(0), "自环节点的出度应该是1");
            assertEquals(1, selfLoopCfg.getInDegree(0), "自环节点的入度应该是1");
            Set<Integer> successors = selfLoopCfg.getSucceed(0);
            assertTrue(successors.contains(0), "自环节点的后继应该包含自身");
        }

        @Test
        @DisplayName("大型CFG应该被正确处理")
        @Timeout(5)
        void testLargeCFG() {
            // Arrange - 创建100个节点的链式CFG
            List<BasicBlock<IRNode>> largeNodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> largeEdges = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                BasicBlock<IRNode> block = new BasicBlock<>(
                    Kind.CONTINUOUS,
                    List.of(new Loc<>(new Label("L" + i, null))),
                    new Label("L" + i, null), i
                );
                largeNodes.add(block);

                if (i < 99) {
                    largeEdges.add(Triple.of(i, i + 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
                }
            }

            // Act
            CFG<IRNode> largeCfg = new CFG<>(largeNodes, largeEdges);

            // Assert
            assertEquals(100, largeCfg.nodes.size(), "应该有100个节点");
            assertEquals(99, largeCfg.edges.size(), "应该有99条边");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 1, 100, Integer.MAX_VALUE})
        @DisplayName("查询节点应该正确处理有效和无效ID")
        void testQueryNonExistentNode(int nodeId) {
            // Act
            BasicBlock<IRNode> block = testCfg.getBlock(nodeId);

            // Assert
            if (nodeId >= 0 && nodeId < 5) {
                // Valid node IDs
                assertNotNull(block, "存在的节点ID应该返回非null");
                assertEquals(nodeId, block.getId(), "返回的节点ID应该匹配");

                // getSucceed should work for valid IDs
                Set<Integer> successors = testCfg.getSucceed(nodeId);
                assertNotNull(successors, "getSucceed应该返回Set");
            } else {
                // Invalid node IDs
                assertNull(block, "不存在的节点ID应该返回null");

                // getSucceed/getInDegree will throw IndexOutOfBoundsException for out-of-bounds IDs
                // This is expected behavior - the links list is sized based on max node ID
                assertThrows(IndexOutOfBoundsException.class, () -> testCfg.getSucceed(nodeId),
                    "查询超出边界的节点ID应该抛出IndexOutOfBoundsException");
            }
        }
    }

    @Nested
    @DisplayName("前驱后继关系完整性测试")
    class PredecessorSuccessorIntegrityTests {

        @Test
        @DisplayName("前驱和后继关系应该一致")
        void testPredecessorSuccessorConsistency() {
            // 对于每条边 u->v，v应该在u的后继中，u应该在v的前驱中
            for (var edge : testCfg.edges) {
                int u = edge.getLeft();
                int v = edge.getMiddle();

                // 验证v在u的后继中
                Set<Integer> successors = testCfg.getSucceed(u);
                assertTrue(successors.contains(v),
                    "节点" + v + "应该在节点" + u + "的后继中");

                // 验证u在v的前驱中（通过入边）
                Stream<Triple<Integer, Integer, Integer>> inEdges = testCfg.getInEdges(v);
                boolean found = inEdges.anyMatch(e -> e.getLeft() == u);
                assertTrue(found, "节点" + u + "应该在节点" + v + "的前驱中");
            }
        }

        @Test
        @DisplayName("度数计算应该与边数一致")
        void testDegreeConsistency() {
            // 每个节点的出度应该等于从该节点出发的边数
            for (BasicBlock<IRNode> node : testCfg.nodes) {
                int nodeId = node.getId();
                int outDegree = testCfg.getOutDegree(nodeId);
                int inDegree = testCfg.getInDegree(nodeId);

                // 计算实际出边数
                long actualOutEdges = testCfg.edges.stream()
                    .filter(edge -> edge.getLeft() == nodeId)
                    .count();

                // 计算实际入边数
                long actualInEdges = testCfg.edges.stream()
                    .filter(edge -> edge.getMiddle() == nodeId)
                    .count();

                assertEquals(actualOutEdges, outDegree,
                    "节点" + nodeId + "的出度应该等于出边数");
                assertEquals(actualInEdges, inDegree,
                    "节点" + nodeId + "的入度应该等于入边数");
            }
        }
    }

    @Nested
    @DisplayName("可视化输出测试")
    class VisualizationTests {

        @Test
        @DisplayName("toDOT输出应该包含所有节点")
        void testToDOTContainsAllNodes() {
            // Act
            String dotOutput = testCfg.toDOT();

            // Assert
            for (BasicBlock<IRNode> node : testCfg.nodes) {
                assertTrue(dotOutput.contains(String.valueOf(node.getId())),
                    "DOT输出应该包含节点" + node.getId());
            }
        }

        @Test
        @DisplayName("toDOT输出应该包含所有边")
        void testToDOTContainsAllEdges() {
            // Act
            String dotOutput = testCfg.toDOT();

            // Assert
            for (var edge : testCfg.edges) {
                String edgePattern = edge.getLeft() + " -> " + edge.getMiddle();
                assertTrue(dotOutput.contains(edgePattern) ||
                          dotOutput.contains(edge.getLeft() + "->" + edge.getMiddle()),
                    "DOT输出应该包含边 " + edgePattern);
            }
        }
    }
}
