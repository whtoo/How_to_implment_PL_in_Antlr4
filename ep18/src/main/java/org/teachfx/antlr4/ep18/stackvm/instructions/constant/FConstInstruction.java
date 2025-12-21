package org.teachfx.antlr4.ep18.stackvm.instructions.constant;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 浮点常量指令
 * 将浮点常量的位表示压入栈
 * 操作数是浮点数的位表示（int），通过Float.intBitsToFloat()转换为浮点数
 */
public class FConstInstruction extends BaseInstruction {
    public static final int OPCODE = 30;

    public FConstInstruction() {
        super("fconst", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        // 操作数已经是浮点数的位表示，直接压入栈
        int value = operand;

        if (context.isTraceEnabled()) {
            float floatValue = Float.intBitsToFloat(value);
            System.out.println("FCONST: pushing " + floatValue + " (bits: 0x" + Integer.toHexString(value) + ")");
        }

        context.push(value);
    }
}