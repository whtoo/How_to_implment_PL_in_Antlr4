package org.teachfx.antlr4.ep18.stackvm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18.stackvm.ABIConvention.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ABIConvention单元测试
 * 验证ABI规范的正确性和一致性
 */
@DisplayName("ABIConvention Tests")
class ABIConventionTest {

    private FunctionSymbol simpleFunction;
    private FunctionSymbol complexFunction;
    private FunctionSymbol voidFunction;

    @BeforeEach
    void setUp() {
        // 创建测试用的函数符号
        simpleFunction = new FunctionSymbol("simple", 2, 3, 100);  // 2个参数，3个局部变量
        complexFunction = new FunctionSymbol("complex", 5, 10, 200); // 5个参数，10个局部变量
        voidFunction = new FunctionSymbol("void", 0, 0, 300); // 无参数，无局部变量
    }

    @Test
    @DisplayName("Should provide correct ABI version")
    void testABIVersion() {
        assertThat(ABIConvention.ABI_VERSION).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Should provide correct stack alignment")
    void testStackAlignment() {
        assertThat(ABIConvention.STACK_ALIGNMENT).isEqualTo(16);
    }

    @Test
    @DisplayName("Should provide correct word size")
    void testWordSize() {
        assertThat(ABIConvention.WORD_SIZE).isEqualTo(8);
    }

    @Test
    @DisplayName("Should provide default stack frame layout")
    void testDefaultStackFrameLayout() {
        StackFrameLayout layout = ABIConvention.getDefaultStackFrameLayout();

        assertThat(layout).isNotNull();
        assertThat(layout.returnAddressOffset()).isEqualTo(0);
        assertThat(layout.savedFramePointerOffset()).isEqualTo(ABIConvention.RETURN_ADDRESS_SIZE);
    }

    @Test
    @DisplayName("Should calculate total frame size using StackFrameCalculator")
    void testTotalFrameSize() {
        StackFrameLayout layout = ABIConvention.getDefaultStackFrameLayout();

        int simpleSize = layout.totalFrameSize(simpleFunction);
        int complexSize = layout.totalFrameSize(complexFunction);
        int voidSize = layout.totalFrameSize(voidFunction);

        assertThat(simpleSize).isGreaterThan(0);
        assertThat(complexSize).isGreaterThan(simpleSize); // 复杂函数应该有更大的栈帧
        assertThat(voidSize).isGreaterThan(0); // 即使没有参数和局部变量，也应该有基本栈帧

        // 验证对齐
        assertThat(simpleSize % ABIConvention.STACK_ALIGNMENT).isEqualTo(0);
        assertThat(complexSize % ABIConvention.STACK_ALIGNMENT).isEqualTo(0);
        assertThat(voidSize % ABIConvention.STACK_ALIGNMENT).isEqualTo(0);
    }

    @Test
    @DisplayName("Should provide default calling convention")
    void testDefaultCallingConvention() {
        CallingConvention convention = ABIConvention.getDefaultCallingConvention();

        assertThat(convention).isNotNull();
        assertThat(convention.parameterPassing()).isEqualTo(ABIConvention.ParameterPassing.STACK);
        assertThat(convention.returnValueLocation()).isEqualTo("stack_top");
        assertThat(convention.isVariadic(simpleFunction)).isFalse();
    }

    @Test
    @DisplayName("Should create valid CallContext")
    void testCallContext() {
        StackFrame frame = new StackFrame(simpleFunction, 500, null);
        Object[] arguments = new Object[] { 42, 100 };
        CallContext context = new CallContext(simpleFunction, 500, arguments, frame);

        assertThat(context).isNotNull();
        assertThat(context.getFunction()).isEqualTo(simpleFunction);
        assertThat(context.getReturnAddress()).isEqualTo(500);
        assertThat(context.getArguments()).isEqualTo(arguments);
        assertThat(context.getFrame()).isEqualTo(frame);
        assertThat(context.validate()).isTrue();
    }

    @Test
    @DisplayName("Should detect invalid CallContext")
    void testInvalidCallContext() {
        // 测试空函数
        CallContext nullFunctionContext = new CallContext(null, 500, new Object[0], new StackFrame(simpleFunction, 500, null));
        assertThat(nullFunctionContext.validate()).isFalse();

        // 测试无效返回地址
        CallContext invalidReturnAddressContext = new CallContext(simpleFunction, -2, new Object[0], new StackFrame(simpleFunction, -2, null));
        assertThat(invalidReturnAddressContext.validate()).isFalse();

        // 测试参数数量不匹配
        Object[] wrongArgs = new Object[] { 42 }; // 应该是2个参数，但只提供了1个
        CallContext wrongArgsContext = new CallContext(simpleFunction, 500, wrongArgs, new StackFrame(simpleFunction, 500, null));
        assertThat(wrongArgsContext.validate()).isFalse();
    }

    @Test
    @DisplayName("Should create valid ReturnContext")
    void testReturnContext() {
        StackFrame frame = new StackFrame(simpleFunction, 500, null);
        ReturnContext context = new ReturnContext(42, frame, 10);

        assertThat(context).isNotNull();
        assertThat(context.getReturnValue()).isEqualTo(42);
        assertThat(context.getFrame()).isEqualTo(frame);
        assertThat(context.getSavedStackDepth()).isEqualTo(10);
        assertThat(context.validate()).isTrue();
    }

    @Test
    @DisplayName("Should detect invalid ReturnContext")
    void testInvalidReturnContext() {
        // 测试空栈帧
        ReturnContext nullFrameContext = new ReturnContext(42, null, 10);
        assertThat(nullFrameContext.validate()).isFalse();

        // 测试负的栈深度
        ReturnContext negativeDepthContext = new ReturnContext(42, new StackFrame(simpleFunction, 500, null), -1);
        assertThat(negativeDepthContext.validate()).isFalse();
    }

    @Test
    @DisplayName("Should validate call context with Validator")
    void testValidateCall() {
        StackFrame frame = new StackFrame(simpleFunction, 500, null);
        Object[] arguments = new Object[] { 42, 100 };
        CallContext context = new CallContext(simpleFunction, 500, arguments, frame);

        assertThat(Validator.validateCall(context)).isTrue();
    }

    @Test
    @DisplayName("Should validate return context with Validator")
    void testValidateReturn() {
        StackFrame frame = new StackFrame(simpleFunction, 500, null);
        ReturnContext context = new ReturnContext(42, frame, 10);

        assertThat(Validator.validateReturn(context)).isTrue();
    }

    @Test
    @DisplayName("Should validate stack alignment")
    void testValidateStackAlignment() {
        assertThat(Validator.validateStackAlignment(0)).isTrue(); // 0是16的倍数
        assertThat(Validator.validateStackAlignment(16)).isTrue();
        assertThat(Validator.validateStackAlignment(32)).isTrue();
        assertThat(Validator.validateStackAlignment(8)).isFalse(); // 8不是16的倍数
        assertThat(Validator.validateStackAlignment(15)).isFalse();
    }

    @Test
    @DisplayName("Should generate validation report")
    void testGenerateReport() {
        StackFrame frame = new StackFrame(simpleFunction, 500, null);
        Object[] arguments = new Object[] { 42, 100 };
        CallContext callContext = new CallContext(simpleFunction, 500, arguments, frame);
        ReturnContext returnContext = new ReturnContext(42, frame, 10);

        String report = Validator.generateReport(callContext, returnContext);

        assertThat(report).isNotNull();
        assertThat(report).contains("ABI Validation Report");
        assertThat(report).contains("Version " + ABIConvention.ABI_VERSION);
        assertThat(report).contains("Call Context Validation:");
        assertThat(report).contains("Return Context Validation:");
    }

    @Test
    @DisplayName("Should handle null contexts in report generation")
    void testGenerateReportWithNullContexts() {
        String report = Validator.generateReport(null, null);

        assertThat(report).isNotNull();
        assertThat(report).contains("ABI Validation Report");
        // 即使上下文为空，也应该生成报告
    }

    @Test
    @DisplayName("Should provide default exception handling")
    void testDefaultExceptionHandling() {
        ExceptionHandling exceptionHandling = ABIConvention.getDefaultExceptionHandling();

        assertThat(exceptionHandling).isNotNull();
        assertThat(exceptionHandling.exceptionHandlerRegister()).isEqualTo("none");
        assertThat(exceptionHandling.needsUnwindInfo()).isFalse();
    }

    @Test
    @DisplayName("Should verify parameter passing is stack-based")
    void testParameterPassingStackBased() {
        CallingConvention convention = ABIConvention.getDefaultCallingConvention();
        assertThat(convention.parameterPassing()).isEqualTo(ABIConvention.ParameterPassing.STACK);
    }

    @Test
    @DisplayName("Should verify no registers in pure stack VM")
    void testNoRegisters() {
        CallingConvention convention = ABIConvention.getDefaultCallingConvention();
        assertThat(convention.callerSavedRegisters()).isEmpty();
        assertThat(convention.calleeSavedRegisters()).isEmpty();
    }

    @Test
    @DisplayName("Should handle CallContext with null arguments for zero-arg functions")
    void testCallContextWithNullArguments() {
        StackFrame frame = new StackFrame(voidFunction, 500, null);
        // 对于无参数函数，arguments可以为null
        CallContext context = new CallContext(voidFunction, 500, null, frame);

        assertThat(context.validate()).isTrue();
    }

    @Test
    @DisplayName("Should handle CallContext with empty arguments array")
    void testCallContextWithEmptyArguments() {
        StackFrame frame = new StackFrame(voidFunction, 500, null);
        CallContext context = new CallContext(voidFunction, 500, new Object[0], frame);

        assertThat(context.validate()).isTrue();
    }

    @Test
    @DisplayName("Should validate frame size consistency")
    void testFrameSizeConsistency() {
        StackFrameLayout layout = ABIConvention.getDefaultStackFrameLayout();
        int calculatedSize = layout.totalFrameSize(simpleFunction);

        // 创建栈帧并验证其大小与计算的一致
        StackFrame frame = new StackFrame(simpleFunction, 500, null);
        assertThat(frame.getFrameSize()).isEqualTo(calculatedSize);
    }
}