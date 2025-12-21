package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 浮点乘法指令
 * 从栈中弹出两个浮点数，相乘后将结果压入栈
 * 使用ArithmeticExecutor进行高效的浮点运算
 */
public class FMulInstruction extends BaseInstruction {
    public static final int OPCODE = 18;

    public FMulInstruction() {
        super("fmul", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int bBits = context.pop();
        int aBits = context.pop();

        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);

        float result = ArithmeticExecutor.multiply(a, b);
        int resultBits = Float.floatToIntBits(result);

        if (context.isTraceEnabled()) {
            System.out.println("FMUL: " + a + " * " + b + " = " + result);
        }

        context.push(resultBits);
    }
}