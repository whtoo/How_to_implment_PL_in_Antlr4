package org.teachfx.antlr4.ep18r.stackvm.interpreter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PRINT指令测试")
public class PrintInstructionTest {

    @Test
    @DisplayName("应该能执行mov_test.vmr并打印输出")
    void testPrintInstruction() throws Exception {
        // 从资源文件加载mov_test.vmr
        InputStream input = getClass().getClassLoader().getResourceAsStream("mov_test.vmr");
        assertThat(input).as("mov_test.vmr资源文件未找到").isNotNull();

        // 使用GUI相同的配置创建解释器
        VMConfig config = new VMConfig.Builder()
            .setHeapSize(1024 * 1024)  // 1MB 堆
            .setStackSize(1024)        // 1K 局部变量
            .setMaxStackDepth(100)     // 最大调用深度
            .setDebugMode(true)
            .build();
        
        RegisterVMInterpreter interpreter = new RegisterVMInterpreter(config);
        interpreter.setTrace(true); // 启用跟踪

        // 加载并执行
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).as("mov_test.vmr加载失败").isFalse();

        // 执行程序
        interpreter.exec();

        // 验证结果：r2应该为42
        int result = interpreter.getRegister(2); // r2/a0 是返回值
        assertThat(result).as("r2应该等于42").isEqualTo(42);
        
        // r3应该也为42 (mov指令复制)
        int r3 = interpreter.getRegister(3);
        assertThat(r3).as("r3应该等于42").isEqualTo(42);
    }

    @Test
    @DisplayName("应该能执行fib.vmr并打印输出")
    void testPrintInFib() throws Exception {
        // 从资源文件加载fib.vmr
        InputStream input = getClass().getClassLoader().getResourceAsStream("fib.vmr");
        assertThat(input).as("fib.vmr资源文件未找到").isNotNull();

        // 使用GUI相同的配置创建解释器
        VMConfig config = new VMConfig.Builder()
            .setHeapSize(1024 * 1024)  // 1MB 堆
            .setStackSize(1024)        // 1K 局部变量
            .setMaxStackDepth(100)     // 最大调用深度
            .setDebugMode(true)
            .build();
        
        RegisterVMInterpreter interpreter = new RegisterVMInterpreter(config);
        interpreter.setTrace(true); // 启用跟踪

        // 加载并执行
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).as("fib.vmr加载失败").isFalse();

        // 执行程序
        interpreter.exec();

        // 验证结果：fib(10) = 55
        int result = interpreter.getRegister(2); // r2/a0 是返回值
        assertThat(result).as("fib(10) 应该等于 55").isEqualTo(55);
    }
}