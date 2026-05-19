package org.teachfx.antlr4.ep05;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.teachfx.antlr4.ep05.parser.CymbolLexer;
import org.teachfx.antlr4.ep05.parser.CymbolParser;
import org.teachfx.antlr4.ep05.parser.CymbolParser.*;
import org.teachfx.antlr4.ep05.symtab.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Stack;

public class Compiler {
    private static SymbolTable globalScope;
    private static Stack<Object> scopeStack = new Stack<>();

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

        // Phase 1: Collect symbols
        globalScope = new SymbolTable("global", null);
        scopeStack.push(globalScope);

        for (int i = 0; i < tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            collectSymbols(child);
        }

        // Phase 2: Print symbol table
        System.out.println("=== EP05 Symbol Table ===");
        System.out.println(SymbolTable.formatTable(globalScope));
    }

    private static void collectSymbols(ParseTree node) {
        if (node instanceof FunctionDeclContext f) {
            String name = f.funcName.getText();
            String retType = f.retType.getText();
            int line = f.start.getLine();
            FunctionSymbol fs = new FunctionSymbol(name, retType, "global", line, globalScope);
            globalScope.define(name, fs);

            scopeStack.push(fs);
            // Collect parameters
            var params = f.params;
            if (params != null) {
                for (FormalParameterContext p : params.formalParameter()) {
                    Symbol sym = new Symbol(p.ID().getText(), Symbol.Kind.PARAMETER,
                        p.type().getText(), name, p.start.getLine());
                    fs.define(sym.getName(), sym);
                }
            }
            // Recurse into block
            if (f.blockDef != null) collectSymbolsInBlock(f.blockDef, fs);
            scopeStack.pop();

        } else if (node instanceof VarDeclContext v) {
            String varName = v.ID().getText();
            String varType = v.type().getText();
            int line = v.start.getLine();
            Object currentScope = scopeStack.peek();
            Symbol sym = new Symbol(varName, Symbol.Kind.VARIABLE, varType,
                getScopeName(currentScope), line);
            if (currentScope instanceof SymbolTable st) st.define(varName, sym);
            else if (currentScope instanceof FunctionSymbol fs) fs.define(varName, sym);

        } else if (node instanceof StatVarDeclContext s) {
            VarDeclContext v = s.varDecl();
            String varName = v.ID().getText();
            String varType = v.type().getText();
            int line = v.start.getLine();
            Object currentScope = scopeStack.peek();
            Symbol sym = new Symbol(varName, Symbol.Kind.VARIABLE, varType,
                getScopeName(currentScope), line);
            if (currentScope instanceof SymbolTable st) st.define(varName, sym);
            else if (currentScope instanceof FunctionSymbol fs) fs.define(varName, sym);

        } else if (node instanceof BlockContext) {
            // Handled through collectSymbolsInBlock
        } else if (node instanceof StatBlockContext s) {
            Object currentScope = scopeStack.peek();
            collectSymbolsInBlock(s.block(), currentScope);

        } else if (node instanceof StatContext || node instanceof StatAssignContext ||
                   node instanceof StatReturnContext || node instanceof StateConditionContext ||
                   node instanceof StateWhileContext) {
            // Recurse into statement children
            for (int i = 0; i < node.getChildCount(); i++) {
                collectSymbols(node.getChild(i));
            }
        }
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
}
