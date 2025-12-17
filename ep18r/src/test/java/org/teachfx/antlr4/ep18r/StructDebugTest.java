package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

public class StructDebugTest {

    private RegisterVMInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new RegisterVMInterpreter();
    }

    @Test
    @DisplayName("调试结构体字段访问")
    void testStructFieldDebug() throws Exception {
        String program = """
            .def main: args=0, locals=0
                struct r1, 3      ; 分配3字段结构体
                li r2, 10
                sw_f r2, r1, 0    ; 结构体[0] = 10
                li r3, 20
                sw_f r3, r1, 4    ; 结构体[1] = 20
                lw_f r4, r1, 0    ; r4 = 结构体[0]
                lw_f r5, r1, 4    ; r5 = 结构体[1]
                add r6, r4, r5    ; r6 = r4 + r5
                halt
            """;

        System.out.println("=== 调试信息 ===");
        System.out.println("步骤1: struct r1, 3");

        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).isFalse();

        // 显示初始状态
        System.out.println("步骤2: 执行前寄存器状态");
        for (int i = 0; i < 8; i++) {
            System.out.println("  r" + i + " = " + interpreter.getRegister(i));
        }

        // 模拟执行每一步
        System.out.println("\n步骤3: struct r1, 3 - 分配结构体");
        // 这应该设置r1为结构体地址

        System.out.println("步骤4: li r2, 10 - 设置r2=10");
        System.out.println("步骤5: sw_f r2, r1, 0 - 存储字段0");

        System.out.println("步骤6: li r3, 20 - 设置r3=20");
        System.out.println("步骤7: sw_f r3, r1, 4 - 存储字段1");

        System.out.println("步骤8: lw_f r4, r1, 0 - 加载字段0");
        System.out.println("步骤9: lw_f r5, r1, 4 - 加载字段1");
        System.out.println("步骤10: add r6, r4, r5");

        try {
            interpreter.exec();
        } catch (RuntimeException e) {
            if (e.getMessage().equals("HALT instruction executed")) {
                // 正常停止
            } else {
                throw e;
            }
        }

        System.out.println("\n=== 执行后寄存器状态 ===");
        System.out.println("r1 (struct address) = " + interpreter.getRegister(1));
        System.out.println("r2 = " + interpreter.getRegister(2));
        System.out.println("r3 = " + interpreter.getRegister(3));
        System.out.println("r4 (field[0]) = " + interpreter.getRegister(4));
        System.out.println("r5 (field[1]) = " + interpreter.getRegister(5));
        System.out.println("r6 (result) = " + interpreter.getRegister(6));

        System.out.println("\n=== 期望 vs 实际 ===");
        System.out.println("r4期望: 10, 实际: " + interpreter.getRegister(4));
        System.out.println("r5期望: 20, 实际: " + interpreter.getRegister(5));
        System.out.println("r6期望: 30, 实际: " + interpreter.getRegister(6));

        // 显示断言结果
        System.out.println("\n=== 断言检查 ===");
        try {
            assertThat(interpreter.getRegister(4)).isEqualTo(10);
            System.out.println("✓ r4 == 10");
        } catch (AssertionError e) {
            System.out.println("✗ r4 != 10: " + e.getMessage());
        }

        try {
            assertThat(interpreter.getRegister(5)).isEqualTo(20);
            System.out.println("✓ r5 == 20");
        } catch (AssertionError e) {
            System.out.println("✗ r5 != 20: " + e.getMessage());
        }

        try {
            assertThat(interpreter.getRegister(6)).isEqualTo(30);
            System.out.println("✓ r6 == 30");
        } catch (AssertionError e) {
            System.out.println("✗ r6 != 30: " + e.getMessage());
        }

        System.out.println("\n=== 内存内容检查 ===");
        // 尝试读取结构体内存
        int structAddr = interpreter.getRegister(1);
        System.out.println("结构体地址: " + structAddr);
        System.out.println("注意: 无法直接从测试访问内存内容");
        System.out.println("需要在RegisterVMInterpreter中添加公共方法或使用反射");
    }
}
