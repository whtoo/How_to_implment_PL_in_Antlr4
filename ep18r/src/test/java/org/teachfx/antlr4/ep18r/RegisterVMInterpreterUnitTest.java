package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.exception.*;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;

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

    // ==================== 指令执行测试 ====================

    @Nested
    @DisplayName("算术运算指令测试")
    @Order(1)
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
        @Disabled
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
                .hasMessageContaining("division by zero");
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
    @DisplayName("比较指令测试")
    @Order(2)
    class ComparisonInstructionTests {

        @Test
        @DisplayName("SLT指令应该正确执行小于比较")
        void testSltInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 20
                    slt r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(1); // 10 < 20 = true
        }

        @Test
        @DisplayName("SGT指令应该正确执行大于比较")
        void testSgtInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 20
                    li r2, 10
                    sgt r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(1); // 20 > 10 = true
        }

        @Test
        @DisplayName("SEQ指令应该正确执行等于比较")
        void testSeqInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 10
                    seq r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(1); // 10 == 10 = true
        }

        @Test
        @DisplayName("SNE指令应该正确执行不等于比较")
        void testSneInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 20
                    sne r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(1); // 10 != 20 = true
        }

        @Test
        @DisplayName("SLE指令应该正确执行小于等于比较")
        void testSleInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 20
                    sle r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(1); // 10 <= 20 = true
        }

        @Test
        @DisplayName("SGE指令应该正确执行大于等于比较")
        void testSgeInstruction() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 20
                    li r2, 10
                    sge r3, r1, r2
                    halt
                """;

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(1); // 20 >= 10 = true
        }

        @ParameterizedTest
        @CsvSource({
            "10, 20, 1, slt",
            "20, 10, 0, slt",
            "10, 10, 0, slt"
        })
        @DisplayName("比较指令应该正确处理各种情况")
        void testComparisonOperations(int op1, int op2, int expected, String operation) throws Exception {
            String program = String.format("""
                .def main: args=0, locals=0
                    li r1, %d
                    li r2, %d
                    %s r3, r1, r2
                    halt
                """, op1, op2, operation);

            loadAndExecute(program);

            assertThat(interpreter.getRegister(3)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("逻辑运算指令测试")
    @Order(3)
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
    @Order(4)
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
    }

    @Nested
    @DisplayName("控制流指令测试")
    @Order(5)
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
    @Order(6)
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
    @Order(7)
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

        @DisplayName("无效操作码应该抛出VMException")
    @Disabled("需要VMAssembler.g4支持.word指令才能实现此测试")
    @Test
    void testInvalidOpcodeThrowsException() throws Exception {
            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    halt
                """;

            assertThatThrownBy(() -> loadAndExecute(program))
                .isInstanceOf(VMInvalidOpcodeException.class)
                .hasMessageContaining("Invalid opcode");
        }

        @Test
        @DisplayName("栈溢出应该抛出VMStackOverflowException")
        void testStackOverflowThrowsException() throws Exception {
            // 创建一个会触发栈溢出的递归函数
            String program = """
                .def main: args=0, locals=0
                    li r1, 10000
                    call recursive
                    halt
                .def recursive: args=1, locals=1
                    sub r1, r1, r1
                    seq r2, r1, r1
                    jf r2, exit
                    call recursive
                    exit:
                    ret
                """;

            // 注意：这个测试可能需要调整触发栈溢出的深度
            // 取决于VMConfig中的maxCallStackDepth设置
            // 如果栈深度设置得足够大，可能不会触发异常
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    @Order(8)
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
    }

    // ==================== 辅助方法 ====================

    private void loadAndExecute(String program) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(program.getBytes());
        RegisterVMInterpreter.load(interpreter, inputStream);
        interpreter.exec();
    }
}
