package org.teachfx.antlr4.ep05.symtab;

import java.util.*;

public class SymbolTable implements Scope {
    private final String scopeName;
    private final Scope enclosingScope;
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();
    private final List<Symbol> allSymbols = new ArrayList<>();

    public SymbolTable(String scopeName, Scope enclosingScope) {
        this.scopeName = scopeName;
        this.enclosingScope = enclosingScope;
    }

    @Override
    public String getScopeName() { return scopeName; }

    @Override
    public Scope getEnclosingScope() { return enclosingScope; }

    @Override
    public void define(String name, Symbol sym) {
        symbols.put(name, sym);
        allSymbols.add(sym);
    }

    @Override
    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if (s != null) return s;
        if (enclosingScope != null) return enclosingScope.resolve(name);
        return null;
    }

    /** Resolve only in this scope, no parent lookup */
    public Symbol resolveLocal(String name) {
        return symbols.get(name);
    }

    public List<Symbol> getAllSymbols() { return allSymbols; }

    public Map<String, Symbol> getSymbols() { return symbols; }

    /** Print a formatted table of all symbols in this scope and nested scopes */
    public static String formatTable(SymbolTable globalScope) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-4s %-18s %-12s %-8s %-15s %s\n",
            "Line", "Name", "Kind", "Type", "Scope", "Full Path"));
        sb.append("---- ------------------ ---------- -------- --------------- ----\n");

        List<String[]> rows = new ArrayList<>();
        collectRows(globalScope, "", rows);

        for (String[] row : rows) {
            sb.append(String.format("%-4s %-18s %-12s %-8s %-15s %s\n",
                row[0], row[1], row[2], row[3], row[4], row[5]));
        }
        sb.append(String.format("\nTotal symbols: %d\n", rows.size()));
        return sb.toString();
    }

    private static void collectRows(Scope scope, String prefix, List<String[]> rows) {
        if (scope instanceof SymbolTable st) {
            for (Symbol sym : st.getAllSymbols()) {
                String fullPath = prefix.isEmpty() ? sym.getName()
                    : prefix + "." + sym.getName();
                rows.add(new String[]{
                    String.valueOf(sym.getLine()),
                    sym.getName(),
                    sym.getKind().name(),
                    sym.getTypeName(),
                    sym.getScopeName(),
                    fullPath
                });
            }
            // Recurse into nested scopes (function scopes)
            for (Symbol sym : st.getAllSymbols()) {
                if (sym instanceof FunctionSymbol fs) {
                    collectRows(fs, prefix.isEmpty() ? sym.getName() : prefix + "." + sym.getName(), rows);
                }
            }
        } else if (scope instanceof FunctionSymbol fs) {
            for (Symbol sym : fs.getSymbols().values()) {
                String fullPath = prefix + ":" + sym.getName();
                rows.add(new String[]{
                    String.valueOf(sym.getLine()),
                    sym.getName(),
                    sym.getKind().name(),
                    sym.getTypeName(),
                    sym.getScopeName(),
                    fullPath
                });
            }
        }
    }
}
