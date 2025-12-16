package org.teachfx.antlr4.ep18r.stackvm;

import java.util.Vector;

public class LabelSymbol {
    String name;

    int address;

    boolean isForwardRef = false;

    boolean isDefined = true;

    // 前向引用列表：每个元素包含 [指令地址, 是否J类型(1=J类型26位, 0=I类型16位)]
    Vector<int[]> forwardRefs = new Vector<int[]>();

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
            addForwardRef(address, false); // 默认I类型
        } else {
            this.address = address;
        }
    }

    public void addForwardRef(int address) {
        addForwardRef(address, false); // 默认I类型
    }

    public void addForwardRef(int address, boolean isJType) {
        forwardRefs.addElement(new int[]{address, isJType ? 1 : 0});
    }

    public void resolveForwardReferences(byte[] code) {
        isForwardRef = false;
        for (int[] ref : forwardRefs) {
            int addrToPath = ref[0];
            boolean isJType = ref[1] == 1;

            if (addrToPath + 3 < code.length) {
                if (isJType) {
                    // J类型：修补低26位（bits 25-0）
                    // 保留操作码（bits 31-26），修改bits 25-0
                    int oldHigh = (code[addrToPath] & 0xFF) & 0xFC; // 保留高6位（操作码）
                    int newImm = address & 0x3FFFFFF; // 取低26位
                    code[addrToPath] = (byte) (oldHigh | ((newImm >> 24) & 0x03));
                    code[addrToPath + 1] = (byte) ((newImm >> 16) & 0xFF);
                    code[addrToPath + 2] = (byte) ((newImm >> 8) & 0xFF);
                    code[addrToPath + 3] = (byte) (newImm & 0xFF);
                } else {
                    // I类型：只修补低16位（bits 15-0）
                    // 保持高16位不变（操作码和寄存器字段）
                    code[addrToPath + 2] = (byte) ((address >> 8) & 0xFF);
                    code[addrToPath + 3] = (byte) (address & 0xFF);
                }
            } else {
                System.err.println("Error: patch address out of bounds: " + addrToPath);
            }
        }
    }

}
