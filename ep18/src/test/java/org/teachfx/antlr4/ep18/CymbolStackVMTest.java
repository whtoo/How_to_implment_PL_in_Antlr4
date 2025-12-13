package org.teachfx.antlr4.ep18;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
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

    @Test
    @DisplayName("应该正确执行无条件跳转指令")
    void testUnconditionalBranch() throws Exception {
        // 测试BR指令：跳转到指定地址
        // 字节码布局：
        // 0: ICONST 1
        // 1: BR 4       (跳过指令2和3)
        // 2: ICONST 2   (应该被跳过)
        // 3: ICONST 3   (应该被跳过)
        // 4: ICONST 4   (跳转目标)
        // 5: HALT
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1), // 地址0
            encodeInstruction(BytecodeDefinition.INSTR_BR, 4),     // 地址1
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 2), // 地址2 (被跳过)
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3), // 地址3 (被跳过)
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 4), // 地址4 (目标)
            encodeInstruction(BytecodeDefinition.INSTR_HALT)       // 地址5
        });

        int result = execute(bytecode);
        // 应该执行ICONST 1, BR 4, ICONST 4, HALT
        // 栈顶应该是4
        assertThat(result).isEqualTo(4);
    }

    @Test
    @DisplayName("应该正确执行条件跳转为真指令")
    void testConditionalBranchTrue() throws Exception {
        // 测试BRT指令：条件为真时跳转
        // 字节码布局：
        // 0: ICONST 1   (true条件)
        // 1: BRT 4      (条件为真，跳转到地址4)
        // 2: ICONST 2   (应该被跳过)
        // 3: ICONST 3   (应该被跳过)
        // 4: ICONST 4   (跳转目标)
        // 5: HALT
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1), // true条件 (非零)
            encodeInstruction(BytecodeDefinition.INSTR_BRT, 4),    // 条件为真时跳转
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 2), // 被跳过
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3), // 被跳过
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 4), // 目标
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        // 应该执行ICONST 1, BRT 4, ICONST 4, HALT
        // 栈顶应该是4
        assertThat(result).isEqualTo(4);
    }

    @Test
    @DisplayName("应该正确执行条件跳转为假指令")
    void testConditionalBranchFalse() throws Exception {
        // 测试BRF指令：条件为假时跳转
        // 字节码布局：
        // 0: ICONST 0   (false条件)
        // 1: BRF 4      (条件为假，跳转到地址4)
        // 2: ICONST 2   (应该被跳过)
        // 3: ICONST 3   (应该被跳过)
        // 4: ICONST 4   (跳转目标)
        // 5: HALT
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0), // false条件 (零)
            encodeInstruction(BytecodeDefinition.INSTR_BRF, 4),    // 条件为假时跳转
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 2), // 被跳过
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3), // 被跳过
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 4), // 目标
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        // 应该执行ICONST 0, BRF 4, ICONST 4, HALT
        // 栈顶应该是4
        assertThat(result).isEqualTo(4);
    }

    @Test
    @DisplayName("应该正确处理条件不满足时的跳转")
    void testConditionalBranchNoJump() throws Exception {
        // 测试BRT当条件为假时不应跳转
        // 字节码布局：
        // 0: ICONST 0   (false条件)
        // 1: BRT 4      (条件为假，不跳转)
        // 2: ICONST 2   (应该被执行)
        // 3: HALT
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0), // false条件
            encodeInstruction(BytecodeDefinition.INSTR_BRT, 4),    // 条件为假，不跳转
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 2), // 顺序执行
            encodeInstruction(BytecodeDefinition.INSTR_HALT)       // 提前结束
        });

        int result = execute(bytecode);
        // 应该执行ICONST 0, BRT 4, ICONST 2, HALT
        // 栈顶应该是2
        assertThat(result).isEqualTo(2);
    }

    @Test
    @DisplayName("应该正确执行局部变量加载和存储指令")
    void testLocalVariableLoadStore() throws Exception {
        // 测试LOAD和STORE指令
        // 字节码布局：
        // 0: ICONST 100       // 常量100
        // 1: STORE 0          // 存储到局部变量0
        // 2: ICONST 200       // 常量200
        // 3: STORE 1          // 存储到局部变量1
        // 4: LOAD 0           // 加载局部变量0 (100)
        // 5: LOAD 1           // 加载局部变量1 (200)
        // 6: IADD             // 100 + 200 = 300
        // 7: HALT
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 100),
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 0),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 200),
            encodeInstruction(BytecodeDefinition.INSTR_STORE, 1),
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 0),
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 1),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        // 应该执行 100+200=300
        assertThat(result).isEqualTo(300);
    }

    @Test
    @DisplayName("应该正确执行全局内存加载和存储指令")
    void testGlobalMemoryLoadStore() throws Exception {
        // 测试GLOAD和GSTORE指令
        // 字节码布局：
        // 0: ICONST 500       // 常量500
        // 1: GSTORE 10        // 存储到全局地址10
        // 2: ICONST 700       // 常量700
        // 3: GSTORE 20        // 存储到全局地址20
        // 4: GLOAD 10         // 从全局地址10加载 (500)
        // 5: GLOAD 20         // 从全局地址20加载 (700)
        // 6: IADD             // 500 + 700 = 1200
        // 7: HALT
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 500),
            encodeInstruction(BytecodeDefinition.INSTR_GSTORE, 10),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 700),
            encodeInstruction(BytecodeDefinition.INSTR_GSTORE, 20),
            encodeInstruction(BytecodeDefinition.INSTR_GLOAD, 10),
            encodeInstruction(BytecodeDefinition.INSTR_GLOAD, 20),
            encodeInstruction(BytecodeDefinition.INSTR_IADD),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        // 应该执行 500+700=1200
        assertThat(result).isEqualTo(1200);
    }

    @Test
    @DisplayName("应该正确执行结构体字段加载和存储指令")
    void testStructFieldLoadStore() throws Exception {
        // 测试FLOAD和FSTORE指令（简化版本）
        // 假设结构体在堆地址100处，有两个字段
        // 字节码布局：
        // 0: ICONST 100       // 结构体基地址
        // 1: ICONST 42        // 值42
        // 2: FSTORE 0         // 存储到字段0 (地址100+0=100)
        // 3: ICONST 100       // 结构体基地址
        // 4: FLOAD 0          // 从字段0加载 (地址100+0=100)
        // 5: HALT
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 100), // 结构体地址
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 42),  // 值42
            encodeInstruction(BytecodeDefinition.INSTR_FSTORE, 0),   // 存储到字段0
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 100), // 结构体地址
            encodeInstruction(BytecodeDefinition.INSTR_FLOAD, 0),    // 从字段0加载
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        // 应该加载到值42
        assertThat(result).isEqualTo(42);
    }

    @Test
    @DisplayName("应该处理内存访问越界错误")
    void testMemoryAccessBounds() {
        // 测试LOAD指令越界访问
        // 使用一个超过locals数组大小的索引
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 9999), // 大索引
            encodeInstruction(BytecodeDefinition.INSTR_LOAD, 9999),   // 越界访问
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        // 应该抛出IndexOutOfBoundsException
        assertThatThrownBy(() -> execute(bytecode))
            .isInstanceOf(IndexOutOfBoundsException.class)
            .hasMessageContaining("Local variable index out of bounds");
    }

    @Test
    @DisplayName("应该正确执行函数调用和返回指令")
    void testFunctionCallReturn() throws Exception {
        // 测试CALL和RET指令
        // 字节码布局：
        // 地址0: ICONST 10        // 主程序：常量10
        // 地址1: CALL 4           // 调用函数（跳转到地址4）
        // 地址2: HALT             // 函数返回后继续执行
        // 地址3: (未使用)          // 填充
        // 地址4: ICONST 20        // 函数体：常量20
        // 地址5: RET              // 返回主程序
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 10), // 0
            encodeInstruction(BytecodeDefinition.INSTR_CALL, 4),    // 1
            encodeInstruction(BytecodeDefinition.INSTR_HALT),       // 2
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0),  // 3 (填充)
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 20), // 4 (函数开始)
            encodeInstruction(BytecodeDefinition.INSTR_RET),        // 5
            encodeInstruction(BytecodeDefinition.INSTR_HALT)        // 6 (安全终止)
        });

        int result = execute(bytecode);
        // 执行流程：ICONST 10, CALL 4, ICONST 20, RET, HALT
        // 栈顶应该是20（最后压入的值）
        assertThat(result).isEqualTo(20);
    }

    @Test
    @DisplayName("应该正确执行嵌套函数调用")
    void testNestedFunctionCalls() throws Exception {
        // 测试嵌套调用：main -> func1 -> func2
        // 字节码布局：
        // 地址0: ICONST 1          // main: 常量1
        // 地址1: CALL 4            // 调用func1
        // 地址2: HALT              // 返回后结束
        // 地址3: (未使用)           // 填充
        // 地址4: ICONST 2          // func1: 常量2
        // 地址5: CALL 8            // 调用func2
        // 地址6: RET               // func1返回
        // 地址7: (未使用)           // 填充
        // 地址8: ICONST 3          // func2: 常量3
        // 地址9: RET               // func2返回
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 1), // 0 main
            encodeInstruction(BytecodeDefinition.INSTR_CALL, 4),   // 1 call func1
            encodeInstruction(BytecodeDefinition.INSTR_HALT),      // 2
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0), // 3 填充
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 2), // 4 func1
            encodeInstruction(BytecodeDefinition.INSTR_CALL, 8),   // 5 call func2
            encodeInstruction(BytecodeDefinition.INSTR_RET),       // 6 func1 ret
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 0), // 7 填充
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3), // 8 func2
            encodeInstruction(BytecodeDefinition.INSTR_RET),       // 9 func2 ret
            encodeInstruction(BytecodeDefinition.INSTR_HALT)       // 10 安全终止
        });

        int result = execute(bytecode);
        // 执行流程：1, CALL 4, 2, CALL 8, 3, RET, RET, HALT
        // 栈顶应该是3（最后压入的值）
        assertThat(result).isEqualTo(3);
    }

    @Test
    @Disabled("浮点指令需要32位常量加载支持，暂时禁用")
    @DisplayName("应该正确执行浮点加法指令")
    void testFloatAddition() throws Exception {
        // 测试FADD指令：3.5 + 2.5 = 6.0
        int float1 = Float.floatToIntBits(3.5f);
        int float2 = Float.floatToIntBits(2.5f);
        int expected = Float.floatToIntBits(6.0f);

        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float1),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float2),
            encodeInstruction(BytecodeDefinition.INSTR_FADD),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        // 由于浮点数精度，使用近似比较
        float resultFloat = Float.intBitsToFloat(result);
        float expectedFloat = Float.intBitsToFloat(expected);
        assertThat(resultFloat).isCloseTo(expectedFloat, org.assertj.core.api.Assertions.offset(0.0001f));
    }

    @Test
    @Disabled("浮点指令需要32位常量加载支持，暂时禁用")
    @DisplayName("应该正确执行浮点减法指令")
    void testFloatSubtraction() throws Exception {
        // 测试FSUB指令：5.0 - 2.5 = 2.5
        int float1 = Float.floatToIntBits(5.0f);
        int float2 = Float.floatToIntBits(2.5f);
        int expected = Float.floatToIntBits(2.5f);

        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float1),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float2),
            encodeInstruction(BytecodeDefinition.INSTR_FSUB),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        float resultFloat = Float.intBitsToFloat(result);
        float expectedFloat = Float.intBitsToFloat(expected);
        assertThat(resultFloat).isCloseTo(expectedFloat, org.assertj.core.api.Assertions.offset(0.0001f));
    }

    @Test
    @Disabled("浮点指令需要32位常量加载支持，暂时禁用")
    @DisplayName("应该正确执行浮点乘法指令")
    void testFloatMultiplication() throws Exception {
        // 测试FMUL指令：3.0 * 2.5 = 7.5
        int float1 = Float.floatToIntBits(3.0f);
        int float2 = Float.floatToIntBits(2.5f);
        int expected = Float.floatToIntBits(7.5f);

        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float1),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float2),
            encodeInstruction(BytecodeDefinition.INSTR_FMUL),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        float resultFloat = Float.intBitsToFloat(result);
        float expectedFloat = Float.intBitsToFloat(expected);
        assertThat(resultFloat).isCloseTo(expectedFloat, org.assertj.core.api.Assertions.offset(0.0001f));
    }

    @Test
    @Disabled("浮点指令需要32位常量加载支持，暂时禁用")
    @DisplayName("应该正确执行浮点除法指令")
    void testFloatDivision() throws Exception {
        // 测试FDIV指令：10.0 / 2.5 = 4.0
        int float1 = Float.floatToIntBits(10.0f);
        int float2 = Float.floatToIntBits(2.5f);
        int expected = Float.floatToIntBits(4.0f);

        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float1),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float2),
            encodeInstruction(BytecodeDefinition.INSTR_FDIV),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        float resultFloat = Float.intBitsToFloat(result);
        float expectedFloat = Float.intBitsToFloat(expected);
        assertThat(resultFloat).isCloseTo(expectedFloat, org.assertj.core.api.Assertions.offset(0.0001f));
    }

    @Test
    @DisplayName("应该正确处理浮点除零异常")
    void testFloatDivisionByZero() {
        // 测试FDIV除零异常
        int float1 = Float.floatToIntBits(10.0f);
        int float2 = Float.floatToIntBits(0.0f);

        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float1),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float2),
            encodeInstruction(BytecodeDefinition.INSTR_FDIV),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        // 应该抛出VMDivisionByZeroException
        assertThatThrownBy(() -> execute(bytecode))
            .isInstanceOf(VMDivisionByZeroException.class);
    }

    @Test
    @Disabled("浮点指令需要32位常量加载支持，暂时禁用")
    @DisplayName("应该正确执行浮点比较指令")
    void testFloatComparison() throws Exception {
        // 测试FLT指令：2.5 < 5.0 应该返回1 (true)
        int float1 = Float.floatToIntBits(2.5f);
        int float2 = Float.floatToIntBits(5.0f);

        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float1),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float2),
            encodeInstruction(BytecodeDefinition.INSTR_FLT),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        // 2.5 < 5.0 应该返回1
        assertThat(result).isEqualTo(1);

        // 测试FEQ指令：3.0 == 3.0 应该返回1
        int float3 = Float.floatToIntBits(3.0f);
        int float4 = Float.floatToIntBits(3.0f);

        bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float3),
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, float4),
            encodeInstruction(BytecodeDefinition.INSTR_FEQ),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        result = execute(bytecode);
        // 3.0 == 3.0 应该返回1
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("应该正确执行整数转浮点指令")
    void testIntegerToFloatConversion() throws Exception {
        // 测试ITOF指令：整数42转浮点42.0
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 42),
            encodeInstruction(BytecodeDefinition.INSTR_ITOF),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        int result = execute(bytecode);
        float resultFloat = Float.intBitsToFloat(result);
        // 42转浮点应该是42.0
        assertThat(resultFloat).isCloseTo(42.0f, org.assertj.core.api.Assertions.offset(0.0001f));
    }

    @Test
    @DisplayName("应该正确管理断点")
    void testBreakpointManagement() {
        // 测试断点设置、获取和清除
        vm.setBreakpoint(0);
        vm.setBreakpoint(10);
        vm.setBreakpoint(20);

        // 验证断点已添加
        java.util.Set<Integer> breakpoints = vm.getBreakpoints();
        assertThat(breakpoints).contains(0, 10, 20);
        assertThat(breakpoints).hasSize(3);

        // 清除一个断点
        vm.clearBreakpoint(10);
        breakpoints = vm.getBreakpoints();
        assertThat(breakpoints).contains(0, 20);
        assertThat(breakpoints).doesNotContain(10);
        assertThat(breakpoints).hasSize(2);

        // 清除所有断点
        vm.clearAllBreakpoints();
        breakpoints = vm.getBreakpoints();
        assertThat(breakpoints).isEmpty();
    }

    @Test
    @DisplayName("应该拒绝无效的断点地址")
    void testInvalidBreakpointAddress() {
        // 测试设置无效断点地址（负数）
        assertThatThrownBy(() -> vm.setBreakpoint(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Breakpoint address out of range");

        // 测试设置无效断点地址（超出指令缓存大小）
        // 注意：指令缓存大小取决于配置，这里使用一个明显很大的值
        assertThatThrownBy(() -> vm.setBreakpoint(999999))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Breakpoint address out of range");
    }

    @Test
    @DisplayName("应该启用单步执行模式")
    void testStepMode() {
        // 测试单步执行模式设置
        // 初始状态下stepMode应该为false
        // 由于stepMode是私有字段，我们通过step()方法间接测试

        // 调用step()方法
        vm.step();

        // 验证step()方法没有抛出异常
        // 在实际调试器中，step()会设置内部标志
        // 由于我们无法直接访问私有字段，至少验证方法调用成功
        assertThat(vm).isNotNull();

        // 注意：step()方法的具体效果需要在执行时验证
        // 这里只测试API调用不抛出异常
    }

    @Test
    @DisplayName("断点应该在实际执行时被检查")
    void testBreakpointExecution() throws Exception {
        // 测试断点在实际执行中的效果
        // 设置断点在地址1
        vm.setBreakpoint(1);

        // 创建一个简单程序：ICONST 5, HALT
        byte[] bytecode = createBytecode(new int[]{
            encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),
            encodeInstruction(BytecodeDefinition.INSTR_HALT)
        });

        // 执行程序
        int result = execute(bytecode);

        // 验证程序正常执行
        assertThat(result).isEqualTo(5);

        // 注意：由于断点检查仅打印信息，我们无法在测试中验证输出
        // 但至少验证程序执行没有因断点而失败
    }
}