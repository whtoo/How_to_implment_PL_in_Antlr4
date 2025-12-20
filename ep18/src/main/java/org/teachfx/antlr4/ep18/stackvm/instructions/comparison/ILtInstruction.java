package org.teachfx.antlr4.ep18.stackvm.instructions.comparison;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数小于指令
 * 比较两个整数，如果第一个小于第二个则压入1，否则压入0
 */
public class ILtInstruction extends BaseInstruction {
    public static final int OPCODE = 5;

    public ILtInstruction() {
        super("ilt", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();
        int result = (a < b) ? 1 : 0;

        if (context.isTraceEnabled()) {
            System.out.println("ILT: " + a + " < " + b + " = " + result);
        }

        context.push(result);
    }
}
