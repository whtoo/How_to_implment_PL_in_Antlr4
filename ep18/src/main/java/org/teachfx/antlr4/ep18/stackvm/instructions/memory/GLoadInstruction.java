package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 加载全局变量指令
 * 从堆内存指定地址加载值到栈顶
 */
public class GLoadInstruction extends BaseInstruction {
    public static final int OPCODE = 33;

    public GLoadInstruction() {
        super("gload", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int address = operand;
        int value = MemoryAccessExecutor.loadGlobal(context, address);

        if (context.isTraceEnabled()) {
            System.out.println("GLOAD: heap[" + address + "] = " + value);
        }

        context.push(value);
    }
}
