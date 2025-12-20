package org.teachfx.antlr4.ep18.stackvm.instructions.controlflow;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 停止指令
 * 停止虚拟机执行
 */
public class HaltInstruction extends BaseInstruction {
    public static final int OPCODE = 42;

    public HaltInstruction() {
        super("halt", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        if (context.isTraceEnabled()) {
            System.out.println("HALT: stopping execution");
        }

        context.getVM().stop();
    }
}
