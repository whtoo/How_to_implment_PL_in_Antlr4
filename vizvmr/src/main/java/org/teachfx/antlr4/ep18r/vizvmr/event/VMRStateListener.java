package org.teachfx.antlr4.ep18r.vizvmr.event;

import java.util.EventListener;

/**
 * 虚拟机状态监听器接口
 * 监听寄存器变化、内存变化、PC变化等状态变化事件
 */
public interface VMRStateListener extends EventListener {

    /**
     * 寄存器值变化时调用
     */
    void registerChanged(RegisterChangeEvent event);

    /**
     * 内存值变化时调用
     */
    void memoryChanged(MemoryChangeEvent event);

    /**
     * 程序计数器变化时调用
     */
    void pcChanged(PCChangeEvent event);

    /**
     * 批量寄存器更新（用于性能优化）
     */
    default void registersUpdated(RegisterChangeEvent[] events) {
        for (RegisterChangeEvent event : events) {
            registerChanged(event);
        }
    }

    /**
     * 批量内存更新（用于性能优化）
     */
    default void memoryUpdated(MemoryChangeEvent[] events) {
        for (MemoryChangeEvent event : events) {
            memoryChanged(event);
        }
    }
}
