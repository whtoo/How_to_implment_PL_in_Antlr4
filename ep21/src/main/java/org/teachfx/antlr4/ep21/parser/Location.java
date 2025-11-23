package org.teachfx.antlr4.ep21.parser;

/**
 * 用于表示源代码中的位置信息
 * 在 ANTLR 4.13.2 中不再自动生成此类
 */
public class Location {
    private final String fileName;
    private final int line;
    private final int startIndex;
    private final int stopIndex;

    public Location(String fileName, int line, int startIndex, int stopIndex) {
        this.fileName = fileName;
        this.line = line;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }

    public Location(String fileName, int line, int column) {
        this.fileName = fileName;
        this.line = line;
        this.startIndex = column;
        this.stopIndex = column;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLine() {
        return line;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getStopIndex() {
        return stopIndex;
    }

    public boolean hasPos() {
        return true; // 简化实现
    }

    @Override
    public String toString() {
        return fileName + ":" + line + ":" + startIndex;
    }

    public int compareTo(Location o) {
        return Integer.compare(this.line, o.line);
    }
}