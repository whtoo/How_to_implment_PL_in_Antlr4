package org.teachfx.antlr4.ep18;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic.ArithmeticExecutor;
import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.VMConfig;
import org.teachfx.antlr4.ep18.stackvm.StackFrame;
import org.teachfx.antlr4.ep18.stackvm.VMOverflowException;
import org.teachfx.antlr4.ep18.stackvm.VMDivisionByZeroException;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ArithmeticExecutor单元测试
 * 测试算术指令执行器的功能
 */
@DisplayName("ArithmeticExecutor算术指令执行器测试")
public class ArithmeticExecutorTest {

    private VMExecutionContext createContext() {
        VMConfig config = new VMConfig.Builder()
            .setHeapSize(1000)
            .setStackSize(100)
            .setMaxFrameCount(10)
            .setDebugMode(false)
            .setTraceEnabled(false)
            .build();

        return new VMExecutionContext(
            null, config, null, 0,
            new int[100], 0,
            new int[100], new int[100],
            new StackFrame[10], -1,
            false, 0,
            null, 1
        );
    }

    @Test
    @DisplayName("应该正确执行整数加法")
    void testIntegerAdd() throws Exception {
        VMExecutionContext context = createContext();

        // 正常加法
        assertThat(ArithmeticExecutor.add(context, 10, 20)).isEqualTo(30);
        assertThat(ArithmeticExecutor.add(context, -5, 15)).isEqualTo(10);
        assertThat(ArithmeticExecutor.add(context, 0, 0)).isEqualTo(0);

        // 溢出检测
        assertThrows(VMOverflowException.class, () -> {
            ArithmeticExecutor.add(context, Integer.MAX_VALUE, 1);
        });

        assertThrows(VMOverflowException.class, () -> {
            ArithmeticExecutor.add(context, Integer.MIN_VALUE, -1);
        });
    }

    @Test
    @DisplayName("应该正确执行整数减法")
    void testIntegerSubtract() throws Exception {
        VMExecutionContext context = createContext();

        // 正常减法
        assertThat(ArithmeticExecutor.subtract(context, 20, 10)).isEqualTo(10);
        assertThat(ArithmeticExecutor.subtract(context, 10, 20)).isEqualTo(-10);
        assertThat(ArithmeticExecutor.subtract(context, 0, 0)).isEqualTo(0);

        // 溢出检测
        assertThrows(VMOverflowException.class, () -> {
            ArithmeticExecutor.subtract(context, Integer.MIN_VALUE, 1);
        });
    }

    @Test
    @DisplayName("应该正确执行整数乘法")
    void testIntegerMultiply() throws Exception {
        VMExecutionContext context = createContext();

        // 正常乘法
        assertThat(ArithmeticExecutor.multiply(context, 10, 20)).isEqualTo(200);
        assertThat(ArithmeticExecutor.multiply(context, -5, 6)).isEqualTo(-30);
        assertThat(ArithmeticExecutor.multiply(context, 0, 100)).isEqualTo(0);

        // 溢出检测
        assertThrows(VMOverflowException.class, () -> {
            ArithmeticExecutor.multiply(context, Integer.MAX_VALUE, 2);
        });
    }

    @Test
    @DisplayName("应该正确执行整数除法")
    void testIntegerDivide() throws Exception {
        VMExecutionContext context = createContext();

        // 正常除法
        assertThat(ArithmeticExecutor.divide(context, 20, 10)).isEqualTo(2);
        assertThat(ArithmeticExecutor.divide(context, 17, 3)).isEqualTo(5);
        assertThat(ArithmeticExecutor.divide(context, -20, 4)).isEqualTo(-5);

        // 除以零
        assertThrows(VMDivisionByZeroException.class, () -> {
            ArithmeticExecutor.divide(context, 10, 0);
        });

        // 溢出检测：Integer.MIN_VALUE / -1
        assertThrows(VMOverflowException.class, () -> {
            ArithmeticExecutor.divide(context, Integer.MIN_VALUE, -1);
        });
    }

    @Test
    @DisplayName("应该正确执行位运算")
    void testBitwiseOperations() {
        // 与运算
        assertThat(ArithmeticExecutor.and(12, 8)).isEqualTo(8);  // 1100 & 1000 = 1000
        assertThat(ArithmeticExecutor.and(15, 7)).isEqualTo(7);  // 1111 & 0111 = 0111

        // 或运算
        assertThat(ArithmeticExecutor.or(12, 8)).isEqualTo(12);  // 1100 | 1000 = 1100
        assertThat(ArithmeticExecutor.or(8, 4)).isEqualTo(12);   // 1000 | 0100 = 1100

        // 异或运算
        assertThat(ArithmeticExecutor.xor(12, 8)).isEqualTo(4);  // 1100 ^ 1000 = 0100
        assertThat(ArithmeticExecutor.xor(7, 3)).isEqualTo(4);   // 0111 ^ 0011 = 0100

        // 非运算
        assertThat(ArithmeticExecutor.not(12)).isEqualTo(-13);   // ~12 = -13
        assertThat(ArithmeticExecutor.not(-1)).isEqualTo(0);     // ~-1 = 0
    }

    @Test
    @DisplayName("应该正确执行移位运算")
    void testShiftOperations() throws Exception {
        VMExecutionContext context = createContext();

        // 左移
        assertThat(ArithmeticExecutor.shiftLeft(context, 4, 2)).isEqualTo(16);   // 4 << 2 = 16
        assertThat(ArithmeticExecutor.shiftLeft(context, 1, 3)).isEqualTo(8);    // 1 << 3 = 8

        // 右移（算术右移，保留符号位）
        assertThat(ArithmeticExecutor.shiftRight(16, 2)).isEqualTo(4);    // 16 >> 2 = 4
        assertThat(ArithmeticExecutor.shiftRight(-8, 1)).isEqualTo(-4);   // -8 >> 1 = -4

        // 无符号右移
        assertThat(ArithmeticExecutor.unsignedShiftRight(16, 2)).isEqualTo(4);        // 16 >>> 2 = 4
        assertThat(ArithmeticExecutor.unsignedShiftRight(-1, 1)).isEqualTo(2147483647); // -1 >>> 1 = 2147483647

        // 无效移位量
        assertThrows(IllegalArgumentException.class, () -> {
            ArithmeticExecutor.shiftLeft(context, 10, -1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ArithmeticExecutor.shiftLeft(context, 10, 32);
        });
    }

    @Test
    @DisplayName("应该正确执行浮点运算")
    void testFloatOperations() {
        // 浮点加法
        assertThat(ArithmeticExecutor.add(10.5f, 20.3f)).isEqualTo(30.8f);

        // 浮点减法
        assertThat(ArithmeticExecutor.subtract(20.5f, 10.2f)).isEqualTo(10.3f);

        // 浮点乘法
        assertThat(ArithmeticExecutor.multiply(5.0f, 3.0f)).isEqualTo(15.0f);

        // 浮点除法
        assertThat(ArithmeticExecutor.divide(15.0f, 3.0f)).isEqualTo(5.0f);

        // 浮点除以零
        assertThrows(ArithmeticException.class, () -> {
            ArithmeticExecutor.divide(10.0f, 0.0f);
        });

        // 类型转换
        assertThat(ArithmeticExecutor.intToFloat(42)).isEqualTo(42.0f);
    }

    @Test
    @DisplayName("应该正确执行类型转换")
    void testTypeConversion() throws Exception {
        VMExecutionContext context = createContext();

        // 整数转浮点数
        assertThat(ArithmeticExecutor.intToFloat(42)).isEqualTo(42.0f);
        assertThat(ArithmeticExecutor.intToFloat(-10)).isEqualTo(-10.0f);

        // 浮点数转整数
        assertThat(ArithmeticExecutor.floatToInt(context, 42.7f)).isEqualTo(42);
        assertThat(ArithmeticExecutor.floatToInt(context, -10.9f)).isEqualTo(-10);

        // 浮点数转整数溢出
        assertThrows(VMOverflowException.class, () -> {
            ArithmeticExecutor.floatToInt(context, (float) Integer.MAX_VALUE + 1000);
        });
    }

    @Test
    @DisplayName("应该正确检查溢出条件")
    void testOverflowCheck() {
        VMExecutionContext context = createContext();

        // 加法溢出检查
        assertThat(ArithmeticExecutor.willAdditionOverflow(Integer.MAX_VALUE, 1)).isTrue();
        assertThat(ArithmeticExecutor.willAdditionOverflow(10, 20)).isFalse();

        // 减法溢出检查
        assertThat(ArithmeticExecutor.willSubtractionOverflow(Integer.MIN_VALUE, 1)).isTrue();
        assertThat(ArithmeticExecutor.willSubtractionOverflow(10, 5)).isFalse();

        // 乘法溢出检查
        assertThat(ArithmeticExecutor.willMultiplicationOverflow(Integer.MAX_VALUE, 2)).isTrue();
        assertThat(ArithmeticExecutor.willMultiplicationOverflow(10, 5)).isFalse();
    }
}
