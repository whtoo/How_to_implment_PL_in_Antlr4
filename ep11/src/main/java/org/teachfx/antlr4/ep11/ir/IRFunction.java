package org.teachfx.antlr4.ep11.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * IRFunction: holds the IR instructions for a single function.
 */
public class IRFunction {
    public final String name;
    public final String returnType;
    public final List<IRInstruction> instructions;
    private int tempCounter = 0;
    private int labelCounter = 0;

    public IRFunction(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
        this.instructions = new ArrayList<>();
    }

    /** Generate a fresh temporary variable name. */
    public String freshTemp() {
        return "t" + (tempCounter++);
    }

    /** Generate a fresh label name. */
    public String freshLabel() {
        return "L" + (labelCounter++);
    }

    /** Add an instruction and return its index. */
    public int emit(IRInstruction instr) {
        instructions.add(instr);
        return instructions.size() - 1;
    }

    /** Print all instructions with line numbers. */
    public void printIR() {
        System.out.println("\n=== Function: " + name + " (returns " + returnType + ") ===");
        for (int i = 0; i < instructions.size(); i++) {
            System.out.printf("%4d: %s%n", i, instructions.get(i));
        }
    }
}
