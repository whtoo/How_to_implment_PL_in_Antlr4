package org.teachfx.antlr4.ep11.stackvm;

/**
 * FunctionSymbol — metadata for a function: name, args, locals, entry address.
 */
public class FunctionSymbol {
    public String name;
    public int nargs;    // number of parameters
    public int nlocals;  // number of local variables
    public int address;  // entry address in code memory

    public FunctionSymbol(String name) {
        this.name = name;
    }

    public FunctionSymbol(String name, int nargs, int nlocals, int address) {
        this.name = name;
        this.nargs = nargs;
        this.nlocals = nlocals;
        this.address = address;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FunctionSymbol && name.equals(((FunctionSymbol) obj).name);
    }

    @Override
    public String toString() {
        return "FunctionSymbol{name='" + name + "', args=" + nargs +
               ", locals=" + nlocals + ", address=" + address + '}';
    }
}
