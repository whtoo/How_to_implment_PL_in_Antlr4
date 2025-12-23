package org.teachfx.antlr4.ep18r.gc;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 引用计数垃圾回收器
 * 使用空闲链表管理的引用计数算法进行自动内存管理
 *
 * 设计改进（2025-12-23）：
 * - 使用TreeMap管理的空闲链表，支持内存重用
 * - 首次适应（First-Fit）分配算法
 * - 空闲块自动合并（简单实现）
 */
public class ReferenceCountingGC implements GarbageCollector {
    private final int heapSize;
    private final byte[] heap;
    private final Map<Integer, GCObjectHeader> objectHeaders;
    private final AtomicInteger nextObjectId;
    private final GCStats stats;

    // 空闲链表管理（按偏移量排序的TreeMap）
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

        // 初始化空闲链表：整个堆是一个大空闲块
        freeList.put(0, new FreeBlock(0, heapSize));
    }

    /**
     * 空闲块表示
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

        // 尝试垃圾回收以释放更多空间
        if (!hasFreeBlockFor(size)) {
            collect();
        }

        // 使用首次适应算法查找合适的空闲块
        Integer offset = findFreeBlock(size);
        if (offset == null) {
            throw new OutOfMemoryError("Out of memory after garbage collection");
        }

        // 从空闲链表中移除该块
        FreeBlock block = freeList.remove(offset);

        // 如果块太大，分裂它
        if (block.size > size) {
            int remainingSize = block.size - size;
            int remainingOffset = offset + size;
            freeList.put(remainingOffset, new FreeBlock(remainingOffset, remainingSize));
        }

        // 创建对象ID和头部
        int objectId = nextObjectId.getAndIncrement();
        GCObjectHeader header = new GCObjectHeader(size);
        header.setOffset(offset);  // 记录偏移量
        header.incrementRef();  // 分配者持有引用，引用计数=1
        objectHeaders.put(objectId, header);

        // 记录统计信息
        stats.recordAllocation(size);

        return objectId;
    }

    /**
     * 检查是否有足够大小的空闲块
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
     * 使用首次适应算法查找合适的空闲块
     * @return 空闲块的偏移量，如果没有找到返回null
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

            // 如果引用计数降为0，回收对象
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

        // 收集引用计数为0的对象
        List<Integer> objectsToCollect = new ArrayList<>();
        for (Map.Entry<Integer, GCObjectHeader> entry : objectHeaders.entrySet()) {
            int objectId = entry.getKey();
            GCObjectHeader header = entry.getValue();

            if (header.getRefCount() <= 0) {
                objectsToCollect.add(objectId);
            }
        }

        // 回收对象
        for (int objectId : objectsToCollect) {
            collectedMemory += collectObject(objectId);
            collectedObjects++;
        }

        long endTime = System.nanoTime();
        long collectionTime = endTime - startTime;

        // 记录统计信息
        stats.recordCollection(collectedObjects, collectedMemory, collectionTime);
    }

    /**
     * 回收单个对象
     * @param objectId 对象ID
     * @return 回收的内存量
     */
    private long collectObject(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        if (header == null) {
            return 0;
        }

        int size = header.getSize();
        int offset = header.getOffset();

        // 将内存块加入空闲链表
        addFreeBlock(offset, size);

        objectHeaders.remove(objectId);
        header.setAlive(false);

        return size;
    }

    /**
     * 将空闲块加入空闲链表，尝试合并相邻的空闲块
     */
    private void addFreeBlock(int offset, int size) {
        // 检查是否可以与前一个块合并
        Map.Entry<Integer, FreeBlock> prevEntry = freeList.floorEntry(offset);
        if (prevEntry != null) {
            FreeBlock prevBlock = prevEntry.getValue();
            if (prevEntry.getKey() + prevBlock.size == offset) {
                // 可以合并，移除前一个块，创建更大的块
                freeList.remove(prevEntry.getKey());
                offset = prevEntry.getKey();
                size += prevBlock.size;
            }
        }

        // 检查是否可以与后一个块合并
        Map.Entry<Integer, FreeBlock> nextEntry = freeList.higherEntry(offset);
        if (nextEntry != null && nextEntry.getKey() == offset + size) {
            // 可以合并，移除后一个块，扩展当前块
            FreeBlock nextBlock = nextEntry.getValue();
            freeList.remove(nextEntry.getKey());
            size += nextBlock.size;
        }

        // 添加合并后的空闲块
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
     * 获取堆使用情况
     * @return 堆使用信息
     */
    public String getHeapInfo() {
        int totalUsed = heapSize - getTotalFreeSize();
        double usagePercent = (double) totalUsed / heapSize * 100;
        return String.format("Heap: %d/%d bytes (%.2f%% used), objects: %d, free blocks: %d",
            totalUsed, heapSize, usagePercent, objectHeaders.size(), freeList.size());
    }

    /**
     * 获取空闲块总大小
     */
    private int getTotalFreeSize() {
        int total = 0;
        for (FreeBlock block : freeList.values()) {
            total += block.size;
        }
        return total;
    }

    /**
     * 手动触发垃圾回收（公开方法）
     */
    public void forceGC() {
        collect();
    }

    /**
     * 检查是否可能存在循环引用
     * @param objectId 对象ID
     * @return 是否可能存在循环引用
     */
    public boolean hasCycleReference(int objectId) {
        // 简单的循环引用检测
        // 在实际实现中，需要跟踪对象的引用关系
        GCObjectHeader header = objectHeaders.get(objectId);
        return header != null && header.getRefCount() > 0 && !header.isAlive();
    }

    /**
     * 清理所有对象（用于测试）
     */
    public void clearAll() {
        objectHeaders.clear();
        freeList.clear();
        nextObjectId.set(1);
        // 重新初始化空闲链表
        freeList.put(0, new FreeBlock(0, heapSize));
    }

    /**
     * 获取当前堆使用量
     * @return 使用的内存量
     */
    public int getHeapUsage() {
        return heapSize - getTotalFreeSize();
    }

    /**
     * 获取堆大小
     * @return 总堆大小
     */
    public int getHeapSize() {
        return heapSize;
    }

    /**
     * 获取对象数量
     * @return 当前对象数量
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
