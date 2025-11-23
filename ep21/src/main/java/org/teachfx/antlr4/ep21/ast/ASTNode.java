package org.teachfx.antlr4.ep21.ast;
import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.debugger.ast.Dumpable;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;
import org.teachfx.antlr4.ep21.parser.Location;

import java.io.PrintStream;

abstract public class ASTNode implements Dumpable {
    public ParserRuleContext ctx;
    public ParserRuleContext getCtx() {
        return  ctx;
    }
    public Location getLocation() {
        if(ctx == null) { return null; }
        // 简化实现，使用默认文件名
        String fileName = "unknown";
        if(ctx.getStart() != ctx.getStop()) {
            return new Location(fileName, ctx.getStart().getLine(), ctx.getStart().getStartIndex(), ctx.stop.getStopIndex());
        }

        return new Location(fileName, ctx.getStart().getLine(), ctx.getStart().getStartIndex(), ctx.stop.getStopIndex());
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

    @Override
    public String toString() {
        return ctx.getText();
    }
}

