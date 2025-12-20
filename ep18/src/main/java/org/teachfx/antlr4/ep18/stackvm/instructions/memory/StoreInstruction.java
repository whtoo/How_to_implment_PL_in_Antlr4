package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 存储局部变量指令
 * 从栈顶弹出值并存储到局部变量数组指定索引
 */
public class StoreInstruction extends BaseInstruction {
    public static final int OPCODE = 35;

    public StoreInstruction() {
        super("store", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int index = operand;
        int value = context.pop();

        if (context.isTraceEnabled()) {
            System.out.println("STORE: local[" + index + "] = " + value);
        }

        context.storeLocal(index, value);
    }
}
