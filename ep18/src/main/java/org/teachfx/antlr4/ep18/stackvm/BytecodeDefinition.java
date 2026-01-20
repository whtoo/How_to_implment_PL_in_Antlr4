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
    public static final short INSTR_IALOAD = 43; // 数组加载
    public static final short INSTR_IASTORE = 44; // 数组存储
    public static final short INSTR_NEWARRAY = 45; // 数组分配

    // all instructions
    public static Instruction[] instructions = new Instruction[]{
            null, // <INVALID> - index 0
            new Instruction("iadd"), // index 1
            new Instruction("isub"), // index 2
            new Instruction("imul"), // index 3
            new Instruction("idiv"), // index 4
            new Instruction("ilt"), // index 5
            new Instruction("ile"), // index 6
            new Instruction("igt"), // index 7
            new Instruction("ige"), // index 8
            new Instruction("ieq"), // index 9
            new Instruction("ine"), // index 10
            new Instruction("ineg"), // index 11
            new Instruction("inot"), // index 12
            new Instruction("iand"), // index 13
            new Instruction("ior"), // index 14
            new Instruction("ixor"), // index 15
            new Instruction("fadd"), // index 16
            new Instruction("fsub"), // index 17
            new Instruction("fmul"), // index 18
            new Instruction("fdiv"), // index 19
            new Instruction("flt"), // index 20
            new Instruction("feq"), // index 21
            new Instruction("itof"), // index 22
            new Instruction("call", FUNC), // index 23
            new Instruction("ret"), // index 24
            new Instruction("br", INT), // index 25
            new Instruction("brt", INT), // index 26
            new Instruction("brf", INT), // index 27
            new Instruction("cconst", INT), // index 28
            new Instruction("iconst", INT), // index 29
            new Instruction("fconst", POOL), // index 30
            new Instruction("sconst", POOL), // index 31
            new Instruction("load", INT), // index 32
            new Instruction("gload", INT), // index 33
            new Instruction("fload", INT), // index 34
            new Instruction("store", INT), // index 35
            new Instruction("gstore", INT), // index 36
            new Instruction("fstore", INT), // index 37
            new Instruction("print"), // index 38
            new Instruction("struct", INT), // index 39
            new Instruction("null"), // index 40
            new Instruction("pop"), // index 41
            new Instruction("halt"), // index 42
            new Instruction("iaload", INT), // index 43
            new Instruction("iastore", INT), // index 44
            new Instruction("newarray", INT), // index 45
    };

    public static class Instruction {
        public String name;
        public int[] type = new int[3];
        public int n = 0;

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
