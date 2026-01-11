package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 简单验证测试 - 确保修复没有破坏基本功能
 */
@DisplayName("简单验证测试")
public class SimpleVerificationTest extends RegisterVMTestBase {

    @Test
    @DisplayName("应该能执行基本的算术运算")
    void testBasicArithmetic() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 10
                li r2, 20
                add r3, r1, r2
                halt
            """;

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();

        // 执行程序
        interpreter.exec();

        // 验证结果：r3应该是30
        assertThat(interpreter.getRegister(3)).isEqualTo(30);
    }

    @Test
    @DisplayName("应该检测到无限循环")
    void testInfiniteLoop() throws Exception {
        String program = """
            .def main: args=0, locals=0
                j 0
                halt
            """;

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();

        // 应该抛出无限循环检测异常
        assertThrows(RuntimeException.class, () -> {
            interpreter.exec();
        });
    }
}