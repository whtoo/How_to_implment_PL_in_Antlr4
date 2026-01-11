package org.teachfx.antlr4.ep18r.pass.codegen;

import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * 32位字节码编码器，用于寄存器VM字节码的编码/解码。
 *
 * <p>EP18R 指令格式（32位固定长度，大端序）:
 * <ul>
 *   <li>R-type: [opcode:6][rd:5][rs1:5][rs2:5][unused:11]</li>
 *   <li>I-type: [opcode:6][rd:5][rs1:5][imm:16]</li>
 *   <li>J-type: [opcode:6][imm:26]</li>
 * </ul>
 *
 * @see RegisterBytecodeDefinition
 */
public class ByteCodeEncoder {

    // 指令格式常量
    public static final int FORMAT_R = 0;
    public static final int FORMAT_I = 1;
    public static final int FORMAT_J = 2;

    // 字段位置和掩码常量
    private static final int OPCODE_SHIFT = 26;
    private static final int OPCODE_MASK = 0x3F;  // 6位
    private static final int RD_SHIFT = 21;
    private static final int RD_MASK = 0x1F;  // 5位
    private static final int RS1_SHIFT = 16;
    private static final int RS1_MASK = 0x1F;  // 5位
    private static final int RS2_SHIFT = 11;
    private static final int RS2_MASK = 0x1F;  // 5位
    private static final int IMM16_MASK = 0xFFFF;  // 16位
    private static final int IMM26_MASK = 0x3FFFFFF;  // 26位

    // 操作码到指令名称的映射（用于调试输出）
    private static final Map<Integer, String> OPCODE_TO_NAME;

    static {
        OPCODE_TO_NAME = new HashMap<>();
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_ADD, "add");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_SUB, "sub");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_MUL, "mul");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_DIV, "div");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_SLT, "slt");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_SLE, "sle");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_SGT, "sgt");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_SGE, "sge");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_SEQ, "seq");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_SNE, "sne");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_NEG, "neg");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_NOT, "not");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_AND, "and");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_OR, "or");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_XOR, "xor");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_FADD, "fadd");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_FSUB, "fsub");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_FMUL, "fmul");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_FDIV, "fdiv");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_FLT, "flt");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_FEQ, "feq");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_ITOF, "itof");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_CALL, "call");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_RET, "ret");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_J, "j");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_JT, "jt");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_JF, "jf");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_LI, "li");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_LC, "lc");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_LF, "lf");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_LS, "ls");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_LW, "lw");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_SW, "sw");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_LW_G, "lw_g");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_SW_G, "sw_g");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_LW_F, "lw_f");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_SW_F, "sw_f");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_PRINT, "print");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_STRUCT, "struct");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_NULL, "null");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_MOV, "mov");
        OPCODE_TO_NAME.put((int) RegisterBytecodeDefinition.INSTR_HALT, "halt");
    }

    /**
     * 编码32位大端序指令字节数组。
     *
     * @param instruction 32位指令字
     * @return 4字节数组（大端序）
     */
    public byte[] encode(int instruction) {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(instruction);
        return buffer.array();
    }

    /**
     * 解码字节数组为32位指令字。
     *
     * @param bytes 4字节数组（大端序）
     * @return 32位指令字
     * @throws IllegalArgumentException 如果字节数组长度不为4
     */
    public int decode(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Instruction must be exactly 4 bytes, got: " + bytes.length);
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }

    /**
     * 编码R类型指令: [opcode:6][rd:5][rs1:5][rs2:5][unused:11]
     *
     * @param opcode 6位操作码
     * @param rd     5位目标寄存器
     * @param rs1    5位源寄存器1
     * @param rs2    5位源寄存器2
     * @return 32位编码指令
     * @throws IllegalArgumentException 如果任何参数超出其位数范围
     */
    public int encodeRType(int opcode, int rd, int rs1, int rs2) {
        validateOpcode(opcode);
        validateRegister(rd);
        validateRegister(rs1);
        validateRegister(rs2);

        return ((opcode & OPCODE_MASK) << OPCODE_SHIFT) |
               ((rd & RD_MASK) << RD_SHIFT) |
               ((rs1 & RS1_MASK) << RS1_SHIFT) |
               ((rs2 & RS2_MASK) << RS2_SHIFT);
    }

    /**
     * 编码I类型指令: [opcode:6][rd:5][rs1:5][imm:16]
     *
     * @param opcode 6位操作码
     * @param rd     5位目标寄存器
     * @param rs1    5位源寄存器1（或基址寄存器）
     * @param imm    16位有符号立即数
     * @return 32位编码指令
     * @throws IllegalArgumentException 如果任何参数超出其位数范围
     */
    public int encodeIType(int opcode, int rd, int rs1, int imm) {
        validateOpcode(opcode);
        validateRegister(rd);
        validateRegister(rs1);
        validateImmediate16(imm);

        // 符号扩展处理：确保16位立即数正确扩展
        int immValue = imm & IMM16_MASK;

        return ((opcode & OPCODE_MASK) << OPCODE_SHIFT) |
               ((rd & RD_MASK) << RD_SHIFT) |
               ((rs1 & RS1_MASK) << RS1_SHIFT) |
               immValue;
    }

    /**
     * 编码J类型指令: [opcode:6][imm:26]
     *
     * @param opcode 6位操作码
     * @param imm    26位有符号立即数（通常为跳转目标地址）
     * @return 32位编码指令
     * @throws IllegalArgumentException 如果任何参数超出其位数范围
     */
    public int encodeJType(int opcode, int imm) {
        validateOpcode(opcode);
        validateImmediate26(imm);

        // 符号扩展处理：确保26位立即数正确扩展
        int immValue = imm & IMM26_MASK;

        return ((opcode & OPCODE_MASK) << OPCODE_SHIFT) | immValue;
    }

    /**
     * 从编码指令中提取操作码。
     *
     * @param instruction 32位编码指令
     * @return 6位操作码
     */
    public int extractOpcode(int instruction) {
        return (instruction >>> OPCODE_SHIFT) & OPCODE_MASK;
    }

    /**
     * 从R/I类型指令中提取rd字段。
     *
     * @param instruction 32位编码指令
     * @return 5位rd寄存器编号
     */
    public int extractRd(int instruction) {
        return (instruction >>> RD_SHIFT) & RD_MASK;
    }

    /**
     * 从R/I类型指令中提取rs1字段。
     *
     * @param instruction 32位编码指令
     * @return 5位rs1寄存器编号
     */
    public int extractRs1(int instruction) {
        return (instruction >>> RS1_SHIFT) & RS1_MASK;
    }

    /**
     * 从R类型指令中提取rs2字段。
     *
     * @param instruction 32位编码指令
     * @return 5位rs2寄存器编号
     */
    public int extractRs2(int instruction) {
        return (instruction >>> RS2_SHIFT) & RS2_MASK;
    }

    /**
     * 从I类型指令中提取16位立即数（带符号扩展）。
     *
     * @param instruction 32位编码指令
     * @return 16位有符号立即数
     */
    public int extractImm16(int instruction) {
        int imm = instruction & IMM16_MASK;
        // 符号扩展：如果最高位为1，则扩展为负数
        if ((imm & 0x8000) != 0) {
            imm |= 0xFFFF0000;
        }
        return imm;
    }

    /**
     * 从J类型指令中提取26位立即数（带符号扩展）。
     *
     * @param instruction 32位编码指令
     * @return 26位有符号立即数
     */
    public int extractImm26(int instruction) {
        int imm = instruction & IMM26_MASK;
        // 符号扩展：如果最高位为1，则扩展为负数
        if ((imm & 0x2000000) != 0) {
            imm |= 0xFC000000;
        }
        return imm;
    }

    /**
     * 判断指令格式类型。
     *
     * @param opcode 操作码
     * @return 指令格式类型 (R/I/J)
     */
    public int getInstructionFormat(int opcode) {
        RegisterBytecodeDefinition.Instruction instr = RegisterBytecodeDefinition.getInstruction(opcode);
        if (instr == null) {
            throw new IllegalArgumentException("Unknown opcode: " + opcode);
        }
        return instr.getFormat();
    }

    /**
     * 验证操作码是否在有效范围内（0-63）。
     */
    private void validateOpcode(int opcode) {
        if (opcode < 0 || opcode > OPCODE_MASK) {
            throw new IllegalArgumentException("Opcode must be 6 bits (0-63), got: " + opcode);
        }
    }

    /**
     * 验证寄存器编号是否在有效范围内（0-31）。
     * EP18R 使用16个寄存器（0-15），但5位可支持32个。
     */
    private void validateRegister(int reg) {
        if (reg < 0 || reg > RD_MASK) {
            throw new IllegalArgumentException("Register must be 5 bits (0-31), got: " + reg);
        }
    }

    /**
     * 验证16位立即数是否在有效范围内。
     */
    private void validateImmediate16(int imm) {
        if (imm < Short.MIN_VALUE || imm > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Immediate must be 16 bits (-32768 to 32767), got: " + imm);
        }
    }

    /**
     * 验证26位立即数是否在有效范围内。
     */
    private void validateImmediate26(int imm) {
        int max26 = (1 << 25) - 1;  // 2^25 - 1
        int min26 = -(1 << 25);     // -2^25
        if (imm < min26 || imm > max26) {
            throw new IllegalArgumentException("Immediate must be 26 bits, got: " + imm);
        }
    }

    /**
     * 将编码指令转换为可读的汇编格式（用于调试）。
     *
     * @param instruction 32位编码指令
     * @return 汇编格式字符串
     */
    public String toAssemblyString(int instruction) {
        int opcode = extractOpcode(instruction);
        RegisterBytecodeDefinition.Instruction instrDef = RegisterBytecodeDefinition.getInstruction(opcode);

        if (instrDef == null) {
            return String.format(".unknown 0x%08X", instruction);
        }

        int format = instrDef.getFormat();
        String name = getInstructionName(opcode);

        return switch (format) {
            case FORMAT_R -> String.format("%s r%d, r%d, r%d",
                name, extractRd(instruction), extractRs1(instruction), extractRs2(instruction));
            case FORMAT_I -> {
                int rd = extractRd(instruction);
                int rs1 = extractRs1(instruction);
                int imm = extractImm16(instruction);

                // 特殊处理jt/jf指令（格式为jt rs1, imm）
                if (name.equals("jt") || name.equals("jf")) {
                    yield String.format("%s r%d, %d", name, rs1, imm);
                }
                // 特殊处理li指令（格式为li rd, imm）
                if (name.equals("li") || name.equals("lc")) {
                    yield String.format("%s r%d, %d", name, rd, imm);
                }
                // 标准I类型指令
                yield String.format("%s r%d, r%d, %d", name, rd, rs1, imm);
            }
            case FORMAT_J -> String.format("%s %d", name, extractImm26(instruction));
            default -> String.format(".unknown_format 0x%08X", instruction);
        };
    }

    /**
     * 根据操作码获取指令名称。
     *
     * @param opcode 操作码
     * @return 指令名称，如果未知返回".unknown"
     */
    private String getInstructionName(int opcode) {
        return OPCODE_TO_NAME.getOrDefault(opcode, ".unknown");
    }
}
