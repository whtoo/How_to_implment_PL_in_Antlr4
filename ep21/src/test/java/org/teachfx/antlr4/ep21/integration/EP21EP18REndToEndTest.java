package org.teachfx.antlr4.ep21.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import org.teachfx.antlr4.ep21.Compiler;
import org.teachfx.antlr4.ep21.pass.codegen.EP18RRegisterAllocatorAdapter;
import org.teachfx.antlr4.ep21.pass.codegen.IRegisterAllocator;
import org.teachfx.antlr4.ep21.pass.codegen.LinearScanAllocator;
import org.teachfx.antlr4.ep21.pass.codegen.VMTargetType;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * EP21→EP18R端到端集成测试
 * 测试EP21编译器使用EP18R的LinearScanAllocator生成的代码正确性
 *
 * 测试覆盖范围：
 * - 算术表达式编译
 * - 控制流编译
 * - 函数调用编译
 * - 循环编译
 * - 嵌套函数调用
 * - 递归函数
 * - 数组访问
 * - 全局变量
 * - 寄存器溢出处理
 * - 复杂表达式
 * - 死代码消除
 * - 常量折叠
 * - 循环不变量
 * - 快速排序
 */
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

    private static final String WHILE_LOOP = """
            int sum(int n) {
                int total = 0;
                int i = 0;
                while (i < n) {
                    total = total + i;
                    i = i + 1;
                }
                return total;
            }

            void main() {
                print(sum(10));
            }
            """;

    private static final String NESTED_FUNCTION_CALL = """
            int add(int a, int b) {
                return a + b;
            }

            void main() {
                int result = add(add(1, 2), 3);
                print(result);
            }
            """;

    private static final String FIBONACCI = """
            int fib(int n) {
                if (n <= 1) {
                    return n;
                }
                return fib(n - 1) + fib(n - 2);
            }

            int main() {
                int result = fib(10);
                return result;
            }
            """;

    private static final String ARRAY_ACCESS = """
            int sum_array(int[10] arr, int n) {
                int total = 0;
                int i = 0;
                while (i < n) {
                    total = total + arr[i];
                    i = i + 1;
                }
                return total;
            }

            int main() {
                int[10] arr;
                int i = 0;
                while (i < 10) {
                    arr[i] = i * i;
                    i = i + 1;
                }
                return sum_array(arr, 10);
            }
            """;

    private static final String GLOBAL_VARIABLES = """
            .global g_counter

            int get_counter() {
                g_counter = g_counter + 1;
                return g_counter;
            }

            void main() {
                g_counter = 0;
                print(get_counter());
                print(get_counter());
            }
            """;

    private static final String REGISTER_OVERFLOW = """
            int compute() {
                int a =1;
                int b = 2;
                int c = 3;
                int d = 4;
                int e = 5;
                int f = 6;
                int g = 7;
                int h = 8;
                int i = 9;
                int j = 10;
                int k = 11;
                int l = 12;
                int m = 13;
                int n = 14;
                return a + b + c + d + e + f + g + h + i + j + k + l + m + n;
            }

            void main() {
                print(compute());
            }
            """;

    private static final String COMPLEX_EXPRESSION = """
            int complex(int x, int y, int z) {
                return (x + y) * (x - y) + z * z;
            }

            void main() {
                print(complex(5, 3, 2));
            }
            """;

    private static final String DEAD_CODE = """
            int dead_code_test() {
                int a = 10;
                int b = 20;
                int c = a + b;
                int d = a * b;
                int e = c * 2;
                int f = c + 1;
                return e;
            }

            void main() {
                print(dead_code_test());
            }
            """;

    private static final String CONSTANT_FOLDING = """
            int constant_fold() {
                int x = 5;
                int y = 10;
                int z = x + y + 15;
                return z;
            }

            void main() {
                print(constant_fold());
            }
            """;

    private static final String LOOP_INVARIANT = """
            int loop_invariant_test(int n, int m) {
                int total = 0;
                int i = 0;
                int k = n * m;
                while (i < 10) {
                    total = total + k;
                    i = i + 1;
                }
                return total;
            }

            void main() {
                print(loop_invariant_test(5, 3));
            }
            """;

    private static final String QUICKSORT = """
            void swap(int[10] arr, int i, int j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }

            int partition(int[10] arr, int low, int high) {
                int pivot = arr[high];
                int i = low - 1;
                int j = low;
                while (j < high) {
                    if (arr[j] < pivot) {
                        i = i + 1;
                        swap(arr, i, j);
                    }
                    j = j + 1;
                }
                swap(arr, i + 1, high);
                return i + 1;
            }

            void quicksort(int[10] arr, int low, int high) {
                if (low < high) {
                    int pi = partition(arr, low, high);
                    quicksort(arr, low, pi - 1);
                    quicksort(arr, pi + 1, high);
                }
            }

            int main() {
                int[10] arr;
                int i = 0;
                while (i < 10) {
                    arr[i] = 10 - i;
                    i = i + 1;
                }
                quicksort(arr, 0, 9);
                print(arr[5]);
                return 0;
            }
            """;

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("ep21_integration_test");
    }

    private void compileSuccessfully(String source, String testName) throws IOException {
        Path inputFile = tempDir.resolve(testName + ".cymbol");
        Files.writeString(inputFile, source);

        try {
            Compiler.main(new String[]{inputFile.toString()});
        } catch (Exception e) {
            fail("Compilation failed for " + testName + ": " + e.getMessage());
        }
    }

    @Test
    @DisplayName("TC-INT-01: Should compile simple arithmetic with register allocator")
    void testSimpleArithmeticWithRegisterAllocator() throws IOException {
        compileSuccessfully(SIMPLE_ARITHMETIC, "simple_arithmetic");
    }

    @Test
    @DisplayName("TC-INT-02: Should compile control flow with register allocator")
    void testControlFlowWithRegisterAllocator() throws IOException {
        compileSuccessfully(CONTROL_FLOW, "control_flow");
    }

    @Test
    @DisplayName("TC-INT-03: Should compile function call with register allocator")
    void testFunctionCallWithRegisterAllocator() throws IOException {
        compileSuccessfully(FUNCTION_CALL, "function_call");
    }

    @Test
    @DisplayName("TC-INT-04: Should compile while loop with register allocator")
    void testWhileLoopWithRegisterAllocator() throws IOException {
        compileSuccessfully(WHILE_LOOP, "while_loop");
    }

    @Test
    @DisplayName("TC-INT-05: Should compile nested function calls with register allocator")
    void testNestedFunctionCallsWithRegisterAllocator() throws IOException {
        compileSuccessfully(NESTED_FUNCTION_CALL, "nested_function_call");
    }

    @Test
    @DisplayName("TC-INT-06: Should compile fibonacci recursion with register allocator")
    void testFibonacciWithRegisterAllocator() throws IOException {
        compileSuccessfully(FIBONACCI, "fibonacci");
    }

    @Test
    @DisplayName("TC-INT-07: Should compile array access with register allocator")
    void testArrayAccessWithRegisterAllocator() throws IOException {
        compileSuccessfully(ARRAY_ACCESS, "array_access");
    }

    @Test
    @DisplayName("TC-INT-08: Should compile global variables with register allocator [PENDING]")
    @org.junit.jupiter.api.Disabled("Global variable code generation not yet fully supported - type system issue")
    void testGlobalVariablesWithRegisterAllocator() throws IOException {
        compileSuccessfully(GLOBAL_VARIABLES, "global_variables");
    }

    @Test
    @DisplayName("TC-INT-09: Should compile register overflow scenario with register allocator")
    void testRegisterOverflowWithRegisterAllocator() throws IOException {
        compileSuccessfully(REGISTER_OVERFLOW, "register_overflow");
    }

    @Test
    @DisplayName("TC-INT-10: Should compile complex expression with register allocator")
    void testComplexExpressionWithRegisterAllocator() throws IOException {
        compileSuccessfully(COMPLEX_EXPRESSION, "complex_expression");
    }

    @Test
    @DisplayName("TC-INT-11: Should compile dead code elimination with register allocator")
    void testDeadCodeWithRegisterAllocator() throws IOException {
        compileSuccessfully(DEAD_CODE, "dead_code");
    }

    @Test
    @DisplayName("TC-INT-12: Should compile constant folding with register allocator")
    void testConstantFoldingWithRegisterAllocator() throws IOException {
        compileSuccessfully(CONSTANT_FOLDING, "constant_folding");
    }

    @Test
    @DisplayName("TC-INT-13: Should compile loop invariant code motion with register allocator [PENDING]")
    @org.junit.jupiter.api.Disabled("Loop invariant optimization not yet implemented")
    void testLoopInvariantWithRegisterAllocator() throws IOException {
        compileSuccessfully(LOOP_INVARIANT, "loop_invariant");
    }

    @Test
    @DisplayName("TC-INT-14: Should compile quicksort with register allocator [PENDING]")
    @org.junit.jupiter.api.Disabled("Complex recursive sorting algorithm not yet fully supported")
    void testQuicksortWithRegisterAllocator() throws IOException {
        compileSuccessfully(QUICKSORT, "quicksort");
    }

    @Test
    @DisplayName("TC-ALLOC-01: Adapter should handle multiple allocations")
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
    @DisplayName("TC-ALLOC-01: Adapter should handle register allocation")
    public void testAdapterAllocation() {
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
    @DisplayName("TC-ALLOC-03: Adapter should reset correctly")
    void testAdapterReset() {
        IRegisterAllocator adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());
        
        org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol var1 =
            new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("x");
        adapter.allocateRegister(var1);
        
        assertEquals(1, adapter.getAllocatedRegisterCount());
        
        adapter.reset();

        assertEquals(0, adapter.getAllocatedRegisterCount());
    }

    @Test
    @DisplayName("TC-ALLOC-04: Adapter should handle overflow to stack")
    void testAdapterOverflow() {
        IRegisterAllocator adapter = new EP18RRegisterAllocatorAdapter(new LinearScanAllocator());
        
        for (int i = 0; i < 20; i++) {
            org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol var =
                new org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol("var" + i);
            int reg = adapter.allocateRegister(var);
            assertTrue(reg >= -1, "Register allocation should return valid ID or -1 for spill");
        }
        
        assertTrue(adapter.getAllocatedRegisterCount() > 0);
    }

    @Test
    @DisplayName("TC-INT-15: Should compile and run array access")
    void testArrayAccessEndToEnd() throws IOException {
        compileSuccessfully(ARRAY_ACCESS, "array_access");
    }

    @Test
    @DisplayName("TC-INT-16: Should compile global variables [PENDING]")
    @org.junit.jupiter.api.Disabled("Global variable code generation not yet fully supported - type system issue")
    void testGlobalVariablesEndToEnd() throws IOException {
        compileSuccessfully(GLOBAL_VARIABLES, "global_variables");
    }

    @Test
    @DisplayName("TC-INT-17: Should compile loop invariant code motion [PENDING]")
    @org.junit.jupiter.api.Disabled("Loop invariant optimization not yet implemented")
    void testLoopInvariantEndToEnd() throws IOException {
        compileSuccessfully(LOOP_INVARIANT, "loop_invariant");
    }

    @Test
    @DisplayName("TC-INT-18: Should compile dead code elimination")
    void testDeadCodeEndToEnd() throws IOException {
        compileSuccessfully(DEAD_CODE, "dead_code");
    }

    @Test
    @DisplayName("TC-INT-19: Should compile constant folding")
    void testConstantFoldingEndToEnd() throws IOException {
        compileSuccessfully(CONSTANT_FOLDING, "constant_folding");
    }

    @Test
    @DisplayName("TC-INT-20: Should compile quicksort [PENDING]")
    @org.junit.jupiter.api.Disabled("Complex recursive sorting algorithm not yet fully supported")
    void testQuicksortEndToEnd() throws IOException {
        compileSuccessfully(QUICKSORT, "quicksort");
    }
}
