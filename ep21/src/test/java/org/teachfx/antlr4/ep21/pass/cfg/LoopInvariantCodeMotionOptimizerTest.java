package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.analysis.dataflow.LoopAnalysis;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.JMPInstr;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;
import org.teachfx.antlr4.ep21.utils.Kind;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("循环不变代码外提优化器测试")
public class LoopInvariantCodeMotionOptimizerTest {

    private LoopInvariantCodeMotionOptimizer optimizer;
    private CFG<IRNode> cfg;

    @BeforeEach
    void setUp() {
        optimizer = new LoopInvariantCodeMotionOptimizer();
    }

    @Nested
    @DisplayName("创建和配置测试")
    class CreationTests {

        @Test
        @DisplayName("应该能够创建循环不变代码外提优化器")
        void testCanCreateOptimizer() {
            assertNotNull(optimizer);
        }

        @Test
        @DisplayName("优化器应该初始化为空状态")
        void testOptimizerInitialState() {
            assertEquals(0, optimizer.getProcessedLoopsCount());
            assertEquals(0, optimizer.getMovedInstructionsCount());
        }
    }

    @Nested
    @DisplayName("基本功能测试")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("空CFG处理")
        void testEmptyCFG() {
            List<BasicBlock<IRNode>> nodes = new ArrayList<>();
            List<Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
            CFG<IRNode> testCfg = new CFG<>(nodes, edges);

            assertDoesNotThrow(() -> optimizer.onHandle(testCfg));
            assertEquals(0, optimizer.getProcessedLoopsCount());
            assertEquals(0, optimizer.getMovedInstructionsCount());
        }
    }
}
