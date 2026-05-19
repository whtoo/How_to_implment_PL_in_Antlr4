package org.teachfx.antlr4.ep15.gc;

/**
 * Garbage Collection Object Header
 * Tracks object metadata including reference count, size, and offset
 */
public class GCObjectHeader {
    private int refCount;      // reference count
    private int size;          // object size
    private int offset;        // object offset in heap
    private boolean marked;    // mark bit (for mark-sweep algorithm)
    private boolean alive;     // whether alive

    public GCObjectHeader(int size) {
        this.refCount = 0;
        this.size = size;
        this.offset = -1;  // -1 means unallocated
        this.marked = false;
        this.alive = true;
    }

    /**
     * Increment reference count
     * @return new reference count
     */
    public int incrementRef() {
        refCount++;
        return refCount;
    }

    /**
     * Decrement reference count
     * @return new reference count
     */
    public int decrementRef() {
        refCount--;
        return refCount;
    }

    // Getters and Setters
    public int getRefCount() {
        return refCount;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public String toString() {
        return String.format("GCObjectHeader{size=%d, refCount=%d, offset=%d, marked=%s, alive=%s}",
            size, refCount, offset, marked, alive);
    }
}
