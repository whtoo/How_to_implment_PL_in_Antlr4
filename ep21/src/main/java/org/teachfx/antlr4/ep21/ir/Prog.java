package org.teachfx.antlr4.ep21.ir;

import org.teachfx.antlr4.ep21.ir.def.Func;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Prog extends IRNode {
    public List<Func> defuncList;

    public Prog(List<Func> stmts) {
        this.defuncList = Objects.isNull(stmts) ? new ArrayList<>() : stmts;
    }

    public <S,E> S accept(IRVisitor<S,E> visitor){

        return visitor.visit(this);
    }

    public void addFunc(Func func) {
        if (Objects.isNull(defuncList))
            defuncList = List.of(func);
        else if (!defuncList.contains(func) && func != null)
            defuncList.add(func);
    }
}
