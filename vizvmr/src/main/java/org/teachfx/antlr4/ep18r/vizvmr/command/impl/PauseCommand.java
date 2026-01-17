package org.teachfx.antlr4.ep18r.vizvmr.command.impl;

import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.command.VMCommand;
import org.teachfx.antlr4.ep18r.vizvmr.controller.VMCommandResult;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;

/**
 * 暂停执行命令
 */
public class PauseCommand implements VMCommand {
    private final RegisterVMInterpreter vm;

    public PauseCommand(RegisterVMInterpreter vm) {
        this.vm = vm;
    }

    @Override
    public VMCommandResult execute() {
        System.out.println("[COMMAND] PauseCommand.execute() - 暂停执行");
        vm.setPaused(true);
        System.out.println("[STATE] VM已暂停");
        return VMCommandResult.success("VM已暂停", VMState.PAUSED);
    }

    @Override
    public String getName() {
        return "Pause";
    }

    @Override
    public boolean canExecute(VMState currentState) {
        System.out.println("[COMMAND] PauseCommand.canExecute() - 当前状态: " + currentState);
        return currentState == VMState.RUNNING || currentState == VMState.STEPPING;
    }
}
