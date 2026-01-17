package org.teachfx.antlr4.ep18r.vizvmr.command.impl;

import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.command.VMCommand;
import org.teachfx.antlr4.ep18r.vizvmr.controller.VMCommandResult;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;

/**
 * 启动执行命令
 */
public class StartCommand implements VMCommand {
    private final RegisterVMInterpreter vm;

    public StartCommand(RegisterVMInterpreter vm) {
        this.vm = vm;
    }

    @Override
    public VMCommandResult execute() {
        System.out.println("[COMMAND] StartCommand.execute() - 开始执行");
        System.out.println("[STATE] 当前VM状态: codeSize=" + vm.getCodeSize());

        if (vm.getCode() == null || vm.getCodeSize() == 0) {
            System.err.println("[ERROR] VM未加载代码");
            return VMCommandResult.failure("VM未加载代码", new IllegalStateException("VM未加载代码"));
        }

        try {
            System.out.println("[COMMAND] 调用vm.exec()");
            vm.exec();
            System.out.println("[COMMAND] vm.exec()完成");
            return VMCommandResult.success("VM执行完成", VMState.RUNNING);
        } catch (Exception e) {
            System.err.println("[ERROR] VM执行失败: " + e.getMessage());
            e.printStackTrace();
            return VMCommandResult.failure("VM执行失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "Start";
    }

    @Override
    public boolean canExecute(VMState currentState) {
        System.out.println("[COMMAND] StartCommand.canExecute() - 当前状态: " + currentState);
        return currentState == VMState.LOADED || currentState == VMState.READY || currentState == VMState.PAUSED || currentState == VMState.STOPPED;
    }
}
