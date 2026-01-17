package org.teachfx.antlr4.ep18r.vizvmr.controller;

import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMStateManager;

/**
 * 虚拟机视图状态适配器
 * 
 * <p>将VMCommandController的状态变化转换为UI视图状态更新。
 * 提供两种集成模式：</p>
 * <ul>
 *   <li>被动模式：通过addStateChangeListener注册，接收状态变化回调</li>
 *   <li>主动模式：polling getViewState()获取当前视图状态</li>
 * </ul>
 */
public class VMViewStateAdapter {

    /**
     * 视图状态监听器接口
     */
    public interface ViewStateListener {
        /**
         * 视图状态变化回调
         * 
         * @param oldState 旧状态
         * @param newState 新状态
         */
        void onViewStateChanged(VMStateChangeEvent.State oldState, VMStateChangeEvent.State newState);
    }

    private final VMCommandController commandController;
    private VMStateChangeEvent.State currentViewState;
    private ViewStateListener viewStateListener;

    public VMViewStateAdapter(VMCommandController commandController) {
        this.commandController = commandController;
        this.currentViewState = convertState(commandController.getCurrentState());
        
        // 注册状态变化监听器
        commandController.addStateChangeListener(this::onCommandStateChanged);
    }

    /**
     * 设置视图状态监听器
     * 
     * @param listener 状态变化监听器
     */
    public void setViewStateListener(ViewStateListener listener) {
        this.viewStateListener = listener;
    }

    /**
     * 获取当前视图状态
     * 
     * @return 当前UI可用的状态
     */
    public VMStateChangeEvent.State getViewState() {
        return currentViewState;
    }

    /**
     * 获取命令控制器状态
     * 
     * @return 内部命令状态
     */
    public VMState getCommandState() {
        return commandController.getCurrentState();
    }

    /**
     * 检查是否可以执行开始操作
     * 
     * @return true表示可以开始执行
     */
    public boolean canStart() {
        VMState state = commandController.getCurrentState();
        return state == VMState.READY || state == VMState.LOADED || 
               state == VMState.STOPPED || state == VMState.PAUSED;
    }

    /**
     * 检查是否可以暂停
     * 
     * @return true表示可以暂停
     */
    public boolean canPause() {
        return commandController.getCurrentState().canPause();
    }

    /**
     * 检查是否可以停止
     * 
     * @return true表示可以停止
     */
    public boolean canStop() {
        VMState state = commandController.getCurrentState();
        return state == VMState.RUNNING || state == VMState.PAUSED || 
               state == VMState.STEPPING;
    }

    /**
     * 检查是否可以单步执行
     * 
     * @return true表示可以单步执行
     */
    public boolean canStep() {
        return commandController.getCurrentState().canStep();
    }

    /**
     * 获取状态描述文本
     * 
     * @return 状态的中文描述
     */
    public String getStateDescription() {
        return commandController.getCurrentState().getDescription();
    }

    /**
     * 获取状态显示名称
     * 
     * @return 状态的显示名称
     */
    public String getStateDisplayName() {
        return commandController.getCurrentState().name();
    }

    /**
     * 检查是否正在运行
     * 
     * @return true表示正在运行
     */
    public boolean isRunning() {
        return commandController.getCurrentState().isRunning();
    }

    /**
     * 检查是否已暂停
     * 
     * @return true表示已暂停
     */
    public boolean isPaused() {
        return commandController.getCurrentState().isPaused();
    }

    /**
     * 检查是否已停止
     * 
     * @return true表示已停止
     */
    public boolean isStopped() {
        return commandController.getCurrentState().isStopped();
    }

    /**
     * 获取状态变更监听器数量（用于测试和调试）
     * 
     * @return 监听器数量
     */
    public int getListenerCount() {
        return viewStateListener != null ? 1 : 0;
    }

    /**
     * 处理命令状态变化
     */
    private void onCommandStateChanged(VMState oldState, VMState newState) {
        VMStateChangeEvent.State oldViewState = currentViewState;
        VMStateChangeEvent.State newViewState = convertState(newState);
        
        if (oldViewState != newViewState) {
            currentViewState = newViewState;
            System.out.println("[VIEW] 视图状态更新: " + oldViewState + " -> " + newViewState);
            
            if (viewStateListener != null) {
                viewStateListener.onViewStateChanged(oldViewState, newViewState);
            }
        }
    }

    /**
     * 将VMState转换为VMStateChangeEvent.State
     */
    private VMStateChangeEvent.State convertState(VMState state) {
        if (state == null) {
            return VMStateChangeEvent.State.CREATED;
        }
        return state.toLegacyState();
    }
}
