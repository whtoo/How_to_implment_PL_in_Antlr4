package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.VMDivisionByZeroException;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 浮点除法指令
 * 从栈中弹出两个浮点数，相除后将结果压入栈
 * 使用ArithmeticExecutor进行高效的浮点运算，包含除零检查
 */
public class FDivInstruction extends BaseInstruction {
    public static final int OPCODE = 19;

    public FDivInstruction() {
        super("fdiv", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int bBits = context.pop();
        int aBits = context.pop();

        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);

        try {
            // ArithmeticExecutor.divide()会抛出ArithmeticException（除以零）
            float result = ArithmeticExecutor.divide(a, b);
            int resultBits = Float.floatToIntBits(result);

            if (context.isTraceEnabled()) {
                System.out.println("FDIV: " + a + " / " + b + " = " + result);
            }

            context.push(resultBits);
        } catch (ArithmeticException e) {
            // 将ArithmeticException转换为VMDivisionByZeroException
            throw new VMDivisionByZeroException(context.getProgramCounter(), "FDIV");
        }
    }
}