package org.teachfx.antlr4.ep15.gc;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reference Counting Garbage Collector
 * Uses a free-list managed reference counting algorithm for automatic memory management
 *
 * Design features:
 * - TreeMap-managed free list for memory reuse
 * - First-Fit allocation algorithm
 * - Automatic free block coalescing
 */
public class ReferenceCountingGC implements GarbageCollector {
    private final int heapSize;
    private final byte[] heap;
    private final Map<Integer, GCObjectHeader> objectHeaders;
    private final AtomicInteger nextObjectId;
    private final GCStats stats;

    // Free list managed by offset-sorted TreeMap
    private final TreeMap<Integer, FreeBlock> freeList;

    public ReferenceCountingGC(int heapSize) {
        if (heapSize <= 0) {
            throw new IllegalArgumentException("Heap size must be positive");
        }

        this.heapSize = heapSize;
        this.heap = new byte[heapSize];
        this.objectHeaders = new ConcurrentHashMap<>();
        this.freeList = new TreeMap<>();
        this.nextObjectId = new AtomicInteger(1);
        this.stats = new GCStats();

        // Initialize free list: entire heap is one big free block
        freeList.put(0, new FreeBlock(0, heapSize));
    }

    /**
     * Free block representation
     */
    private static class FreeBlock implements Comparable<FreeBlock> {
        final int offset;
        final int size;

        FreeBlock(int offset, int size) {
            this.offset = offset;
            this.size = size;
        }

        @Override
        public int compareTo(FreeBlock other) {
            return Integer.compare(this.offset, other.offset);
        }

        @Override
        public String toString() {
            return String.format("FreeBlock{offset=%d, size=%d}", offset, size);
        }
    }

    @Override
    public int allocate(int size) throws OutOfMemoryError {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        if (size > heapSize) {
            throw new OutOfMemoryError("Object size exceeds heap size");
        }

        // Try garbage collection to free more space
        if (!hasFreeBlockFor(size)) {
            collect();
        }

        // Use first-fit algorithm to find a suitable free block
        Integer offset = findFreeBlock(size);
        if (offset == null) {
            throw new OutOfMemoryError("Out of memory after garbage collection");
        }

        // Remove block from free list
        FreeBlock block = freeList.remove(offset);

        // If block is too large, split it
        if (block.size > size) {
            int remainingSize = block.size - size;
            int remainingOffset = offset + size;
            freeList.put(remainingOffset, new FreeBlock(remainingOffset, remainingSize));
        }

        // Create object ID and header
        int objectId = nextObjectId.getAndIncrement();
        GCObjectHeader header = new GCObjectHeader(size);
        header.setOffset(offset);
        header.incrementRef();  // Allocator holds reference, refCount=1
        objectHeaders.put(objectId, header);

        // Record statistics
        stats.recordAllocation(size);

        return objectId;
    }

    /**
     * Check if there is a free block large enough
     */
    private boolean hasFreeBlockFor(int size) {
        for (FreeBlock block : freeList.values()) {
            if (block.size >= size) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find a suitable free block using first-fit algorithm
     * @return offset of free block, or null if not found
     */
    private Integer findFreeBlock(int size) {
        for (Map.Entry<Integer, FreeBlock> entry : freeList.entrySet()) {
            if (entry.getValue().size >= size) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void incrementRef(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        if (header != null) {
            header.incrementRef();
        }
    }

    @Override
    public void decrementRef(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        if (header != null) {
            int refCount = header.decrementRef();

            // If reference count drops to 0, reclaim object
            if (refCount <= 0) {
                collectObject(objectId);
            }
        }
    }

    @Override
    public void collect() {
        long startTime = System.nanoTime();

        int collectedObjects = 0;
        long collectedMemory = 0;

        // Collect objects with reference count of 0
        List<Integer> objectsToCollect = new ArrayList<>();
        for (Map.Entry<Integer, GCObjectHeader> entry : objectHeaders.entrySet()) {
            int objectId = entry.getKey();
            GCObjectHeader header = entry.getValue();

            if (header.getRefCount() <= 0) {
                objectsToCollect.add(objectId);
            }
        }

        // Reclaim objects
        for (int objectId : objectsToCollect) {
            collectedMemory += collectObject(objectId);
            collectedObjects++;
        }

        long endTime = System.nanoTime();
        long collectionTime = endTime - startTime;

        // Record statistics
        stats.recordCollection(collectedObjects, collectedMemory, collectionTime);
    }

    /**
     * Reclaim a single object
     * @param objectId object ID
     * @return amount of memory reclaimed
     */
    private long collectObject(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        if (header == null) {
            return 0;
        }

        int size = header.getSize();
        int offset = header.getOffset();

        // Add memory block to free list
        addFreeBlock(offset, size);

        objectHeaders.remove(objectId);
        header.setAlive(false);

        return size;
    }

    /**
     * Add free block to free list, attempt to merge adjacent blocks
     */
    private void addFreeBlock(int offset, int size) {
        // Check if we can merge with previous block
        Map.Entry<Integer, FreeBlock> prevEntry = freeList.floorEntry(offset);
        if (prevEntry != null) {
            FreeBlock prevBlock = prevEntry.getValue();
            if (prevEntry.getKey() + prevBlock.size == offset) {
                freeList.remove(prevEntry.getKey());
                offset = prevEntry.getKey();
                size += prevBlock.size;
            }
        }

        // Check if we can merge with next block
        Map.Entry<Integer, FreeBlock> nextEntry = freeList.higherEntry(offset);
        if (nextEntry != null && nextEntry.getKey() == offset + size) {
            FreeBlock nextBlock = nextEntry.getValue();
            freeList.remove(nextEntry.getKey());
            size += nextBlock.size;
        }

        // Add merged free block
        freeList.put(offset, new FreeBlock(offset, size));
    }

    @Override
    public boolean isObjectAlive(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        boolean alive = header != null && header.isAlive();
        return alive;
    }

    @Override
    public GCStats getStats() {
        return stats;
    }

    @Override
    public void resetStats() {
        stats.reset();
    }

    /**
     * Get heap usage information
     * @return heap usage info
     */
    public String getHeapInfo() {
        int totalUsed = heapSize - getTotalFreeSize();
        double usagePercent = (double) totalUsed / heapSize * 100;
        return String.format("Heap: %d/%d bytes (%.2f%% used), objects: %d, free blocks: %d",
            totalUsed, heapSize, usagePercent, objectHeaders.size(), freeList.size());
    }

    /**
     * Get total free size
     */
    private int getTotalFreeSize() {
        int total = 0;
        for (FreeBlock block : freeList.values()) {
            total += block.size;
        }
        return total;
    }

    /**
     * Manually trigger garbage collection (public method)
     */
    public void forceGC() {
        collect();
    }

    /**
     * Get current heap usage
     * @return used memory amount
     */
    public int getHeapUsage() {
        return heapSize - getTotalFreeSize();
    }

    /**
     * Get heap size
     * @return total heap size
     */
    public int getHeapSize() {
        return heapSize;
    }

    /**
     * Get object count
     * @return current number of objects
     */
    public int getObjectCount() {
        return objectHeaders.size();
    }

    @Override
    public String toString() {
        return String.format("ReferenceCountingGC{heapSize=%d, used=%d, objects=%d, collections=%d, freeBlocks=%d}",
            heapSize, getHeapUsage(), objectHeaders.size(), stats.getTotalCollections(), freeList.size());
    }
}
