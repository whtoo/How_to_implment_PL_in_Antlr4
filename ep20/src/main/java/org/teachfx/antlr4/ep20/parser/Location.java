package org.teachfx.antlr4.ep20.parser;

public class Location {
    protected int startLine;
    protected int startColumn;
    protected int endLine;
    protected int endColumn;

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
}
