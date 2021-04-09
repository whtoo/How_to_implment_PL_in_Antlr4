package org.teachfx.antlr4.ep18.stackvm;

public class StackFrame {
    FunctionSymbol symbol;
    int returnAddress;
    Object[] locals;
    public StackFrame(FunctionSymbol symbol,int returnAddress) {
        this.symbol = symbol;
        this.returnAddress = returnAddress;
        locals = new Object[symbol.nargs+symbol.nlocals];
    }
}
