package org.teachfx.antlr4.ep18r.vizvmr.command.impl;

import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.command.VMCommand;
import org.teachfx.antlr4.ep18r.vizvmr.controller.VMCommandResult;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;

import java.io.InputStream;

/**
 * 加载代码命令
 */
public class LoadCodeCommand implements VMCommand {
    private final RegisterVMInterpreter vm;
    private final InputStream codeStream;

    public LoadCodeCommand(RegisterVMInterpreter vm, InputStream codeStream) {
        this.vm = vm;
        this.codeStream = codeStream;
    }

    @Override
    public VMCommandResult execute() {
        try {
            boolean hasErrors = RegisterVMInterpreter.load(vm, codeStream);
            if (hasErrors) {
                return VMCommandResult.failure("代码加载失败：存在语法错误", null);
            }
            return VMCommandResult.success("代码加载成功", VMState.LOADED);
        } catch (Exception e) {
            return VMCommandResult.failure("代码加载异常: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "LoadCode";
    }

    @Override
    public boolean canExecute(VMState currentState) {
        return currentState == VMState.CREATED || currentState == VMState.STOPPED;
    }
}
