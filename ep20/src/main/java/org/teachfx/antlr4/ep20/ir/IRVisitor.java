package org.teachfx.antlr4.ep20.ir;

import org.teachfx.antlr4.ep20.ir.expr.CallFunc;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.expr.addr.StackSlot;
import org.teachfx.antlr4.ep20.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep20.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep20.ir.expr.val.IntVal;
import org.teachfx.antlr4.ep20.ir.stmt.*;

public interface IRVisitor<S,E> {

    E visit(BinExpr node);

    E visit(UnaryExpr node);
    E visit(CallFunc callFunc);

    /// Stmt IRNodes
    S visit(Label label);
    S visit(JMP jmp);
    S visit(CJMP cjmp);
    S visit(Assign assign);

    default S visit(Stmt stmt) { return stmt.accept(this);}

    S visit(ReturnVal returnVal);
    S visit(ExprStmt exprStmt);

    S visit(Prog prog);

    E visit(StackSlot stackSlot);

    E visit(FrameSlot frameSlot);

    <T> E visit(IntVal<T> tIntVal);

    /// Stmt



}
