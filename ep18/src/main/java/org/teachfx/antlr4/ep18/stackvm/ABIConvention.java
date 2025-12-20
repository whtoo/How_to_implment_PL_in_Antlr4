package org.teachfx.antlr4.ep18.stackvm;

import org.teachfx.antlr4.ep18.stackvm.stackframe.StackFrameCalculator;

/**
 * 统一的应用程序二进制接口（ABI）规范
 * 定义Cymbol语言编译器和虚拟机之间的调用约定
 */
public interface ABIConvention {

    /**
     * ABI版本标识
     */
    String ABI_VERSION = "1.0.0";

    /**
     * 栈对齐要求（字节）
     */
    int STACK_ALIGNMENT = 16;

    /**
     * 返回地址大小（字节）
     */
    int RETURN_ADDRESS_SIZE = 8;

    /**
     * 字大小（字节） - 适用于所有基本类型
     */
    int WORD_SIZE = 8;

    /**
     * 参数传递方式
     */
    enum ParameterPassing {
        /**
         * 参数通过栈传递（当前实现）
         */
        STACK,

        /**
         * 参数通过寄存器传递（未来扩展）
         */
        REGISTER
    }

    /**
     * 栈帧布局
     */
    interface StackFrameLayout {
        /**
         * 获取返回地址偏移量（相对于栈帧底部）
         */
        int returnAddressOffset();

        /**
         * 获取保存的帧指针偏移量（如果有）
         */
        int savedFramePointerOffset();

        /**
         * 获取局部变量区域偏移量
         */
        int localVariablesOffset();

        /**
         * 获取参数区域偏移量（从调用者栈中）
         */
        int parametersOffset();

        /**
         * 获取临时存储区域偏移量
         */
        int temporaryStorageOffset();

        /**
         * 获取栈帧总大小
         */
        int totalFrameSize(FunctionSymbol function);
    }

    /**
     * 调用约定规范
     */
    interface CallingConvention {
        /**
         * 获取参数传递方式
         */
        ParameterPassing parameterPassing();

        /**
         * 获取调用者保存的寄存器集合
         * @return 寄存器名称数组，当前为空（纯栈虚拟机）
         */
        default String[] callerSavedRegisters() {
            return new String[0];
        }

        /**
         * 获取被调用者保存的寄存器集合
         * @return 寄存器名称数组，当前为空（纯栈虚拟机）
         */
        default String[] calleeSavedRegisters() {
            return new String[0];
        }

        /**
         * 返回值位置
         * @return 返回值存储位置描述，当前为"栈顶"
         */
        default String returnValueLocation() {
            return "stack_top";
        }

        /**
         * 是否为可变参数函数
         * @param function 函数符号
         * @return 是否为可变参数
         */
        default boolean isVariadic(FunctionSymbol function) {
            return false; // Cymbol当前不支持可变参数
        }
    }

    /**
     * 异常处理规范
     */
    interface ExceptionHandling {
        /**
         * 异常处理帧偏移量
         */
        int exceptionFrameOffset();

        /**
         * 异常处理器寄存器名称
         */
        String exceptionHandlerRegister();

        /**
         * 是否需要栈展开信息
         */
        boolean needsUnwindInfo();
    }

    /**
     * 获取默认的栈帧布局
     */
    static StackFrameLayout getDefaultStackFrameLayout() {
        return new DefaultStackFrameLayout();
    }

    /**
     * 获取默认的调用约定
     */
    static CallingConvention getDefaultCallingConvention() {
        return new DefaultCallingConvention();
    }

    /**
     * 获取默认的异常处理规范
     */
    static ExceptionHandling getDefaultExceptionHandling() {
        return new DefaultExceptionHandling();
    }

    /**
     * 默认栈帧布局实现
     */
    class DefaultStackFrameLayout implements StackFrameLayout {
        @Override
        public int returnAddressOffset() {
            return 0; // 返回地址在栈帧底部
        }

        @Override
        public int savedFramePointerOffset() {
            return RETURN_ADDRESS_SIZE; // 紧跟返回地址
        }

        @Override
        public int localVariablesOffset() {
            // 局部变量在返回地址和保存的帧指针之后
            int offset = RETURN_ADDRESS_SIZE; // 返回地址
            offset += RETURN_ADDRESS_SIZE;    // 保存的帧指针（可选）
            // 对齐到16字节边界
            return alignTo(offset, STACK_ALIGNMENT);
        }

        @Override
        public int parametersOffset() {
            // 参数在调用者栈中，在被调用者栈帧之上
            // 对于被调用者，参数在返回地址之上
            return RETURN_ADDRESS_SIZE;
        }

        @Override
        public int temporaryStorageOffset() {
            // 临时存储区域在局部变量之后
            int offset = localVariablesOffset();
            // 为局部变量预留空间（在calculateFrameSize中计算）
            // 临时区域紧随其后
            return offset;
        }

        @Override
        public int totalFrameSize(FunctionSymbol function) {
            // 使用StackFrameCalculator计算总大小
            return StackFrameCalculator.calculateFrameSize(function);
        }

        private int alignTo(int value, int alignment) {
            return (value + alignment - 1) & ~(alignment - 1);
        }
    }

    /**
     * 默认调用约定实现
     */
    class DefaultCallingConvention implements CallingConvention {
        @Override
        public ParameterPassing parameterPassing() {
            return ParameterPassing.STACK;
        }

        @Override
        public String[] callerSavedRegisters() {
            // 纯栈虚拟机，无寄存器
            return new String[0];
        }

        @Override
        public String[] calleeSavedRegisters() {
            // 纯栈虚拟机，无寄存器
            return new String[0];
        }

        @Override
        public String returnValueLocation() {
            return "stack_top";
        }

        @Override
        public boolean isVariadic(FunctionSymbol function) {
            // Cymbol当前不支持可变参数
            return false;
        }
    }

    /**
     * 默认异常处理实现
     */
    class DefaultExceptionHandling implements ExceptionHandling {
        @Override
        public int exceptionFrameOffset() {
            // 异常处理信息在栈帧的特定位置
            return -WORD_SIZE; // 在栈帧末尾
        }

        @Override
        public String exceptionHandlerRegister() {
            // 纯栈虚拟机，无寄存器
            return "none";
        }

        @Override
        public boolean needsUnwindInfo() {
            // 当前不需要栈展开信息
            return false;
        }
    }

    /**
     * 函数调用上下文信息
     */
    class CallContext {
        private final FunctionSymbol function;
        private final int returnAddress;
        private final Object[] arguments;
        private final StackFrame frame;

        public CallContext(FunctionSymbol function, int returnAddress, Object[] arguments, StackFrame frame) {
            this.function = function;
            this.returnAddress = returnAddress;
            this.arguments = arguments;
            this.frame = frame;
        }

        public FunctionSymbol getFunction() {
            return function;
        }

        public int getReturnAddress() {
            return returnAddress;
        }

        public Object[] getArguments() {
            return arguments;
        }

        public StackFrame getFrame() {
            return frame;
        }

        /**
         * 验证调用上下文是否符合ABI规范
         */
        public boolean validate() {
            if (function == null) {
                return false;
            }
            if (returnAddress < 0 && returnAddress != -1) { // -1用于特殊情况（如main函数）
                return false;
            }
            if (arguments != null && arguments.length != function.nargs) {
                return false;
            }
            if (frame == null) {
                return false;
            }
            return frame.isValid();
        }
    }

    /**
     * 函数返回上下文信息
     */
    class ReturnContext {
        private final Object returnValue;
        private final StackFrame frame;
        private final int savedStackDepth;

        public ReturnContext(Object returnValue, StackFrame frame, int savedStackDepth) {
            this.returnValue = returnValue;
            this.frame = frame;
            this.savedStackDepth = savedStackDepth;
        }

        public Object getReturnValue() {
            return returnValue;
        }

        public StackFrame getFrame() {
            return frame;
        }

        public int getSavedStackDepth() {
            return savedStackDepth;
        }

        /**
         * 验证返回上下文是否符合ABI规范
         */
        public boolean validate() {
            if (frame == null) {
                return false;
            }
            if (savedStackDepth < 0) {
                return false;
            }
            return true;
        }
    }

    /**
     * ABI规范验证器
     */
    class Validator {
        /**
         * 验证函数调用是否符合ABI规范
         */
        public static boolean validateCall(CallContext context) {
            if (context == null) {
                return false;
            }
            if (!context.validate()) {
                return false;
            }

            FunctionSymbol function = context.getFunction();
            StackFrame frame = context.getFrame();

            // 验证栈帧大小符合规范
            int calculatedSize = StackFrameCalculator.calculateFrameSize(function);
            if (frame.getFrameSize() != calculatedSize) {
                return false;
            }

            // 验证返回地址在合法范围内
            int returnAddress = context.getReturnAddress();
            if (returnAddress < 0 && returnAddress != -1) {
                return false;
            }

            return true;
        }

        /**
         * 验证函数返回是否符合ABI规范
         */
        public static boolean validateReturn(ReturnContext context) {
            if (context == null) {
                return false;
            }
            if (!context.validate()) {
                return false;
            }

            StackFrame frame = context.getFrame();

            // 验证栈帧有效
            if (!frame.isValid()) {
                return false;
            }

            return true;
        }

        /**
         * 验证栈帧对齐
         */
        public static boolean validateStackAlignment(int stackPointer) {
            return stackPointer % STACK_ALIGNMENT == 0;
        }

        /**
         * 生成ABI验证报告
         */
        public static String generateReport(CallContext callContext, ReturnContext returnContext) {
            StringBuilder report = new StringBuilder();
            report.append("ABI Validation Report (Version ").append(ABI_VERSION).append(")\n");
            report.append("========================================\n");

            report.append("Call Context Validation: ").append(validateCall(callContext)).append("\n");
            report.append("Return Context Validation: ").append(validateReturn(returnContext)).append("\n");

            if (callContext != null && callContext.getFrame() != null) {
                report.append("Stack Frame Size: ").append(callContext.getFrame().getFrameSize()).append(" bytes\n");
                report.append("Stack Alignment: ").append(validateStackAlignment(callContext.getFrame().getFrameSize()) ? "OK" : "FAILED").append("\n");
            }

            return report.toString();
        }
    }
}