package org.teachfx.antlr4.ep21.pass.cfg;

import org.jetbrains.annotations.NotNull;

public interface IOrdIdentity<I> extends Comparable<IOrdIdentity<I>> {
    I getID();
    void setID(I id);
}
