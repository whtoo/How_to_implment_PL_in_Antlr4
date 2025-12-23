package org.teachfx.antlr4.ep18.stackvm.codegen;

import org.teachfx.antlr4.ep18.stackvm.BytecodeDefinition;

/**
 * 字节码定义接口
 * 为EP21提供统一的指令定义查询接口
 * 抽象了EP18的BytecodeDefinition，便于代码生成器访问
 */
public interface BytecodeDefinitionInterface {

    /**
     * 获取指令名称
     * @param opcode 指令操作码
     * @return 指令名称
     */
    String getInstructionName(int opcode);

    /**
     * 根据名称获取操作码
     * @param name 指令名称
     * @return 操作码，如果未找到返回-1
     */
    int getOpcode(String name);

    /**
     * 获取指令参数数量
     * @param opcode 指令操作码
     * @return 参数数量
     */
    int getOperandCount(int opcode);

    /**
     * 获取指令参数类型
     * @param opcode 指令操作码
     * @param operandIndex 参数索引
     * @return 参数类型（INT, FUNC, REG, POOL等）
     */
    int getOperandType(int opcode, int operandIndex);

    /**
     * 检查指令是否存在
     * @param opcode 指令操作码
     * @return 是否存在
     */
    boolean isValidOpcode(int opcode);

    /**
     * 获取最大操作码值
     * @return 最大操作码
     */
    int getMaxOpcode();

    /**
     * 获取所有指令名称
     * @return 指令名称数组
     */
    String[] getAllInstructionNames();

    /**
     * EP18字节码定义默认实现
     */
    class DefaultBytecodeDefinition implements BytecodeDefinitionInterface {

        private final BytecodeDefinition.Instruction[] instructions;

        public DefaultBytecodeDefinition() {
            this.instructions = BytecodeDefinition.instructions;
        }

        @Override
        public String getInstructionName(int opcode) {
            if (opcode <= 0 || opcode >= instructions.length) {
                return null;
            }
            return instructions[opcode].name;
        }

        @Override
        public int getOpcode(String name) {
            for (int i = 1; i < instructions.length; i++) {
                if (instructions[i].name.equalsIgnoreCase(name)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getOperandCount(int opcode) {
            if (opcode <= 0 || opcode >= instructions.length) {
                return 0;
            }
            BytecodeDefinition.Instruction instr = instructions[opcode];
            int count = 0;
            for (int i = 0; i < 3; i++) {
                if (instr.type[i] != 0) {
                    count++;
                } else {
                    break;
                }
            }
            return count;
        }

        @Override
        public int getOperandType(int opcode, int operandIndex) {
            if (opcode <= 0 || opcode >= instructions.length) {
                return 0;
            }
            BytecodeDefinition.Instruction instr = instructions[opcode];
            if (operandIndex < 0 || operandIndex >= 3) {
                return 0;
            }
            return instr.type[operandIndex];
        }

        @Override
        public boolean isValidOpcode(int opcode) {
            return opcode > 0 && opcode < instructions.length;
        }

        @Override
        public int getMaxOpcode() {
            return instructions.length - 1;
        }

        @Override
        public String[] getAllInstructionNames() {
            String[] names = new String[instructions.length - 1];
            for (int i = 1; i < instructions.length; i++) {
                names[i - 1] = instructions[i].name;
            }
            return names;
        }
    }
}
