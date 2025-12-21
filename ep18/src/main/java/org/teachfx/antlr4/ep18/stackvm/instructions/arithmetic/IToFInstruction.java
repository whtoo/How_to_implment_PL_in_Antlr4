package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数转浮点指令
 * 从栈中弹出一个整数，转换为浮点数后压入栈
 * 使用ArithmeticExecutor进行高效的转换
 */
public class IToFInstruction extends BaseInstruction {
    public static final int OPCODE = 22;

    public IToFInstruction() {
        super("itof", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int a = context.pop();

        float result = ArithmeticExecutor.intToFloat(a);
        int resultBits = Float.floatToIntBits(result);

        if (context.isTraceEnabled()) {
            System.out.println("ITOF: " + a + " -> " + result);
        }

        context.push(resultBits);
    }
}