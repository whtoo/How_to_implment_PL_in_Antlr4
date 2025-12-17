package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep18r.stackvm.RegisterVMInterpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 无限循环修复测试
 * 验证修复后的虚拟机能够正确处理可能导致无限循环的程序
 */
@DisplayName("无限循环修复测试")
public class InfiniteLoopFixTest extends RegisterVMTestBase {

    @Test
    @DisplayName("应该能正确执行简单循环而不陷入无限循环")
    void testSimpleLoop() throws Exception {
        String program = """
            .def main: args=0, locals=2
                li r1, 0
                li r2, 0
                li r3, 0
                li r4, 10
            loop_start:
                li r5, 0
                li r6, 0
                li r7, 0
                li r8, 10
                sge r9, r1, r8
                jt r9, loop_end
                add r3, r3, r1
                li r10, 1
                add r1, r1, r10
                j loop_start
            loop_end:
                halt
            """;

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();

        // 执行程序（应该正常终止）
        assertDoesNotThrow(() -> interpreter.exec());

        // 验证结果：r3应该是45 (0+1+2+...+9)
        assertThat(interpreter.getRegister(3)).isEqualTo(45);
    }

    @Test
    @DisplayName("应该在检测到无限循环时抛出异常")
    void testInfiniteLoopDetection() throws Exception {
        String infiniteLoopProgram = """
            .def main: args=0, locals=0
                li r1, 1
                j 4
                halt
            """;

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        InputStream input = new ByteArrayInputStream(infiniteLoopProgram.getBytes());

        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();

        // 应该抛出无限循环检测异常
        assertThrows(RuntimeException.class, () -> {
            interpreter.exec();
        }, "应该检测到无限循环并抛出异常");
    }

    @Test
    @DisplayName("应该能正确处理条件跳转")
    void testConditionalJumps() throws Exception {
        String program = """
            .def main: args=0, locals=1
                li r1, 5
                li r2, 10
                sgt r3, r1, r2
                jt r3, skip_print
                li r1, 15
            skip_print:
                halt
            """;

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();

        // 执行程序（应该正常终止）
        assertDoesNotThrow(() -> interpreter.exec());
    }

    @Test
    @DisplayName("应该能正确处理函数调用")
    void testFunctionCalls() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 5
                li r2, 3
                call add_func
                halt

            .def add_func: args=2, locals=0
                add r1, r1, r2
                ret
            """;

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();

        // 执行程序（应该正常终止）
        assertDoesNotThrow(() -> interpreter.exec());
    }

    @Test
    @DisplayName("应该在跳转目标无效时抛出异常")
    void testInvalidJumpTarget() throws Exception {
        String program = """
            .def main: args=0, locals=0
                j 999999       ; 无效的跳转目标
                halt
            """;

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();

        // 应该抛出无效跳转目标异常
        assertThrows(IllegalArgumentException.class, () -> {
            interpreter.exec();
        }, "应该检测到无效跳转目标并抛出异常");
    }
}