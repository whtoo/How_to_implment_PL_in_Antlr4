package org.teachfx.antlr4.ep06.symtab;

public class Symbol {
    public enum Kind { VARIABLE, FUNCTION, PARAMETER, TYPE }

    protected String name;
    protected Kind kind;
    protected String typeName;
    protected String scopeName;
    protected int line;

    public Symbol(String name, Kind kind, String typeName, String scopeName, int line) {
        this.name = name;
        this.kind = kind;
        this.typeName = typeName;
        this.scopeName = scopeName;
        this.line = line;
    }

    public String getName() { return name; }
    public Kind getKind() { return kind; }
    public String getTypeName() { return typeName; }
    public String getScopeName() { return scopeName; }
    public int getLine() { return line; }
}
