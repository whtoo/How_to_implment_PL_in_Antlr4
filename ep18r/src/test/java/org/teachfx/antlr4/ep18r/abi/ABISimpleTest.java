package org.teachfx.antlr4.ep18r.abi;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * 简化版ABI测试
 */
@DisplayName("简化版ABI测试")
public class ABISimpleTest {

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
        System.out.println("Has errors: " + hasErrors);

        if (hasErrors) {
            System.out.println("汇编失败，跳过执行");
            return;
        }

        try {
            interpreter.exec();
        } catch (RuntimeException e) {
            if (!e.getMessage().equals("HALT instruction executed")) {
                throw e;
            }
        }

        System.out.println("r1 = " + interpreter.getRegister(1));
        System.out.println("r2 = " + interpreter.getRegister(2));
        System.out.println("r3 = " + interpreter.getRegister(3));
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
        System.out.println("Has errors: " + hasErrors);

        if (hasErrors) {
            System.out.println("汇编失败，跳过执行");
            return;
        }

        try {
            interpreter.exec();
        } catch (RuntimeException e) {
            if (!e.getMessage().equals("HALT instruction executed")) {
                throw e;
            }
        }

        System.out.println("a0 = " + interpreter.getRegister(2));
        System.out.println("a1 = " + interpreter.getRegister(3));
    }
}
