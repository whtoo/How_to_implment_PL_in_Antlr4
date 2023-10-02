package org.teachfx.antlr4.ep20.ir.def;

import org.teachfx.antlr4.ep20.ir.IRVisitor;

public class Var extends Def {

    public String name;

    public Var(String name) {
        this.name = name;
    }


    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}
