package org.teachfx.antlr4.ep18r.vizvmr.unified.core;

/**
 * VM类型定义 - 避免循环依赖
 */
public class VMTypes {

    /**
     * VM执行状态
     */
    public enum VMState {
        CREATED,     // 已创建
        LOADED,      // 已加载代码
        READY,       // 准备就绪
        RUNNING,     // 运行中
        PAUSED,      // 已暂停
        STEPPING,    // 单步执行中
        HALTED,      // 已停止
        ERROR        // 错误状态
    }

    /**
     * VM命令类型
     */
    public enum Command {
        LOAD_CODE,
        START,
        PAUSE,
        STOP,
        STEP,
        RESUME
    }

    /**
     * 命令执行结果
     */
    public static class CommandResult {
        private final Command command;
        private final boolean success;
        private final String message;
        private final Throwable error;
        private final VMState finalState;

        public CommandResult(Command command, boolean success, String message,
                           Throwable error, VMState finalState) {
            this.command = command;
            this.success = success;
            this.message = message;
            this.error = error;
            this.finalState = finalState;
        }

        public static CommandResult success(Command command, String message, VMState finalState) {
            return new CommandResult(command, true, message, null, finalState);
        }

        public static CommandResult failure(Command command, String message, Throwable error) {
            return new CommandResult(command, false, message, error, null);
        }

        public Command getCommand() { return command; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Throwable getError() { return error; }
        public VMState getFinalState() { return finalState; }
    }
}
