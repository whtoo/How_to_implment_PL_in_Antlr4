package org.teachfx.antlr4.ep18r.vizvmr.state;

import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;

/**
 * 虚拟机状态 - 状态机模式
 * 定义虚拟机可能的所有执行状态
 */
public enum VMState {
    CREATED("已创建", VMStateChangeEvent.State.CREATED),
    LOADED("已加载", VMStateChangeEvent.State.LOADED),
    READY("就绪", VMStateChangeEvent.State.LOADED),
    RUNNING("运行中", VMStateChangeEvent.State.RUNNING),
    PAUSED("已暂停", VMStateChangeEvent.State.PAUSED),
    STEPPING("单步执行中", VMStateChangeEvent.State.STEPPING),
    STOPPED("已停止", VMStateChangeEvent.State.HALTED),
    ERROR("错误", VMStateChangeEvent.State.HALTED);

    private final String description;
    private final VMStateChangeEvent.State legacyState;

    VMState(String description, VMStateChangeEvent.State legacyState) {
        this.description = description;
        this.legacyState = legacyState;
    }

    public String getDescription() {
        return description;
    }

    public VMStateChangeEvent.State toLegacyState() {
        return legacyState;
    }

    public boolean isRunning() {
        return this == RUNNING || this == STEPPING;
    }

    public boolean isPaused() {
        return this == PAUSED;
    }

    public boolean isStopped() {
        return this == STOPPED;
    }

    public boolean canExecute() {
        return this == READY || this == LOADED || this == PAUSED || this == STOPPED || this == RUNNING;
    }

    public boolean canPause() {
        return this == RUNNING || this == STEPPING;
    }

    public boolean canStep() {
        return this == READY || this == LOADED || this == PAUSED || this == STOPPED;
    }

    public boolean autoTransitionToReady() {
        System.out.println("[STATE] 自动转换触发: LOADED -> READY");
        return this == LOADED;
    }

    public static VMState fromLegacyState(VMStateChangeEvent.State legacyState) {
        for (VMState state : values()) {
            if (state.legacyState == legacyState && state != READY) {
                return state;
            }
        }
        if (legacyState == VMStateChangeEvent.State.LOADED) {
            return LOADED;
        }
        return CREATED;
    }
}
