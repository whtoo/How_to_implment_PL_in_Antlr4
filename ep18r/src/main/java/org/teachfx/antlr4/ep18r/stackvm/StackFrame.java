package org.teachfx.antlr4.ep18r.stackvm;

public class StackFrame {
    public FunctionSymbol symbol;
    public int returnAddress;
    public Object[] locals;

    public StackFrame(FunctionSymbol symbol, int returnAddress) {
        this.symbol = symbol;
        this.returnAddress = returnAddress;
        locals = new Object[symbol.nargs + symbol.nlocals];
    }
}
