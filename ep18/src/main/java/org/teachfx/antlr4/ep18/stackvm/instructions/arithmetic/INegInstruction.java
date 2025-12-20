package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数取负指令
 * 从栈中弹出一个整数，取负后将结果压入栈
 */
public class INegInstruction extends BaseInstruction {
    public static final int OPCODE = 11;

    public INegInstruction() {
        super("ineg", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int a = context.pop();
        int result = ArithmeticExecutor.negate(context, a);

        if (context.isTraceEnabled()) {
            System.out.println("INEG: -" + a + " = " + result);
        }

        context.push(result);
    }
}
