package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 存储全局变量指令
 * 将栈顶值存储到堆内存指定地址
 */
public class GStoreInstruction extends BaseInstruction {
    public static final int OPCODE = 36;

    public GStoreInstruction() {
        super("gstore", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int address = operand;
        int value = context.pop();

        if (context.isTraceEnabled()) {
            System.out.println("GSTORE: heap[" + address + "] = " + value);
        }

        MemoryAccessExecutor.storeGlobal(context, address, value);
    }
}
