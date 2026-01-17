package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 栈帧结构
 * 记录函数调用的状态信息，包括返回地址、保存的寄存器等
 */
public class StackFrame {
    private static boolean trace = false;
    
    public static void setTrace(boolean trace) {
        StackFrame.trace = trace;
    }
    
    public static boolean isTraceEnabled() {
        return trace;
    }
    
    public FunctionSymbol symbol;
    public int returnAddress;
    public int frameBasePointer; // 栈帧基地址（在heap中的位置）
    public int[] savedCallerRegisters; // 保存caller-saved寄存器 a1(r3), a2(r4), a3(r5), a4(r6), a5(r7), lr(r15), ra(r1) 共7个

    public FunctionSymbol getFunctionSymbol() {
        return symbol;
    }

    public int getReturnAddress() {
        return returnAddress;
    }

    public int getFrameBasePointer() {
        return frameBasePointer;
    }

    public int[] getSavedCallerRegisters() {
        return savedCallerRegisters;
    }

    public StackFrame(FunctionSymbol symbol, int returnAddress, int frameBasePointer) {
        this.symbol = symbol;
        this.returnAddress = returnAddress;
        this.frameBasePointer = frameBasePointer;
        this.savedCallerRegisters = new int[7]; // 保存7个调用者保存寄存器（不包括a0/r2，因为它是返回值寄存器）

        if (isTraceEnabled()) {
            if (symbol != null) {
                System.err.printf("[StackFrame] 创建栈帧: 函数=%s, 返回地址=%d, 帧基址=%d, 参数数量=%d, 局部变量数量=%d%n",
                    symbol.name, returnAddress, frameBasePointer, symbol.nargs, symbol.nlocals);
            } else {
                System.err.printf("[StackFrame] 创建匿名栈帧: 返回地址=%d, 帧基址=%d%n", returnAddress, frameBasePointer);
            }
        }
    }
}
