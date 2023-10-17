package org.teachfx.antlr4.ep20.pass.cfg;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.Prog;
import org.teachfx.antlr4.ep20.ir.expr.CallFunc;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.expr.addr.StackSlot;
import org.teachfx.antlr4.ep20.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep20.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep20.ir.expr.val.IntVal;
import org.teachfx.antlr4.ep20.ir.stmt.*;

public class ControlFlowAnalysis implements IRVisitor<Void,Void> {
    @Override
    public <T> Void visit(IntVal<T> tIntVal) {
        return null;
    }

    @Override
    public Void visit(BinExpr node) {
        return null;
    }

    @Override
    public Void visit(UnaryExpr node) {
        return null;
    }

    @Override
    public Void visit(CallFunc callFunc) {
        return null;
    }

    @Override
    public Void visit(Label label) {
        return null;
    }

    @Override
    public Void visit(JMP jmp) {
        return null;
    }

    @Override
    public Void visit(CJMP cjmp) {
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        return null;
    }

    @Override
    public Void visit(ReturnVal returnVal) {
        return null;
    }

    @Override
    public Void visit(ExprStmt exprStmt) {
        return null;
    }

    @Override
    public Void visit(Prog prog) {
        return null;
    }

    @Override
    public Void visit(StackSlot stackSlot) {
        return null;
    }

    @Override
    public Void visit(FrameSlot frameSlot) {
        return null;
    }
}
