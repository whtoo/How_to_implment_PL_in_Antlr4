package org.teachfx.antlr4.ep18.stackvm.instructions.controlflow;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 弹出指令
 * 从栈中弹出一个值并丢弃
 */
public class PopInstruction extends BaseInstruction {
    public static final int OPCODE = 41;

    public PopInstruction() {
        super("pop", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int value = context.pop();

        if (context.isTraceEnabled()) {
            System.out.println("POP: discarded " + value);
        }
    }
}
