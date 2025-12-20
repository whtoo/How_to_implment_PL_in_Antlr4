package org.teachfx.antlr4.ep18.stackvm.instructions.comparison;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数大于比较指令
 */
public class IGtInstruction extends BaseInstruction {
    public static final int OPCODE = 7;

    public IGtInstruction() {
        super("igt", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();
        int result = (a > b) ? 1 : 0;

        if (context.isTraceEnabled()) {
            System.out.println("IGT: " + a + " > " + b + " = " + result);
        }

        context.push(result);
    }
}
