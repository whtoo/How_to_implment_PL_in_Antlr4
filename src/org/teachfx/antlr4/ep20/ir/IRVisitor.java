package org.teachfx.antlr4.ep20.ir;

import org.teachfx.antlr4.ep20.ir.def.Func;
import org.teachfx.antlr4.ep20.ir.def.Var;
import org.teachfx.antlr4.ep20.ir.expr.*;
import org.teachfx.antlr4.ep20.ir.stmt.*;

public interface IRVisitor<S,E> {
    /// Expr
    E visit(IntVal node);
    E visit(BoolVal node);
    E visit(StringVal node);

    E visit(BinExpr node);

    E visit(UnaryExpr node);
    E visit(CallFunc callFunc);

    /// Stmt IRNodes
    S visit(LabelStmt labelStmt);
    S visit(JMP jmp);
    S visit(CJMP cjmp);
    S visit(Assign assign);

    S visit(Func func);

    S visit(Var var);




    /// Stmt



}
