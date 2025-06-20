package org.teachfx.antlr4.ep19;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Performance benchmarks for the compiler.
 * These tests measure compilation time and memory usage for different program sizes.
 * 
 * Note: These tests are disabled by default because they are meant to be run manually
 * when performance testing is needed, not as part of the regular test suite.
 */
@Disabled("Performance tests are disabled by default")
public class PerformanceBenchmarkTest {

    private static final String BENCHMARK_DIR = "benchmark_results";
    private static final String RESULTS_FILE = BENCHMARK_DIR + "/benchmark_results.csv";
    private Runtime runtime;
    private long usedMemoryBefore;

    @BeforeEach
    void setUp() {
        // Create benchmark directory if it doesn't exist
        File dir = new File(BENCHMARK_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Force garbage collection to get a more accurate memory measurement
        runtime = Runtime.getRuntime();
        System.gc();
        usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
    }

    @AfterEach
    void tearDown() {
        // Force garbage collection again
        System.gc();
    }

    /**
     * Measures the compilation time and memory usage for a given code snippet.
     * 
     * @param code The code to compile
     * @param testName The name of the test
     * @param programSize The size of the program (small, medium, large)
     * @return A BenchmarkResult object containing the results
     */
    private BenchmarkResult measurePerformance(String code, String testName, String programSize) {
        long startTime = System.nanoTime();
        
        // Compile the code
        CompilerTestUtil.CompilationResult result = CompilerTestUtil.compile(code, false);
        
        long endTime = System.nanoTime();
        long compilationTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        // Measure memory usage
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = usedMemoryAfter - usedMemoryBefore;
        
        // Log the results
        System.out.println("Test: " + testName);
        System.out.println("Program Size: " + programSize);
        System.out.println("Compilation Time: " + compilationTime + " ms");
        System.out.println("Memory Used: " + memoryUsed + " bytes");
        System.out.println("Compilation Success: " + result.success);
        
        // Save the results to a CSV file
        saveResults(testName, programSize, compilationTime, memoryUsed, result.success);
        
        return new BenchmarkResult(compilationTime, memoryUsed, result.success);
    }

    /**
     * Saves the benchmark results to a CSV file.
     */
    private void saveResults(String testName, String programSize, long compilationTime, long memoryUsed, boolean success) {
        try {
            boolean fileExists = Files.exists(Paths.get(RESULTS_FILE));
            FileWriter writer = new FileWriter(RESULTS_FILE, true);
            
            // Write header if file doesn't exist
            if (!fileExists) {
                writer.append("Test Name,Program Size,Compilation Time (ms),Memory Used (bytes),Success\n");
            }
            
            // Write results
            writer.append(testName).append(",")
                  .append(programSize).append(",")
                  .append(String.valueOf(compilationTime)).append(",")
                  .append(String.valueOf(memoryUsed)).append(",")
                  .append(String.valueOf(success)).append("\n");
            
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println("Error saving benchmark results: " + e.getMessage());
        }
    }

    /**
     * Generates a program with the specified number of variable declarations and operations.
     */
    private String generateProgram(int varCount, int operationCount) {
        StringBuilder code = new StringBuilder();
        code.append("void main() {\n");
        
        // Generate variable declarations
        for (int i = 0; i < varCount; i++) {
            code.append("    int var").append(i).append(" = ").append(i).append(";\n");
        }
        
        // Generate operations
        for (int i = 0; i < operationCount; i++) {
            int var1 = i % varCount;
            int var2 = (i + 1) % varCount;
            code.append("    var").append(var1).append(" = var").append(var1)
                .append(" + var").append(var2).append(";\n");
        }
        
        // Print the result of the last variable
        code.append("    print(var").append(varCount - 1).append(");\n");
        code.append("}");
        
        return code.toString();
    }

    /**
     * Generates a program with nested structs of the specified depth.
     */
    private String generateNestedStructProgram(int depth) {
        StringBuilder code = new StringBuilder();
        
        // Generate nested struct declarations
        for (int i = depth; i > 0; i--) {
            code.append("struct Level").append(i).append(" {\n");
            if (i > 1) {
                code.append("    Level").append(i - 1).append(" inner;\n");
            } else {
                code.append("    int value;\n");
            }
            code.append("}\n\n");
        }
        
        // Generate main function
        code.append("void main() {\n");
        code.append("    Level").append(depth).append(" obj;\n");
        
        // Generate nested field access
        StringBuilder access = new StringBuilder("obj");
        for (int i = depth; i > 1; i--) {
            access.append(".inner");
        }
        access.append(".value = 42;\n");
        code.append("    ").append(access);
        
        // Print the value
        code.append("    print(").append(access.substring(0, access.length() - 5)).append(");\n");
        code.append("}");
        
        return code.toString();
    }

    @Test
    void benchmarkSmallProgram() {
        String code = generateProgram(10, 20);
        BenchmarkResult result = measurePerformance(code, "SmallProgram", "small");
        assertTrue(result.success, "Small program should compile successfully");
    }

    @Test
    void benchmarkMediumProgram() {
        String code = generateProgram(50, 100);
        BenchmarkResult result = measurePerformance(code, "MediumProgram", "medium");
        assertTrue(result.success, "Medium program should compile successfully");
    }

    @Test
    void benchmarkLargeProgram() {
        String code = generateProgram(100, 500);
        BenchmarkResult result = measurePerformance(code, "LargeProgram", "large");
        assertTrue(result.success, "Large program should compile successfully");
    }

    @Test
    void benchmarkNestedStructs() {
        String code = generateNestedStructProgram(5);
        BenchmarkResult result = measurePerformance(code, "NestedStructs", "medium");
        assertTrue(result.success, "Nested structs program should compile successfully");
    }

    @Test
    void benchmarkComplexProgram() {
        // A more complex program with structs, functions, and control flow
        String code = 
            "struct Point {\n" +
            "    int x;\n" +
            "    int y;\n" +
            "    float distance() {\n" +
            "        return (x * x + y * y) * 1.0;\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "int factorial(int n) {\n" +
            "    if (n <= 1) {\n" +
            "        return 1;\n" +
            "    } else {\n" +
            "        return n * factorial(n - 1);\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    // Create and use 10 points\n" +
            "    Point points[10];\n" +
            "    float totalDistance = 0.0;\n" +
            "    \n" +
            "    for (int i = 0; i < 10; i++) {\n" +
            "        points[i].x = i;\n" +
            "        points[i].y = i * 2;\n" +
            "        totalDistance = totalDistance + points[i].distance();\n" +
            "    }\n" +
            "    \n" +
            "    print(\"Total distance: \");\n" +
            "    print(totalDistance);\n" +
            "    \n" +
            "    // Calculate factorials\n" +
            "    for (int i = 1; i <= 10; i++) {\n" +
            "        print(\"Factorial of \");\n" +
            "        print(i);\n" +
            "        print(\": \");\n" +
            "        print(factorial(i));\n" +
            "    }\n" +
            "}";
        
        BenchmarkResult result = measurePerformance(code, "ComplexProgram", "large");
        assertTrue(result.success, "Complex program should compile successfully");
    }

    /**
     * Compares the performance of the current compiler version with a previous version.
     * This test reads the benchmark results from the CSV file and compares them.
     */
    @Test
    void compareWithPreviousVersion() throws IOException {
        // First, run the benchmarks for the current version
        benchmarkSmallProgram();
        benchmarkMediumProgram();
        benchmarkLargeProgram();
        
        // Read the results from the CSV file
        List<String> lines = Files.readAllLines(Paths.get(RESULTS_FILE));
        if (lines.size() <= 1) {
            System.out.println("Not enough data to compare with previous version");
            return;
        }
        
        // Parse the results
        List<BenchmarkResult> currentResults = new ArrayList<>();
        List<BenchmarkResult> previousResults = new ArrayList<>();
        
        // Skip the header
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            String testName = parts[0];
            long compilationTime = Long.parseLong(parts[2]);
            long memoryUsed = Long.parseLong(parts[3]);
            boolean success = Boolean.parseBoolean(parts[4]);
            
            BenchmarkResult result = new BenchmarkResult(compilationTime, memoryUsed, success);
            
            // Assume the last 3 results are from the current version
            if (i >= lines.size() - 3) {
                currentResults.add(result);
            } else if (i >= lines.size() - 6 && i < lines.size() - 3) {
                // And the 3 before that are from the previous version
                previousResults.add(result);
            }
        }
        
        // Compare the results
        if (currentResults.size() == 3 && previousResults.size() == 3) {
            for (int i = 0; i < 3; i++) {
                BenchmarkResult current = currentResults.get(i);
                BenchmarkResult previous = previousResults.get(i);
                
                double timeRatio = (double) current.compilationTime / previous.compilationTime;
                double memoryRatio = (double) current.memoryUsed / previous.memoryUsed;
                
                System.out.println("Test " + (i + 1) + " time ratio: " + timeRatio + 
                                  " (< 1 means faster, > 1 means slower)");
                System.out.println("Test " + (i + 1) + " memory ratio: " + memoryRatio + 
                                  " (< 1 means less memory, > 1 means more memory)");
            }
        } else {
            System.out.println("Not enough data to compare with previous version");
        }
    }

    /**
     * A simple class to hold benchmark results.
     */
    private static class BenchmarkResult {
        final long compilationTime;
        final long memoryUsed;
        final boolean success;
        
        BenchmarkResult(long compilationTime, long memoryUsed, boolean success) {
            this.compilationTime = compilationTime;
            this.memoryUsed = memoryUsed;
            this.success = success;
        }
    }
}