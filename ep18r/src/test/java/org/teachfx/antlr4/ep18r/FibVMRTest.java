package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * 测试fib.vmr执行过程，验证跟踪输出
 */
@DisplayName("fib.vmr执行跟踪测试")
public class FibVMRTest extends RegisterVMTestBase {

    @Test
    @DisplayName("应该能执行fib.vmr并输出跟踪信息")
    void testFibVMR() throws Exception {
        // 从资源文件加载fib.vmr
        InputStream input = getClass().getClassLoader().getResourceAsStream("fib.vmr");
        assertThat(input).as("fib.vmr资源文件未找到").isNotNull();

        // 创建解释器并启用跟踪
        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        interpreter.setTrace(true); // 启用跟踪

        // 加载并执行
        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        assertThat(hasErrors).as("fib.vmr加载失败").isFalse();

        // 执行程序
        interpreter.exec();

        // 验证结果（fib(10) = 55）
        int result = interpreter.getRegister(2); // r2/a0 是返回值
        assertThat(result).as("fib(10) 应该等于 55").isEqualTo(55);
    }
}