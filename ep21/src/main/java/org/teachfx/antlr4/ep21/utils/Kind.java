package org.teachfx.antlr4.ep21.utils;

public enum Kind {
    /// No jump instruction
    CONTINUOUS,
    /// Conditional instruction
    END_BY_CJMP,
    /// Unconditional jump instruction
    END_BY_JMP,
    /// Return instruction
    END_BY_RETURN
}
