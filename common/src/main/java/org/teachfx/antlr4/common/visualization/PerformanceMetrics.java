package org.teachfx.antlr4.common.visualization;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能指标类
 * 
 * <p>该类提供虚拟机性能的统一度量，包括执行时间、内存使用、
 * 指令统计等关键性能指标。支持线程安全的并发访问。</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段一
 */
public class PerformanceMetrics {
    
    // ==================== 时间指标 ====================
    
    /**
     * 总执行时间（毫秒）
     */
    private final AtomicLong totalExecutionTime;
    
    /**
     * 平均执行时间（毫秒）
     */
    private volatile double averageExecutionTime;
    
    /**
     * 最短执行时间（毫秒）
     */
    private volatile long minExecutionTime;
    
    /**
     * 最长执行时间（毫秒）
     */
    private volatile long maxExecutionTime;
    
    // ==================== 指令统计 ====================
    
    /**
     * 执行的指令总数
     */
    private final AtomicLong totalInstructions;
    
    /**
     * 每秒平均指令数（IPS）
     */
    private volatile double instructionsPerSecond;
    
    /**
     * 函数调用次数
     */
    private final AtomicLong functionCalls;
    
    /**
     * 分支指令次数
     */
    private final AtomicLong branchInstructions;
    
    /**
     * 分支预测准确率
     */
    private volatile double branchPredictionAccuracy;
    
    // ==================== 内存指标 ====================
    
    /**
     * 峰值内存使用量（字节）
     */
    private final AtomicLong peakMemoryUsage;
    
    /**
     * 当前内存使用量（字节）
     */
    private volatile long currentMemoryUsage;
    
    /**
     * 总内存分配量（字节）
     */
    private final AtomicLong totalMemoryAllocated;
    
    /**
     * 垃圾回收次数
     */
    private final AtomicLong garbageCollectionCount;
    
    /**
     * 垃圾回收总时间（毫秒）
     */
    private final AtomicLong garbageCollectionTime;
    
    // ==================== 其他指标 ====================
    
    /**
     * 错误次数
     */
    private final AtomicLong errorCount;
    
    /**
     * 警告次数
     */
    private final AtomicLong warningCount;
    
    /**
     * 优化命中次数
     */
    private final AtomicLong optimizationHits;
    
    /**
     * 缓存命中率
     */
    private volatile double cacheHitRate;
    
    /**
     * VM类型标识
     */
    private final String vmType;
    
    /**
     * 创建时间戳
     */
    private final long creationTime;
    
    /**
     * 构造函数
     * 
     * @param vmType 虚拟机类型标识
     */
    public PerformanceMetrics(String vmType) {
        this.vmType = vmType != null ? vmType : "Unknown";
        this.creationTime = System.currentTimeMillis();
        
        // 初始化原子变量
        this.totalExecutionTime = new AtomicLong(0);
        this.totalInstructions = new AtomicLong(0);
        this.functionCalls = new AtomicLong(0);
        this.branchInstructions = new AtomicLong(0);
        this.peakMemoryUsage = new AtomicLong(0);
        this.totalMemoryAllocated = new AtomicLong(0);
        this.garbageCollectionCount = new AtomicLong(0);
        this.garbageCollectionTime = new AtomicLong(0);
        this.errorCount = new AtomicLong(0);
        this.warningCount = new AtomicLong(0);
        this.optimizationHits = new AtomicLong(0);
        
        // 初始化volatile变量
        this.averageExecutionTime = 0.0;
        this.minExecutionTime = Long.MAX_VALUE;
        this.maxExecutionTime = 0;
        this.instructionsPerSecond = 0.0;
        this.branchPredictionAccuracy = 0.0;
        this.currentMemoryUsage = 0;
        this.cacheHitRate = 0.0;
    }
    
    // ==================== 时间指标方法 ====================
    
    /**
     * 记录执行时间
     * 
     * @param executionTime 执行时间（毫秒）
     */
    public void recordExecutionTime(long executionTime) {
        totalExecutionTime.addAndGet(executionTime);
        
        // 更新最短和最长时间
        if (executionTime < minExecutionTime) {
            minExecutionTime = executionTime;
        }
        if (executionTime > maxExecutionTime) {
            maxExecutionTime = executionTime;
        }
        
        updateAverageExecutionTime();
    }
    
    /**
     * 获取总执行时间
     * 
     * @return 总执行时间（毫秒）
     */
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }
    
    /**
     * 获取平均执行时间
     * 
     * @return 平均执行时间（毫秒）
     */
    public double getAverageExecutionTime() {
        return averageExecutionTime;
    }
    
    /**
     * 获取最短执行时间
     * 
     * @return 最短执行时间（毫秒）
     */
    public long getMinExecutionTime() {
        return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
    }
    
    /**
     * 获取最长执行时间
     * 
     * @return 最长执行时间（毫秒）
     */
    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }
    
    /**
     * 更新平均执行时间
     */
    private void updateAverageExecutionTime() {
        long total = totalExecutionTime.get();
        long count = totalInstructions.get();
        if (count > 0) {
            averageExecutionTime = (double) total / count;
        }
    }
    
    // ==================== 指令统计方法 ====================
    
    /**
     * 增加指令计数
     * 
     * @param count 增加的指令数
     */
    public void addInstructions(long count) {
        totalInstructions.addAndGet(count);
        updateInstructionsPerSecond();
        updateAverageExecutionTime();
    }
    
    /**
     * 增加函数调用计数
     * 
     * @param count 增加的调用数
     */
    public void addFunctionCalls(long count) {
        functionCalls.addAndGet(count);
    }
    
    /**
     * 增加分支指令计数
     * 
     * @param count 增加的分支指令数
     */
    public void addBranchInstructions(long count) {
        branchInstructions.addAndGet(count);
    }
    
    /**
     * 设置分支预测准确率
     * 
     * @param accuracy 准确率（0.0-1.0）
     */
    public void setBranchPredictionAccuracy(double accuracy) {
        this.branchPredictionAccuracy = Math.max(0.0, Math.min(1.0, accuracy));
    }
    
    /**
     * 获取总指令数
     * 
     * @return 总指令数
     */
    public long getTotalInstructions() {
        return totalInstructions.get();
    }
    
    /**
     * 获取每秒指令数
     * 
     * @return 每秒指令数
     */
    public double getInstructionsPerSecond() {
        return instructionsPerSecond;
    }
    
    /**
     * 获取函数调用次数
     * 
     * @return 函数调用次数
     */
    public long getFunctionCalls() {
        return functionCalls.get();
    }
    
    /**
     * 获取分支指令次数
     * 
     * @return 分支指令次数
     */
    public long getBranchInstructions() {
        return branchInstructions.get();
    }
    
    /**
     * 获取分支预测准确率
     * 
     * @return 分支预测准确率
     */
    public double getBranchPredictionAccuracy() {
        return branchPredictionAccuracy;
    }
    
    /**
     * 更新每秒指令数
     */
    private void updateInstructionsPerSecond() {
        long elapsed = System.currentTimeMillis() - creationTime;
        if (elapsed > 0) {
            instructionsPerSecond = (double) totalInstructions.get() * 1000.0 / elapsed;
        }
    }
    
    // ==================== 内存指标方法 ====================
    
    /**
     * 记录内存使用
     * 
     * @param memoryUsage 当前内存使用量（字节）
     */
    public void recordMemoryUsage(long memoryUsage) {
        this.currentMemoryUsage = memoryUsage;
        long currentPeak = peakMemoryUsage.get();
        if (memoryUsage > currentPeak) {
            peakMemoryUsage.compareAndSet(currentPeak, memoryUsage);
        }
    }
    
    /**
     * 记录内存分配
     * 
     * @param allocated 分配的内存量（字节）
     */
    public void recordMemoryAllocation(long allocated) {
        totalMemoryAllocated.addAndGet(allocated);
    }
    
    /**
     * 记录垃圾回收
     * 
     * @param gcTime 垃圾回收时间（毫秒）
     */
    public void recordGarbageCollection(long gcTime) {
        garbageCollectionCount.incrementAndGet();
        garbageCollectionTime.addAndGet(gcTime);
    }
    
    /**
     * 获取峰值内存使用量
     * 
     * @return 峰值内存使用量（字节）
     */
    public long getPeakMemoryUsage() {
        return peakMemoryUsage.get();
    }
    
    /**
     * 获取当前内存使用量
     * 
     * @return 当前内存使用量（字节）
     */
    public long getCurrentMemoryUsage() {
        return currentMemoryUsage;
    }
    
    /**
     * 获取总内存分配量
     * 
     * @return 总内存分配量（字节）
     */
    public long getTotalMemoryAllocated() {
        return totalMemoryAllocated.get();
    }
    
    /**
     * 获取垃圾回收次数
     * 
     * @return 垃圾回收次数
     */
    public long getGarbageCollectionCount() {
        return garbageCollectionCount.get();
    }
    
    /**
     * 获取垃圾回收总时间
     * 
     * @return 垃圾回收总时间（毫秒）
     */
    public long getGarbageCollectionTime() {
        return garbageCollectionTime.get();
    }
    
    // ==================== 其他指标方法 ====================
    
    /**
     * 增加错误计数
     * 
     * @param count 增加的错误数
     */
    public void addErrors(long count) {
        errorCount.addAndGet(count);
    }
    
    /**
     * 增加警告计数
     * 
     * @param count 增加的警告数
     */
    public void addWarnings(long count) {
        warningCount.addAndGet(count);
    }
    
    /**
     * 增加优化命中计数
     * 
     * @param count 增加的命中数
     */
    public void addOptimizationHits(long count) {
        optimizationHits.addAndGet(count);
    }
    
    /**
     * 设置缓存命中率
     * 
     * @param hitRate 命中率（0.0-1.0）
     */
    public void setCacheHitRate(double hitRate) {
        this.cacheHitRate = Math.max(0.0, Math.min(1.0, hitRate));
    }
    
    /**
     * 获取错误次数
     * 
     * @return 错误次数
     */
    public long getErrorCount() {
        return errorCount.get();
    }
    
    /**
     * 获取警告次数
     * 
     * @return 警告次数
     */
    public long getWarningCount() {
        return warningCount.get();
    }
    
    /**
     * 获取优化命中次数
     * 
     * @return 优化命中次数
     */
    public long getOptimizationHits() {
        return optimizationHits.get();
    }
    
    /**
     * 获取缓存命中率
     * 
     * @return 缓存命中率
     */
    public double getCacheHitRate() {
        return cacheHitRate;
    }
    
    /**
     * 获取VM类型
     * 
     * @return VM类型标识
     */
    public String getVmType() {
        return vmType;
    }
    
    /**
     * 获取创建时间
     * 
     * @return 创建时间戳（毫秒）
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * 生成性能报告
     * 
     * @return 格式化的性能报告字符串
     */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Performance Metrics Report ===\n");
        sb.append("VM Type: ").append(vmType).append("\n");
        sb.append("Creation Time: ").append(new java.util.Date(creationTime)).append("\n\n");
        
        sb.append("Timing Metrics:\n");
        sb.append("  Total Execution Time: ").append(getTotalExecutionTime()).append(" ms\n");
        sb.append("  Average Execution Time: ").append(String.format("%.2f", getAverageExecutionTime())).append(" ms\n");
        sb.append("  Min Execution Time: ").append(getMinExecutionTime()).append(" ms\n");
        sb.append("  Max Execution Time: ").append(getMaxExecutionTime()).append(" ms\n\n");
        
        sb.append("Instruction Statistics:\n");
        sb.append("  Total Instructions: ").append(getTotalInstructions()).append("\n");
        sb.append("  Instructions per Second: ").append(String.format("%.2f", getInstructionsPerSecond())).append("\n");
        sb.append("  Function Calls: ").append(getFunctionCalls()).append("\n");
        sb.append("  Branch Instructions: ").append(getBranchInstructions()).append("\n");
        sb.append("  Branch Prediction Accuracy: ").append(String.format("%.2f%%", getBranchPredictionAccuracy() * 100)).append("\n\n");
        
        sb.append("Memory Metrics:\n");
        sb.append("  Peak Memory Usage: ").append(getPeakMemoryUsage()).append(" bytes\n");
        sb.append("  Current Memory Usage: ").append(getCurrentMemoryUsage()).append(" bytes\n");
        sb.append("  Total Memory Allocated: ").append(getTotalMemoryAllocated()).append(" bytes\n");
        sb.append("  GC Count: ").append(getGarbageCollectionCount()).append("\n");
        sb.append("  GC Total Time: ").append(getGarbageCollectionTime()).append(" ms\n\n");
        
        sb.append("Other Metrics:\n");
        sb.append("  Error Count: ").append(getErrorCount()).append("\n");
        sb.append("  Warning Count: ").append(getWarningCount()).append("\n");
        sb.append("  Optimization Hits: ").append(getOptimizationHits()).append("\n");
        sb.append("  Cache Hit Rate: ").append(String.format("%.2f%%", getCacheHitRate() * 100)).append("\n");
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format(
            "PerformanceMetrics{vmType='%s', instructions=%d, execTime=%dms, memory=%dB}",
            vmType, getTotalInstructions(), getTotalExecutionTime(), getPeakMemoryUsage()
        );
    }
}