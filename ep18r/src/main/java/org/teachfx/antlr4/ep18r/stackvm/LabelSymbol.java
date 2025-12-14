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
            ByteCodeAssembler.writeInt(code, addrToPath, address);
        }
    }

}
