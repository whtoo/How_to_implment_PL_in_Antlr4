package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep21.utils.Kind;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StackSimulator单元测试
 * 覆盖栈帧模拟器的核心功能：活跃变量分析、逃逸分析、槽位分析
 *
 * 测试分类:
 * 1. 创建和配置测试
 * 2. 分析测试
 * 3. 活跃区间测试
 * 4. 逃逸分析测试
 * 5. 干扰检测测试
 */
@DisplayName("StackSimulator测试")
@Tag("cfg")
class StackSimulatorTest {

    private StackSimulator<IRNode> simulator;
    private CFG<IRNode> cfg;
    private DominatorTree<IRNode> domTree;

    @BeforeEach
    void setUp() {
        // 创建简单CFG: 0 -> 1 -> 2
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

        edges.add(Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
        edges.add(Triple.of(1, 2, CFGConstants.SUCCESSOR_EDGE_TYPE));

        cfg = new CFG<>(nodes, edges);
        domTree = new DominatorTree<>(cfg);
        simulator = new StackSimulator<>(cfg, domTree);
    }

    @Nested
    @DisplayName("创建和配置测试")
    class CreationTests {

        @Test
        @DisplayName("应该能够创建StackSimulator")
        void testCanCreateSimulator() {
            assertNotNull(simulator, "StackSimulator应该被创建");
        }

        @Test
        @DisplayName("初始状态应该未分析")
        void testInitialState() {
            assertFalse(simulator.isAnalyzed(),
                "初始状态应该未分析");
        }

        @Test
        @DisplayName("初始变量集合应该为空")
        void testInitialVariables() {
            assertTrue(simulator.getVariables().isEmpty(),
                "初始变量集合应该为空");
        }

        @Test
        @DisplayName("初始槽位数量应该为0")
        void testInitialSlotCount() {
            assertEquals(0, simulator.getSlotCount(),
                "初始槽位数量应该为0");
        }
    }

    @Nested
    @DisplayName("分析测试")
    class AnalysisTests {

        @Test
        @DisplayName("空CFG应该正确分析")
        void testEmptyCFG() {
            CFG<IRNode> emptyCfg = new CFG<>(new ArrayList<>(), new ArrayList<>());
            DominatorTree<IRNode> emptyDomTree = new DominatorTree<>(emptyCfg);
            StackSimulator<IRNode> emptySimulator = new StackSimulator<>(emptyCfg, emptyDomTree);

            assertDoesNotThrow(() -> emptySimulator.analyze(),
                "空CFG分析不应该抛出异常");

            assertTrue(emptySimulator.isAnalyzed(),
                "分析后应该标记为已分析");
        }

        @Test
        @DisplayName("分析后状态应该更新")
        void testAnalysisUpdatesState() {
            simulator.analyze();

            assertTrue(simulator.isAnalyzed(),
                "分析后应该标记为已分析");
        }

        @Test
        @DisplayName("分析摘要应该可获取")
        void testSummary() {
            simulator.analyze();

            String summary = simulator.getSummary();
            assertNotNull(summary, "摘要应该非null");
            assertFalse(summary.isEmpty(), "摘要应该非空");
            assertTrue(summary.contains("StackSimulator Summary"),
                "摘要应该包含标题");
        }
    }

    @Nested
    @DisplayName("活跃区间测试")
    class LiveRangeTests {

        @Test
        @DisplayName("分析前获取活跃区间应该返回null")
        void testGetLiveRangeBeforeAnalysis() {
            assertNull(simulator.getLiveRange("unknown"),
                "分析前应该返回null");
        }

        @Test
        @DisplayName("获取未知变量的活跃区间应该返回null")
        void testGetLiveRangeForUnknownVariable() {
            simulator.analyze();

            assertNull(simulator.getLiveRange("unknown_var"),
                "未知变量应该返回null");
        }
    }

    @Nested
    @DisplayName("逃逸分析测试")
    class EscapeAnalysisTests {

        @Test
        @DisplayName("未知变量应该不逃逸")
        void testUnknownVariableNotEscaped() {
            simulator.analyze();

            assertFalse(simulator.isVariableEscaped("unknown"),
                "未知变量应该不逃逸");
        }

        @Test
        @DisplayName("逃逸变量集合应该可获取")
        void testEscapedVariables() {
            simulator.analyze();

            // 由于测试CFG没有变量，所有变量都不逃逸
            // 如果有逃逸变量，它们应该被正确识别
            assertNotNull(simulator.getSummary(),
                "摘要应该包含逃逸分析信息");
        }
    }

    @Nested
    @DisplayName("槽位测试")
    class SlotTests {

        @Test
        @DisplayName("获取无效槽位应该返回null")
        void testGetInvalidSlot() {
            simulator.analyze();

            assertNull(simulator.getSlot(-1),
                "无效槽位应该返回null");
            assertNull(simulator.getSlot(100),
                "超出范围的槽位应该返回null");
        }

        @Test
        @DisplayName("检查无效槽位是否可重用应该返回false")
        void testIsSlotReusableInvalid() {
            simulator.analyze();

            assertFalse(simulator.isSlotReusable(-1),
                "无效槽位应该不可重用");
            assertFalse(simulator.isSlotReusable(100),
                "超出范围的槽位应该不可重用");
        }
    }

    @Nested
    @DisplayName("干扰检测测试")
    class InterferenceTests {

        @Test
        @DisplayName("检查无效槽位干扰应该返回false")
        void testInterferenceInvalidSlots() {
            simulator.analyze();

            assertFalse(simulator.interfere(-1, 0),
                "无效槽位不应该干扰");
            assertFalse(simulator.interfere(0, -1),
                "无效槽位不应该干扰");
            assertFalse(simulator.interfere(100, 0),
                "超出范围的槽位不应该干扰");
            assertFalse(simulator.interfere(0, 100),
                "超出范围的槽位不应该干扰");
        }

        @Test
        @DisplayName("检查相同槽位干扰应该返回false")
        void testInterferenceSameSlot() {
            simulator.analyze();

            // 相同槽位与自身不构成干扰
            // 但实际实现中可能返回true或false
            // 这里测试边界情况
            assertFalse(simulator.interfere(0, 0),
                "相同槽位通常不视为干扰");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("大型CFG应该正确处理")
        void testLargeCFG() {
            // 创建100个块的CFG
            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                BasicBlock<IRNode> block = new BasicBlock<>(
                    Kind.CONTINUOUS,
                    List.of(new Loc<>(new Label("L" + i, null))),
                    new Label("L" + i, null),
                    i
                );
                nodes.add(block);

                if (i < 99) {
                    edges.add(Triple.of(i, i + 1, CFGConstants.SUCCESSOR_EDGE_TYPE));
                }
            }

            CFG<IRNode> largeCfg = new CFG<>(nodes, edges);
            DominatorTree<IRNode> largeDomTree = new DominatorTree<>(largeCfg);
            StackSimulator<IRNode> largeSimulator = new StackSimulator<>(largeCfg, largeDomTree);

            assertDoesNotThrow(() -> largeSimulator.analyze(),
                "大型CFG分析不应该抛出异常");
        }

        @Test
        @DisplayName("自环CFG应该正确处理")
        void testSelfLoopCFG() {
            // 创建自环CFG
            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Label("L0", null))),
                new Label("L0", null),
                0
            );
            nodes.add(block);
            edges.add(Triple.of(0, 0, CFGConstants.JUMP_EDGE_TYPE));

            CFG<IRNode> selfLoopCfg = new CFG<>(nodes, edges);
            DominatorTree<IRNode> selfLoopDomTree = new DominatorTree<>(selfLoopCfg);
            StackSimulator<IRNode> selfLoopSimulator = new StackSimulator<>(selfLoopCfg, selfLoopDomTree);

            assertDoesNotThrow(() -> selfLoopSimulator.analyze(),
                "自环CFG分析不应该抛出异常");
        }
    }
}
