package org.teachfx.antlr4.ep18.gc;

/**
 * 垃圾回收器接口
 * 定义虚拟机垃圾回收的基本操作
 */
public interface GarbageCollector {
    /**
     * 分配内存对象
     * @param size 对象大小
     * @return 对象ID
     * @throws OutOfMemoryError 内存不足
     */
    int allocate(int size) throws OutOfMemoryError;

    /**
     * 增加引用计数
     * @param objectId 对象ID
     */
    void incrementRef(int objectId);

    /**
     * 减少引用计数
     * @param objectId 对象ID
     */
    void decrementRef(int objectId);

    /**
     * 执行垃圾回收
     */
    void collect();

    /**
     * 检查对象是否存活
     * @param objectId 对象ID
     * @return 是否存活
     */
    boolean isObjectAlive(int objectId);

    /**
     * 获取GC统计信息
     * @return 统计信息
     */
    GCStats getStats();

    /**
     * 重置统计信息
     */
    void resetStats();
}