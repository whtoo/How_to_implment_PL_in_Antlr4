package org.teachfx.antlr4.ep18r.vizvmr.unified.event;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * VM事件总线实现
 *
 * <p>使用RxJava PublishSubject作为事件总线，支持发布/订阅模式</p>
 */
public class VMEventBusImpl implements IVMEventBus {

    private final Map<Class<? extends VMEvent>, PublishSubject<? extends VMEvent>> eventSubjects;
    private final PublishSubject<VMEvent> allEventsSubject;
    private final List<Object> listeners;

    private int totalEventCount;

    public VMEventBusImpl() {
        this.eventSubjects = new ConcurrentHashMap<>();
        this.allEventsSubject = PublishSubject.create();
        this.listeners = new CopyOnWriteArrayList<>();
        this.totalEventCount = 0;
    }

    // ==================== 事件发布 ====================

    @Override
    public void publish(VMEvent event) {
        if (event == null) {
            return;
        }

        totalEventCount++;

        allEventsSubject.onNext(event);

        PublishSubject subject = eventSubjects.get(event.getClass());
        if (subject != null) {
            subject.onNext(event);
        }
    }

    @Override
    public void publish(VMEvent... events) {
        if (events == null || events.length == 0) {
            return;
        }

        for (VMEvent event : events) {
            publish(event);
        }
    }

    // ==================== 事件订阅 ====================

    @Override
    public Observable<VMEvent> events() {
        return allEventsSubject.hide();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends VMEvent> Observable<T> events(Class<T> eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }

        PublishSubject subject = eventSubjects.computeIfAbsent(
            eventType,
            k -> PublishSubject.create()
        );

        return (Observable<T>) subject.hide();
    }

    @Override
    public Observable<VMEvent> events(VMEvent.VMEventType eventType) {
        return allEventsSubject
            .filter(event -> event.getEventType() == eventType)
            .hide();
    }

    // ==================== 注册事件源 ====================

    @Override
    public void registerListener(Object listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void unregisterListener(Object listener) {
        listeners.remove(listener);
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    // ==================== 状态查询 ====================

    @Override
    public int getEventCount() {
        return totalEventCount;
    }

    @Override
    public void clearHistory() {
        totalEventCount = 0;
    }

    @Override
    public boolean hasActiveListeners() {
        return !listeners.isEmpty();
    }
}
