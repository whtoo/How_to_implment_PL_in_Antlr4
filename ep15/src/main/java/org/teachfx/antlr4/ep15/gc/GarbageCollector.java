package org.teachfx.antlr4.ep15.gc;

/**
 * Garbage Collector Interface
 * Defines the basic operations for VM garbage collection
 */
public interface GarbageCollector {
    /**
     * Allocate a memory object
     * @param size object size
     * @return object ID
     * @throws OutOfMemoryError if out of memory
     */
    int allocate(int size) throws OutOfMemoryError;

    /**
     * Increment reference count
     * @param objectId object ID
     */
    void incrementRef(int objectId);

    /**
     * Decrement reference count
     * @param objectId object ID
     */
    void decrementRef(int objectId);

    /**
     * Perform garbage collection
     */
    void collect();

    /**
     * Check if object is alive
     * @param objectId object ID
     * @return whether alive
     */
    boolean isObjectAlive(int objectId);

    /**
     * Get GC statistics
     * @return statistics
     */
    GCStats getStats();

    /**
     * Reset statistics
     */
    void resetStats();
}
