package org.teachfx.antlr4.ep21.error;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.parser.Location;

public abstract class CymbalError implements Comparable<CymbalError> {
    public Location location;

    public abstract String getErrMessage();

    public CymbalError(Location location1) {
        this.location = location1;
    }

    protected Location getPos() {
        return location;
    }

    /**
     * 返回包含位置信息在内的完整错误信息
     */
    @Override
    public String toString() {
        if (!location.hasPos()) {
            return "*** Error: " + getErrMessage();
        } else {
            return "*** Error at " + location + ": " + getErrMessage();
        }
    }

    @Override
    @Contract(pure = true)
    public int compareTo(@NotNull CymbalError o) {
        return location.compareTo(o.location);
    }
}