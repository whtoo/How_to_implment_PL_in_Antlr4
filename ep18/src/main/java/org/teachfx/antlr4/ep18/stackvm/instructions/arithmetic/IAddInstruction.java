package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;
import org.teachfx.antlr4.ep18.stackvm.VMOverflowException;

/**
 * 整数加法指令
 * 从栈中弹出两个整数，相加后将结果压入栈
 * 使用ArithmeticExecutor进行高效的算术运算和溢出检测
 */
public class IAddInstruction extends BaseInstruction {
    public static final int OPCODE = 1;

    public IAddInstruction() {
        super("iadd", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();

        // 使用ArithmeticExecutor进行带溢出检测的加法运算
        int result = ArithmeticExecutor.add(context, a, b);

        if (context.isTraceEnabled()) {
            System.out.println("IADD: " + a + " + " + b + " = " + result);
        }

        context.push(result);
    }
}
