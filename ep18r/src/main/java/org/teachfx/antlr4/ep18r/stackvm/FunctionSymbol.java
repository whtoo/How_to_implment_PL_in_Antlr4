package org.teachfx.antlr4.ep18r.stackvm;

public class FunctionSymbol {
    public String name;
    public int nargs; //函数参数个数
    public int nlocals; //域内变量个数

    public int address; // 入口地址

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
        return "FunctionSymbol{" +
                "name='" + name + '\'' +
                ", args=" + nargs +
                ", locals=" + nlocals +
                ", address=" + address +
                '}';
    }
}
