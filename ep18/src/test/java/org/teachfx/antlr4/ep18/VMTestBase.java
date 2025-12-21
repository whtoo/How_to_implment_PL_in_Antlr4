package org.teachfx.antlr4.ep18;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.teachfx.antlr4.ep18.stackvm.CymbolStackVM;
import org.teachfx.antlr4.ep18.stackvm.VMConfig;

/**
 * VMTestBase - Enhanced Test Base for EP18 TDD Refactoring
 *
 * Provides unified test environment, utilities, and assertions for VM testing.
 * This is the foundation for all VM-related tests in the TDD refactoring effort.
 *
 * @author EP18 TDD Refactoring Team
 * @version 2.0.0 - Enhanced for TDD
 */
public abstract class VMTestBase {

    /** The VM instance under test */
    protected CymbolStackVM vm;

    /** Test configuration with optimized settings for testing */
    protected VMConfig testConfig;

    /** Performance metrics for benchmarking tests */
    protected TestMetrics testMetrics;

    /**
     * Set up the test environment before each test
     */
    @BeforeEach
    void setUpVMTest() {
        // Create test configuration optimized for testing
        testConfig = createTestConfig();

        // Create VM instance
        vm = new CymbolStackVM(testConfig);

        // Initialize performance metrics
        testMetrics = new TestMetrics();
    }

    /**
     * Clean up after each test
     */
    @AfterEach
    void tearDownVMTest() {
        if (vm != null) {
            // Reset VM state if needed
            vm = null;
        }
        testMetrics = null;
    }

    /**
     * Create test configuration optimized for testing
     * Enables debugging, verbose errors, and safety checks
     *
     * @return test configuration
     */
    protected VMConfig createTestConfig() {
        return new VMConfig.Builder()
            .setHeapSize(1024 * 1024) // 1MB for tests
            .setStackSize(1024)
            .setMaxStackDepth(100)
            .setDebugMode(true)
            .setVerboseErrors(true)
            .setEnableBoundsCheck(true)
            .setEnableTypeCheck(true)
            .build();
    }

    /**
     * Execute bytecode and return result
     *
     * @param bytecode the bytecode to execute
     * @return execution result
     * @throws Exception if execution fails
     */
    protected int execute(byte[] bytecode) throws Exception {
        long startTime = System.nanoTime();

        int result = vm.execute(bytecode);

        long endTime = System.nanoTime();
        if (testMetrics != null) {
            testMetrics.recordExecution(endTime - startTime);
        }

        return result;
    }

    /**
     * Execute bytecode from a test program string
     *
     * @param program the VM program string
     * @return execution result
     * @throws Exception if execution fails
     */
    protected int executeProgram(String program) throws Exception {
        // Convert program string to bytecode
        byte[] bytecode = parseProgramToBytecode(program);
        return execute(bytecode);
    }

    /**
     * Parse VM program string to bytecode
     * This is a placeholder - actual implementation would parse the program
     *
     * @param program the VM program string
     * @return bytecode array
     */
    protected byte[] parseProgramToBytecode(String program) {
        // TODO: Implement actual parsing
        // For now, return empty bytecode
        return new byte[0];
    }

    /**
     * Create simple bytecode from instruction array
     *
     * @param instructions instruction array (opcode and params alternating)
     * @return bytecode array
     */
    protected byte[] createBytecode(int[] instructions) {
        byte[] bytecode = new byte[instructions.length * 4]; // Each instruction is 4 bytes
        int index = 0;

        for (int instr : instructions) {
            // Convert integer to bytes (big-endian)
            bytecode[index++] = (byte) ((instr >> 24) & 0xFF);
            bytecode[index++] = (byte) ((instr >> 16) & 0xFF);
            bytecode[index++] = (byte) ((instr >> 8) & 0xFF);
            bytecode[index++] = (byte) (instr & 0xFF);
        }

        return bytecode;
    }

    /**
     * Encode instruction with parameter
     *
     * @param opcode operation code
     * @param param parameter (optional)
     * @return encoded instruction
     */
    protected int encodeInstruction(int opcode, int param) {
        return (opcode << 24) | (param & 0xFFFFFF);
    }

    /**
     * Encode instruction without parameter
     *
     * @param opcode operation code
     * @return encoded instruction
     */
    protected int encodeInstruction(int opcode) {
        return opcode << 24;
    }

    /**
     * Encode instruction with 32-bit parameter (extended format)
     * Returns two instruction words: first word contains opcode with high bit set,
     * second word contains 32-bit parameter
     *
     * @param opcode operation code
     * @param param 32-bit parameter
     * @return array of two encoded instruction words
     */
    protected int[] encodeInstruction32(int opcode, int param) {
        // Set high bit of opcode to indicate extended instruction
        int extendedOpcode = opcode | 0x80; // Use bit 7 as extended format flag
        return new int[]{
            extendedOpcode << 24, // First word: extended opcode
            param                 // Second word: 32-bit parameter
        };
    }

    // ====================
    // Enhanced Assertions
    // ====================

    /**
     * Assert execution succeeds with expected result
     *
     * @param bytecode bytecode to execute
     * @param expected expected result
     * @throws Exception execution exception
     */
    protected void assertExecutionResult(byte[] bytecode, int expected) throws Exception {
        int result = execute(bytecode);
        Assertions.assertThat(result)
            .as("Execution result")
            .isEqualTo(expected);
    }

    /**
     * Assert execution of a program string succeeds with expected result
     *
     * @param program program string
     * @param expected expected result
     * @throws Exception execution exception
     */
    protected void assertProgramResult(String program, int expected) throws Exception {
        int result = executeProgram(program);
        Assertions.assertThat(result)
            .as("Program execution result for: " + program.trim())
            .isEqualTo(expected);
    }

    /**
     * Assert execution throws expected exception type
     *
     * @param bytecode bytecode to execute
     * @param expectedException expected exception type
     */
    protected void assertExecutionThrows(byte[] bytecode, Class<? extends Exception> expectedException) {
        Assertions.assertThatThrownBy(() -> execute(bytecode))
            .as("Execution should throw " + expectedException.getSimpleName())
            .isInstanceOf(expectedException);
    }

    /**
     * Assert execution throws any exception (i.e., fails)
     *
     * @param bytecode bytecode to execute
     */
    protected void assertExecutionFails(byte[] bytecode) {
        Assertions.assertThatThrownBy(() -> execute(bytecode))
            .as("Execution should fail")
            .isNotNull();
    }

    /**
     * Assert execution completes successfully (no exceptions)
     *
     * @param bytecode bytecode to execute
     * @throws Exception if execution fails
     */
    protected void assertExecutionSucceeds(byte[] bytecode) throws Exception {
        Assertions.assertThatCode(() -> execute(bytecode))
            .as("Execution should succeed")
            .doesNotThrowAnyException();
    }

    /**
     * Assert execution time is within expected range
     *
     * @param bytecode bytecode to execute
     * @param maxNanos maximum execution time in nanoseconds
     * @throws Exception execution exception
     */
    protected void assertExecutionTime(byte[] bytecode, long maxNanos) throws Exception {
        long startTime = System.nanoTime();
        execute(bytecode);
        long endTime = System.nanoTime();
        long executionTime = endTime - startTime;

        Assertions.assertThat(executionTime)
            .as("Execution time should be less than %d ns", maxNanos)
            .isLessThan(maxNanos);
    }

    // ====================
    // Performance Metrics
    // ====================

    /**
     * Get performance metrics for the last execution
     *
     * @return test metrics
     */
    protected TestMetrics getTestMetrics() {
        return testMetrics;
    }

    /**
     * Reset performance metrics
     */
    protected void resetMetrics() {
        if (testMetrics != null) {
            testMetrics.reset();
        }
    }

    /**
     * Check if test metrics are available
     *
     * @return true if metrics are available
     */
    protected boolean hasMetrics() {
        return testMetrics != null && testMetrics.hasMeasurements();
    }

    /**
     * Inner class for tracking test performance metrics
     */
    protected static class TestMetrics {
        private long executionCount = 0;
        private long totalExecutionTime = 0;
        private long minExecutionTime = Long.MAX_VALUE;
        private long maxExecutionTime = 0;

        /**
         * Record an execution time measurement
         *
         * @param nanos execution time in nanoseconds
         */
        public void recordExecution(long nanos) {
            executionCount++;
            totalExecutionTime += nanos;
            minExecutionTime = Math.min(minExecutionTime, nanos);
            maxExecutionTime = Math.max(maxExecutionTime, nanos);
        }

        /**
         * Reset all metrics
         */
        public void reset() {
            executionCount = 0;
            totalExecutionTime = 0;
            minExecutionTime = Long.MAX_VALUE;
            maxExecutionTime = 0;
        }

        /**
         * Get average execution time
         *
         * @return average time in nanoseconds
         */
        public long getAverageTime() {
            return executionCount > 0 ? totalExecutionTime / executionCount : 0;
        }

        /**
         * Get minimum execution time
         *
         * @return minimum time in nanoseconds
         */
        public long getMinTime() {
            return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
        }

        /**
         * Get maximum execution time
         *
         * @return maximum time in nanoseconds
         */
        public long getMaxTime() {
            return maxExecutionTime;
        }

        /**
         * Get total execution time
         *
         * @return total time in nanoseconds
         */
        public long getTotalTime() {
            return totalExecutionTime;
        }

        /**
         * Get execution count
         *
         * @return number of executions
         */
        public long getExecutionCount() {
            return executionCount;
        }

        /**
         * Check if any measurements have been taken
         *
         * @return true if measurements exist
         */
        public boolean hasMeasurements() {
            return executionCount > 0;
        }

        /**
         * Get average execution time in milliseconds
         *
         * @return average time in milliseconds
         */
        public double getAverageTimeMs() {
            return getAverageTime() / 1_000_000.0;
        }

        /**
         * Format metrics as string
         *
         * @return formatted metrics string
         */
        @Override
        public String toString() {
            return String.format(
                "TestMetrics{count=%d, avg=%.2f ms, min=%.2f ms, max=%.2f ms}",
                executionCount,
                getAverageTimeMs(),
                getMinTime() / 1_000_000.0,
                getMaxTime() / 1_000_000.0
            );
        }
    }
}