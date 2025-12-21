# Garbage Collection API Reference

<cite>
**Referenced Files in This Document**
- [GarbageCollector.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/gc/GarbageCollector.java)
- [ReferenceCountingGC.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/gc/ReferenceCountingGC.java)
- [GCObjectHeader.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/gc/GCObjectHeader.java)
- [GCStats.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/gc/GCStats.java)
</cite>

## Table of Contents
1. [GarbageCollector Interface](#garbagecollector-interface)
2. [ReferenceCountingGC Class](#referencecountinggc-class)
3. [GCObjectHeader Class](#gcobjectheader-class)
4. [GCStats Class](#gcstats-class)
5. [Usage Examples](#usage-examples)
6. [Error Handling](#error-handling)
7. [Performance Considerations](#performance-considerations)

## GarbageCollector Interface

The `GarbageCollector` interface defines the contract for all garbage collection implementations in the system.

### Interface Definition

```java
package org.teachfx.antlr4.ep18.gc;

/**
 * 垃圾回收器接口
 * 定义虚拟机垃圾回收的基本操作
 */
public interface GarbageCollector {
    /**
     * 分配内存对象
     * @param size 对象大小（字节）
     * @return 对象ID（大于0）
     * @throws OutOfMemoryError 内存不足
     * @throws IllegalArgumentException 大小无效（<=0）
     */
    int allocate(int size) throws OutOfMemoryError;

    /**
     * 增加引用计数
     * @param objectId 对象ID
     */
    void incrementRef(int objectId);

    /**
     * 减少引用计数
     * @param objectId 对象ID
     */
    void decrementRef(int objectId);

    /**
     * 执行垃圾回收
     */
    void collect();

    /**
     * 检查对象是否存活
     * @param objectId 对象ID
     * @return true如果对象存活，false如果对象不存在或已回收
     */
    boolean isObjectAlive(int objectId);

    /**
     * 获取GC统计信息
     * @return GC统计信息对象
     */
    GCStats getStats();

    /**
     * 重置统计信息
     */
    void resetStats();
}
```

### Method Details

#### `allocate(int size)`

Allocates memory for an object and returns a unique object ID.

**Parameters:**
- `size`: Size of the object in bytes (must be > 0)

**Returns:**
- `int`: Object ID (greater than 0)

**Throws:**
- `IllegalArgumentException`: If `size <= 0`
- `OutOfMemoryError`: If insufficient memory available

**Example:**
```java
GarbageCollector gc = new ReferenceCountingGC(1024 * 1024);
int objectId = gc.allocate(100); // Allocate 100 bytes
```

#### `incrementRef(int objectId)`

Increments the reference count for the specified object.

**Parameters:**
- `objectId`: Object ID to increment reference count for

**Notes:**
- Safe to call with invalid object IDs (no exception thrown)
- Typically called when a new reference to the object is created

**Example:**
```java
gc.incrementRef(objectId); // Object now has one more reference
```

#### `decrementRef(int objectId)`

Decrements the reference count for the specified object.

**Parameters:**
- `objectId`: Object ID to decrement reference count for

**Notes:**
- Safe to call with invalid object IDs (no exception thrown)
- If reference count reaches 0, object becomes eligible for collection
- Typically called when a reference to the object is removed

**Example:**
```java
gc.decrementRef(objectId); // Remove one reference
```

#### `collect()`

Performs garbage collection, reclaiming memory from objects with reference count <= 0.

**Notes:**
- May be called manually or automatically when memory is low
- Updates statistics in `GCStats`
- Objects are collected immediately when reference count reaches 0

**Example:**
```java
gc.collect(); // Reclaim unused memory
```

#### `isObjectAlive(int objectId)`

Checks if an object is currently alive (allocated and not collected).

**Parameters:**
- `objectId`: Object ID to check

**Returns:**
- `boolean`: `true` if object exists and is alive, `false` otherwise

**Example:**
```java
if (gc.isObjectAlive(objectId)) {
    // Object can be used
}
```

#### `getStats()` and `resetStats()`

Manage garbage collection statistics.

**Example:**
```java
GCStats stats = gc.getStats();
System.out.println("Collections: " + stats.getTotalCollections());
gc.resetStats(); // Reset statistics
```

## ReferenceCountingGC Class

The `ReferenceCountingGC` class implements the `GarbageCollector` interface using reference counting algorithm.

### Class Definition

```java
package org.teachfx.antlr4.ep18.gc;

/**
 * 引用计数垃圾回收器
 * 使用引用计数算法进行自动内存管理
 */
public class ReferenceCountingGC implements GarbageCollector {
    /**
     * 创建引用计数垃圾回收器
     * @param heapSize 堆大小（字节，必须>0）
     * @throws IllegalArgumentException 如果heapSize <= 0
     */
    public ReferenceCountingGC(int heapSize) {
        // Implementation
    }

    // Interface methods implementation...

    /**
     * 获取堆使用信息
     * @return 堆使用信息字符串
     */
    public String getHeapInfo() {
        // Implementation
    }

    /**
     * 手动触发垃圾回收（公开方法）
     */
    public void forceGC() {
        // Implementation
    }

    /**
     * 检查是否可能存在循环引用
     * @param objectId 对象ID
     * @return 是否可能存在循环引用
     */
    public boolean hasCycleReference(int objectId) {
        // Implementation
    }

    /**
     * 清理所有对象（用于测试）
     */
    public void clearAll() {
        // Implementation
    }

    /**
     * 获取当前堆使用量
     * @return 使用的内存量（字节）
     */
    public int getHeapUsage() {
        // Implementation
    }

    /**
     * 获取堆大小
     * @return 总堆大小（字节）
     */
    public int getHeapSize() {
        // Implementation
    }

    /**
     * 获取对象数量
     * @return 当前对象数量
     */
    public int getObjectCount() {
        // Implementation
    }

    /**
     * 获取字符串表示
     * @return 垃圾回收器状态字符串
     */
    @Override
    public String toString() {
        // Implementation
    }
}
```

### Constructor

#### `ReferenceCountingGC(int heapSize)`

Creates a new reference counting garbage collector with the specified heap size.

**Parameters:**
- `heapSize`: Total heap size in bytes (must be > 0)

**Throws:**
- `IllegalArgumentException`: If `heapSize <= 0`

**Example:**
```java
// Create GC with 1MB heap
ReferenceCountingGC gc = new ReferenceCountingGC(1024 * 1024);
```

### Additional Methods

#### `getHeapInfo()`

Returns a formatted string with heap usage information.

**Returns:**
- `String`: Heap information (e.g., "Heap: 512/1024 bytes (50.00% used)")

**Example:**
```java
String info = gc.getHeapInfo();
System.out.println(info); // Heap: 512/1024 bytes (50.00% used)
```

#### `forceGC()`

Manually triggers garbage collection. This is equivalent to calling `collect()` but provides a more explicit API.

**Example:**
```java
gc.forceGC(); // Force garbage collection
```

#### `hasCycleReference(int objectId)`

Checks if an object might be part of a circular reference.

**Parameters:**
- `objectId`: Object ID to check

**Returns:**
- `boolean`: `true` if object might be in a circular reference

**Notes:**
- This is a simple detection mechanism
- Reference counting cannot automatically collect circular references

**Example:**
```java
if (gc.hasCycleReference(objectId)) {
    // Potential circular reference detected
}
```

#### `clearAll()`

Clears all objects from the heap (for testing purposes).

**Notes:**
- Resets heap usage to 0
- Resets object ID counter
- Useful for test cleanup

**Example:**
```java
gc.clearAll(); // Reset GC state
```

#### `getHeapUsage()`, `getHeapSize()`, `getObjectCount()`

Utility methods for monitoring GC state.

**Example:**
```java
int used = gc.getHeapUsage();
int total = gc.getHeapSize();
int objects = gc.getObjectCount();
double usagePercent = (double) used / total * 100;
```

## GCObjectHeader Class

The `GCObjectHeader` class represents metadata for garbage collected objects.

### Class Definition

```java
package org.teachfx.antlr4.ep18.gc;

/**
 * 垃圾回收对象头部
 * 存储对象的元数据信息
 */
public class GCObjectHeader {
    /**
     * 创建对象头部
     * @param size 对象大小（字节）
     */
    public GCObjectHeader(int size) {
        // Implementation
    }

    /**
     * 获取对象大小
     * @return 对象大小（字节）
     */
    public int getSize() {
        // Implementation
    }

    /**
     * 获取引用计数
     * @return 当前引用计数
     */
    public int getRefCount() {
        // Implementation
    }

    /**
     * 检查对象是否存活
     * @return true如果对象存活
     */
    public boolean isAlive() {
        // Implementation
    }

    /**
     * 设置对象存活状态
     * @param alive 存活状态
     */
    public void setAlive(boolean alive) {
        // Implementation
    }

    /**
     * 增加引用计数
     */
    public void incrementRef() {
        // Implementation
    }

    /**
     * 减少引用计数
     * @return 减少后的引用计数
     */
    public int decrementRef() {
        // Implementation
    }
}
```

### Usage Notes

- This class is used internally by `ReferenceCountingGC`
- Each allocated object has an associated `GCObjectHeader`
- Headers are stored separately from object data
- Reference counting operations are performed on headers

## GCStats Class

The `GCStats` class tracks garbage collection statistics.

### Class Definition

```java
package org.teachfx.antlr4.ep18.gc;

/**
 * 垃圾回收统计信息
 * 记录GC的性能和效率指标
 */
public class GCStats {
    /**
     * 记录内存分配
     * @param size 分配的内存大小
     */
    public void recordAllocation(int size) {
        // Implementation
    }

    /**
     * 记录垃圾回收
     * @param objects 回收的对象数量
     * @param memory 回收的内存大小
     * @param time 回收耗时（纳秒）
     */
    public void recordCollection(int objects, long memory, long time) {
        // Implementation
    }

    /**
     * 重置所有统计信息
     */
    public void reset() {
        // Implementation
    }

    /**
     * 获取总分配次数
     * @return 分配次数
     */
    public long getTotalAllocations() {
        // Implementation
    }

    /**
     * 获取总分配内存
     * @return 分配的内存总量（字节）
     */
    public long getTotalAllocatedMemory() {
        // Implementation
    }

    /**
     * 获取总回收次数
     * @return 回收次数
     */
    public long getTotalCollections() {
        // Implementation
    }

    /**
     * 获取总回收内存
     * @return 回收的内存总量（字节）
     */
    public long getTotalCollectedMemory() {
        // Implementation
    }

    /**
     * 获取总回收时间
     * @return 回收总耗时（纳秒）
     */
    public long getTotalCollectionTime() {
        // Implementation
    }

    /**
     * 获取平均回收时间
     * @return 平均回收时间（纳秒）
     */
    public long getAverageCollectionTime() {
        // Implementation
    }

    /**
     * 获取字符串表示
     * @return 统计信息字符串
     */
    @Override
    public String toString() {
        // Implementation
    }
}
```

### Statistics Tracking

The class tracks the following metrics:
- Total number of allocations
- Total allocated memory
- Total number of collections
- Total collected memory
- Total collection time
- Average collection time

**Example:**
```java
GCStats stats = gc.getStats();
System.out.println("Statistics:");
System.out.println("  Allocations: " + stats.getTotalAllocations());
System.out.println("  Allocated Memory: " + stats.getTotalAllocatedMemory() + " bytes");
System.out.println("  Collections: " + stats.getTotalCollections());
System.out.println("  Collected Memory: " + stats.getTotalCollectedMemory() + " bytes");
System.out.println("  Collection Time: " + stats.getTotalCollectionTime() + " ns");
System.out.println("  Avg Collection Time: " + stats.getAverageCollectionTime() + " ns");
```

## Usage Examples

### Basic Usage

```java
// Create garbage collector with 1MB heap
GarbageCollector gc = new ReferenceCountingGC(1024 * 1024);

// Allocate an object
int objectId = gc.allocate(100);

// Increment reference count (when referenced)
gc.incrementRef(objectId);

// Use the object...

// Decrement reference count (when reference removed)
gc.decrementRef(objectId);

// Check if object is alive
boolean alive = gc.isObjectAlive(objectId);

// Manually trigger garbage collection
gc.collect();

// Get statistics
GCStats stats = gc.getStats();
```

### Object Lifecycle Management

```java
public class ManagedObject {
    private final GarbageCollector gc;
    private final int objectId;

    public ManagedObject(GarbageCollector gc, int size) {
        this.gc = gc;
        this.objectId = gc.allocate(size);
        gc.incrementRef(objectId); // Initial reference
    }

    public int getId() {
        return objectId;
    }

    public void addReference() {
        gc.incrementRef(objectId);
    }

    public void removeReference() {
        gc.decrementRef(objectId);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            // Ensure object is properly cleaned up
            gc.decrementRef(objectId);
        } finally {
            super.finalize();
        }
    }
}
```

### Integration with Virtual Machine

```java
public class VMWithGC {
    private final GarbageCollector gc;
    private final Map<Integer, Object> objectTable;

    public VMWithGC(int heapSize) {
        this.gc = new ReferenceCountingGC(heapSize);
        this.objectTable = new HashMap<>();
    }

    public int allocateObject(int size, Object data) {
        int objectId = gc.allocate(size);
        gc.incrementRef(objectId);
        objectTable.put(objectId, data);
        return objectId;
    }

    public void storeReference(int referenceId, int objectId) {
        // Add new reference
        gc.incrementRef(objectId);

        // Remove old reference if any
        Integer oldObjectId = getReference(referenceId);
        if (oldObjectId != null) {
            gc.decrementRef(oldObjectId);
        }

        // Update reference
        setReference(referenceId, objectId);
    }

    public void collectGarbage() {
        gc.collect();

        // Clean up object table
        objectTable.entrySet().removeIf(entry -> !gc.isObjectAlive(entry.getKey()));
    }
}
```

### Error Handling Example

```java
public class SafeGCWrapper {
    private final GarbageCollector gc;

    public SafeGCWrapper(int heapSize) {
        if (heapSize <= 0) {
            throw new IllegalArgumentException("Heap size must be positive");
        }
        this.gc = new ReferenceCountingGC(heapSize);
    }

    public Optional<Integer> tryAllocate(int size) {
        try {
            int objectId = gc.allocate(size);
            return Optional.of(objectId);
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory: " + e.getMessage());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid size: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void safeIncrementRef(Integer objectId) {
        if (objectId != null && objectId > 0) {
            gc.incrementRef(objectId);
        }
    }

    public void safeDecrementRef(Integer objectId) {
        if (objectId != null && objectId > 0) {
            gc.decrementRef(objectId);
        }
    }
}
```

## Error Handling

### Common Exceptions

1. **IllegalArgumentException**
   - Thrown by `allocate()` when `size <= 0`
   - Thrown by `ReferenceCountingGC` constructor when `heapSize <= 0`

2. **OutOfMemoryError**
   - Thrown by `allocate()` when insufficient memory
   - Can occur even after garbage collection attempt

### Safe Usage Patterns

```java
// Always validate parameters
public int safeAllocate(GarbageCollector gc, int size) {
    if (size <= 0) {
        throw new IllegalArgumentException("Size must be positive: " + size);
    }

    try {
        return gc.allocate(size);
    } catch (OutOfMemoryError e) {
        // Try to free memory and retry
        gc.collect();
        return gc.allocate(size); // May still throw OutOfMemoryError
    }
}

// Handle invalid object IDs gracefully
public void safeReferenceOperation(GarbageCollector gc, int objectId) {
    if (objectId <= 0) {
        // Invalid object ID, log and return
        System.err.println("Invalid object ID: " + objectId);
        return;
    }

    // incrementRef and decrementRef are safe with invalid IDs
    gc.incrementRef(objectId);
    // ... use object
    gc.decrementRef(objectId);
}
```

### Recovery Strategies

1. **Memory Exhaustion**
   ```java
   public int allocateWithRetry(GarbageCollector gc, int size, int maxRetries) {
       for (int i = 0; i < maxRetries; i++) {
           try {
               return gc.allocate(size);
           } catch (OutOfMemoryError e) {
               if (i < maxRetries - 1) {
                   gc.collect(); // Try to free memory
                   continue;
               }
               throw e; // Re-throw after max retries
           }
       }
       throw new IllegalStateException("Should not reach here");
   }
   ```

2. **Object Validation**
   ```java
   public boolean validateObject(GarbageCollector gc, int objectId) {
       if (objectId <= 0) {
           return false;
       }

       if (!gc.isObjectAlive(objectId)) {
           System.err.println("Object " + objectId + " is not alive");
           return false;
       }

       return true;
   }
   ```

## Performance Considerations

### Allocation Performance

- **Small objects**: Fast allocation from free list
- **Large objects**: May trigger garbage collection
- **Fragmentation**: Can affect allocation performance over time

### Reference Counting Overhead

- **Increment/Decrement**: O(1) operations
- **Memory overhead**: Per-object header (12-16 bytes)
- **Collection overhead**: Linear scan of all objects

### Optimization Tips

1. **Batch Operations**
   ```java
   // Instead of individual allocations
   List<Integer> objectIds = new ArrayList<>();
   for (int i = 0; i < 100; i++) {
       objectIds.add(gc.allocate(100));
   }

   // Process batch
   for (int objectId : objectIds) {
       gc.incrementRef(objectId);
   }
   ```

2. **Object Reuse**
   ```java
   // Reuse objects instead of reallocating
   public class ObjectPool {
       private final GarbageCollector gc;
       private final Queue<Integer> pool;

       public ObjectPool(GarbageCollector gc, int size, int objectSize) {
           this.gc = gc;
           this.pool = new LinkedList<>();

           for (int i = 0; i < size; i++) {
               int objectId = gc.allocate(objectSize);
               gc.incrementRef(objectId);
               pool.add(objectId);
           }
       }

       public Integer acquire() {
           return pool.poll();
       }

       public void release(int objectId) {
           pool.add(objectId);
       }
   }
   ```

3. **Monitoring and Tuning**
   ```java
   public class GCMonitor {
       private final GarbageCollector gc;
       private long lastCollectionTime;

       public void monitor() {
           GCStats stats = gc.getStats();

           // Check collection frequency
           if (stats.getTotalCollections() > 100) {
               System.out.println("High GC frequency, consider increasing heap size");
           }

           // Check collection duration
           long avgTime = stats.getAverageCollectionTime();
           if (avgTime > 1_000_000) { // 1ms
               System.out.println("Long GC pauses, consider tuning");
           }
       }
   }
   ```

### Memory Fragmentation Management

Reference counting can lead to memory fragmentation. Consider:

1. **Regular Compaction**: Periodically compact memory
2. **Size Classes**: Allocate objects from size-class arenas
3. **Large Object Separate**: Handle large objects separately

**Example of fragmentation-aware allocation:**
```java
public class FragmentationAwareGC {
    private final GarbageCollector gc;
    private final Map<Integer, List<Integer>> sizeClasses;

    public int allocate(int size) {
        // Round up to nearest size class
        int sizeClass = roundToSizeClass(size);

        // Try to reuse from size class
        List<Integer> pool = sizeClasses.get(sizeClass);
        if (pool != null && !pool.isEmpty()) {
            return pool.remove(pool.size() - 1);
        }

        // Allocate new object
        return gc.allocate(sizeClass);
    }

    private int roundToSizeClass(int size) {
        // Round to nearest power of 2 or other size class
        return Integer.highestOneBit(size * 2 - 1);
    }
}
```