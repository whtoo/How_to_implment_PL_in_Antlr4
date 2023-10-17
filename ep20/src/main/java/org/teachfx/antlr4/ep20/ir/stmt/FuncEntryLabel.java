package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.symtab.scope.Scope;

public class FuncEntryLabel extends Label{

    public FuncEntryLabel(String funcName,int args,int locals, Scope scope) {
        super(".def %s: args=%d ,locals=%d".formatted(funcName,args,locals), scope);
    }


    @Override
    public String toSource() {
        return getRawLabel();
    }

    @Override
    public String toString() {
        return getRawLabel();
    }
}
