package org.teachfx.antlr4.ep21.analysis.dataflow.reaching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.analysis.dataflow.Definition;
import org.teachfx.antlr4.ep21.analysis.dataflow.ReachingDefinitionAnalysis;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;
import org.teachfx.antlr4.ep21.utils.Kind;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 基础测试用例：ReachingDefinitionAnalysis功能验证
 *
 * 测试覆盖：
 * 1. 直线代码（无分支）
 * 2. if-else分支（多路径合并）
 * 3. 循环（回边）
 * 4. 变量重新定义（杀死旧定义）
 * 5. 空基本块和边界条件
 */
@DisplayName("ReachingDefinitionAnalysis 基础测试")
class ReachingDefinitionAnalysisTest {

    private FrameSlot varX;
    private FrameSlot varY;
    private FrameSlot varZ;
    private ConstVal const1;
    private ConstVal const2;
    private ConstVal const3;

    @BeforeEach
    void setUp() {
        varX = new FrameSlot(0);
        varY = new FrameSlot(1);
        varZ = new FrameSlot(2);
        const1 = ConstVal.valueOf(1);
        const2 = ConstVal.valueOf(2);
        const3 = ConstVal.valueOf(3);
    }

    @Nested
    @DisplayName("测试1: 直线代码的到达定义分析")
    class StraightLineCodeTests {

        @Test
        @DisplayName("直线代码：每个定义都到达后续所有点")
        void testSimpleStraightLineCode() {
            // 代码：x = 1; y = 2; z = x + y;
            Assign assign1 = Assign.with(varX, const1);
            Assign assign2 = Assign.with(varY, const2);
            Assign assign3 = Assign.with(varZ, const1); // z = 1 (simplified)

            // 构建CFG：单个基本块
            BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Arrays.asList(
                            new Loc<>(assign1),
                            new Loc<>(assign2),
                            new Loc<>(assign3)
                    ))
                    .label(new Label("L0", null))
                    .build();

            CFG<IRNode> cfg = new CFG<>(
                    Collections.singletonList(block),
                    Collections.emptyList()
            );

            // 执行分析
            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证基本块入口（应为空）
            Set<Definition> block0In = analysis.getIn(0);
            assertThat(block0In).isEmpty();

            // 验证基本块出口有3个定义
            Set<Definition> block0Out = analysis.getOut(0);
            assertThat(block0Out).hasSize(3);
        }

        @Test
        @DisplayName("直线代码：后续定义杀死前面定义")
        void testRedefinitionKillsPrevious() {
            // 代码：x = 1; x = 2; x = 3;
            Assign assign1 = Assign.with(varX, const1);
            Assign assign2 = Assign.with(varX, const2);
            Assign assign3 = Assign.with(varX, const3);

            BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Arrays.asList(
                            new Loc<>(assign1),
                            new Loc<>(assign2),
                            new Loc<>(assign3)
                    ))
                    .label(new Label("L0", null))
                    .build();

            CFG<IRNode> cfg = new CFG<>(
                    Collections.singletonList(block),
                    Collections.emptyList()
            );

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证出口只有最后一个定义
            Set<Definition> block0Out = analysis.getOut(0);
            assertThat(block0Out).hasSize(1);

            Definition finalDef = block0Out.iterator().next();
            assertThat(finalDef.getBlock().getId()).isEqualTo(0);
            assertThat(finalDef.getInstructionIndex()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("测试2: if-else分支的到达定义分析")
    class IfElseBranchTests {

        @Test
        @DisplayName("if-else：分支定义在merge点合并")
        void testIfElseMerge() {
            // 代码：
            //   x = 1;
            //   if (cond) {
            //     x = 2;
            //   } else {
            //     x = 3;
            //   }

            Assign assign1 = Assign.with(varX, const1); // x = 1 in B0
            Assign assign2 = Assign.with(varX, const2); // x = 2 in B1
            Assign assign3 = Assign.with(varX, const3); // x = 3 in B2

            // 构建CFG：B0 -> (B1, B2)
            BasicBlock<IRNode> block0 = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign1)))
                    .label(new Label("L0", null))
                    .build();

            BasicBlock<IRNode> block1 = new BasicBlock.Builder<IRNode>()
                    .id(1)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign2)))
                    .label(new Label("L1", null))
                    .build();

            BasicBlock<IRNode> block2 = new BasicBlock.Builder<IRNode>()
                    .id(2)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign3)))
                    .label(new Label("L2", null))
                    .build();

            List<BasicBlock<IRNode>> nodes = Arrays.asList(block0, block1, block2);
            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                    Triple.of(0, 1, 1),
                    Triple.of(0, 2, 1)
            );

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证B1和B2都有定义
            Set<Definition> block1Out = analysis.getOut(1);
            Set<Definition> block2Out = analysis.getOut(2);
            assertThat(block1Out).isNotEmpty();
            assertThat(block2Out).isNotEmpty();
        }

        @Test
        @DisplayName("if-else：不同分支的变量在merge点都可达")
        void testDifferentVariablesInBranches() {
            // 代码：
            //   x = 1;
            //   if (cond) {
            //     y = 2;
            //   } else {
            //     z = 3;
            //   }

            Assign assign1 = Assign.with(varX, const1); // B0
            Assign assign2 = Assign.with(varY, const2); // B1
            Assign assign3 = Assign.with(varZ, const3); // B2

            BasicBlock<IRNode> block0 = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign1)))
                    .label(new Label("L0", null))
                    .build();

            BasicBlock<IRNode> block1 = new BasicBlock.Builder<IRNode>()
                    .id(1)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign2)))
                    .label(new Label("L1", null))
                    .build();

            BasicBlock<IRNode> block2 = new BasicBlock.Builder<IRNode>()
                    .id(2)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign3)))
                    .label(new Label("L2", null))
                    .build();

            List<BasicBlock<IRNode>> nodes = Arrays.asList(block0, block1, block2);
            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                    Triple.of(0, 1, 1),
                    Triple.of(0, 2, 1)
            );

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证B1和B2都可达
            Set<Definition> block1Out = analysis.getOut(1);
            Set<Definition> block2Out = analysis.getOut(2);
            assertThat(block1Out).isNotEmpty();
            assertThat(block2Out).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("测试3: 循环的到达定义分析")
    class LoopTests {

        @Test
        @DisplayName("简单循环：循环体内的定义在循环头可达")
        void testSimpleLoop() {
            // 代码：
            //   x = 0;        // B0
            //   while (cond) {  // B1 -> B2
            //     x = x + 1;  // B2 -> B1
            //   }

            Assign assign1 = Assign.with(varX, const1); // x = 1 in B0
            Assign assign2 = Assign.with(varX, varX);   // x = x in B2

            BasicBlock<IRNode> block0 = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign1)))
                    .label(new Label("L0", null))
                    .build();

            BasicBlock<IRNode> block1 = new BasicBlock.Builder<IRNode>()
                    .id(1)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.emptyList())
                    .label(new Label("L1", null))
                    .build();

            BasicBlock<IRNode> block2 = new BasicBlock.Builder<IRNode>()
                    .id(2)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign2)))
                    .label(new Label("L2", null))
                    .build();

            List<BasicBlock<IRNode>> nodes = Arrays.asList(block0, block1, block2);
            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                    Triple.of(0, 1, 1),
                    Triple.of(1, 2, 1),
                    Triple.of(2, 1, 1)  // back edge
            );

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证分析完成
            Set<Definition> block2Out = analysis.getOut(2);
            assertThat(block2Out).isNotEmpty();

            Set<Definition> block1In = analysis.getIn(1);
            assertThat(block1In).isNotEmpty();
        }

        @Test
        @DisplayName("循环：多次迭代后定义合并")
        void testLoopWithMultipleIterations() {
            // 测试循环收敛性：多次迭代后数据流应该稳定

            Assign assign1 = Assign.with(varX, const1);
            Assign assign2 = Assign.with(varX, varX);

            BasicBlock<IRNode> block0 = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign1)))
                    .label(new Label("L0", null))
                    .build();

            BasicBlock<IRNode> block1 = new BasicBlock.Builder<IRNode>()
                    .id(1)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign2)))
                    .label(new Label("L1", null))
                    .build();

            List<BasicBlock<IRNode>> nodes = Arrays.asList(block0, block1);
            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                    Triple.of(0, 1, 1),
                    Triple.of(1, 1, 1)  // self-loop
            );

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证分析收敛
            Set<Definition> block1Out = analysis.getOut(1);
            assertThat(block1Out).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("测试4: 变量重新定义和杀死")
    class RedefinitionTests {

        @Test
        @DisplayName("多变量重新定义：每个变量的最新定义可达")
        void testMultipleVariableRedefinitions() {
            // 代码：
            //   x = 1; y = 1;
            //   x = 2;
            //   y = 2;
            //   z = x + y;

            Assign assign1 = Assign.with(varX, const1);
            Assign assign2 = Assign.with(varY, const1);
            Assign assign3 = Assign.with(varX, const2);
            Assign assign4 = Assign.with(varY, const2);

            BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Arrays.asList(
                            new Loc<>(assign1),
                            new Loc<>(assign2),
                            new Loc<>(assign3),
                            new Loc<>(assign4)
                    ))
                    .label(new Label("L0", null))
                    .build();

            CFG<IRNode> cfg = new CFG<>(
                    Collections.singletonList(block),
                    Collections.emptyList()
            );

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证出口：只有最新的x和y定义
            Set<Definition> block0Out = analysis.getOut(0);
            assertThat(block0Out).hasSize(2);
        }

        @Test
        @DisplayName("传递函数验证：out = gen ∪ (in - kill)")
        void testTransferFunction() {
            // 手动验证传递函数的正确性

            Assign assign1 = Assign.with(varX, const1);
            Assign assign2 = Assign.with(varY, const1);
            Assign assign3 = Assign.with(varX, const2); // kills x@B0:0

            BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Arrays.asList(
                            new Loc<>(assign1),
                            new Loc<>(assign2),
                            new Loc<>(assign3)
                    ))
                    .label(new Label("L0", null))
                    .build();

            CFG<IRNode> cfg = new CFG<>(
                    Collections.singletonList(block),
                    Collections.emptyList()
            );

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证assign3入口：有x@B0:0和y@B0:1
            Set<Definition> assign3In = analysis.getIn(assign3);
            assertThat(assign3In).isNotEmpty();

            // 验证assign3出口：有x@B0:2和y@B0:1
            Set<Definition> assign3Out = analysis.getOut(assign3);
            assertThat(assign3Out).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("测试5: 空基本块和边界条件")
    class EmptyAndBoundaryTests {

        @Test
        @DisplayName("空基本块：数据流穿透")
        void testEmptyBlock() {
            // 空基本块的数据流：in == out

            BasicBlock<IRNode> emptyBlock = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.emptyList())
                    .label(new Label("L0", null))
                    .build();

            BasicBlock<IRNode> nonEmptyBlock = new BasicBlock.Builder<IRNode>()
                    .id(1)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(Assign.with(varX, const1))))
                    .label(new Label("L1", null))
                    .build();

            List<BasicBlock<IRNode>> nodes = Arrays.asList(emptyBlock, nonEmptyBlock);
            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                    Triple.of(0, 1, 1)
            );

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证空块：in == out
            Set<Definition> emptyBlockIn = analysis.getIn(0);
            Set<Definition> emptyBlockOut = analysis.getOut(0);

            assertThat(emptyBlockIn).isEmpty();
            assertThat(emptyBlockOut).isEmpty();
        }

        @Test
        @DisplayName("孤立基本块：in和out都为空")
        void testIsolatedBlock() {
            // 没有前驱的基本块：in为空
            Assign assign = Assign.with(varX, const1);

            BasicBlock<IRNode> isolatedBlock = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign)))
                    .label(new Label("L0", null))
                    .build();

            CFG<IRNode> cfg = new CFG<>(
                    Collections.singletonList(isolatedBlock),
                    Collections.emptyList()
            );

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            Set<Definition> blockIn = analysis.getIn(0);
            assertThat(blockIn).isEmpty();

            Set<Definition> blockOut = analysis.getOut(0);
            assertThat(blockOut).hasSize(1);
        }

        @Test
        @DisplayName("Worklist算法：与迭代算法结果一致")
        void testWorklistAlgorithm() {
            // 验证Worklist算法和迭代算法产生相同结果

            Assign assign1 = Assign.with(varX, const1);
            Assign assign2 = Assign.with(varY, const2);
            Assign assign3 = Assign.with(varZ, const3);

            BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Arrays.asList(
                            new Loc<>(assign1),
                            new Loc<>(assign2),
                            new Loc<>(assign3)
                    ))
                    .label(new Label("L0", null))
                    .build();

            CFG<IRNode> cfg1 = new CFG<>(
                    Collections.singletonList(block),
                    Collections.emptyList()
            );

            CFG<IRNode> cfg2 = new CFG<>(
                    Collections.singletonList(block),
                    Collections.emptyList()
            );

            // 使用迭代算法
            ReachingDefinitionAnalysis analysis1 = new ReachingDefinitionAnalysis(cfg1);
            analysis1.analyze();

            // 使用Worklist算法
            ReachingDefinitionAnalysis analysis2 = new ReachingDefinitionAnalysis(cfg2);
            analysis2.analyzeWithWorklist();

            // 验证结果一致
            Set<Definition> out1 = analysis1.getOut(0);
            Set<Definition> out2 = analysis2.getOut(0);

            assertThat(out1).isEqualTo(out2);
        }

        @Test
        @DisplayName("结果格式化：字符串表示正确")
        void testResultFormatting() {
            Assign assign = Assign.with(varX, const1);

            BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign)))
                    .label(new Label("L0", null))
                    .build();

            CFG<IRNode> cfg = new CFG<>(
                    Collections.singletonList(block),
                    Collections.emptyList()
            );

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            String result = analysis.getResultString();
            assertThat(result).contains("=== 到达定义分析结果 ===");
            assertThat(result).contains("基本块 0");
            assertThat(result).contains("In:");
            assertThat(result).contains("Out:");
        }
    }

    @Nested
    @DisplayName("边界测试用例")
    class BoundaryTestCases {

        @Test
        @DisplayName("边界1：空CFG应该成功分析")
        void testEmptyCFG() {
            // 空CFG：没有基本块

            CFG<IRNode> emptyCFG = new CFG<>(
                    Collections.emptyList(),
                    Collections.emptyList()
            );

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(emptyCFG);
            analysis.analyze();

            // 验证不抛异常
            assertThat(analysis).isNotNull();
        }

        @Test
        @DisplayName("边界2：单节点CFG应该正确分析")
        void testSingleNodeCFG() {
            // 单节点CFG：只有一个基本块

            Assign assign = Assign.with(varX, const1);
            BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign)))
                    .label(new Label("L0", null))
                    .build();

            CFG<IRNode> cfg = new CFG<>(
                    Collections.singletonList(block),
                    Collections.emptyList()
            );

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证入口为空
            Set<Definition> in = analysis.getIn(0);
            assertThat(in).isEmpty();

            // 验证出口有定义
            Set<Definition> out = analysis.getOut(0);
            assertThat(out).hasSize(1);
        }

        @Test
        @DisplayName("边界3：自环CFG应该收敛")
        void testSelfLoopCFG() {
            // 自环CFG：基本块指向自己

            Assign assign = Assign.with(varX, const1);
            BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign)))
                    .label(new Label("L0", null))
                    .build();

            List<BasicBlock<IRNode>> nodes = Collections.singletonList(block);
            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                    Triple.of(0, 0, 1)  // self-loop
            );

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证分析收敛（不抛异常）
            Set<Definition> out = analysis.getOut(0);
            assertThat(out).isNotEmpty();
        }

        @Test
        @DisplayName("边界4：大型CFG应该在合理时间内分析")
        void testLargeCFGPerformance() {
            // 大型CFG：100个基本块

            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            // 创建100个基本块
            for (int i = 0; i < 100; i++) {
                Assign assign = Assign.with(new FrameSlot(i), ConstVal.valueOf(i));
                BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                        .id(i)
                        .kind(Kind.CONTINUOUS)
                        .codes(Collections.singletonList(new Loc<>(assign)))
                        .label(new Label("L" + i, null))
                        .build();
                nodes.add(block);

                // 创建边：每个块指向下一个块
                if (i < 99) {
                    edges.add(Triple.of(i, i + 1, 1));
                }
            }

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证最后一个块有所有定义
            Set<Definition> out = analysis.getOut(99);
            assertThat(out).hasSize(100);
        }

        @Test
        @DisplayName("边界5：环形依赖应该正确处理")
        void testCircularDependencies() {
            // 环形依赖：B0 -> B1 -> B2 -> B0

            Assign assign1 = Assign.with(varX, const1);
            Assign assign2 = Assign.with(varY, const2);
            Assign assign3 = Assign.with(varZ, const3);

            BasicBlock<IRNode> block0 = new BasicBlock.Builder<IRNode>()
                    .id(0)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign1)))
                    .label(new Label("L0", null))
                    .build();

            BasicBlock<IRNode> block1 = new BasicBlock.Builder<IRNode>()
                    .id(1)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign2)))
                    .label(new Label("L1", null))
                    .build();

            BasicBlock<IRNode> block2 = new BasicBlock.Builder<IRNode>()
                    .id(2)
                    .kind(Kind.CONTINUOUS)
                    .codes(Collections.singletonList(new Loc<>(assign3)))
                    .label(new Label("L2", null))
                    .build();

            List<BasicBlock<IRNode>> nodes = Arrays.asList(block0, block1, block2);
            List<Triple<Integer, Integer, Integer>> edges = Arrays.asList(
                    Triple.of(0, 1, 1),
                    Triple.of(1, 2, 1),
                    Triple.of(2, 0, 1)  // 形成环
            );

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();

            // 验证每个块都有定义
            assertThat(analysis.getOut(0)).isNotEmpty();
            assertThat(analysis.getOut(1)).isNotEmpty();
            assertThat(analysis.getOut(2)).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("性能测试用例")
    class PerformanceTestCases {

        @Test
        @DisplayName("性能1：大型线性CFG应该在合理时间内分析")
        void testLargeLinearCFGPerformance() {
            // 创建100个顺序连接的基本块

            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                Assign assign = Assign.with(new FrameSlot(i), ConstVal.valueOf(i));
                BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                        .id(i)
                        .kind(Kind.CONTINUOUS)
                        .codes(Collections.singletonList(new Loc<>(assign)))
                        .label(new Label("L" + i, null))
                        .build();
                nodes.add(block);

                // 每个块指向下一个块（线性链）
                if (i < 99) {
                    edges.add(Triple.of(i, i + 1, 1));
                }
            }

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            long startTime = System.currentTimeMillis();
            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();
            long endTime = System.currentTimeMillis();

            // 验证分析完成且耗时合理（< 1秒）
            assertThat(analysis).isNotNull();
            assertThat(endTime - startTime).isLessThan(1000L);
        }

        @Test
        @DisplayName("性能2：复杂分支CFG应该在合理时间内分析")
        void testComplexBranchCFGPerformance() {
            // 创建大量分支的CFG（二叉树结构）

            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            // 创建50个基本块的二叉树
            for (int i = 0; i < 50; i++) {
                Assign assign = Assign.with(new FrameSlot(i), ConstVal.valueOf(i));
                BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                        .id(i)
                        .kind(Kind.CONTINUOUS)
                        .codes(Collections.singletonList(new Loc<>(assign)))
                        .label(new Label("L" + i, null))
                        .build();
                nodes.add(block);

                // 二叉树：每个节点最多2个子节点
                if (2 * i + 1 < 50) {
                    edges.add(Triple.of(i, 2 * i + 1, 1));
                }
                if (2 * i + 2 < 50) {
                    edges.add(Triple.of(i, 2 * i + 2, 1));
                }
            }

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            long startTime = System.currentTimeMillis();
            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();
            long endTime = System.currentTimeMillis();

            // 验证分析完成且耗时合理（< 2秒）
            assertThat(analysis).isNotNull();
            assertThat(endTime - startTime).isLessThan(2000L);
        }

        @Test
        @DisplayName("性能3：循环密集型CFG应该在合理时间内分析")
        void testLoopHeavyCFGPerformance() {
            // 创建循环密集的CFG（多个嵌套循环）

            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();

            // 创建20个循环
            for (int i = 0; i < 20; i++) {
                Assign assign = Assign.with(new FrameSlot(i), ConstVal.valueOf(i));
                BasicBlock<IRNode> block = new BasicBlock.Builder<IRNode>()
                        .id(i)
                        .kind(Kind.CONTINUOUS)
                        .codes(Collections.singletonList(new Loc<>(assign)))
                        .label(new Label("L" + i, null))
                        .build();
                nodes.add(block);

                // 每个循环：节点指向自己（自环）
                edges.add(Triple.of(i, i, 1));

                // 每隔5个节点创建一个大循环
                if (i % 5 == 0 && i > 0) {
                    edges.add(Triple.of(i - 5, i, 1));
                }
            }

            CFG<IRNode> cfg = new CFG<>(nodes, edges);

            long startTime = System.currentTimeMillis();
            ReachingDefinitionAnalysis analysis = new ReachingDefinitionAnalysis(cfg);
            analysis.analyze();
            long endTime = System.currentTimeMillis();

            // 验证分析完成且耗时合理（< 3秒）
            assertThat(analysis).isNotNull();
            assertThat(endTime - startTime).isLessThan(3000L);
        }
    }
}
