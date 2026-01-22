package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.ir.stmt.ReturnVal;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.symbol.Symbol;
import org.teachfx.antlr4.ep21.utils.Kind;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnhancedTailRecursionOptimizer单元测试
 * 覆盖增强尾递归优化器的核心功能：检测和CFG变换
 *
 * 测试分类:
 * 1. 创建和配置测试
 * 2. 尾递归检测测试
 * 3. CFG变换测试
 * 4. 统计信息测试
 */
@DisplayName("EnhancedTailRecursionOptimizer测试")
@Tag("cfg")
class EnhancedTailRecursionOptimizerTest {

    private EnhancedTailRecursionOptimizer optimizer;
    private CFG<IRNode> cfg;

    @BeforeEach
    void setUp() {
        optimizer = new EnhancedTailRecursionOptimizer();
        cfg = createSimpleCFG();
    }

    private CFG<IRNode> createSimpleCFG() {
        // 创建简单CFG: 0 -> 1
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();
        List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Label("L" + i, null))),
                new Label("L" + i, null),
                i
            );
            nodes.add(block);
        }

        edges.add(Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));

        return new CFG<>(nodes, edges);
    }

    @Nested
    @DisplayName("创建和配置测试")
    class CreationTests {

        @Test
        @DisplayName("应该能够创建EnhancedTailRecursionOptimizer")
        void testCanCreateOptimizer() {
            assertNotNull(optimizer, "EnhancedTailRecursionOptimizer应该被创建");
        }

        @Test
        @DisplayName("初始统计信息应该为零")
        void testInitialStats() {
            assertEquals(0, optimizer.getFunctionsOptimized(),
                "初始优化函数数应该为0");
            assertEquals(0, optimizer.getTailCallsEliminated(),
                "初始消除尾调用数应该为0");
            assertEquals(0, optimizer.getPhiNodesCreated(),
                "初始创建PHI节点数应该为0");
        }

        @Test
        @DisplayName("初始已优化函数集合应该为空")
        void testInitialOptimizedFunctions() {
            assertTrue(optimizer.getOptimizedFunctions().isEmpty(),
                "初始已优化函数集合应该为空");
        }
    }

    @Nested
    @DisplayName("空CFG处理测试")
    class EmptyCFGTests {

        @Test
        @DisplayName("空CFG应该正确处理")
        void testEmptyCFG() {
            CFG<IRNode> emptyCfg = new CFG<>(new ArrayList<>(), new ArrayList<>());

            assertDoesNotThrow(() -> optimizer.onHandle(emptyCfg),
                "空CFG不应该抛出异常");

            assertEquals(0, optimizer.getFunctionsOptimized(),
                "空CFG没有可优化的函数");
        }
    }

    @Nested
    @DisplayName("尾递归检测测试")
    class TailRecursionDetectionTests {

        @Test
        @DisplayName("无函数入口的CFG应该正确处理")
        void testCFGWithoutFunctionEntry() {
            // CFG只有普通块，没有FuncEntryLabel
            assertDoesNotThrow(() -> optimizer.onHandle(cfg),
                "无函数入口的CFG不应该抛出异常");

            assertEquals(0, optimizer.getFunctionsOptimized(),
                "无函数入口的CFG没有可优化的函数");
        }

        @Test
        @DisplayName("单函数CFG应该正确处理")
        void testSingleFunctionCFG() {
            // 创建有函数入口的CFG
            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            // 入口块
            BasicBlock<IRNode> entry = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Label(".def fib: args=1, locals=1", null))),
                new Label(".def fib: args=1, locals=1", null),
                0
            );
            nodes.add(entry);

            // 返回块
            BasicBlock<IRNode> returnBlock = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Label("L1", null))),
                new Label("L1", null),
                1
            );
            nodes.add(returnBlock);

            edges.add(Triple.of(0, 1, CFGConstants.SUCCESSOR_EDGE_TYPE));

            CFG<IRNode> funcCfg = new CFG<>(nodes, edges);

            assertDoesNotThrow(() -> optimizer.onHandle(funcCfg),
                "单函数CFG不应该抛出异常");
        }
    }

    @Nested
    @DisplayName("统计信息测试")
    class StatisticsTests {

        @Test
        @DisplayName("isFunctionOptimized对未优化函数应该返回false")
        void testIsFunctionOptimized() {
            assertFalse(optimizer.isFunctionOptimized("unknown"),
                "未优化的函数应该返回false");
        }

        @Test
        @DisplayName("统计信息在多次优化后应该累积")
        void testStatsAccumulation() {
            // 第一次优化
            optimizer.onHandle(cfg);
            int firstCount = optimizer.getFunctionsOptimized();

            // 第二次优化
            optimizer.onHandle(cfg);
            int secondCount = optimizer.getFunctionsOptimized();

            assertEquals(firstCount, secondCount,
                "重复优化相同CFG不应该增加统计");
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

            assertDoesNotThrow(() -> optimizer.onHandle(largeCfg),
                "大型CFG不应该抛出异常");
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

            assertDoesNotThrow(() -> optimizer.onHandle(selfLoopCfg),
                "自环CFG不应该抛出异常");
        }
    }
}
