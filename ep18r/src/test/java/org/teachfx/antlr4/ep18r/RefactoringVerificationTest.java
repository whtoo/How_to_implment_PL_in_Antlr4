package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.teachfx.antlr4.ep18r.stackvm.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * 重构验证测试套件
 * 用于确保重构过程中不破坏现有功能
 *
 * 测试驱动重构原则：
 * 1. 先写测试
 * 2. 运行测试确保失败
 * 3. 重构代码
 * 4. 运行测试确保通过
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("重构验证测试套件")
public class RefactoringVerificationTest {

    private RegisterVMInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new RegisterVMInterpreter();
    }

    // ==================== 基础功能测试 ====================

    @Test
    @Order(1)
    @DisplayName("基础算术运算测试")
    void testBasicArithmetic() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 10
                li r2, 20
                add r3, r1, r2
                sub r4, r2, r1
                mul r5, r1, r2
                div r6, r2, r1
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(3)).isEqualTo(30); // 10 + 20
        assertThat(interpreter.getRegister(4)).isEqualTo(10); // 20 - 10
        assertThat(interpreter.getRegister(5)).isEqualTo(200); // 10 * 20
        assertThat(interpreter.getRegister(6)).isEqualTo(2); // 20 / 10
    }

    @Test
    @Order(2)
    @DisplayName("比较指令测试")
    void testComparisonInstructions() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 10
                li r2, 20
                slt r3, r1, r2
                sgt r4, r1, r2
                seq r5, r1, r2
                sle r6, r1, r2
                sge r7, r1, r2
                sne r8, r1, r2
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(3)).isEqualTo(1);
        assertThat(interpreter.getRegister(4)).isEqualTo(0);
        assertThat(interpreter.getRegister(5)).isEqualTo(0);
        assertThat(interpreter.getRegister(6)).isEqualTo(1);
        assertThat(interpreter.getRegister(7)).isEqualTo(0);
        assertThat(interpreter.getRegister(8)).isEqualTo(1);
    }

    @Test
    @Order(3)
    @DisplayName("逻辑运算测试")
    void testLogicalOperations() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 5
                li r2, 3
                and r3, r1, r2
                or r4, r1, r2
                xor r5, r1, r2
                li r6, 0
                not r6, r6
                neg r7, r1
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(3)).isEqualTo(1);  // 5 & 3
        assertThat(interpreter.getRegister(4)).isEqualTo(7);  // 5 | 3
        assertThat(interpreter.getRegister(5)).isEqualTo(6);  // 5 ^ 3
        assertThat(interpreter.getRegister(6)).isEqualTo(1);  // logical NOT of 0 = 1
        assertThat(interpreter.getRegister(7)).isEqualTo(-5); // -5
    }

    // ==================== 控制流测试 ====================

    @Test
    @Order(10)
    @DisplayName("无条件跳转测试")
    void testUnconditionalJump() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 1
                j skip
                li r1, 999
            skip:
                li r2, 2
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(1)).isEqualTo(1); // 未修改
        assertThat(interpreter.getRegister(2)).isEqualTo(2); // 执行到
    }

    @Test
    @Order(11)
    @DisplayName("条件跳转测试 - 为真跳转")
    void testConditionalJumpTrue() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 1
                li r2, 1
                seq r3, r1, r2      ; r3 = 1 (true)
                jt r3, target       ; 应该跳转
                li r1, 999          ; 应该跳过
            target:
                li r4, 42
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(1)).isEqualTo(1);   // 原始值
        assertThat(interpreter.getRegister(3)).isEqualTo(1);   // 比较结果
        assertThat(interpreter.getRegister(4)).isEqualTo(42);  // 跳转后设置
    }

    @Test
    @Order(12)
    @DisplayName("条件跳转测试 - 为假跳转")
    void testConditionalJumpFalse() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 1
                li r2, 2
                seq r3, r1, r2      ; r3 = 0 (false)
                jf r3, target       ; 应该跳转
                li r1, 999          ; 应该跳过
            target:
                li r4, 42
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(1)).isEqualTo(1);   // 原始值
        assertThat(interpreter.getRegister(3)).isEqualTo(0);   // 比较结果
        assertThat(interpreter.getRegister(4)).isEqualTo(42);  // 跳转后设置
    }

    // ==================== 函数调用测试 ====================

    @Test
    @Order(20)
    @DisplayName("简单函数调用测试")
    void testSimpleFunctionCall() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 5
                li r2, 3
                call add_func
                halt

            .def add_func: args=2, locals=0
                add r1, r1, r2     ; r1 = 5 + 3 = 8
                ret
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(1)).isEqualTo(8); // 返回值
    }

    @Test
    @Order(21)
    @DisplayName("嵌套函数调用测试")
    void testNestedFunctionCalls() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 5
                call f
                halt

            .def f: args=1, locals=0
                li r2, 10
                call g
                add r1, r1, r2
                ret

            .def g: args=0, locals=0
                li r2, 100
                ret
            """;

        loadAndExecute(program);

        // main调用f，f调用g
        // g返回后，f的r2=100，然后f返回到main
        // main中r1 = 5 + 100 = 105
        assertThat(interpreter.getRegister(1)).isEqualTo(105);
    }

    @Test
    @Order(22)
    @DisplayName("递归函数调用测试")
    void testRecursiveFunctionCall() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 5
                call fib
                halt

            .def fib: args=1, locals=3
                li r2, 1
                sle r3, r1, r2
                jt r3, base
                li r2, 1
                sub r4, r1, r2
                li r2, 2
                sub r5, r1, r2
                mov r1, r4
                call fib
                mov r6, r1
                mov r1, r5
                call fib
                add r1, r6, r1
                ret
            base:
                li r1, 1
                ret
            """;

        loadAndExecute(program);

        // fib(5) = 8
        assertThat(interpreter.getRegister(1)).isEqualTo(8);
    }

    // ==================== 内存访问测试 ====================

    @Test
    @Order(30)
    @DisplayName("局部变量访问测试")
    void testLocalVariableAccess() throws Exception {
        String program = """
            .def main: args=0, locals=4
                li r1, 10
                sw r1, r13, 0      ; locals[0] = 10
                li r2, 20
                sw r2, r13, 4      ; locals[1] = 20
                lw r3, r13, 0      ; r3 = locals[0]
                lw r4, r13, 4      ; r4 = locals[1]
                add r5, r3, r4
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(5)).isEqualTo(30);
    }

    @Test
    @Order(31)
    @DisplayName("结构体操作测试")
    void testStructOperations() throws Exception {
        String program = """
            .def main: args=0, locals=0
                struct r1, 3      ; 分配3字段结构体
                li r2, 10
                sw_f r2, r1, 0    ; 结构体[0] = 10
                li r3, 20
                sw_f r3, r1, 4    ; 结构体[1] = 20
                lw r4, r1, 0      ; r4 = 结构体[0]
                lw r5, r1, 4      ; r5 = 结构体[1]
                add r6, r4, r5
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(6)).isEqualTo(30);
    }

    // ==================== 边界条件测试 ====================

    @Test
    @Order(40)
    @DisplayName("寄存器零值测试")
    void testZeroRegister() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 100
                mov r0, r1        ; 尝试写入r0（应该被忽略）
                add r2, r0, r1    ; r2 = 0 + 100 = 100
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(0)).isEqualTo(0);  // r0恒为0
        assertThat(interpreter.getRegister(2)).isEqualTo(100); // r2 = r0 + r1
    }

    @Test
    @Order(41)
    @DisplayName("立即数符号扩展测试")
    void testImmediateSignExtension() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 32767      ; 最大16位正数
                li r2, -32768     ; 最大16位负数
                li r3, -1         ; -1的所有1位
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(1)).isEqualTo(32767);
        assertThat(interpreter.getRegister(2)).isEqualTo(-32768);
        assertThat(interpreter.getRegister(3)).isEqualTo(-1);
    }

    @Test
    @Order(42)
    @DisplayName("除零错误测试")
    void testDivisionByZero() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 10
                li r2, 0
                div r3, r1, r2    ; 除以零
                halt
            """;

        assertThatThrownBy(() -> loadAndExecute(program))
            .isInstanceOf(ArithmeticException.class)
            .hasMessageContaining("Division by zero");
    }

    // ==================== 安全性测试 ====================

    @Test
    @Order(50)
    @DisplayName("无限循环检测测试")
    void testInfiniteLoopDetection() throws Exception {
        String program = """
            .def main: args=0, locals=0
                j 0              ; 无穷跳转到自己
                halt
            """;

        assertThatThrownBy(() -> loadAndExecute(program))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("infinite loop")
            .hasMessageContaining("Maximum execution steps exceeded");
    }

    @Test
    @Order(51)
    @DisplayName("无效跳转目标测试")
    void testInvalidJumpTarget() throws Exception {
        String program = """
            .def main: args=0, locals=0
                j 999999         ; 无效地址
                halt
            """;

        assertThatThrownBy(() -> loadAndExecute(program))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid jump target");
    }

    @Test
    @Order(52)
    @DisplayName("无效操作码测试")
    void testInvalidOpcode() throws Exception {
        // 通过直接操作字节码
        byte[] maliciousBytecode = createBytecodeWithInvalidOpcode();

        RegisterVMInterpreter vm = new RegisterVMInterpreter();
        vm.loadCode(maliciousBytecode);

        assertThatThrownBy(vm::exec)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid opcode");
    }

    // ==================== 性能测试 ====================

    @Test
    @Order(60)
    @DisplayName("性能基准测试 - 简单循环")
    void testPerformanceSimpleLoop() throws Exception {
        String program = """
            .def main: args=0, locals=2
                li r1, 0
                li r2, 1000
            loop:
                slt r3, r1, r2
                jf r3, end
                add r1, r1, r1
                li r4, 1
                add r1, r1, r4
                j loop
            end:
                halt
            """;

        long startTime = System.nanoTime();
        loadAndExecute(program);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒

        // 1000次循环应该在合理时间内完成
        assertThat(duration).isLessThan(1000); // 1秒上限
        System.out.println("1000次循环执行时间: " + duration + "ms");
    }

    @Test
    @Order(61)
    @DisplayName("性能基准测试 - 递归计算")
    void testPerformanceRecursiveCalculation() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 10
                call fib
                halt

            .def fib: args=1, locals=3
                li r2, 1
                sle r3, r1, r2
                jt r3, base
                li r2, 1
                sub r4, r1, r2
                li r2, 2
                sub r5, r1, r2
                mov r1, r4
                call fib
                mov r6, r1
                mov r1, r5
                call fib
                add r1, r6, r1
                ret
            base:
                li r1, 1
                ret
            """;

        long startTime = System.nanoTime();
        loadAndExecute(program);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;

        // fib(10)递归计算应该在合理时间内完成
        assertThat(duration).isLessThan(2000); // 2秒上限
        System.out.println("fib(10)递归计算时间: " + duration + "ms");
        assertThat(interpreter.getRegister(1)).isEqualTo(89); // fib(10) = 89
    }

    // ==================== 重构验证测试 ====================

    @Test
    @Order(70)
    @DisplayName("重构验证：配置对象测试")
    void testVMConfiguration() throws Exception {
        // 验证VMConfig类是否正确工作
        org.teachfx.antlr4.ep18r.stackvm.VMConfig config =
            new org.teachfx.antlr4.ep18r.stackvm.VMConfig.Builder()
                .heapSize(2048 * 1024)
                .maxExecutionSteps(2_000_000)
                .build();

        assertThat(config.getHeapSize()).isEqualTo(2048 * 1024);
        assertThat(config.getMaxExecutionSteps()).isEqualTo(2_000_000);
    }

    @Test
    @Order(71)
    @DisplayName("重构验证：寄存器访问封装")
    void testRegisterAccessEncapsulation() throws Exception {
        // 验证寄存器访问是否被正确封装
        interpreter.setRegister(5, 100);
        assertThat(interpreter.getRegister(5)).isEqualTo(100);

        // 验证零寄存器
        interpreter.setRegister(0, 999);
        assertThat(interpreter.getRegister(0)).isEqualTo(0); // 仍为0
    }

    @Test
    @Order(72)
    @DisplayName("重构验证：异常体系测试")
    void testExceptionHierarchy() throws Exception {
        // 验证除零异常 - 当前实现使用标准ArithmeticException
        String program = """
            .def main: args=0, locals=0
                li r1, 0
                li r2, 0
                div r3, r1, r2
                halt
            """;

        assertThatThrownBy(() -> loadAndExecute(program))
            .isInstanceOf(ArithmeticException.class)
            .hasMessageContaining("Division by zero");
    }

    @ParameterizedTest
    @Order(80)
    @ValueSource(ints = {0, 1, 5, 10, 15})
    @DisplayName("参数化测试：所有寄存器可访问")
    void testAllRegistersAccessible(int regNum) throws Exception {
        interpreter.setRegister(regNum, regNum * 10);
        assertThat(interpreter.getRegister(regNum)).isEqualTo(regNum * 10);
    }

    @Test
    @Order(81)
    @DisplayName("参数化测试：16位立即数边界值")
    void testArithmeticBoundaryValues() throws Exception {
        // LI指令使用16位立即数，范围是-32768到32767
        String program = """
            .def main: args=0, locals=0
                li r1, 32767
                li r2, -32768
                li r3, 0
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(1)).isEqualTo(32767);  // 最大16位正数
        assertThat(interpreter.getRegister(2)).isEqualTo(-32768); // 最大16位负数
        assertThat(interpreter.getRegister(3)).isEqualTo(0);
    }

    // ==================== 工具方法 ====================

    private void loadAndExecute(String program) throws Exception {
        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();
        interpreter.exec();
    }

    private byte[] createBytecodeWithInvalidOpcode() {
        // 创建一个包含无效操作码的字节码
        byte[] bytecode = new byte[8];
        // 无效操作码：255
        bytecode[0] = (byte) 0xFF;
        bytecode[1] = 0x00;
        bytecode[2] = 0x00;
        bytecode[3] = 0x00;
        return bytecode;
    }
}

/**
 * 辅助类：VM配置（用于重构验证）
 */
class VMConfig {
    private final int heapSize;
    private final int localsSize;
    private final int maxCallStackDepth;
    private final int maxExecutionSteps;

    private VMConfig(Builder builder) {
        this.heapSize = builder.heapSize;
        this.localsSize = builder.localsSize;
        this.maxCallStackDepth = builder.maxCallStackDepth;
        this.maxExecutionSteps = builder.maxExecutionSteps;
    }

    public static class Builder {
        private int heapSize = 1024 * 1024;
        private int localsSize = 1024;
        private int maxCallStackDepth = 1024;
        private int maxExecutionSteps = 1_000_000;

        public Builder heapSize(int size) {
            this.heapSize = size;
            return this;
        }

        public VMConfig build() {
            return new VMConfig(this);
        }

        public int getHeapSize() { return heapSize; }
        public int getLocalsSize() { return localsSize; }
        public int getMaxCallStackDepth() { return maxCallStackDepth; }
        public int getMaxExecutionSteps() { return maxExecutionSteps; }
    }

    public int getHeapSize() { return heapSize; }
    public int getLocalsSize() { return localsSize; }
    public int getMaxCallStackDepth() { return maxCallStackDepth; }
    public int getMaxExecutionSteps() { return maxExecutionSteps; }
}

/**
 * 异常体系（用于重构验证）
 */
class VMException extends RuntimeException {
    protected final int pc;
    protected final ErrorCode errorCode;

    public VMException(String message, int pc, ErrorCode errorCode) {
        super(message);
        this.pc = pc;
        this.errorCode = errorCode;
    }

    public int getPc() { return pc; }
    public ErrorCode getErrorCode() { return errorCode; }
}

enum ErrorCode {
    DIVISION_BY_ZERO("DIV_ZERO"),
    INVALID_OPCODE("INV_OP"),
    INVALID_REGISTER("INV_REG"),
    INVALID_ADDRESS("INV_ADDR"),
    INFINITE_LOOP("INF_LOOP");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() { return code; }
}
