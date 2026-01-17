package org.teachfx.antlr4.ep18r.vizvmr.command;

import org.teachfx.antlr4.ep18r.vizvmr.controller.VMCommandResult;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;

/**
 * 命令模式：虚拟机命令接口
 * 封装对虚拟机的所有操作请求
 */
public interface VMCommand {

    /**
     * 执行命令
     * @return 命令执行结果
     */
    VMCommandResult execute();

    /**
     * 获取命令名称（用于日志和调试）
     */
    String getName();

    /**
     * 获取命令在当前状态下是否可执行
     */
    boolean canExecute(VMState currentState);
}
