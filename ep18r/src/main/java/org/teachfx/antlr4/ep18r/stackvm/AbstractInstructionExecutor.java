package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 指令执行器抽象基类
 * 使用模板方法模式统一指令执行逻辑，减少代码重复
 *
 * 执行流程：
 * 1. 提取操作数（寄存器编号、立即数）
 * 2. 读取源操作数的值
 * 3. 子类执行具体计算（由子类实现）
 * 4. 将结果写入目标寄存器/内存
 */
public abstract class AbstractInstructionExecutor implements InstructionExecutor {

    /**
     * 执行R类型指令模板（rd = rs1 op rs2）
     *
     * @param operand 指令操作数
     * @param context 执行上下文
     * @param binaryOp 二元运算操作
     */
    protected void executeRType(int operand, ExecutionContext context, BinaryOperator binaryOp) throws Exception {
        int rd = RegisterOperandExtractor.extractRd(operand);
        int rs1 = RegisterOperandExtractor.extractRs1(operand);
        int rs2 = RegisterOperandExtractor.extractRs2(operand);

        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);

        int result = binaryOp.apply(val1, val2);
        context.setRegister(rd, result);
    }

    /**
     * 执行I类型指令模板（rd = op(rs1, imm) 或 rd = op(imm)）
     *
     * @param operand 指令操作数
     * @param context 执行上下文
     * @param unaryOp 一元运算操作（立即数运算）
     */
    protected void executeITypeImmediate(int operand, ExecutionContext context, UnaryOperator unaryOp) {
        int rd = RegisterOperandExtractor.extractRd(operand);
        int imm = RegisterOperandExtractor.extractImm16(operand);

        int result = unaryOp.apply(imm);
        context.setRegister(rd, result);
    }

    /**
     * 执行I类型指令模板（rd = rs1 op imm）
     *
     * @param operand 指令操作数
     * @param context 执行上下文
     * @param binaryOp 二元运算操作
     */
    protected void executeITypeBinary(int operand, ExecutionContext context, BinaryOperator binaryOp) throws Exception {
        int rd = RegisterOperandExtractor.extractRd(operand);
        int rs1 = RegisterOperandExtractor.extractRs1(operand);
        int imm = RegisterOperandExtractor.extractImm16(operand);

        int val1 = context.getRegister(rs1);
        int val2 = imm;

        int result = binaryOp.apply(val1, val2);
        context.setRegister(rd, result);
    }

    /**
     * 执行I类型指令模板（rd = memory[rs1 + imm]）
     *
     * @param operand 指令操作数
     * @param context 执行上下文
     */
    protected void executeLoad(int operand, ExecutionContext context) {
        int rd = RegisterOperandExtractor.extractRd(operand);
        int rs1 = RegisterOperandExtractor.extractRs1(operand);
        int offset = RegisterOperandExtractor.extractImm16(operand);

        int baseAddr = context.getRegister(rs1);
        int address = baseAddr + offset;

        int value = context.readMemory(address);
        context.setRegister(rd, value);
    }

    /**
     * 执行I类型指令模板（memory[rs1 + imm] = rs）
     *
     * @param operand 指令操作数
     * @param context 执行上下文
     */
    protected void executeStore(int operand, ExecutionContext context) {
        int rs = RegisterOperandExtractor.extractRd(operand);
        int rs1 = RegisterOperandExtractor.extractRs1(operand);
        int offset = RegisterOperandExtractor.extractImm16(operand);

        int baseAddr = context.getRegister(rs1);
        int address = baseAddr + offset;

        int value = context.getRegister(rs);
        context.writeMemory(address, value);
    }

    /**
     * 二元运算函数式接口
     */
    @FunctionalInterface
    public interface BinaryOperator {
        int apply(int a, int b) throws Exception;
    }

    /**
     * 一元运算函数式接口
     */
    @FunctionalInterface
    public interface UnaryOperator {
        int apply(int value);
    }

    /**
     * 比较运算函数式接口（返回0或1）
     */
    @FunctionalInterface
    public interface ComparisonOperator {
        int compare(int a, int b);
    }

    /**
     * 执行比较指令模板（rd = (rs1 cmp rs2) ? 1 : 0）
     *
     * @param operand 指令操作数
     * @param context 执行上下文
     * @param comparisonOp 比较运算操作
     */
    protected void executeComparison(int operand, ExecutionContext context, ComparisonOperator comparisonOp) {
        int rd = RegisterOperandExtractor.extractRd(operand);
        int rs1 = RegisterOperandExtractor.extractRs1(operand);
        int rs2 = RegisterOperandExtractor.extractRs2(operand);

        int val1 = context.getRegister(rs1);
        int val2 = context.getRegister(rs2);

        int result = comparisonOp.compare(val1, val2);
        context.setRegister(rd, result);
    }
}
