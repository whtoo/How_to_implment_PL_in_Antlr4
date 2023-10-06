package org.teachfx.antlr4.ep18.stackvm;

import java.util.Arrays;

public class StructSpace {
    Object[] fields;

    public StructSpace(int nfields) {
        this.fields = new Object[nfields];
    }

    public String toString() {
        return Arrays.toString(fields);
    }
}
