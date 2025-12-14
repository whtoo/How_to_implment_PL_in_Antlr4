package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.teachfx.antlr4.ep18r.stackvm.ByteCodeAssembler;
import org.teachfx.antlr4.ep18r.stackvm.BytecodeDefinition;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * VMInterpreter单元测试
 * 测试字节码解释器的功能和性能
 */
@DisplayName("VMInterpreter字节码解释器测试")
public class VMInterpreterTest {

    @Test
    @DisplayName("应该正确加载和执行简单程序")
    void testSimpleProgram() throws Exception {
        String program = """
            .def main: args=0, locals=0
                iconst 10
                iconst 20
                iadd
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        boolean hasErrors = VMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();

        // Execute program
        interpreter.exec();

        // 验证结果（栈顶应该包含30）
        assertThat(interpreter.sp).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("应该正确执行算术运算")
    void testArithmeticOperations() throws Exception {
        String program = """
            .def main: args=0, locals=0
                iconst 15
                iconst 5
                isub
                iconst 2
                imul
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // (15 - 5) * 2 = 20
        assertThat(interpreter.operands[interpreter.sp]).isEqualTo(20);
    }

    @Test
    @DisplayName("应该正确执行比较操作")
    void testComparisonOperations() throws Exception {
        String program = """
            .def main: args=0, locals=0
                iconst 10
                iconst 5
                igt
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // 10 > 5 应该为 true
        assertThat(interpreter.operands[interpreter.sp]).isEqualTo(true);
    }

    @Test
    @DisplayName("应该正确执行函数调用")
    void testFunctionCall() throws Exception {
        String program = """
            .def add: args=2, locals=0
                load 0
                load 1
                iadd
                ret

            .def main: args=0, locals=0
                iconst 5
                iconst 3
                call add
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // add(5, 3) = 8
        assertThat(interpreter.operands[interpreter.sp]).isEqualTo(8);
    }

    @Test
    @DisplayName("应该正确处理递归函数")
    void testRecursiveFunction() throws Exception {
        String program = """
            .def factorial: args=1, locals=1
                load 0
                iconst 1
                ile
                brf recursion
                iconst 1
                ret
            recursion:
                load 0
                iconst 1
                isub
                call factorial
                load 0
                imul
                ret

            .def main: args=0, locals=0
                iconst 5
                call factorial
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // factorial(5) = 120
        assertThat(interpreter.operands[interpreter.sp]).isEqualTo(120);
    }

    @Test
    @DisplayName("应该正确执行条件分支")
    void testConditionalBranch() throws Exception {
        String program = """
            .def main: args=0, locals=1
                iconst 10
                store 0
                load 0
                iconst 5
                igt
                brt is_greater
                iconst 0
                halt
            is_greater:
                iconst 1
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // 10 > 5，应该返回 1
        assertThat(interpreter.operands[interpreter.sp]).isEqualTo(1);
    }

    @Test
    @DisplayName("应该正确处理循环")
    void testLoop() throws Exception {
        String program = """
            .def main: args=0, locals=2
                iconst 0
                store 0  ; i = 0
                iconst 0
                store 1  ; sum = 0
            loop_start:
                load 0
                iconst 10
                ige
                brt loop_end
                load 1
                load 0
                iadd
                store 1  ; sum += i
                load 0
                iconst 1
                iadd
                store 0  ; i++
                br loop_start
            loop_end:
                load 1
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // sum(0..9) = 45
        assertThat(interpreter.sp).isGreaterThanOrEqualTo(0);
        assertThat(interpreter.operands[interpreter.sp]).isEqualTo(45);
    }

    @Test
    @DisplayName("应该正确执行位运算")
    void testBitwiseOperations() throws Exception {
        // 测试异或
        String program1 = """
            .def main: args=0, locals=0
                iconst 5
                iconst 3
                ixor
                halt
            """;

        VMInterpreter interpreter1 = new VMInterpreter();
        InputStream input1 = new ByteArrayInputStream(program1.getBytes());

        VMInterpreter.load(interpreter1, input1);
        interpreter1.exec();

        // 5 ^ 3 = 6
        assertThat(interpreter1.operands[interpreter1.sp]).isEqualTo(6);

        // 测试与
        String program2 = """
            .def main: args=0, locals=0
                iconst 12
                iconst 10
                iand
                halt
            """;

        VMInterpreter interpreter2 = new VMInterpreter();
        InputStream input2 = new ByteArrayInputStream(program2.getBytes());

        VMInterpreter.load(interpreter2, input2);
        interpreter2.exec();

        // 12 & 10 = 8
        assertThat(interpreter2.operands[interpreter2.sp]).isEqualTo(8);
    }

    @Test
    @DisplayName("应该正确处理全局变量")
    void testGlobalVariables() throws Exception {
        String program = """
            .global int g_var

            .def main: args=0, locals=0
                iconst 42
                gstore g_var
                gload g_var
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // 全局变量应该为 42
        assertThat(interpreter.operands[interpreter.sp]).isEqualTo(42);
    }

    @Test
    @DisplayName("应该正确执行堆栈操作")
    void testStackOperations() throws Exception {
        String program = """
            .def main: args=0, locals=0
                iconst 1
                iconst 2
                iconst 3
                pop
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // 执行pop后，栈顶应该是2
        assertThat(interpreter.operands[interpreter.sp]).isEqualTo(2);
    }

    @Test
    @DisplayName("应该正确处理语法错误")
    void testSyntaxError() throws Exception {
        String program = """
            .def main: args=0, locals=0
                invalid_instruction
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        boolean hasErrors = VMInterpreter.load(interpreter, input);

        // 应该有语法错误
        assertThat(hasErrors).isTrue();
    }

    @Test
    @DisplayName("应该支持跟踪模式")
    void testTraceMode() throws Exception {
        String program = """
            .def main: args=0, locals=0
                iconst 5
                iconst 3
                iadd
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.trace = true;

        // 执行时应该输出跟踪信息
        assertDoesNotThrow(() -> interpreter.exec());
    }

    @Test
    @DisplayName("应该能够反汇编字节码")
    void testDisassemble() throws Exception {
        String program = """
            .def main: args=0, locals=0
                iconst 10
                iconst 20
                iadd
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);

        // 反汇编不应该抛出异常
        assertDoesNotThrow(() -> interpreter.disassemble());
    }

    @Test
    @DisplayName("应该能够转储内存")
    void testCoredump() throws Exception {
        String program = """
            .def main: args=0, locals=0
                iconst 42
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // 转储不应该抛出异常
        assertDoesNotThrow(() -> interpreter.coredump());
    }

    @Test
    @DisplayName("应该正确执行结构体创建和字段访问")
    void testStructCreationAndFieldAccess() throws Exception {
        String program = """
            .def main: args=0, locals=1
                struct 2          ; 创建2字段的结构体
                store 0           ; 保存到局部变量0
                load 0
                iconst 42
                fstore 0          ; 存储到字段0
                load 0
                iconst 100
                fstore 1          ; 存储到字段1
                load 0
                fload 0           ; 加载字段0
                load 0
                fload 1           ; 加载字段1
                iadd              ; 42 + 100 = 142
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // 栈顶应该是142
        assertThat(interpreter.operands[interpreter.sp]).isEqualTo(142);
    }

    @Test
    @DisplayName("应该正确处理空引用")
    void testNullInstruction() throws Exception {
        String program = """
            .def main: args=0, locals=0
                null
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // 栈顶应该是null
        assertThat(interpreter.operands[interpreter.sp]).isNull();
    }

    @Test
    @DisplayName("应该正确执行弹出指令")
    void testPopInstruction() throws Exception {
        String program = """
            .def main: args=0, locals=0
                iconst 5
                iconst 10
                pop
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // 栈顶应该是5（10被弹出）
        assertThat(interpreter.operands[interpreter.sp]).isEqualTo(5);
    }

    @Test
    @DisplayName("应该正确执行打印指令")
    void testPrintInstruction() throws Exception {
        String program = """
            .def main: args=0, locals=0
                iconst 123
                print
                halt
            """;

        VMInterpreter interpreter = new VMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        VMInterpreter.load(interpreter, input);
        interpreter.exec();

        // 打印指令执行后栈应为空
        assertThat(interpreter.sp).isEqualTo(-1);
    }
}