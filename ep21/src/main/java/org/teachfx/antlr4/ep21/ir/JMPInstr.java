package org.teachfx.antlr4.ep21.ir;

import org.teachfx.antlr4.ep21.ir.stmt.Label;

public interface JMPInstr {
    public Label getTarget();
}
