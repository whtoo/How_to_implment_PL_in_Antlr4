package org.teachfx.antlr4.ep20.misc;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.lang.reflect.Field;

public class Util {

    public static String name(ParserRuleContext ctx) {
        
        try {
            TerminalNode id = (TerminalNode) ctx.getClass().getMethod("ID", new Class[0])
                    .invoke(ctx, new Object[0]);
            if(id != null) { return id.getSymbol().getText(); }
            else { return ctx.start.getText(); }
        } catch (Throwable e) {
            throw new IllegalStateException("Context does not have an ID to derive name from "
                    + ctx.getClass() + "\nCause was\n" + e);
        }
    }
    
    public static boolean isArrayDeclaration(ParserRuleContext ctx) {
        try {
            Field array = ctx.getClass().getField("array");
            return array.get(ctx) != null;
        } catch (Throwable e) {
            throw new IllegalStateException("Context does not have an array field ["
                    + ctx.getClass() + "]\nCause was\n" + e);
        }
    }

}
