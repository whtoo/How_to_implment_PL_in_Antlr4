package org.teachfx.antlr4.ep20.pass.cfg;

public interface IOrdIdentity<I> extends Comparable<IOrdIdentity<I>> {
    I getID();
    void setID(I id);
}
