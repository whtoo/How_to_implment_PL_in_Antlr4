package org.teachfx.antlr4.ep09.ir;

import org.teachfx.antlr4.ep09.ir.stmt.Label;

public interface JMPInstr {
    public Label getTarget();
}
