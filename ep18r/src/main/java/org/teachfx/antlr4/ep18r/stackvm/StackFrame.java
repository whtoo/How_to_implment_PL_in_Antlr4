package org.teachfx.antlr4.ep18r.stackvm;

public class StackFrame {
    public FunctionSymbol symbol;
    public int returnAddress;
    public Object[] locals;
    public int[] savedCallerRegisters; // 保存caller-saved寄存器 r1-r7

    public StackFrame(FunctionSymbol symbol, int returnAddress) {
        this.symbol = symbol;
        this.returnAddress = returnAddress;
        if (symbol != null) {
            locals = new Object[symbol.nargs + symbol.nlocals];
        } else {
            locals = new Object[0];
        }
        savedCallerRegisters = new int[7]; // r1-r7 (索引0对应r1，索引6对应r7)
    }
}
