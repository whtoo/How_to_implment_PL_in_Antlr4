package org.teachfx.antlr4.ep18r.stackvm;

public class StackFrame {
    public FunctionSymbol symbol;
    public int returnAddress;
    public Object[] locals;
    public int[] savedCallerRegisters; // 保存caller-saved寄存器 r3-r7（a1-a5）

    public StackFrame(FunctionSymbol symbol, int returnAddress) {
        this.symbol = symbol;
        this.returnAddress = returnAddress;
        if (symbol != null) {
            locals = new Object[symbol.nargs + symbol.nlocals];
            // 调试跟踪输出
            System.out.printf("[StackFrame] 创建栈帧: 函数=%s, 返回地址=%d, 参数数量=%d, 局部变量数量=%d, locals数组大小=%d%n",
                symbol.name, returnAddress, symbol.nargs, symbol.nlocals, locals.length);
            // 打印局部变量偏移信息
            StackOffsets.printLocalVarOffsets(symbol.name, symbol.nargs, symbol.nlocals);
        } else {
            locals = new Object[0];
            System.out.printf("[StackFrame] 创建匿名栈帧: 返回地址=%d%n", returnAddress);
        }
        savedCallerRegisters = new int[5]; // r3-r7 (索引0对应r3，索引4对应r7)
    }
}
