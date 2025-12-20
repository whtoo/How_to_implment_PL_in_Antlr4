package org.teachfx.antlr4.ep18.stackvm.stackframe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18.stackvm.FunctionSymbol;

import static org.assertj.core.api.Assertions.*;

/**
 * StackFrameCalculator单元测试
 * 测试栈帧布局计算、偏移量计算、对齐验证等功能
 */
@DisplayName("StackFrameCalculator Tests")
class StackFrameCalculatorTest {

    private FunctionSymbol simpleFunction;
    private FunctionSymbol complexFunction;
    private FunctionSymbol noArgsFunction;

    @BeforeEach
    void setUp() {
        // 创建测试用的函数符号
        // 简单函数：1个参数，2个局部变量
        simpleFunction = new FunctionSymbol("simple", 1, 2, 100);

        // 复杂函数：5个参数，10个局部变量
        complexFunction = new FunctionSymbol("complex", 5, 10, 200);

        // 无参数函数：0个参数，1个局部变量
        noArgsFunction = new FunctionSymbol("noArgs", 0, 1, 300);
    }

    @Test
    @DisplayName("Should calculate frame size for simple function")
    void testCalculateFrameSizeSimple() {
        int frameSize = StackFrameCalculator.calculateFrameSize(simpleFunction);

        // 验证帧大小计算
        assertThat(frameSize).isGreaterThan(0);
        assertThat(frameSize % 16).isEqualTo(0); // 16字节对齐
    }

    @Test
    @DisplayName("Should calculate frame size for complex function")
    void testCalculateFrameSizeComplex() {
        int frameSize = StackFrameCalculator.calculateFrameSize(complexFunction);

        // 复杂函数应该有更大的栈帧
        assertThat(frameSize).isGreaterThan(
            StackFrameCalculator.calculateFrameSize(simpleFunction)
        );
        assertThat(frameSize % 16).isEqualTo(0); // 16字节对齐
    }

    @Test
    @DisplayName("Should calculate frame size for function with no args")
    void testCalculateFrameSizeNoArgs() {
        int frameSize = StackFrameCalculator.calculateFrameSize(noArgsFunction);

        assertThat(frameSize).isGreaterThan(0);
        assertThat(frameSize % 16).isEqualTo(0); // 16字节对齐
    }

    @Test
    @DisplayName("Should throw exception for null function symbol")
    void testNullFunctionSymbol() {
        assertThatThrownBy(() -> StackFrameCalculator.calculateFrameSize(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("FunctionSymbol cannot be null");
    }

    @Test
    @DisplayName("Should calculate local variable offsets correctly")
    void testLocalVariableOffset() {
        // 测试简单函数的局部变量偏移
        int offset0 = StackFrameCalculator.getLocalVariableOffset(simpleFunction, 0);
        int offset1 = StackFrameCalculator.getLocalVariableOffset(simpleFunction, 1);

        assertThat(offset0).isGreaterThanOrEqualTo(0);
        assertThat(offset1).isGreaterThan(offset0); // 第二个变量应该在更高地址
        assertThat(offset1 - offset0).isEqualTo(8); // 每个局部变量8字节
    }

    @Test
    @DisplayName("Should calculate offsets for all local variables")
    void testAllLocalVariableOffsets() {
        // 测试复杂函数的所有局部变量偏移
        for (int i = 0; i < 10; i++) {
            int offset = StackFrameCalculator.getLocalVariableOffset(complexFunction, i);
            assertThat(offset).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("Should throw exception for invalid local variable index")
    void testInvalidLocalVariableIndex() {
        assertThatThrownBy(() ->
            StackFrameCalculator.getLocalVariableOffset(simpleFunction, -1)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() ->
            StackFrameCalculator.getLocalVariableOffset(simpleFunction, 2) // 只有2个局部变量
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should calculate parameter offsets correctly")
    void testParameterOffset() {
        // 测试参数偏移计算
        for (int i = 0; i < simpleFunction.nargs; i++) {
            int offset = StackFrameCalculator.getParameterOffset(simpleFunction, i);
            assertThat(offset).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("Should calculate offsets for all parameters")
    void testAllParameterOffsets() {
        // 测试复杂函数的所有参数偏移
        for (int i = 0; i < complexFunction.nargs; i++) {
            int offset = StackFrameCalculator.getParameterOffset(complexFunction, i);
            assertThat(offset).isGreaterThanOrEqualTo(0);
            assertThat(offset % 8).isEqualTo(0); // 参数应该8字节对齐
        }
    }

    @Test
    @DisplayName("Should throw exception for invalid parameter index")
    void testInvalidParameterIndex() {
        assertThatThrownBy(() ->
            StackFrameCalculator.getParameterOffset(simpleFunction, -1)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() ->
            StackFrameCalculator.getParameterOffset(simpleFunction, 1) // 只有1个参数
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should calculate return address offset")
    void testReturnAddressOffset() {
        int offset = StackFrameCalculator.getReturnAddressOffset();
        assertThat(offset).isEqualTo(0); // 返回地址在栈帧底部
    }

    @Test
    @DisplayName("Should calculate saved frame pointer offset")
    void testSavedFramePointerOffset() {
        int offset = StackFrameCalculator.getSavedFramePointerOffset();
        assertThat(offset).isEqualTo(8); // 紧跟返回地址
    }

    @Test
    @DisplayName("Should calculate initial stack pointer")
    void testInitialStackPointer() {
        int frameSize = 128;
        int sp = StackFrameCalculator.getInitialStackPointer(frameSize);
        assertThat(sp).isEqualTo(frameSize);
    }

    @Test
    @DisplayName("Should calculate initial frame pointer")
    void testInitialFramePointer() {
        int frameSize = 128;
        int fp = StackFrameCalculator.getInitialFramePointer(frameSize);
        assertThat(fp).isEqualTo(frameSize - 8); // 减去返回地址大小
    }

    @Test
    @DisplayName("Should validate correct layout")
    void testValidateCorrectLayout() {
        StackFrameCalculator.StackFrameLayout layout =
            StackFrameCalculator.validateLayout(simpleFunction,
                StackFrameCalculator.calculateFrameSize(simpleFunction));

        assertThat(layout.isValid()).isTrue();
        assertThat(layout.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should detect misaligned frame size")
    void testDetectMisalignedFrame() {
        // 使用不对齐的帧大小
        int unalignedSize = 100; // 不是16的倍数
        StackFrameCalculator.StackFrameLayout layout =
            StackFrameCalculator.validateLayout(simpleFunction, unalignedSize);

        assertThat(layout.isValid()).isFalse();
        assertThat(layout.getErrors()).isNotEmpty();
        assertThat(layout.getErrors().get(0)).contains("not aligned");
    }

    @Test
    @DisplayName("Should detect out of bounds local variable offset")
    void testDetectOutOfBoundsLocalVariable() {
        // 创建虚假函数，实际局部变量数量与传入的不匹配
        FunctionSymbol invalidFunction = new FunctionSymbol("invalid", 0, 2, 400);

        // 故意传入错误的帧大小，导致局部变量偏移越界
        int tooSmallFrameSize = 24; // 小于所有局部变量所需的空间
        StackFrameCalculator.StackFrameLayout layout =
            StackFrameCalculator.validateLayout(invalidFunction, tooSmallFrameSize);

        // 应该检测到错误
        assertThat(layout.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should detect out of bounds parameter offset")
    void testDetectOutOfBoundsParameter() {
        // 使用过小的帧大小导致参数偏移越界
        int tooSmallFrameSize = 16; // 刚好容纳返回地址
        StackFrameCalculator.StackFrameLayout layout =
            StackFrameCalculator.validateLayout(complexFunction, tooSmallFrameSize);

        assertThat(layout.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should align values correctly")
    void testAlignment() {
        // 测试对齐函数（通过验证帧大小来间接测试）
        int unalignedValue = 100;
        // 由于alignTo是private的，我们通过计算帧大小来验证对齐
        FunctionSymbol testFunc = new FunctionSymbol("test", 0, 6, 0); // 6个局部变量
        int frameSize = StackFrameCalculator.calculateFrameSize(testFunc);

        assertThat(frameSize % 16).isEqualTo(0);
        assertThat(frameSize).isGreaterThanOrEqualTo(unalignedValue);
    }

    @Test
    @DisplayName("Should provide detailed layout information")
    void testLayoutToString() {
        StackFrameCalculator.StackFrameLayout layout =
            StackFrameCalculator.validateLayout(simpleFunction,
                StackFrameCalculator.calculateFrameSize(simpleFunction));

        String str = layout.toString();
        assertThat(str).contains("StackFrameLayout");
        assertThat(str).contains(simpleFunction.name);
        assertThat(str).contains("Frame Size:");
        assertThat(str).contains("Return Address Offset:");
    }

    @Test
    @DisplayName("Should handle layout with errors")
    void testLayoutWithErrorsToString() {
        int unalignedSize = 100;
        StackFrameCalculator.StackFrameLayout layout =
            StackFrameCalculator.validateLayout(simpleFunction, unalignedSize);

        String str = layout.toString();
        assertThat(str).contains("Errors:");
    }

    @Test
    @DisplayName("Should compare frame sizes correctly")
    void testFrameSizeComparison() {
        int simpleSize = StackFrameCalculator.calculateFrameSize(simpleFunction);
        int complexSize = StackFrameCalculator.calculateFrameSize(complexFunction);
        int noArgsSize = StackFrameCalculator.calculateFrameSize(noArgsFunction);

        // 复杂函数应该有最大的栈帧
        assertThat(complexSize).isGreaterThan(simpleSize);
        assertThat(complexSize).isGreaterThan(noArgsSize);

        // 简单函数和无参数函数的大小取决于局部变量数量
        // 简单函数有2个局部变量，无参数函数有1个
        // 所以简单函数应该更大或相等
        assertThat(simpleSize).isGreaterThanOrEqualTo(noArgsSize);
    }

    @Test
    @DisplayName("Should handle edge case with zero locals")
    void testZeroLocals() {
        FunctionSymbol zeroLocals = new FunctionSymbol("zeroLocals", 1, 0, 500);
        int frameSize = StackFrameCalculator.calculateFrameSize(zeroLocals);

        assertThat(frameSize).isGreaterThan(0);
        assertThat(frameSize % 16).isEqualTo(0);

        // 验证局部变量偏移不会抛出异常（虽然数量为0）
        assertThatCode(() -> {
            for (int i = 0; i < zeroLocals.nlocals; i++) {
                StackFrameCalculator.getLocalVariableOffset(zeroLocals, i);
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle edge case with zero args")
    void testZeroArgs() {
        FunctionSymbol zeroArgs = new FunctionSymbol("zeroArgs", 0, 5, 600);
        int frameSize = StackFrameCalculator.calculateFrameSize(zeroArgs);

        assertThat(frameSize).isGreaterThan(0);

        // 验证参数偏移不会抛出异常（虽然数量为0）
        assertThatCode(() -> {
            for (int i = 0; i < zeroArgs.nargs; i++) {
                StackFrameCalculator.getParameterOffset(zeroArgs, i);
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should maintain consistent alignment across different functions")
    void testConsistentAlignment() {
        // 所有函数的帧大小都应该是对齐的
        FunctionSymbol[] functions = {simpleFunction, complexFunction, noArgsFunction};

        for (FunctionSymbol func : functions) {
            int frameSize = StackFrameCalculator.calculateFrameSize(func);
            assertThat(frameSize % 16).isEqualTo(0)
                .as("Frame size for %s should be 16-byte aligned", func.name);
        }
    }
}
