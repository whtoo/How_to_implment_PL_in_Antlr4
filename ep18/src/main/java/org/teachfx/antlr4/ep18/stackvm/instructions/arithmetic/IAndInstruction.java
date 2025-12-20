package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数按位与指令
 * 从栈中弹出两个整数，按位与后将结果压入栈
 */
public class IAndInstruction extends BaseInstruction {
    public static final int OPCODE = 13;

    public IAndInstruction() {
        super("iand", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();
        int result = ArithmeticExecutor.and(a, b);

        if (context.isTraceEnabled()) {
            System.out.println("IAND: " + a + " & " + b + " = " + result);
        }

        context.push(result);
    }
}
