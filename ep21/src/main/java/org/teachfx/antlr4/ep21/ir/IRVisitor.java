package org.teachfx.antlr4.ep21.ir;

import org.teachfx.antlr4.ep21.ir.def.Func;
import org.teachfx.antlr4.ep21.ir.expr.ArrayAccessExpr;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.ClassAccessExpr;
import org.teachfx.antlr4.ep21.ir.expr.Temp;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.expr.values.BoolVal;
import org.teachfx.antlr4.ep21.ir.expr.values.IntVal;
import org.teachfx.antlr4.ep21.ir.expr.values.StringVal;
import org.teachfx.antlr4.ep21.ir.expr.values.Var;
import org.teachfx.antlr4.ep21.ir.stmt.*;

public interface IRVisitor<S,E> {
    /// Expr
    E visit(IntVal node);
    E visit(BoolVal node);
    E visit(StringVal node);

    E visit(BinExpr node);

    E visit(UnaryExpr node);
    E visit(CallFunc callFunc);

    /// Stmt IRNodes
    S visit(Label label);
    S visit(JMP jmp);
    S visit(CJMP cjmp);
    S visit(Assign assign);

    S visit(Func func);

    E visit(Var var);

    E visit(ClassAccessExpr classAccessExpr);

    E visit(ArrayAccessExpr arrayAccessExpr);

    default S visit(Stmt stmt) { return stmt.accept(this);}

    S visit(ReturnVal returnVal);
    S visit(ExprStmt exprStmt);

    S visit(Prog prog);

    E visit(Temp temp);

    /// Stmt



}
