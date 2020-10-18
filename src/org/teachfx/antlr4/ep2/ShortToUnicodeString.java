package org.teachfx.antlr4.ep2;

import org.teachfx.antlr4.ep2.ArrayInitParser.ArrayContext;
import org.teachfx.antlr4.ep2.ArrayInitParser.ValueContext;

/** Convert short array init like {1,2,3} to "\u0001\u0002\u0003" */
public class ShortToUnicodeString extends ArrayInitBaseListener {
	@Override
	public void enterArray(ArrayContext ctx) {
        System.out.print('"');
	}

    @Override
    public void enterValue(ValueContext ctx) {
        int value = Integer.valueOf(ctx.INT().getText());
        System.out.printf("\\u%04x",value);
    }

    @Override
    public void exitArray(ArrayContext ctx) {
        System.out.print('"');
    }

    @Override
    public void exitValue(ValueContext ctx) {
        super.exitValue(ctx);
    }
    

}
