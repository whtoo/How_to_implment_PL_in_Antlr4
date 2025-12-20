package org.teachfx.antlr4.ep18.stackvm.instructions.controlflow;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 无条件跳转指令
 * 修改程序计数器到指定地址
 */
public class BrInstruction extends BaseInstruction {
    public static final int OPCODE = 25;

    public BrInstruction() {
        super("br", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int address = operand;

        if (context.isTraceEnabled()) {
            System.out.println("BR: jumping to address " + address);
        }

        context.setProgramCounter(address);
    }
}
