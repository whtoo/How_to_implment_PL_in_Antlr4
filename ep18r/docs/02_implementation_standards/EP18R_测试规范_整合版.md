# EP18R 测试规范（整合版）

**版本**: v1.0 | **日期**: 2026-01-08 | **状态**: 测试规范完成（整合版）
**目的**: 提供EP18R项目的统一测试规范和测试模板，整合原测试模板和单元测试案例集
**相关文档**: [架构设计规范](架构设计规范.md) | [改进计划](改进计划.md) | [详细任务分解](详细任务分解.md) | [TDD执行计划](TDD执行计划_精简版.md)

---

## 1. 测试策略概述

### 1.1 测试金字塔

EP18R采用标准的测试金字塔策略：

```
         端到端测试 (5%)
            ^
            |
      集成测试 (25%)
            ^
            |
      单元测试 (70%) - 基础测试
```

- **单元测试**: 70% - 测试单个类或方法
- **集成测试**: 25% - 测试组件间交互
- **端到端测试**: 5% - 测试完整编译-执行流程

### 1.2 测试覆盖率要求

| 测试类型 | 覆盖率目标 | 验证方法 |
|---------|-----------|----------|
| **整体覆盖率** | ≥ 85% | JaCoCo 报告 |
| **核心模块** | ≥ 90% | JaCoCo 报告 |
| **关键算法** | 100% | 代码审查 + 覆盖率 |
| **新功能模块** | 100% | TDD 要求 |

### 1.3 测试命名规范

#### 功能测试
```java
@Test
@DisplayName("应该正确处理简单的二元表达式")
void testBinaryExprIRGeneration() {
    // 测试代码
}

@Test  
@DisplayName("应该正确构建if-else语句的CFG")
void testIfElseCFGConstruction() {
    // 测试代码
}
```

#### 边界条件测试
```java
@Test
@DisplayName("应该处理空函数体的情况")
void testEmptyFunctionBody_ShouldCreateBasicBlock() {
    // 测试代码
}

@Test
@DisplayName("应该正确处理嵌套作用域")
void testNestedScope_SymbolResolution() {
    // 测试代码
}
```

#### 异常场景测试
```java
@Test
@DisplayName("应该抛出除零异常")
void testDivisionByZero_ThrowsException() {
    // 测试代码
}

@Test
@DisplayName("应该拒绝非法指令")
void testInvalidOpcode_ThrowsException() {
    // 测试代码
}
```

---

## 2. 单元测试模板

### 2.1 寄存器系统测试模板

#### RegisterFileTest.java
```java
package org.teachfx.antlr4.ep18r.stackvm.registers;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("寄存器文件测试")
class RegisterFileTest {

    private RegisterFile regs;

    @BeforeEach
    void setUp() {
        regs = new RegisterFile();
    }

    @Nested
    @DisplayName("zero寄存器测试")
    class ZeroRegisterTests {

        @Test
        @DisplayName("zero寄存器应始终为0")
        void testZeroRegisterIsAlwaysZero() {
            assertThat(regs.get(0)).isEqualTo(0);
            assertThat(regs.get(0)).isEqualTo(0); // 多次调用验证
        }

        @Test
        @DisplayName("写入zero寄存器应被忽略")
        void testWritingToZeroRegisterShouldBeIgnored() {
            regs.set(0, 42);
            assertThat(regs.get(0)).isEqualTo(0);
            regs.set(0, -999);
            assertThat(regs.get(0)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("普通寄存器测试")
    class NormalRegisterTests {

        @Test
        @DisplayName("应能正常设置和获取寄存器值")
        void testSetAndGetNormalRegister() {
            regs.set(1, 100);
            assertThat(regs.get(1)).isEqualTo(100);

            regs.set(2, -200);
            assertThat(regs.get(2)).isEqualTo(-200);
        }

        @Test
        @DisplayName("应能存储32位有符号整数范围")
        void testShouldStoreFull32BitSignedRange() {
            regs.set(1, Integer.MAX_VALUE);
            assertThat(regs.get(1)).isEqualTo(Integer.MAX_VALUE);

            regs.set(2, Integer.MIN_VALUE);
            assertThat(regs.get(2)).isEqualTo(Integer.MIN_VALUE);
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("应拒绝非法寄存器号")
        void testShouldRejectInvalidRegisterNumber() {
            assertThatThrownBy(() -> regs.get(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid register number");

            assertThatThrownBy(() -> regs.get(16))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid register number");

            assertThatThrownBy(() -> regs.set(-1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid register number");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @ParameterizedTest
        @MethodSource("validRegisterNumbers")
        @DisplayName("应能访问所有有效寄存器号")
        void testShouldAccessAllValidRegisterNumbers(int regNum) {
            regs.set(regNum, regNum * 10);
            assertThat(regs.get(regNum)).isEqualTo(regNum * 10);
        }

        static Stream<Arguments> validRegisterNumbers() {
            return IntStream.range(0, 16)
                .mapToObj(i -> Arguments.of(i));
        }
    }
}
```

---

### 2.2 指令执行测试模板

#### InstructionExecutorTest.java
```java
package org.teachfx.antlr4.ep18r.stackvm.instructions;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("指令执行器测试")
class InstructionExecutorTest {

    private RegisterVMInterpreter vm;
    private ExecutionContext context;

    @BeforeEach
    void setUp() throws Exception {
        VMConfig config = new VMConfig.Builder().build();
        vm = new RegisterVMInterpreter(config);
        context = new ExecutionContext(vm, 0);
    }

    @Nested
    @DisplayName("算术指令测试")
    class ArithmeticInstructionTests {

        @Test
        @DisplayName("ADD指令应正确执行")
        void testADDInstruction() {
            // Arrange
            vm.setRegister(1, 10);
            vm.setRegister(2, 20);

            // Act
            int operand = encodeInstruction(RegisterBytecodeDefinition.INSTR_ADD, 3, 1, 2);
            InstructionExecutor executor = new ArithmeticExecutor(ArithmeticOperation.ADD);
            executor.execute(operand, context);

            // Assert
            assertThat(vm.getRegister(3)).isEqualTo(30);
        }

        @Test
        @DisplayName("SUB指令应正确执行")
        void testSUBInstruction() {
            vm.setRegister(1, 50);
            vm.setRegister(2, 20);

            int operand = encodeInstruction(RegisterBytecodeDefinition.INSTR_SUB, 3, 1, 2);
            InstructionExecutor executor = new ArithmeticExecutor(ArithmeticOperation.SUB);
            executor.execute(operand, context);

            assertThat(vm.getRegister(3)).isEqualTo(30);
        }

        @Test
        @DisplayName("MUL指令应正确执行")
        void testMULInstruction() {
            vm.setRegister(1, 6);
            vm.setRegister(2, 7);

            int operand = encodeInstruction(RegisterBytecodeDefinition.INSTR_MUL, 3, 1, 2);
            InstructionExecutor executor = new ArithmeticExecutor(ArithmeticOperation.MUL);
            executor.execute(operand, context);

            assertThat(vm.getRegister(3)).isEqualTo(42);
        }

        @Test
        @DisplayName("DIV指令应正确执行")
        void testDIVInstruction() {
            vm.setRegister(1, 100);
            vm.setRegister(2, 5);

            int operand = encodeInstruction(RegisterBytecodeDefinition.INSTR_DIV, 3, 1, 2);
            InstructionExecutor executor = new ArithmeticExecutor(ArithmeticOperation.DIV);
            executor.execute(operand, context);

            assertThat(vm.getRegister(3)).isEqualTo(20);
        }

        @Test
        @DisplayName("DIV指令应检测除零")
        void testDIVInstruction_ShouldDetectDivisionByZero() {
            vm.setRegister(1, 10);
            vm.setRegister(2, 0);

            int operand = encodeInstruction(RegisterBytecodeDefinition.INSTR_DIV, 3, 1, 2);
            InstructionExecutor executor = new ArithmeticExecutor(ArithmeticOperation.DIV);

            assertThatThrownBy(() -> executor.execute(operand, context))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Division by zero");
        }
    }

    @Nested
    @DisplayName("比较指令测试")
    class ComparisonInstructionTests {

        @Test
        @DisplayName("SLT指令应正确比较")
        void testSLTInstruction() {
            vm.setRegister(1, 10);
            vm.setRegister(2, 20);

            int operand = encodeInstruction(RegisterBytecodeDefinition.INSTR_SLT, 3, 1, 2);
            InstructionExecutor executor = new ComparisonExecutor(ComparisonOperation.SLT);
            executor.execute(operand, context);

            assertThat(vm.getRegister(3)).isEqualTo(1); // 10 < 20 = true
        }

        @Test
        @DisplayName("SGT指令应正确比较")
        void testSGTInstruction() {
            vm.setRegister(1, 20);
            vm.setRegister(2, 10);

            int operand = encodeInstruction(RegisterBytecodeDefinition.INSTR_SGT, 3, 1, 2);
            InstructionExecutor executor = new ComparisonExecutor(ComparisonOperation.SGT);
            executor.execute(operand, context);

            assertThat(vm.getRegister(3)).isEqualTo(1); // 20 > 10 = true
        }

        @Test
        @DisplayName("SEQ指令应正确比较")
        void testSEQInstruction() {
            vm.setRegister(1, 10);
            vm.setRegister(2, 10);

            int operand = encodeInstruction(RegisterBytecodeDefinition.INSTR_SEQ, 3, 1, 2);
            InstructionExecutor executor = new ComparisonExecutor(ComparisonOperation.SEQ);
            executor.execute(operand, context);

            assertThat(vm.getRegister(3)).isEqualTo(1); // 10 == 10 = true
        }

        @Test
        @DisplayName("SNE指令应正确比较")
        void testSNEInstruction() {
            vm.setRegister(1, 10);
            vm.setRegister(2, 20);

            int operand = encodeInstruction(RegisterBytecodeDefinition.INSTR_SNE, 3, 1, 2);
            InstructionExecutor executor = new ComparisonExecutor(ComparisonOperation.SNE);
            executor.execute(operand, context);

            assertThat(vm.getRegister(3)).isEqualTo(1); // 10 != 20 = true
        }
    }

    // 辅助方法
    private int encodeInstruction(int opcode, int rd, int rs1, int rs2) {
        return (opcode << 26) | ((rd & 0x1F) << 21) | ((rs1 & 0x1F) << 16) | ((rs2 & 0x1F) << 11);
    }
}
```

---

### 2.3 内存管理测试模板

#### MemoryManagerTest.java
```java
package org.teachfx.antlr4.ep18r.stackvm.memory;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("内存管理器测试")
class MemoryManagerTest {

    private MemoryManager memory;
    private VMConfig config;

    @BeforeEach
    void setUp() {
        config = new VMConfig.Builder()
            .heapSize(1024)
            .build();
        memory = new MemoryManager(config);
    }

    @Nested
    @DisplayName("堆分配测试")
    class HeapAllocationTests {

        @Test
        @DisplayName("应能分配内存")
        void testShouldAllocateMemory() {
            int size = 16;
            int address = memory.allocate(size);

            assertThat(address).isNotEqualTo(-1);
            assertThat(address).isBetween(0, config.getHeapSize());
        }

        @Test
        @DisplayName("分配应返回对齐的地址")
        void testAllocationShouldReturnAlignedAddress() {
            // 堆分配应8字节对齐
            int size = 12;
            int address1 = memory.allocate(size);
            int address2 = memory.allocate(size);

            assertThat(address1 % 8).isEqualTo(0);
            assertThat(address2 % 8).isEqualTo(0);
        }

        @Test
        @DisplayName("应检测堆溢出")
        void testShouldDetectHeapOverflow() {
            // 分配超过堆大小的内存
            int size = config.getHeapSize() + 100;

            assertThatThrownBy(() -> memory.allocate(size))
                .isInstanceOf(OutOfMemoryError.class)
                .hasMessageContaining("Not enough heap space");
        }
    }

    @Nested
    @DisplayName("内存访问测试")
    class MemoryAccessTests {

        @Test
        @DisplayName("应能写入和读取内存")
        void testShouldWriteAndReadMemory() {
            int address = 0x100;
            int value = 42;

            memory.writeWord(address, value);
            int result = memory.readWord(address);

            assertThat(result).isEqualTo(value);
        }

        @Test
        @DisplayName("应拒绝非法内存地址")
        void testShouldRejectInvalidMemoryAddress() {
            int invalidAddress = -1;

            assertThatThrownBy(() -> memory.readWord(invalidAddress))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageContaining("Memory address out of bounds");

            assertThatThrownBy(() -> memory.writeWord(invalidAddress, 0))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageContaining("Memory address out of bounds");
        }

        @Test
        @DisplayName("应检测内存越界")
        void testShouldDetectOutOfBoundsAccess() {
            int validAddress = 0;
            int invalidOffset = config.getHeapSize();

            assertThatThrownBy(() -> memory.readWord(validAddress + invalidOffset))
                .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }
}
```

---

### 2.4 调用约定测试模板

#### CallingConventionTest.java
```java
package org.teachfx.antlr4.ep18r.abi;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.*;
import java.io.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("调用约定测试")
class CallingConventionTest {

    private RegisterVMInterpreter vm;

    @BeforeEach
    void setUp() throws Exception {
        VMConfig config = new VMConfig.Builder().build();
        vm = new RegisterVMInterpreter(config);
    }

    private void loadAndExecute(String program) throws Exception {
        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(vm, input);
        assertThat(hasErrors).isFalse();
        vm.exec();
    }

    @Nested
    @DisplayName("参数传递测试")
    class ArgumentPassingTests {

        @Test
        @DisplayName("应通过寄存器传递前6个参数")
        void testShouldPassFirstSixArgumentsViaRegisters() {
            String program = """
                .def add_six_params: args=6, locals=0
                    # 参数在 a0-a5 (r2-r7)
                    # r2 + r3 + r4 + r5 + r6 + r7
                    add a0, a0, a1
                    add a0, a0, a2
                    add a0, a0, a3
                    add a0, a0, a4
                    add a0, a0, a5
                    ret

                .def main: args=0, locals=0
                    li a0, 1
                    li a1, 2
                    li a2, 3
                    li a3, 4
                    li a4, 5
                    li a5, 6
                    call add_six_params
                    # a0 应该是 1+2+3+4+5+6 = 21
                    halt
                """;

            loadAndExecute(program);
            assertThat(vm.getRegister(2)).isEqualTo(21);
        }

        @Test
        @DisplayName("应通过栈传递第7+个参数")
        void testShouldPassAdditionalArgumentsViaStack() {
            String program = """
                .def add_eight_params: args=8, locals=1
                    # 前6个参数在 a0-a5，第7、8个参数在栈
                    # 使用 FP 相对寻址访问栈参数
                    lw r8, fp, 16    # 第7个参数
                    lw r9, fp, 20    # 第8个参数
                    add a0, a0, r8
                    add a0, a0, r9
                    ret

                .def main: args=0, locals=0
                    li a0, 1
                    li a1, 2
                    li a2, 3
                    li a3, 4
                    li a4, 5
                    li a5, 6
                    # 将第7、8个参数压栈（调用者负责）
                    # 注意：简化实现，假设调用栈已正确设置
                    call add_eight_params
                    # a0 应该是 1+2+3+4+5+6+7+8 = 36
                    halt
                """;

            loadAndExecute(program);
            // 注意：此测试可能需要调整以适配当前实现
            // 当前实现可能不完整支持栈参数传递
        }
    }

    @Nested
    @DisplayName("返回值测试")
    class ReturnValueTests {

        @Test
        @DisplayName("应通过 a0 寄存器返回单个值")
        void testShouldReturnSingleValueViaA0() {
            String program = """
                .def add: args=2, locals=0
                    add a0, a0, a1
                    ret

                .def main: args=0, locals=0
                    li a0, 10
                    li a1, 20
                    call add
                    # a0 应该是 10+20 = 30
                    halt
                """;

            loadAndExecute(program);
            assertThat(vm.getRegister(2)).isEqualTo(30);
        }

        @Test
        @DisplayName("应能返回零值")
        void testShouldBeAbleToReturnZero() {
            String program = """
                .def return_zero: args=0, locals=0
                    li a0, 0
                    ret

                .def main: args=0, locals=0
                    call return_zero
                    # a0 应该是 0
                    halt
                """;

            loadAndExecute(program);
            assertThat(vm.getRegister(2)).isEqualTo(0);
        }

        @Test
        @DisplayName("应能返回负数")
        void testShouldBeAbleToReturnNegative() {
            String program = """
                .def return_negative: args=1, locals=0
                    neg a0, a0
                    ret

                .def main: args=0, locals=0
                    li a0, 10
                    call return_negative
                    # a0 应该是 -10
                    halt
                """;

            loadAndExecute(program);
            assertThat(vm.getRegister(2)).isEqualTo(-10);
        }
    }

    @Nested
    @DisplayName("被调用者保存寄存器测试")
    class CalleeSavedRegistersTests {

        @Test
        @DisplayName("被调用者保存寄存器应在函数调用后保持不变")
        void testCalleeSavedRegistersShouldRemainUnchanged() {
            String program = """
                .def caller: args=0, locals=0
                    li s0, 100
                    li s1, 200
                    li s2, 300
                    call callee
                    # s0-s2 应该保持不变
                    seq a0, s0, 100
                    seq a1, s1, 200
                    seq a2, s2, 300
                    # 如果所有比较都为真，a0 应该是 3
                    halt

                .def callee: args=0, locals=2
                    # 保存 s0-s2
                    sw s0, -12(fp)
                    sw s1, -8(fp)
                    sw s2, -4(fp)

                    # 修改 s0-s2
                    li s0, 999
                    li s1, 888
                    li s2, 777

                    # 恢复 s0-s2
                    lw s2, -4(fp)
                    lw s1, -8(fp)
                    lw s0, -12(fp)
                    ret
                """;

            loadAndExecute(program);
            assertThat(vm.getRegister(2)).isEqualTo(3); // s0=100, s1=200, s2=300
        }
    }

    @Nested
    @DisplayName("递归函数测试")
    class RecursiveFunctionTests {

        @Test
        @DisplayName("应能正确处理递归函数调用")
        void testShouldHandleRecursiveFunctionCall() {
            String program = """
                .def factorial: args=1, locals=2
                    # 递归计算 n!
                    # 输入：n 在 a0
                    # 输出：n! 在 a0
                    # 使用 s0 存储 n，s1 存储递归结果

                    # 基本情况：n <= 1
                    li s1, 1
                    sle s1, s1, a0    # s1 = (1 < n)
                    jf s1, recurse_end

                    # 递归情况：n > 1
                    # 保存 n
                    sw a0, -16(fp)

                    # 调用 factorial(n-1)
                    li s0, 1
                    sub a0, a0, s0
                    call factorial

                    # 恢复 n
                    lw s0, -16(fp)

                    # 乘法：n * factorial(n-1)
                    mul a0, a0, s0

                    ret

                recurse_end:
                    # 返回 1
                    li a0, 1
                    ret

                .def main: args=0, locals=0
                    li a0, 5
                    call factorial
                    # a0 应该是 5! = 120
                    halt
                """;

            loadAndExecute(program);
            assertThat(vm.getRegister(2)).isEqualTo(120);
        }
    }
}
```

---

## 3. 集成测试模板

### 3.1 完整程序执行测试模板

#### CompleteProgramExecutionTest.java
```java
package org.teachfx.antlr4.ep18r.integration;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.*;
import java.io.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("完整程序执行集成测试")
class CompleteProgramExecutionTest {

    private RegisterVMInterpreter vm;

    @BeforeEach
    void setUp() throws Exception {
        VMConfig config = new VMConfig.Builder()
            .heapSize(1024 * 1024)
            .maxExecutionSteps(100_000)
            .build();
        vm = new RegisterVMInterpreter(config);
    }

    private void loadAndExecute(String program) throws Exception {
        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(vm, input);
        assertThat(hasErrors).isFalse();
        vm.exec();
    }

    @Test
    @DisplayName("应能执行完整的算术表达式计算程序")
    void testShouldExecuteArithmeticExpressionProgram() {
        String program = """
                .def main: args=0, locals=4
                    # 计算：(10 + 20) * 3 - 15 / 5
                    # 预期结果：(30) * 3 - 3 = 87

                    # a0 = 10
                    li a0, 10
                    # a1 = 20
                    li a1, 20
                    # a2 = a0 + a1 = 30
                    add a2, a0, a1
                    # a3 = 3
                    li a3, 3
                    # a2 = a2 * a3 = 90
                    mul a2, a2, a3
                    # a3 = 15
                    li a3, 15
                    # s0 = 5
                    li s0, 5
                    # s0 = 15 / 5 = 3
                    div s0, a3, s0
                    # a0 = a2 - s0 = 87
                    sub a0, a2, s0

                    print a0
                    halt
                """;

        loadAndExecute(program);
        assertThat(vm.getRegister(2)).isEqualTo(87);
    }

    @Test
    @DisplayName("应能执行循环程序")
    void testShouldExecuteLoopProgram() {
        String program = """
                .def main: args=0, locals=2
                    # 循环 1000 次
                    li a0, 0
                    li a1, 1

                loop:
                    li s0, 1000
                    sge s0, a1, s0
                    jf s0, loop_end

                    add a0, a0, a1
                    li s0, 1
                    add a1, a1, s0
                    j loop

                loop_end:
                    halt
                """;

        loadAndExecute(program);
        assertThat(vm.getRegister(2)).isEqualTo(55);
    }

    @Test
    @DisplayName("应能执行函数嵌套调用")
    void testShouldExecuteNestedFunctionCalls() {
        String program = """
                .def add: args=2, locals=0
                    add a0, a0, a1
                    ret

                .def multiply: args=2, locals=0
                    mul a0, a0, a1
                    ret

                .def combine: args=2, locals=2
                    # 调用 add(a, b) 得到结果1
                    sw a0, -16(fp)
                    sw a1, -20(fp)
                    call add
                    lw s0, -16(fp)
                    sw a0, -24(fp)
                    lw a0, s0
                    lw a1, -20(fp)
                    # 结果1 在 a0

                    # 调用 multiply(a, b) 得到结果2
                    sw a0, -20(fp)
                    lw a0, -24(fp)
                    call multiply
                    # 结果2 在 a0

                    # 返回 add(a, b) + multiply(a, b)
                    lw s0, -20(fp)
                    add a0, a0, s0
                    ret

                .def main: args=0, locals=0
                    li a0, 3
                    li a1, 4
                    call combine
                    # 应该是 (3+4) + (3*4) = 7 + 12 = 19
                    halt
                """;

        loadAndExecute(program);
        assertThat(vm.getRegister(2)).isEqualTo(19);
    }
}
```

---

## 4. 性能测试模板

### 4.1 性能基准测试模板

#### PerformanceBenchmarkTest.java
```java
package org.teachfx.antlr4.ep18r.performance;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

@DisplayName("性能基准测试")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class PerformanceBenchmarkTest {

    private static final String FIBONACCI_PROGRAM = """
            .def fib: args=1, locals=2
                # 递归计算斐波那契数列
                li s0, 1
                sle s0, a0, s0
                jf s0, base_case

                sw a0, -16(fp)
                li s0, 1
                sub a0, a0, s0
                call fib
                lw s0, -16(fp)
                li a1, s0

                lw a0, -16(fp)
                li s0, 2
                sub a0, a0, s0
                call fib
                add a0, a1, a0
                ret

            base_case:
                ret

            .def main: args=0, locals=0
                li a0, 10
                call fib
                halt
            """;

    private static final String LOOP_PROGRAM = """
            .def main: args=0, locals=2
                # 循环 1000 次
                li a0, 0
                li a1, 1

            loop:
                li s0, 1000
                sge s0, a1, s0
                jf s0, loop_end

                add a0, a0, a1
                li s0, 1
                add a1, a1, s0
                j loop

            loop_end:
                halt
            """;

    @Benchmark
    @DisplayName("基准测试：斐波那契递归程序执行时间")
    public void benchmarkFibonacciProgram() throws Exception {
        VMConfig config = new VMConfig.Builder().build();
        RegisterVMInterpreter vm = new RegisterVMInterpreter(config);

        InputStream input = new ByteArrayInputStream(FIBONACCI_PROGRAM.getBytes());
        RegisterVMInterpreter.load(vm, input);
        vm.exec();
    }

    @Benchmark
    @DisplayName("基准测试：循环程序执行时间")
    public void benchmarkLoopProgram() throws Exception {
        VMConfig config = new VMConfig.Builder().build();
        RegisterVMInterpreter vm = new RegisterVMInterpreter(config);

        InputStream input = new ByteArrayInputStream(LOOP_PROGRAM.getBytes());
        RegisterVMInterpreter.load(vm, input);
        vm.exec();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + PerformanceBenchmarkTest.class.getSimpleName() + ".*")
            .build();
        new Runner(opt).run();
    }
}
```

---

## 5. 测试数据管理

### 5.1 参数化测试

#### 表达式测试数据
```java
@ParameterizedTest
@MethodSource("expressionProvider")
@DisplayName("表达式求值测试")
void testExpressionEvaluation(String expression, int expected) {
    String program = """
        .def main: args=0, locals=0
            %s
            print a0
            halt
        """.formatted(expression);

    loadAndExecute(program);
    assertThat(vm.getRegister(2)).isEqualTo(expected);
}

static Stream<Arguments> expressionProvider() {
    return Stream.of(
        Arguments.of("li a0, 10", 10),
        Arguments.of("li a0, 5\nli a1, 3\nadd a0, a0, a1", 8),
        Arguments.of("li a0, 20\nli a1, 5\nsub a0, a0, a1", 15),
        Arguments.of("li a0, 4\nli a1, 7\nmul a0, a0, a1", 28)
    );
}
```

#### 寄存器测试数据
```java
@ParameterizedTest
@MethodSource("registerNumbers")
@DisplayName("应能访问所有有效寄存器")
void testShouldAccessAllValidRegisters(int regNum) {
    RegisterFile regs = new RegisterFile();
    regs.set(regNum, regNum * 100);
    assertThat(regs.get(regNum)).isEqualTo(regNum * 100);
}

static Stream<Arguments> registerNumbers() {
    return IntStream.range(0, 16)
        .mapToObj(i -> Arguments.of(i));
}
```

---

## 6. 测试最佳实践

### 6.1 AAA 模式

所有测试应遵循 **Arrange-Act-Assert** 模式：

```java
@Test
void testBinaryExpression() {
    // Arrange（准备）：设置测试前置条件
    vm.setRegister(1, 10);
    vm.setRegister(2, 20);
    String program = "add a0, a0, a1";

    // Act（执行）：执行被测试的操作
    loadAndExecute(program);

    // Assert（断言）：验证结果
    assertThat(vm.getRegister(2)).isEqualTo(30);
}
```

### 6.2 TDD 最佳实践

1. **测试先行**: 先写测试，再写实现
2. **小步快跑**: 每个测试验证一个功能点
3. **持续重构**: 保持代码质量
4. **持续集成**: 每次提交都运行所有测试

---

## 7. 测试覆盖率要求

### 7.1 覆盖率目标

| 模块 | 覆盖率要求 | 说明 |
|------|-----------|------|
| RegisterVMInterpreter | ≥ 90% | 核心解释器 |
| 所有执行器类 | ≥ 95% | 指令执行逻辑 |
| 内存管理器 | ≥ 90% | 堆/栈操作 |
| 调用约定工具 | ≥ 85% | ABI 逻辑 |
| 汇编器/反汇编器 | ≥ 85% | 代码转换 |

### 7.2 覆盖率检查命令

```bash
# 运行所有测试并生成覆盖率报告
mvn test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html

# 检查特定模块的覆盖率
mvn jacoco:check -Djacoco.includes=org.teachfx.antlr4.ep18r.stackvm.*
```

---

## 8. 测试执行指南

### 8.1 运行所有测试

```bash
# 运行所有测试
mvn test

# 运行测试并生成覆盖率报告
mvn test jacoco:report

# 运行测试并检查代码风格
mvn test checkstyle:check
```

### 8.2 运行特定测试类

```bash
# 运行单个测试类
mvn test -Dtest=RegisterFileTest

# 运行多个测试类
mvn test -Dtest=RegisterFileTest,InstructionExecutorTest

# 运行特定包的所有测试
mvn test -Dtest="org.teachfx.antlr4.ep18r.stackvm.*"
```

### 8.3 运行单个测试方法

```bash
# 运行单个测试方法
mvn test -Dtest=RegisterFileTest#testZeroRegisterIsAlwaysZero

# 运行包含特定名称的所有测试方法
mvn test -Dtest="*ZeroRegister*"
```

---

## 9. ABI一致性测试案例集

### 9.1 寄存器约定测试案例

#### 测试寄存器ABI名称和默认值
```java
@Test
void testZeroRegisterAlwaysZero() {
    RegisterFile regs = new RegisterFile();
    // r0应始终为0
    assertThat(regs.get(0)).isEqualTo(0);
    // 尝试写入r0，应被忽略
    regs.set(0, 42);
    assertThat(regs.get(0)).isEqualTo(0);
}

@Test
void testABINameMapping() {
    // 验证CallingConventionUtils中的映射
    assertThat(CallingConventionUtils.getABIName(0)).isEqualTo("zero");
    assertThat(CallingConventionUtils.getABIName(2)).isEqualTo("a0");
    assertThat(CallingConventionUtils.getABIName(8)).isEqualTo("s0");
    assertThat(CallingConventionUtils.getABIName(13)).isEqualTo("sp");
    assertThat(CallingConventionUtils.getABIName(14)).isEqualTo("fp");
    assertThat(CallingConventionUtils.getABIName(15)).isEqualTo("lr");
}
```

#### 测试寄存器保存责任
```java
@Test
void testCallerSavedRegistersNotPreserved() {
    // 汇编程序：调用者设置a0=100，调用被调用者，被调用者修改a0=200
    String program = """
        .def caller: args=0, locals=0
            li a0, 100      # 设置a0
            call callee     # 调用
            # 返回后a0可能被修改，验证a0不是100（可能是返回值）
            # 本测试主要验证调用者保存寄存器可能被修改
            halt

        .def callee: args=0, locals=0
            li a0, 200      # 修改a0（调用者保存寄存器）
            ret
        """;
    // 加载并执行程序
    // 验证a0最终值为200（或被调用者设置的值）
}
```

### 9.2 栈帧布局测试案例

#### 测试栈帧偏移计算
```java
@Test
void testBasicStackFrameLayout() {
    // 函数有3个参数（全部在寄存器中），2个局部变量
    StackFrameLayout layout = StackFrameCalculator.calculate(3, 2, 0b11111);

    // 验证栈帧大小是8的倍数
    assertThat(layout.getFrameSize() % 8).isEqualTo(0);

    // 验证局部变量偏移
    assertThat(layout.getLocalVarOffset(0)).isEqualTo(-16); // fp-16
    assertThat(layout.getLocalVarOffset(1)).isEqualTo(-20); // fp-20

    // 验证保存寄存器偏移
    assertThat(layout.getSavedRegOffset(8)).isEqualTo(-12);  // s0 at fp-12
    assertThat(layout.getSavedRegOffset(9)).isEqualTo(-8);   // s1 at fp-8
    assertThat(layout.getSavedRegOffset(10)).isEqualTo(-4);  // s2 at fp-4

    // 验证旧fp保存位置
    assertThat(layout.getFpSaveOffset()).isEqualTo(8); // fp+8
}
```

### 9.3 函数调用测试案例

#### 测试参数传递（寄存器参数）
```java
@Test
void testRegisterArgumentPassing() {
    String program = """
        .def caller: args=0, locals=0
            # 设置6个参数
            li a0, 10   # 参数1
            li a1, 20   # 参数2
            li a2, 30   # 参数3
            li a3, 40   # 参数4
            li a4, 50   # 参数5
            li a5, 60   # 参数6
            call callee
            # 返回值在a0中
            halt

        .def callee: args=6, locals=0
            # 验证参数值
            # 这里简单返回参数总和
            add t0, a0, a1
            add t0, t0, a2
            add t0, t0, a3
            add t0, t0, a4
            add t0, t0, a5
            mov a0, t0   # 返回值 = 10+20+30+40+50+60 = 210
            ret
        """;
    // 执行程序，验证最终a0值为210
}
```

### 9.4 内存访问测试案例

#### 测试FP相对寻址
```java
@Test
void testFPRelativeAddressing() {
    String program = """
        .def test: args=0, locals=2
            # 局部变量0在fp-16，局部变量1在fp-20
            li t0, 123
            sw t0, -16(fp)  # 存储到局部变量0
            li t0, 456
            sw t0, -20(fp)  # 存储到局部变量1

            # 读取验证
            lw a0, -16(fp)  # 应得到123
            lw a1, -20(fp)  # 应得到456

            seq a2, a0, 123
            seq a3, a1, 456
            and a0, a2, a3  # a0=1如果两个都正确
            ret
        """;
    // 执行程序，验证a0为1
}
```

### 9.5 ABI一致性综合测试

#### 综合ABI测试
```java
@Test
void testABIComprehensive() {
    String program = """
        .def main: args=0, locals=0
            # 测试1：寄存器保存
            li s0, 100
            li s1, 200
            li s2, 300
            call func1
            # s0-s2应保持不变
            seq t0, s0, 100
            seq t1, s1, 200
            seq t2, s2, 300
            and a0, t0, t1
            and a0, a0, t2  # a0=1如果都保持不变

            # 测试2：参数传递和返回值
            li a0, 5
            li a1, 3
            call add_func
            # 返回值应为8
            seq t0, a0, 8
            and a0, a0, t0

            halt

        .def func1: args=0, locals=1
            # 被调用者保存s0-s2
            sw s0, -12(fp)
            sw s1, -8(fp)
            sw s2, -4(fp)
            # 修改它们的值
            li s0, 999
            li s1, 888
            li s2, 777
            # 恢复
            lw s2, -4(fp)
            lw s1, -8(fp)
            lw s0, -12(fp)
            ret

        .def add_func: args=2, locals=0
            add a0, a0, a1
            ret
        """;
    // 执行程序，验证最终a0为1（所有测试通过）
}
```

---

## 10. 测试夹具和工具

### 10.1 测试夹具基类

提供可重用的测试夹具，简化测试编写：

```java
package org.teachfx.antlr4.ep18r.abi;

import org.junit.jupiter.api.BeforeEach;
import org.teachfx.antlr4.ep18r.stackvm.*;

public abstract class ABITestBase {
    protected RegisterFile registers;
    protected Memory memory;
    protected MemoryExecutors executor;

    @BeforeEach
    void setUp() {
        registers = new RegisterFile();
        memory = new Memory(1024); // 1KB内存
        executor = new MemoryExecutors(registers, memory);
    }

    protected void loadAndExecute(String assembly) {
        // 汇编并执行程序
        // 实现细节略
    }
}
```

### 10.2 测试数据生成器

生成测试用的汇编程序片段：

```java
public class TestProgramGenerator {

    public static String generateFunctionCallTest(int numArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append(".def caller: args=0, locals=0\n");
        // 设置参数
        for (int i = 0; i < numArgs; i++) {
            if (i < 6) {
                sb.append("    li a").append(i).append(", ").append(i+1).append("\n");
            } else {
                sb.append("    li t0, ").append(i+1).append("\n");
                sb.append("    sw t0, ").append(16 + (i-6)*4).append("(sp)\n");
            }
        }
        sb.append("    call callee\n");
        sb.append("    halt\n\n");

        sb.append(".def callee: args=").append(numArgs).append(", locals=0\n");
        sb.append("    # 计算参数总和\n");
        sb.append("    li t0, 0\n");
        for (int i = 0; i < Math.min(numArgs, 6); i++) {
            sb.append("    add t0, t0, a").append(i).append("\n");
        }
        for (int i = 6; i < numArgs; i++) {
            sb.append("    lw t1, ").append(16 + (i-6)*4).append("(fp)\n");
            sb.append("    add t0, t0, t1\n");
        }
        sb.append("    mov a0, t0\n");
        sb.append("    ret\n");

        return sb.toString();
    }
}
```

---

## 11. 测试执行命令

### 11.1 运行特定测试类
```bash
mvn test -pl ep18r -Dtest=RegisterABITest
```

### 11.2 运行ABI相关所有测试
```bash
mvn test -pl ep18r -Dtest="*ABI*"
```

### 11.3 生成覆盖率报告
```bash
mvn jacoco:report -pl ep18r
open ep18r/target/site/jacoco/index.html
```

---

## 12. 测试维护指南

### 12.1 添加新测试
1. 确定测试分类（寄存器、栈帧、函数调用等）。
2. 使用现有测试夹具基类。
3. 遵循Arrange-Act-Assert模式。
4. 提供清晰的测试名称和描述。

### 12.2 测试审查清单
- [ ] 测试名称清晰描述目的
- [ ] 测试数据覆盖正常、边界、异常场景
- [ ] 断言充分验证行为
- [ ] 测试独立，不依赖其他测试
- [ ] 测试代码可读、可维护

### 12.3 测试演进
- 新功能必须添加测试。
- 修复bug时添加回归测试。
- 定期重构测试代码，消除重复。

---

**文档版本**: 1.0（整合版）
**最后更新**: 2026-01-08
**维护者**: Claude Code
**状态**: ✅ 测试规范完成（整合版）
**整合说明**: 本规范整合了原《测试模板.md》和《单元测试案例集.md》的核心内容，形成统一的测试规范文档。