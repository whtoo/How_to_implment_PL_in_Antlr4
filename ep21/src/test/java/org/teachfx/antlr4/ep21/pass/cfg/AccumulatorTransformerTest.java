package org.teachfx.antlr4.ep21.pass.cfg;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccumulatorTransformer单元测试
 * 覆盖累加器变换器的核心功能：累加器模式检测
 *
 * 测试分类:
 * 1. 创建和配置测试
 * 2. 累加器信息测试
 * 3. 单位值和操作符测试
 */
@DisplayName("AccumulatorTransformer测试")
@Tag("cfg")
class AccumulatorTransformerTest {

    private AccumulatorTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new AccumulatorTransformer();
    }

    private FrameSlot createFrameSlot(int index) {
        return new FrameSlot(index);
    }

    @Nested
    @DisplayName("创建和配置测试")
    class CreationTests {

        @Test
        @DisplayName("应该能够创建AccumulatorTransformer")
        void testCanCreateTransformer() {
            assertNotNull(transformer, "AccumulatorTransformer应该被创建");
        }

        @Test
        @DisplayName("初始统计信息应该为零")
        void testInitialStats() {
            assertEquals(0, transformer.getFunctionsTransformed(),
                "初始变换函数数应该为0");
            assertEquals(0, transformer.getAccumulatorsAdded(),
                "初始添加累加器数应该为0");
        }

        @Test
        @DisplayName("初始已变换函数集合应该为空")
        void testInitialTransformedFunctions() {
            assertTrue(transformer.getTransformedFunctions().isEmpty(),
                "初始已变换函数集合应该为空");
        }

        @Test
        @DisplayName("初始检测到的累加器模式应该为空")
        void testInitialAccumulatorPatterns() {
            assertTrue(transformer.getAccumulatorPatterns().isEmpty(),
                "初始检测到的累加器模式应该为空");
        }
    }

    @Nested
    @DisplayName("累加器信息测试")
    class AccumulatorInfoTests {

        @Test
        @DisplayName("应该能够创建累加器信息（乘法模式）")
        void testCreateAccumulatorInfoMultiplication() {
            FrameSlot accumulatorSlot = createFrameSlot(0);
            FrameSlot resultSlot = createFrameSlot(1);

            AccumulatorTransformer.AccumulatorInfo info = new AccumulatorTransformer.AccumulatorInfo(
                accumulatorSlot,
                resultSlot,
                OperatorType.BinaryOpType.MUL,
                accumulatorSlot
            );

            assertNotNull(info, "累加器信息应该被创建");
            assertEquals(accumulatorSlot, info.accumulatorSlot,
                "累加器槽位应该匹配");
            assertEquals(resultSlot, info.resultSlot,
                "结果槽位应该匹配");
            assertEquals(OperatorType.BinaryOpType.MUL, info.operator,
                "操作符应该是MUL");
        }

        @Test
        @DisplayName("应该能够创建累加器信息（加法模式）")
        void testCreateAccumulatorInfoAddition() {
            FrameSlot accumulatorSlot = createFrameSlot(0);
            FrameSlot resultSlot = createFrameSlot(1);

            AccumulatorTransformer.AccumulatorInfo info = new AccumulatorTransformer.AccumulatorInfo(
                accumulatorSlot,
                resultSlot,
                OperatorType.BinaryOpType.ADD,
                accumulatorSlot
            );

            assertNotNull(info, "累加器信息应该被创建");
            assertEquals(OperatorType.BinaryOpType.ADD, info.operator,
                "操作符应该是ADD");
        }
    }

    @Nested
    @DisplayName("单位值测试")
    class IdentityValueTests {

        @Test
        @DisplayName("乘法累加器的单位值应该是1")
        void testMultiplicationIdentityValue() {
            FrameSlot accumulatorSlot = createFrameSlot(0);
            FrameSlot resultSlot = createFrameSlot(1);

            AccumulatorTransformer.AccumulatorInfo info = new AccumulatorTransformer.AccumulatorInfo(
                accumulatorSlot,
                resultSlot,
                OperatorType.BinaryOpType.MUL,
                accumulatorSlot
            );

            assertEquals(1, info.getIdentityValue(),
                "乘法的单位值应该是1");
        }

        @Test
        @DisplayName("加法累加器的单位值应该是0")
        void testAdditionIdentityValue() {
            FrameSlot accumulatorSlot = createFrameSlot(0);
            FrameSlot resultSlot = createFrameSlot(1);

            AccumulatorTransformer.AccumulatorInfo info = new AccumulatorTransformer.AccumulatorInfo(
                accumulatorSlot,
                resultSlot,
                OperatorType.BinaryOpType.ADD,
                accumulatorSlot
            );

            assertEquals(0, info.getIdentityValue(),
                "加法的单位值应该是0");
        }

        @Test
        @DisplayName("减法累加器的单位值应该是0")
        void testSubtractionIdentityValue() {
            FrameSlot accumulatorSlot = createFrameSlot(0);
            FrameSlot resultSlot = createFrameSlot(1);

            AccumulatorTransformer.AccumulatorInfo info = new AccumulatorTransformer.AccumulatorInfo(
                accumulatorSlot,
                resultSlot,
                OperatorType.BinaryOpType.SUB,
                accumulatorSlot
            );

            assertEquals(0, info.getIdentityValue(),
                "减法的默认单位值应该是0");
        }
    }

    @Nested
    @DisplayName("组合操作测试")
    class CombineOperationTests {

        @Test
        @DisplayName("乘法累加器应该返回*操作符")
        void testMultiplicationCombineOperation() {
            FrameSlot accumulatorSlot = createFrameSlot(0);
            FrameSlot resultSlot = createFrameSlot(1);

            AccumulatorTransformer.AccumulatorInfo info = new AccumulatorTransformer.AccumulatorInfo(
                accumulatorSlot,
                resultSlot,
                OperatorType.BinaryOpType.MUL,
                accumulatorSlot
            );

            assertEquals("*", info.getCombineOperation(),
                "乘法的组合操作应该是*");
        }

        @Test
        @DisplayName("加法累加器应该返回+操作符")
        void testAdditionCombineOperation() {
            FrameSlot accumulatorSlot = createFrameSlot(0);
            FrameSlot resultSlot = createFrameSlot(1);

            AccumulatorTransformer.AccumulatorInfo info = new AccumulatorTransformer.AccumulatorInfo(
                accumulatorSlot,
                resultSlot,
                OperatorType.BinaryOpType.ADD,
                accumulatorSlot
            );

            assertEquals("+", info.getCombineOperation(),
                "加法的组合操作应该是+");
        }

        @Test
        @DisplayName("未知操作符应该返回?")
        void testUnknownCombineOperation() {
            FrameSlot accumulatorSlot = createFrameSlot(0);
            FrameSlot resultSlot = createFrameSlot(1);

            AccumulatorTransformer.AccumulatorInfo info = new AccumulatorTransformer.AccumulatorInfo(
                accumulatorSlot,
                resultSlot,
                OperatorType.BinaryOpType.SUB,
                accumulatorSlot
            );

            assertEquals("?", info.getCombineOperation(),
                "未知操作的组合操作应该是?");
        }
    }

    @Nested
    @DisplayName("空CFG处理测试")
    class EmptyCFGTests {

        @Test
        @DisplayName("空CFG应该正确处理")
        void testEmptyCFG() {
            CFG<IRNode> emptyCfg = new CFG<>(new ArrayList<>(), new ArrayList<>());

            assertDoesNotThrow(() -> transformer.onHandle(emptyCfg),
                "空CFG不应该抛出异常");
        }

        @Test
        @DisplayName("空CFG处理后统计信息应该为零")
        void testEmptyCFGStats() {
            CFG<IRNode> emptyCfg = new CFG<>(new ArrayList<>(), new ArrayList<>());
            transformer.onHandle(emptyCfg);

            assertEquals(0, transformer.getFunctionsTransformed(),
                "空CFG处理后变换函数数应该为0");
        }
    }

    @Nested
    @DisplayName("函数检测测试")
    class FunctionDetectionTests {

        @Test
        @DisplayName("isFunctionTransformed应该正确检测未变换函数")
        void testIsFunctionTransformedFalse() {
            assertFalse(transformer.isFunctionTransformed("unknown"),
                "未变换的函数应该返回false");
        }

        @Test
        @DisplayName("累加器模式检测应该返回空映射")
        void testAccumulatorPatternsEmpty() {
            CFG<IRNode> cfg = createSimpleCFG();
            transformer.onHandle(cfg);

            assertTrue(transformer.getAccumulatorPatterns().isEmpty(),
                "简单CFG不应该检测到累加器模式");
        }
    }

    @Nested
    @DisplayName("测试数据完整性")
    class DataIntegrityTests {

        @Test
        @DisplayName("返回不可修改的已变换函数集合")
        void testTransformedFunctionsImmutability() {
            assertThrows(UnsupportedOperationException.class,
                () -> transformer.getTransformedFunctions().add("test"),
                "返回的集合应该是不可修改的");
        }

        @Test
        @DisplayName("返回不可修改的累加器模式映射")
        void testAccumulatorPatternsImmutability() {
            assertThrows(UnsupportedOperationException.class,
                () -> transformer.getAccumulatorPatterns().put("test", null),
                "返回的映射应该是不可修改的");
        }
    }

    private CFG<IRNode> createSimpleCFG() {
        // 创建简单CFG: 0 -> 1
        List<BasicBlock<IRNode>> nodes = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            BasicBlock<IRNode> block = new BasicBlock<>(
                org.teachfx.antlr4.ep21.utils.Kind.CONTINUOUS,
                List.of(new Loc<>(new Label("L" + i, null))),
                new Label("L" + i, null),
                i
            );
            nodes.add(block);
        }

        return new CFG<>(nodes, new ArrayList<>());
    }
}
