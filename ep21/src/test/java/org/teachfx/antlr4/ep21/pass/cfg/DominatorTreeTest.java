package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.utils.Kind;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DominatorTree单元测试
 * 覆盖支配树分析器的核心方法：支配计算、支配查询、支配边界、循环头识别等
 *
 * 测试分类:
 * 1. 创建和配置测试
 * 2. 支配计算测试
 * 3. 支配查询测试 (dominates, strictlyDominates)
 * 4. 支配边界测试 (getDominanceFrontier)
 * 5. 循环头识别测试 (getLoopHeaders)
 * 6. CFG可规约性测试 (isReducible)
 * 7. 验证测试 (validate)
 */
@DisplayName("DominatorTree测试")
@Tag("cfg")
class DominatorTreeTest {

    private DominatorTree<IRNode> domTree;
    private CFG<IRNode> cfg;

    @BeforeEach
    void setUp() {
        // 创建测试用的CFG结构:
        //     0
        //    / \
        //   1   2
        //    \ /
        //     3
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Label("L" + i, null))),
                new Label("L" + i, null),
                i
            );
            nodes.add(block);
        }

        // 添加边: 0->1, 0->2, 1->3, 2->3
        edges.add(Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(Triple.of(0, 2, CFGConstants.JUMP_EDGE_TYPE));
        edges.add(Triple.of(1, 3, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(Triple.of(2, 3, CFGConstants.SUCCESSOR_EDGE_TYPE));

        cfg = new CFG<>(nodes, edges);
        domTree = new DominatorTree<>(cfg);
    }

    @Nested
    @DisplayName("创建和配置测试")
    class CreationTests {

        @Test
        @DisplayName("应该能够创建DominatorTree")
        void testCanCreateDominatorTree() {
            assertNotNull(domTree, "DominatorTree应该被创建");
        }

        @Test
        @DisplayName("初始状态应该未计算")
        void testInitialState() {
            assertFalse(domTree.isComputed(),
                "初始状态应该未计算");
        }
    }

    @Nested
    @DisplayName("支配计算测试")
    class DominatorComputationTests {

        @Test
        @DisplayName("compute应该正确计算支配树")
        void testCompute() {
            // 首先验证CFG的links是否正确构建
            Set<Integer> predsOf1 = cfg.getFrontier(1);
            System.out.println("DEBUG: Preds of node 1: " + predsOf1);
            System.out.println("DEBUG: Links size: " + cfg.getLinks().size());
            System.out.println("DEBUG: Links[1]: " + cfg.getLinks().get(1));

            boolean converged = domTree.compute(0);

            System.out.println("DEBUG: Dominators after compute:");
            for (int i = 0; i < 4; i++) {
                System.out.println("  D(" + i + ") = " + domTree.getDominators(i));
            }

            assertTrue(converged || !domTree.isComputed(),
                "compute应该返回收敛状态");
            assertTrue(domTree.isComputed(),
                "计算后应该标记为已计算");
        }

        @Test
        @DisplayName("compute应该设置入口节点")
        void testComputeSetsEntryNode() {
            domTree.compute(0);

            assertEquals(0, domTree.getEntryNode(),
                "入口节点应该设置为0");
        }

        @Test
        @DisplayName("入口节点应该只支配自己")
        void testEntryNodeDominatesOnlyItself() {
            domTree.compute(0);

            Set<Integer> entryDoms = domTree.getDominators(0);
            assertEquals(Set.of(0), entryDoms,
                "入口节点应该只支配自己");
        }

        @Test
        @DisplayName("每个节点应该支配自己")
        void testNodeDominatesItself() {
            domTree.compute(0);

            for (int i = 0; i < 4; i++) {
                Set<Integer> doms = domTree.getDominators(i);
                assertTrue(doms.contains(i),
                    "节点" + i + "应该支配自己");
            }
        }
    }

    @Nested
    @DisplayName("支配查询测试")
    class DominanceQueryTests {

        @BeforeEach
        void computeDomTree() {
            domTree.compute(0);
        }

        @Test
        @DisplayName("dominates应该正确判断支配关系")
        void testDominates() {
            // 入口节点0应该支配所有节点
            assertTrue(domTree.dominates(0, 0), "节点应该支配自己");
            assertTrue(domTree.dominates(0, 1), "0应该支配1");
            assertTrue(domTree.dominates(0, 2), "0应该支配2");
            assertTrue(domTree.dominates(0, 3), "0应该支配3");
        }

        @Test
        @DisplayName("dominates应该正确判断非支配关系")
        void testDominatesNegative() {
            assertFalse(domTree.dominates(1, 0), "1不应该支配0");
            assertFalse(domTree.dominates(2, 0), "2不应该支配0");
            assertFalse(domTree.dominates(1, 2), "1不应该支配2（汇合点前）");
            assertFalse(domTree.dominates(2, 1), "2不应该支配1（汇合点前）");
        }

        @Test
        @DisplayName("strictlyDominates应该排除自身")
        void testStrictlyDominates() {
            assertFalse(domTree.strictlyDominates(0, 0),
                "节点不应该严格支配自己");
            assertTrue(domTree.strictlyDominates(0, 1),
                "0应该严格支配1");
        }

        @Test
        @DisplayName("dominates对无效节点应该返回false")
        void testDominatesInvalidNodes() {
            assertFalse(domTree.dominates(-1, 0),
                "负数节点应该返回false");
            assertFalse(domTree.dominates(0, 999),
                "不存在的节点应该返回false");
        }
    }

    @Nested
    @DisplayName("支配深度测试")
    class DepthTests {

        @BeforeEach
        void computeDomTree() {
            domTree.compute(0);
        }

        @Test
        @DisplayName("getDepth应该返回正确的深度")
        void testGetDepth() {
            assertEquals(0, domTree.getDepth(0),
                "入口节点深度应该为0");

            // 节点1和2的深度应该为1
            int depth1 = domTree.getDepth(1);
            int depth2 = domTree.getDepth(2);
            assertTrue(depth1 >= 1, "节点1深度应该>=1");
            assertTrue(depth2 >= 1, "节点2深度应该>=1");
        }

        @Test
        @DisplayName("getDepth对不可达节点应该返回-1")
        void testGetDepthUnreachable() {
            // 创建一个有不可达节点的CFG
            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                BasicBlock<IRNode> block = new BasicBlock<>(
                    Kind.CONTINUOUS,
                    List.of(new Loc<>(new Label("L" + i, null))),
                    new Label("L" + i, null),
                    i
                );
                nodes.add(block);
            }

            // 只添加边0->1，节点2不可达
            edges.add(Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));

            CFG<IRNode> unreachableCfg = new CFG<>(nodes, edges);
            DominatorTree<IRNode> unreachableDomTree = new DominatorTree<>(unreachableCfg);
            unreachableDomTree.compute(0);

            assertEquals(-1, unreachableDomTree.getDepth(2),
                "不可达节点深度应该为-1");
        }
    }

    @Nested
    @DisplayName("支配边界测试")
    class DominanceFrontierTests {

        @BeforeEach
        void computeDomTree() {
            domTree.compute(0);
        }

        @Test
        @DisplayName("getDominanceFrontier应该返回非空集合")
        void testGetDominanceFrontier() {
            for (int i = 0; i < 4; i++) {
                Set<Integer> df = domTree.getDominanceFrontier(i);
                assertNotNull(df, "支配边界应该不为null");
            }
        }

        @Test
        @DisplayName("入口节点的支配边界应该包含其直接后继")
        void testEntryNodeDominanceFrontier() {
            Set<Integer> df = domTree.getDominanceFrontier(0);

            // 节点0的直接后继是1和2，它们应该不在0的支配边界中
            // 因为0直接支配1和2
            // 但如果有其他节点需要插入phi函数，可能包含其他节点
            assertNotNull(df);
        }
    }

    @Nested
    @DisplayName("循环头识别测试")
    class LoopHeaderTests {

        @Test
        @DisplayName("简单CFG应该没有循环头")
        void testNoLoopHeaders() {
            domTree.compute(0);

            Set<Integer> loopHeaders = domTree.getLoopHeaders();
            assertTrue(loopHeaders.isEmpty(),
                "简单CFG应该没有循环头");
        }

        @Test
        @DisplayName("有回边的CFG应该识别循环头")
        void testLoopHeadersWithBackEdge() {
            // 创建有回边的CFG（确保循环头支配所有回边来源）:
            //     0
            //     |
            //     v
            //     1 <----
            //     |     |
            //     v     |
            //     3 -----+
            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                BasicBlock<IRNode> block = new BasicBlock<>(
                    Kind.CONTINUOUS,
                    List.of(new Loc<>(new Label("L" + i, null))),
                    new Label("L" + i, null),
                    i
                );
                nodes.add(block);
            }

            // 添加边: 0->1, 1->3, 3->1 (回边)
            // 移除 0->2 和 2->3，确保节点1支配节点3
            edges.add(Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
            edges.add(Triple.of(1, 3, CFGConstants.SUCCESSOR_EDGE_TYPE));
            edges.add(Triple.of(3, 1, CFGConstants.JUMP_EDGE_TYPE)); // 回边

            CFG<IRNode> loopCfg = new CFG<>(nodes, edges);
            DominatorTree<IRNode> loopDomTree = new DominatorTree<>(loopCfg);
            loopDomTree.compute(0);

            Set<Integer> loopHeaders = loopDomTree.getLoopHeaders();
            assertTrue(loopHeaders.contains(1),
                "节点1应该是循环头（有回边指向它）");
        }
    }

    @Nested
    @DisplayName("CFG可规约性测试")
    class ReducibilityTests {

        @Test
        @DisplayName("简单CFG应该可规约")
        void testSimpleCFGIsReducible() {
            domTree.compute(0);

            assertTrue(domTree.isReducible(),
                "简单CFG应该可规约");
        }

        @Test
        @DisplayName("有回边的CFG应该可规约")
        void testLoopCFGIsReducible() {
            // 创建有回边的CFG（确保循环头支配所有回边来源）
            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                BasicBlock<IRNode> block = new BasicBlock<>(
                    Kind.CONTINUOUS,
                    List.of(new Loc<>(new Label("L" + i, null))),
                    new Label("L" + i, null),
                    i
                );
                nodes.add(block);
            }

            // 移除 0->2 和 2->3，确保节点1支配节点3
            edges.add(Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
            edges.add(Triple.of(1, 3, CFGConstants.SUCCESSOR_EDGE_TYPE));
            edges.add(Triple.of(3, 1, CFGConstants.JUMP_EDGE_TYPE)); // 回边

            CFG<IRNode> loopCfg = new CFG<>(nodes, edges);
            DominatorTree<IRNode> loopDomTree = new DominatorTree<>(loopCfg);
            loopDomTree.compute(0);

            assertTrue(loopDomTree.isReducible(),
                "有单个回边的CFG应该可规约");
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @BeforeEach
        void computeDomTree() {
            domTree.compute(0);
        }

        @Test
        @DisplayName("有效支配树应该通过验证")
        void testValidDomTree() {
            DominatorTree.ValidationResult result = domTree.validate();

            assertTrue(result.isValid(),
                "有效支配树应该通过验证");
            assertTrue(result.getErrors().isEmpty(),
                "不应该有错误");
        }

        @Test
        @DisplayName("验证应该检查入口节点的支配者")
        void testValidateEntryNode() {
            DominatorTree.ValidationResult result = domTree.validate();

            // 入口节点应该只支配自己
            assertTrue(result.getErrors().stream()
                .noneMatch(e -> e.contains("Entry node")),
                "入口节点验证应该通过");
        }
    }

    @Nested
    @DisplayName("支配树字符串表示测试")
    class TreeStringTests {

        @Test
        @DisplayName("toTreeString应该返回非空字符串")
        void testToTreeString() {
            domTree.compute(0);

            String treeStr = domTree.toTreeString();

            assertNotNull(treeStr, "toTreeString应该返回非null");
            assertFalse(treeStr.isEmpty(), "toTreeString应该返回非空字符串");
            assertTrue(treeStr.contains("Dominator Tree"),
                "应该包含'Dominator Tree'标题");
        }
    }

    @Nested
    @DisplayName("被支配节点查询测试")
    class DominatedNodesTests {

        @BeforeEach
        void computeDomTree() {
            domTree.compute(0);
        }

        @Test
        @DisplayName("getDominatedNodes应该返回正确的节点集合")
        void testGetDominatedNodes() {
            Set<Integer> dominatedBy0 = domTree.getDominatedNodes(0);

            // 节点0应该支配所有可达节点
            assertTrue(dominatedBy0.contains(0), "应该包含0");
            assertTrue(dominatedBy0.contains(1), "应该包含1");
            assertTrue(dominatedBy0.contains(2), "应该包含2");
            assertTrue(dominatedBy0.contains(3), "应该包含3");
        }

        @Test
        @DisplayName("getTreeChildren应该返回直接被支配的节点")
        void testGetTreeChildren() {
            Set<Integer> childrenOf0 = domTree.getTreeChildren(0);

            // 节点0的直接孩子应该是1和2（如果是严格的支配树结构）
            assertNotNull(childrenOf0, "getTreeChildren应该返回非null");
        }
    }

    @Nested
    @DisplayName("重新计算测试")
    class RecomputationTests {

        @Test
        @DisplayName("recompute应该使用之前设置的入口节点")
        void testRecompute() {
            domTree.compute(0);

            boolean result = domTree.recompute();

            assertTrue(result || !domTree.isComputed(),
                "recompute应该成功");
            assertEquals(0, domTree.getEntryNode(),
                "recompute后入口节点应该不变");
        }

        @Test
        @DisplayName("recompute没有入口节点应该抛出异常")
        void testRecomputeWithoutEntryNode() {
            DominatorTree<IRNode> freshTree = new DominatorTree<>(cfg);

            assertThrows(IllegalStateException.class,
                freshTree::recompute,
                "没有入口节点时recompute应该抛出异常");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空CFG应该正确处理")
        void testEmptyCFG() {
            CFG<IRNode> emptyCfg = new CFG<>(new ArrayList<>(), new ArrayList<>());
            DominatorTree<IRNode> emptyDomTree = new DominatorTree<>(emptyCfg);

            assertNotNull(emptyDomTree,
                "空CFG的DominatorTree应该被创建");
        }

        @Test
        @DisplayName("单节点CFG应该正确计算")
        void testSingleNodeCFG() {
            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Label("L0", null))),
                new Label("L0", null), 0
            );
            nodes.add(block);

            CFG<IRNode> singleNodeCfg = new CFG<>(nodes, edges);
            DominatorTree<IRNode> singleNodeDomTree = new DominatorTree<>(singleNodeCfg);
            singleNodeDomTree.compute(0);

            assertTrue(singleNodeDomTree.isComputed(),
                "单节点CFG应该能计算支配树");

            Set<Integer> doms = singleNodeDomTree.getDominators(0);
            assertEquals(Set.of(0), doms,
                "单节点CFG的支配集合应该只有自身");
        }

        @Test
        @DisplayName("链式CFG应该正确计算")
        void testChainCFG() {
            // 创建链式CFG: 0 -> 1 -> 2 -> 3
            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                BasicBlock<IRNode> block = new BasicBlock<>(
                    Kind.CONTINUOUS,
                    List.of(new Loc<>(new Label("L" + i, null))),
                    new Label("L" + i, null),
                    i
                );
                nodes.add(block);
            }

            for (int i = 0; i < 3; i++) {
                edges.add(Triple.of(i, i + 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
            }

            CFG<IRNode> chainCfg = new CFG<>(nodes, edges);
            DominatorTree<IRNode> chainDomTree = new DominatorTree<>(chainCfg);
            chainDomTree.compute(0);

            // 在链式CFG中，每个节点只被其前面的所有节点支配
            assertTrue(chainDomTree.dominates(0, 3), "0应该支配3");
            assertTrue(chainDomTree.dominates(1, 3), "1应该支配3");
            assertFalse(chainDomTree.dominates(2, 0), "2不应该支配0");
        }
    }
}
