package org.teachfx.antlr4.ep18.stackvm.instructions.controlflow;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 打印指令
 * 从栈中弹出一个值并打印到标准输出
 */
public class PrintInstruction extends BaseInstruction {
    public static final int OPCODE = 38;

    public PrintInstruction() {
        super("print", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int value = context.pop();

        System.out.println(value);

        if (context.isTraceEnabled()) {
            System.out.println("PRINT: " + value);
        }
    }
}
