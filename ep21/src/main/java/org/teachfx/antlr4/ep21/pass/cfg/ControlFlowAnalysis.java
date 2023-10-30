package org.teachfx.antlr4.ep21.pass.cfg;

import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.Prog;
import org.teachfx.antlr4.ep21.ir.def.Func;
import org.teachfx.antlr4.ep21.ir.expr.*;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.expr.values.BoolVal;
import org.teachfx.antlr4.ep21.ir.expr.values.IntVal;
import org.teachfx.antlr4.ep21.ir.expr.values.StringVal;
import org.teachfx.antlr4.ep21.ir.expr.values.Var;
import org.teachfx.antlr4.ep21.ir.stmt.*;

public class ControlFlowAnalysis implements IRVisitor<Void,Void> {

    @Override
    public Void visit(IntVal node) {
        return null;
    }

    @Override
    public Void visit(BoolVal node) {
        return null;
    }

    @Override
    public Void visit(StringVal node) {
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
    public Void visit(Func func) {
        return null;
    }

    @Override
    public Void visit(Var var) {
        return null;
    }

    @Override
    public Void visit(ClassAccessExpr classAccessExpr) {
        return null;
    }

    @Override
    public Void visit(ArrayAccessExpr arrayAccessExpr) {
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
    public Void visit(Temp temp) {
        return null;
    }
}
