package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.BeforeEach;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition.*;
import static org.assertj.core.api.Assertions.*;

public abstract class RegisterVMTestBase {
    protected RegisterVMInterpreter vm;
    protected VMConfig testConfig;

    @BeforeEach
    void setUpVMTest() {
        vm = new RegisterVMInterpreter();
        testConfig = createTestConfig();
    }

    protected VMConfig createTestConfig() {
        return new VMConfig.Builder()
                .setHeapSize(1024 * 1024)
                .setStackSize(1024)
                .setMaxStackDepth(100)
                .setDebugMode(true)
                .setVerboseErrors(true)
                .setEnableBoundsCheck(true)
                .setEnableTypeCheck(true)
                .build();
    }

    protected void loadAndExecute(RegisterVMInterpreter vm, String program) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(vm, inputStream);
        assertThat(hasErrors).isFalse();
        vm.exec();
    }

    protected int getRegisterValue(int regNum) {
        return vm.getRegister(regNum);
    }

    protected void setRegisterValue(int regNum, int value) {
        vm.setRegister(regNum, value);
    }

    protected void assertRegisterEquals(int regNum, int expected) {
        int actual = getRegisterValue(regNum);
        assertThat(actual).as("Register r" + regNum + " should be " + expected).isEqualTo(expected);
    }

    protected void assertRegisters(int... regNums) {
        for (int regNum : regNums) {
            assertRegisterEquals(regNum, regNums[regNum]);
        }
    }

    protected int encodeRType(int opcode, int rd, int rs1, int rs2) {
        if (opcode < 0 || opcode > 0x3F) {
            throw new IllegalArgumentException("Opcode must be 6-bit (0-63): " + opcode);
        }
        if (rd < 0 || rd > 0x1F || rs1 < 0 || rs1 > 0x1F || rs2 < 0 || rs2 > 0x1F) {
            throw new IllegalArgumentException("Register numbers must be 5-bit (0-31)");
        }
        return (opcode << 26) | (rd << 21) | (rs1 << 16) | (rs2 << 11);
    }

    protected int encodeIType(int opcode, int rd, int rs1, int imm) {
        if (opcode < 0 || opcode > 0x3F) {
            throw new IllegalArgumentException("Opcode must be 6-bit (0-63): " + opcode);
        }
        if (rd < 0 || rd > 0x1F || rs1 < 0 || rs1 > 0x1F) {
            throw new IllegalArgumentException("Register numbers must be 5-bit (0-31)");
        }
        imm = imm & 0xFFFF;
        return (opcode << 26) | (rd << 21) | (rs1 << 16) | imm;
    }

    protected int encodeJType(int opcode, int address) {
        if (opcode < 0 || opcode > 0x3F) {
            throw new IllegalArgumentException("Opcode must be 6-bit (0-63): " + opcode);
        }
        address = address & 0x3FFFFFF;
        return (opcode << 26) | address;
    }

    protected int encodeLI(int rd, int imm) {
        return encodeIType(
            org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition.INSTR_LI,
            rd, 0, imm
        );
    }

    protected int encodeADD(int rd, int rs1, int rs2) {
        return encodeRType(
            org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition.INSTR_ADD,
            rd, rs1, rs2
        );
    }
}
