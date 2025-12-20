package org.teachfx.antlr4.ep18.stackvm.instructions.constant;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数常量指令
 * 将立即数压入栈
 */
public class IConstInstruction extends BaseInstruction {
    public static final int OPCODE = 29;

    public IConstInstruction() {
        super("iconst", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int value = operand;

        if (context.isTraceEnabled()) {
            System.out.println("ICONST: pushing " + value);
        }

        context.push(value);
    }
}
