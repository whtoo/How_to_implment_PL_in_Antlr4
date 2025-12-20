package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;
import org.teachfx.antlr4.ep18.stackvm.VMOverflowException;

/**
 * 整数减法指令
 * 从栈中弹出两个整数，相减后将结果压入栈
 * 使用ArithmeticExecutor进行高效的算术运算和溢出检测
 */
public class ISubInstruction extends BaseInstruction {
    public static final int OPCODE = 2;

    public ISubInstruction() {
        super("isub", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();

        // 使用ArithmeticExecutor进行带溢出检测的减法运算
        int result = ArithmeticExecutor.subtract(context, a, b);

        if (context.isTraceEnabled()) {
            System.out.println("ISUB: " + a + " - " + b + " = " + result);
        }

        context.push(result);
    }
}
