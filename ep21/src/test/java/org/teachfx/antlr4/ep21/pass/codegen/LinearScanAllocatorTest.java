package org.teachfx.antlr4.ep21.pass.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * 线性扫描寄存器分配器单元测试
 *
 * <p>测试覆盖：</p>
 * <ul>
 *   <li>基本分配和释放功能</li>
 *   <li>寄存器溢出到栈</li>
 *   <li>调用约定合规性</li>
 *   <li>边界条件和错误处理</li>
 * </ul>
 *
 * @author EP18R Register VM Team
 */
@DisplayName("LinearScanAllocator Tests")
class LinearScanAllocatorTest {

    private org.teachfx.antlr4.ep18r.stackvm.codegen.IRegisterAllocator allocator;

    @BeforeEach
    void setUp() {
        allocator = new LinearScanAllocator();
    }

    @Nested
    @DisplayName("Basic Allocation Tests")
    class BasicAllocationTests {

        @Test
        @DisplayName("Should allocate first variable to callee-saved register (s0)")
        void testFirstAllocation() {
            int reg = allocator.allocate("x");
            assertThat(reg).isEqualTo(8); // s0 (r8)
            assertThat(allocator.getRegister("x")).isEqualTo(8);
        }

        @Test
        @DisplayName("Should allocate multiple variables to different registers")
        void testMultipleAllocations() {
            int reg1 = allocator.allocate("x");
            int reg2 = allocator.allocate("y");
            int reg3 = allocator.allocate("z");

            assertThat(reg1).isNotEqualTo(reg2);
            assertThat(reg2).isNotEqualTo(reg3);
            assertThat(reg1).isNotEqualTo(reg3);
        }

        @Test
        @DisplayName("Should return same register for reallocation of same variable")
        void testReallocation() {
            int reg1 = allocator.allocate("x");
            int reg2 = allocator.allocate("x");
            int reg3 = allocator.allocate("x");

            assertThat(reg1).isEqualTo(reg2);
            assertThat(reg2).isEqualTo(reg3);
        }

        @Test
        @DisplayName("Should return -1 for unallocated variable")
        void testGetUnallocatedVariable() {
            assertThat(allocator.getRegister("nonexistent")).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should free register after free() call")
        void testFree() {
            int reg1 = allocator.allocate("x");
            allocator.free("x");

            assertThat(allocator.getRegister("x")).isEqualTo(-1);

            // Next allocation should reuse the freed register
            int reg2 = allocator.allocate("y");
            assertThat(reg2).isEqualTo(reg1);
        }
    }

    @Nested
    @DisplayName("Callee-Saved Register Priority Tests")
    class CalleeSavedPriorityTests {

        @Test
        @DisplayName("Should prioritize callee-saved registers (s0-s4)")
        void testCalleeSavedPriority() {
            // Allocate 5 variables, should use s0-s4 (r8-r12)
            for (int i = 0; i < 5; i++) {
                int reg = allocator.allocate("var" + i);
                assertThat(reg).isBetween(8, 12); // s0-s4
            }
        }

        @Test
        @DisplayName("Should use caller-saved registers after callee-saved exhausted")
        void testCallerSavedFallback() {
            // Allocate 6 variables, first 5 use s0-s4, 6th uses caller-saved
            int[] regs = new int[6];
            for (int i = 0; i < 6; i++) {
                regs[i] = allocator.allocate("var" + i);
            }

            // First 5 should be callee-saved (r8-r12)
            for (int i = 0; i < 5; i++) {
                assertThat(regs[i]).isBetween(8, 12);
            }

            // 6th should be caller-saved (not r8-r12)
            boolean notInCalleeSaved = regs[5] < 8 || regs[5] > 12;
            assertThat(notInCalleeSaved).isTrue();
            assertThat(regs[5]).isNotIn(0, 13, 14); // Not reserved
        }
    }

    @Nested
    @DisplayName("Register Spilling Tests")
    class RegisterSpillingTests {

        @Test
        @DisplayName("Should spill variable to stack when registers exhausted")
        void testSpillToStack() {
            // Allocate all available registers (13 total: r1-r12, r15)
            for (int i = 0; i < 13; i++) {
                allocator.allocate("var" + i);
            }

            // Next allocation should automatically spill an existing variable
            int reg = allocator.allocate("overflow");

            // Variable should be in a register (after spilling another)
            // or spilled to stack (if no registers available)
            assertThat(reg).isNotEqualTo(0); // Never allocate r0
            assertThat(reg).isNotEqualTo(13); // Never allocate r13 (sp)
            assertThat(reg).isNotEqualTo(14); // Never allocate r14 (fp)

            // At least one variable should be spilled
            assertThat(allocator.getSpilledVariables().size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return correct spill slot offset")
        void testSpillSlotOffset() {
            allocator.allocate("x");
            allocator.allocate("y");

            int slot1 = allocator.spillToStack("z");
            int slot2 = allocator.spillToStack("w");

            // Slots should be at different offsets
            assertThat(slot1).isNotEqualTo(slot2);

            // Slots should be negative (below fp)
            assertThat(slot1).isNegative();
            assertThat(slot2).isNegative();
        }

        @Test
        @DisplayName("Should return existing spill slot for already spilled variable")
        void testExistingSpillSlot() {
            int slot1 = allocator.spillToStack("x");
            int slot2 = allocator.spillToStack("x");

            assertThat(slot1).isEqualTo(slot2);
        }

        @Test
        @DisplayName("Should free register when variable is spilled")
        void testSpillFreesRegister() {
            int reg = allocator.allocate("x");
            int availableBefore = allocator.getAvailableRegisterCount();

            allocator.spillToStack("x");

            // Register should be freed
            assertThat(allocator.getRegister("x")).isEqualTo(-1);
            assertThat(allocator.getAvailableRegisterCount()).isGreaterThan(availableBefore);
        }

        @Test
        @DisplayName("Should return -1 for non-spilled variable")
        void testGetSpillSlotForNonSpilled() {
            assertThat(allocator.getSpillSlot("nonexistent")).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("Should reset all allocations")
        void testReset() {
            allocator.allocate("x");
            allocator.allocate("y");
            allocator.spillToStack("z");

            int availableBefore = allocator.getAvailableRegisterCount();

            allocator.reset();

            // All mappings should be cleared
            assertThat(allocator.getAllocation()).isEmpty();
            assertThat(allocator.getRegister("x")).isEqualTo(-1);
            assertThat(allocator.isSpilled("z")).isFalse();

            // All registers should be available
            assertThat(allocator.getAvailableRegisterCount())
                    .isGreaterThan(availableBefore);
        }
    }

    @Nested
    @DisplayName("Reserved Register Tests")
    class ReservedRegisterTests {

        @Test
        @DisplayName("Should not allocate R0 (zero register)")
        void testZeroRegisterReserved() {
            // Allocate many variables
            for (int i = 0; i < 15; i++) {
                int reg = allocator.allocate("var" + i);
                assertThat(reg).isNotEqualTo(0); // Never allocate r0
            }
        }

        @Test
        @DisplayName("Should not allocate R13 (stack pointer)")
        void testStackPointerReserved() {
            for (int i = 0; i < 15; i++) {
                int reg = allocator.allocate("var" + i);
                assertThat(reg).isNotEqualTo(13); // Never allocate r13
            }
        }

        @Test
        @DisplayName("Should not allocate R14 (frame pointer)")
        void testFramePointerReserved() {
            for (int i = 0; i < 15; i++) {
                int reg = allocator.allocate("var" + i);
                assertThat(reg).isNotEqualTo(14); // Never allocate r14
            }
        }
    }

    @Nested
    @DisplayName("Force Allocation Tests")
    class ForceAllocationTests {

        @Test
        @DisplayName("Should force allocate to specific register")
        void testForceAllocate() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            alloc.forceAllocate("param0", 2); // Force to a0 (r2)

            assertThat(alloc.getRegister("param0")).isEqualTo(2);
            assertThat(alloc.getRegisterAbiName("param0")).isEqualTo("a0");
        }

        @Test
        @DisplayName("Should throw on force allocate to reserved register")
        void testForceAllocateReserved() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            assertThatThrownBy(() -> alloc.forceAllocate("x", 0)) // r0
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reserved");

            assertThatThrownBy(() -> alloc.forceAllocate("x", 13)) // sp
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reserved");
        }

        @Test
        @DisplayName("Should throw on force allocate to occupied register")
        void testForceAllocateOccupied() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            alloc.allocate("x");
            int reg = alloc.getRegister("x");

            assertThatThrownBy(() -> alloc.forceAllocate("y", reg))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already allocated");
        }

        @Test
        @DisplayName("Should free previous allocation when force allocating")
        void testForceAllocateFreesPrevious() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            alloc.allocate("x");
            int reg1 = alloc.getRegister("x");

            alloc.forceAllocate("x", 3); // Force to a1 (r3)

            assertThat(alloc.getRegister("x")).isEqualTo(3);
            assertThat(reg1).isNotEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Calling Convention Tests")
    class CallingConventionTests {

        @Test
        @DisplayName("Should identify caller-saved registers correctly")
        void testCallerSavedIdentification() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            // Caller-saved: ra (r1), a0-a5 (r2-r7), lr (r15)
            assertThat(alloc.isCallerSaved(1)).isTrue();  // ra
            assertThat(alloc.isCallerSaved(2)).isTrue();  // a0
            assertThat(alloc.isCallerSaved(7)).isTrue();  // a5
            assertThat(alloc.isCallerSaved(15)).isTrue(); // lr

            // Callee-saved should be false
            assertThat(alloc.isCallerSaved(8)).isFalse(); // s0
        }

        @Test
        @DisplayName("Should identify callee-saved registers correctly")
        void testCalleeSavedIdentification() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            // Callee-saved: s0-s4 (r8-r12)
            assertThat(alloc.isCalleeSaved(8)).isTrue();  // s0
            assertThat(alloc.isCalleeSaved(12)).isTrue(); // s4

            // Caller-saved should be false
            assertThat(alloc.isCalleeSaved(2)).isFalse(); // a0
        }

        @Test
        @DisplayName("Should identify argument registers correctly")
        void testArgumentRegisterIdentification() {
            // a0-a5 (r2-r7) are argument registers
            assertThat(LinearScanAllocator.isArgumentRegister(2)).isTrue(); // a0
            assertThat(LinearScanAllocator.isArgumentRegister(7)).isTrue(); // a5
            assertThat(LinearScanAllocator.isArgumentRegister(8)).isFalse(); // s0
        }

        @Test
        @DisplayName("Should get correct argument register for index")
        void testGetArgRegister() {
            assertThat(LinearScanAllocator.getArgRegister(0)).isEqualTo(2); // a0
            assertThat(LinearScanAllocator.getArgRegister(5)).isEqualTo(7); // a5

            assertThatThrownBy(() -> LinearScanAllocator.getArgRegister(-1))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> LinearScanAllocator.getArgRegister(6))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should return a0 (r2) as return value register")
        void testReturnValueRegister() {
            assertThat(LinearScanAllocator.getReturnValueRegister()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should get correct register ABI name")
        void testGetRegisterName() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            assertThat(alloc.getRegisterName(0)).isEqualTo("zero");
            assertThat(alloc.getRegisterName(2)).isEqualTo("a0");
            assertThat(alloc.getRegisterName(8)).isEqualTo("s0");
            assertThat(alloc.getRegisterName(13)).isEqualTo("sp");
            assertThat(alloc.getRegisterName(14)).isEqualTo("fp");
        }

        @Test
        @DisplayName("Should get variable's register ABI name")
        void testGetRegisterAbiName() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            alloc.allocate("x");
            int reg = alloc.getRegister("x");

            assertThat(alloc.getRegisterAbiName("x")).isEqualTo(alloc.getRegisterName(reg));
        }

        @Test
        @DisplayName("Should return null for unallocated variable ABI name")
        void testGetRegisterAbiNameForUnallocated() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            assertThat(alloc.getRegisterAbiName("nonexistent")).isNull();
        }

        @Test
        @DisplayName("Should get all allocated variables")
        void testGetAllocatedVariables() {
            allocator.allocate("x");
            allocator.allocate("y");
            allocator.allocate("z");

            assertThat(allocator.getAllocatedVariables())
                    .containsExactlyInAnyOrder("x", "y", "z");
        }

        @Test
        @DisplayName("Should get all spilled variables")
        void testGetSpilledVariables() {
            allocator.spillToStack("x");
            allocator.spillToStack("y");

            assertThat(allocator.getSpilledVariables())
                    .containsExactlyInAnyOrder("x", "y");
        }

        @Test
        @DisplayName("Should get available registers by type")
        void testGetAvailableRegistersByType() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            // Allocate some callee-saved registers
            alloc.allocate("x"); // s0
            alloc.allocate("y"); // s1

            int[] callerSaved = alloc.getAvailableRegistersByType(true);
            int[] calleeSaved = alloc.getAvailableRegistersByType(false);

            // Should have all caller-saved available
            assertThat(callerSaved).isNotEmpty();

            // Should have fewer callee-saved available (2 allocated)
            assertThat(calleeSaved.length).isEqualTo(3); // 5 total - 2 allocated
        }

        @Test
        @DisplayName("Should generate allocation report")
        void testGenerateAllocationReport() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            alloc.allocate("x");
            alloc.allocate("y");
            alloc.spillToStack("z");

            String report = alloc.generateAllocationReport();

            assertThat(report)
                    .contains("Register Allocation Report")
                    .contains("Variable to Register Mapping")
                    .contains("Spilled Variables")
                    .contains("Register Status");
        }

        @Test
        @DisplayName("Should get unmodifiable allocation map")
        void testGetAllocationIsUnmodifiable() {
            allocator.allocate("x");

            Map<String, Integer> allocation = allocator.getAllocation();

            assertThatThrownBy(() -> allocation.put("y", 8))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle allocation of all available registers")
        void testAllocateAllRegisters() {
            // 13 available registers (r1-r12, r15)
            for (int i = 0; i < 13; i++) {
                allocator.allocate("var" + i);
            }

            assertThat(allocator.getAllocatedRegisterCount()).isEqualTo(13);
            assertThat(allocator.getAvailableRegisterCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle free of non-existent variable gracefully")
        void testFreeNonExistentVariable() {
            // Should not throw exception
            assertThatCode(() -> allocator.free("nonexistent"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle multiple allocators independently")
        void testMultipleAllocators() {
            org.teachfx.antlr4.ep18r.stackvm.codegen.IRegisterAllocator alloc1 = new LinearScanAllocator();
            org.teachfx.antlr4.ep18r.stackvm.codegen.IRegisterAllocator alloc2 = new LinearScanAllocator();

            int reg1 = alloc1.allocate("x");
            int reg2 = alloc2.allocate("x");

            // Both should allocate to same register number
            assertThat(reg1).isEqualTo(reg2);

            // But allocators should be independent
            alloc1.free("x");
            assertThat(alloc1.getRegister("x")).isEqualTo(-1);
            assertThat(alloc2.getRegister("x")).isEqualTo(reg2);
        }

        @Test
        @DisplayName("Should create allocator with caller-saved priority")
        void testCallerSavedPriorityAllocator() {
            org.teachfx.antlr4.ep18r.stackvm.codegen.IRegisterAllocator alloc = new LinearScanAllocator(false);

            int reg1 = alloc.allocate("x");
            int reg2 = alloc.allocate("y");

            // Without callee-saved priority, should still allocate valid registers
            assertThat(reg1).isNotIn(0, 13, 14);
            assertThat(reg2).isNotIn(0, 13, 14);
        }
    }

    @Nested
    @DisplayName("Integration Scenario Tests")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("Should handle function parameter scenario")
        void testFunctionParameterScenario() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            // Force allocate parameters to a0-a3
            alloc.forceAllocate("param0", LinearScanAllocator.getArgRegister(0)); // a0
            alloc.forceAllocate("param1", LinearScanAllocator.getArgRegister(1)); // a1
            alloc.forceAllocate("param2", LinearScanAllocator.getArgRegister(2)); // a2
            alloc.forceAllocate("param3", LinearScanAllocator.getArgRegister(3)); // a3

            // Allocate local variables (should use s0-s4)
            alloc.allocate("local1");
            alloc.allocate("local2");

            assertThat(alloc.getRegisterAbiName("local1")).isIn("s0", "s1", "s2", "s3", "s4");
            assertThat(alloc.getRegisterAbiName("local2")).isIn("s0", "s1", "s2", "s3", "s4");
        }

        @Test
        @DisplayName("Should handle many local variables with spilling")
        void testManyLocalVariables() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            // Allocate more variables than available registers
            for (int i = 0; i < 20; i++) {
                alloc.allocate("local" + i);
            }

            // First 13 should be in registers initially, then some get spilled
            // After all allocations, some variables should be in registers
            // and some should be spilled to stack
            int inRegisters = 0;
            int spilled = 0;
            for (int i = 0; i < 20; i++) {
                int reg = alloc.getRegister("local" + i);
                if (reg != -1) {
                    // Verify it's not a reserved register
                    assertThat(reg).isNotIn(0, 13, 14);
                    inRegisters++;
                }
                if (alloc.isSpilled("local" + i)) {
                    spilled++;
                }
            }

            // Should have some in registers and some spilled
            assertThat(inRegisters).isGreaterThan(0);
            assertThat(spilled).isGreaterThan(0);
            assertThat(inRegisters + spilled).isEqualTo(20);
        }

        @Test
        @DisplayName("Should handle mixed allocation and spilling")
        void testMixedAllocationAndSpilling() {
            LinearScanAllocator alloc = new LinearScanAllocator();

            // Allocate some variables
            alloc.allocate("x");
            alloc.allocate("y");
            alloc.allocate("z");

            // Spill one
            int slot = alloc.spillToStack("x");
            assertThat(slot).isNegative();
            assertThat(alloc.isSpilled("x")).isTrue();

            // Allocate more
            alloc.allocate("w");

            // Original register for x should be reused
            assertThat(alloc.getAvailableRegisterCount()).isLessThan(13);
        }
    }
}
