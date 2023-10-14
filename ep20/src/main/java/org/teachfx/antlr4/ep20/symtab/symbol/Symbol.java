package org.teachfx.antlr4.ep20.symtab.symbol;

import org.teachfx.antlr4.ep20.debugger.ast.Dumpable;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;
import org.teachfx.antlr4.ep20.symtab.type.Type;

import java.util.Objects;

public class Symbol implements Dumpable {
    static Type UNDEFINED;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isBuiltIn() {
        return false;
    }

    // Mark which type
    protected Type type;
    // Locate where I am.
    public Scope scope;

    private int baseOffset = 0;

    private int slotIdx = -1;

    public String name;

    public Symbol(String name) {
        this.name = name;
        this.type = UNDEFINED;
    }

    public Symbol(String name, Type type) {
        this(name);
        this.type = type != null ? type : UNDEFINED;
    }

    public static String stripBrackets(String s) {
        return s.substring(1, s.length() - 1);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        String s = "";
        if (scope != null) s = scope.getScopeName() + ".";
        if (type != null) return '<' + s + getName() + ":" + type + ">";
        return s + getName();
    }

    @Override
    public void dump(Dumper dumper) {
        dumper.printMember("symbol", toString());
    }

    public int getSlotIdx() {
        return slotIdx;
    }

    public void setSlotIdx(int slotIdx) {
        this.slotIdx = slotIdx;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Symbol symbol)) return false;
        return Objects.equals(getType(), symbol.getType()) && Objects.equals(getName(), symbol.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getName());
    }

    public int getBaseOffset() {
        return baseOffset;
    }

    public void setBaseOffset(int baseOffset) {
        this.baseOffset = baseOffset;
    }
}
