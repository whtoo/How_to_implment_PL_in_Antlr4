package org.teachfx.antlr4.ep18.stackvm.stackframe;

import org.teachfx.antlr4.ep18.stackvm.FunctionSymbol;

/**
 * 栈帧计算器
 * 根据ABI（应用程序二进制接口）规范计算栈帧布局
 * 确保栈帧的内存对齐、参数传递和局部变量存储符合标准
 */
public class StackFrameCalculator {

    /**
     * 栈帧布局常量（按ABI规范）
     */
    public static final int STACK_ALIGNMENT = 16; // 16字节对齐
    public static final int RETURN_ADDRESS_SIZE = 8; // 返回地址大小（字节）
    public static final int SAVED_FRAME_POINTER_SIZE = 8; // 保存的帧指针大小（字节）

    /**
     * 计算栈帧大小
     * @param functionSymbol 函数符号
     * @return 栈帧大小（字节）
     */
    public static int calculateFrameSize(FunctionSymbol functionSymbol) {
        if (functionSymbol == null) {
            throw new IllegalArgumentException("FunctionSymbol cannot be null");
        }

        int nargs = functionSymbol.nargs;
        int nlocals = functionSymbol.nlocals;

        // 计算总大小
        int totalSize = 0;

        // 1. 返回地址
        totalSize += RETURN_ADDRESS_SIZE;

        // 2. 保存的帧指针（可选，根据调用约定）
        // totalSize += SAVED_FRAME_POINTER_SIZE;

        // 3. 参数区域（从调用者传递）
        // 在我们的栈式VM中，参数通过栈传递，不需要额外空间

        // 4. 局部变量区域
        int localsSize = nlocals * 8; // 每个局部变量8字节（64位）
        totalSize += localsSize;

        // 5. 临时存储区域（用于表达式求值）
        int tempSize = Math.max(64, nlocals * 4); // 最小64字节
        totalSize += tempSize;

        // 6. 对齐到16字节边界
        totalSize = alignTo(totalSize, STACK_ALIGNMENT);

        return totalSize;
    }

    /**
     * 计算局部变量偏移量
     * @param functionSymbol 函数符号
     * @param localIndex 局部变量索引
     * @return 偏移量（字节）
     */
    public static int getLocalVariableOffset(FunctionSymbol functionSymbol, int localIndex) {
        if (functionSymbol == null) {
            throw new IllegalArgumentException("FunctionSymbol cannot be null");
        }
        if (localIndex < 0 || localIndex >= functionSymbol.nlocals) {
            throw new IllegalArgumentException("Local variable index out of bounds: " + localIndex);
        }

        int offset = 0;

        // 返回地址
        offset += RETURN_ADDRESS_SIZE;

        // 保存的帧指针
        // offset += SAVED_FRAME_POINTER_SIZE;

        // 局部变量从帧指针+16开始（对齐后）
        offset = alignTo(offset, STACK_ALIGNMENT);
        offset += localIndex * 8; // 每个局部变量8字节

        return offset;
    }

    /**
     * 计算参数偏移量
     * @param functionSymbol 函数符号
     * @param argIndex 参数索引
     * @return 偏移量（字节，从调用者栈顶开始）
     */
    public static int getParameterOffset(FunctionSymbol functionSymbol, int argIndex) {
        if (functionSymbol == null) {
            throw new IllegalArgumentException("FunctionSymbol cannot be null");
        }
        if (argIndex < 0 || argIndex >= functionSymbol.nargs) {
            throw new IllegalArgumentException("Parameter index out of bounds: " + argIndex);
        }

        // 在我们的栈式VM中，参数在调用前压入栈
        // 返回地址在参数之上
        // 因此参数的偏移量需要从调用者的角度计算
        int offset = 0;

        // 返回地址（由CALL指令压入）
        offset += RETURN_ADDRESS_SIZE;

        // 参数（从右到左压栈，所以最后一个参数在最低地址）
        offset += argIndex * 8; // 每个参数8字节

        return offset;
    }

    /**
     * 计算返回地址偏移量
     * @return 返回地址偏移量（字节）
     */
    public static int getReturnAddressOffset() {
        return 0; // 返回地址在栈帧底部
    }

    /**
     * 计算保存的帧指针偏移量
     * @return 保存的帧指针偏移量（字节）
     */
    public static int getSavedFramePointerOffset() {
        return RETURN_ADDRESS_SIZE; // 紧跟返回地址
    }

    /**
     * 计算栈指针初始值
     * @param frameSize 栈帧大小
     * @return 栈指针初始值（字节偏移）
     */
    public static int getInitialStackPointer(int frameSize) {
        // 栈向下增长，所以初始SP指向帧底
        return frameSize;
    }

    /**
     * 计算帧指针初始值
     * @param frameSize 栈帧大小
     * @return 帧指针初始值（字节偏移）
     */
    public static int getInitialFramePointer(int frameSize) {
        // FP指向保存的返回地址位置
        return frameSize - RETURN_ADDRESS_SIZE;
    }

    /**
     * 验证栈帧布局
     * @param functionSymbol 函数符号
     * @param frameSize 栈帧大小
     * @return 验证结果
     */
    public static StackFrameLayout validateLayout(FunctionSymbol functionSymbol, int frameSize) {
        StackFrameLayout layout = new StackFrameLayout(functionSymbol, frameSize);

        // 验证对齐
        if (frameSize % STACK_ALIGNMENT != 0) {
            layout.addError("Stack frame size " + frameSize + " is not aligned to " + STACK_ALIGNMENT + " bytes");
        }

        // 验证局部变量偏移
        for (int i = 0; i < functionSymbol.nlocals; i++) {
            int offset = getLocalVariableOffset(functionSymbol, i);
            if (offset < 0 || offset >= frameSize) {
                layout.addError("Local variable " + i + " offset " + offset + " is out of bounds");
            }
        }

        // 验证参数偏移
        for (int i = 0; i < functionSymbol.nargs; i++) {
            int offset = getParameterOffset(functionSymbol, i);
            if (offset < 0 || offset >= frameSize) {
                layout.addError("Parameter " + i + " offset " + offset + " is out of bounds");
            }
        }

        return layout;
    }

    /**
     * 对齐到指定边界
     * @param value 要对齐的值
     * @param alignment 对齐边界
     * @return 对齐后的值
     */
    private static int alignTo(int value, int alignment) {
        return (value + alignment - 1) & ~(alignment - 1);
    }

    /**
     * 栈帧布局信息类
     */
    public static class StackFrameLayout {
        private final FunctionSymbol functionSymbol;
        private final int frameSize;
        private final java.util.List<String> errors;

        public StackFrameLayout(FunctionSymbol functionSymbol, int frameSize) {
            this.functionSymbol = functionSymbol;
            this.frameSize = frameSize;
            this.errors = new java.util.ArrayList<>();
        }

        public FunctionSymbol getFunctionSymbol() {
            return functionSymbol;
        }

        public int getFrameSize() {
            return frameSize;
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public java.util.List<String> getErrors() {
            return new java.util.ArrayList<>(errors);
        }

        public void addError(String error) {
            errors.add(error);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("StackFrameLayout for ").append(functionSymbol.name).append(":\n");
            sb.append("  Frame Size: ").append(frameSize).append(" bytes\n");
            sb.append("  Return Address Offset: ").append(getReturnAddressOffset()).append("\n");
            sb.append("  Saved Frame Pointer Offset: ").append(getSavedFramePointerOffset()).append("\n");
            if (!errors.isEmpty()) {
                sb.append("  Errors:\n");
                for (String error : errors) {
                    sb.append("    - ").append(error).append("\n");
                }
            }
            return sb.toString();
        }
    }
}
