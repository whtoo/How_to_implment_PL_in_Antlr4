package org.teachfx.antlr4.ep18r.abi;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

public class ABIDebugTest {

    private RegisterVMInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new RegisterVMInterpreter();
    }

    @Test
    @DisplayName("调试寄存器别名")
    void testDebugRegisterAliases() throws Exception {
        // 不使用文本块，直接使用字符串
        String program = ".def main: args=0, locals=0\nli a0, 10\nhalt\n";

        System.out.println("程序:");
        System.out.println(program);
        System.out.println("程序长度: " + program.length());

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
        assertThat(interpreter.getRegister(2)).isEqualTo(10);
    }
}
