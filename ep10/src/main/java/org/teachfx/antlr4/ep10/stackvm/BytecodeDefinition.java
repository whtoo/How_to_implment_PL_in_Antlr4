package org.teachfx.antlr4.ep10.stackvm;

/**
 * BytecodeDefinition — all opcodes and instruction metadata.
 * The VM instruction set: Arithmetic, Comparison, ControlFlow, Memory, Constants.
 */
public class BytecodeDefinition {

    // === Token types (mirror parser token types) ===
    public static final int REG   = 100;
    public static final int FUNC  = 101;
    public static final int INT   = 102;
    public static final int POOL  = 1000; // constant pool reference

    // === Arithmetic ===
    public static final short INSTR_IADD  = 1;   // int add
    public static final short INSTR_ISUB  = 2;   // int subtract
    public static final short INSTR_IMUL  = 3;   // int multiply
    public static final short INSTR_IDIV  = 4;   // int divide
    public static final short INSTR_INEG  = 11;  // int negate
    public static final short INSTR_INOT  = 12;  // int logical NOT
    public static final short INSTR_IAND  = 13;  // int bitwise AND
    public static final short INSTR_IOR   = 14;  // int bitwise OR
    public static final short INSTR_IXOR  = 15;  // int bitwise XOR

    // === Float Arithmetic ===
    public static final short INSTR_FADD  = 16;  // float add
    public static final short INSTR_FSUB  = 17;  // float subtract
    public static final short INSTR_FMUL  = 18;  // float multiply
    public static final short INSTR_FDIV  = 19;  // float divide
    public static final short INSTR_FLT   = 20;  // float less-than
    public static final short INSTR_FEQ   = 21;  // float equal
    public static final short INSTR_ITOF  = 22;  // int to float

    // === Comparison ===
    public static final short INSTR_ILT   = 5;   // int less-than
    public static final short INSTR_ILE   = 6;   // int less-or-equal
    public static final short INSTR_IGT   = 7;   // int greater-than
    public static final short INSTR_IGE   = 8;   // int greater-or-equal
    public static final short INSTR_IEQ   = 9;   // int equal
    public static final short INSTR_INE   = 10;  // int not-equal

    // === Control Flow ===
    public static final short INSTR_CALL  = 23;  // call function
    public static final short INSTR_RET   = 24;  // return
    public static final short INSTR_BR    = 25;  // unconditional branch
    public static final short INSTR_BRT   = 26;  // branch if true
    public static final short INSTR_BRF   = 27;  // branch if false

    // === Constants ===
    public static final short INSTR_CCONST = 28; // char constant
    public static final short INSTR_ICONST = 29; // int constant
    public static final short INSTR_FCONST = 30; // float constant (pool)
    public static final short INSTR_SCONST = 31; // string constant (pool)

    // === Memory (load/store) ===
    public static final short INSTR_LOAD   = 32; // load local variable
    public static final short INSTR_STORE  = 35; // store local variable
    public static final short INSTR_GLOAD  = 33; // load global
    public static final short INSTR_GSTORE = 36; // store global

    // === Miscellaneous ===
    public static final short INSTR_PRINT  = 38; // print top of stack
    public static final short INSTR_POP    = 41; // pop top of stack
    public static final short INSTR_HALT   = 42; // halt execution

    /**
     * Instruction metadata: name + operand types.
     */
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

    // === All instructions indexed by opcode ===
    public static Instruction[] instructions = new Instruction[] {
        null,                         // index 0 — invalid
        new Instruction("iadd"),      // 1
        new Instruction("isub"),      // 2
        new Instruction("imul"),      // 3
        new Instruction("idiv"),      // 4
        new Instruction("ilt"),       // 5
        new Instruction("ile"),       // 6
        new Instruction("igt"),       // 7
        new Instruction("ige"),       // 8
        new Instruction("ieq"),       // 9
        new Instruction("ine"),       // 10
        new Instruction("ineg"),      // 11
        new Instruction("inot"),      // 12
        new Instruction("iand"),      // 13
        new Instruction("ior"),       // 14
        new Instruction("ixor"),      // 15
        new Instruction("fadd"),      // 16
        new Instruction("fsub"),      // 17
        new Instruction("fmul"),      // 18
        new Instruction("fdiv"),      // 19
        new Instruction("flt"),       // 20
        new Instruction("feq"),       // 21
        new Instruction("itof"),      // 22
        new Instruction("call", FUNC),// 23
        new Instruction("ret"),       // 24
        new Instruction("br", INT),   // 25
        new Instruction("brt", INT),  // 26
        new Instruction("brf", INT),  // 27
        new Instruction("cconst", INT), // 28
        new Instruction("iconst", INT), // 29
        new Instruction("fconst", POOL), // 30
        new Instruction("sconst", POOL), // 31
        new Instruction("load", INT),   // 32
        new Instruction("gload", INT),  // 33
        null,                          // 34 — unused
        new Instruction("store", INT), // 35
        new Instruction("gstore", INT), // 36
        null,                          // 37 — unused
        new Instruction("print"),      // 38
        null, null,                    // 39-40 — unused
        new Instruction("pop"),        // 41
        new Instruction("halt"),       // 42
    };
}
