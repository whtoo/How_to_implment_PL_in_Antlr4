package org.teachfx.antlr4.ep18.stackvm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 32位定长指令格式测试
 * <p>
 * 验证InstructionEncoder/Decoder和汇编器/反汇编器的正确性
 * </p>
 */
@DisplayName("32-bit Instruction Format Tests")
public class InstructionFormat32Test {

    @BeforeEach
    public void setUp() {
        // Setup if needed
    }

    // ==================== Encoder Tests ====================

    @Test
    @DisplayName("Should encode nop instruction correctly")
    void testEncodeNop() {
        int instruction = InstructionEncoder.encode(InstructionEncoder.OP_NOP);
        // opcode(8) << 24
        assertThat(instruction).isEqualTo(0x00000000);
        assertThat(InstructionEncoder.decodeOpcode(instruction)).isEqualTo(InstructionEncoder.OP_NOP);
    }

    @Test
    @DisplayName("Should encode halt instruction correctly")
    void testEncodeHalt() {
        int instruction = InstructionEncoder.encode(InstructionEncoder.OP_HALT);
        // opcode(8) << 24, OP_HALT = 0x01
        assertThat(instruction).isEqualTo(0x01000000);
        assertThat(InstructionEncoder.decodeOpcode(instruction)).isEqualTo(InstructionEncoder.OP_HALT);
    }

    @Test
    @DisplayName("Should encode push instruction with immediate")
    void testEncodePush() {
        int instruction = InstructionEncoder.encodeImm(InstructionEncoder.OP_PUSH, 10);
        // opcode(8) << 24 + immediate(9), OP_PUSH = 0x02
        assertThat(instruction).isEqualTo(0x0200000A);
        assertThat(InstructionEncoder.decodeOpcode(instruction)).isEqualTo(InstructionEncoder.OP_PUSH);
        assertThat(InstructionEncoder.decodeImm(instruction)).isEqualTo(10);
    }

    @Test
    @DisplayName("Should encode push with negative immediate")
    void testEncodePushNegative() {
        int instruction = InstructionEncoder.encodeImm(InstructionEncoder.OP_PUSH, -5);

        // Verify sign extension
        assertThat(InstructionEncoder.decodeImm(instruction)).isEqualTo(-5);
    }

    @Test
    @DisplayName("Should encode register move instruction")
    void testEncodeMov() {
        int instruction = InstructionEncoder.encodeRegReg(InstructionEncoder.OP_MOV, 2, 3);
        // opcode(8) << 24 + rd(5) << 19 + rs1(5) << 14, OP_MOV = 0x60
        // 0x60 << 24 = 0x60000000, rd=2 << 19 = 0x00400000, rs1=3 << 14 = 0x000C0000
        int expected = (0x60 << 24) | (2 << 19) | (3 << 14);
        assertThat(instruction).isEqualTo(expected);
        assertThat(InstructionEncoder.decodeOpcode(instruction)).isEqualTo(InstructionEncoder.OP_MOV);
        assertThat(InstructionEncoder.decodeRd(instruction)).isEqualTo(2);
        assertThat(InstructionEncoder.decodeRs1(instruction)).isEqualTo(3);
    }

    @Test
    @DisplayName("Should encode three-register add instruction")
    void testEncodeAdd() {
        int instruction = InstructionEncoder.encodeRegRegReg(InstructionEncoder.OP_ADD, 4, 2, 3);
        // opcode(8) << 24 + rd(5) << 19 + rs1(5) << 14 + rs2(5) << 9, OP_ADD = 0x64
        int expected = (0x64 << 24) | (4 << 19) | (2 << 14) | (3 << 9);
        assertThat(instruction).isEqualTo(expected);
        assertThat(InstructionEncoder.decodeOpcode(instruction)).isEqualTo(InstructionEncoder.OP_ADD);
        assertThat(InstructionEncoder.decodeRd(instruction)).isEqualTo(4);
        assertThat(InstructionEncoder.decodeRs1(instruction)).isEqualTo(2);
        assertThat(InstructionEncoder.decodeRs2(instruction)).isEqualTo(3);
    }

    @Test
    @DisplayName("Should encode li instruction with immediate")
    void testEncodeLi() {
        int instruction = InstructionEncoder.encodeRegImm(InstructionEncoder.OP_LI, 2, 42);
        // opcode(8) << 24 + rd(5) << 19 + imm(9), OP_LI = 0x61
        int expected = (0x61 << 24) | (2 << 19) | 42;
        assertThat(instruction).isEqualTo(expected);
        assertThat(InstructionEncoder.decodeOpcode(instruction)).isEqualTo(InstructionEncoder.OP_LI);
        assertThat(InstructionEncoder.decodeRd(instruction)).isEqualTo(2);
        assertThat(InstructionEncoder.decodeImm(instruction)).isEqualTo(42);
    }

    @Test
    @DisplayName("Should encode jf conditional jump")
    void testEncodeJump() {
        int instruction = InstructionEncoder.encodeJump(InstructionEncoder.OP_JF, 5, 100);

        assertThat(InstructionEncoder.decodeOpcode(instruction)).isEqualTo(InstructionEncoder.OP_JF);
        assertThat(InstructionEncoder.decodeRs1(instruction)).isEqualTo(5);
        assertThat(InstructionEncoder.decodeImm(instruction)).isEqualTo(100);
    }

    // ==================== Sign Extension Tests ====================

    @Test
    @DisplayName("Should sign extend positive 9-bit value")
    void testSignExtendPositive() {
        int result = InstructionEncoder.signExtend9(100);
        assertThat(result).isEqualTo(100);
    }

    @Test
    @DisplayName("Should sign extend negative 9-bit value")
    void testSignExtendNegative() {
        int result = InstructionEncoder.signExtend9(256); // 0x100, bit 9 set
        assertThat(result).isEqualTo(-256);
    }

    @Test
    @DisplayName("Should validate immediate range")
    void testValidateImmediateRange() {
        assertThat(InstructionEncoder.isValidImmediate9(-256)).isTrue();
        assertThat(InstructionEncoder.isValidImmediate9(255)).isTrue();
        assertThat(InstructionEncoder.isValidImmediate9(-257)).isFalse();
        assertThat(InstructionEncoder.isValidImmediate9(256)).isFalse();
    }

    @Test
    @DisplayName("Should validate register numbers")
    void testValidateRegister() {
        assertThat(InstructionEncoder.isValidRegister(0)).isTrue();
        assertThat(InstructionEncoder.isValidRegister(31)).isTrue();
        assertThat(InstructionEncoder.isValidRegister(32)).isFalse();
        assertThat(InstructionEncoder.isValidRegister(-1)).isFalse();
    }

    // ==================== Byte Conversion Tests ====================

    @Test
    @DisplayName("Should convert instruction to bytes and back")
    void testByteConversion() {
        int original = 0x12345678;
        byte[] bytes = InstructionEncoder.toBytes(original);
        int result = InstructionEncoder.fromBytes(bytes);

        assertThat(result).isEqualTo(original);
    }

    @Test
    @DisplayName("Should use big-endian byte order")
    void testBigEndian() {
        int instruction = 0x12345678;
        byte[] bytes = InstructionEncoder.toBytes(instruction);

        assertThat(bytes[0]).isEqualTo((byte) 0x12); // Most significant byte
        assertThat(bytes[1]).isEqualTo((byte) 0x34);
        assertThat(bytes[2]).isEqualTo((byte) 0x56);
        assertThat(bytes[3]).isEqualTo((byte) 0x78); // Least significant byte
    }

    // ==================== Mnemonic Tests ====================

    @Test
    @DisplayName("Should return correct mnemonics")
    void testMnemonics() {
        assertThat(InstructionEncoder.getMnemonic(InstructionEncoder.OP_NOP)).isEqualTo("nop");
        assertThat(InstructionEncoder.getMnemonic(InstructionEncoder.OP_HALT)).isEqualTo("halt");
        assertThat(InstructionEncoder.getMnemonic(InstructionEncoder.OP_IADD)).isEqualTo("iadd");
        assertThat(InstructionEncoder.getMnemonic(InstructionEncoder.OP_ADD)).isEqualTo("add");
        assertThat(InstructionEncoder.getMnemonic(InstructionEncoder.OP_LI)).isEqualTo("li");
        assertThat(InstructionEncoder.getMnemonic(0xFF)).isEqualTo("unknown");
    }

    // ==================== Opcode Validation Tests ====================

    @Test
    @DisplayName("Should validate opcodes in valid range")
    void testValidateOpcodes() {
        assertThat(InstructionEncoder.isValidOpcode(0x00)).isTrue();
        assertThat(InstructionEncoder.isValidOpcode(0x6E)).isTrue();
        assertThat(InstructionEncoder.isValidOpcode(0x7F)).isFalse();
        assertThat(InstructionEncoder.isValidOpcode(0xFF)).isFalse();
    }

    // ==================== Combined Instruction Tests ====================

    @Test
    @DisplayName("Should encode and decode full instruction cycle")
    void testFullCycle() {
        // Encode
        int rd = 5;
        int rs1 = 10;
        int rs2 = 15;
        int instruction = InstructionEncoder.encodeRegRegReg(InstructionEncoder.OP_ADD, rd, rs1, rs2);

        // Decode
        assertThat(InstructionEncoder.decodeOpcode(instruction)).isEqualTo(InstructionEncoder.OP_ADD);
        assertThat(InstructionEncoder.decodeRd(instruction)).isEqualTo(rd);
        assertThat(InstructionEncoder.decodeRs1(instruction)).isEqualTo(rs1);
        assertThat(InstructionEncoder.decodeRs2(instruction)).isEqualTo(rs2);
    }

    @Test
    @DisplayName("Should encode simple addition program")
    void testEncodeSimpleProgram() {
        // 10 + 20 = 30
        int[] program = new int[]{
            InstructionEncoder.encodeImm(InstructionEncoder.OP_PUSH, 10),
            InstructionEncoder.encodeImm(InstructionEncoder.OP_PUSH, 20),
            InstructionEncoder.encode(InstructionEncoder.OP_IADD),
            InstructionEncoder.encode(InstructionEncoder.OP_PRINT),
            InstructionEncoder.encode(InstructionEncoder.OP_HALT)
        };

        assertThat(program).hasSize(5);
        assertThat(InstructionEncoder.decodeOpcode(program[0])).isEqualTo(InstructionEncoder.OP_PUSH);
        assertThat(InstructionEncoder.decodeOpcode(program[2])).isEqualTo(InstructionEncoder.OP_IADD);
        assertThat(InstructionEncoder.decodeOpcode(program[4])).isEqualTo(InstructionEncoder.OP_HALT);
    }

    @Test
    @DisplayName("Should encode register VM program")
    void testEncodeRegisterProgram() {
        // li r2, 10; li r3, 20; add r4, r2, r3; print r4; halt
        int[] program = new int[]{
            InstructionEncoder.encodeRegImm(InstructionEncoder.OP_LI, 2, 10),
            InstructionEncoder.encodeRegImm(InstructionEncoder.OP_LI, 3, 20),
            InstructionEncoder.encodeRegRegReg(InstructionEncoder.OP_ADD, 4, 2, 3),
            InstructionEncoder.encodeReg(InstructionEncoder.OP_PRINT, 4),
            InstructionEncoder.encode(InstructionEncoder.OP_HALT)
        };

        assertThat(program).hasSize(5);
        assertThat(InstructionEncoder.decodeOpcode(program[0])).isEqualTo(InstructionEncoder.OP_LI);
        assertThat(InstructionEncoder.decodeRd(program[0])).isEqualTo(2);
        assertThat(InstructionEncoder.decodeImm(program[0])).isEqualTo(10);
    }
}
