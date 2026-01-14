package org.teachfx.antlr4.ep18r.vizvmr.event;

import java.util.EventObject;

/**
 * 虚拟机状态变化的基类事件
 */
public abstract class VMRStateEvent extends EventObject {
    private final long timestamp;
    private final int stepNumber;

    protected VMRStateEvent(Object source, int stepNumber) {
        super(source);
        this.timestamp = System.currentTimeMillis();
        this.stepNumber = stepNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    /**
     * 获取事件的简要描述
     */
    public abstract String getDescription();
}
