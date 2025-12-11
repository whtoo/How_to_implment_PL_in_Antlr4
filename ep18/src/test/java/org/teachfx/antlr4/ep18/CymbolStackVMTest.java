package org.teachfx.antlr4.ep18;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep18.stackvm.CymbolStackVM;
import org.teachfx.antlr4.ep18.stackvm.VMConfig;
import org.teachfx.antlr4.ep18.stackvm.VMDivisionByZeroException;
import org.teachfx.antlr4.ep18.stackvm.BytecodeDefinition;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CymbolStackVM单元测试
 * 测试虚拟机核心功能和指令执行
 */
@DisplayName("CymbolStackVM虚拟机测试")
public class CymbolStackVMTest extends VMTestBase {

    @Test
    @DisplayName("应该正确创建虚拟机实例")
    void testVMCreation() {
        assertThat(vm).isNotNull();
        assertThat(vm.getConfig()).isNotNull();
        assertThat(vm.isRunning()).isFalse();
    }

    @Test
    @DisplayName("应该正确执行整数加法")
    void testIntegerAddition() throws Exception {
        // 创建字节码：iconst 5; iconst 3; iadd; halt;
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        assertThat(result).isEqualTo(8);
    }

    @Test
    @DisplayName("应该正确执行整数减法")
    void testIntegerSubtraction() throws Exception {
        // 创建字节码：iconst 10; iconst 3; isub; halt;
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 10),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),
            encodeInstruction(BytecodeDefinition.INSTR_ISUB),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        assertThat(result).isEqualTo(7);
    }

    @Test
    @DisplayName("应该正确执行整数乘法")
    void testIntegerMultiplication() throws Exception {
        // 创建字节码：iconst 5; iconst 6; imul; halt;
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 6),
            encodeInstruction(BytecodeDefinition.INSTR_IMUL),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        assertThat(result).isEqualTo(30);
    }

    @Test
    @DisplayName("应该正确执行整数除法")
    void testIntegerDivision() throws Exception {
        // 创建字节码：iconst 15; iconst 3; idiv; halt;
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 15),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),
            encodeInstruction(BytecodeDefinition.INSTR_IDIV),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        assertThat(result).isEqualTo(5);
    }

    @Test
    @DisplayName("应该正确抛出除零异常")
    void testDivisionByZero() {
        // 创建字节码：iconst 10; iconst 0; idiv; halt;
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 10),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0),
            encodeInstruction(BytecodeDefinition.INSTR_IDIV),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        assertThatThrownBy(() -> execute(bytecode))
            .isInstanceOf(VMDivisionByZeroException.class);
    }

    @Test
    @DisplayName("应该正确执行比较指令")
    void testComparisonInstructions() throws Exception {
        // 测试 5 > 3，应该返回 1
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),
            encodeInstruction(BytecodeDefinition.INSTR_IGT),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        assertThat(result).isEqualTo(1);

        // 测试 3 > 5，应该返回 0
        bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),
            encodeInstruction(BytecodeDefinition.INSTR_IGT),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        result = execute(bytecode);
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("应该正确执行逻辑指令")
    void testLogicalInstructions() throws Exception {
        // 测试整数异或
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),  // 5
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),  // 3
            encodeInstruction(BytecodeDefinition.INSTR_IXOR),       // 5 ^ 3 = 6
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        assertThat(result).isEqualTo(6);

        // 测试整数与
        bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 6),  // 6 (110)
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),  // 3 (011)
            encodeInstruction(BytecodeDefinition.INSTR_IAND),       // 6 & 3 = 2 (010)
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        result = execute(bytecode);
        assertThat(result).isEqualTo(2);
    }

    @Test
    @DisplayName("应该正确处理复杂表达式")
    void testComplexExpression() throws Exception {
        // 计算 (10 + 5) * 2 - 8 / 4
        // 结果应该是：30 - 2 = 28
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 10), // 10
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),  // 5
            encodeInstruction(BytecodeDefinition.INSTR_IADD),       // 10 + 5 = 15
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 2),  // 2
            encodeInstruction(BytecodeDefinition.INSTR_IMUL),       // 15 * 2 = 30
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 8),  // 8
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 4),  // 4
            encodeInstruction(BytecodeDefinition.INSTR_IDIV),       // 8 / 4 = 2
            encodeInstruction(BytecodeDefinition.INSTR_ISUB),       // 30 - 2 = 28
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        assertThat(result).isEqualTo(28);
    }

    @Test
    @DisplayName("应该正确处理负数")
    void testNegativeNumbers() throws Exception {
        // 计算 -5 + 3 = -2
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, -5), // -5
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),  // 3
            encodeInstruction(BytecodeDefinition.INSTR_IADD),       // -5 + 3 = -2
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        assertThat(result).isEqualTo(-2);
    }

    @Test
    @DisplayName("应该正确处理空字节码")
    void testEmptyBytecode() {
        byte[] bytecode = new byte[0];

        assertThatThrownBy(() -> execute(bytecode))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Bytecode cannot be null or empty");
    }

    @Test
    @DisplayName("应该正确处理null字节码")
    void testNullBytecode() {
        assertThatThrownBy(() -> execute(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Bytecode cannot be null or empty");
    }

    @Test
    @DisplayName("应该正确执行负数常量")
    void testNegativeConstants() throws Exception {
        // 测试有符号常量扩展
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, -10), // -10
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        assertThat(result).isEqualTo(-10);
    }
}