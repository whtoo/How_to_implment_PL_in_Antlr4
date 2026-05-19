package org.teachfx.antlr4.ep06;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.teachfx.antlr4.ep06.parser.CymbolLexer;
import org.teachfx.antlr4.ep06.parser.CymbolParser;
import org.teachfx.antlr4.ep06.parser.CymbolParser.*;
import org.teachfx.antlr4.ep06.symtab.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class Compiler {
    private static SymbolTable globalScope;
    private static Stack<Object> scopeStack = new Stack<>();
    private static List<String> typeErrors = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        String fileName = "src/main/resources/t.cymbol";
        if (args.length > 0) fileName = args[0];

        InputStream is;
        try { is = new FileInputStream(fileName); }
        catch (Exception e) { is = System.in; }

        CharStream cs = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokens);
        FileContext tree = parser.file();

        System.out.println("=== EP06 Type Checking ===");

        // Phase 1: Collect symbols
        globalScope = new SymbolTable("global", null);
        scopeStack.push(globalScope);

        for (int i = 0; i < tree.getChildCount(); i++) {
            collectSymbols(tree.getChild(i));
        }

        System.out.println("Symbols collected: " + countAllSymbols(globalScope));

        // Phase 2: Type check
        typeErrors.clear();
        for (int i = 0; i < tree.getChildCount(); i++) {
            typeCheck(tree.getChild(i));
        }

        // Print results
        if (typeErrors.isEmpty()) {
            System.out.println("Type Check: PASS");
        } else {
            for (String err : typeErrors) {
                System.out.println(err);
            }
            System.out.println("Type Check: FAIL (" + typeErrors.size() + " errors)");
        }

        System.out.println("\n=== Symbol Table ===");
        System.out.println(SymbolTable.formatTable(globalScope));
    }

    private static int countAllSymbols(SymbolTable scope) {
        int count = scope.getAllSymbols().size();
        for (Symbol sym : scope.getAllSymbols()) {
            if (sym instanceof FunctionSymbol fs) {
                count += fs.getSymbols().size();
            }
        }
        return count;
    }

    // === Symbol collection (same as EP05) ===
    private static void collectSymbols(ParseTree node) {
        if (node instanceof FunctionDeclContext f) {
            String name = f.funcName.getText();
            String retType = f.retType.getText();
            int line = f.start.getLine();
            FunctionSymbol fs = new FunctionSymbol(name, retType, "global", line, globalScope);
            globalScope.define(name, fs);

            scopeStack.push(fs);
            if (f.params != null) {
                for (FormalParameterContext p : f.params.formalParameter()) {
                    Symbol sym = new Symbol(p.ID().getText(), Symbol.Kind.PARAMETER,
                        p.type().getText(), name, p.start.getLine());
                    fs.define(sym.getName(), sym);
                }
            }
            if (f.blockDef != null) collectSymbolsInBlock(f.blockDef, fs);
            scopeStack.pop();

        } else if (node instanceof VarDeclContext v) {
            defineVar(v.ID().getText(), v.type().getText(), v.start.getLine());

        } else if (node instanceof StatVarDeclContext s) {
            VarDeclContext v = s.varDecl();
            defineVar(v.ID().getText(), v.type().getText(), v.start.getLine());

        } else if (node instanceof StatBlockContext s) {
            Object currentScope = scopeStack.peek();
            collectSymbolsInBlock(s.block(), currentScope);

        } else if (node instanceof StatContext || node instanceof StatAssignContext ||
                   node instanceof StatReturnContext || node instanceof StateConditionContext ||
                   node instanceof StateWhileContext) {
            for (int i = 0; i < node.getChildCount(); i++) {
                collectSymbols(node.getChild(i));
            }
        }
    }

    private static void defineVar(String varName, String varType, int line) {
        Object currentScope = scopeStack.peek();
        Symbol sym = new Symbol(varName, Symbol.Kind.VARIABLE, varType,
            getScopeName(currentScope), line);
        if (currentScope instanceof SymbolTable st) st.define(varName, sym);
        else if (currentScope instanceof FunctionSymbol fs) fs.define(varName, sym);
    }

    private static void collectSymbolsInBlock(BlockContext block, Object parentScope) {
        if (block == null) return;
        SymbolTable blockScope = new SymbolTable("block", (parentScope instanceof Scope s) ? s : globalScope);
        scopeStack.push(blockScope);
        for (StatementContext stmt : block.statement()) {
            collectSymbols(stmt);
        }
        scopeStack.pop();
    }

    private static String getScopeName(Object scope) {
        if (scope instanceof SymbolTable st) return st.getScopeName();
        if (scope instanceof FunctionSymbol fs) return fs.getScopeName();
        return "?";
    }

    // === Type checking ===
    private static void typeCheck(ParseTree node) {
        if (node instanceof VarDeclContext v) {
            checkVarDecl(v);
        } else if (node instanceof StatVarDeclContext s) {
            checkVarDecl(s.varDecl());
        } else if (node instanceof StatAssignContext s) {
            checkAssign(s);
        } else if (node instanceof StatReturnContext s) {
            checkReturn(s);
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                typeCheck(node.getChild(i));
            }
        }
    }

    private static void checkVarDecl(VarDeclContext v) {
        if (v.expr() != null) {
            String varType = v.type().getText();
            String exprType = inferType(v.expr());
            checkTypeMatch(v.start.getLine(), varType, exprType,
                "variable '" + v.ID().getText() + "'");
        }
    }

    private static void checkAssign(StatAssignContext s) {
        String lhsType = inferType(s.expr(0));
        String rhsType = inferType(s.expr(1));
        Token start = s.getStart();
        checkTypeMatch(start.getLine(), lhsType, rhsType, "assignment");
    }

    private static void checkReturn(StatReturnContext s) {
        // Walk up to find enclosing function
        // For simplicity, just check the expression type
    }

    private static void checkTypeMatch(int line, String expected, String actual, String context) {
        if (expected == null || actual == null) return;
        if (expected.equals(actual)) return;
        // int and float are somewhat compatible but we flag it as a warning/error for demo
        typeErrors.add("ERROR line " + line + ": type mismatch in " + context +
            " — expected '" + expected + "' but got '" + actual + "'");
    }

    private static String inferType(ParseTree expr) {
        if (expr instanceof ExprBinaryContext b) {
            String left = inferType(b.expr(0));
            String right = inferType(b.expr(1));
            if (left == null || right == null) return null;
            // If either is float, result is float
            if (left.equals("float") || right.equals("float")) return "float";
            // Comparison operators return int (acting as bool)
            String op = b.o != null ? b.o.getText() : "";
            if (op.equals("==") || op.equals("!=") || op.equals(">") ||
                op.equals(">=") || op.equals("<") || op.equals("<=")) return "int";
            return left;
        }
        if (expr instanceof ExprUnaryContext) {
            return inferType(expr.getChild(1));
        }
        if (expr instanceof ExprPrimaryContext p) {
            return inferPrimary(p.primary());
        }
        if (expr instanceof ExprGroupContext) {
            return inferType(expr.getChild(1));
        }
        if (expr instanceof ExprFuncCallContext fc) {
            // Look up function return type
            String funcName = fc.ID().getText();
            Symbol sym = resolveSymbol(funcName);
            if (sym != null && sym.getKind() == Symbol.Kind.FUNCTION) return sym.getTypeName();
            return null;
        }
        return null;
    }

    private static String inferPrimary(ParseTree primary) {
        if (primary instanceof PrimaryINTContext) return "int";
        if (primary instanceof PrimaryFLOATContext) return "float";
        if (primary instanceof PrimaryIDContext) {
            String name = primary.getText();
            Symbol sym = resolveSymbol(name);
            return sym != null ? sym.getTypeName() : null;
        }
        return null;
    }

    private static Symbol resolveSymbol(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Object scope = scopeStack.get(i);
            if (scope instanceof Scope s) {
                Symbol sym = s.resolve(name);
                if (sym != null) return sym;
            }
        }
        return null;
    }
}
