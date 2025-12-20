package org.teachfx.antlr4.ep18.stackvm.instructions.comparison;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数相等比较指令
 */
public class IEqInstruction extends BaseInstruction {
    public static final int OPCODE = 9;

    public IEqInstruction() {
        super("ieq", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();
        int result = (a == b) ? 1 : 0;

        if (context.isTraceEnabled()) {
            System.out.println("IEQ: " + a + " == " + b + " = " + result);
        }

        context.push(result);
    }
}
