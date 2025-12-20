package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 整数按位异或指令
 * 从栈中弹出两个整数，按位异或后将结果压入栈
 */
public class IXorInstruction extends BaseInstruction {
    public static final int OPCODE = 15;

    public IXorInstruction() {
        super("ixor", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();
        int result = ArithmeticExecutor.xor(a, b);

        if (context.isTraceEnabled()) {
            System.out.println("IXOR: " + a + " ^ " + b + " = " + result);
        }

        context.push(result);
    }
}
