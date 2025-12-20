package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 创建结构体指令
 * 创建一个新的结构体实例并将引用压入栈
 */
public class StructInstruction extends BaseInstruction {
    public static final int OPCODE = 39;

    public StructInstruction() {
        super("struct", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int nfields = operand;

        // 创建结构体实例
        int structRef = context.createStruct(nfields);

        if (context.isTraceEnabled()) {
            System.out.println("STRUCT: created struct with " + nfields + " fields, ref=" + structRef);
        }

        context.push(structRef);
    }
}
