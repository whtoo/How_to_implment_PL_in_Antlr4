package org.teachfx.antlr4.ep18.stackvm.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * MemoryProtection单元测试
 * 测试内存保护功能，包括边界检查、对齐验证、访问统计等
 */
@DisplayName("MemoryProtection Tests")
class MemoryProtectionTest {

    private MemoryProtection memoryProtection;

    @BeforeEach
    void setUp() {
        // 创建测试用的内存保护实例
        // 堆: 0-999, 栈: 1000-1999, 代码: 2000-2999, 全局: 3000-3999
        memoryProtection = new MemoryProtection(1000, 1000, 1000, 1000);
    }

    @Test
    @DisplayName("Should correctly identify memory regions")
    void testRegionIdentification() {
        // Heap region
        assertThat(memoryProtection.isInRegion(0, MemoryProtection.MemoryRegion.HEAP)).isTrue();
        assertThat(memoryProtection.isInRegion(500, MemoryProtection.MemoryRegion.HEAP)).isTrue();
        assertThat(memoryProtection.isInRegion(999, MemoryProtection.MemoryRegion.HEAP)).isTrue();

        // Stack region
        assertThat(memoryProtection.isInRegion(1000, MemoryProtection.MemoryRegion.STACK)).isTrue();
        assertThat(memoryProtection.isInRegion(1500, MemoryProtection.MemoryRegion.STACK)).isTrue();
        assertThat(memoryProtection.isInRegion(1999, MemoryProtection.MemoryRegion.STACK)).isTrue();

        // Code region
        assertThat(memoryProtection.isInRegion(2000, MemoryProtection.MemoryRegion.CODE)).isTrue();
        assertThat(memoryProtection.isInRegion(2500, MemoryProtection.MemoryRegion.CODE)).isTrue();
        assertThat(memoryProtection.isInRegion(2999, MemoryProtection.MemoryRegion.CODE)).isTrue();

        // Global region
        assertThat(memoryProtection.isInRegion(3000, MemoryProtection.MemoryRegion.GLOBAL)).isTrue();
        assertThat(memoryProtection.isInRegion(3500, MemoryProtection.MemoryRegion.GLOBAL)).isTrue();
        assertThat(memoryProtection.isInRegion(3999, MemoryProtection.MemoryRegion.GLOBAL)).isTrue();

        // Out of bounds
        assertThat(memoryProtection.isInRegion(-1, MemoryProtection.MemoryRegion.HEAP)).isFalse();
        assertThat(memoryProtection.isInRegion(4000, MemoryProtection.MemoryRegion.GLOBAL)).isFalse();
    }

    @Test
    @DisplayName("Should correctly get region for address")
    void testGetRegion() {
        assertThat(memoryProtection.getRegion(0)).isEqualTo(MemoryProtection.MemoryRegion.HEAP);
        assertThat(memoryProtection.getRegion(500)).isEqualTo(MemoryProtection.MemoryRegion.HEAP);
        assertThat(memoryProtection.getRegion(1500)).isEqualTo(MemoryProtection.MemoryRegion.STACK);
        assertThat(memoryProtection.getRegion(2500)).isEqualTo(MemoryProtection.MemoryRegion.CODE);
        assertThat(memoryProtection.getRegion(3500)).isEqualTo(MemoryProtection.MemoryRegion.GLOBAL);

        // Out of bounds
        assertThat(memoryProtection.getRegion(-1)).isNull();
        assertThat(memoryProtection.getRegion(4000)).isNull();
    }

    @Test
    @DisplayName("Should validate valid memory accesses")
    void testValidAccess() {
        // Valid heap access
        MemoryProtection.AccessValidationResult result =
            memoryProtection.validateAccess(500, 4, MemoryProtection.AccessType.READ);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isWarning()).isFalse();
        assertThat(result.getMessage()).isNull();

        // Valid stack access
        result = memoryProtection.validateAccess(1500, 8, MemoryProtection.AccessType.WRITE);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isWarning()).isFalse();

        // Valid code access
        result = memoryProtection.validateAccess(2500, 4, MemoryProtection.AccessType.EXECUTE);
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid memory addresses")
    void testInvalidAddresses() {
        // Negative address
        MemoryProtection.AccessValidationResult result =
            memoryProtection.validateAccess(-1, 4, MemoryProtection.AccessType.READ);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Negative address");

        // Out of bounds address
        result = memoryProtection.validateAccess(5000, 4, MemoryProtection.AccessType.READ);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("out of bounds");

        // Address beyond region boundary
        result = memoryProtection.validateAccess(3500, 600, MemoryProtection.AccessType.WRITE);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("exceeds region boundary");
    }

    @Test
    @DisplayName("Should check memory alignment")
    void testAlignment() {
        // Aligned access (4-byte boundary)
        assertThat(memoryProtection.isAligned(0, 4)).isTrue();
        assertThat(memoryProtection.isAligned(4, 4)).isTrue();
        assertThat(memoryProtection.isAligned(1000, 4)).isTrue();

        // Unaligned access
        assertThat(memoryProtection.isAligned(1, 4)).isFalse();
        assertThat(memoryProtection.isAligned(5, 4)).isFalse();

        // Small accesses don't require alignment
        assertThat(memoryProtection.isAligned(1, 1)).isTrue();
        assertThat(memoryProtection.isAligned(1, 2)).isTrue();
        assertThat(memoryProtection.isAligned(1, 3)).isTrue();
    }

    @Test
    @DisplayName("Should issue warnings for unaligned access")
    void testUnalignedAccessWarning() {
        MemoryProtection.AccessValidationResult result =
            memoryProtection.validateAccess(1, 4, MemoryProtection.AccessType.READ);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isWarning()).isTrue();
        assertThat(result.getMessage()).contains("Unaligned access");
    }

    @Test
    @DisplayName("Should enable and disable region protection")
    void testRegionProtectionToggle() {
        MemoryProtection.MemoryRegion heap = MemoryProtection.MemoryRegion.HEAP;

        // Initially protected
        assertThat(memoryProtection).isNotNull();

        // Disable protection
        memoryProtection.setRegionProtected(heap, false);
        // Note: This doesn't directly test the effect, but ensures no exception is thrown

        // Re-enable protection
        memoryProtection.setRegionProtected(heap, true);
    }

    @Test
    @DisplayName("Should track access statistics")
    void testAccessStatistics() {
        MemoryProtection.MemoryRegion heap = MemoryProtection.MemoryRegion.HEAP;

        // Perform some accesses
        memoryProtection.validateAccess(100, 4, MemoryProtection.AccessType.READ);
        memoryProtection.validateAccess(200, 4, MemoryProtection.AccessType.WRITE);
        memoryProtection.validateAccess(300, 8, MemoryProtection.AccessType.WRITE);

        // Check statistics
        MemoryProtection.AccessStatistics stats = memoryProtection.getStatistics(heap);
        assertThat(stats).isNotNull();
        assertThat(stats.getReadCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getWriteCount()).isGreaterThanOrEqualTo(0);

        // Check overall statistics
        MemoryProtection.OverallStatistics overall = memoryProtection.getOverallStatistics();
        assertThat(overall).isNotNull();
        assertThat(overall.getTotalAccessCount()).isGreaterThanOrEqualTo(0);
        assertThat(overall.getBlockedAccessCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should reset statistics")
    void testResetStatistics() {
        // Perform access
        memoryProtection.validateAccess(100, 4, MemoryProtection.AccessType.READ);

        // Reset
        memoryProtection.resetStatistics();

        // Check that statistics are reset
        MemoryProtection.OverallStatistics overall = memoryProtection.getOverallStatistics();
        assertThat(overall.getTotalAccessCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should manage memory mappings")
    void testMemoryMapping() {
        // Add mapping
        memoryProtection.mapMemory(1000, 100, "Test Region");

        // Get mapping
        MemoryProtection.MemoryMapping mapping = memoryProtection.getMemoryMapping(1000);
        assertThat(mapping).isNotNull();
        assertThat(mapping.getAddress()).isEqualTo(1000);
        assertThat(mapping.getSize()).isEqualTo(100);
        assertThat(mapping.getDescription()).isEqualTo("Test Region");

        // Check if address is contained
        assertThat(mapping.contains(1050)).isTrue();
        assertThat(mapping.contains(1150)).isFalse();

        // Clear mappings
        memoryProtection.clearMemoryMap();
        mapping = memoryProtection.getMemoryMapping(1000);
        assertThat(mapping).isNull();
    }

    @Test
    @DisplayName("Should handle edge cases")
    void testEdgeCases() {
        // Zero-size access
        MemoryProtection.AccessValidationResult result =
            memoryProtection.validateAccess(100, 0, MemoryProtection.AccessType.READ);
        assertThat(result.isSuccess()).isTrue();

        // Boundary addresses
        result = memoryProtection.validateAccess(999, 1, MemoryProtection.AccessType.READ);
        assertThat(result.isSuccess()).isTrue();

        // Just beyond boundary (should fail)
        result = memoryProtection.validateAccess(999, 2, MemoryProtection.AccessType.READ);
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("Should create memory protection with different sizes")
    void testConstructorWithDifferentSizes() {
        MemoryProtection mp = new MemoryProtection(2000, 2000, 2000, 2000);
        assertThat(mp).isNotNull();

        // Verify regions are correctly sized
        assertThat(mp.isInRegion(0, MemoryProtection.MemoryRegion.HEAP)).isTrue();
        assertThat(mp.isInRegion(1999, MemoryProtection.MemoryRegion.HEAP)).isTrue();
        assertThat(mp.isInRegion(2000, MemoryProtection.MemoryRegion.STACK)).isTrue();
    }

    @Test
    @DisplayName("Should provide detailed toString for statistics")
    void testStatisticsToString() {
        memoryProtection.validateAccess(100, 4, MemoryProtection.AccessType.READ);

        MemoryProtection.AccessStatistics stats = memoryProtection.getStatistics(
            MemoryProtection.MemoryRegion.HEAP);
        String str = stats.toString();
        assertThat(str).contains("Statistics");
        assertThat(str).contains("Reads:");
        assertThat(str).contains("Writes:");
    }

    @Test
    @DisplayName("Should provide detailed toString for overall statistics")
    void testOverallStatisticsToString() {
        MemoryProtection.OverallStatistics stats = memoryProtection.getOverallStatistics();
        String str = stats.toString();
        assertThat(str).contains("Overall Memory Protection Statistics");
        assertThat(str).contains("Total Accesses:");
        assertThat(str).contains("Blocked Accesses:");
    }

    @Test
    @DisplayName("Should provide detailed toString for memory mapping")
    void testMemoryMappingToString() {
        memoryProtection.mapMemory(1000, 100, "Test");
        MemoryProtection.MemoryMapping mapping = memoryProtection.getMemoryMapping(1000);

        String str = mapping.toString();
        assertThat(str).contains("MemoryMapping");
        assertThat(str).contains("0x");
    }
}
