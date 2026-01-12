package org.teachfx.antlr4.ep21.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import org.teachfx.antlr4.ep21.Compiler;
import org.teachfx.antlr4.ep21.pass.codegen.EP18RRegisterAllocatorAdapter;
import org.teachfx.antlr4.ep21.pass.codegen.IRegisterAllocator;
import org.teachfx.antlr4.ep21.pass.codegen.VMTargetType;
import org.teachfx.antlr4.ep18r.stackvm.codegen.LinearScanAllocator;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class EP21EP18REndToEndTest {

    private static final String SIMPLE_ARITHMETIC = """
            int add(int a, int b) {
                return a + b;
            }
            
            void main() {
                print(add(3, 4));
            }
            """;

    private static final String CONTROL_FLOW = """
            int max(int a, int b) {
                if (a > b) {
                    return a;
                } else {
                    return b;
                }
            }
            
            void main() {
                print(max(10, 20));
            }
            """;

    private static final String FUNCTION_CALL = """
            int square(int x) {
                return x * x;
            }
            
            void main() {
                int result = square(5);
                print(result);
            }
            """;

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("ep21_integration_test");
    }

    @Test
    @DisplayName("Should compile simple arithmetic with register allocator")
    void testSimpleArithmeticWithRegisterAllocator() throws IOException {
        Path inputFile = tempDir.resolve("simple_arithmetic.cymbol");
        Files.writeString(inputFile, SIMPLE_ARITHMETIC);
        
        try {
            Compiler.main(new String[]{inputFile.toString()});
        } catch (Exception e) {
            fail("Compilation failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should compile control flow with register allocator")
    void testControlFlowWithRegisterAllocator() throws IOException {
        Path inputFile = tempDir.resolve("control_flow.cymbol");
        Files.writeString(inputFile, CONTROL_FLOW);
        
        try {
            Compiler.main(new String[]{inputFile.toString()});
        } catch (Exception e) {
            fail("Compilation failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should compile function call with register allocator")
    void testFunctionCallWithRegisterAllocator() throws IOException {
        Path inputFile = tempDir.resolve("function_call.cymbol");
        Files.writeString(inputFile, FUNCTION_CALL);
        
        try {
            Compiler.main(new String[]{inputFile.toString()});
        } catch (Exception e) {
            fail("Compilation failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Adapter should handle multiple allocations")
    void testAdapterMultipleAllocations() {
        IRegisterAllocator adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());
        org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol var1 = 
            new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("x");
        org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol var2 = 
            new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("y");
        org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol var3 = 
            new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("z");

        int reg1 = adapter.allocateRegister(var1);
        int reg2 = adapter.allocateRegister(var2);
        int reg3 = adapter.allocateRegister(var3);

        assertNotEquals(reg1, reg2);
        assertNotEquals(reg2, reg3);
        assertNotEquals(reg1, reg3);
        assertEquals(3, adapter.getAllocatedRegisterCount());
    }

    @Test
    @DisplayName("Adapter should handle register allocation")
    void testAdapterAllocation() {
        IRegisterAllocator adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());
        
        for (int i = 0; i < 10; i++) {
            org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol var = 
                new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("var" + i);
            adapter.allocateRegister(var);
        }

        assertTrue(adapter.getAllocatedRegisterCount() > 0);
        assertTrue(adapter.getAllocatedRegisterCount() <= 13, "Should not exceed available registers");
    }

    @Test
    @DisplayName("Adapter should reset correctly")
    void testAdapterReset() {
        IRegisterAllocator adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());
        
        org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol var1 = 
            new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("x");
        adapter.allocateRegister(var1);
        
        assertEquals(1, adapter.getAllocatedRegisterCount());
        
        adapter.reset();
        
        assertEquals(0, adapter.getAllocatedRegisterCount());
    }
}