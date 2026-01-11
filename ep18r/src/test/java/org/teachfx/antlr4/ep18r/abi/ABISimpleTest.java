package org.teachfx.antlr4.ep18r.abi;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("简化版ABI测试")
class ABISimpleTest {

    private RegisterVMInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new RegisterVMInterpreter();
    }

    @Test
    @DisplayName("简单加法测试")
    void testSimpleAdd() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li r1, 10
                li r2, 20
                add r3, r1, r2
                halt
            """;

        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).as("Program should load without errors").isFalse();

        assertThatCode(() -> interpreter.exec()).doesNotThrowAnyException();
        assertThat(interpreter.getRegister(1)).as("r1 should be 10").isEqualTo(10);
        assertThat(interpreter.getRegister(2)).as("r2 should be 20").isEqualTo(20);
        assertThat(interpreter.getRegister(3)).as("r3 should be 30").isEqualTo(30);
    }

    @Test
    @DisplayName("寄存器别名测试")
    void testRegisterAliases() throws Exception {
        String program = """
            .def main: args=0, locals=0
                li a0, 10
                li a1, 20
                add a0, a0, a1
                halt
            """;

        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).as("Program should load without errors").isFalse();

        assertThatCode(() -> interpreter.exec()).doesNotThrowAnyException();
        assertThat(interpreter.getRegister(2)).as("a0 should be 30").isEqualTo(30);
        assertThat(interpreter.getRegister(3)).as("a1 should be 20").isEqualTo(20);
    }
}
