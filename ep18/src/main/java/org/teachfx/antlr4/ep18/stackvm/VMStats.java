package org.teachfx.antlr4.ep18.stackvm;

import java.util.concurrent.atomic.AtomicLong;

/**
 * VMStats - 虚拟机统计信息类
 * 记录虚拟机的执行统计和性能数据
 */
public class VMStats {
    // 执行统计
    private final AtomicLong executionCount;
    private final AtomicLong totalExecutionTime;
    private final AtomicLong minExecutionTime;
    private final AtomicLong maxExecutionTime;
    
    // 错误统计
    private final AtomicLong errorCount;
    private final AtomicLong lastErrorTime;
    
    // 内存使用统计
    private final AtomicLong peakMemoryUsage;
    private final AtomicLong totalMemoryAllocated;
    
    public VMStats() {
        this.executionCount = new AtomicLong(0);
        this.totalExecutionTime = new AtomicLong(0);
        this.minExecutionTime = new AtomicLong(Long.MAX_VALUE);
        this.maxExecutionTime = new AtomicLong(0);
        this.errorCount = new AtomicLong(0);
        this.lastErrorTime = new AtomicLong(0);
        this.peakMemoryUsage = new AtomicLong(0);
        this.totalMemoryAllocated = new AtomicLong(0);
    }
    
    /**
     * 记录一次执行
     * @param startTime 执行开始时间（毫秒）
     */
    public void recordExecution(long startTime) {
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        executionCount.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);
        
        // 更新最短时间
        long currentMin = minExecutionTime.get();
        if (executionTime < currentMin) {
            minExecutionTime.compareAndSet(currentMin, executionTime);
        }
        
        // 更新最长时间
        long currentMax = maxExecutionTime.get();
        if (executionTime > currentMax) {
            maxExecutionTime.compareAndSet(currentMax, executionTime);
        }
    }
    
    /**
     * 记录一次错误
     * @param error 错误信息
     */
    public void recordError(Exception error) {
        errorCount.incrementAndGet();
        lastErrorTime.set(System.currentTimeMillis());
    }
    
    /**
     * 记录内存使用
     * @param memoryUsage 内存使用量
     */
    public void recordMemoryUsage(long memoryUsage) {
        long currentPeak = peakMemoryUsage.get();
        if (memoryUsage > currentPeak) {
            peakMemoryUsage.compareAndSet(currentPeak, memoryUsage);
        }
    }
    
    /**
     * 记录内存分配
     * @param allocated 分配的内存量
     */
    public void recordMemoryAllocation(long allocated) {
        totalMemoryAllocated.addAndGet(allocated);
    }
    
    // Getters
    public long getExecutionCount() {
        return executionCount.get();
    }
    
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }
    
    public long getMinExecutionTime() {
        long min = minExecutionTime.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }
    
    public long getMaxExecutionTime() {
        return maxExecutionTime.get();
    }
    
    public long getAverageExecutionTime() {
        long count = executionCount.get();
        if (count == 0) {
            return 0;
        }
        return totalExecutionTime.get() / count;
    }
    
    public long getErrorCount() {
        return errorCount.get();
    }
    
    public long getLastErrorTime() {
        return lastErrorTime.get();
    }
    
    public long getPeakMemoryUsage() {
        return peakMemoryUsage.get();
    }
    
    public long getTotalMemoryAllocated() {
        return totalMemoryAllocated.get();
    }
    
    /**
     * 获取统计摘要
     * @return 格式化的统计摘要字符串
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("VM Statistics:\n");
        sb.append("  Execution Count: ").append(getExecutionCount()).append("\n");
        sb.append("  Total Execution Time: ").append(getTotalExecutionTime()).append("ms\n");
        sb.append("  Average Execution Time: ").append(getAverageExecutionTime()).append("ms\n");
        sb.append("  Min Execution Time: ").append(getMinExecutionTime()).append("ms\n");
        sb.append("  Max Execution Time: ").append(getMaxExecutionTime()).append("ms\n");
        sb.append("  Error Count: ").append(getErrorCount()).append("\n");
        sb.append("  Peak Memory Usage: ").append(getPeakMemoryUsage()).append(" bytes\n");
        sb.append("  Total Memory Allocated: ").append(getTotalMemoryAllocated()).append(" bytes\n");
        return sb.toString();
    }
    
    /**
     * 重置统计信息
     */
    public void reset() {
        executionCount.set(0);
        totalExecutionTime.set(0);
        minExecutionTime.set(Long.MAX_VALUE);
        maxExecutionTime.set(0);
        errorCount.set(0);
        lastErrorTime.set(0);
        peakMemoryUsage.set(0);
        totalMemoryAllocated.set(0);
    }
}