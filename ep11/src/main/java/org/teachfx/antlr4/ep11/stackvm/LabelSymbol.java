package org.teachfx.antlr4.ep11.stackvm;

import java.util.ArrayList;
import java.util.List;

/**
 * LabelSymbol — tracks code labels and resolves forward references.
 */
public class LabelSymbol {
    String name;
    int address;
    boolean isForwardRef = false;
    boolean isDefined = true;
    List<Integer> forwardRefs = new ArrayList<>();

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
        forwardRefs.add(address);
    }

    public void resolveForwardReferences(byte[] code) {
        isForwardRef = false;
        for (int addrToPatch : forwardRefs) {
            ByteCodeAssembler.writeInt(code, addrToPatch, address);
        }
    }

    public String getName() {
        return name;
    }

    public int getAddress() {
        return address;
    }
}
