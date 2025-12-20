package org.teachfx.antlr4.ep18.stackvm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * VMExceptionStatistics - 异常统计信息
 * 封装异常监控的统计结果
 */
public class VMExceptionStatistics {
    private final Map<Class<? extends VMException>, VMExceptionMonitor.ExceptionTypeStats> exceptionStats;
    private final long totalExceptionCount;
    private final long handledExceptionCount;
    private final List<VMException> recentExceptions;
    private final double totalProcessingTime;

    public VMExceptionStatistics(
            Map<Class<? extends VMException>, VMExceptionMonitor.ExceptionTypeStats> exceptionStats,
            long totalExceptionCount,
            long handledExceptionCount,
            List<VMException> recentExceptions,
            double totalProcessingTime) {
        this.exceptionStats = new HashMap<>(exceptionStats);
        this.totalExceptionCount = totalExceptionCount;
        this.handledExceptionCount = handledExceptionCount;
        this.recentExceptions = new ArrayList<>(recentExceptions);
        this.totalProcessingTime = totalProcessingTime;
    }

    public Map<Class<? extends VMException>, VMExceptionMonitor.ExceptionTypeStats> getExceptionStats() {
        return new HashMap<>(exceptionStats);
    }

    public long getTotalExceptionCount() {
        return totalExceptionCount;
    }

    public long getHandledExceptionCount() {
        return handledExceptionCount;
    }

    public double getHandledRate() {
        return totalExceptionCount > 0 ? (double) handledExceptionCount / totalExceptionCount : 0.0;
    }

    public List<VMException> getRecentExceptions() {
        return new ArrayList<>(recentExceptions);
    }

    public double getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public double getAverageProcessingTime() {
        return totalExceptionCount > 0 ? totalProcessingTime / totalExceptionCount : 0.0;
    }

    public long getExceptionCount(Class<? extends VMException> exceptionType) {
        VMExceptionMonitor.ExceptionTypeStats stats = exceptionStats.get(exceptionType);
        return stats != null ? stats.getTotalCount() : 0;
    }

    public double getExceptionRate(Class<? extends VMException> exceptionType) {
        long count = getExceptionCount(exceptionType);
        return totalExceptionCount > 0 ? (double) count / totalExceptionCount : 0.0;
    }

    public Class<? extends VMException> getMostFrequentException() {
        return exceptionStats.entrySet().stream()
                .max(Comparator.comparingLong(e -> e.getValue().getTotalCount()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public Class<? extends VMException> getMostFrequentException(int limit) {
        return getRecentExceptions().stream()
                .limit(limit)
                .collect(Collectors.groupingBy(e -> e.getClass(), Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparingLong(entry -> entry.getValue()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public List<Class<? extends VMException>> getExceptionTypes() {
        return new ArrayList<>(exceptionStats.keySet());
    }

    public double getExceptionRate() {
        // 这里需要获取总的指令执行数量来计算异常率
        // 暂时返回基于异常处理时间的比率
        return totalExceptionCount > 0 ? totalProcessingTime / 1000.0 : 0.0;
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalExceptions", totalExceptionCount);
        summary.put("handledExceptions", handledExceptionCount);
        summary.put("handledRate", getHandledRate());
        summary.put("averageProcessingTime", getAverageProcessingTime());
        summary.put("exceptionTypes", exceptionStats.size());
        summary.put("mostFrequentException", getMostFrequentException());
        
        return summary;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VM Exception Statistics:\n");
        sb.append("  Total Exceptions: ").append(totalExceptionCount).append("\n");
        sb.append("  Handled Exceptions: ").append(handledExceptionCount);
        sb.append(" (").append(String.format("%.2f%%", getHandledRate() * 100)).append(")\n");
        sb.append("  Exception Types: ").append(exceptionStats.size()).append("\n");
        sb.append("  Average Processing Time: ").append(String.format("%.3f", getAverageProcessingTime())).append(" ms\n");
        
        if (!exceptionStats.isEmpty()) {
            sb.append("  Exception Breakdown:\n");
            exceptionStats.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue().getTotalCount(), e1.getValue().getTotalCount()))
                    .forEach(entry -> {
                        Class<? extends VMException> type = entry.getKey();
                        VMExceptionMonitor.ExceptionTypeStats stats = entry.getValue();
                        sb.append("    ").append(type.getSimpleName()).append(": ")
                          .append(stats.getTotalCount()).append(" (")
                          .append(String.format("%.2f%%", getExceptionRate(type) * 100)).append(")\n");
                    });
        }
        
        return sb.toString();
    }
}