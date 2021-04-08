package org.teachfx.antlr4.ep18.stackvm;

public class FunctionSymbol {
    String name;
    int nargs; //函数参数个数
    int nlocals; //域内变量个数

    int address; // 入口地址
    public FunctionSymbol(String name) { this.name = name; }

    public FunctionSymbol(String name, int nargs, int nlocals,int address) { 
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
