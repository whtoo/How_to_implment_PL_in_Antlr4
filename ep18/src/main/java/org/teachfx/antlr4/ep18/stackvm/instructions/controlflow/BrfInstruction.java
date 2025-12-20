package org.teachfx.antlr4.ep18.stackvm.instructions.controlflow;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 条件跳转指令（条件为假时跳转）
 * 弹出栈顶值，如果为0则跳转到指定地址
 */
public class BrfInstruction extends BaseInstruction {
    public static final int OPCODE = 27;

    public BrfInstruction() {
        super("brf", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int condition = context.pop();
        int targetAddress = operand;

        boolean jumped = ControlFlowExecutor.jumpIfFalse(context, condition, targetAddress);

        if (context.isTraceEnabled()) {
            System.out.println("BRF: condition=" + condition + ", target=" + targetAddress +
                              ", jumped=" + jumped);
        }
    }
}
