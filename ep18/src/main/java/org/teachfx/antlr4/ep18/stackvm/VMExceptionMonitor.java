package org.teachfx.antlr4.ep18.stackvm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * VMExceptionMonitor - 虚拟机异常监控器
 * 提供异常统计、模式分析、趋势监控和预警功能
 */
public class VMExceptionMonitor {
    private final Map<Class<? extends VMException>, ExceptionTypeStats> exceptionStats;
    private final List<VMException> recentExceptions;
    private final AtomicLong totalExceptionCount;
    private final AtomicLong handledExceptionCount;
    private final DoubleAdder exceptionProcessingTime;
    private final int maxRecentExceptions;
    
    // 异常模式检测阈值
    private static final double HIGH_EXCEPTION_RATE_THRESHOLD = 0.05; // 5%
    private static final int RECENT_TIME_WINDOW_SECONDS = 60; // 1分钟
    private static final int PATTERN_DETECTION_THRESHOLD = 3; // 相同异常发生3次就认为可能是模式

    public VMExceptionMonitor() {
        this(1000); // 默认保留最近1000个异常
    }

    public VMExceptionMonitor(int maxRecentExceptions) {
        this.exceptionStats = new ConcurrentHashMap<>();
        this.recentExceptions = new CopyOnWriteArrayList<>();
        this.totalExceptionCount = new AtomicLong(0);
        this.handledExceptionCount = new AtomicLong(0);
        this.exceptionProcessingTime = new DoubleAdder();
        this.maxRecentExceptions = maxRecentExceptions;
    }

    /**
     * 记录异常发生
     * @param exception 发生的异常
     * @param handled 是否被成功处理
     * @param processingTime 处理耗时（毫秒）
     */
    public void recordException(VMException exception, boolean handled, long processingTime) {
        totalExceptionCount.incrementAndGet();
        
        if (handled) {
            handledExceptionCount.incrementAndGet();
        }
        
        if (processingTime > 0) {
            exceptionProcessingTime.add(processingTime);
        }

        // 记录异常统计
        ExceptionTypeStats stats = exceptionStats.computeIfAbsent(
            exception.getClass(), k -> new ExceptionTypeStats(k)
        );
        stats.recordOccurrence(handled, processingTime);

        // 添加到最近异常列表
        recentExceptions.add(exception);
        if (recentExceptions.size() > maxRecentExceptions) {
            recentExceptions.remove(0);
        }

        // 检测异常模式
        detectExceptionPatterns(exception);
    }

    /**
     * 记录异常发生（简化版本）
     */
    public void recordException(VMException exception) {
        recordException(exception, false, 0);
    }

    /**
     * 检测异常模式
     */
    private void detectExceptionPatterns(VMException exception) {
        ExceptionTypeStats stats = exceptionStats.get(exception.getClass());
        if (stats == null) {
            return;
        }

        // 检查异常率是否过高
        double recentRate = stats.getRecentRate();
        if (recentRate > HIGH_EXCEPTION_RATE_THRESHOLD) {
            triggerAlert("High exception rate for " + exception.getClass().getSimpleName() + 
                        ": " + String.format("%.2f%%", recentRate * 100));
        }

        // 检查是否频繁发生相同异常
        long recentCount = stats.getRecentCount();
        if (recentCount >= PATTERN_DETECTION_THRESHOLD) {
            // 检查这些异常是否有相似的特征
            List<VMException> similarExceptions = findSimilarExceptions(exception);
            if (similarExceptions.size() >= PATTERN_DETECTION_THRESHOLD) {
                triggerAlert("Detected pattern of " + exception.getClass().getSimpleName() + 
                            " with " + similarExceptions.size() + " similar occurrences");
            }
        }
    }

    /**
     * 查找相似的异常
     */
    private List<VMException> findSimilarExceptions(VMException target) {
        List<VMException> similar = new ArrayList<>();
        
        for (VMException exception : recentExceptions) {
            if (exception.getClass().equals(target.getClass()) && 
                isSimilarException(exception, target)) {
                similar.add(exception);
            }
        }
        
        return similar;
    }

    /**
     * 判断两个异常是否相似
     */
    private boolean isSimilarException(VMException e1, VMException e2) {
        if (!e1.getClass().equals(e2.getClass())) {
            return false;
        }

        // 比较程序计数器（如果相近则认为相似）
        if (Math.abs(e1.getPC() - e2.getPC()) > 10) {
            return false;
        }

        // 比较指令（如果相同则认为相似）
        if (e1.getInstruction() != null && e2.getInstruction() != null &&
            !e1.getInstruction().equals(e2.getInstruction())) {
            return false;
        }

        return true;
    }

    /**
     * 触发预警
     */
    private void triggerAlert(String message) {
        // 在实际实现中，这里可以发送邮件、短信、调用webhook等
        System.err.println("[VM Exception Alert] " + new Date() + ": " + message);
    }

    /**
     * 获取异常统计信息
     */
    public VMExceptionStatistics getStatistics() {
        return new VMExceptionStatistics(
            new HashMap<>(exceptionStats),
            totalExceptionCount.get(),
            handledExceptionCount.get(),
            new ArrayList<>(recentExceptions),
            exceptionProcessingTime.sum()
        );
    }

    /**
     * 获取指定类型的异常统计
     */
    public ExceptionTypeStats getExceptionStats(Class<? extends VMException> exceptionType) {
        return exceptionStats.get(exceptionType);
    }

    /**
     * 获取总的异常数量
     */
    public long getTotalExceptionCount() {
        return totalExceptionCount.get();
    }

    /**
     * 获取已处理的异常数量
     */
    public long getHandledExceptionCount() {
        return handledExceptionCount.get();
    }

    /**
     * 获取异常处理率
     */
    public double getHandledRate() {
        long total = totalExceptionCount.get();
        return total > 0 ? (double) handledExceptionCount.get() / total : 0.0;
    }

    /**
     * 获取指定类型的异常数量
     */
    public long getExceptionCount(Class<? extends VMException> exceptionType) {
        VMExceptionMonitor.ExceptionTypeStats stats = exceptionStats.get(exceptionType);
        return stats != null ? stats.getTotalCount() : 0;
    }

    /**
     * 获取平均异常处理时间
     */
    public double getAverageProcessingTime() {
        long total = totalExceptionCount.get();
        return total > 0 ? exceptionProcessingTime.sum() / total : 0.0;
    }

    /**
     * 获取最近的异常
     */
    public List<VMException> getRecentExceptions(int count) {
        int size = recentExceptions.size();
        if (count >= size) {
            return new ArrayList<>(recentExceptions);
        }
        return new ArrayList<>(recentExceptions.subList(size - count, size));
    }

    /**
     * 清理过期的统计数据
     */
    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        for (ExceptionTypeStats stats : exceptionStats.values()) {
            stats.cleanup(currentTime);
        }
    }

    /**
     * 重置所有统计数据
     */
    public void reset() {
        exceptionStats.clear();
        recentExceptions.clear();
        totalExceptionCount.set(0);
        handledExceptionCount.set(0);
        exceptionProcessingTime.reset();
    }

    /**
     * 异常类型统计信息
     */
    public static class ExceptionTypeStats {
        private final Class<? extends VMException> exceptionType;
        private final AtomicLong totalCount;
        private final AtomicLong handledCount;
        private final DoubleAdder totalProcessingTime;
        private final List<OccurrenceRecord> recentOccurrences;

        public ExceptionTypeStats(Class<? extends VMException> exceptionType) {
            this.exceptionType = exceptionType;
            this.totalCount = new AtomicLong(0);
            this.handledCount = new AtomicLong(0);
            this.totalProcessingTime = new DoubleAdder();
            this.recentOccurrences = new CopyOnWriteArrayList<>();
        }

        public void recordOccurrence(boolean handled, long processingTime) {
            totalCount.incrementAndGet();
            if (handled) {
                handledCount.incrementAndGet();
            }
            if (processingTime > 0) {
                totalProcessingTime.add(processingTime);
            }
            recentOccurrences.add(new OccurrenceRecord(System.currentTimeMillis(), handled, processingTime));
        }

        public void recordOccurrence() {
            recordOccurrence(false, 0);
        }

        public long getTotalCount() {
            return totalCount.get();
        }

        public long getHandledCount() {
            return handledCount.get();
        }

        public double getHandledRate() {
            long total = totalCount.get();
            return total > 0 ? (double) handledCount.get() / total : 0.0;
        }

        public double getAverageProcessingTime() {
            long total = totalCount.get();
            return total > 0 ? totalProcessingTime.sum() / total : 0.0;
        }

        public long getRecentCount() {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - (RECENT_TIME_WINDOW_SECONDS * 1000);
            
            return recentOccurrences.stream()
                    .filter(occ -> occ.timestamp >= windowStart)
                    .count();
        }

        public double getRecentRate() {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - (RECENT_TIME_WINDOW_SECONDS * 1000);
            
            long recentCount = recentOccurrences.stream()
                    .filter(occ -> occ.timestamp >= windowStart)
                    .count();
            
            // 计算每分钟异常率
            return (double) recentCount / RECENT_TIME_WINDOW_SECONDS * 60;
        }

        public void cleanup(long currentTime) {
            long cutoffTime = currentTime - (RECENT_TIME_WINDOW_SECONDS * 1000 * 10); // 保留10分钟的数据
            recentOccurrences.removeIf(occ -> occ.timestamp < cutoffTime);
        }

        private static class OccurrenceRecord {
            final long timestamp;
            final boolean handled;
            final long processingTime;

            OccurrenceRecord(long timestamp, boolean handled, long processingTime) {
                this.timestamp = timestamp;
                this.handled = handled;
                this.processingTime = processingTime;
            }
        }
    }
}