package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数按位非指令
 * 从栈中弹出一个整数，按位取反后将结果压入栈
 */
public class INotInstruction extends BaseInstruction {
    public static final int OPCODE = 12;

    public INotInstruction() {
        super("inot", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int a = context.pop();
        int result = ArithmeticExecutor.not(a);

        if (context.isTraceEnabled()) {
            System.out.println("INOT: ~" + a + " = " + result);
        }

        context.push(result);
    }
}
