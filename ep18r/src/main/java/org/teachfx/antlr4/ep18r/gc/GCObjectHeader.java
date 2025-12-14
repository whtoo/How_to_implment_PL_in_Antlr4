package org.teachfx.antlr4.ep18r.gc;

/**
 * 垃圾回收对象头部信息
 * 跟踪对象的元数据，包括引用计数和大小
 */
public class GCObjectHeader {
    private int refCount;      // 引用计数
    private int size;          // 对象大小
    private boolean marked;    // 标记位（用于标记-清除算法）
    private boolean alive;     // 是否存活

    public GCObjectHeader(int size) {
        this.refCount = 0;
        this.size = size;
        this.marked = false;
        this.alive = true;
    }

    /**
     * 增加引用计数
     * @return 新的引用计数
     */
    public int incrementRef() {
        refCount++;
        return refCount;
    }

    /**
     * 减少引用计数
     * @return 新的引用计数
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
        return String.format("GCObjectHeader{size=%d, refCount=%d, marked=%s, alive=%s}",
            size, refCount, marked, alive);
    }
}