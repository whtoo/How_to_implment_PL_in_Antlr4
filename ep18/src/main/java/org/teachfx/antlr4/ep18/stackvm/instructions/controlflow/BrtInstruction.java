package org.teachfx.antlr4.ep18.stackvm.instructions.controlflow;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 条件为真跳转指令
 * 如果栈顶值非零，则跳转到指定地址
 */
public class BrtInstruction extends BaseInstruction {
    public static final int OPCODE = 26;

    public BrtInstruction() {
        super("brt", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int address = operand;
        int condition = context.pop();

        if (condition != 0) {
            if (context.isTraceEnabled()) {
                System.out.println("BRT: condition=" + condition + ", jumping to " + address);
            }
            context.setProgramCounter(address);
        } else {
            if (context.isTraceEnabled()) {
                System.out.println("BRT: condition=" + condition + ", not jumping");
            }
        }
    }
}
