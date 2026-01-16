package org.teachfx.antlr4.ep18r.vizvmr.event;

/**
 * 栈帧变化事件
 * 当调用栈发生变化时触发此事件
 */
public class StackFrameEvent extends VMRStateEvent {

    private final org.teachfx.antlr4.ep18r.stackvm.StackFrame frame;
    private final EventType eventType;

    public enum EventType {
        PUSH,
        POP
    }

    public StackFrameEvent(Object source, org.teachfx.antlr4.ep18r.stackvm.StackFrame frame, EventType eventType, int stepNumber) {
        super(source, stepNumber);
        this.frame = frame;
        this.eventType = eventType;
    }

    public org.teachfx.antlr4.ep18r.stackvm.StackFrame getFrame() {
        return frame;
    }

    public EventType getEventType() {
        return eventType;
    }

    @Override
    public String getDescription() {
        if (frame == null) {
            return "栈帧: null";
        }

        String action = eventType == EventType.PUSH ? "压入" : "弹出";
        return String.format("栈帧: %s, 函数: %s, 返回: 0x%04X, 帧基址: 0x%04X",
                action,
                frame.getFunctionSymbol() != null ? frame.getFunctionSymbol().name : "<匿名>",
                frame.getReturnAddress(),
                frame.getFrameBasePointer());
    }
}
