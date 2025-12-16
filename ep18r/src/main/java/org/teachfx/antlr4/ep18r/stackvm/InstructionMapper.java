package org.teachfx.antlr4.ep18r.stackvm;

import java.util.Map;
import java.util.HashMap;

/**
 * 指令映射器
 * 将操作码映射到对应的指令执行器
 * 采用策略模式，消除代码重复
 */
public class InstructionMapper {
    private final Map<Integer, InstructionExecutor> executors;

    public InstructionMapper() {
        this.executors = new HashMap<>();
        initializeExecutors();
    }

    /**
     * 初始化所有指令执行器映射
     */
    private void initializeExecutors() {
        // ==================== 算术运算指令 ====================
        executors.put((int) RegisterBytecodeDefinition.INSTR_ADD, ArithmeticExecutors.ADD);
        executors.put((int) RegisterBytecodeDefinition.INSTR_SUB, ArithmeticExecutors.SUB);
        executors.put((int) RegisterBytecodeDefinition.INSTR_MUL, ArithmeticExecutors.MUL);
        executors.put((int) RegisterBytecodeDefinition.INSTR_DIV, ArithmeticExecutors.DIV);

        // ==================== 逻辑运算指令 ====================
        executors.put((int) RegisterBytecodeDefinition.INSTR_AND, ArithmeticExecutors.AND);
        executors.put((int) RegisterBytecodeDefinition.INSTR_OR, ArithmeticExecutors.OR);
        executors.put((int) RegisterBytecodeDefinition.INSTR_XOR, ArithmeticExecutors.XOR);

        // ==================== 比较运算指令 ====================
        executors.put((int) RegisterBytecodeDefinition.INSTR_SLT, ArithmeticExecutors.SLT);
        executors.put((int) RegisterBytecodeDefinition.INSTR_SLE, ArithmeticExecutors.SLE);
        executors.put((int) RegisterBytecodeDefinition.INSTR_SGT, ArithmeticExecutors.SGT);
        executors.put((int) RegisterBytecodeDefinition.INSTR_SGE, ArithmeticExecutors.SGE);
        executors.put((int) RegisterBytecodeDefinition.INSTR_SEQ, ArithmeticExecutors.SEQ);
        executors.put((int) RegisterBytecodeDefinition.INSTR_SNE, ArithmeticExecutors.SNE);

        // ==================== 单目运算指令 ====================
        executors.put((int) RegisterBytecodeDefinition.INSTR_NEG, ComparisonExecutors.NEG);
        executors.put((int) RegisterBytecodeDefinition.INSTR_NOT, ComparisonExecutors.NOT);

        // ==================== 浮点运算指令 ====================
        executors.put((int) RegisterBytecodeDefinition.INSTR_FADD, ComparisonExecutors.FADD);
        executors.put((int) RegisterBytecodeDefinition.INSTR_FSUB, ComparisonExecutors.FSUB);
        executors.put((int) RegisterBytecodeDefinition.INSTR_FMUL, ComparisonExecutors.FMUL);
        executors.put((int) RegisterBytecodeDefinition.INSTR_FDIV, ComparisonExecutors.FDIV);
        executors.put((int) RegisterBytecodeDefinition.INSTR_FLT, ComparisonExecutors.FLT);
        executors.put((int) RegisterBytecodeDefinition.INSTR_FEQ, ComparisonExecutors.FEQ);

        // ==================== 类型转换指令 ====================
        executors.put((int) RegisterBytecodeDefinition.INSTR_ITOF, ComparisonExecutors.ITOF);

        // ==================== 控制流指令 ====================
        executors.put((int) RegisterBytecodeDefinition.INSTR_CALL, ControlFlowExecutors.CALL);
        executors.put((int) RegisterBytecodeDefinition.INSTR_RET, ControlFlowExecutors.RET);
        executors.put((int) RegisterBytecodeDefinition.INSTR_J, ControlFlowExecutors.J);
        executors.put((int) RegisterBytecodeDefinition.INSTR_JT, ControlFlowExecutors.JT);
        executors.put((int) RegisterBytecodeDefinition.INSTR_JF, ControlFlowExecutors.JF);

        // ==================== 常量加载指令 ====================
        executors.put((int) RegisterBytecodeDefinition.INSTR_LI, MemoryExecutors.LI);
        executors.put((int) RegisterBytecodeDefinition.INSTR_LC, MemoryExecutors.LC);
        executors.put((int) RegisterBytecodeDefinition.INSTR_LF, MemoryExecutors.LF);
        executors.put((int) RegisterBytecodeDefinition.INSTR_LS, MemoryExecutors.LS);

        // ==================== 内存访问指令 ====================
        executors.put((int) RegisterBytecodeDefinition.INSTR_LW, MemoryExecutors.LW);
        executors.put((int) RegisterBytecodeDefinition.INSTR_SW, MemoryExecutors.SW);
        executors.put((int) RegisterBytecodeDefinition.INSTR_LW_G, MemoryExecutors.LW_G);
        executors.put((int) RegisterBytecodeDefinition.INSTR_SW_G, MemoryExecutors.SW_G);
        executors.put((int) RegisterBytecodeDefinition.INSTR_LW_F, MemoryExecutors.LW_F);
        executors.put((int) RegisterBytecodeDefinition.INSTR_SW_F, MemoryExecutors.SW_F);

        // ==================== 其他指令 ====================
        executors.put((int) RegisterBytecodeDefinition.INSTR_PRINT, MemoryExecutors.PRINT);
        executors.put((int) RegisterBytecodeDefinition.INSTR_STRUCT, MemoryExecutors.STRUCT);
        executors.put((int) RegisterBytecodeDefinition.INSTR_NULL, MemoryExecutors.NULL);
        executors.put((int) RegisterBytecodeDefinition.INSTR_MOV, MemoryExecutors.MOV);
        executors.put((int) RegisterBytecodeDefinition.INSTR_HALT, MemoryExecutors.HALT);
    }

    /**
     * 获取指令执行器
     * @param opcode 操作码
     * @return 指令执行器，如果不存在返回null
     */
    public InstructionExecutor getExecutor(int opcode) {
        return executors.get(opcode);
    }

    /**
     * 检查操作码是否有效
     * @param opcode 操作码
     * @return 如果有效返回true，否则返回false
     */
    public boolean isValidOpcode(int opcode) {
        return executors.containsKey(opcode);
    }

    /**
     * 获取已注册的操作码数量
     * @return 操作码数量
     */
    public int getRegisteredOpcodeCount() {
        return executors.size();
    }

    /**
     * 获取所有已注册的操作码
     * @return 操作码数组
     */
    public int[] getRegisteredOpcodes() {
        return executors.keySet().stream()
            .mapToInt(Integer::intValue)
            .sorted()
            .toArray();
    }
}
