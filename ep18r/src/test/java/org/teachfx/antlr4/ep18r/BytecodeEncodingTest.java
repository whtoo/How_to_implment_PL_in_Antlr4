package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class BytecodeEncodingTest {

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
        assertThat(hasErrors).isFalse();

        byte[] code = interpreter.getCode();
        System.out.println("=== 简单跳转程序字节码 ===");
        for (int i = 0; i < code.length; i += 4) {
            if (i + 3 < code.length) {
                int instr = ((code[i] & 0xFF)) |
                           ((code[i+1] & 0xFF) << 8) |
                           ((code[i+2] & 0xFF) << 16) |
                           ((code[i+3] & 0xFF) << 24);
                System.out.printf("  [%d] 0x%08X\n", i, instr);
            }
        }

        // 执行程序
        assertDoesNotThrow(() -> interpreter.exec());
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
        assertThat(hasErrors).isFalse();

        byte[] code = interpreter.getCode();
        System.out.println("=== 结构体字段访问程序字节码 ===");
        for (int i = 0; i < code.length; i += 4) {
            if (i + 3 < code.length) {
                int instr = ((code[i] & 0xFF)) |
                           ((code[i+1] & 0xFF) << 8) |
                           ((code[i+2] & 0xFF) << 16) |
                           ((code[i+3] & 0xFF) << 24);
                System.out.printf("  [%d] 0x%08X\n", i, instr);
            }
        }

        // 执行程序
        assertDoesNotThrow(() -> interpreter.exec());
        System.out.println("r1 (struct) = " + interpreter.getRegister(1));
        System.out.println("r4 (field[0]) = " + interpreter.getRegister(4));
        System.out.println("r5 (field[1]) = " + interpreter.getRegister(5));
    }
}
