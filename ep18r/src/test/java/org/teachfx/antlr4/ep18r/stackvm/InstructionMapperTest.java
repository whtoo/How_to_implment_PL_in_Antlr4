package org.teachfx.antlr4.ep18r.stackvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.ExecutionContext;
import org.teachfx.antlr4.ep18r.stackvm.instructions.InstructionExecutor;
import org.teachfx.antlr4.ep18r.stackvm.instructions.InstructionMapper;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;

import static org.assertj.core.api.Assertions.*;

/**
 * InstructionMapper动态注册功能单元测试
 * 测试动态添加、替换、注销指令执行器的功能
 */
@DisplayName("InstructionMapper动态注册测试")
class InstructionMapperTest {

    @Test
    @DisplayName("应该正确注册新的指令执行器")
    void testRegisterNewExecutor() {
        InstructionMapper mapper = new InstructionMapper();
        int initialCount = mapper.getRegisteredOpcodeCount();

        InstructionExecutor customExecutor = new InstructionExecutor() {
            @Override
            public void execute(int operand, ExecutionContext context) {
                context.setRegister(0, 999);
            }
        };

        int customOpcode = 60;
        mapper.registerExecutor(customOpcode, customExecutor);

        assertThat(mapper.getRegisteredOpcodeCount()).isEqualTo(initialCount + 1);
        assertThat(mapper.isValidOpcode(customOpcode)).isTrue();
        assertThat(mapper.getExecutor(customOpcode)).isSameAs(customExecutor);
    }

    @Test
    @DisplayName("应该允许替换已存在的指令执行器")
    void testRegisterReplaceExistingExecutor() {
        InstructionMapper mapper = new InstructionMapper();

        int existingOpcode = RegisterBytecodeDefinition.INSTR_ADD;
        InstructionExecutor originalExecutor = mapper.getExecutor(existingOpcode);
        assertThat(originalExecutor).isNotNull();

        InstructionExecutor newExecutor = new InstructionExecutor() {
            @Override
            public void execute(int operand, ExecutionContext context) {
            }
        };

        mapper.registerExecutor(existingOpcode, newExecutor);

        assertThat(mapper.getExecutor(existingOpcode)).isSameAs(newExecutor);
        assertThat(mapper.getExecutor(existingOpcode)).isNotSameAs(originalExecutor);
    }

    @Test
    @DisplayName("应该拒绝null执行器")
    void testRejectNullExecutor() {
        InstructionMapper mapper = new InstructionMapper();

        assertThatThrownBy(() -> mapper.registerExecutor(10, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("应该拒绝超出范围的操作码（负数）")
    void testRejectNegativeOpcode() {
        InstructionMapper mapper = new InstructionMapper();

        InstructionExecutor executor = new InstructionExecutor() {
            @Override
            public void execute(int operand, ExecutionContext context) {
            }
        };

        assertThatThrownBy(() -> mapper.registerExecutor(-1, executor))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("应该拒绝超出范围的操作码（>63）")
    void testRejectTooLargeOpcode() {
        InstructionMapper mapper = new InstructionMapper();

        InstructionExecutor executor = new InstructionExecutor() {
            @Override
            public void execute(int operand, ExecutionContext context) {
            }
        };

        assertThatThrownBy(() -> mapper.registerExecutor(64, executor))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("应该接受边界操作码值（0和63）")
    void testAcceptBoundaryOpcodeValues() {
        InstructionMapper mapper = new InstructionMapper();

        InstructionExecutor executor = new InstructionExecutor() {
            @Override
            public void execute(int operand, ExecutionContext context) {
            }
        };

        assertThatCode(() -> mapper.registerExecutor(0, executor)).doesNotThrowAnyException();
        assertThatCode(() -> mapper.registerExecutor(63, executor)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应该正确注销指令执行器")
    void testUnregisterExecutor() {
        InstructionMapper mapper = new InstructionMapper();
        int initialCount = mapper.getRegisteredOpcodeCount();

        InstructionExecutor customExecutor = new InstructionExecutor() {
            @Override
            public void execute(int operand, ExecutionContext context) {
            }
        };
        int customOpcode = 60;
        mapper.registerExecutor(customOpcode, customExecutor);

        InstructionExecutor removed = mapper.unregisterExecutor(customOpcode);

        assertThat(removed).isSameAs(customExecutor);
        assertThat(mapper.isValidOpcode(customOpcode)).isFalse();
        assertThat(mapper.getExecutor(customOpcode)).isNull();
        assertThat(mapper.getRegisteredOpcodeCount()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("注销不存在的操作码应该返回null")
    void testUnregisterNonExistingOpcode() {
        InstructionMapper mapper = new InstructionMapper();

        InstructionExecutor removed = mapper.unregisterExecutor(99);

        assertThat(removed).isNull();
    }

    @Test
    @DisplayName("应该正确清空所有指令")
    void testClear() {
        InstructionMapper mapper = new InstructionMapper();

        mapper.clear();

        assertThat(mapper.getRegisteredOpcodeCount()).isEqualTo(0);
        assertThat(mapper.isValidOpcode(RegisterBytecodeDefinition.INSTR_ADD)).isFalse();
        assertThat(mapper.getExecutor(RegisterBytecodeDefinition.INSTR_ADD)).isNull();
    }

    @Test
    @DisplayName("应该正确重置为默认指令集")
    void testResetToDefaults() {
        InstructionMapper mapper = new InstructionMapper();
        int defaultCount = mapper.getRegisteredOpcodeCount();

        InstructionExecutor customExecutor = new InstructionExecutor() {
            @Override
            public void execute(int operand, ExecutionContext context) {
            }
        };
        mapper.registerExecutor(60, customExecutor);
        mapper.registerExecutor(61, customExecutor);

        int customCount = mapper.getRegisteredOpcodeCount();
        assertThat(customCount).isGreaterThan(defaultCount);

        mapper.resetToDefaults();

        assertThat(mapper.getRegisteredOpcodeCount()).isEqualTo(defaultCount);
        assertThat(mapper.isValidOpcode(RegisterBytecodeDefinition.INSTR_ADD)).isTrue();
        assertThat(mapper.isValidOpcode(60)).isFalse();
    }

    @Test
    @DisplayName("默认情况下应该注册所有42条指令")
    void testDefaultInstructionsRegistered() {
        InstructionMapper mapper = new InstructionMapper();

        assertThat(mapper.getRegisteredOpcodeCount()).isEqualTo(42);

        assertThat(mapper.isValidOpcode(RegisterBytecodeDefinition.INSTR_ADD)).isTrue();
        assertThat(mapper.isValidOpcode(RegisterBytecodeDefinition.INSTR_SUB)).isTrue();
        assertThat(mapper.isValidOpcode(RegisterBytecodeDefinition.INSTR_CALL)).isTrue();
        assertThat(mapper.isValidOpcode(RegisterBytecodeDefinition.INSTR_RET)).isTrue();
        assertThat(mapper.isValidOpcode(RegisterBytecodeDefinition.INSTR_HALT)).isTrue();
    }

    @Test
    @DisplayName("getRegisteredOpcodes应该返回排序后的操作码数组")
    void testGetRegisteredOpcodesSorted() {
        InstructionMapper mapper = new InstructionMapper();

        int[] opcodes = mapper.getRegisteredOpcodes();

        for (int i = 1; i < opcodes.length; i++) {
            assertThat(opcodes[i]).isGreaterThan(opcodes[i - 1]);
        }
    }
}
