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

        // 更新context中的程序计数器以匹配VM的新状态
        // returnFromFunction已经设置了VM的programCounter，所以从VM获取
        context.setProgramCounter(context.getVM().getProgramCounter());

        // 注意：returnFromFunction还可能修改了stackPointer和framePointer
        // 但在当前实现中，这些状态通过context的副本在executeInstruction中更新
        // 如果需要，这里也应该更新其他状态
    }
}
