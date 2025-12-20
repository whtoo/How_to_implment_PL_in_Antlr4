package org.teachfx.antlr4.ep18.stackvm.instructions.comparison;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数大于等于比较指令
 */
public class IGeInstruction extends BaseInstruction {
    public static final int OPCODE = 8;

    public IGeInstruction() {
        super("ige", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();
        int result = (a >= b) ? 1 : 0;

        if (context.isTraceEnabled()) {
            System.out.println("IGE: " + a + " >= " + b + " = " + result);
        }

        context.push(result);
    }
}
