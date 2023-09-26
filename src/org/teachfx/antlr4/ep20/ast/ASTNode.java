package org.teachfx.antlr4.ep20.ast;
import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.debugger.Dumpable;
import org.teachfx.antlr4.ep20.debugger.Dumper;
import org.teachfx.antlr4.ep20.parser.Location;

import java.io.PrintStream;

abstract public class ASTNode implements Dumpable {
    public ParserRuleContext ctx;

    public Location getLocation() {
        if(ctx == null) { return null; }
        if(ctx.getStart() != ctx.getStop()) {
            return new Location(ctx.getStart().getLine(),ctx.getStart().getStartIndex(),ctx.stop.getLine(),ctx.stop.getStopIndex());
        }

        return new Location(ctx.getStart().getLine(),ctx.getStart().getStartIndex(),ctx.stop.getLine(),ctx.stop.getStopIndex());
    }

    public void accept(ASTVisitor visitor){}
    public void setCtx(ParserRuleContext ctx){
        this.ctx = ctx;
    }

    public void dump() {
        dump(System.out);
    }

    public void dump(PrintStream s) {
        dump(new Dumper(s));
    }

    public void dump(Dumper d) {
        d.printClass(this, getLocation());
        _dump(d);
    }

    abstract protected void _dump(Dumper d);
}

