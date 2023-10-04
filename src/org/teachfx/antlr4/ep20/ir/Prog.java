package org.teachfx.antlr4.ep20.ir;

import org.teachfx.antlr4.ep20.ir.def.Func;
import org.teachfx.antlr4.ep20.ir.stmt.Stmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Prog extends IRNode {
    public List<Func> defuncs;

    public Prog(List<Func> stmts) {
        this.defuncs = Objects.isNull(stmts) ? new ArrayList<>() : stmts;;
    }

    public <S,E> S accept(IRVisitor<S,E> visitor){

        return visitor.visit(this);
    }

    public void addFunc(Func func) {
        if (Objects.isNull(defuncs))
            defuncs = List.of(func);
        else if (!defuncs.contains(func) && func != null)
            defuncs.add(func);
    }
}
