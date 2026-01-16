package org.teachfx.antlr4.ep18r.vizvmr.event;

import java.util.EventObject;

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

    public abstract String getDescription();
}
