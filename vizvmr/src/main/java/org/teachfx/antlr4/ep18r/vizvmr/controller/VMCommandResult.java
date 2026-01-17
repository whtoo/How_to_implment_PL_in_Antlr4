package org.teachfx.antlr4.ep18r.vizvmr.controller;

import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;

/**
 * 命令执行结果
 */
public class VMCommandResult {
    private final boolean success;
    private final String message;
    private final Exception error;
    private final VMState newState;

    private VMCommandResult(boolean success, String message, Exception error, VMState newState) {
        this.success = success;
        this.message = message;
        this.error = error;
        this.newState = newState;
    }

    public static VMCommandResult success(String message, VMState newState) {
        return new VMCommandResult(true, message, null, newState);
    }

    public static VMCommandResult failure(String message, Exception error) {
        return new VMCommandResult(false, message, error, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Exception getError() {
        return error;
    }

    public VMState getNewState() {
        return newState;
    }
}
