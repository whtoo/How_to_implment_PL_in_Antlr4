package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 加载局部变量指令
 * 从局部变量数组指定索引加载值到栈顶
 */
public class LoadInstruction extends BaseInstruction {
    public static final int OPCODE = 32;

    public LoadInstruction() {
        super("load", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int index = operand;
        int value = context.loadLocal(index);

        if (context.isTraceEnabled()) {
            System.out.println("LOAD: local[" + index + "] = " + value);
        }

        context.push(value);
    }
}
