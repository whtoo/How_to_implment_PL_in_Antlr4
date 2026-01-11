package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("字节码编码测试")
class BytecodeEncodingTest {

    @Test
    @DisplayName("检查简单跳转指令编码")
    void testJumpEncoding() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 1
                j 8
                halt
            """;

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).as("Program should load without errors").isFalse();

        assertThatCode(() -> interpreter.exec()).doesNotThrowAnyException();
        assertThat(interpreter.getRegister(1)).as("r1 should be 1 after li").isEqualTo(1);
    }

    @Test
    @DisplayName("检查sw_f和lw_f指令编码")
    void testStructFieldEncoding() throws Exception {
        String program = """
            .def main: args=0, locals=0
                struct r1, 2
                li r2, 10
                sw_f r2, r1, 0
                li r3, 20
                sw_f r3, r1, 4
                lw_f r4, r1, 0
                lw_f r5, r1, 4
                halt
            """;

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).as("Program should load without errors").isFalse();

        assertThatCode(() -> interpreter.exec()).doesNotThrowAnyException();
        assertThat(interpreter.getRegister(4)).as("field[0] should be 10").isEqualTo(10);
        assertThat(interpreter.getRegister(5)).as("field[1] should be 20").isEqualTo(20);
    }
}
