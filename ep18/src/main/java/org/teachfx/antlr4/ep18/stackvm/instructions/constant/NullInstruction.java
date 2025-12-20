package org.teachfx.antlr4.ep18.stackvm.instructions.constant;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 压入null指令
 * 将null引用（0）压入栈
 */
public class NullInstruction extends BaseInstruction {
    public static final int OPCODE = 40;

    public NullInstruction() {
        super("null", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        if (context.isTraceEnabled()) {
            System.out.println("NULL: pushing null reference (0)");
        }

        context.push(0); // null引用用0表示
    }
}
