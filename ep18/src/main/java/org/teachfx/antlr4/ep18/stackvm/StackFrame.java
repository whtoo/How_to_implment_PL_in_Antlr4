package org.teachfx.antlr4.ep18.stackvm;

import org.teachfx.antlr4.ep18.stackvm.stackframe.StackFrameCalculator;

/**
 * 栈帧类 - 符合ABI规范的函数调用栈帧实现
 * 管理函数调用的上下文信息，包括返回地址、局部变量和调试信息
 */
public class StackFrame {
    // 栈帧元数据
    private final FunctionSymbol symbol;
    private final int frameSize;
    private final long creationTime;

    // 调用信息
    private final int returnAddress;
    private final StackFrame previousFrame;

    // 局部变量存储
    private final Object[] locals;
    private final int localOffset;

    // 参数存储（可选，参数通常在调用者栈中）
    private final Object[] parameters;
    private final int paramOffset;

    // 调试信息
    private final String debugInfo;
    private final java.util.Map<String, Object> debugData;

    /**
     * 构造函数 - 创建新的栈帧（完整版）
     * @param symbol 函数符号
     * @param returnAddress 返回地址（允许-1等特殊值）
     * @param previousFrame 前一个栈帧
     */
    public StackFrame(FunctionSymbol symbol, int returnAddress, StackFrame previousFrame) {
        if (symbol == null) {
            throw new IllegalArgumentException("FunctionSymbol cannot be null");
        }
        // 允许特殊值如-1用于特殊情况

        this.symbol = symbol;
        this.returnAddress = returnAddress;
        this.previousFrame = previousFrame;
        this.creationTime = System.nanoTime();

        // 计算栈帧大小和偏移
        this.frameSize = StackFrameCalculator.calculateFrameSize(symbol);
        // 只有当存在局部变量时才计算偏移
        this.localOffset = (symbol.nlocals > 0)
            ? StackFrameCalculator.getLocalVariableOffset(symbol, 0)
            : -1;
        // 只有当存在参数时才计算偏移
        this.paramOffset = (symbol.nargs > 0)
            ? StackFrameCalculator.getParameterOffset(symbol, 0)
            : -1;

        // 初始化局部变量数组
        this.locals = new Object[symbol.nlocals];

        // 初始化参数数组（如果需要）
        this.parameters = (symbol.nargs > 0) ? new Object[symbol.nargs] : null;

        // 创建调试信息
        this.debugInfo = createDebugInfo();
        this.debugData = new java.util.HashMap<>();
    }

    /**
     * 获取函数符号
     */
    public FunctionSymbol getSymbol() {
        return symbol;
    }

    /**
     * 获取返回地址
     */
    public int getReturnAddress() {
        return returnAddress;
    }

    /**
     * 获取前一个栈帧
     */
    public StackFrame getPreviousFrame() {
        return previousFrame;
    }

    /**
     * 获取栈帧大小
     */
    public int getFrameSize() {
        return frameSize;
    }

    /**
     * 获取局部变量数组
     */
    public Object[] getLocals() {
        return locals;
    }

    /**
     * 获取指定索引的局部变量
     */
    public Object getLocal(int index) {
        if (index < 0 || index >= symbol.nlocals) {
            throw new IndexOutOfBoundsException("Local variable index out of bounds: " + index);
        }
        return locals[index];
    }

    /**
     * 设置指定索引的局部变量
     */
    public void setLocal(int index, Object value) {
        if (index < 0 || index >= symbol.nlocals) {
            throw new IndexOutOfBoundsException("Local variable index out of bounds: " + index);
        }
        locals[index] = value;
    }

    /**
     * 获取参数数组
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * 获取指定索引的参数
     */
    public Object getParameter(int index) {
        if (parameters == null) {
            throw new IllegalStateException("This function has no parameters");
        }
        if (index < 0 || index >= symbol.nargs) {
            throw new IndexOutOfBoundsException("Parameter index out of bounds: " + index);
        }
        return parameters[index];
    }

    /**
     * 设置指定索引的参数
     */
    public void setParameter(int index, Object value) {
        if (parameters == null) {
            throw new IllegalStateException("This function has no parameters");
        }
        if (index < 0 || index >= symbol.nargs) {
            throw new IndexOutOfBoundsException("Parameter index out of bounds: " + index);
        }
        parameters[index] = value;
    }

    /**
     * 获取局部变量偏移量
     */
    public int getLocalOffset() {
        return localOffset;
    }

    /**
     * 获取参数偏移量
     */
    public int getParamOffset() {
        return paramOffset;
    }

    /**
     * 获取创建时间
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * 获取调试信息
     */
    public String getDebugInfo() {
        return debugInfo;
    }

    /**
     * 获取调试数据映射
     */
    public java.util.Map<String, Object> getDebugData() {
        return debugData;
    }

    /**
     * 设置调试数据
     */
    public void setDebugData(String key, Object value) {
        debugData.put(key, value);
    }

    /**
     * 获取调试数据
     */
    public Object getDebugData(String key) {
        return debugData.get(key);
    }

    /**
     * 验证栈帧完整性
     */
    public boolean isValid() {
        if (symbol == null || returnAddress < 0) {
            return false;
        }
        if (locals == null || locals.length != symbol.nlocals) {
            return false;
        }
        if (parameters != null && parameters.length != symbol.nargs) {
            return false;
        }
        return true;
    }

    /**
     * 创建栈帧的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StackFrame[");
        sb.append("function=").append(symbol.name);
        sb.append(", returnAddress=").append(returnAddress);
        sb.append(", locals=").append(symbol.nlocals);
        sb.append(", args=").append(symbol.nargs);
        sb.append("]");
        return sb.toString();
    }

    /**
     * 创建详细的调试信息
     */
    private String createDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Function: ").append(symbol.name).append("\n");
        sb.append("  Arguments: ").append(symbol.nargs).append("\n");
        sb.append("  Locals: ").append(symbol.nlocals).append("\n");
        sb.append("  Return Address: ").append(returnAddress).append("\n");
        sb.append("  Frame Size: ").append(frameSize).append(" bytes\n");
        sb.append("  Local Offset: ").append(localOffset).append("\n");
        sb.append("  Parameter Offset: ").append(paramOffset).append("\n");
        sb.append("  Creation Time: ").append(creationTime).append(" ns\n");
        return sb.toString();
    }

    /**
     * 获取栈帧统计信息
     */
    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Stack Frame Statistics for ").append(symbol.name).append(":\n");
        sb.append("  Total Size: ").append(frameSize).append(" bytes\n");
        sb.append("  Local Variables: ").append(locals.length).append("\n");
        sb.append("  Parameters: ").append(parameters != null ? parameters.length : 0).append("\n");
        sb.append("  Alignment: ").append(frameSize % 16 == 0 ? "16-byte aligned" : "NOT aligned").append("\n");
        sb.append("  Lifespan: ").append(System.nanoTime() - creationTime).append(" ns\n");
        return sb.toString();
    }

    /**
     * 向后兼容的构造函数 - 仅接受symbol和returnAddress
     * @param symbol 函数符号
     * @param returnAddress 返回地址
     */
    public StackFrame(FunctionSymbol symbol, int returnAddress) {
        this(symbol, returnAddress, null);
    }
}

