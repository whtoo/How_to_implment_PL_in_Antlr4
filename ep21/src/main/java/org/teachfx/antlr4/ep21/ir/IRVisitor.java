package org.teachfx.antlr4.ep21.ir;

import org.teachfx.antlr4.ep21.ir.expr.ArrayAccess;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.ir.lir.LIRArrayInit;
import org.teachfx.antlr4.ep21.ir.lir.LIRArrayLoad;
import org.teachfx.antlr4.ep21.ir.lir.LIRArrayStore;
import org.teachfx.antlr4.ep21.ir.lir.LIRNewArray;

public interface IRVisitor<S,E> {

    E visit(BinExpr node);

    E visit(UnaryExpr node);
    E visit(CallFunc callFunc);

    /// Stmt IRNodes
    S visit(Label label);
    S visit(JMP jmp);
    S visit(CJMP cjmp);
    S visit(Assign assign);
    S visit(ArrayAssign arrayAssign);

    default S visit(Stmt stmt) { return stmt.accept(this);}

    S visit(ReturnVal returnVal);
    default S visit(ExprStmt exprStmt) { return exprStmt.accept(this); }

    default S visit(Prog prog) { return null; }

    E visit(OperandSlot operandSlot);

    E visit(FrameSlot frameSlot);

    <T> E visit(ConstVal<T> tConstVal);
    E visit(ArrayAccess arrayAccess);

    /// LIR Instructions
    <S, E> S visit(LIRArrayInit lirArrayInit);
    <S, E> S visit(LIRArrayLoad lirArrayLoad);
    <S, E> S visit(LIRArrayStore lirArrayStore);
    <S, E> S visit(LIRNewArray lirNewArray);

    /// Stmt



}
