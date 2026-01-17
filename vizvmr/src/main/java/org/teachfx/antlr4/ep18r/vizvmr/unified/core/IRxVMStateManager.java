package org.teachfx.antlr4.ep18r.vizvmr.unified.core;

import io.reactivex.rxjava3.core.Observable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 统一响应式虚拟机状态管理器接口
 *
 * <p>整合原有三重状态管理（ReactiveVMRStateModel、VMRStateModel、VMRVisualBridge）
 * 为单一响应式状态管理器，提供：</p>
 *
 * <ul>
 *   <li>RxJava响应式状态流</li>
 *   <li>统一事件总线集成</li>
 *   <li>命令执行能力</li>
 *   <li>断点管理</li>
 *   <li>终端可观测性</li>
 * </ul>
 *
 * @author TeachFX Team
 * @version 1.0
 * @since Unified Refactoring
 */
public interface IRxVMStateManager {

    // ==================== RxJava状态流 ====================

    /**
     * 获取寄存器状态流
     * @return 寄存器数组（16个寄存器）的可观察流
     */
    Observable<int[]> registers();

    /**
     * 获取单个寄存器状态流
     * @param regNum 寄存器编号（0-15）
     * @return 指定寄存器值的可观察流
     */
    Observable<Integer> register(int regNum);

    /**
     * 获取程序计数器状态流
     * @return PC值的可观察流
     */
    Observable<Integer> pc();

    /**
     * 获取堆内存状态流
     * @return 堆内存数组的可观察流
     */
    Observable<int[]> heap();

    /**
     * 获取全局变量状态流
     * @return 全局变量数组的可观察流
     */
    Observable<int[]> globals();

    /**
     * 获取调用栈状态流
     * @return 调用栈帧数组的可观察流
     */
    Observable<int[]> callStack();

    /**
     * 获取虚拟机执行状态流
     * @return VM状态的可观察流
     */
    Observable<VMTypes.VMState> state();

    /**
     * 获取执行步数流
     * @return 已执行指令数的可观察流
     */
    Observable<Long> executionSteps();

    // ==================== 命令执行 ====================

    /**
     * 异步执行命令
     * @param command 要执行的VM命令
     * @return CompletableFuture，包含执行结果
     */
    CompletableFuture<VMTypes.CommandResult> executeAsync(VMTypes.Command command);

    /**
     * 同步执行命令
     * @param command 要执行的VM命令
     * @return 命令执行结果
     */
    VMTypes.CommandResult execute(VMTypes.Command command);

    /**
     * 加载代码到VM
     * @param codeStream 代码输入流
     * @return 加载结果
     */
    CompletableFuture<VMTypes.CommandResult> loadCode(java.io.InputStream codeStream);

    /**
     * 启动VM执行
     */
    void start();

    /**
     * 暂停VM执行
     */
    void pause();

    /**
     * 停止VM执行
     */
    void stop();

    /**
     * 单步执行
     */
    void step();

    /**
     * 恢复执行（从暂停状态）
     */
    void resume();

    // ==================== 断点管理 ====================

    /**
     * 设置断点
     * @param pc 断点地址
     */
    void setBreakpoint(int pc);

    /**
     * 清除断点
     * @param pc 断点地址
     */
    void clearBreakpoint(int pc);

    /**
     * 切换断点
     * @param pc 断点地址
     */
    void toggleBreakpoint(int pc);

    /**
     * 清除所有断点
     */
    void clearAllBreakpoints();

    /**
     * 获取所有断点
     * @return 断点地址集合
     */
    Set<Integer> getBreakpoints();

    /**
     * 检查是否有断点
     * @return 是否存在断点
     */
    boolean hasBreakpoints();

    // ==================== 终端可观测性 ====================

    /**
     * 获取终端可观测性工具
     * @return 可观测性接口
     */
    ITerminalObservability getObservability();

    // ==================== 状态查询 ====================

    /**
     * 获取当前寄存器值快照
     * @return 寄存器数组副本
     */
    int[] getRegistersSnapshot();

    /**
     * 获取指定寄存器当前值
     * @param regNum 寄存器编号
     * @return 寄存器值
     */
    int getRegisterValue(int regNum);

    /**
     * 获取当前PC值
     * @return PC值
     */
    int getPC();

    /**
     * 获取堆内存快照
     * @return 堆内存数组副本
     */
    int[] getHeapSnapshot();

    /**
     * 获取全局变量快照
     * @return 全局变量数组副本
     */
    int[] getGlobalsSnapshot();

    /**
     * 获取当前VM状态
     * @return VM状态
     */
    VMTypes.VMState getCurrentState();

    /**
     * 是否正在运行
     * @return 运行状态
     */
    boolean isRunning();

    /**
     * 是否已暂停
     * @return 暂停状态
     */
    boolean isPaused();

    // ==================== 配置 ====================

    /**
     * 设置自动步进模式
     * @param autoStepMode 是否启用自动步进
     */
    void setAutoStepMode(boolean autoStepMode);

    /**
     * 设置自动步进延迟
     * @param delayMs 延迟毫秒数
     */
    void setAutoStepDelay(int delayMs);

    /**
     * 获取自动步进延迟
     * @return 延迟毫秒数
     */
    int getAutoStepDelay();
}
