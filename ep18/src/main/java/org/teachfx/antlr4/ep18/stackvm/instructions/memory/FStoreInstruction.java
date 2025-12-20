package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 存储结构体字段指令
 * 将值存储到结构体指定字段
 * 栈布局: [..., structRef, value] -> [...]
 */
public class FStoreInstruction extends BaseInstruction {
    public static final int OPCODE = 37;

    public FStoreInstruction() {
        super("fstore", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int fieldOffset = operand;
        int value = context.pop();
        int structRef = context.pop();

        if (context.isTraceEnabled()) {
            System.out.println("FSTORE: struct[" + structRef + "].field[" + fieldOffset + "] = " + value);
        }

        MemoryAccessExecutor.storeField(context, structRef, fieldOffset, value);
    }
}
