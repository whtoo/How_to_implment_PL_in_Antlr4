package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.teachfx.antlr4.ep18r.stackvm.registers.RegisterFile;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;

import static org.assertj.core.api.Assertions.*;

/**
 * 寄存器和内存管理单元测试
 * 专注于RegisterFile的核心功能和IMemoryManager接口测试
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RegisterFile和MemoryManager单元测试套件")
public class RegisterFileAndMemoryTest {

    private RegisterFile registerFile;
    private RegisterVMInterpreter interpreter;

    @BeforeEach
    void setUp() {
        // RegisterFile uses default constructor
        registerFile = new RegisterFile();

        // RegisterVMInterpreter requires VMConfig
        VMConfig config = new VMConfig.Builder().build();
        interpreter = new RegisterVMInterpreter(config);
    }

    @Nested
    @DisplayName("RegisterFile基础功能测试")
    @Order(1)
    class RegisterFileBasicTests {

        @Test
        @DisplayName("零寄存器应该始终返回0且只读")
        void testZeroRegister() {
            assertThatThrownBy(() -> registerFile.write(0, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot write to zero register");
            assertThat(registerFile.read(0)).isEqualTo(0);

            assertThatThrownBy(() -> registerFile.write(0, 9999))
                .isInstanceOf(IllegalArgumentException.class);
            assertThat(registerFile.read(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("普通寄存器应该正确读写")
        void testNormalRegistersReadWrite() {
            for (int reg = 1; reg <= 12; reg++) {
                int value = reg * 1000;
                registerFile.write(reg, value);
                assertThat(registerFile.read(reg)).isEqualTo(value);
            }
        }

        @Test
        @DisplayName("特殊寄存器SP应该正确读写")
        void testStackPointerRegister() {
            int value = 99999;
            registerFile.write(RegisterBytecodeDefinition.R13, value);
            assertThat(registerFile.read(RegisterBytecodeDefinition.R13)).isEqualTo(value);
        }

        @Test
        @DisplayName("特殊寄存器FP应该正确读写")
        void testFramePointerRegister() {
            int value = 88888;
            registerFile.write(RegisterBytecodeDefinition.R14, value);
            assertThat(registerFile.read(RegisterBytecodeDefinition.R14)).isEqualTo(value);
        }

        @Test
        @DisplayName("特殊寄存器LR应该正确读写")
        void testLinkRegister() {
            int value = 77777;
            registerFile.write(RegisterBytecodeDefinition.R15, value);
            assertThat(registerFile.read(RegisterBytecodeDefinition.R15)).isEqualTo(value);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 16, 100, -100})
        @DisplayName("访问无效寄存器应该抛出异常")
        void testInvalidRegisterAccess(int invalidReg) {
            assertThatThrownBy(() -> registerFile.read(invalidReg))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> registerFile.write(invalidReg, 0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("reset()应该重置所有寄存器为0")
        void testReset() {
            for (int reg = 1; reg <= 15; reg++) {
                registerFile.write(reg, reg * 100);
            }

            registerFile.reset();

            for (int reg = 0; reg <= 15; reg++) {
                assertThat(registerFile.read(reg)).isEqualTo(0);
            }
        }

        @Test
        @DisplayName("getStackPointer()应该返回R13的值")
        void testGetStackPointer() {
            int value = 12345;
            registerFile.write(RegisterBytecodeDefinition.R13, value);
            assertThat(registerFile.getStackPointer()).isEqualTo(value);
        }

        @Test
        @DisplayName("setStackPointer()应该更新R13的值")
        void testSetStackPointer() {
            int value = 54321;
            registerFile.setStackPointer(value);
            assertThat(registerFile.read(RegisterBytecodeDefinition.R13)).isEqualTo(value);
            assertThat(registerFile.getStackPointer()).isEqualTo(value);
        }

        @Test
        @DisplayName("getFramePointer()应该返回R14的值")
        void testGetFramePointer() {
            int value = 67890;
            registerFile.write(RegisterBytecodeDefinition.R14, value);
            assertThat(registerFile.getFramePointer()).isEqualTo(value);
        }

        @Test
        @DisplayName("setFramePointer()应该更新R14的值")
        void testSetFramePointer() {
            int value = 98765;
            registerFile.setFramePointer(value);
            assertThat(registerFile.read(RegisterBytecodeDefinition.R14)).isEqualTo(value);
            assertThat(registerFile.getFramePointer()).isEqualTo(value);
        }

        @Test
        @DisplayName("getLinkRegister()应该返回R15的值")
        void testGetLinkRegister() {
            int value = 10987;
            registerFile.write(RegisterBytecodeDefinition.R15, value);
            assertThat(registerFile.getLinkRegister()).isEqualTo(value);
        }

        @Test
        @DisplayName("setLinkRegister()应该更新R15的值")
        void testSetLinkRegister() {
            int value = 21098;
            registerFile.setLinkRegister(value);
            assertThat(registerFile.read(RegisterBytecodeDefinition.R15)).isEqualTo(value);
            assertThat(registerFile.getLinkRegister()).isEqualTo(value);
        }
    }

    @Nested
    @DisplayName("RegisterFile高级功能测试")
    @Order(2)
    class RegisterFileAdvancedTests {

        @Test
        @DisplayName("批量读取多个寄存器")
        void testReadMultipleRegisters() {
            int[] regNums = {1, 2, 3, 4, 5};
            int[] values = {100, 200, 300, 400, 500};

            for (int i = 0; i < regNums.length; i++) {
                registerFile.write(regNums[i], values[i]);
            }

            int[] readValues = registerFile.readMultiple(regNums);

            assertThat(readValues).isEqualTo(values);
        }

        @Test
        @DisplayName("批量写入多个寄存器")
        void testWriteMultipleRegisters() {
            int[] regNums = {6, 7, 8, 9, 10};
            int[] values = {600, 700, 800, 900, 1000};

            registerFile.writeMultiple(regNums, values);

            for (int i = 0; i < regNums.length; i++) {
                assertThat(registerFile.read(regNums[i])).isEqualTo(values[i]);
            }
        }

        @Test
        @DisplayName("批量读写应该正确处理")
        void testReadWriteMultipleOperations() {
            int[] regNums = {11, 12};
            int[] values = {1100, 1200};

            registerFile.writeMultiple(regNums, values);
            int[] readValues = registerFile.readMultiple(regNums);

            assertThat(readValues).isEqualTo(values);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 5, 10})
        @DisplayName("批量操作应该支持各种数量")
        void testBulkOperationsWithVariousCounts(int count) {
            int[] regNums = new int[count];
            int[] values = new int[count];

            for (int i = 0; i < count; i++) {
                regNums[i] = i + 1;
                values[i] = (i + 1) * 1000;
            }

            registerFile.writeMultiple(regNums, values);
            int[] readValues = registerFile.readMultiple(regNums);

            assertThat(readValues).isEqualTo(values);
        }

        @Test
        @DisplayName("snapshot()应该返回所有寄存器值的副本")
        void testSnapshot() {
            int[] expected = new int[16];
            for (int i = 0; i < 16; i++) {
                expected[i] = i * 1111;
            }
            // r0 is always 0, and r1-r15 can be written
            for (int i = 1; i < 16; i++) {
                registerFile.write(i, expected[i]);
            }

            int[] snapshot = registerFile.snapshot();

            assertThat(snapshot).isNotNull();
            assertThat(snapshot).hasSize(16);
            // r0 should be 0, others should match
            assertThat(snapshot[0]).isEqualTo(0);
            for (int i = 1; i < 16; i++) {
                assertThat(snapshot[i]).isEqualTo(expected[i]);
            }
        }
    }

    @Nested
    @DisplayName("IMemoryManager基础功能测试")
    @Order(3)
    class MemoryManagerBasicTests {

        @Test
        @DisplayName("readMemory应该正确读取内存值")
        void testReadMemory() {
            int address = 0;
            int value = 42;

            interpreter.writeMemory(address, value);
            assertThat(interpreter.readMemory(address)).isEqualTo(value);
        }

        @Test
        @DisplayName("writeMemory应该正确写入内存值")
        void testWriteMemory() {
            int address = 1;
            int value = 84;

            interpreter.writeMemory(address, value);
            assertThat(interpreter.readMemory(address)).isEqualTo(value);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100, 1000, 10000, 99999})
        @DisplayName("内存读写应该支持各种值")
        void testReadWriteWithVariousValues(int value) {
            int address = 2;

            interpreter.writeMemory(address, value);
            assertThat(interpreter.readMemory(address)).isEqualTo(value);
        }

        @Test
        @DisplayName("连续内存地址应该正确读写")
        void testConsecutiveMemoryAccess() {
            int baseAddress = 10;
            int[] values = {10, 20, 30, 40, 50};

            for (int i = 0; i < values.length; i++) {
                interpreter.writeMemory(baseAddress + i, values[i]);
            }

            for (int i = 0; i < values.length; i++) {
                assertThat(interpreter.readMemory(baseAddress + i)).isEqualTo(values[i]);
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -100, -10000})
        @DisplayName("内存读写应该支持负数")
        void testReadWriteWithNegativeValues(int value) {
            int address = 5;

            interpreter.writeMemory(address, value);
            assertThat(interpreter.readMemory(address)).isEqualTo(value);
        }
    }

    @Nested
    @DisplayName("RegisterVMInterpreter集成测试")
    @Order(4)
    class IntegrationTests {

        @Test
        @DisplayName("通过VMInterpreter设置寄存器值")
        void testSetRegisterViaInterpreter() {
            int reg = 1;
            int value = 999;

            interpreter.setRegister(reg, value);
            assertThat(interpreter.getRegister(reg)).isEqualTo(value);
        }

        @Test
        @DisplayName("通过VMInterpreter读取寄存器值")
        void testGetRegisterViaInterpreter() {
            int reg = 2;
            int value = 888;

            interpreter.setRegister(reg, value);
            assertThat(interpreter.getRegister(reg)).isEqualTo(value);
        }

        @Test
        @DisplayName("VMInterpreter的寄存器访问应该持久化")
        void testInterpreterRegisterPersistence() {
            int reg1 = 3;
            int reg2 = 4;
            int value1 = 777;
            int value2 = 666;

            interpreter.setRegister(reg1, value1);
            interpreter.setRegister(reg2, value2);

            assertThat(interpreter.getRegister(reg1)).isEqualTo(value1);
            assertThat(interpreter.getRegister(reg2)).isEqualTo(value2);
        }

        @Test
        @DisplayName("特殊寄存器通过VMInterpreter访问")
        void testSpecialRegistersViaInterpreter() {
            int spValue = 1111;
            int fpValue = 2222;
            int lrValue = 3333;

            interpreter.setRegister(RegisterBytecodeDefinition.R13, spValue);
            interpreter.setRegister(RegisterBytecodeDefinition.R14, fpValue);
            interpreter.setRegister(RegisterBytecodeDefinition.R15, lrValue);

            assertThat(interpreter.getRegister(RegisterBytecodeDefinition.R13)).isEqualTo(spValue);
            assertThat(interpreter.getRegister(RegisterBytecodeDefinition.R14)).isEqualTo(fpValue);
            assertThat(interpreter.getRegister(RegisterBytecodeDefinition.R15)).isEqualTo(lrValue);
        }
    }
}
