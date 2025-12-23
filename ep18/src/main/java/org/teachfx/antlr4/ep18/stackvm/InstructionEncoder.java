package org.teachfx.antlr4.ep18.stackvm;

/**
 * 32位定长指令编码器/解码器
 * <p>
 * 统一指令格式: opcode(8) + rd(5) + rs1(5) + rs2(5) + imm(9)
 * </p>
 * <p>
 * EP18栈式VM: 使用opcode字段，其余字段填0<br>
 * EP18R寄存器VM: 使用所有字段
 * </p>
 */
public class InstructionEncoder {

    /**
     * 指令编码格式版本
     */
    public static final String FORMAT_VERSION = "1.0";

    /**
     * 32位指令掩码
     */
    private static final int OPCODE_MASK = 0xFF000000;
    private static final int RD_MASK = 0x00F80000;
    private static final int RS1_MASK = 0x0007C000;
    private static final int RS2_MASK = 0x00003E00;
    private static final int IMM_MASK = 0x000001FF;

    /**
     * 指令字段移位量
     */
    private static final int OPCODE_SHIFT = 24;
    private static final int RD_SHIFT = 19;
    private static final int RS1_SHIFT = 14;
    private static final int RS2_SHIFT = 9;
    private static final int IMM_SHIFT = 0;

    /**
     * EP18栈式VM指令操作码
     */
    public static final int OP_NOP = 0x00;
    public static final int OP_HALT = 0x01;
    public static final int OP_PUSH = 0x02;
    public static final int OP_POP = 0x03;
    public static final int OP_DUP = 0x04;
    public static final int OP_ILOAD = 0x10;
    public static final int OP_ISTORE = 0x11;
    public static final int OP_GLOAD = 0x12;
    public static final int OP_GSTORE = 0x13;
    public static final int OP_IADD = 0x20;
    public static final int OP_ISUB = 0x21;
    public static final int OP_IMUL = 0x22;
    public static final int OP_IDIV = 0x23;
    public static final int OP_IMOD = 0x24;
    public static final int OP_INEG = 0x25;
    public static final int OP_IAND = 0x26;
    public static final int OP_IOR = 0x27;
    public static final int OP_IXOR = 0x28;
    public static final int OP_ICMP = 0x30;
    public static final int OP_ZCMP = 0x31;
    public static final int OP_BEQZ = 0x40;
    public static final int OP_BNEZ = 0x41;
    public static final int OP_J = 0x42;
    public static final int OP_CALL = 0x43;
    public static final int OP_RET = 0x44;
    public static final int OP_PRINT = 0x50;
    public static final int OP_PRINTS = 0x51;

    /**
     * EP18R寄存器VM扩展指令操作码
     */
    public static final int OP_MOV = 0x60;
    public static final int OP_LI = 0x61;
    public static final int OP_LW = 0x62;
    public static final int OP_SW = 0x63;
    public static final int OP_ADD = 0x64;
    public static final int OP_SUB = 0x65;
    public static final int OP_MUL = 0x66;
    public static final int OP_DIV = 0x67;
    public static final int OP_SLT = 0x68;
    public static final int OP_SLE = 0x69;
    public static final int OP_SGT = 0x6A;
    public static final int OP_SGE = 0x6B;
    public static final int OP_SEQ = 0x6C;
    public static final int OP_SNE = 0x6D;
    public static final int OP_JF = 0x6E;

    /**
     * 编码无操作数指令（EP18栈式VM）
     *
     * @param opcode 操作码
     * @return 32位指令编码
     */
    public static int encode(int opcode) {
        return (opcode & 0xFF) << OPCODE_SHIFT;
    }

    /**
     * 编码单立即数指令（EP18栈式VM）
     *
     * @param opcode 操作码
     * @param imm    立即数（9位，符号扩展）
     * @return 32位指令编码
     */
    public static int encodeImm(int opcode, int imm) {
        int signExtendedImm = signExtend9(imm);
        return ((opcode & 0xFF) << OPCODE_SHIFT) | (signExtendedImm & IMM_MASK);
    }

    /**
     * 编码单寄存器指令（EP18R寄存器VM）
     *
     * @param opcode 操作码
     * @param rd     目标寄存器（0-31）
     * @return 32位指令编码
     */
    public static int encodeReg(int opcode, int rd) {
        return ((opcode & 0xFF) << OPCODE_SHIFT) | ((rd & 0x1F) << RD_SHIFT);
    }

    /**
     * 编码寄存器+立即数指令（EP18R寄存器VM）
     *
     * @param opcode 操作码
     * @param rd     目标寄存器（0-31）
     * @param imm    立即数（9位，符号扩展）
     * @return 32位指令编码
     */
    public static int encodeRegImm(int opcode, int rd, int imm) {
        int signExtendedImm = signExtend9(imm);
        return ((opcode & 0xFF) << OPCODE_SHIFT) |
               ((rd & 0x1F) << RD_SHIFT) |
               (signExtendedImm & IMM_MASK);
    }

    /**
     * 编码双寄存器指令（EP18R寄存器VM）
     *
     * @param opcode 操作码
     * @param rd     目标寄存器（0-31）
     * @param rs     源寄存器（0-31）
     * @return 32位指令编码
     */
    public static int encodeRegReg(int opcode, int rd, int rs) {
        return ((opcode & 0xFF) << OPCODE_SHIFT) |
               ((rd & 0x1F) << RD_SHIFT) |
               ((rs & 0x1F) << RS1_SHIFT);
    }

    /**
     * 编码三寄存器指令（EP18R寄存器VM）
     *
     * @param opcode 操作码
     * @param rd     目标寄存器（0-31）
     * @param rs1    源寄存器1（0-31）
     * @param rs2    源寄存器2（0-31）
     * @return 32位指令编码
     */
    public static int encodeRegRegReg(int opcode, int rd, int rs1, int rs2) {
        return ((opcode & 0xFF) << OPCODE_SHIFT) |
               ((rd & 0x1F) << RD_SHIFT) |
               ((rs1 & 0x1F) << RS1_SHIFT) |
               ((rs2 & 0x1F) << RS2_SHIFT);
    }

    /**
     * 编码寄存器+偏移量指令（EP18R寄存器VM）
     *
     * @param opcode 操作码
     * @param rd     基址寄存器（0-31）
     * @param offset 偏移量（9位，符号扩展）
     * @return 32位指令编码
     */
    public static int encodeRegOffset(int opcode, int rd, int offset) {
        int signExtendedOffset = signExtend9(offset);
        return ((opcode & 0xFF) << OPCODE_SHIFT) |
               ((rd & 0x1F) << RD_SHIFT) |
               (signExtendedOffset & IMM_MASK);
    }

    /**
     * 编码条件跳转指令（EP18R寄存器VM）
     *
     * @param opcode 操作码
     * @param rs     源寄存器（0-31）
     * @param offset 跳转偏移量（9位，符号扩展）
     * @return 32位指令编码
     */
    public static int encodeJump(int opcode, int rs, int offset) {
        int signExtendedOffset = signExtend9(offset);
        return ((opcode & 0xFF) << OPCODE_SHIFT) |
               ((rs & 0x1F) << RS1_SHIFT) |
               (signExtendedOffset & IMM_MASK);
    }

    // ==================== 解码方法 ====================

    /**
     * 解码操作码
     *
     * @param instruction 32位指令编码
     * @return 操作码
     */
    public static int decodeOpcode(int instruction) {
        return (instruction & OPCODE_MASK) >>> OPCODE_SHIFT;
    }

    /**
     * 解码rd字段
     *
     * @param instruction 32位指令编码
     * @return rd寄存器编号
     */
    public static int decodeRd(int instruction) {
        return (instruction & RD_MASK) >>> RD_SHIFT;
    }

    /**
     * 解码rs1字段
     *
     * @param instruction 32位指令编码
     * @return rs1寄存器编号
     */
    public static int decodeRs1(int instruction) {
        return (instruction & RS1_MASK) >>> RS1_SHIFT;
    }

    /**
     * 解码rs2字段
     *
     * @param instruction 32位指令编码
     * @return rs2寄存器编号
     */
    public static int decodeRs2(int instruction) {
        return (instruction & RS2_MASK) >>> RS2_SHIFT;
    }

    /**
     * 解码imm字段（符号扩展）
     *
     * @param instruction 32位指令编码
     * @return 立即数（32位，带符号扩展）
     */
    public static int decodeImm(int instruction) {
        int imm = (instruction & IMM_MASK) >>> IMM_SHIFT;
        return signExtend9(imm);
    }

    /**
     * 9位符号扩展
     *
     * @param value 9位值
     * @return 32位符号扩展值
     */
    public static int signExtend9(int value) {
        if ((value & 0x100) != 0) {
            return value | 0xFFFFFE00;
        }
        return value & 0x1FF;
    }

    /**
     * 编码为字节数组（大端序）
     *
     * @param instruction 32位指令编码
     * @return 4字节数组
     */
    public static byte[] toBytes(int instruction) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((instruction >>> 24) & 0xFF);
        bytes[1] = (byte) ((instruction >>> 16) & 0xFF);
        bytes[2] = (byte) ((instruction >>> 8) & 0xFF);
        bytes[3] = (byte) (instruction & 0xFF);
        return bytes;
    }

    /**
     * 从字节数组解码（大端序）
     *
     * @param bytes 4字节数组
     * @return 32位指令编码
     */
    public static int fromBytes(byte[] bytes) {
        if (bytes.length < 4) {
            throw new IllegalArgumentException("需要至少4个字节");
        }
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }

    /**
     * 获取操作码对应的助记符
     *
     * @param opcode 操作码
     * @return 助记符
     */
    public static String getMnemonic(int opcode) {
        return switch (opcode) {
            case OP_NOP -> "nop";
            case OP_HALT -> "halt";
            case OP_PUSH -> "push";
            case OP_POP -> "pop";
            case OP_DUP -> "dup";
            case OP_ILOAD -> "iload";
            case OP_ISTORE -> "istore";
            case OP_GLOAD -> "gload";
            case OP_GSTORE -> "gstore";
            case OP_IADD -> "iadd";
            case OP_ISUB -> "isub";
            case OP_IMUL -> "imul";
            case OP_IDIV -> "idiv";
            case OP_IMOD -> "imod";
            case OP_INEG -> "ineg";
            case OP_IAND -> "iand";
            case OP_IOR -> "ior";
            case OP_IXOR -> "ixor";
            case OP_ICMP -> "icmp";
            case OP_ZCMP -> "zcmp";
            case OP_BEQZ -> "beqz";
            case OP_BNEZ -> "bnez";
            case OP_J -> "j";
            case OP_CALL -> "call";
            case OP_RET -> "ret";
            case OP_PRINT -> "print";
            case OP_PRINTS -> "prints";
            case OP_MOV -> "mov";
            case OP_LI -> "li";
            case OP_LW -> "lw";
            case OP_SW -> "sw";
            case OP_ADD -> "add";
            case OP_SUB -> "sub";
            case OP_MUL -> "mul";
            case OP_DIV -> "div";
            case OP_SLT -> "slt";
            case OP_SLE -> "sle";
            case OP_SGT -> "sgt";
            case OP_SGE -> "sge";
            case OP_SEQ -> "seq";
            case OP_SNE -> "sne";
            case OP_JF -> "jf";
            default -> "unknown";
        };
    }

    /**
     * 验证寄存器编号是否有效
     *
     * @param reg 寄存器编号
     * @return 是否有效
     */
    public static boolean isValidRegister(int reg) {
        return reg >= 0 && reg <= 31;
    }

    /**
     * 验证立即数范围（9位）
     *
     * @param imm 立即数
     * @return 是否在范围内
     */
    public static boolean isValidImmediate9(int imm) {
        return imm >= -256 && imm <= 255;
    }

    /**
     * 验证操作码是否有效
     *
     * @param opcode 操作码
     * @return 是否为有效操作码
     */
    public static boolean isValidOpcode(int opcode) {
        return opcode >= 0x00 && opcode <= 0x6E;
    }
}
