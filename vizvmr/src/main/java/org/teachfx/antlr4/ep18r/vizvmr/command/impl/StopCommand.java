package org.teachfx.antlr4.ep18r.vizvmr.command.impl;

import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.command.VMCommand;
import org.teachfx.antlr4.ep18r.vizvmr.controller.VMCommandResult;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;

/**
 * 停止执行命令
 */
public class StopCommand implements VMCommand {
    private final RegisterVMInterpreter vm;

    public StopCommand(RegisterVMInterpreter vm) {
        this.vm = vm;
    }

    @Override
    public VMCommandResult execute() {
        System.out.println("[COMMAND] StopCommand.execute() - 停止执行");
        vm.setPaused(false);
        System.out.println("[STATE] VM已停止");
        return VMCommandResult.success("VM已停止", VMState.STOPPED);
    }

    @Override
    public String getName() {
        return "Stop";
    }

    @Override
    public boolean canExecute(VMState currentState) {
        System.out.println("[COMMAND] StopCommand.canExecute() - 当前状态: " + currentState);
        return currentState == VMState.RUNNING || currentState == VMState.PAUSED || currentState == VMState.STEPPING;
    }
}
