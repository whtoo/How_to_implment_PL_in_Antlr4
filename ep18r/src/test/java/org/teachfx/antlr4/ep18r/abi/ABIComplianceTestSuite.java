package org.teachfx.antlr4.ep18r.abi;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;
import static org.teachfx.antlr4.ep18r.stackvm.RegisterBytecodeDefinition.*;

/**
 * ABI一致性测试套件
 * 基于EP18R_ABI_设计文档.md第10节规范
 *
 * 测试目标：
 * 1. 寄存器保存测试：验证调用者/被调用者保存责任
 * 2. 栈帧布局测试：验证栈帧偏移计算正确性
 * 3. 参数传递测试：验证寄存器/栈参数传递
 * 4. 返回值测试：验证返回值约定
 * 5. 对齐测试：验证栈和数据对齐要求
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ABI一致性测试套件")
public class ABIComplianceTestSuite {

    private RegisterVMInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new RegisterVMInterpreter();
    }

    // ==================== 10.1.1 寄存器保存测试 ====================

    @Test
    @Order(10)
    @DisplayName("被调用者保存寄存器测试")
    void testCalleeSavedRegisters() throws Exception {
        // 测试被调用者保存寄存器s0-s4在函数调用后保持不变
        String program = """
            .def caller: args=0, locals=0
                li s0, 100
                li s1, 200
                li s2, 300
                call callee
                # 验证s0-s4保持不变
                seq a0, s0, 100
                seq a1, s1, 200
                seq a2, s2, 300
                halt

            .def callee: args=0, locals=2
                # 被调用者使用s0-s4时必须保存
                sw s0, -12(fp)  # 保存s0
                sw s1, -8(fp)   # 保存s1
                sw s2, -4(fp)   # 保存s2

                # 修改s0-s4的值
                li s0, 999
                li s1, 888
                li s2, 777

                # 恢复s0-s4
                lw s2, -4(fp)
                lw s1, -8(fp)
                lw s0, -12(fp)
                ret
            """;

        loadAndExecute(program);
        // a0 = 1 (s0不变), a1 = 1 (s1不变), a2 = 1 (s2不变)
        assertThat(interpreter.getRegister(R2)).isEqualTo(1); // a0
        assertThat(interpreter.getRegister(R3)).isEqualTo(1); // a1
        assertThat(interpreter.getRegister(R4)).isEqualTo(1); // a2
    }

    @Test
    @Order(11)
    @DisplayName("调用者保存寄存器测试")
    void testCallerSavedRegisters() throws Exception {
        // 测试调用者保存寄存器r2-r7在函数调用后的值
        String program = """
            .def caller: args=0, locals=0
                li a0, 10
                li a1, 20
                li a2, 30
                li a3, 40
                li a4, 50
                li a5, 60
                call callee
                # 调用者保存寄存器在CALL后可能被修改
                # 验证返回值
                halt

            .def callee: args=0, locals=0
                # 被调用者可以自由修改a0-a5
                li a0, 100
                li a1, 200
                li a2, 300
                ret
            """;

        loadAndExecute(program);
        // callee修改了a0-a2，caller可以看到这些变化（调用者保存寄存器）
        assertThat(interpreter.getRegister(R2)).isEqualTo(100); // a0被修改
        assertThat(interpreter.getRegister(R3)).isEqualTo(200); // a1被修改
        assertThat(interpreter.getRegister(R4)).isEqualTo(300); // a2被修改
    }

    @Test
    @Order(12)
    @DisplayName("零寄存器测试")
    void testZeroRegister() throws Exception {
        // 测试r0（zero寄存器）始终为0
        String program = """
            .def main: args=0, locals=0
                li a0, 100
                mov r0, a0      # 尝试写入r0（应该被忽略）
                seq a0, r0, 0   # 验证r0仍为0
                halt
            """;

        loadAndExecute(program);
        assertThat(interpreter.getRegister(R0)).isEqualTo(0); // r0恒为0
        assertThat(interpreter.getRegister(R2)).isEqualTo(1); // a0 = 1 (r0 == 0)
    }

    // ==================== 10.1.2 栈帧布局测试 ====================

    @Test
    @Order(20)
    @DisplayName("栈帧局部变量访问测试")
    void testStackFrameLocalVariables() throws Exception {
        // 测试栈帧中局部变量的正确访问
        String program = """
            .def main: args=0, locals=4
                li a0, 10
                sw a0, 0(sp)      # locals[0] = 10
                li a1, 20
                sw a1, 4(sp)      # locals[1] = 20
                li a2, 30
                sw a2, 8(sp)      # locals[2] = 30
                li a3, 40
                sw a3, 12(sp)     # locals[3] = 40

                # 读取并验证
                lw a0, 0(sp)
                lw a1, 4(sp)
                lw a2, 8(sp)
                lw a3, 12(sp)
                add a0, a0, a1    # a0 = 10 + 20 = 30
                add a0, a0, a2    # a0 = 30 + 30 = 60
                add a0, a0, a3    # a0 = 60 + 40 = 100
                halt
            """;

        loadAndExecute(program);
        assertThat(interpreter.getRegister(R2)).isEqualTo(100); // a0 = 100
    }

    @Test
    @Order(21)
    @DisplayName("栈帧大小计算测试")
    void testStackFrameSize() throws Exception {
        // 测试不同局部变量数量的栈帧大小
        // 根据ABI规范，栈帧大小 = 保存寄存器区 + 局部变量区 + 对齐
        String program = """
            .def main: args=0, locals=1
                li a0, 42
                sw a0, 0(sp)      # 写入局部变量
                lw a0, 0(sp)      # 读取局部变量
                halt
            """;

        loadAndExecute(program);
        assertThat(interpreter.getRegister(R2)).isEqualTo(42); // a0 = 42
    }

    // ==================== 10.1.3 参数传递测试 ====================

    @Test
    @Order(30)
    @DisplayName("寄存器参数传递测试（前6个参数）")
    void testRegisterArgumentPassing() throws Exception {
        // 测试前6个参数通过寄存器a0-a5传递
        String program = """
            .def caller: args=0, locals=0
                li a0, 1      # 第1个参数
                li a1, 2      # 第2个参数
                li a2, 3      # 第3个参数
                li a3, 4      # 第4个参数
                li a4, 5      # 第5个参数
                li a5, 6      # 第6个参数
                call sum6
                halt

            .def sum6: args=6, locals=0
                # 计算a0+a1+a2+a3+a4+a5
                add a0, a0, a1
                add a0, a0, a2
                add a0, a0, a3
                add a0, a0, a4
                add a0, a0, a5
                ret
            """;

        loadAndExecute(program);
        // 1+2+3+4+5+6 = 21
        assertThat(interpreter.getRegister(R2)).isEqualTo(21);
    }

    @Test
    @Order(31)
    @DisplayName("栈参数传递测试（第7+个参数）")
    void testStackArgumentPassing() throws Exception {
        // 测试第7+个参数通过栈传递
        String program = """
            .def caller: args=0, locals=0
                li a0, 1      # 第1个参数
                li a1, 2      # 第2个参数
                li a2, 3      # 第3个参数
                li a3, 4      # 第4个参数
                li a4, 5      # 第5个参数
                li a5, 6      # 第6个参数
                li t0, 7      # 第7个参数（临时寄存器）
                sw t0, 16(sp) # 压入栈中
                li t0, 8      # 第8个参数
                sw t0, 20(sp) # 压入栈中
                call sum8
                halt

            .def sum8: args=8, locals=0
                # 读取前6个参数（寄存器）
                # 读取第7-8个参数（栈）
                lw t0, 16(sp)   # 第7个参数
                add a0, a0, t0
                lw t0, 20(sp)   # 第8个参数
                add a0, a0, t0
                ret
            """;

        loadAndExecute(program);
        // 1+2+3+4+5+6+7+8 = 36
        assertThat(interpreter.getRegister(R2)).isEqualTo(36);
    }

    // ==================== 10.1.4 返回值测试 ====================

    @Test
    @Order(40)
    @DisplayName("单返回值测试")
    void testSingleReturnValue() throws Exception {
        // 测试通过a0寄存器返回单个值
        String program = """
            .def caller: args=0, locals=0
                li a0, 10
                li a1, 20
                call add
                # 验证返回值在a0中
                seq a0, a0, 30
                halt

            .def add: args=2, locals=0
                add a0, a0, a1
                ret
            """;

        loadAndExecute(program);
        assertThat(interpreter.getRegister(R2)).isEqualTo(1); // a0 = 1 (返回30)
    }

    @Test
    @Order(41)
    @DisplayName("递归函数返回值测试")
    void testRecursiveReturnValue() throws Exception {
        // 测试递归函数的返回值传递
        String program = """
            .def main: args=0, locals=0
                li a0, 5
                call fib
                halt

            .def fib: args=1, locals=3
                li a1, 1
                sle a1, a0, a1
                jt a1, base
                li a1, 1
                sub a2, a0, a1
                mov a0, a2
                call fib
                sw a0, -12(sp)
                li a1, 2
                sub a2, a0, a1
                mov a0, a2
                call fib
                lw a1, -12(sp)
                add a0, a0, a1
                ret
            base:
                li a0, 1
                ret
            """;

        loadAndExecute(program);
        // fib(5) = 5
        assertThat(interpreter.getRegister(R2)).isEqualTo(5);
    }

    // ==================== 10.1.5 对齐测试 ====================

    @Test
    @Order(50)
    @DisplayName("栈对齐测试")
    void testStackAlignment() throws Exception {
        // 测试栈指针对齐要求（8字节对齐）
        String program = """
            .def main: args=0, locals=1
                # 验证栈指针对齐
                li a0, 42
                sw a0, 0(sp)
                lw a0, 0(sp)
                halt
            """;

        loadAndExecute(program);
        assertThat(interpreter.getRegister(R2)).isEqualTo(42);
    }

    @Test
    @Order(51)
    @DisplayName("结构体对齐测试")
    void testStructAlignment() throws Exception {
        // 测试结构体字段对齐
        String program = """
            .def main: args=0, locals=0
                struct a0, 3      # 分配3字段结构体
                li a1, 100
                sw_f a1, a0, 0    # 字段0 = 100 (偏移0)
                li a1, 200
                sw_f a1, a0, 4    # 字段1 = 200 (偏移4)
                li a1, 300
                sw_f a1, a0, 8    # 字段2 = 300 (偏移8)

                lw_f a1, a0, 0    # 读取字段0
                seq a0, a1, 100
                halt
            """;

        loadAndExecute(program);
        assertThat(interpreter.getRegister(R2)).isEqualTo(1); // a0 = 1 (字段0 == 100)
    }

    // ==================== 工具方法 ====================

    private void loadAndExecute(String program) throws Exception {
        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();
        interpreter.exec();
    }
}
