package org.teachfx.antlr4.ep18.gc;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 引用计数垃圾回收器
 * 使用引用计数算法进行自动内存管理
 */
public class ReferenceCountingGC implements GarbageCollector {
    private final int heapSize;
    private final byte[] heap;
    private final Map<Integer, GCObjectHeader> objectHeaders;
    private final Queue<Integer> freeList;
    private final AtomicInteger nextObjectId;
    private final GCStats stats;

    // 内存管理
    private int heapUsed;
    private int nextFreeOffset;

    public ReferenceCountingGC(int heapSize) {
        if (heapSize <= 0) {
            throw new IllegalArgumentException("Heap size must be positive");
        }

        this.heapSize = heapSize;
        this.heap = new byte[heapSize];
        this.objectHeaders = new ConcurrentHashMap<>();
        this.freeList = new ConcurrentLinkedQueue<>();
        this.nextObjectId = new AtomicInteger(1);
        this.stats = new GCStats();
        this.heapUsed = 0;
        this.nextFreeOffset = 0;

        // 初始化空闲链表
        initializeFreeList();
    }

    /**
     * 初始化空闲链表
     */
    private void initializeFreeList() {
        // 将整个堆空间加入空闲链表
        freeList.offer(0);
    }

    @Override
    public int allocate(int size) throws OutOfMemoryError {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        if (size > heapSize) {
            throw new OutOfMemoryError("Object size exceeds heap size");
        }

        // 检查是否有足够的空间
        if (heapUsed + size > heapSize) {
            // 尝试垃圾回收
            collect();
        }

        if (heapUsed + size > heapSize) {
            throw new OutOfMemoryError("Out of memory after garbage collection");
        }

        int objectId = nextObjectId.getAndIncrement();
        int offset = nextFreeOffset;

        // 分配内存
        heapUsed += size;
        nextFreeOffset += size;

        // 创建对象头部
        GCObjectHeader header = new GCObjectHeader(size);
        // 新分配的对象引用计数为1（分配者持有引用）
        header.incrementRef();
        objectHeaders.put(objectId, header);

        // 调试信息
        System.out.println("DEBUG allocate: objectId=" + objectId + ", size=" + size + ", header=" + header);
        System.out.println("DEBUG objectHeaders size: " + objectHeaders.size());

        // 记录统计信息
        stats.recordAllocation(size);

        if (offset >= heapSize) {
            throw new OutOfMemoryError("Out of memory: unable to allocate " + size + " bytes");
        }

        return objectId;
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
        heapUsed -= size;
        objectHeaders.remove(objectId);
        header.setAlive(false);

        return size;
    }

    @Override
    public boolean isObjectAlive(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        boolean alive = header != null && header.isAlive();
        // 调试信息
        if (objectId == 1) {
            System.out.println("DEBUG isObjectAlive(" + objectId + "): header=" + header + ", alive=" + alive);
            if (header != null) {
                System.out.println("DEBUG header refCount=" + header.getRefCount() + ", size=" + header.getSize());
            }
        }
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
        double usagePercent = (double) heapUsed / heapSize * 100;
        return String.format("Heap: %d/%d bytes (%.2f%% used)",
            heapUsed, heapSize, usagePercent);
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
        heapUsed = 0;
        nextFreeOffset = 0;
        nextObjectId.set(1);
    }

    /**
     * 获取当前堆使用量
     * @return 使用的内存量
     */
    public int getHeapUsage() {
        return heapUsed;
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
        return String.format("ReferenceCountingGC{heapSize=%d, used=%d, objects=%d, collections=%d}",
            heapSize, heapUsed, objectHeaders.size(), stats.getTotalCollections());
    }
}