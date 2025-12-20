package org.teachfx.antlr4.ep18.stackvm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * VMExceptionMonitor 测试类
 */
class VMExceptionMonitorTest {

    private VMExceptionMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new VMExceptionMonitor();
    }

    @Test
    @DisplayName("Should record exception occurrences")
    void testRecordException() {
        // Given
        VMOverflowException exception = new VMOverflowException("Overflow", 10, "iadd");

        // When
        monitor.recordException(exception);

        // Then
        assertThat(monitor.getTotalExceptionCount()).isEqualTo(1);
        assertThat(monitor.getHandledExceptionCount()).isEqualTo(0);
        assertThat(monitor.getHandledRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should record handled exceptions")
    void testRecordHandledException() {
        // Given
        VMOverflowException exception = new VMOverflowException("Overflow", 10, "iadd");

        // When
        monitor.recordException(exception, true, 5); // 已处理，耗时5ms

        // Then
        assertThat(monitor.getTotalExceptionCount()).isEqualTo(1);
        assertThat(monitor.getHandledExceptionCount()).isEqualTo(1);
        assertThat(monitor.getHandledRate()).isEqualTo(1.0);
        assertThat(monitor.getAverageProcessingTime()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Should track exception statistics by type")
    void testExceptionTypeStatistics() {
        // Given
        VMOverflowException overflow1 = new VMOverflowException("Overflow1", 10, "iadd");
        VMOverflowException overflow2 = new VMOverflowException("Overflow2", 15, "isub");
        VMDivisionByZeroException divException = new VMDivisionByZeroException(20, "idiv");

        // When
        monitor.recordException(overflow1, true, 2);
        monitor.recordException(overflow2, false, 3);
        monitor.recordException(divException, false, 1);

        // Then
        assertThat(monitor.getExceptionCount(VMOverflowException.class)).isEqualTo(2);
        assertThat(monitor.getExceptionCount(VMDivisionByZeroException.class)).isEqualTo(1);
        
        VMExceptionMonitor.ExceptionTypeStats overflowStats = monitor.getExceptionStats(VMOverflowException.class);
        assertThat(overflowStats).isNotNull();
        assertThat(overflowStats.getTotalCount()).isEqualTo(2);
        assertThat(overflowStats.getHandledCount()).isEqualTo(1);
        assertThat(overflowStats.getHandledRate()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("Should get recent exceptions")
    void testRecentExceptions() {
        // Given
        for (int i = 0; i < 10; i++) {
            VMException exception = new VMOverflowException("Overflow" + i, i, "iadd");
            monitor.recordException(exception);
        }

        // When
        List<VMException> recent = monitor.getRecentExceptions(5);

        // Then
        assertThat(recent).hasSize(5);
        // 应该获取最后5个异常
        for (int i = 0; i < 5; i++) {
            VMException exception = recent.get(i);
            assertThat(exception).isInstanceOf(VMOverflowException.class);
            assertThat(exception.getPC()).isEqualTo(5 + i); // PC应该是5, 6, 7, 8, 9
        }
    }

    @Test
    @DisplayName("Should get exception statistics")
    void testExceptionStatistics() {
        // Given
        VMOverflowException exception1 = new VMOverflowException("Overflow1", 10, "iadd");
        VMOverflowException exception2 = new VMOverflowException("Overflow2", 15, "isub");
        VMDivisionByZeroException exception3 = new VMDivisionByZeroException(20, "idiv");

        monitor.recordException(exception1, true, 2);
        monitor.recordException(exception2, false, 3);
        monitor.recordException(exception3, true, 1);

        // When
        VMExceptionStatistics stats = monitor.getStatistics();

        // Then
        assertThat(stats.getTotalExceptionCount()).isEqualTo(3);
        assertThat(stats.getHandledExceptionCount()).isEqualTo(2);
        assertThat(stats.getHandledRate()).isEqualTo(2.0 / 3.0);
        assertThat(stats.getMostFrequentException()).isEqualTo(VMOverflowException.class);
        assertThat(stats.getExceptionCount(VMOverflowException.class)).isEqualTo(2);
        assertThat(stats.getExceptionCount(VMDivisionByZeroException.class)).isEqualTo(1);
    }

    @Test
    @DisplayName("Should reset statistics")
    void testReset() {
        // Given
        VMException exception = new VMOverflowException("Overflow", 10, "iadd");
        monitor.recordException(exception, true, 5);
        assertThat(monitor.getTotalExceptionCount()).isEqualTo(1);

        // When
        monitor.reset();

        // Then
        assertThat(monitor.getTotalExceptionCount()).isEqualTo(0);
        assertThat(monitor.getHandledExceptionCount()).isEqualTo(0);
        assertThat(monitor.getRecentExceptions(10)).isEmpty();
    }

    @Test
    @DisplayName("Should detect high exception rates")
    void testHighExceptionRateDetection() {
        // Given - 短时间内产生大量相同类型的异常
        for (int i = 0; i < 10; i++) {
            VMOverflowException exception = new VMOverflowException("Overflow" + i, i, "iadd");
            monitor.recordException(exception);
        }

        // When - 获取统计信息
        VMExceptionMonitor.ExceptionTypeStats stats = monitor.getExceptionStats(VMOverflowException.class);

        // Then - 应该检测到高异常率（这里只是验证统计功能，实际预警需要在detectExceptionPatterns中实现）
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalCount()).isEqualTo(10);
        assertThat(stats.getRecentCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should handle different exception types correctly")
    void testDifferentExceptionTypes() {
        // Given
        VMOverflowException overflow = new VMOverflowException("Overflow", 10, "iadd");
        VMDivisionByZeroException divByZero = new VMDivisionByZeroException(15, "idiv");
        VMStackUnderflowException underflow = new VMStackUnderflowException(20, "pop");
        VMMemoryAccessException memoryAccess = new VMMemoryAccessException(25, "load", 0x1000, VMMemoryException.MemoryAccessType.READ);

        // When
        monitor.recordException(overflow, true, 1);
        monitor.recordException(divByZero, false, 2);
        monitor.recordException(underflow, true, 3);
        monitor.recordException(memoryAccess, false, 4);

        // Then
        assertThat(monitor.getTotalExceptionCount()).isEqualTo(4);
        assertThat(monitor.getHandledExceptionCount()).isEqualTo(2);
        assertThat(monitor.getHandledRate()).isEqualTo(0.5);
        
        // 验证各种类型的统计
        assertThat(monitor.getExceptionCount(VMOverflowException.class)).isEqualTo(1);
        assertThat(monitor.getExceptionCount(VMDivisionByZeroException.class)).isEqualTo(1);
        assertThat(monitor.getExceptionCount(VMStackUnderflowException.class)).isEqualTo(1);
        assertThat(monitor.getExceptionCount(VMMemoryAccessException.class)).isEqualTo(1);
    }
}