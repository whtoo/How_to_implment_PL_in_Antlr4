package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;
import org.teachfx.antlr4.ep18.stackvm.VMOverflowException;

/**
 * 整数除法指令
 * 从栈中弹出两个整数，相除后将结果压入栈
 * 使用ArithmeticExecutor进行高效的算术运算和溢出检测
 */
public class IDivInstruction extends BaseInstruction {
    public static final int OPCODE = 4;

    public IDivInstruction() {
        super("idiv", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int b = context.pop();
        int a = context.pop();

        // 使用ArithmeticExecutor进行带溢出检测的除法运算
        // ArithmeticExecutor.divide()会抛出ArithmeticException（除以零）和VMOverflowException（整数溢出）
        int result = ArithmeticExecutor.divide(context, a, b);

        if (context.isTraceEnabled()) {
            System.out.println("IDIV: " + a + " / " + b + " = " + result);
        }

        context.push(result);
    }
}
