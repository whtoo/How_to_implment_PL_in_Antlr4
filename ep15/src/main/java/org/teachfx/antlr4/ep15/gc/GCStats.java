package org.teachfx.antlr4.ep15.gc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Garbage Collection Statistics
 * Records performance and statistical data for the garbage collector
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
     * Record a collection event
     * @param collectedObjects number of objects collected
     * @param collectedMemory amount of memory collected
     * @param collectionTime collection time in nanoseconds
     */
    public void recordCollection(int collectedObjects, long collectedMemory, long collectionTime) {
        totalCollections.incrementAndGet();
        totalCollectedObjects.addAndGet(collectedObjects);
        totalCollectedMemory.addAndGet(collectedMemory);
        totalCollectionTime.addAndGet(collectionTime);

        long currentMax = maxCollectionTime.get();
        if (collectionTime > currentMax) {
            maxCollectionTime.compareAndSet(currentMax, collectionTime);
        }
    }

    /**
     * Record memory allocation
     * @param allocated amount allocated
     */
    public void recordAllocation(long allocated) {
        totalAllocatedMemory.addAndGet(allocated);

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
     * Get formatted summary
     * @return formatted statistics summary
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
     * Reset statistics
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
