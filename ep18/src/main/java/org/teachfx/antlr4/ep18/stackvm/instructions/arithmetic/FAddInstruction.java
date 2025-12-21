package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 浮点加法指令
 * 从栈中弹出两个浮点数，相加后将结果压入栈
 * 使用ArithmeticExecutor进行高效的浮点运算
 */
public class FAddInstruction extends BaseInstruction {
    public static final int OPCODE = 16;

    public FAddInstruction() {
        super("fadd", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        // 浮点数以整数形式存储（Float.floatToIntBits）
        int bBits = context.pop();
        int aBits = context.pop();

        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);

        // 使用ArithmeticExecutor进行浮点加法运算
        float result = ArithmeticExecutor.add(a, b);
        int resultBits = Float.floatToIntBits(result);

        if (context.isTraceEnabled()) {
            System.out.println("FADD: " + a + " + " + b + " = " + result);
        }

        context.push(resultBits);
    }
}