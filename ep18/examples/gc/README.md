# GC Integration Examples

This directory contains examples demonstrating garbage collection integration in the Cymbol language virtual machine.

## Examples

### 1. Basic GC Usage (`basic-gc.cymbol`)
Demonstrates basic struct allocation and automatic garbage collection.

### 2. Reference Counting (`reference-counting.cymbol`)
Shows explicit reference counting operations and memory management.

### 3. Memory Leak Detection (`memory-leak-demo.cymbol`)
Demonstrates memory leak patterns and how GC helps detect them.

### 4. Performance Comparison (`performance-comparison.cymbol`)
Compares performance with and without GC enabled.

## Running Examples

```bash
# Compile and run a GC example
./scripts/run.sh compile ep18
./scripts/run.sh run ep18 "ep18/examples/gc/basic-gc.cymbol"

# Run with GC debugging enabled
./scripts/run.sh run ep18 "ep18/examples/gc/basic-gc.cymbol" --gc-debug

# Run performance comparison
./scripts/run.sh run ep18 "ep18/examples/gc/performance-comparison.cymbol" --benchmark
```

## GC Configuration Options

The virtual machine supports various GC configuration options:

```java
// Example VM configuration with GC
VMConfig config = VMConfig.builder()
    .setHeapSize(16 * 1024 * 1024)  // 16MB heap
    .setStackSize(4096)
    .setEnableGC(true)              // Enable garbage collection
    .setGCType(GCType.REFERENCE_COUNTING)
    .setGCDebugMode(true)           // Enable GC debugging
    .setGCThreshold(0.75)           // Collect at 75% heap usage
    .build();
```

## Best Practices

1. **Enable GC for long-running programs**: Prevents memory leaks
2. **Monitor GC statistics**: Use `GCStats` to track performance
3. **Configure appropriate heap size**: Based on application needs
4. **Use GC debugging during development**: Helps identify memory issues

## Testing GC Integration

Run the comprehensive GC test suite:

```bash
# Run all GC tests
mvn test -pl ep18 -Dtest="*GC*"

# Run integration tests
mvn test -pl ep18 -Dtest="CymbolStackVMGCIntegrationTest"

# Run performance benchmarks
mvn verify -pl ep18 -Pbenchmarks
```