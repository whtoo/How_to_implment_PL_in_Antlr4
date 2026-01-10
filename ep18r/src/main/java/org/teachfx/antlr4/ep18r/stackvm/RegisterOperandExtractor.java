package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 寄存器操作数提取工具类
 * 统一处理32位指令格式中的各种字段提取操作
 *
 * 指令格式定义（参考EP18R设计规范）：
 *
 * R类型（寄存器-寄存器运算）：
 *  31        26 25     21 20     16 15     11 10         0
 * ┌────────────┬─────────┬─────────┬─────────┬─────────────┐
 * │   opcode   │    rd   │   rs1   │   rs2   │   unused    │
 * │    (6)     │   (5)   │   (5)   │   (5)   │    (11)     │
 * └────────────┴─────────┴─────────┴─────────┴─────────────┘
 *
 * I类型（立即数/内存访问）：
 *  31        26 25     21 20     16 15                    0
 * ┌────────────┬─────────┬─────────┬───────────────────────┐
 * │   opcode   │    rd   │   rs1   │      immediate        │
 * │    (6)     │   (5)   │   (5)   │         (16)          │
 * └────────────┴─────────┴─────────┴───────────────────────┘
 *
 * J类型（跳转指令）：
 *  31        26 25                                         0
 * ┌────────────┬───────────────────────────────────────────┐
 * │   opcode   │               address                     │
 * │    (6)     │                  (26)                     │
 * └────────────┴───────────────────────────────────────────┘
 */
public class RegisterOperandExtractor {

    // 私有构造函数，防止实例化（工具类模式）
    private RegisterOperandExtractor() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    /** 操作码掩码（bits 31-26） */
    public static final int OPCODE_MASK = 0xFC000000;

    /** rd字段掩码（bits 25-21） */
    public static final int RD_MASK = 0x03E00000;

    /** rs1字段掩码（bits 20-16） */
    public static final int RS1_MASK = 0x001F0000;

    /** rs2字段掩码（bits 15-11） */
    public static final int RS2_MASK = 0x0000F800;

    /** 16位立即数掩码（bits 15-0） */
    public static final int IMM16_MASK = 0x0000FFFF;

    /** 26位地址掩码（bits 25-0） */
    public static final int IMM26_MASK = 0x03FFFFFF;

    /**
     * 从32位指令中提取操作码字段（bits 31-26）
     *
     * @param instruction 32位指令字
     * @return 操作码（0-63）
     */
    public static int extractOpcode(int instruction) {
        return (instruction >>> 26) & 0x3F;
    }

    /**
     * 从32位指令中提取rd字段（bits 25-21，目标寄存器）
     *
     * @param instruction 32位指令字
     * @return 寄存器编号（0-31）
     */
    public static int extractRd(int instruction) {
        return (instruction >>> 21) & 0x1F;
    }

    /**
     * 从32位指令中提取rs1字段（bits 20-16，源寄存器1）
     *
     * @param instruction 32位指令字
     * @return 寄存器编号（0-31）
     */
    public static int extractRs1(int instruction) {
        return (instruction >>> 16) & 0x1F;
    }

    /**
     * 从32位指令中提取rs2字段（bits 15-11，源寄存器2）
     *
     * @param instruction 32位指令字
     * @return 寄存器编号（0-31）
     */
    public static int extractRs2(int instruction) {
        return (instruction >>> 11) & 0x1F;
    }

    /**
     * 从32位指令中提取16位立即数（bits 15-0，符号扩展到32位）
     *
     * @param instruction 32位指令字
     * @return 符号扩展后的32位立即数
     */
    public static int extractImm16(int instruction) {
        int imm = instruction & IMM16_MASK;
        // 符号扩展：如果最高位（bit 15）为1，则扩展为负数
        if ((imm & 0x8000) != 0) {
            imm |= 0xFFFF0000;
        }
        return imm;
    }

    /**
     * 从32位指令中提取26位地址（bits 25-0，符号扩展到32位）
     *
     * @param instruction 32位指令字
     * @return 符号扩展后的32位地址
     */
    public static int extractImm26(int instruction) {
        int imm = instruction & IMM26_MASK;
        // 符号扩展：如果最高位（bit 25）为1，则扩展为负数
        if ((imm & 0x02000000) != 0) {
            imm |= 0xFC000000;
        }
        return imm;
    }



    /**
     * 将各字段组装成32位指令（R类型）
     *
     * @param opcode 操作码（0-63）
     * @param rd 目标寄存器（0-31）
     * @param rs1 源寄存器1（0-31）
     * @param rs2 源寄存器2（0-31）
     * @return 32位指令字
     * @throws IllegalArgumentException 如果任何参数超出范围
     */
    public static int buildRTypeInstruction(int opcode, int rd, int rs1, int rs2) {
        validateOpcode(opcode);
        validateRegister(rd);
        validateRegister(rs1);
        validateRegister(rs2);

        return ((opcode & 0x3F) << 26) |
               ((rd & 0x1F) << 21) |
               ((rs1 & 0x1F) << 16) |
               ((rs2 & 0x1F) << 11);
    }

    /**
     * 将各字段组装成32位指令（I类型）
     *
     * @param opcode 操作码（0-63）
     * @param rd 目标寄存器（0-31）
     * @param rs1 源寄存器1（0-31）
     * @param imm16 16位立即数（-32768 到 32767）
     * @return 32位指令字
     * @throws IllegalArgumentException 如果任何参数超出范围
     */
    public static int buildITypeInstruction(int opcode, int rd, int rs1, int imm16) {
        validateOpcode(opcode);
        validateRegister(rd);
        validateRegister(rs1);
        validateImm16(imm16);

        return ((opcode & 0x3F) << 26) |
               ((rd & 0x1F) << 21) |
               ((rs1 & 0x1F) << 16) |
               (imm16 & 0xFFFF);
    }

    /**
     * 将各字段组装成32位指令（J类型）
     *
     * @param opcode 操作码（0-63）
     * @param address 26位地址
     * @return 32位指令字
     * @throws IllegalArgumentException 如果任何参数超出范围
     */
    public static int buildJTypeInstruction(int opcode, int address) {
        validateOpcode(opcode);
        validateImm26(address);

        return ((opcode & 0x3F) << 26) | (address & 0x03FFFFFF);
    }



    /**
     * 验证操作码范围
     *
     * @param opcode 操作码
     * @throws IllegalArgumentException 如果操作码超出范围
     */
    public static void validateOpcode(int opcode) {
        if (opcode < 0 || opcode > 63) {
            throw new IllegalArgumentException(
                "Invalid opcode: " + opcode + ", must be 0-63");
        }
    }

    /**
     * 验证寄存器编号范围
     *
     * @param regNum 寄存器编号
     * @throws IllegalArgumentException 如果寄存器编号超出范围
     */
    public static void validateRegister(int regNum) {
        if (regNum < 0 || regNum >= RegisterBytecodeDefinition.NUM_REGISTERS) {
            throw new IllegalArgumentException(
                "Invalid register number: " + regNum + ", must be 0-" +
                (RegisterBytecodeDefinition.NUM_REGISTERS - 1));
        }
    }

    /**
     * 验证16位立即数范围
     *
     * @param imm16 16位立即数
     * @throws IllegalArgumentException 如果立即数超出范围
     */
    public static void validateImm16(int imm16) {
        if (imm16 < -32768 || imm16 > 32767) {
            throw new IllegalArgumentException(
                "Invalid 16-bit immediate: " + imm16 + ", must be -32768 to 32767");
        }
    }

    /**
     * 验证26位地址范围
     *
     * @param address 26位地址
     * @throws IllegalArgumentException 如果地址超出范围
     */
    public static void validateImm26(int address) {
        if (address < -0x02000000 || address > 0x03FFFFFF) {
            throw new IllegalArgumentException(
                "Invalid 26-bit address: " + address +
                ", must be -33,554,432 to 67,108,863");
        }
    }

    /**
     * 检查指令是否为R类型（通过检查未使用字段是否全为0）
     *
     * @param instruction 32位指令字
     * @return true如果可能是R类型指令
     */
    public static boolean isRType(int instruction) {
        return (instruction & 0x7FF) == 0;
    }

    /**
     * 检查指令是否为I类型
     *
     * @param instruction 32位指令字
     * @return true如果可能是I类型指令
     */
    public static boolean isIType(int instruction) {
        return true;
    }

    /**
     * 检查指令是否为J类型（根据操作码判断）
     *
     * @param instruction 32位指令字
     * @return true如果可能是J类型指令
     */
    public static boolean isJType(int instruction) {
        int opcode = extractOpcode(instruction);
        return opcode == 22 || opcode == 24 || opcode == 25;
    }
}
