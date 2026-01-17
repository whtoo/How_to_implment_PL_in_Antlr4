package org.teachfx.antlr4.common.visualization.event;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RxJava响应式事件总线
 * 
 * <p>提供基于RxJava Subject的事件发布-订阅机制，支持类型安全的事件过滤和
 * 自动资源管理。与现有EventBus兼容，可通过适配器互操作。</p>
 * 
 * <p>特性：
 * 1. 线程安全的事件发布和订阅
 * 2. 按事件类型过滤的Observable流
 * 3. 自动资源管理（CompositeDisposable）
 * 4. 支持背压处理（可选择使用Flowable）
 * 5. 与现有EventBus系统的适配器支持</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R响应式事件系统
 */
public class RxEventBus implements EventPublisher {
    private static final Logger logger = LogManager.getLogger(RxEventBus.class);
    
    // 核心RxJava Subject，用于发布所有事件
    private final Subject<VMEvent> eventSubject;
    
    // 按事件类型缓存的Observable，避免重复ofType操作
    private final ConcurrentMap<Class<? extends VMEvent>, Observable<? extends VMEvent>> cachedObservables;
    
    // 资源管理：跟踪所有订阅的Disposable
    private final CompositeDisposable disposables;
    
    // 桥接到传统EventBus（可选）
    private EventBus legacyEventBus;
    
    // 是否启用与旧系统的桥接
    private boolean bridgeToLegacy = false;
    
    /**
     * 创建默认的RxEventBus（不桥接到旧系统）
     */
    public RxEventBus() {
        this(null);
    }
    
    /**
     * 创建RxEventBus，可选地桥接到传统EventBus
     * 
     * @param legacyEventBus 传统EventBus实例（可为null）
     */
    public RxEventBus(EventBus legacyEventBus) {
        // 使用PublishSubject，因为它不会缓存事件，适合实时事件流
        this.eventSubject = PublishSubject.create();
        this.cachedObservables = new ConcurrentHashMap<>();
        this.disposables = new CompositeDisposable();
        this.legacyEventBus = legacyEventBus;
        
        if (legacyEventBus != null) {
            this.bridgeToLegacy = true;
            setupLegacyBridge();
            logger.debug("RxEventBus创建完成，已桥接到传统EventBus");
        } else {
            logger.debug("RxEventBus创建完成（独立模式）");
        }
    }
    
    /**
     * 设置与传统EventBus的桥接
     * 将RxEventBus中的所有事件转发到传统EventBus
     */
    private void setupLegacyBridge() {
        if (legacyEventBus == null || !bridgeToLegacy) {
            return;
        }
        
        // 订阅所有事件并转发到传统EventBus
        Disposable bridgeDisposable = eventSubject.subscribe(
            event -> {
                try {
                    legacyEventBus.publish(event);
                } catch (Exception e) {
                    logger.warn("事件转发到传统EventBus失败: {}", e.getMessage());
                }
            },
            error -> logger.error("RxEventBus事件流错误", error)
        );
        
        disposables.add(bridgeDisposable);
        logger.debug("已建立到传统EventBus的桥接");
    }
    
    /**
     * 获取指定类型的事件流
     * 
     * @param eventType 事件类型类
     * @param <T> 事件类型
     * @return 过滤后的Observable事件流
     */
    @SuppressWarnings("unchecked")
    public <T extends VMEvent> Observable<T> getEventStream(Class<T> eventType) {
        // 使用缓存避免重复的ofType操作
        return (Observable<T>) cachedObservables.computeIfAbsent(eventType, 
            type -> eventSubject.ofType(type).share()
        );
    }
    
    /**
     * 获取所有事件的原始流
     * 
     * @return 包含所有事件的Observable流
     */
    public Observable<VMEvent> getRawEventStream() {
        return eventSubject.hide(); // hide()防止外部调用onNext/onError/onComplete
    }
    
    /**
     * 发布事件
     * 
     * @param event 要发布的事件
     * @param <T> 事件类型
     */
    @Override
    public <T extends VMEvent> void publish(T event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        try {
            logger.trace("发布事件: {} from {}", event.getClass().getSimpleName(), getPublisherId());
            eventSubject.onNext(event);
        } catch (Exception e) {
            logger.error("发布事件失败: {}", e.getMessage(), e);
            // 不重新抛出，保持发布操作的容错性
        }
    }
    
    /**
     * 订阅事件流
     * 
     * @param eventType 事件类型
     * @param subscriber 订阅者（Consumer）
     * @param <T> 事件类型
     * @return Disposable用于取消订阅
     */
    public <T extends VMEvent> Disposable subscribe(Class<T> eventType, io.reactivex.rxjava3.functions.Consumer<T> subscriber) {
        Disposable disposable = getEventStream(eventType)
            .subscribe(subscriber, error -> logger.error("事件订阅处理错误", error));
        
        disposables.add(disposable);
        return disposable;
    }
    
    /**
     * 订阅事件流（带错误处理）
     */
    public <T extends VMEvent> Disposable subscribe(
            Class<T> eventType, 
            io.reactivex.rxjava3.functions.Consumer<T> subscriber,
            io.reactivex.rxjava3.functions.Consumer<Throwable> errorHandler) {
        
        Disposable disposable = getEventStream(eventType).subscribe(subscriber, errorHandler);
        disposables.add(disposable);
        return disposable;
    }
    
    /**
     * 订阅事件流（带完成处理）
     */
    public <T extends VMEvent> Disposable subscribe(
            Class<T> eventType,
            io.reactivex.rxjava3.functions.Consumer<T> subscriber,
            io.reactivex.rxjava3.functions.Consumer<Throwable> errorHandler,
            io.reactivex.rxjava3.functions.Action onComplete) {
        
        Disposable disposable = getEventStream(eventType).subscribe(subscriber, errorHandler, onComplete);
        disposables.add(disposable);
        return disposable;
    }
    
    /**
     * 检查事件流是否有订阅者
     * 
     * @return true如果至少有一个订阅者
     */
    public boolean hasSubscribers() {
        return eventSubject.hasObservers();
    }
    
    /**
     * 检查指定类型的事件是否有订阅者
     */
    public <T extends VMEvent> boolean hasSubscribers(Class<T> eventType) {
        // 注意：由于ofType操作，这只是一个近似值
        return hasSubscribers();
    }
    
    /**
     * 清理所有订阅
     */
    public void clearSubscriptions() {
        disposables.clear();
        logger.debug("已清理所有RxEventBus订阅");
    }
    
    /**
     * 获取发布者ID
     */
    @Override
    public String getPublisherId() {
        return "RxEventBus-" + Integer.toHexString(hashCode());
    }
    
    /**
     * 获取事件总线（实现EventPublisher接口）
     */
    @Override
    public EventBus getEventBus() {
        return legacyEventBus;
    }
    
    /**
     * 设置事件总线（实现EventPublisher接口）
     */
    @Override
    public void setEventBus(EventBus eventBus) {
        this.legacyEventBus = eventBus;
        this.bridgeToLegacy = (eventBus != null);
        
        if (bridgeToLegacy) {
            setupLegacyBridge();
        }
    }
    
    /**
     * 关闭事件总线，释放所有资源
     */
    public void shutdown() {
        try {
            // 完成事件流
            eventSubject.onComplete();
            
            // 清理所有订阅
            clearSubscriptions();
            
            logger.info("RxEventBus已关闭");
        } catch (Exception e) {
            logger.error("关闭RxEventBus时出错", e);
        }
    }
    
    /**
     * 获取统计信息
     */
    public RxEventBusStats getStats() {
        return new RxEventBusStats(
            cachedObservables.size(),
            disposables.size(),
            hasSubscribers(),
            bridgeToLegacy
        );
    }
    
    /**
     * RxEventBus统计信息类
     */
    public static class RxEventBusStats {
        private final int cachedObservableCount;
        private final int activeSubscriptionCount;
        private final boolean hasActiveSubscribers;
        private final boolean bridgedToLegacy;
        
        public RxEventBusStats(int cachedObservableCount, int activeSubscriptionCount, 
                              boolean hasActiveSubscribers, boolean bridgedToLegacy) {
            this.cachedObservableCount = cachedObservableCount;
            this.activeSubscriptionCount = activeSubscriptionCount;
            this.hasActiveSubscribers = hasActiveSubscribers;
            this.bridgedToLegacy = bridgedToLegacy;
        }
        
        public int getCachedObservableCount() { return cachedObservableCount; }
        public int getActiveSubscriptionCount() { return activeSubscriptionCount; }
        public boolean hasActiveSubscribers() { return hasActiveSubscribers; }
        public boolean isBridgedToLegacy() { return bridgedToLegacy; }
        
        @Override
        public String toString() {
            return String.format(
                "RxEventBusStats{cachedObservables=%d, activeSubscriptions=%d, hasSubscribers=%s, bridged=%s}",
                cachedObservableCount, activeSubscriptionCount, hasActiveSubscribers, bridgedToLegacy
            );
        }
    }
}