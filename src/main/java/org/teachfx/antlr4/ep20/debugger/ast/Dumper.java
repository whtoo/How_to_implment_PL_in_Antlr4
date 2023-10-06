package org.teachfx.antlr4.ep20.debugger.ast;

import org.teachfx.antlr4.ep20.ast.type.*;
import org.teachfx.antlr4.ep20.parser.Location;
import org.teachfx.antlr4.ep20.symtab.type.Type;

import java.io.PrintStream;
import java.util.List;

public class Dumper {
    protected int nIndent;
    protected PrintStream stream;

    public Dumper(PrintStream s) {
        this.stream = s;
        this.nIndent = 0;
    }

    public void printClass(Object obj, Location loc) {
        printIndent();
        stream.println("<<" + obj.getClass().getSimpleName() + ">> (" + loc + ")");
    }

    public void printNodeList(String name, List<? extends Dumpable> nodes) {
        if(nodes == null || nodes.isEmpty()) return;
        printIndent();
        stream.println(name + ":");
        indent();
        for (Dumpable n : nodes) {
            n.dump(this);
        }
        unindent();
    }

    public void printMember(String name, int n) {
        printPair(name, "" + n);
    }

    public void printMember(String name, long n) {
        printPair(name, "" + n);
    }

    public void printMember(String name, boolean b) {
        printPair(name, "" + b);
    }

    public void printMember(String name, Type t) {
        printPair(name, (t == null ? "null" : t.toString()));
    }

    public void printMember(String name, String str, boolean isResolved) {
        printPair(name, str
                + (isResolved ? " (resolved)" : ""));
    }

    public void printMember(String name, String str) {
        printMember(name, str, false);
    }

    protected void printPair(String name, String value) {
        printIndent();
        stream.println(name + ": " + value);
    }

    public void printMember(String name, TypeNode n) {
        printIndent();
        stream.println(name + ": " + n.getBaseType());
    }

    public void printMember(String name, Dumpable n) {
        printIndent();
        if (n == null) {
            stream.println(name + ": null");
        }
        else {
            stream.println(name + ":");
            indent();
            n.dump(this);
            unindent();
        }
    }

    protected void indent() { nIndent++; }
    protected void unindent() { nIndent--; }

    static final protected String indentString = "    ";

    protected void printIndent() {
        int n = nIndent;
        while (n > 0) {
            stream.print(indentString);
            n--;
        }
    }
}
