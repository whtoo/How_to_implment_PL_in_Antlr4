package org.teachfx.antlr4.ep18.stackvm.instructions.comparison;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数小于等于比较指令
 */
public class ILeInstruction extends BaseInstruction {
    public static final int OPCODE = 6;

    public ILeInstruction() {
        super("ile", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();
        int result = (a <= b) ? 1 : 0;

        if (context.isTraceEnabled()) {
            System.out.println("ILE: " + a + " <= " + b + " = " + result);
        }

        context.push(result);
    }
}
