package org.teachfx.antlr4.ep18.stackvm.instructions.comparison;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 浮点相等指令
 * 比较两个浮点数是否相等，相等则压入1，否则压入0
 * 使用Float.compare()进行健壮的浮点比较（处理NaN）
 */
public class FEqInstruction extends BaseInstruction {
    public static final int OPCODE = 21;

    public FEqInstruction() {
        super("feq", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int bBits = context.pop();
        int aBits = context.pop();

        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);
        int result = (Float.compare(a, b) == 0) ? 1 : 0;

        if (context.isTraceEnabled()) {
            System.out.println("FEQ: " + a + " == " + b + " = " + result);
        }

        context.push(result);
    }
}