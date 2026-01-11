package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

public class BytecodeInspectionTest {

    private RegisterVMInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new RegisterVMInterpreter();
    }

    @Test
    @DisplayName("检查结构体操作字节码")
    void testStructBytecode() throws Exception {
        String program = """
            .def main: args=0, locals=0
                struct r1, 3
                li r2, 10
                sw_f r2, r1, 0
                li r3, 20
                sw_f r3, r1, 4
                lw_f r4, r1, 0
                lw_f r5, r1, 4
                add r6, r4, r5
                halt
            """;

        System.out.println("=== 汇编代码 ===");
        System.out.println(program);

        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();

        System.out.println("\n=== 生成的字节码 ===");
        byte[] code = interpreter.getCode();
        for (int i = 0; i < code.length; i += 4) {
            if (i + 3 < code.length) {
                int instr = ((code[i] & 0xFF)) |
                           ((code[i+1] & 0xFF) << 8) |
                           ((code[i+2] & 0xFF) << 16) |
                           ((code[i+3] & 0xFF) << 24);
                System.out.printf("  [%d] 0x%08X\n", i, instr);
            }
        }

        System.out.println("\n=== 执行结果 ===");
        try {
            interpreter.exec();
        } catch (RuntimeException e) {
            if (!e.getMessage().equals("HALT instruction executed")) {
                throw e;
            }
        }

        System.out.println("r1 (struct addr) = " + interpreter.getRegister(1));
        System.out.println("r2 = " + interpreter.getRegister(2));
        System.out.println("r3 = " + interpreter.getRegister(3));
        System.out.println("r4 (field[0]) = " + interpreter.getRegister(4));
        System.out.println("r5 (field[1]) = " + interpreter.getRegister(5));
        System.out.println("r6 (sum) = " + interpreter.getRegister(6));

        // 添加一个getCode()方法到RegisterVMInterpreter
        assertThat(interpreter.getRegister(6)).isEqualTo(30);
    }
}
