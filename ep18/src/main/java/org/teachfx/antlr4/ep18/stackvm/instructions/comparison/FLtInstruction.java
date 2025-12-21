package org.teachfx.antlr4.ep18.stackvm.instructions.comparison;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 浮点小于指令
 * 比较两个浮点数，如果第一个小于第二个则压入1，否则压入0
 */
public class FLtInstruction extends BaseInstruction {
    public static final int OPCODE = 20;

    public FLtInstruction() {
        super("flt", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int bBits = context.pop();
        int aBits = context.pop();

        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);
        int result = (a < b) ? 1 : 0;

        if (context.isTraceEnabled()) {
            System.out.println("FLT: " + a + " < " + b + " = " + result);
        }

        context.push(result);
    }
}