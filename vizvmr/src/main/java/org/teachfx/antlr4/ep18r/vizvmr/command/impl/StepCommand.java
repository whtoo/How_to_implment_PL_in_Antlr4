package org.teachfx.antlr4.ep18r.vizvmr.command.impl;

import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.command.VMCommand;
import org.teachfx.antlr4.ep18r.vizvmr.controller.VMCommandResult;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;

/**
 * 单步执行命令
 */
public class StepCommand implements VMCommand {
    private final RegisterVMInterpreter vm;

    public StepCommand(RegisterVMInterpreter vm) {
        this.vm = vm;
    }

    @Override
    public VMCommandResult execute() {
        System.out.println("[COMMAND] StepCommand.execute() - 单步执行");
        System.out.println("[STATE] 当前PC: " + vm.getProgramCounter());

        if (vm.getCode() == null || vm.getCodeSize() == 0) {
            System.err.println("[ERROR] VM未加载代码");
            return VMCommandResult.failure("VM未加载代码", new IllegalStateException("VM未加载代码"));
        }

        try {
            System.out.println("[COMMAND] 设置单步模式并执行");
            vm.setStepMode(true);
            vm.setPaused(false);

            System.out.println("[COMMAND] 调用vm.exec()");
            vm.exec();
            System.out.println("[STATE] 执行后PC: " + vm.getProgramCounter());
            return VMCommandResult.success("单步执行完成", VMState.STEPPING);
        } catch (Exception e) {
            System.err.println("[ERROR] 单步执行失败: " + e.getMessage());
            e.printStackTrace();
            return VMCommandResult.failure("单步执行失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "Step";
    }

    @Override
    public boolean canExecute(VMState currentState) {
        System.out.println("[COMMAND] StepCommand.canExecute() - 当前状态: " + currentState);
        return currentState == VMState.READY || currentState == VMState.PAUSED || currentState == VMState.STOPPED;
    }
}
