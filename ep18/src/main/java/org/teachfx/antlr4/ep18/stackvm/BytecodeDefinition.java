package org.teachfx.antlr4.ep18.stackvm;

import org.teachfx.antlr4.ep18.parser.VMAssemblerParser;

/**
 * 字节码定义
 * 所有用到的指令和寄存器定义都在这里
 */
public class BytecodeDefinition {

    public static final int REG = VMAssemblerParser.REG;
    public static final int FUNC = VMAssemblerParser.FUNC;
    public static final int INT = VMAssemblerParser.INT;
    public static final int POOL = 1000;
    //字节码都是有符号整数，以字节为单位计算长度，后面我们说3或者4都是指的3 bytes or 4 bytes.(1 byte means 1 unsigned byte -- [0..255] )
    public static final short INSTR_IADD = 1; // int add
    public static final short INSTR_ISUB = 2; // int subtract
    public static final short INSTR_IMUL = 3; // int mul
    // generate other cases for `instructions`
    public static final short INSTR_IDIV = 4; // int divide
    public static final short INSTR_ILT = 5; // int less than
    public static final short INSTR_ILE = 6; // int less or equal
    public static final short INSTR_IGT = 7; // int greater than
    public static final short INSTR_IGE = 8; // int greater or equal
    public static final short INSTR_IEQ = 9; // int equal
    public static final short INSTR_INE = 10; // int not equal
    public static final short INSTR_INEG = 11; // int negate
    public static final short INSTR_INOT = 12; // int not
    public static final short INSTR_IAND = 13; // int and
    public static final short INSTR_IOR = 14; // int or
    public static final short INSTR_IXOR = 15; // int xor
    public static final short INSTR_FADD = 16; // float add
    public static final short INSTR_FSUB = 17; // float subtract
    public static final short INSTR_FMUL = 18; // float mul
    public static final short INSTR_FDIV = 19; // float divide
    public static final short INSTR_FLT = 20; // float less than
    public static final short INSTR_FEQ = 21;
    public static final short INSTR_ITOF = 22;
    public static final short INSTR_CALL = 23; // call
    public static final short INSTR_RET = 24; // return
    public static final short INSTR_BR = 25;
    // generate other cases for `instructions`
    public static final short INSTR_BRT = 26;
    public static final short INSTR_BRF = 27;
    public static final short INSTR_CCONST = 28; // const
    public static final short INSTR_ICONST = 29; // const
    public static final short INSTR_FCONST = 30; // const
    public static final short INSTR_SCONST = 31; // const
    public static final short INSTR_LOAD = 32;
    public static final short INSTR_GLOAD = 33;
    public static final short INSTR_FLOAD = 34;
    public static final short INSTR_STORE = 35;
    public static final short INSTR_GSTORE = 36;
    public static final short INSTR_FSTORE = 37;
    public static final short INSTR_PRINT = 38;
    public static final short INSTR_STRUCT = 39;
    public static final short INSTR_NULL = 40;
    public static final short INSTR_POP = 41;
    public static final short INSTR_HALT = 42;


    // all instructions
    public static Instruction[] instructions = new Instruction[]{
            null, // <INVALID>
            new Instruction("iadd"), // index is the opcode
            new Instruction("isub"),
            new Instruction("imul"),
            new Instruction("idiv"),
            new Instruction("ilt"),
            new Instruction("ile"),
            new Instruction("igt"),
            new Instruction("ige"),
            new Instruction("ieq"),
            new Instruction("ine"),
            new Instruction("ineg"),
            new Instruction("inot"),
            new Instruction("iand"),
            new Instruction("ior"),
            new Instruction("ixor"),
            new Instruction("fadd"),
            new Instruction("fsub"),
            new Instruction("fmul"),
            new Instruction("fdiv"),
            new Instruction("flt"),
            new Instruction("feq"),
            new Instruction("ixor"),
            new Instruction("itof"),
            new Instruction("call", FUNC),
            new Instruction("ret"),
            new Instruction("br", INT),
            new Instruction("brt", INT),
            new Instruction("brf", INT),
            new Instruction("cconst", INT),
            new Instruction("iconst", INT),
            new Instruction("fconst", POOL),
            new Instruction("sconst", POOL),
            new Instruction("load", INT),
            new Instruction("gload", INT),
            new Instruction("fload", INT),
            new Instruction("store", INT),
            new Instruction("gstore", INT),
            new Instruction("fstore", INT),
            new Instruction("print"),
            new Instruction("struct", INT),
            new Instruction("null"),
            new Instruction("pop"),
            new Instruction("halt"),
    };

    public static class Instruction {
        String name;
        int[] type = new int[3];
        int n = 0;

        public Instruction(String name) {
            this(name, 0, 0, 0);
            n = 0;
        }

        public Instruction(String name, int a) {
            this(name, a, 0, 0);
            n = 1;
        }

        public Instruction(String name, int a, int b) {
            this(name, a, b, 0);
            n = 2;
        }

        public Instruction(String name, int a, int b, int c) {
            this.name = name;
            type[0] = a;
            type[1] = b;
            type[2] = c;
            n = 3;
        }
    }

}
