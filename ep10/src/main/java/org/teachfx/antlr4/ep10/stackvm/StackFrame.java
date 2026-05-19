package org.teachfx.antlr4.ep10.stackvm;

/**
 * StackFrame — represents a function call activation record.
 * Holds the function symbol, return address, local variables,
 * and parameters for the current call.
 */
public class StackFrame {
    private final FunctionSymbol symbol;
    private final int returnAddress;
    private final Object[] locals;
    private final Object[] parameters;

    public StackFrame(FunctionSymbol symbol, int returnAddress) {
        if (symbol == null) {
            throw new IllegalArgumentException("FunctionSymbol cannot be null");
        }
        this.symbol = symbol;
        this.returnAddress = returnAddress;
        this.locals = new Object[symbol.nlocals];
        this.parameters = (symbol.nargs > 0) ? new Object[symbol.nargs] : null;
    }

    public FunctionSymbol getSymbol() {
        return symbol;
    }

    public int getReturnAddress() {
        return returnAddress;
    }

    public Object[] getLocals() {
        return locals;
    }

    public Object getLocal(int index) {
        if (index < 0 || index >= symbol.nlocals) {
            throw new IndexOutOfBoundsException("Local variable index out of bounds: " + index);
        }
        return locals[index];
    }

    public void setLocal(int index, Object value) {
        if (index < 0 || index >= symbol.nlocals) {
            throw new IndexOutOfBoundsException("Local variable index out of bounds: " + index);
        }
        locals[index] = value;
    }

    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "StackFrame[function=" + symbol.name +
               ", returnAddress=" + returnAddress +
               ", locals=" + symbol.nlocals +
               ", args=" + symbol.nargs + "]";
    }
}
