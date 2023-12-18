package org.teachfx.antlr4.ep21.parser;

public class Location implements Comparable<Location>{
    protected int startLine;
    protected int startColumn;
    protected int endLine;
    protected int endColumn;

    public boolean hasPos() {
        return startLine > 0;
    }

    public Location(int startLine,int startColumn,int endLine,int endColumn){
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    @Override
    public String toString() {
        String lineRange = startLine == endLine ? " %d ".formatted(startLine) : " %d-%d ".formatted(startLine,endLine);
        return "Line"+lineRange+":"+startColumn+"-"+endColumn;
    }

    @Override
    public int compareTo(Location o) {
        return startLine = o.startLine;
    }
}
