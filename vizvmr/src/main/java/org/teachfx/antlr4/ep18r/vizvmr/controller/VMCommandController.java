package org.teachfx.antlr4.ep18r.vizvmr.controller;

import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.command.VMCommand;
import org.teachfx.antlr4.ep18r.vizvmr.command.impl.LoadCodeCommand;
import org.teachfx.antlr4.ep18r.vizvmr.command.impl.StartCommand;
import org.teachfx.antlr4.ep18r.vizvmr.command.impl.PauseCommand;
import org.teachfx.antlr4.ep18r.vizvmr.command.impl.StopCommand;
import org.teachfx.antlr4.ep18r.vizvmr.command.impl.StepCommand;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMStateManager;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 虚拟机命令控制器
 * 命令逻辑控制器 - 独立于UI，完全可观测
 * 
 * <p>支持同步和异步两种执行模式：
 * <ul>
 *   <li>同步执行：blocking start/pause/stop/step方法会阻塞直到操作完成</li>
 *   <li>异步执行：asyncStart/asyncPause等方法立即返回CompletableFuture</li>
 * </ul>
 * </p>
 */
public class VMCommandController {
    private final RegisterVMInterpreter vm;
    private final VMStateManager stateManager;
    private final ExecutorService executorService;
    
    /**
     * 异步执行监听器
     */
    private Consumer<VMCommandResult> asyncResultListener;

    public VMCommandController(RegisterVMInterpreter vm) {
        System.out.println("[CONTROLLER] VMCommandController初始化");
        this.vm = vm;
        this.stateManager = new VMStateManager();
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "VM-Command-Thread");
            t.setDaemon(true);
            return t;
        });
        System.out.println("[STATE] 初始状态: " + stateManager.getCurrentState());
    }

    public VMState getCurrentState() {
        return stateManager.getCurrentState();
    }

    public void addStateChangeListener(VMStateManager.StateChangeListener listener) {
        stateManager.addStateChangeListener(listener);
    }

    public void removeStateChangeListener(VMStateManager.StateChangeListener listener) {
        stateManager.removeStateChangeListener(listener);
    }

    /**
     * 设置异步执行结果监听器
     * 
     * @param listener 当异步命令执行完成时回调
     */
    public void setAsyncResultListener(Consumer<VMCommandResult> listener) {
        this.asyncResultListener = listener;
    }

    // ==================== 同步执行方法 ====================

    public VMCommandResult loadCode(InputStream codeStream) {
        System.out.println("[CONTROLLER] 接收LoadCode命令 (同步)");
        if (codeStream == null) {
            System.out.println("[WARN] LoadCode命令收到null输入，跳过加载");
            return VMCommandResult.success("跳过代码加载", stateManager.getCurrentState());
        }
        VMCommand command = new LoadCodeCommand(vm, codeStream);
        VMCommandResult result = executeCommand(command, VMState.LOADED);

        if (result.isSuccess()) {
            System.out.println("[STATE] LoadCode成功，触发自动状态转换LOADED->READY");
            stateManager.autoTransitionToReady();
        }

        return result;
    }

    public VMCommandResult start() {
        System.out.println("[CONTROLLER] 接收Start命令 (同步)");
        VMCommand command = new StartCommand(vm);
        return executeCommand(command, VMState.RUNNING);
    }

    public VMCommandResult pause() {
        System.out.println("[CONTROLLER] 接收Pause命令 (同步)");
        VMCommand command = new PauseCommand(vm);
        return executeCommand(command, VMState.PAUSED);
    }

    public VMCommandResult stop() {
        System.out.println("[CONTROLLER] 接收Stop命令 (同步)");
        VMCommand command = new StopCommand(vm);
        return executeCommand(command, VMState.STOPPED);
    }

    public VMCommandResult step() {
        System.out.println("[CONTROLLER] 接收Step命令 (同步)");
        VMCommand command = new StepCommand(vm);
        return executeCommand(command, VMState.STEPPING);
    }

    // ==================== 异步执行方法 ====================

    /**
     * 异步加载代码
     * 
     * @param codeStream 代码输入流
     * @return CompletableFuture，包含加载结果
     */
    public CompletableFuture<VMCommandResult> asyncLoadCode(InputStream codeStream) {
        System.out.println("[CONTROLLER] 接收LoadCode命令 (异步)");
        return CompletableFuture.supplyAsync(() -> loadCode(codeStream), executorService);
    }

    /**
     * 异步开始执行
     * 
     * @return CompletableFuture，包含执行结果
     */
    public CompletableFuture<VMCommandResult> asyncStart() {
        System.out.println("[CONTROLLER] 接收Start命令 (异步)");
        return CompletableFuture.supplyAsync(() -> {
            VMCommandResult result = start();
            notifyAsyncResult(result);
            return result;
        }, executorService);
    }

    /**
     * 异步暂停执行
     * 
     * @return CompletableFuture，包含暂停结果
     */
    public CompletableFuture<VMCommandResult> asyncPause() {
        System.out.println("[CONTROLLER] 接收Pause命令 (异步)");
        return CompletableFuture.supplyAsync(() -> {
            VMCommandResult result = pause();
            notifyAsyncResult(result);
            return result;
        }, executorService);
    }

    /**
     * 异步停止执行
     * 
     * @return CompletableFuture，包含停止结果
     */
    public CompletableFuture<VMCommandResult> asyncStop() {
        System.out.println("[CONTROLLER] 接收Stop命令 (异步)");
        return CompletableFuture.supplyAsync(() -> {
            VMCommandResult result = stop();
            notifyAsyncResult(result);
            return result;
        }, executorService);
    }

    /**
     * 异步单步执行
     * 
     * @return CompletableFuture，包含单步执行结果
     */
    public CompletableFuture<VMCommandResult> asyncStep() {
        System.out.println("[CONTROLLER] 接收Step命令 (异步)");
        return CompletableFuture.supplyAsync(() -> {
            VMCommandResult result = step();
            notifyAsyncResult(result);
            return result;
        }, executorService);
    }

    /**
     * 通知异步执行结果
     */
    private void notifyAsyncResult(VMCommandResult result) {
        if (asyncResultListener != null) {
            asyncResultListener.accept(result);
        }
    }

    // ==================== 内部方法 ====================

    private VMCommandResult executeCommand(VMCommand command, VMState expectedState) {
        System.out.println("[CONTROLLER] 执行命令: " + command.getName());
        System.out.println("[CONTROLLER] 当前状态: " + stateManager.getCurrentState());
        System.out.println("[CONTROLLER] 期望状态: " + expectedState);

        VMState currentState = stateManager.getCurrentState();

        if (!command.canExecute(currentState)) {
            String errorMsg = String.format("命令'%s'在状态'%s'下不可执行",
                    command.getName(), currentState);
            System.err.println("[ERROR] " + errorMsg);
            return VMCommandResult.failure(errorMsg, new IllegalStateException(errorMsg));
        }

        System.out.println("[COMMAND] 执行命令: " + command.getName());

        VMCommandResult result = command.execute();

        System.out.println("[COMMAND] 命令执行结果:");
        System.out.println("  成功: " + result.isSuccess());
        System.out.println("  消息: " + result.getMessage());
        System.out.println("  当前状态: " + stateManager.getCurrentState());

        if (result.isSuccess()) {
            if (expectedState == VMState.RUNNING) {
                System.out.println("[STATE] 检测到Start/Step/Pause命令，触发自动转换到RUNNING");
            }

            boolean transitionSuccess = stateManager.transitionTo(expectedState);

            if (!transitionSuccess) {
                System.err.println("[ERROR] 状态转换失败: " + expectedState);
                return VMCommandResult.failure("状态转换失败", null);
            }
        }

        return result;
    }

    public void reset() {
        System.out.println("[CONTROLLER] 重置VM和状态管理器");
        vm.setPaused(true);
        vm.setStepMode(false);
        stateManager.transitionTo(VMState.STOPPED);
    }

    /**
     * 关闭控制器，释放资源
     */
    public void shutdown() {
        System.out.println("[CONTROLLER] 关闭VMCommandController");
        executorService.shutdownNow();
    }
}
