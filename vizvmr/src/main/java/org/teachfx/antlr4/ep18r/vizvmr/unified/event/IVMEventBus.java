package org.teachfx.antlr4.ep18r.vizvmr.unified.event;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * 统一事件总线接口
 *
 * <p>作为中间层，连接所有事件源（stackvm、common、vizvmr）</p>
 * <p>提供统一的发布/订阅机制，避免三重事件监听</p>
 */
public interface IVMEventBus {

    // ==================== 事件发布 ====================

    /**
     * 发布事件
     * @param event 要发布的事件
     */
    void publish(VMEvent event);

    /**
     * 发布多个事件
     * @param events 事件数组
     */
    void publish(VMEvent... events);

    // ==================== 事件订阅 ====================

    /**
     * 订阅所有事件
     * @return 事件流
     */
    Observable<VMEvent> events();

    /**
     * 订阅特定类型的事件
     * @param eventType 事件类
     * @param <T> 事件类型
     * @return 事件流
     */
    <T extends VMEvent> Observable<T> events(Class<T> eventType);

    /**
     * 订阅特定类型的事件
     * @param eventType 事件类型枚举
     * @return 事件流
     */
    Observable<VMEvent> events(VMEvent.VMEventType eventType);

    // ==================== 注册事件源 ====================

    /**
     * 注册事件监听器
     * @param listener 事件监听器
     */
    void registerListener(Object listener);

    /**
     * 注销事件监听器
     * @param listener 事件监听器
     */
    void unregisterListener(Object listener);

    /**
     * 清除所有监听器
     */
    void clearListeners();

    // ==================== 状态查询 ====================

    /**
     * 获取事件计数
     * @return 已发布的事件数量
     */
    int getEventCount();

    /**
     * 清除所有事件历史
     */
    void clearHistory();

    /**
     * 是否有活跃监听器
     * @return 是否有监听器
     */
    boolean hasActiveListeners();
}
