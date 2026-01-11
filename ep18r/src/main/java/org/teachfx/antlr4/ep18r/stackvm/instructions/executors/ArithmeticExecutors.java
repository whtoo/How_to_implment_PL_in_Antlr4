package org.teachfx.antlr4.ep18r.stackvm.instructions.executors;

import org.teachfx.antlr4.ep18r.stackvm.instructions.InstructionExecutor;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;

import java.util.Map;
import java.util.function.BinaryOperator;

/**
 * 算术指令执行器集合
 * 将重复的算术运算逻辑提取为策略，消除代码重复
 */
public class ArithmeticExecutors {

    // ==================== 算术运算映射 ====================

    /**
     * 二元算术运算接口
     */
    @FunctionalInterface
    interface BinaryArithmeticOperation {
        int apply(int a, int b);
    }

    // 算术运算映射：操作码 -> 运算逻辑
    // 注意：Map.of要求所有值类型一致，所以我们使用具体的lambda表达式而不是映射
    // 这个映射将在RegisterVMInterpreter中用于指令分发

    // 逻辑运算映射：操作码 -> 运算逻辑
    // 注意：同样的类型一致性问题

    // 比较运算映射：操作码 -> 运算逻辑
    // 注意：同样的类型一致性问题

    private static int getCurrentPC() {
        // 简化实现，实际应该从上下文获取
        return -1;
    }

    // ==================== 具体指令执行器 ====================

    /**
     * 加法指令执行器
     */
    public static final InstructionExecutor ADD = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = val1 + val2;
        context.setRegister(rd, result);
    };

    /**
     * 减法指令执行器
     */
    public static final InstructionExecutor SUB = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = val1 - val2;
        context.setRegister(rd, result);
    };

    /**
     * 乘法指令执行器
     */
    public static final InstructionExecutor MUL = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = val1 * val2;
        context.setRegister(rd, result);
    };

    /**
     * 除法指令执行器
     */
    public static final InstructionExecutor DIV = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);

        if (val2 == 0) {
            throw new ArithmeticException("Division by zero at PC=" + context.getProgramCounter());
        }

        int result = val1 / val2;
        context.setRegister(rd, result);
    };

    /**
     * 与指令执行器
     */
    public static final InstructionExecutor AND = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = val1 & val2;
        context.setRegister(rd, result);
    };

    /**
     * 或指令执行器
     */
    public static final InstructionExecutor OR = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = val1 | val2;
        context.setRegister(rd, result);
    };

    /**
     * 异或指令执行器
     */
    public static final InstructionExecutor XOR = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = val1 ^ val2;
        context.setRegister(rd, result);
    };

    /**
     * 小于比较指令执行器
     */
    public static final InstructionExecutor SLT = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = (val1 < val2) ? 1 : 0;
        context.setRegister(rd, result);
    };

    /**
     * 小于等于比较指令执行器
     */
    public static final InstructionExecutor SLE = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = (val1 <= val2) ? 1 : 0;
        context.setRegister(rd, result);
    };

    /**
     * 大于比较指令执行器
     */
    public static final InstructionExecutor SGT = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = (val1 > val2) ? 1 : 0;
        context.setRegister(rd, result);
    };

    /**
     * 大于等于比较指令执行器
     */
    public static final InstructionExecutor SGE = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = (val1 >= val2) ? 1 : 0;
        context.setRegister(rd, result);
    };

    /**
     * 等于比较指令执行器
     */
    public static final InstructionExecutor SEQ = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = (val1 == val2) ? 1 : 0;
        context.setRegister(rd, result);
    };

    /**
     * 不等于比较指令执行器
     */
    public static final InstructionExecutor SNE = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);
        int result = (val1 != val2) ? 1 : 0;
        context.setRegister(rd, result);
    };
}
