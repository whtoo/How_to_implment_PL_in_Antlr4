package org.teachfx.antlr4.ep20.ir;

import org.teachfx.antlr4.ep20.ir.stmt.Label;

public interface JMPInstr {
    public Label getTarget();
}
