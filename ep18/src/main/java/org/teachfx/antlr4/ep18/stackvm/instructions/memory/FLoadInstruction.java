package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 加载结构体字段指令
 * 从结构体指定字段加载值到栈顶
 * 栈布局: [..., structRef] -> [..., fieldValue]
 */
public class FLoadInstruction extends BaseInstruction {
    public static final int OPCODE = 34;

    public FLoadInstruction() {
        super("fload", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int fieldOffset = operand;
        int structRef = context.pop();

        int value = MemoryAccessExecutor.loadField(context, structRef, fieldOffset);

        if (context.isTraceEnabled()) {
            System.out.println("FLOAD: struct[" + structRef + "].field[" + fieldOffset + "] = " + value);
        }

        context.push(value);
    }
}
