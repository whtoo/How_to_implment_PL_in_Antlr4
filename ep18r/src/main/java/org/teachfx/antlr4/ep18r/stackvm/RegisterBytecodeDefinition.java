package org.teachfx.antlr4.ep18r.stackvm;

import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser;

/**
 * 寄存器虚拟机字节码定义
 * 基于寄存器架构的独立指令集，专注于寄存器优化
 */
public class RegisterBytecodeDefinition {

    public static final int REG = VMAssemblerParser.REG;
    public static final int FUNC = VMAssemblerParser.FUNC;
    public static final int INT = VMAssemblerParser.INT;
    public static final int POOL = 1000;

    // 寄存器定义
    public static final int R0 = 0;   // 零寄存器（恒为0）
    public static final int R1 = 1;
    public static final int R2 = 2;
    public static final int R3 = 3;
    public static final int R4 = 4;
    public static final int R5 = 5;
    public static final int R6 = 6;
    public static final int R7 = 7;
    public static final int R8 = 8;
    public static final int R9 = 9;
    public static final int R10 = 10;
    public static final int R11 = 11;
    public static final int R12 = 12;
    public static final int R13 = 13; // 栈指针 (SP)
    public static final int R14 = 14; // 帧指针 (FP)
    public static final int R15 = 15; // 链接寄存器 (LR)

    public static final int NUM_REGISTERS = 16;

    // 指令定义
    // 整数算术指令 (R类型: rd, rs1, rs2)
    public static final short INSTR_ADD = 1;   // add rd, rs1, rs2
    public static final short INSTR_SUB = 2;   // sub rd, rs1, rs2
    public static final short INSTR_MUL = 3;   // mul rd, rs1, rs2
    public static final short INSTR_DIV = 4;   // div rd, rs1, rs2
    public static final short INSTR_SLT = 5;   // slt rd, rs1, rs2 (set less than)
    public static final short INSTR_SLE = 6;   // sle rd, rs1, rs2 (set less or equal)
    public static final short INSTR_SGT = 7;   // sgt rd, rs1, rs2 (set greater than)
    public static final short INSTR_SGE = 8;   // sge rd, rs1, rs2 (set greater or equal)
    public static final short INSTR_SEQ = 9;   // seq rd, rs1, rs2 (set equal)
    public static final short INSTR_SNE = 10;  // sne rd, rs1, rs2 (set not equal)
    public static final short INSTR_NEG = 11;  // neg rd, rs1
    public static final short INSTR_NOT = 12;  // not rd, rs1
    public static final short INSTR_AND = 13;  // and rd, rs1, rs2
    public static final short INSTR_OR = 14;   // or rd, rs1, rs2
    public static final short INSTR_XOR = 15;  // xor rd, rs1, rs2

    // 浮点算术指令
    public static final short INSTR_FADD = 16; // fadd rd, rs1, rs2
    public static final short INSTR_FSUB = 17; // fsub rd, rs1, rs2
    public static final short INSTR_FMUL = 18; // fmul rd, rs1, rs2
    public static final short INSTR_FDIV = 19; // fdiv rd, rs1, rs2
    public static final short INSTR_FLT = 20;  // flt rd, rs1, rs2
    public static final short INSTR_FEQ = 21;  // feq rd, rs1, rs2

    // 类型转换
    public static final short INSTR_ITOF = 22; // itof rd, rs1

    // 控制流指令
    public static final short INSTR_CALL = 23; // call target (I类型: rd=LR, rs1=?, immediate=target)
    public static final short INSTR_RET = 24;  // ret (J类型: 从LR跳转)
    public static final short INSTR_J = 25;    // j target (J类型: 无条件跳转)
    public static final short INSTR_JT = 26;   // jt rs1, target (I类型: 条件为真跳转)
    public static final short INSTR_JF = 27;   // jf rs1, target (I类型: 条件为假跳转)

    // 常量加载指令 (I类型: rd, immediate)
    public static final short INSTR_LI = 28;   // li rd, immediate (加载整数立即数)
    public static final short INSTR_LC = 29;   // lc rd, immediate (加载字符立即数)
    public static final short INSTR_LF = 30;   // lf rd, pool_index (加载浮点常量，来自常量池)
    public static final short INSTR_LS = 31;   // ls rd, pool_index (加载字符串常量，来自常量池)

    // 内存访问指令 (I类型: rd/rs, base, offset)
    public static final short INSTR_LW = 32;   // lw rd, base, offset (加载字)
    public static final short INSTR_SW = 33;   // sw rs, base, offset (存储字)
    public static final short INSTR_LW_G = 34; // lw_g rd, offset (全局加载，base=全局基址)
    public static final short INSTR_SW_G = 35; // sw_g rs, offset (全局存储)
    public static final short INSTR_LW_F = 36; // lw_f rd, offset (字段加载，base=对象指针)
    public static final short INSTR_SW_F = 37; // sw_f rs, offset (字段存储)

    // 其他指令
    public static final short INSTR_PRINT = 38; // print rs (打印寄存器值)
    public static final short INSTR_STRUCT = 39; // struct rd, size (分配结构体)
    public static final short INSTR_NULL = 40;   // null rd (加载空指针)
    public static final short INSTR_MOV = 41;    // mov rd, rs1 (寄存器间移动)
    public static final short INSTR_HALT = 42;   // halt (停止执行)

    // 所有指令数组
    public static Instruction[] instructions = new Instruction[]{
            null, // <INVALID> - index 0
            new Instruction("add", REG, REG, REG), // index 1
            new Instruction("sub", REG, REG, REG), // index 2
            new Instruction("mul", REG, REG, REG), // index 3
            new Instruction("div", REG, REG, REG), // index 4
            new Instruction("slt", REG, REG, REG), // index 5
            new Instruction("sle", REG, REG, REG), // index 6
            new Instruction("sgt", REG, REG, REG), // index 7
            new Instruction("sge", REG, REG, REG), // index 8
            new Instruction("seq", REG, REG, REG), // index 9
            new Instruction("sne", REG, REG, REG), // index 10
            new Instruction("neg", REG, REG),     // index 11 (rd, rs1)
            new Instruction("not", REG, REG),     // index 12
            new Instruction("and", REG, REG, REG), // index 13
            new Instruction("or", REG, REG, REG),  // index 14
            new Instruction("xor", REG, REG, REG), // index 15
            new Instruction("fadd", REG, REG, REG), // index 16
            new Instruction("fsub", REG, REG, REG), // index 17
            new Instruction("fmul", REG, REG, REG), // index 18
            new Instruction("fdiv", REG, REG, REG), // index 19
            new Instruction("flt", REG, REG, REG),  // index 20
            new Instruction("feq", REG, REG, REG),  // index 21
            new Instruction("itof", REG, REG),      // index 22
            new Instruction("call", INT),            // index 23 (跳转到函数地址，J类型)
            new Instruction("ret"),                 // index 24
            new Instruction("j", INT),              // index 25
            new Instruction("jt", REG, INT),        // index 26
            new Instruction("jf", REG, INT),        // index 27
            new Instruction("li", REG, INT),        // index 28
            new Instruction("lc", REG, INT),        // index 29
            new Instruction("lf", REG, POOL),       // index 30
            new Instruction("ls", REG, POOL),       // index 31
            new Instruction("lw", REG, REG, INT),   // index 32
            new Instruction("sw", REG, REG, INT),   // index 33
            new Instruction("lw_g", REG, INT),      // index 34
            new Instruction("sw_g", REG, INT),      // index 35
            new Instruction("lw_f", REG, REG, INT), // index 36 (字段加载: rd, base, offset)
            new Instruction("sw_f", REG, REG, INT), // index 37 (字段存储: rs, base, offset)
            new Instruction("print", REG),          // index 38 (I类型: rd=寄存器)
            new Instruction("struct", REG, INT),    // index 39
            new Instruction("null", REG),           // index 40 (I类型: rd=寄存器)
            new Instruction("mov", REG, REG),       // index 41
            new Instruction("halt"),                // index 42
    };

    // 静态初始化块：修正指令格式
    static {
        // print和null是单操作数I类型指令（寄存器操作数）
        instructions[RegisterBytecodeDefinition.INSTR_PRINT].setFormat(RegisterBytecodeDefinition.FORMAT_I);
        instructions[RegisterBytecodeDefinition.INSTR_NULL].setFormat(RegisterBytecodeDefinition.FORMAT_I);
    }

    // 指令格式常量
    public static final int FORMAT_R = 0;  // R类型: op rd, rs1, rs2
    public static final int FORMAT_I = 1;  // I类型: op rd, rs1, imm
    public static final int FORMAT_J = 2;  // J类型: op imm

    // 指令信息类
    public static class Instruction {
        String name;
        int[] type = new int[3];  // 操作数类型: REG, INT, POOL, FUNC
        int n = 0;                // 操作数数量
        int format = FORMAT_R;    // 指令格式

        public Instruction(String name) {
            this(name, 0, 0, 0);
            n = 0;
            format = FORMAT_J; // 无操作数指令通常为J类型
        }

        public Instruction(String name, int a) {
            this(name, a, 0, 0);
            n = 1;
            format = FORMAT_J; // 单操作数（立即数）为J类型
        }

        public void setFormat(int format) {
            this.format = format;
        }

        public Instruction(String name, int a, int b) {
            this(name, a, b, 0);
            n = 2;
            // 判断格式: 如果两个都是REG，可能是R类型（如neg rd, rs1）
            // 否则可能是I类型（如li rd, imm）
            if (a == REG && b == REG) {
                format = FORMAT_R;
            } else {
                format = FORMAT_I;
            }
        }

        public Instruction(String name, int a, int b, int c) {
            this.name = name;
            type[0] = a;
            type[1] = b;
            type[2] = c;
            n = 3;
            // 三个操作数: 可能是R类型（reg, reg, reg）或I类型（reg, reg, imm）
            if (c == REG) {
                format = FORMAT_R;
            } else {
                format = FORMAT_I;
            }
        }

        // 获取操作数类型
        public int getOperandType(int index) {
            if (index < 0 || index >= n) return 0;
            return type[index];
        }

        // 获取指令格式
        public int getFormat() {
            return format;
        }
    }

    // 根据操作码获取指令
    public static Instruction getInstruction(int opcode) {
        if (opcode < 0 || opcode >= instructions.length) {
            return null;
        }
        return instructions[opcode];
    }

    // 根据名称查找操作码
    public static int getOpcode(String name) {
        for (int i = 1; i < instructions.length; i++) {
            if (instructions[i] != null && instructions[i].name.equals(name)) {
                return i;
            }
        }
        return 0; // 无效操作码
    }
}