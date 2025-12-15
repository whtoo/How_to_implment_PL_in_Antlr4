package org.teachfx.antlr4.ep18r.stackvm;

import java.util.Vector;

public class LabelSymbol {
    String name;

    int address;

    boolean isForwardRef = false;

    boolean isDefined = true;

    Vector<Integer> forwardRefs = new Vector<Integer>();

    public LabelSymbol(String name) {
        this.name = name;
    }

    public LabelSymbol(String name, int address) {
        this(name);
        this.address = address;
    }

    public LabelSymbol(String name, int address, boolean isForwardRef) {
        this(name);
        this.isForwardRef = isForwardRef;
        if (isForwardRef) {
            addForwardRef(address);
        } else {
            this.address = address;
        }
    }

    public void addForwardRef(int address) {
        forwardRefs.addElement(Integer.valueOf(address));
    }

    public void resolveForwardReferences(byte[] code) {
        isForwardRef = false;
        Vector<Integer> operandsToPath = forwardRefs;
        for (int addrToPath : operandsToPath) {
            // 只修补低16位立即数字段（假设I类型指令）
            // 立即数在指令字的低16位（bits 15-0），对应字节addrToPath+2和addrToPath+3
            if (addrToPath + 3 < code.length) {
                // 写入地址的低16位（大端序）
                code[addrToPath + 2] = (byte) ((address >> 8) & 0xFF);
                code[addrToPath + 3] = (byte) (address & 0xFF);
                // 保持高16位不变（操作码和寄存器字段）
            } else {
                System.err.println("Error: patch address out of bounds: " + addrToPath);
            }
        }
    }

}
