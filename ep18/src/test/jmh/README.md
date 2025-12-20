# EP18 Performance Benchmarks

This directory contains JMH (Java Microbenchmark Harness) benchmarks for measuring the performance of EP18 Stack VM operations.

## Overview

The benchmark suite measures:

1. **Instruction Execution** (`InstructionExecutionBenchmark.java`)
   - Individual instruction performance
   - Arithmetic operations
   - Control flow operations
   - Function call overhead

2. **Memory Access** (`MemoryAccessBenchmark.java`)
   - Stack push/pop operations
   - Heap allocation
   - Array access patterns
   - Local variable access

## Running Benchmarks

### Run All Benchmarks
```bash
cd ep18
mvn clean test
```

### Run Specific Benchmark
```bash
# Run instruction execution benchmarks
mvn test -Dtest=InstructionExecutionBenchmark

# Run memory access benchmarks
mvn test -Dtest=MemoryAccessBenchmark
```

### Run with JMH Plugin
```bash
# Run all JMH benchmarks
mvn clean integration-test

# Run with custom options
mvn jmh:benchmark -Djmh.include=InstructionExecutionBenchmark
```

### Custom JMH Options

You can customize benchmark execution by editing `pom.xml`:

```xml
<configuration>
    <timeout>240s</timeout>
    <fork>1</fork>
    <jvmArgs>-Xmx1G</jvmArgs>
    <timeUnit>ns</timeUnit>
    <warmupIterations>5</warmupIterations>
    <measurementIterations>10</measurementIterations>
    <threads>1</threads>
</configuration>
```

## Benchmark Results

### Performance Targets (from TDD Refactoring Plan)

| Operation | Target (ns/instruction) |
|-----------|------------------------|
| Stack operations | < 30 ns |
| Arithmetic | < 40 ns |
| Control flow | < 50 ns |
| Memory access | < 100 ns |
| Function call | < 200 ns |

### Understanding Results

Benchmark output includes:

```
Benchmark                                          Mode  Cnt     Score     Error   Units
InstructionExecutionBenchmark.benchmarkSimpleAdd   avgt   10   152.341 ±  12.456  ns/op
MemoryAccessBenchmark.benchmarkStackPush           avgt   10    28.765 ±   2.134   ns/op
```

- **Mode**: Average Time (avgt)
- **Cnt**: Number of iterations
- **Score**: Average time per operation
- **Error**: Standard error
- **Units**: nanoseconds per operation (ns/op)

## Interpreting Performance

### Good Performance
- Results below target thresholds
- Low standard error
- Consistent across iterations

### Performance Issues
- Results above target thresholds
- High standard error (inconsistent)
- Large variance between min/max

### Optimization Opportunities
- High-scoring operations (slowest)
- High variance operations (inconsistent)
- Operations not meeting targets

## Benchmark Development

### Adding New Benchmarks

1. Create benchmark class in `src/test/jmh/java/`
2. Extend `Runner` class or use JMH annotations
3. Follow naming convention: `*Benchmark.java`
4. Document expected performance targets

Example:
```java
@Benchmark
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public void myOperation() throws Exception {
    // Benchmark code here
}
```

### Best Practices

1. **Isolation**: Each benchmark should test one operation
2. **Warmup**: Use @Warmup for JIT compilation
3. **Measurement**: Use @Measurement for accurate results
4. **Forking**: Use @Fork to isolate JVM state
5. **State**: Use @State for shared setup

### Common Pitfalls

1. **Dead Code Elimination**: Always use the result
2. **Constant Folding**: Use variables, not constants
3. **Loop Unrolling**: Be aware of JIT optimizations
4. **GC Effects**: Monitor GC during long benchmarks

## Performance Regression Testing

### Baseline Establishment
1. Run benchmarks on clean build
2. Save results to `benchmark-baseline.txt`
3. Use as comparison for future changes

### Regression Detection
```bash
# Compare current results to baseline
./scripts/compare-benchmarks.sh baseline.txt current.txt
```

### Automated Monitoring
- Run benchmarks in CI/CD pipeline
- Fail build if performance degrades > 10%
- Generate performance reports

## Resources

- [JMH Documentation](https://openjdk.java.net/projects/code-tools/jmh/)
- [Java Microbenchmark Harness Tutorial](https://www.baeldung.com/java-microbenchmark-harness)
- [EP18 TDD Refactoring Plan](../docs/EP18_TDD_重构优化方案.md)
