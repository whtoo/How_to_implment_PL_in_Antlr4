package org.teachfx.antlr4.ep18.gc;

/**
 * 无操作垃圾回收器
 * 当GC被禁用时使用，提供空实现以保持代码兼容性
 */
public class NoOpGarbageCollector implements GarbageCollector {

    private final GCStats stats = new GCStats();

    @Override
    public int allocate(int size) throws OutOfMemoryError {
        // 返回一个虚拟对象ID（总是1，因为不真正分配内存）
        return 1;
    }

    @Override
    public void incrementRef(int objectId) {
        // 无操作
    }

    @Override
    public void decrementRef(int objectId) {
        // 无操作
    }

    @Override
    public void collect() {
        // 无操作
    }

    @Override
    public boolean isObjectAlive(int objectId) {
        // 总是返回true，因为不跟踪对象生命周期
        return true;
    }

    @Override
    public GCStats getStats() {
        return stats;
    }

    @Override
    public void resetStats() {
        stats.reset();
    }

    @Override
    public String toString() {
        return "NoOpGarbageCollector{GC disabled}";
    }
}