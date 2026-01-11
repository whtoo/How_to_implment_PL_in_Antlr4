package org.teachfx.antlr4.ep18r.stackvm.instructions;

/**
 * 比较指令执行器集合
 * 处理所有比较和条件设置指令
 */
public class ComparisonExecutors {

    // ==================== 单目运算指令 ====================

    /**
     * 取负指令执行器
     */
    public static final InstructionExecutor NEG = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int val = context.getRegister(rs1);
        context.setRegister(rd, -val);
    };

    /**
     * 逻辑取反指令执行器
     */
    public static final InstructionExecutor NOT = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int val = context.getRegister(rs1);
        context.setRegister(rd, val == 0 ? 1 : 0);
    };

    // ==================== 浮点运算指令 ====================

    /**
     * 浮点加法指令执行器
     */
    public static final InstructionExecutor FADD = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        float val1 = Float.intBitsToFloat(context.getRegister(rs1));
        float val2 = Float.intBitsToFloat(context.getRegister(rs2));
        int result = Float.floatToIntBits(val1 + val2);
        context.setRegister(rd, result);
    };

    /**
     * 浮点减法指令执行器
     */
    public static final InstructionExecutor FSUB = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        float val1 = Float.intBitsToFloat(context.getRegister(rs1));
        float val2 = Float.intBitsToFloat(context.getRegister(rs2));
        int result = Float.floatToIntBits(val1 - val2);
        context.setRegister(rd, result);
    };

    /**
     * 浮点乘法指令执行器
     */
    public static final InstructionExecutor FMUL = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        float val1 = Float.intBitsToFloat(context.getRegister(rs1));
        float val2 = Float.intBitsToFloat(context.getRegister(rs2));
        int result = Float.floatToIntBits(val1 * val2);
        context.setRegister(rd, result);
    };

    /**
     * 浮点除法指令执行器
     */
    public static final InstructionExecutor FDIV = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        float val1 = Float.intBitsToFloat(context.getRegister(rs1));
        float val2 = Float.intBitsToFloat(context.getRegister(rs2));

        if (val2 == 0.0f) {
            throw new ArithmeticException("Floating point division by zero at PC=" + context.getProgramCounter());
        }

        int result = Float.floatToIntBits(val1 / val2);
        context.setRegister(rd, result);
    };

    /**
     * 浮点小于比较指令执行器
     */
    public static final InstructionExecutor FLT = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        float val1 = Float.intBitsToFloat(context.getRegister(rs1));
        float val2 = Float.intBitsToFloat(context.getRegister(rs2));
        int result = (val1 < val2) ? 1 : 0;
        context.setRegister(rd, result);
    };

    /**
     * 浮点等于比较指令执行器
     */
    public static final InstructionExecutor FEQ = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int rs2 = context.extractRs2(operand);
        float val1 = Float.intBitsToFloat(context.getRegister(rs1));
        float val2 = Float.intBitsToFloat(context.getRegister(rs2));
        int result = (val1 == val2) ? 1 : 0;
        context.setRegister(rd, result);
    };

    // ==================== 类型转换指令 ====================

    /**
     * 整数转浮点指令执行器
     */
    public static final InstructionExecutor ITOF = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int val = context.getRegister(rs1);
        float fval = (float) val;
        int result = Float.floatToIntBits(fval);
        context.setRegister(rd, result);
    };
}
