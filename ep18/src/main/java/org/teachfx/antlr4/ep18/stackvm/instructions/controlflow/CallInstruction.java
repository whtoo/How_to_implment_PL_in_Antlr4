package org.teachfx.antlr4.ep18.stackvm.instructions.controlflow;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.StackFrame;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 函数调用指令
 * 保存返回地址和当前状态，跳转到函数入口
 */
public class CallInstruction extends BaseInstruction {
    public static final int OPCODE = 23;

    public CallInstruction() {
        super("call", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int functionAddress = operand;

        // 保存返回地址（当前PC已经指向call指令的下一条）
        int returnAddress = context.getProgramCounter();

        if (context.isTraceEnabled()) {
            System.out.println("CALL: function at " + functionAddress + ", return to " + returnAddress);
        }

        // 调用VM的函数调用逻辑
        context.getVM().callFunction(functionAddress, returnAddress);

        // 更新context中的程序计数器以匹配VM的新状态
        context.setProgramCounter(functionAddress);
    }
}
