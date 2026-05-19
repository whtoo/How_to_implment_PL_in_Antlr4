package org.teachfx.antlr4.ep07;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.teachfx.antlr4.ep07.parser.CymbolLexer;
import org.teachfx.antlr4.ep07.parser.CymbolParser;
import org.teachfx.antlr4.ep07.parser.CymbolParser.*;
import org.teachfx.antlr4.ep07.symtab.*;

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

        System.out.println("=== EP07 Full Cymbol: 3-Pass Pipeline ===");

        // Pass 1: Define symbols
        System.out.println("\nPass 1: Define symbols...");
        globalScope = new SymbolTable("global", null);
        scopeStack.push(globalScope);

        for (int i = 0; i < tree.getChildCount(); i++) {
            collectSymbols(tree.getChild(i));
        }

        int funcCount = 0, varCount = 0, paramCount = 0;
        for (Symbol s : globalScope.getAllSymbols()) {
            if (s.getKind() == Symbol.Kind.FUNCTION) funcCount++;
            else if (s.getKind() == Symbol.Kind.VARIABLE) varCount++;
        }
        System.out.println("  Defined " + funcCount + " function(s), " + varCount + " global variable(s)");

        // Pass 2: Resolve references
        System.out.println("\nPass 2: Resolve references...");
        scopeStack.clear();
        scopeStack.push(globalScope);
        int resolvedRefs = 0;
        for (int i = 0; i < tree.getChildCount(); i++) {
            resolvedRefs += resolveRefs(tree.getChild(i));
        }
        System.out.println("  Resolved " + resolvedRefs + " reference(s)");

        // Pass 3: Type check
        System.out.println("\nPass 3: Type check...");
        typeErrors.clear();
        scopeStack.clear();
        scopeStack.push(globalScope);
        for (int i = 0; i < tree.getChildCount(); i++) {
            typeCheck(tree.getChild(i));
        }

        if (typeErrors.isEmpty()) {
            System.out.println("  Type Check: PASS");
        } else {
            for (String err : typeErrors) {
                System.out.println("  " + err);
            }
            System.out.println("  Type Check: FAIL (" + typeErrors.size() + " error(s))");
        }

        System.out.println("\n=== Pipeline Complete ===");
    }

    // === Pass 1: Define symbols ===
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

    // === Pass 2: Resolve references ===
    private static int resolveRefs(ParseTree node) {
        int count = 0;
        if (node instanceof ExprFuncCallContext fc) {
            String funcName = fc.ID().getText();
            Symbol sym = resolveSymbol(funcName);
            if (sym != null) count++;
        } else if (node instanceof PrimaryIDContext) {
            String name = node.getText();
            Symbol sym = resolveSymbol(name);
            if (sym != null) count++;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            count += resolveRefs(node.getChild(i));
        }
        return count;
    }

    // === Pass 3: Type check ===
    private static void typeCheck(ParseTree node) {
        if (node instanceof VarDeclContext v) {
            checkVarDecl(v);
        } else if (node instanceof StatVarDeclContext s) {
            checkVarDecl(s.varDecl());
        } else if (node instanceof StatAssignContext s) {
            String lhsType = inferType(s.expr(0));
            String rhsType = inferType(s.expr(1));
            checkTypeMatch(s.getStart().getLine(), lhsType, rhsType, "assignment");
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

    private static void checkTypeMatch(int line, String expected, String actual, String context) {
        if (expected == null || actual == null) return;
        if (expected.equals(actual)) return;
        typeErrors.add("ERROR line " + line + ": type mismatch in " + context +
            " — expected '" + expected + "' but got '" + actual + "'");
    }

    private static String inferType(ParseTree expr) {
        if (expr instanceof ExprBinaryContext b) {
            String left = inferType(b.expr(0));
            String right = inferType(b.expr(1));
            if (left == null || right == null) return null;
            if (left.equals("float") || right.equals("float")) return "float";
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

    private static String getScopeName(Object scope) {
        if (scope instanceof SymbolTable st) return st.getScopeName();
        if (scope instanceof FunctionSymbol fs) return fs.getScopeName();
        return "?";
    }
}
