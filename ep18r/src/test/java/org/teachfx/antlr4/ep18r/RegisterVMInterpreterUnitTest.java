package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.exception.*;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;
import org.teachfx.antlr4.ep18r.stackvm.ErrorCode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * RegisterVMInterpreter单元测试
 * 专门针对RegisterVMInterpreter核心功能的全面测试套件
 *
 * 测试范围：
 * 1. 42条指令的执行验证
 * 2. 异常处理（除零、栈溢出等）
 * 3. 边界条件（最大寄存器、最大栈深度）
 * 4. 寄存器文件操作
 * 5. 内存访问
 * 6. 栈帧管理
 *
 * === ABI寄存器使用约定 ===
 *
 * 寄存器架构（16个通用寄存器）：
 * - r0: 零寄存器（恒为0）
 * - r1 (ra): 返回地址寄存器，由CALL/RET自动保存/恢复
 * - r2 (a0): 第一个参数寄存器 / 返回值寄存器
 * - r3-r7 (a1-a5): 参数寄存器，caller-saved
 * - r8-r12 (s0-s4): 保存寄存器，callee-saved
 * - r13 (sp): 栈指针
 * - r14 (fp): 帧指针
 * - r15 (lr): 链接寄存器，caller-saved
 *
 * 测试中的重要注意事项：
 * 1. 避免在涉及函数调用的测试中使用r1作为通用数据寄存器
 *    - r1在CALL时被保存，在RET时被恢复
 *    - 函数内修改r1会在返回时丢失
 * 2. 在函数调用场景中，使用r2-r7或r8-r12作为通用数据寄存器
 * 3. 在没有函数调用的简单测试中，r1可以作为通用寄存器使用
 *
 * 示例（错误）：
 * .def main: args=0, locals=0
 *     li r1, 10
 *     call func    ; r1会被保存
 *     halt
 * .def func: args=1, locals=0
 *     add r1, r1, r1  ; 修改r1，但RET时会恢复为调用前值
 *     ret
 *
 * 示例（正确）：
 * .def main: args=0, locals=0
 *     li r2, 10
 *     call func
 *     halt
 * .def func: args=1, locals=0
 *     add r2, r2, r2  ; r2在函数调用中正确传递和修改
 *     ret
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RegisterVMInterpreter单元测试套件")
public class RegisterVMInterpreterUnitTest {

    private RegisterVMInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new RegisterVMInterpreter();
    }

    @AfterEach
    void tearDown() {
        interpreter = null;
    }

    // ==================== 指令执行测试 ====================

    @Nested
    @DisplayName("算术指令测试")
    class ArithmeticInstructionTests {

        @Test
        @DisplayName("ADD指令应该正确执行加法")
        void testAddInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 20
                    add r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(30);
        }

        @Test
        @DisplayName("SUB指令应该正确执行减法")
        void testSubInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 20
                    li r2, 10
                    sub r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(10);
        }

        @Test
        @DisplayName("MUL指令应该正确执行乘法")
        void testMulInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 20
                    mul r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(200);
        }

        @Test
        @DisplayName("DIV指令应该正确执行除法")
        void testDivInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 20
                    li r2, 10
                    div r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(2);
        }

        @Test
        @DisplayName("DIV指令应该抛出除零异常")
        void testDivByZero() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 0
                    div r3, r1, r2
                    halt
                """;

            assertThatThrownBy(() -> loadAndExecute(program))
                .isInstanceOf(VMDivisionByZeroException.class)
                .hasMessageContaining("Division by zero");
        }

        @Test
        @DisplayName("算术指令应该正确执行")
        void testAllArithmeticInstructions() throws Exception {
            // Test ADD
            String addProgram = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 20
                    add r3, r1, r2
                    halt
                """;
            loadAndExecute(addProgram);
            assertThat(interpreter.getRegister(3)).isEqualTo(30);

            // Test SUB
            String subProgram = """
                .def main: args=0, locals=0
                    li r1, 20
                    li r2, 10
                    sub r3, r1, r2
                    halt
                """;
            loadAndExecute(subProgram);
            assertThat(interpreter.getRegister(3)).isEqualTo(10);

            // Test MUL
            String mulProgram = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 20
                    mul r3, r1, r2
                    halt
                """;
            loadAndExecute(mulProgram);
            assertThat(interpreter.getRegister(3)).isEqualTo(200);

            // Test DIV
            String divProgram = """
                .def main: args=0, locals=0
                    li r1, 20
                    li r2, 10
                    div r3, r1, r2
                    halt
                """;
            loadAndExecute(divProgram);
            assertThat(interpreter.getRegister(3)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("逻辑指令测试")
    class LogicalInstructionTests {

        @Test
        @DisplayName("AND指令应该正确执行逻辑与")
        void testAndInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 5
                    li r2, 3
                    and r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(1); // 5 & 3 = 1
        }

        @Test
        @DisplayName("OR指令应该正确执行逻辑或")
        void testOrInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 5
                    li r2, 3
                    or r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(7); // 5 | 3 = 7
        }

        @Test
        @DisplayName("XOR指令应该正确执行逻辑异或")
        void testXorInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 5
                    li r2, 3
                    xor r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(6); // 5 ^ 3 = 6
        }

        @Test
        @DisplayName("NOT指令应该正确执行逻辑非")
        void testNotInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 5
                    not r2, r1
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(-6);
        }

        @Test
        @DisplayName("NEG指令应该正确执行取负")
        void testNegInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    neg r2, r1
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(-10);
        }
    }

    @Nested
    @DisplayName("内存访问指令测试")
    class MemoryInstructionTests {

        @Test
        @DisplayName("LI指令应该正确加载立即数")
        void testLiInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 42
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(1)).isEqualTo(42);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, -1, 100, -100, 32767, -32768})
        @DisplayName("LI指令应该正确加载各种大小的立即数")
        void testLiInstructionWithVariousValues(int value) throws Exception {
            String program = String.format("""
                .def main: args=0, locals=0
                    li r1, %d
                    halt
                """, value);

            loadAndExecute(program);

            assertThat(interpreter.getRegister(1)).isEqualTo(value);
        }

    @Test
    @DisplayName("LW指令应该正确加载字数据")
    void testLwInstruction() throws Exception {
        String program = """
            .def main: args=0, locals=1
                li r1, 100
                sw r1, r14, 0
                lw r2, r14, 0
                halt
            """;

        loadAndExecute(program);

        assertThat(interpreter.getRegister(2)).isEqualTo(100);
    }

        @Test
        @DisplayName("SW指令应该正确存储字数据")
        void testSwInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=1
                    li r1, 42
                    sw r1, r14, 0
                    lw r2, r14, 0
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(42);
        }

        @Test
        @DisplayName("MOV指令应该正确移动寄存器值")
        void testMovInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 42
                    mov r2, r1
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(42);
        }

        @Test
        @DisplayName("LW_G指令应该正确从全局内存加载数据")
        void testLwGlobalInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 12345
                    sw_g r1, 0
                    lw_g r2, 0
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(12345);
        }

        @Test
        @DisplayName("SW_G指令应该正确存储数据到全局内存")
        void testSwGlobalInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 111
                    sw_g r1, 0
                    li r1, 222
                    sw_g r1, 4
                    li r1, 333
                    sw_g r1, 8
                    lw_g r2, 0
                    lw_g r3, 4
                    lw_g r4, 8
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(111);
            assertThat(interpreter.getRegister(3)).isEqualTo(222);
            assertThat(interpreter.getRegister(4)).isEqualTo(333);
        }

        @Test
        @DisplayName("LW_G和SW_G应该能正确读写全局内存")
        void testGlobalMemoryReadWrite() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 12345
                    sw_g r1, 100
                    lw_g r2, 100
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(12345);
        }

        @Test
        @DisplayName("LW_G和SW_G应该支持负数")
        void testGlobalMemoryNegativeValues() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, -12345
                    sw_g r1, 0
                    lw_g r2, 0
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(-12345);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 4, 8, 12, 100, 500})
        @DisplayName("LW_G和SW_G应该支持不同的内存偏移")
        void testGlobalMemoryWithVariousOffsets(int offset) throws Exception {
            String program = String.format("""
                .def main: args=0, locals=0
                    li r1, 42
                    sw_g r1, %d
                    lw_g r2, %d
                    halt
                """, offset, offset);

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(42);
        }

        @Test
        @DisplayName("LC指令应该正确加载字符立即数")
        void testLcInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    lc r1, 65
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(1)).isEqualTo(65);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 127, 255})
        @DisplayName("LC指令应该支持各种字符值")
        void testLcInstructionWithVariousValues(int charValue) throws Exception {
            String program = String.format("""
                .def main: args=0, locals=0
                    lc r1, %d
                    halt
                """, charValue);

            loadAndExecute(program);

            assertThat(interpreter.getRegister(1)).isEqualTo(charValue);
        }
    }

    @Nested
    @DisplayName("控制流指令测试")
    class ControlFlowInstructionTests {

        @Test
        @DisplayName("J指令应该正确执行无条件跳转")
        void testJInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    j 8
                    li r1, 20
                    li r2, 30
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(1)).isEqualTo(20); // 应该跳转到目标地址的指令
            assertThat(interpreter.getRegister(2)).isEqualTo(30); // 执行了跳转后的指令
        }

        @Test
        @DisplayName("JT指令应该在条件为真时跳转")
        void testJtInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 1
                    jt r1, 12
                    li r2, 20
                    li r2, 30
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(30); // 应该跳转
        }

        @Test
        @DisplayName("JF指令应该在条件为假时跳转")
        void testJfInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 0
                    jf r1, 12
                    li r2, 20
                    li r2, 30
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(30); // 应该跳转
        }

        @Test
        @DisplayName("CALL指令应该正确调用函数")
        void testCallInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    call 12
                    halt
                .def foo: args=1, locals=0
                    add r2, r1, r1
                    ret
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(20); // foo函数应该执行
        }

        @Test
        @DisplayName("RET指令应该正确从函数返回")
        void testRetInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    call 12
                    halt
                .def foo: args=1, locals=0
                    add r2, r1, r1
                    ret
                """;

            loadAndExecute(program);

            // RET应该返回到main，并且halt
            assertThat(interpreter.getRegister(2)).isEqualTo(20);
        }

        @Test
        @DisplayName("HALT指令应该正确停止执行")
        void testHaltInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    halt
                    li r1, 20
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(1)).isEqualTo(10); // HALT后不执行
        }
    }

    @Nested
    @DisplayName("寄存器文件操作测试")
    class RegisterFileTests {

        @Test
        @DisplayName("零寄存器应该始终返回0")
        void testZeroRegister() {
            for (int i = 0; i < 10; i++) {
                interpreter.setRegister(0, i * 10);
                assertThat(interpreter.getRegister(0)).isEqualTo(0);
            }
        }

        @Test
        @DisplayName("应该正确读写普通寄存器")
        void testReadWriteNormalRegisters() {
            for (int reg = 1; reg <= 12; reg++) {
                int value = reg * 10;
                interpreter.setRegister(reg, value);
                assertThat(interpreter.getRegister(reg)).isEqualTo(value);
            }
        }

        @Test
        @DisplayName("应该正确读写特殊寄存器")
        void testReadWriteSpecialRegisters() {
            // SP (R13)
            interpreter.setRegister(RegisterBytecodeDefinition.R13, 1000);
            assertThat(interpreter.getRegister(RegisterBytecodeDefinition.R13)).isEqualTo(1000);

            // FP (R14)
            interpreter.setRegister(RegisterBytecodeDefinition.R14, 2000);
            assertThat(interpreter.getRegister(RegisterBytecodeDefinition.R14)).isEqualTo(2000);

            // LR (R15)
            interpreter.setRegister(RegisterBytecodeDefinition.R15, 3000);
            assertThat(interpreter.getRegister(RegisterBytecodeDefinition.R15)).isEqualTo(3000);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 16, 20, 100})
        @DisplayName("访问无效寄存器应该抛出异常")
        void testInvalidRegisterAccess(int invalidReg) {
            assertThatThrownBy(() -> interpreter.getRegister(invalidReg))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> interpreter.setRegister(invalidReg, 0))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== 异常处理测试 ====================

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("除零应该抛出VMDivisionByZeroException")
        void testDivisionByZeroThrowsException() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 0
                    div r3, r1, r2
                    halt
                """;

            assertThatThrownBy(() -> loadAndExecute(program))
                .isInstanceOf(VMDivisionByZeroException.class);
        }

    @Test
    @DisplayName("无效操作码应该抛出VMException")
    void testInvalidOpcodeThrowsException() throws Exception {
            // 直接构造包含无效操作码(0)的字节码
            // 指令格式: [opcode:6][rd:5][rs1:5][rs2:5][unused:11]
            // 操作码0 = 无效指令 (RegisterBytecodeDefinition.instructions[0] = null)
            byte[] invalidOpcodeCode = new byte[] {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 无效操作码 0
                (byte) 0x0A, (byte) 0x80, (byte) 0x00, (byte) 0x00  // HALT: opcode=42 << 26 = 0xA8000000
            };

            interpreter.loadCode(invalidOpcodeCode);

            assertThatThrownBy(() -> interpreter.exec())
                .isInstanceOf(VMInvalidOpcodeException.class)
                .matches(ex -> ((VMInvalidOpcodeException) ex).getErrorCode() == ErrorCode.INVALID_OPCODE);
        }

        @Test
        @DisplayName("栈溢出应该抛出VMStackOverflowException")
        void testStackOverflowThrowsException() throws Exception {
            // 创建一个会触发栈溢出的递归函数
            // 使用 2000 次递归（超过 maxStackDepth=1000）
            String program = """
                .def main: args=0, locals=0
                    li r1, 2000
                    call recursive
                    halt
                .def recursive: args=1, locals=1
                    sub r1, r1, r2
                    seq r2, r1, r1
                    jf r2, exit
                    call recursive
                    exit:
                    ret
                """;

            assertThatThrownBy(() -> loadAndExecute(program))
                .isInstanceOf(VMStackOverflowException.class)
                .satisfies(ex -> {
                    VMStackOverflowException vmEx = (VMStackOverflowException) ex;
                    assertThat(vmEx.getErrorCode()).isEqualTo(ErrorCode.STACK_OVERFLOW);
                });
        }

        @Test
        @DisplayName("无效操作码应该正确抛出异常")
        void testInvalidOpcodeZero() throws Exception {
            byte[] invalidCode = new byte[] {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0xA8, (byte) 0x00, (byte) 0x00, (byte) 0x00
            };

            interpreter.loadCode(invalidCode);

            assertThatThrownBy(() -> interpreter.exec())
                .isInstanceOf(VMInvalidOpcodeException.class);
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("应该处理最大立即数值")
        void testMaximumImmediateValue() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 32767
                    li r2, -32768
                    add r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(-1);
        }

        @Test
        @DisplayName("应该处理最大寄存器编号")
        void testMaximumRegisterNumber() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r12, 42
                    add r12, r12, r12
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(12)).isEqualTo(84);
        }

        @Test

        @DisplayName("应该处理复杂嵌套调用")
        void testNestedFunctionCalls() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r2, 10
                    call level1
                    halt
                .def level1: args=1, locals=0
                    add r2, r2, r2
                    call level2
                    ret
                .def level2: args=1, locals=0
                    add r2, r2, r2
                    ret
                """;

            loadAndExecute(program);

            // main: r2=10
            // level1: r2=20, call level2
            // level2: r2=40, return
            // level1: return to main
            assertThat(interpreter.getRegister(2)).isEqualTo(40);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 15, 32767})
        @DisplayName("应该正确处理各种立即数值")
        void testVariousImmediateValues(int value) throws Exception {
            String program = String.format("""
                .def main: args=0, locals=0
                    li r1, %d
                    halt
                """, value);

            loadAndExecute(program);

            assertThat(interpreter.getRegister(1)).isEqualTo(value);
        }

        @Test
        @DisplayName("应该正确访问内存边界值")
        void testMemoryBoundaryAccess() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 100
                    sw r1, r14, 0
                    lw r2, r14, 0
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(100);
        }

        @Test
        @DisplayName("应该正确访问全局内存边界值")
        void testGlobalMemoryBoundaryAccess() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 500
                    sw_g r1, 1000
                    lw_g r2, 1000
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(500);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 4, 100, 500, 1000})
        @DisplayName("应该支持不同内存偏移的访问")
        void testVariousMemoryOffsets(int offset) throws Exception {
            String program = String.format("""
                .def main: args=0, locals=0
                    li r1, 42
                    sw r1, r14, %d
                    lw r2, r14, %d
                    halt
                """, offset, offset);

            loadAndExecute(program);

            assertThat(interpreter.getRegister(2)).isEqualTo(42);
        }
    }

    // ==================== 辅助方法 ====================

    private void loadAndExecute(String program) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(program.getBytes());
        RegisterVMInterpreter.load(interpreter, inputStream);
        interpreter.exec();
    }
}
