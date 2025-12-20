package org.teachfx.antlr4.ep18.stackvm.instructions.controlflow;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 函数返回指令
 * 恢复调用者状态，跳转到返回地址
 */
public class RetInstruction extends BaseInstruction {
    public static final int OPCODE = 24;

    public RetInstruction() {
        super("ret", OPCODE, false);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        if (context.isTraceEnabled()) {
            System.out.println("RET: returning from function");
        }

        // 调用VM的函数返回逻辑
        context.getVM().returnFromFunction();
    }
}
