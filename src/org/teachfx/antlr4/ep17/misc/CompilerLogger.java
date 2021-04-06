package org.teachfx.antlr4.ep17.misc;

import org.antlr.v4.runtime.ParserRuleContext;

public class CompilerLogger {
    
    public static void error(ParserRuleContext context, String message) {
        int lineNo = context.getStart().getLine();
        int col = context.getStart().getStartIndex();
        System.err.println("At line " + lineNo + "col " + col + ": " + message);
    }
    
}
