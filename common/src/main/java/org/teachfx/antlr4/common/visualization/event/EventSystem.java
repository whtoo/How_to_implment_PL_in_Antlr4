package org.teachfx.antlr4.common.visualization.event;

/**
 * 统一事件系统的核心接口定义
 * 
 * <p>提供事件驱动架构的基础抽象，支持虚拟机可视化工具的
 * 实时状态更新和教育功能。</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since 统一可视化框架
 */
public interface EventSystem {
    
    /**
     * 获取事件总线实例
     * 
     * @return 线程安全的事件总线
     */
    EventBus getEventBus();
    
    /**
     * 获取事件发布者
     * 
     * @return 事件发布者实例
     */
    EventPublisher getEventPublisher();
    
    /**
     * 启动事件系统
     * 
     * @throws IllegalStateException 如果事件系统已经启动
     */
    void start() throws IllegalStateException;
    
    /**
     * 停止事件系统
     * 
     * @throws IllegalStateException 如果事件系统未启动
     */
    void stop() throws IllegalStateException;
    
    /**
     * 检查事件系统是否运行中
     * 
     * @return true表示运行中，false表示已停止
     */
    boolean isRunning();
}