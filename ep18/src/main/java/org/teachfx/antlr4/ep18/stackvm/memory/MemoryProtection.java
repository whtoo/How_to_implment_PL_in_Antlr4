package org.teachfx.antlr4.ep18.stackvm.memory;

import java.util.*;

/**
 * 内存保护类
 * 提供内存访问保护、边界检查、对齐验证和访问统计
 */
public class MemoryProtection {
    // 内存区域定义
    public enum MemoryRegion {
        STACK("Stack", 0),
        HEAP("Heap", 1),
        CODE("Code", 2),
        GLOBAL("Global", 3);

        private final String name;
        private final int id;

        MemoryRegion(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }
    }

    // 内存区域配置
    private final int heapStart;
    private final int heapEnd;
    private final int stackStart;
    private final int stackEnd;
    private final int codeStart;
    private final int codeEnd;
    private final int globalStart;
    private final int globalEnd;

    // 访问控制
    private final Set<MemoryRegion> protectedRegions;
    private final boolean strictBoundsChecking;

    // 访问统计
    private final Map<MemoryRegion, AccessStatistics> statistics;
    private long totalAccessCount;
    private long blockedAccessCount;

    // 内存映射表
    private final Map<Integer, MemoryMapping> memoryMap;

    /**
     * 构造函数
     * @param heapSize 堆大小
     * @param stackSize 栈大小
     * @param codeSize 代码段大小
     * @param globalSize 全局数据区大小
     */
    public MemoryProtection(int heapSize, int stackSize, int codeSize, int globalSize) {
        // 设置内存区域边界
        this.heapStart = 0;
        this.heapEnd = heapSize - 1;

        this.stackStart = heapSize;
        this.stackEnd = heapSize + stackSize - 1;

        this.codeStart = heapSize + stackSize;
        this.codeEnd = codeStart + codeSize - 1;

        this.globalStart = codeStart + codeSize;
        this.globalEnd = globalStart + globalSize - 1;

        // 初始化保护区域（默认保护所有区域）
        this.protectedRegions = new HashSet<>(Arrays.asList(MemoryRegion.values()));

        // 启用严格边界检查
        this.strictBoundsChecking = true;

        // 初始化统计
        this.statistics = new HashMap<>();
        for (MemoryRegion region : MemoryRegion.values()) {
            statistics.put(region, new AccessStatistics(region));
        }
        this.totalAccessCount = 0;
        this.blockedAccessCount = 0;

        // 初始化内存映射
        this.memoryMap = new HashMap<>();
    }

    /**
     * 检查内存地址是否在指定区域内
     */
    public boolean isInRegion(int address, MemoryRegion region) {
        switch (region) {
            case HEAP:
                return address >= heapStart && address <= heapEnd;
            case STACK:
                return address >= stackStart && address <= stackEnd;
            case CODE:
                return address >= codeStart && address <= codeEnd;
            case GLOBAL:
                return address >= globalStart && address <= globalEnd;
            default:
                return false;
        }
    }

    /**
     * 获取地址所属的内存区域
     */
    public MemoryRegion getRegion(int address) {
        if (isInRegion(address, MemoryRegion.HEAP)) {
            return MemoryRegion.HEAP;
        } else if (isInRegion(address, MemoryRegion.STACK)) {
            return MemoryRegion.STACK;
        } else if (isInRegion(address, MemoryRegion.CODE)) {
            return MemoryRegion.CODE;
        } else if (isInRegion(address, MemoryRegion.GLOBAL)) {
            return MemoryRegion.GLOBAL;
        }
        return null;
    }

    /**
     * 验证内存访问
     * @param address 内存地址
     * @param size 访问大小（字节）
     * @param accessType 访问类型
     * @return 验证结果
     */
    public AccessValidationResult validateAccess(int address, int size, AccessType accessType) {
        totalAccessCount++;

        // 检查地址范围
        if (address < 0) {
            blockedAccessCount++;
            return AccessValidationResult.failure("Negative address: " + address);
        }

        // 检查边界
        if (strictBoundsChecking && (address < heapStart || address > globalEnd)) {
            blockedAccessCount++;
            return AccessValidationResult.failure("Address out of bounds: " + address);
        }

        // 检查区域保护
        MemoryRegion region = getRegion(address);
        if (region != null && protectedRegions.contains(region)) {
            // 检查大小是否超出区域边界
            int regionEnd = getRegionEnd(region);
            if (address + size - 1 > regionEnd) {
                blockedAccessCount++;
                return AccessValidationResult.failure(
                    "Access size " + size + " exceeds region boundary at address " + address
                );
            }
        }

        // 检查对齐
        if (!isAligned(address, size)) {
            return AccessValidationResult.warning(
                "Unaligned access at address " + address + " with size " + size
            );
        }

        // 记录访问统计
        if (region != null) {
            statistics.get(region).recordAccess(accessType, size);
        }

        return AccessValidationResult.success();
    }

    /**
     * 检查内存对齐
     */
    public boolean isAligned(int address, int size) {
        // 按字节对齐（对于int类型，要求4字节对齐）
        if (size >= 4) {
            return address % 4 == 0;
        }
        return true; // 小于4字节的访问不需要对齐
    }

    /**
     * 获取区域结束地址
     */
    private int getRegionEnd(MemoryRegion region) {
        switch (region) {
            case HEAP:
                return heapEnd;
            case STACK:
                return stackEnd;
            case CODE:
                return codeEnd;
            case GLOBAL:
                return globalEnd;
            default:
                return -1;
        }
    }

    /**
     * 启用/禁用区域保护
     */
    public void setRegionProtected(MemoryRegion region, boolean protected_) {
        if (protected_) {
            protectedRegions.add(region);
        } else {
            protectedRegions.remove(region);
        }
    }

    /**
     * 获取访问统计
     */
    public AccessStatistics getStatistics(MemoryRegion region) {
        return statistics.get(region);
    }

    /**
     * 获取所有统计信息
     */
    public Map<MemoryRegion, AccessStatistics> getAllStatistics() {
        return new HashMap<>(statistics);
    }

    /**
     * 获取总体统计
     */
    public OverallStatistics getOverallStatistics() {
        long totalReads = 0;
        long totalWrites = 0;
        long totalBytesRead = 0;
        long totalBytesWritten = 0;

        for (AccessStatistics stats : statistics.values()) {
            totalReads += stats.getReadCount();
            totalWrites += stats.getWriteCount();
            totalBytesRead += stats.getBytesRead();
            totalBytesWritten += stats.getBytesWritten();
        }

        return new OverallStatistics(
            totalAccessCount,
            blockedAccessCount,
            totalReads,
            totalWrites,
            totalBytesRead,
            totalBytesWritten,
            protectedRegions.size()
        );
    }

    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        for (AccessStatistics stats : statistics.values()) {
            stats.reset();
        }
        totalAccessCount = 0;
        blockedAccessCount = 0;
    }

    /**
     * 清除内存映射
     */
    public void clearMemoryMap() {
        memoryMap.clear();
    }

    /**
     * 添加内存映射
     */
    public void mapMemory(int address, int size, String description) {
        memoryMap.put(address, new MemoryMapping(address, size, description));
    }

    /**
     * 获取内存映射
     */
    public MemoryMapping getMemoryMapping(int address) {
        return memoryMap.get(address);
    }

    /**
     * 获取所有内存映射
     */
    public Collection<MemoryMapping> getAllMemoryMappings() {
        return memoryMap.values();
    }

    // 内部类

    /**
     * 访问类型
     */
    public enum AccessType {
        READ("Read"),
        WRITE("Write"),
        EXECUTE("Execute");

        private final String name;

        AccessType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 访问验证结果
     */
    public static class AccessValidationResult {
        private final boolean success;
        private final boolean warning;
        private final String message;

        private AccessValidationResult(boolean success, boolean warning, String message) {
            this.success = success;
            this.warning = warning;
            this.message = message;
        }

        public static AccessValidationResult success() {
            return new AccessValidationResult(true, false, null);
        }

        public static AccessValidationResult failure(String message) {
            return new AccessValidationResult(false, false, message);
        }

        public static AccessValidationResult warning(String message) {
            return new AccessValidationResult(true, true, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isWarning() {
            return warning;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 访问统计
     */
    public static class AccessStatistics {
        private final MemoryRegion region;
        private long readCount;
        private long writeCount;
        private long bytesRead;
        private long bytesWritten;

        public AccessStatistics(MemoryRegion region) {
            this.region = region;
            this.readCount = 0;
            this.writeCount = 0;
            this.bytesRead = 0;
            this.bytesWritten = 0;
        }

        public void recordAccess(AccessType type, int size) {
            if (type == AccessType.READ) {
                readCount++;
                bytesRead += size;
            } else if (type == AccessType.WRITE) {
                writeCount++;
                bytesWritten += size;
            }
        }

        public void reset() {
            readCount = 0;
            writeCount = 0;
            bytesRead = 0;
            bytesWritten = 0;
        }

        public MemoryRegion getRegion() {
            return region;
        }

        public long getReadCount() {
            return readCount;
        }

        public long getWriteCount() {
            return writeCount;
        }

        public long getBytesRead() {
            return bytesRead;
        }

        public long getBytesWritten() {
            return bytesWritten;
        }

        public long getTotalAccessCount() {
            return readCount + writeCount;
        }

        public long getTotalBytes() {
            return bytesRead + bytesWritten;
        }

        @Override
        public String toString() {
            return String.format(
                "%s Statistics:\n" +
                "  Reads: %d (%d bytes)\n" +
                "  Writes: %d (%d bytes)\n" +
                "  Total: %d accesses (%d bytes)",
                region.getName(),
                readCount, bytesRead,
                writeCount, bytesWritten,
                getTotalAccessCount(), getTotalBytes()
            );
        }
    }

    /**
     * 总体统计
     */
    public static class OverallStatistics {
        private final long totalAccessCount;
        private final long blockedAccessCount;
        private final long totalReads;
        private final long totalWrites;
        private final long totalBytesRead;
        private final long totalBytesWritten;
        private final int protectedRegionsCount;

        public OverallStatistics(long totalAccessCount, long blockedAccessCount,
                               long totalReads, long totalWrites,
                               long totalBytesRead, long totalBytesWritten,
                               int protectedRegionsCount) {
            this.totalAccessCount = totalAccessCount;
            this.blockedAccessCount = blockedAccessCount;
            this.totalReads = totalReads;
            this.totalWrites = totalWrites;
            this.totalBytesRead = totalBytesRead;
            this.totalBytesWritten = totalBytesWritten;
            this.protectedRegionsCount = protectedRegionsCount;
        }

        public long getTotalAccessCount() {
            return totalAccessCount;
        }

        public long getBlockedAccessCount() {
            return blockedAccessCount;
        }

        public double getBlockRate() {
            return totalAccessCount > 0 ? (double) blockedAccessCount / totalAccessCount : 0.0;
        }

        public long getTotalReads() {
            return totalReads;
        }

        public long getTotalWrites() {
            return totalWrites;
        }

        public long getTotalBytesRead() {
            return totalBytesRead;
        }

        public long getTotalBytesWritten() {
            return totalBytesWritten;
        }

        public int getProtectedRegionsCount() {
            return protectedRegionsCount;
        }

        @Override
        public String toString() {
            return String.format(
                "Overall Memory Protection Statistics:\n" +
                "  Total Accesses: %d\n" +
                "  Blocked Accesses: %d (%.2f%%)\n" +
                "  Total Reads: %d (%d bytes)\n" +
                "  Total Writes: %d (%d bytes)\n" +
                "  Protected Regions: %d",
                totalAccessCount,
                blockedAccessCount,
                getBlockRate() * 100,
                totalReads, totalBytesRead,
                totalWrites, totalBytesWritten,
                protectedRegionsCount
            );
        }
    }

    /**
     * 内存映射
     */
    public static class MemoryMapping {
        private final int address;
        private final int size;
        private final String description;
        private final long creationTime;

        public MemoryMapping(int address, int size, String description) {
            this.address = address;
            this.size = size;
            this.description = description;
            this.creationTime = System.currentTimeMillis();
        }

        public int getAddress() {
            return address;
        }

        public int getSize() {
            return size;
        }

        public String getDescription() {
            return description;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public boolean contains(int addr) {
            return addr >= address && addr < address + size;
        }

        @Override
        public String toString() {
            return String.format("MemoryMapping[0x%08X-0x%08X, %d bytes, %s]",
                address, address + size - 1, size, description);
        }
    }
}
