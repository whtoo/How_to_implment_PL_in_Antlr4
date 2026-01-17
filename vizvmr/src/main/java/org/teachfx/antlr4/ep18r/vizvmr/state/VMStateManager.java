package org.teachfx.antlr4.ep18r.vizvmr.state;

import java.util.ArrayList;
import java.util.List;

/**
 * 虚拟机状态管理器 - 状态机模式
 * 负责管理状态转换并验证有效性
 */
public class VMStateManager {
    private VMState currentState;
    private final List<StateChangeListener> listeners;

    public VMStateManager() {
        this.currentState = VMState.CREATED;
        this.listeners = new ArrayList<>();
    }

    public VMState getCurrentState() {
        return currentState;
    }

    public boolean canTransitionTo(VMState newState) {
        System.out.println("[STATE] 状态转换检查: " + currentState + " -> " + newState);

        switch (currentState) {
            case CREATED:
                return newState == VMState.LOADED;
            case LOADED:
                return newState == VMState.READY || newState == VMState.RUNNING || newState == VMState.STOPPED;
            case READY:
                return newState == VMState.RUNNING || newState == VMState.STEPPING || newState == VMState.STOPPED;
            case RUNNING:
                return newState == VMState.PAUSED || newState == VMState.STOPPED || newState == VMState.ERROR;
            case PAUSED:
                return newState == VMState.RUNNING || newState == VMState.STEPPING || newState == VMState.STOPPED;
            case STEPPING:
                return newState == VMState.RUNNING || newState == VMState.PAUSED || newState == VMState.STOPPED || newState == VMState.ERROR;
            case STOPPED:
                return newState == VMState.READY || newState == VMState.RUNNING || newState == VMState.STEPPING;
            case ERROR:
                return newState == VMState.STOPPED || newState == VMState.READY;
            default:
                return false;
        }
    }

    public boolean transitionTo(VMState newState) {
        if (!canTransitionTo(newState)) {
            System.err.println("[STATE] 无效状态转换: " + currentState + " -> " + newState);
            return false;
        }

        VMState oldState = currentState;
        currentState = newState;
        System.out.println("[STATE] 状态转换成功: " + oldState + " -> " + currentState);

        notifyStateChanged(oldState, currentState);
        return true;
    }

    public void addStateChangeListener(StateChangeListener listener) {
        listeners.add(listener);
    }

    public void removeStateChangeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyStateChanged(VMState oldState, VMState newState) {
        System.out.println("[EVENT] 触发状态变更事件，监听器数量: " + listeners.size());
        for (StateChangeListener listener : listeners) {
            listener.onStateChanged(oldState, newState);
        }
    }

    public interface StateChangeListener {
        void onStateChanged(VMState oldState, VMState newState);
    }
}
