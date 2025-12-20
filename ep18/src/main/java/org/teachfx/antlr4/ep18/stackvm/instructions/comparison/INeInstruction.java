package org.teachfx.antlr4.ep18.stackvm.instructions.comparison;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数不等比较指令
 */
public class INeInstruction extends BaseInstruction {
    public static final int OPCODE = 10;

    public INeInstruction() {
        super("ine", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();
        int result = (a != b) ? 1 : 0;

        if (context.isTraceEnabled()) {
            System.out.println("INE: " + a + " != " + b + " = " + result);
        }

        context.push(result);
    }
}
