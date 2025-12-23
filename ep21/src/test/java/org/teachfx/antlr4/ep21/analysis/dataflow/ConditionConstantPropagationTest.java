package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;
import org.teachfx.antlr4.ep21.utils.Kind;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 条件常量传播分析测试套件
 *
 * 测试内容：
 * - 基本常量传播
 * - 变量到变量的常量传播
 * - 条件分支的常量识别
 * - 交汇操作（不同路径收敛）
 * - 边界条件
 *
 * @author EP21 Team
 */
@DisplayName("条件常量传播分析测试")
@Tag("dataflow")
@Tag("condition-constant-propagation")
class ConditionConstantPropagationTest {

    private ConditionConstantPropagation analysis;

    @BeforeEach
    void setUp() {
        // 每次测试前创建新的分析器
        analysis = null;
    }

    private void createAnalysis(CFG<IRNode> cfg) {
        analysis = new ConditionConstantPropagation(cfg);
        analysis.analyze();
    }

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("应该能够创建分析器")
        void testCanCreateAnalysis() {
            CFG<IRNode> cfg = new CFG<>(new ArrayList<>(), new ArrayList<>());
            analysis = new ConditionConstantPropagation(cfg);
            assertNotNull(analysis);
        }

        @Test
        @DisplayName("空CFG应该正确处理")
        void testEmptyCFG() {
            CFG<IRNode> cfg = new CFG<>(new ArrayList<>(), new ArrayList<>());
            createAnalysis(cfg);
            assertNotNull(analysis);
        }
    }

    @Nested
    @DisplayName("基本常量传播测试")
    class BasicConstantPropagationTests {

        @Test
        @DisplayName("应该能够识别常量赋值")
        void testConstantAssignment() {
            // L0: a = 10
            // L1: b = 20

            OperandSlot a = OperandSlot.genTemp();
            OperandSlot b = OperandSlot.genTemp();

            Assign assignA = new Assign(a, ConstVal.valueOf(10));
            Assign assignB = new Assign(b, ConstVal.valueOf(20));

            List<Loc<IRNode>> codes = Arrays.asList(
                new Loc<>(assignA),
                new Loc<>(assignB)
            );

            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS, codes, new Label("test", null), 0
            );

            CFG<IRNode> cfg = new CFG<>(List.of(block), new ArrayList<>());
            createAnalysis(cfg);

            // 检查变量a是否是常量（在基本块0的out状态）
            assertTrue(analysis.isConstantOut(a, 0));
            assertEquals(10, analysis.getConstantValueOut(a, 0).getVal());

            // 检查变量b是否是常量
            assertTrue(analysis.isConstantOut(b, 0));
            assertEquals(20, analysis.getConstantValueOut(b, 0).getVal());
        }

        @Test
        @DisplayName("应该能够传播变量到变量的常量")
        void testVariableToVariablePropagation() {
            // a = 10
            // b = a

            OperandSlot a = OperandSlot.genTemp();
            OperandSlot b = OperandSlot.genTemp();

            Assign assignA = new Assign(a, ConstVal.valueOf(10));
            Assign assignB = new Assign(b, a);

            List<Loc<IRNode>> codes = Arrays.asList(
                new Loc<>(assignA),
                new Loc<>(assignB)
            );

            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS, codes, new Label("test", null), 0
            );

            CFG<IRNode> cfg = new CFG<>(List.of(block), new ArrayList<>());
            createAnalysis(cfg);

            // a应该是常量（在out状态）
            assertTrue(analysis.isConstantOut(a, 0));
            assertEquals(10, analysis.getConstantValueOut(a, 0).getVal());

            // b应该是常量（通过a传播，在out状态）
            assertTrue(analysis.isConstantOut(b, 0));
            assertEquals(10, analysis.getConstantValueOut(b, 0).getVal());
        }

        @Test
        @DisplayName("应该能够处理多块CFG中的常量传播")
        void testMultiBlockConstantPropagation() {
            // L0: a = 10
            // L1: b = a
            // L2: c = b

            OperandSlot a = OperandSlot.genTemp();
            OperandSlot b = OperandSlot.genTemp();
            OperandSlot c = OperandSlot.genTemp();

            Assign assignA = new Assign(a, ConstVal.valueOf(10));
            Assign assignB = new Assign(b, a);
            Assign assignC = new Assign(c, b);

            BasicBlock<IRNode> block0 = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(assignA)),
                new Label("L0", null), 0
            );

            BasicBlock<IRNode> block1 = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(assignB)),
                new Label("L1", null), 1
            );

            BasicBlock<IRNode> block2 = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(assignC)),
                new Label("L2", null), 2
            );

            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                Triple.of(0, 1, 1),
                Triple.of(1, 2, 1)
            );

            CFG<IRNode> cfg = new CFG<>(Arrays.asList(block0, block1, block2), edges);
            createAnalysis(cfg);

            // 所有变量都应该是常量：
            // - a在block0的out中有值（赋值在block0中）
            // - b在block1的out中有值（赋值在block1中，a的值从block0传播来）
            // - c在block2的out中有值（赋值在block2中，b的值从block1传播来）
            assertTrue(analysis.isConstantOut(a, 0));
            assertTrue(analysis.isConstantOut(b, 1));  // b在block1中被赋值，值来自a
            assertTrue(analysis.isConstantOut(c, 2));  // c在block2中被赋值，值来自b
        }
    }

    @Nested
    @DisplayName("条件分支测试")
    class ConditionalBranchTests {

        @Test
        @DisplayName("应该能够识别常量条件分支")
        void testConstantConditionalBranch() {
            // L0: flag = true
            // L1: if flag -> L2 else L3

            OperandSlot flag = OperandSlot.genTemp();
            Assign flagAssign = new Assign(flag, ConstVal.valueOf(true));

            BasicBlock<IRNode> block0 = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(flagAssign)),
                new Label("L0", null), 0
            );

            BasicBlock<IRNode> block1 = new BasicBlock<>(  // if block
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("L1", null), 1
            );

            BasicBlock<IRNode> block2 = new BasicBlock<>(  // then block
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("L2", null), 2
            );

            BasicBlock<IRNode> block3 = new BasicBlock<>(  // else block
                Kind.CONTINUOUS,
                new ArrayList<>(),
                new Label("L3", null), 3
            );

            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                Triple.of(0, 1, 1),
                Triple.of(1, 2, 1),  // true分支
                Triple.of(1, 3, 1)   // false分支
            );

            CFG<IRNode> cfg = new CFG<>(Arrays.asList(block0, block1, block2, block3), edges);
            createAnalysis(cfg);

            // flag应该是常量true
            assertTrue(analysis.isConstant(flag, 1));
            assertEquals(true, analysis.getConstantValue(flag, 1).getVal());
        }
    }

    @Nested
    @DisplayName("交汇操作测试")
    class MeetOperationTests {

        @Test
        @DisplayName("应该能够合并不同路径的常量")
        void testMergeConstants() {
            // L0: entry
            // L1: a = 10
            // L2: b = 20
            // L3: merge

            OperandSlot a = OperandSlot.genTemp();
            OperandSlot b = OperandSlot.genTemp();

            BasicBlock<IRNode> block0 = new BasicBlock<>(
                Kind.CONTINUOUS, new ArrayList<>(), new Label("L0", null), 0
            );

            BasicBlock<IRNode> block1 = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Assign(a, ConstVal.valueOf(10)))),
                new Label("L1", null), 1
            );

            BasicBlock<IRNode> block2 = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Assign(b, ConstVal.valueOf(20)))),
                new Label("L2", null), 2
            );

            BasicBlock<IRNode> block3 = new BasicBlock<>(
                Kind.CONTINUOUS, new ArrayList<>(), new Label("L3", null), 3
            );

            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                Triple.of(0, 1, 1),
                Triple.of(0, 2, 1),
                Triple.of(1, 3, 1),
                Triple.of(2, 3, 1)
            );

            CFG<IRNode> cfg = new CFG<>(Arrays.asList(block0, block1, block2, block3), edges);
            createAnalysis(cfg);

            // 在合并块中，a和b都应该是常量
            assertTrue(analysis.isConstant(a, 3));
            assertEquals(10, analysis.getConstantValue(a, 3).getVal());

            assertTrue(analysis.isConstant(b, 3));
            assertEquals(20, analysis.getConstantValue(b, 3).getVal());
        }

        @Test
        @DisplayName("应该能够检测冲突的常量赋值（UNKNOWN）")
        void testConflictingConstants() {
            // L0: entry
            // L1: a = 10
            // L2: a = 20
            // L3: merge (a从两个路径来)

            OperandSlot a = OperandSlot.genTemp();

            BasicBlock<IRNode> block0 = new BasicBlock<>(
                Kind.CONTINUOUS, new ArrayList<>(), new Label("L0", null), 0
            );

            BasicBlock<IRNode> block1 = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Assign(a, ConstVal.valueOf(10)))),
                new Label("L1", null), 1
            );

            BasicBlock<IRNode> block2 = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Assign(a, ConstVal.valueOf(20)))),
                new Label("L2", null), 2
            );

            BasicBlock<IRNode> block3 = new BasicBlock<>(
                Kind.CONTINUOUS, new ArrayList<>(), new Label("L3", null), 3
            );

            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                Triple.of(0, 1, 1),
                Triple.of(0, 2, 1),
                Triple.of(1, 3, 1),
                Triple.of(2, 3, 1)
            );

            CFG<IRNode> cfg = new CFG<>(Arrays.asList(block0, block1, block2, block3), edges);
            createAnalysis(cfg);

            // 在合并块中，a应该是UNKNOWN（因为从两个路径来，值不同）
            assertTrue(analysis.isUnknown(a, 3));
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("未赋值的变量应该不是常量")
        void testUnassignedVariable() {
            OperandSlot x = OperandSlot.genTemp();

            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS, new ArrayList<>(), new Label("L0", null), 0
            );

            CFG<IRNode> cfg = new CFG<>(List.of(block), new ArrayList<>());
            createAnalysis(cfg);

            // 未赋值的变量不是常量
            assertFalse(analysis.isConstant(x, 0));
            assertNull(analysis.getConstantValue(x, 0));
        }

        @Test
        @DisplayName("应该支持多次分析调用")
        void testMultipleAnalysisCalls() {
            OperandSlot a = OperandSlot.genTemp();

            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                List.of(new Loc<>(new Assign(a, ConstVal.valueOf(42)))),
                new Label("L0", null), 0
            );

            CFG<IRNode> cfg = new CFG<>(List.of(block), new ArrayList<>());

            assertDoesNotThrow(() -> {
                analysis = new ConditionConstantPropagation(cfg);
                analysis.analyze();
                analysis.analyze();
            });
        }

        @Test
        @DisplayName("应该能够处理不同类型的常量")
        void testDifferentConstantTypes() {
            // 测试整数、布尔、字符串常量

            OperandSlot intVar = OperandSlot.genTemp();
            OperandSlot boolVar = OperandSlot.genTemp();
            OperandSlot strVar = OperandSlot.genTemp();

            Assign intAssign = new Assign(intVar, ConstVal.valueOf(100));
            Assign boolAssign = new Assign(boolVar, ConstVal.valueOf(false));
            Assign strAssign = new Assign(strVar, ConstVal.valueOf("hello"));

            BasicBlock<IRNode> block = new BasicBlock<>(
                Kind.CONTINUOUS,
                Arrays.asList(
                    new Loc<>(intAssign),
                    new Loc<>(boolAssign),
                    new Loc<>(strAssign)
                ),
                new Label("L0", null), 0
            );

            CFG<IRNode> cfg = new CFG<>(List.of(block), new ArrayList<>());
            createAnalysis(cfg);

            // 使用out状态检查常量
            assertTrue(analysis.isConstantOut(intVar, 0));
            assertEquals(100, analysis.getConstantValueOut(intVar, 0).getVal());

            assertTrue(analysis.isConstantOut(boolVar, 0));
            assertEquals(false, analysis.getConstantValueOut(boolVar, 0).getVal());

            assertTrue(analysis.isConstantOut(strVar, 0));
            assertEquals("hello", analysis.getConstantValueOut(strVar, 0).getVal());
        }

        @Test
        @DisplayName("应该能够处理空基本块")
        void testEmptyBlocks() {
            BasicBlock<IRNode> block0 = new BasicBlock<>(
                Kind.CONTINUOUS, new ArrayList<>(), new Label("L0", null), 0
            );

            BasicBlock<IRNode> block1 = new BasicBlock<>(
                Kind.CONTINUOUS, new ArrayList<>(), new Label("L1", null), 1
            );

            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                Triple.of(0, 1, 1)
            );

            CFG<IRNode> cfg = new CFG<>(Arrays.asList(block0, block1), edges);
            createAnalysis(cfg);

            // 应该能正常处理空基本块
            assertNotNull(analysis);
        }
    }

    @Nested
    @DisplayName("LatticeValue测试")
    class LatticeValueTests {

        @Test
        @DisplayName("UNDEF应该正确工作")
        void testUNDEF() {
            ConditionConstantPropagation.UNDEF undef = new ConditionConstantPropagation.UNDEF();

            assertFalse(undef.isConstant());
            assertNull(undef.getConstant());
            assertEquals("UNDEF", undef.toString());

            // 两个UNDEF应该相等
            ConditionConstantPropagation.UNDEF undef2 = new ConditionConstantPropagation.UNDEF();
            assertEquals(undef, undef2);
            assertEquals(undef.hashCode(), undef2.hashCode());
        }

        @Test
        @DisplayName("KNOWN_CONSTANT应该正确工作")
        void testKnownConstant() {
            ConstVal<Integer> constVal = ConstVal.valueOf(42);
            ConditionConstantPropagation.KnownConstant known =
                new ConditionConstantPropagation.KnownConstant(constVal);

            assertTrue(known.isConstant());
            assertEquals(constVal, known.getConstant());
            assertEquals("Const(42)", known.toString());
        }

        @Test
        @DisplayName("UNKNOWN应该正确工作")
        void testUNKNOWN() {
            ConditionConstantPropagation.UNKNOWN unknown = new ConditionConstantPropagation.UNKNOWN();

            assertFalse(unknown.isConstant());
            assertNull(unknown.getConstant());
            assertEquals("UNKNOWN", unknown.toString());

            // 两个UNKNOWN应该相等
            ConditionConstantPropagation.UNKNOWN unknown2 = new ConditionConstantPropagation.UNKNOWN();
            assertEquals(unknown, unknown2);
            assertEquals(unknown.hashCode(), unknown2.hashCode());
        }

        @Test
        @DisplayName("UNDEF和UNKNOWN应该不相等")
        void testUNDEFAndUNKNOWNNotEqual() {
            ConditionConstantPropagation.UNDEF undef = new ConditionConstantPropagation.UNDEF();
            ConditionConstantPropagation.UNKNOWN unknown = new ConditionConstantPropagation.UNKNOWN();

            assertNotEquals(undef, unknown);
        }
    }
}
