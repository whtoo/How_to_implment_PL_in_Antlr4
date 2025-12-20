package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存访问执行器
 * 提供高效的内存访问实现，包括访问统计、边界检查和缓存友好访问
 */
public class MemoryAccessExecutor {

    /**
     * 内存访问统计
     */
    public static class MemoryAccessStatistics {
        private final AtomicLong localReads = new AtomicLong(0);
        private final AtomicLong localWrites = new AtomicLong(0);
        private final AtomicLong globalReads = new AtomicLong(0);
        private final AtomicLong globalWrites = new AtomicLong(0);
        private final AtomicLong fieldReads = new AtomicLong(0);
        private final AtomicLong fieldWrites = new AtomicLong(0);
        private final AtomicLong heapAllocations = new AtomicLong(0);
        private final AtomicLong totalBytesAllocated = new AtomicLong(0);

        public void recordLocalRead() { localReads.incrementAndGet(); }
        public void recordLocalWrite() { localWrites.incrementAndGet(); }
        public void recordGlobalRead() { globalReads.incrementAndGet(); }
        public void recordGlobalWrite() { globalWrites.incrementAndGet(); }
        public void recordFieldRead() { fieldReads.incrementAndGet(); }
        public void recordFieldWrite() { fieldWrites.incrementAndGet(); }
        public void recordHeapAllocation(int size) {
            heapAllocations.incrementAndGet();
            totalBytesAllocated.addAndGet(size * 4); // 假设每个值4字节
        }

        public long getLocalReads() { return localReads.get(); }
        public long getLocalWrites() { return localWrites.get(); }
        public long getGlobalReads() { return globalReads.get(); }
        public long getGlobalWrites() { return globalWrites.get(); }
        public long getFieldReads() { return fieldReads.get(); }
        public long getFieldWrites() { return fieldWrites.get(); }
        public long getHeapAllocations() { return heapAllocations.get(); }
        public long getTotalBytesAllocated() { return totalBytesAllocated.get(); }

        public long getTotalReads() {
            return localReads.get() + globalReads.get() + fieldReads.get();
        }

        public long getTotalWrites() {
            return localWrites.get() + globalWrites.get() + fieldWrites.get();
        }

        public long getTotalAccesses() {
            return getTotalReads() + getTotalWrites();
        }

        @Override
        public String toString() {
            return String.format(
                "Memory Access Statistics:\n" +
                "  Local reads: %d\n" +
                "  Local writes: %d\n" +
                "  Global reads: %d\n" +
                "  Global writes: %d\n" +
                "  Field reads: %d\n" +
                "  Field writes: %d\n" +
                "  Total reads: %d\n" +
                "  Total writes: %d\n" +
                "  Heap allocations: %d\n" +
                "  Total bytes allocated: %d",
                localReads.get(), localWrites.get(),
                globalReads.get(), globalWrites.get(),
                fieldReads.get(), fieldWrites.get(),
                getTotalReads(), getTotalWrites(),
                heapAllocations.get(), totalBytesAllocated.get()
            );
        }

        public void reset() {
            localReads.set(0);
            localWrites.set(0);
            globalReads.set(0);
            globalWrites.set(0);
            fieldReads.set(0);
            fieldWrites.set(0);
            heapAllocations.set(0);
            totalBytesAllocated.set(0);
        }
    }

    private static final MemoryAccessStatistics globalStats = new MemoryAccessStatistics();

    /**
     * 获取全局内存访问统计
     */
    public static MemoryAccessStatistics getGlobalStatistics() {
        return globalStats;
    }

    /**
     * 加载局部变量
     * @param context 执行上下文
     * @param index 局部变量索引
     * @return 变量值
     */
    public static int loadLocal(VMExecutionContext context, int index) {
        globalStats.recordLocalRead();
        return context.loadLocal(index);
    }

    /**
     * 存储局部变量
     * @param context 执行上下文
     * @param index 局部变量索引
     * @param value 值
     */
    public static void storeLocal(VMExecutionContext context, int index, int value) {
        globalStats.recordLocalWrite();
        context.storeLocal(index, value);
    }

    /**
     * 加载全局变量
     * @param context 执行上下文
     * @param address 全局地址
     * @return 变量值
     */
    public static int loadGlobal(VMExecutionContext context, int address) {
        globalStats.recordGlobalRead();
        return context.heapRead(address);
    }

    /**
     * 存储全局变量
     * @param context 执行上下文
     * @param address 全局地址
     * @param value 值
     */
    public static void storeGlobal(VMExecutionContext context, int address, int value) {
        globalStats.recordGlobalWrite();
        context.heapWrite(address, value);
    }

    /**
     * 加载结构体字段
     * @param context 执行上下文
     * @param structRef 结构体引用
     * @param fieldOffset 字段偏移
     * @return 字段值
     */
    public static int loadField(VMExecutionContext context, int structRef, int fieldOffset) {
        globalStats.recordFieldRead();
        return context.loadStructField(structRef, fieldOffset);
    }

    /**
     * 存储结构体字段
     * @param context 执行上下文
     * @param structRef 结构体引用
     * @param fieldOffset 字段偏移
     * @param value 值
     */
    public static void storeField(VMExecutionContext context, int structRef, int fieldOffset, int value) {
        globalStats.recordFieldWrite();
        context.storeStructField(structRef, fieldOffset, value);
    }

    /**
     * 分配堆内存
     * @param context 执行上下文
     * @param size 分配大小
     * @return 分配的地址
     */
    public static int allocateHeap(VMExecutionContext context, int size) {
        globalStats.recordHeapAllocation(size);
        return context.heapAlloc(size);
    }

    /**
     * 验证局部变量索引
     * @param index 索引
     * @param maxLocals 最大局部变量数
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public static void validateLocalIndex(int index, int maxLocals) {
        if (index < 0 || index >= maxLocals) {
            throw new IndexOutOfBoundsException(
                String.format("Local variable index out of bounds: %d (max: %d)", index, maxLocals)
            );
        }
    }

    /**
     * 验证堆地址
     * @param address 地址
     * @param heapSize 堆大小
     * @throws IndexOutOfBoundsException 如果地址无效
     */
    public static void validateHeapAddress(int address, int heapSize) {
        if (address < 0 || address >= heapSize) {
            throw new IndexOutOfBoundsException(
                String.format("Heap address out of bounds: %d (size: %d)", address, heapSize)
            );
        }
    }

    /**
     * 重置统计信息
     */
    public static void resetStatistics() {
        globalStats.reset();
    }
}
