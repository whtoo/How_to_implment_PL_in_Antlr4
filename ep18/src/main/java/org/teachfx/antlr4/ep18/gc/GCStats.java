package org.teachfx.antlr4.ep18.gc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 垃圾回收统计信息类
 * 记录垃圾回收器的性能和统计数据
 */
public class GCStats {
    private final AtomicLong totalCollections;
    private final AtomicLong totalCollectedObjects;
    private final AtomicLong totalCollectedMemory;
    private final AtomicLong peakMemoryUsage;
    private final AtomicLong totalAllocatedMemory;
    private final AtomicLong totalCollectionTime;
    private final AtomicLong maxCollectionTime;

    public GCStats() {
        this.totalCollections = new AtomicLong(0);
        this.totalCollectedObjects = new AtomicLong(0);
        this.totalCollectedMemory = new AtomicLong(0);
        this.peakMemoryUsage = new AtomicLong(0);
        this.totalAllocatedMemory = new AtomicLong(0);
        this.totalCollectionTime = new AtomicLong(0);
        this.maxCollectionTime = new AtomicLong(0);
    }

    /**
     * 记录一次回收
     * @param collectedObjects 回收的对象数量
     * @param collectedMemory 回收的内存量
     * @param collectionTime 回收耗时（纳秒）
     */
    public void recordCollection(int collectedObjects, long collectedMemory, long collectionTime) {
        totalCollections.incrementAndGet();
        totalCollectedObjects.addAndGet(collectedObjects);
        totalCollectedMemory.addAndGet(collectedMemory);
        totalCollectionTime.addAndGet(collectionTime);

        // 更新最大回收时间
        long currentMax = maxCollectionTime.get();
        if (collectionTime > currentMax) {
            maxCollectionTime.compareAndSet(currentMax, collectionTime);
        }
    }

    /**
     * 记录内存分配
     * @param allocated 分配的内存量
     */
    public void recordAllocation(long allocated) {
        totalAllocatedMemory.addAndGet(allocated);

        // 更新峰值内存使用
        long currentPeak = peakMemoryUsage.get();
        if (allocated > currentPeak) {
            peakMemoryUsage.compareAndSet(currentPeak, allocated);
        }
    }

    // Getters
    public long getTotalCollections() {
        return totalCollections.get();
    }

    public long getTotalCollectedObjects() {
        return totalCollectedObjects.get();
    }

    public long getTotalCollectedMemory() {
        return totalCollectedMemory.get();
    }

    public long getPeakMemoryUsage() {
        return peakMemoryUsage.get();
    }

    public long getTotalAllocatedMemory() {
        return totalAllocatedMemory.get();
    }

    public long getTotalCollectionTime() {
        return totalCollectionTime.get();
    }

    public long getMaxCollectionTime() {
        return maxCollectionTime.get();
    }

    public long getAverageCollectionTime() {
        long collections = totalCollections.get();
        if (collections == 0) {
            return 0;
        }
        return totalCollectionTime.get() / collections;
    }

    /**
     * 获取统计摘要
     * @return 格式化的统计摘要
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("GC Statistics:\n");
        sb.append("  Total Collections: ").append(getTotalCollections()).append("\n");
        sb.append("  Total Collected Objects: ").append(getTotalCollectedObjects()).append("\n");
        sb.append("  Total Collected Memory: ").append(getTotalCollectedMemory()).append(" bytes\n");
        sb.append("  Peak Memory Usage: ").append(getPeakMemoryUsage()).append(" bytes\n");
        sb.append("  Total Allocated Memory: ").append(getTotalAllocatedMemory()).append(" bytes\n");
        sb.append("  Total Collection Time: ").append(getTotalCollectionTime() / 1_000_000.0).append(" ms\n");
        sb.append("  Average Collection Time: ").append(getAverageCollectionTime() / 1_000_000.0).append(" ms\n");
        sb.append("  Max Collection Time: ").append(getMaxCollectionTime() / 1_000_000.0).append(" ms\n");
        return sb.toString();
    }

    /**
     * 重置统计信息
     */
    public void reset() {
        totalCollections.set(0);
        totalCollectedObjects.set(0);
        totalCollectedMemory.set(0);
        totalAllocatedMemory.set(0);
        totalCollectionTime.set(0);
        maxCollectionTime.set(0);
        peakMemoryUsage.set(0);
    }
}