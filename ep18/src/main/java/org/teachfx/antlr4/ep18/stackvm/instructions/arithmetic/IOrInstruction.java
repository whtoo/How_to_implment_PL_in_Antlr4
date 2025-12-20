package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数按位或指令
 * 从栈中弹出两个整数，按位或后将结果压入栈
 */
public class IOrInstruction extends BaseInstruction {
    public static final int OPCODE = 14;

    public IOrInstruction() {
        super("ior", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();
        int result = ArithmeticExecutor.or(a, b);

        if (context.isTraceEnabled()) {
            System.out.println("IOR: " + a + " | " + b + " = " + result);
        }

        context.push(result);
    }
}
