package org.teachfx.antlr4.ep21.pass.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep21.symtab.type.TypeTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("图着色寄存器分配器测试")
public class GraphColoringAllocatorTest {

    private GraphColoringAllocator allocator;

    private VariableSymbol createVariable(String name) {
        VariableSymbol var = new VariableSymbol(name, TypeTable.INT);
        var.setSlotIdx(0);
        return var;
    }

    @BeforeEach
    void setUp() {
        allocator = new GraphColoringAllocator();
    }

    @Nested
    @DisplayName("创建和配置测试")
    class CreationTests {

        @Test
        @DisplayName("应该能够创建图着色寄存器分配器")
        void testCanCreateAllocator() {
            assertNotNull(allocator);
        }

        @Test
        @DisplayName("分配器应该初始化为空状态")
        void testAllocatorInitialState() {
            assertEquals(0, allocator.getAllocatedRegisterCount());
            assertEquals(0, allocator.getSpilledCount());
        }
    }

    @Nested
    @DisplayName("活跃区间测试")
    class LiveIntervalTests {

        @Test
        @DisplayName("应该能够设置活跃区间")
        void testSetLiveIntervals() {
            VariableSymbol var1 = createVariable("x");
            VariableSymbol var2 = createVariable("y");

            Map<VariableSymbol, LiveInterval> intervals = new HashMap<>();
            intervals.put(var1, new LiveInterval("x", 1, 5));
            intervals.put(var2, new LiveInterval("y", 2, 6));

            allocator.setLiveIntervals(intervals);

            Map<VariableSymbol, LiveInterval> result = allocator.getLiveIntervals();
            assertEquals(2, result.size());
            assertNotNull(result.get(var1));
            assertNotNull(result.get(var2));
        }

        @Test
        @DisplayName("应该能够构建干扰图")
        void testBuildInterferenceGraph() {
            VariableSymbol var1 = createVariable("x");
            VariableSymbol var2 = createVariable("y");
            VariableSymbol var3 = createVariable("z");

            Map<VariableSymbol, LiveInterval> intervals = new HashMap<>();
            intervals.put(var1, new LiveInterval("x", 1, 5));
            intervals.put(var2, new LiveInterval("y", 2, 6));
            intervals.put(var3, new LiveInterval("z", 3, 7));

            allocator.setLiveIntervals(intervals);

            Map<VariableSymbol, Set<VariableSymbol>> graph = allocator.getInterferenceGraph();
            assertEquals(3, graph.size());
            assertEquals(2, graph.get(var1).size());
            assertEquals(2, graph.get(var2).size());
            assertEquals(2, graph.get(var3).size());
        }
    }

    @Nested
    @DisplayName("寄存器分配测试")
    class AllocationTests {

        @Test
        @DisplayName("应该能够分配第一个变量")
        void testAllocateFirstVariable() {
            VariableSymbol var = createVariable("x");
            LiveInterval interval = new LiveInterval("x", 1, 5);
            Map<VariableSymbol, LiveInterval> intervals = new HashMap<>();
            intervals.put(var, interval);
            allocator.setLiveIntervals(intervals);

            int reg = allocator.allocateRegister(var);

            assertEquals(1, reg);
            assertEquals(1, allocator.getAllocatedRegisterCount());
            assertEquals(1, allocator.getRegister(var));
        }

        @Test
        @DisplayName("应该能够分配多个变量到不同寄存器")
        void testAllocateMultipleVariables() {
            VariableSymbol var1 = createVariable("x");
            VariableSymbol var2 = createVariable("y");
            VariableSymbol var3 = createVariable("z");

            Map<VariableSymbol, LiveInterval> intervals = new HashMap<>();
            intervals.put(var1, new LiveInterval("x", 1, 5));
            intervals.put(var2, new LiveInterval("y", 2, 6));
            intervals.put(var3, new LiveInterval("z", 3, 7));

            allocator.setLiveIntervals(intervals);

            int reg1 = allocator.allocateRegister(var1);
            int reg2 = allocator.allocateRegister(var2);
            int reg3 = allocator.allocateRegister(var3);

            assertEquals(3, allocator.getAllocatedRegisterCount());
            assertNotEquals(reg1, reg2);
            assertNotEquals(reg2, reg3);
            assertNotEquals(reg1, reg3);
        }

        @Test
        @DisplayName("应该能够处理有干扰变量的分配")
        void testAllocateInterferingVariables() {
            VariableSymbol var1 = createVariable("x");
            VariableSymbol var2 = createVariable("y");

            Map<VariableSymbol, LiveInterval> intervals = new HashMap<>();
            intervals.put(var1, new LiveInterval("x", 1, 5));
            intervals.put(var2, new LiveInterval("y", 2, 6));

            allocator.setLiveIntervals(intervals);

            int reg1 = allocator.allocateRegister(var1);
            int reg2 = allocator.allocateRegister(var2);

            assertEquals(2, allocator.getAllocatedRegisterCount());
            assertNotEquals(reg1, reg2);
            assertFalse(allocator.isSpilled(var1));
            assertFalse(allocator.isSpilled(var2));
        }
    }

    @Nested
    @DisplayName("寄存器释放测试")
    class RegisterReleaseTests {

        @Test
        @DisplayName("应该能够释放已分配的寄存器")
        void testReleaseRegister() {
            VariableSymbol var = createVariable("x");
            LiveInterval interval = new LiveInterval("x", 1, 5);
            Map<VariableSymbol, LiveInterval> intervals = new HashMap<>();
            intervals.put(var, interval);
            allocator.setLiveIntervals(intervals);

            int reg = allocator.allocateRegister(var);

            allocator.freeRegister(var);

            assertEquals(0, allocator.getAllocatedRegisterCount());
            assertEquals(-1, allocator.getRegister(var));
        }

        @Test
        @DisplayName("释放寄存器应该允许重新分配")
        void testReleaseAllowsReAllocation() {
            VariableSymbol var1 = createVariable("x");
            VariableSymbol var2 = createVariable("y");

            Map<VariableSymbol, LiveInterval> intervals = new HashMap<>();
            intervals.put(var1, new LiveInterval("x", 1, 5));
            intervals.put(var2, new LiveInterval("y", 2, 6));

            allocator.setLiveIntervals(intervals);

            int reg1 = allocator.allocateRegister(var1);
            allocator.freeRegister(var1);

            int reg2 = allocator.allocateRegister(var2);

            assertTrue(reg2 >= 1 && reg2 <= 12);
            assertEquals(1, allocator.getAllocatedRegisterCount());
        }
    }

    @Nested
    @DisplayName("重置测试")
    class ResetTests {

        @Test
        @DisplayName("应该能够重置分配器状态")
        void testResetAllocator() {
            VariableSymbol var = createVariable("x");
            LiveInterval interval = new LiveInterval("x", 1, 5);
            Map<VariableSymbol, LiveInterval> intervals = new HashMap<>();
            intervals.put(var, interval);
            allocator.setLiveIntervals(intervals);

            // 需要实际分配寄存器，而不仅仅是设置活跃区间
            allocator.allocateRegister(var);

            assertEquals(1, allocator.getAllocatedRegisterCount());

            allocator.reset();

            assertEquals(0, allocator.getAllocatedRegisterCount());
            assertEquals(0, allocator.getSpilledCount());
        }
    }
}
