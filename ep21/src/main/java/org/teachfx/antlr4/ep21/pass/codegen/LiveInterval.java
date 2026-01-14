package org.teachfx.antlr4.ep21.pass.codegen;

public class LiveInterval {
    private final String variable;
    private final int start;
    private final int end;

    public LiveInterval(String variable, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("Start must be less than or equal to end");
        }
        this.variable = variable;
        this.start = start;
        this.end = end;
    }

    public String getVariable() {
        return variable;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return end - start;
    }

    public boolean overlaps(LiveInterval other) {
        return !(this.end < other.start || this.start > other.end);
    }

    public boolean contains(int position) {
        return position >= start && position < end;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LiveInterval)) return false;
        LiveInterval other = (LiveInterval) obj;
        return variable.equals(other.variable);
    }

    @Override
    public int hashCode() {
        return variable != null ? variable.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d]: %s", start, end, variable);
    }
}
