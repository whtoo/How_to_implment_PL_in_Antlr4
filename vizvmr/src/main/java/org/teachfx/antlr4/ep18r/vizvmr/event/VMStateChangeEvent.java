package org.teachfx.antlr4.ep18r.vizvmr.event;

/**
 * 虚拟机状态变化事件
 */
public class VMStateChangeEvent extends VMRStateEvent {
    public enum State {
        CREATED, LOADED, RUNNING, PAUSED, STEPPING, HALTED, ERROR
    }

    private final State oldState;
    private final State newState;
    private final String message;

    public VMStateChangeEvent(Object source, int stepNumber, State oldState, State newState, String message) {
        super(source, stepNumber);
        this.oldState = oldState;
        this.newState = newState;
        this.message = message;
    }

    public VMStateChangeEvent(Object source, int stepNumber, State oldState, State newState) {
        this(source, stepNumber, oldState, newState, null);
    }

    public State getOldState() {
        return oldState;
    }

    public State getNewState() {
        return newState;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getDescription() {
        String desc = String.format("VM状态: %s → %s", oldState, newState);
        if (message != null) {
            desc += String.format(" (%s)", message);
        }
        return desc;
    }
}
