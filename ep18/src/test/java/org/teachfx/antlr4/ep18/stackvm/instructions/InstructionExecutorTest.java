package org.teachfx.antlr4.ep18.stackvm.instructions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18.stackvm.*;
import org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic.*;
import org.teachfx.antlr4.ep18.stackvm.instructions.comparison.*;
import org.teachfx.antlr4.ep18.stackvm.instructions.constant.*;
import org.teachfx.antlr4.ep18.stackvm.instructions.controlflow.*;
import org.teachfx.antlr4.ep18.stackvm.instructions.memory.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 指令执行器单元测试
 * 测试Phase 3实现的所有指令
 */
@DisplayName("Instruction Executor Tests")
public class InstructionExecutorTest {

    private VMExecutionContext createContext(int... initialStack) {
        VMConfig config = VMConfig.builder()
            .setStackSize(100)
            .setHeapSize(1000)
            .setMaxFrameCount(10)
            .setTraceEnabled(false)
            .build();
        VMStats stats = new VMStats();

        int[] stack = new int[100];
        int sp = 0;
        for (int val : initialStack) {
            stack[sp++] = val;
        }

        int[] heap = new int[1000];
        int[] locals = new int[100];
        StackFrame[] callStack = new StackFrame[10];
        List<StructValue> structTable = new ArrayList<>();

        return new VMExecutionContext(
            null, config, stats, 0, stack, sp,
            heap, locals, callStack, -1, false,
            0, structTable, 1
        );
    }

    @Nested
    @DisplayName("Arithmetic Instructions")
    class ArithmeticInstructionTests {

        @Test
        @DisplayName("INeg should negate integer")
        void testINeg() throws Exception {
            VMExecutionContext ctx = createContext(10);
            new INegInstruction().execute(ctx, 0);
            assertEquals(-10, ctx.pop());
        }

        @Test
        @DisplayName("INot should perform bitwise not")
        void testINot() throws Exception {
            VMExecutionContext ctx = createContext(0);
            new INotInstruction().execute(ctx, 0);
            assertEquals(~0, ctx.pop());
        }

        @Test
        @DisplayName("IAnd should perform bitwise and")
        void testIAnd() throws Exception {
            VMExecutionContext ctx = createContext(0b1100, 0b1010);
            new IAndInstruction().execute(ctx, 0);
            assertEquals(0b1000, ctx.pop());
        }

        @Test
        @DisplayName("IOr should perform bitwise or")
        void testIOr() throws Exception {
            VMExecutionContext ctx = createContext(0b1100, 0b1010);
            new IOrInstruction().execute(ctx, 0);
            assertEquals(0b1110, ctx.pop());
        }

        @Test
        @DisplayName("IXor should perform bitwise xor")
        void testIXor() throws Exception {
            VMExecutionContext ctx = createContext(0b1100, 0b1010);
            new IXorInstruction().execute(ctx, 0);
            assertEquals(0b0110, ctx.pop());
        }
    }

    @Nested
    @DisplayName("Comparison Instructions")
    class ComparisonInstructionTests {

        @Test
        @DisplayName("ILe should compare less than or equal")
        void testILe() throws Exception {
            VMExecutionContext ctx1 = createContext(5, 10);
            new ILeInstruction().execute(ctx1, 0);
            assertEquals(1, ctx1.pop()); // 5 <= 10

            VMExecutionContext ctx2 = createContext(10, 10);
            new ILeInstruction().execute(ctx2, 0);
            assertEquals(1, ctx2.pop()); // 10 <= 10

            VMExecutionContext ctx3 = createContext(15, 10);
            new ILeInstruction().execute(ctx3, 0);
            assertEquals(0, ctx3.pop()); // 15 <= 10
        }

        @Test
        @DisplayName("IGt should compare greater than")
        void testIGt() throws Exception {
            VMExecutionContext ctx = createContext(15, 10);
            new IGtInstruction().execute(ctx, 0);
            assertEquals(1, ctx.pop()); // 15 > 10
        }

        @Test
        @DisplayName("IGe should compare greater than or equal")
        void testIGe() throws Exception {
            VMExecutionContext ctx = createContext(10, 10);
            new IGeInstruction().execute(ctx, 0);
            assertEquals(1, ctx.pop()); // 10 >= 10
        }

        @Test
        @DisplayName("IEq should compare equality")
        void testIEq() throws Exception {
            VMExecutionContext ctx1 = createContext(10, 10);
            new IEqInstruction().execute(ctx1, 0);
            assertEquals(1, ctx1.pop()); // 10 == 10

            VMExecutionContext ctx2 = createContext(10, 20);
            new IEqInstruction().execute(ctx2, 0);
            assertEquals(0, ctx2.pop()); // 10 == 20
        }

        @Test
        @DisplayName("INe should compare inequality")
        void testINe() throws Exception {
            VMExecutionContext ctx = createContext(10, 20);
            new INeInstruction().execute(ctx, 0);
            assertEquals(1, ctx.pop()); // 10 != 20
        }
    }

    @Nested
    @DisplayName("Control Flow Instructions")
    class ControlFlowInstructionTests {

        @Test
        @DisplayName("Brf should jump when condition is false")
        void testBrf() throws Exception {
            VMExecutionContext ctx1 = createContext(0); // condition is false
            new BrfInstruction().execute(ctx1, 100);
            assertEquals(100, ctx1.getProgramCounter());

            VMExecutionContext ctx2 = createContext(1); // condition is true
            new BrfInstruction().execute(ctx2, 100);
            assertEquals(0, ctx2.getProgramCounter()); // should not jump
        }

        @Test
        @DisplayName("Print should pop and output value")
        void testPrint() throws Exception {
            VMExecutionContext ctx = createContext(42);
            new PrintInstruction().execute(ctx, 0);
            assertTrue(ctx.isStackEmpty());
        }

        @Test
        @DisplayName("Pop should discard top value")
        void testPop() throws Exception {
            VMExecutionContext ctx = createContext(1, 2, 3);
            new PopInstruction().execute(ctx, 0);
            assertEquals(2, ctx.getStackDepth());
            assertEquals(2, ctx.pop());
        }
    }

    @Nested
    @DisplayName("Memory Instructions")
    class MemoryInstructionTests {

        @Test
        @DisplayName("GLoad should load from global memory")
        void testGLoad() throws Exception {
            VMExecutionContext ctx = createContext();
            ctx.heapWrite(5, 42);
            new GLoadInstruction().execute(ctx, 5);
            assertEquals(42, ctx.pop());
        }

        @Test
        @DisplayName("GStore should store to global memory")
        void testGStore() throws Exception {
            VMExecutionContext ctx = createContext(99);
            new GStoreInstruction().execute(ctx, 10);
            assertEquals(99, ctx.heapRead(10));
        }

        @Test
        @DisplayName("Struct should create new struct")
        void testStruct() throws Exception {
            VMExecutionContext ctx = createContext();
            new StructInstruction().execute(ctx, 3); // 3 fields
            int structRef = ctx.pop();
            assertTrue(structRef > 0);
        }

        @Test
        @DisplayName("Null should push null reference")
        void testNull() throws Exception {
            VMExecutionContext ctx = createContext();
            new NullInstruction().execute(ctx, 0);
            assertEquals(0, ctx.pop());
        }
    }

    @Nested
    @DisplayName("ControlFlowExecutor Statistics")
    class ControlFlowExecutorTests {

        @Test
        @DisplayName("Should track branch statistics")
        void testBranchStatistics() {
            ControlFlowExecutor.BranchStatistics stats = ControlFlowExecutor.getGlobalStatistics();
            long initialBranches = stats.getTotalBranches();

            // Execute some branches
            VMExecutionContext ctx1 = createContext();
            ControlFlowExecutor.jumpIfTrue(ctx1, 1, 100);
            ControlFlowExecutor.jumpIfFalse(ctx1, 0, 200);

            assertTrue(stats.getTotalBranches() >= initialBranches + 2);
        }
    }

    @Nested
    @DisplayName("MemoryAccessExecutor Statistics")
    class MemoryAccessExecutorTests {

        @Test
        @DisplayName("Should track memory access statistics")
        void testMemoryStatistics() {
            MemoryAccessExecutor.resetStatistics();
            MemoryAccessExecutor.MemoryAccessStatistics stats = MemoryAccessExecutor.getGlobalStatistics();

            VMExecutionContext ctx = createContext();
            ctx.storeLocal(0, 42);

            MemoryAccessExecutor.loadLocal(ctx, 0);
            MemoryAccessExecutor.storeLocal(ctx, 1, 10);

            assertEquals(1, stats.getLocalReads());
            assertEquals(1, stats.getLocalWrites());
        }
    }

    @Nested
    @DisplayName("InstructionFactory Tests")
    class InstructionFactoryTests {

        @Test
        @DisplayName("Should return all registered instructions")
        void testGetSupportedInstructions() {
            InstructionFactory factory = InstructionFactory.getInstance();
            List<Instruction> instructions = factory.getSupportedInstructions();

            // We registered 27 instructions in Phase 2-3
            assertTrue(instructions.size() >= 25);
        }

        @Test
        @DisplayName("Should support all Phase 3 instructions")
        void testPhase3Instructions() {
            InstructionFactory factory = InstructionFactory.getInstance();

            // Arithmetic
            assertTrue(factory.isSupported(INegInstruction.OPCODE));
            assertTrue(factory.isSupported(INotInstruction.OPCODE));
            assertTrue(factory.isSupported(IAndInstruction.OPCODE));
            assertTrue(factory.isSupported(IOrInstruction.OPCODE));
            assertTrue(factory.isSupported(IXorInstruction.OPCODE));

            // Comparison
            assertTrue(factory.isSupported(ILeInstruction.OPCODE));
            assertTrue(factory.isSupported(IGtInstruction.OPCODE));
            assertTrue(factory.isSupported(IGeInstruction.OPCODE));
            assertTrue(factory.isSupported(IEqInstruction.OPCODE));
            assertTrue(factory.isSupported(INeInstruction.OPCODE));

            // Control flow
            assertTrue(factory.isSupported(BrfInstruction.OPCODE));
            assertTrue(factory.isSupported(CallInstruction.OPCODE));
            assertTrue(factory.isSupported(RetInstruction.OPCODE));
            assertTrue(factory.isSupported(PrintInstruction.OPCODE));
            assertTrue(factory.isSupported(PopInstruction.OPCODE));

            // Memory
            assertTrue(factory.isSupported(GLoadInstruction.OPCODE));
            assertTrue(factory.isSupported(GStoreInstruction.OPCODE));
            assertTrue(factory.isSupported(FLoadInstruction.OPCODE));
            assertTrue(factory.isSupported(FStoreInstruction.OPCODE));
            assertTrue(factory.isSupported(StructInstruction.OPCODE));
            assertTrue(factory.isSupported(NullInstruction.OPCODE));
        }
    }
}
