package org.teachfx.antlr4.ep21.pass.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import org.teachfx.antlr4.ep21.pass.codegen.LinearScanAllocator;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

class EP18RRegisterAllocatorAdapterTest {

    private EP18RRegisterAllocatorAdapter adapter;
    private VariableSymbol var1;
    private VariableSymbol var2;
    private VariableSymbol var3;

    @BeforeEach
    void setUp() {
        adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());
        adapter.reset();
        var1 = new VariableSymbol("x");
        var2 = new VariableSymbol("y");
        var3 = new VariableSymbol("z");
    }

    @Test
    @DisplayName("Should allocate register for first variable")
    void testAllocateRegisterForFirstVariable() {
        int reg = adapter.allocateRegister(var1);
        assertTrue(reg >= 1 && reg <= 15, "Should allocate a valid register");
        assertEquals(reg, adapter.getRegister(var1), "Should return the same register");
        assertFalse(adapter.isSpilled(var1), "Variable should not be spilled");
    }

    @Test
    @DisplayName("Should allocate different registers for different variables")
    void testAllocateDifferentRegisters() {
        int reg1 = adapter.allocateRegister(var1);
        int reg2 = adapter.allocateRegister(var2);
        
        assertEquals(reg1, adapter.getRegister(var1), "var1 should have assigned register");
        assertEquals(reg2, adapter.getRegister(var2), "var2 should have assigned register");
    }

    @Test
    @DisplayName("Should return same register for existing variable")
    void testReturnSameRegisterForExistingVariable() {
        int reg1 = adapter.allocateRegister(var1);
        int reg2 = adapter.allocateRegister(var1);
        
        assertEquals(reg1, reg2, "Should return same register for same variable");
    }

    @Test
    @DisplayName("Should handle register allocation and deallocation")
    void testRegisterAllocationAndDeallocation() {
        int reg1 = adapter.allocateRegister(var1);
        assertEquals(1, adapter.getAllocatedRegisterCount());
        
        adapter.freeRegister(var1);
        assertEquals(-1, adapter.getRegister(var1), "Register should be freed");
        assertEquals(0, adapter.getAllocatedRegisterCount());
    }

    @Test
    @DisplayName("Should handle variables without names")
    void testVariablesWithoutNames() {
        VariableSymbol unnamedVar1 = new VariableSymbol(null);
        VariableSymbol unnamedVar2 = new VariableSymbol(null);

        int reg1 = adapter.allocateRegister(unnamedVar1);
        adapter.freeRegister(unnamedVar1);
        int reg2 = adapter.allocateRegister(unnamedVar2);

        assertTrue(reg1 >= 1 && reg1 <= 15);
        assertTrue(reg2 >= 1 && reg2 <= 15);
    }

    @Test
    @DisplayName("Should reset allocator state")
    void testResetAllocator() {
        adapter.allocateRegister(var1);
        adapter.allocateRegister(var2);
        assertEquals(2, adapter.getAllocatedRegisterCount());
        
        adapter.reset();
        assertEquals(0, adapter.getAllocatedRegisterCount());
        assertEquals(-1, adapter.getRegister(var1));
        assertEquals(-1, adapter.getRegister(var2));
    }

    @Test
    @DisplayName("Should handle spilled variables")
    void testSpilledVariables() {
        adapter.allocateRegister(var1);
        adapter.allocateRegister(var2);
        
        int offset1 = adapter.getStackOffset(var1);
        int offset2 = adapter.getStackOffset(var2);
        
        assertEquals(-1, offset1, "Non-spilled variable should return -1");
        assertEquals(-1, offset2, "Non-spilled variable should return -1");
    }

    @Test
    @DisplayName("Should throw exception for null variable")
    void testNullVariableException() {
        assertThrows(IllegalArgumentException.class, () -> adapter.allocateRegister(null));
        assertThrows(IllegalArgumentException.class, () -> adapter.getRegister(null));
        assertThrows(IllegalArgumentException.class, () -> adapter.isSpilled(null));
        assertThrows(IllegalArgumentException.class, () -> adapter.freeRegister(null));
        assertThrows(IllegalArgumentException.class, () -> adapter.getStackOffset(null));
    }

    @Test
    @DisplayName("Should generate allocation report")
    void testAllocationReport() {
        adapter.reset();
        adapter.allocateRegister(var1);
        adapter.allocateRegister(var2);
        
        String report = adapter.generateAllocationReport();
        
        assertNotNull(report);
        assertTrue(report.contains("EP18R Register Allocator Adapter Report"));
        assertTrue(report.contains("Managed variables: 2"));
        assertTrue(report.contains("Allocated registers: 2"));
    }

    @Test
    @DisplayName("Should provide access to underlying EP18R allocator")
    void testEp18rAllocatorAccess() {
        adapter.reset();
        assertNotNull(adapter.getEp18rAllocator());
        assertTrue(adapter.getEp18rAllocator() instanceof LinearScanAllocator);
    }

    @Test
    @DisplayName("Should track managed variables")
    void testManagedVariables() {
        adapter.allocateRegister(var1);
        adapter.allocateRegister(var2);
        
        var managedVars = adapter.getManagedVariables();
        assertEquals(2, managedVars.size());
        assertTrue(managedVars.contains(var1));
        assertTrue(managedVars.contains(var2));
    }

    @Test
    @DisplayName("Should map variable symbols to names")
    void testVariableSymbolMapping() {
        adapter.reset();
        adapter.allocateRegister(var1);
        
        VariableSymbol retrieved = adapter.getVariableSymbol("x");
        assertEquals(var1, retrieved);
        
        VariableSymbol unnamed = new VariableSymbol(null);
        adapter.allocateRegister(unnamed);
        VariableSymbol retrievedUnnamed = adapter.getVariableSymbol("var1");
        assertEquals(unnamed, retrievedUnnamed);
    }

    @Test
    @DisplayName("Should handle toString correctly")
    void testToString() {
        adapter.reset();
        adapter.allocateRegister(var1);
        adapter.allocateRegister(var2);
        
        String result = adapter.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("EP18RRegisterAllocatorAdapter"));
        assertTrue(result.contains("managedVariables=2"));
        assertTrue(result.contains("allocatedRegisters=2"));
    }
}