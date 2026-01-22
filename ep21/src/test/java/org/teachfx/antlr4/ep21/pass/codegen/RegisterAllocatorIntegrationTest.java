package org.teachfx.antlr4.ep21.pass.codegen;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.pass.codegen.LinearScanAllocator;
import static org.assertj.core.api.Assertions.assertThat;

public class RegisterAllocatorIntegrationTest {

    @Test
    public void testEP18RAdapterBasicAllocation() {
        EP18RRegisterAllocatorAdapter adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());

        adapter.reset();
        assertThat(adapter.getAllocatedRegisterCount()).isZero();
    }

    @Test
    public void testEP18RAdapterWithVariables() {
        EP18RRegisterAllocatorAdapter adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());

        adapter.reset();
        int reg1 = adapter.allocateRegister(new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("x"));
        int reg2 = adapter.allocateRegister(new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("y"));
        int reg3 = adapter.allocateRegister(new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("z"));

        assertThat(reg1).isNotEqualTo(reg2);
        assertThat(reg2).isNotEqualTo(reg3);
        assertThat(adapter.getAllocatedRegisterCount()).isEqualTo(3);
    }

    @Test
    public void testEP18RAdapterReset() {
        EP18RRegisterAllocatorAdapter adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());

        adapter.reset();
        adapter.allocateRegister(new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("a"));
        assertThat(adapter.getAllocatedRegisterCount()).isEqualTo(1);

        adapter.reset();
        assertThat(adapter.getAllocatedRegisterCount()).isZero();
    }

    @Test
    public void testEP18RAdapterSpilling() {
        EP18RRegisterAllocatorAdapter adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());

        adapter.reset();
        for (int i = 0; i < 10; i++) {
            adapter.allocateRegister(new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("var" + i));
        }

        assertThat(adapter.getAllocatedRegisterCount()).isPositive();
    }

    @Test
    public void testEP18RAdapterGenerateReport() {
        EP18RRegisterAllocatorAdapter adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());

        adapter.reset();
        adapter.allocateRegister(new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("testVar"));

        String report = adapter.generateAllocationReport();
        assertThat(report).contains("EP18R Register Allocator Adapter Report");
        assertThat(report).contains("Managed variables:");
    }
}
