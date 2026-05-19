package org.teachfx.antlr4.ep08;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.teachfx.antlr4.ep08.parser.CymbolLexer;
import org.teachfx.antlr4.ep08.parser.CymbolParser;
import org.teachfx.antlr4.ep08.parser.CymbolParser.*;
import org.teachfx.antlr4.ep08.ir.*;
import org.teachfx.antlr4.ep08.symtab.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class Compiler {
    private static GlobalScope globalScope;
    private static List<IRFunction> irFunctions = new ArrayList<>();
    private static Deque<IRFunction> functionStack = new ArrayDeque<>();

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

        System.out.println("=== EP08 IR: Three-Address Code ===");

        // Phase 1: Build symbol table
        globalScope = new GlobalScope();
        for (int i = 0; i < tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            if (child instanceof FunctionDeclContext f) {
                MethodSymbol ms = new MethodSymbol(f.funcName.getText(),
                    resolveType(f.retType.getText()), globalScope);
                if (f.params != null) {
                    for (FormalParameterContext p : f.params.formalParameter()) {
                        ms.defineParam(p.ID().getText(), resolveType(p.type().getText()));
                    }
                }
                globalScope.define(ms);
            } else if (child instanceof VarDeclContext v) {
                globalScope.define(new VariableSymbol(v.ID().getText(),
                    resolveType(v.type().getText())));
            }
        }

        // Phase 2: Generate IR for each function
        System.out.println("Generating IR...\n");
        for (int i = 0; i < tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            if (child instanceof FunctionDeclContext f) {
                generateFunction(f);
            }
        }

        // Phase 3: Print IR listing
        System.out.println("=== IR Listing ===");
        for (IRFunction func : irFunctions) {
            func.printIR();
        }

        System.out.println("=== " + irFunctions.size() + " function(s) compiled to IR ===");
    }

    private static Type resolveType(String name) {
        if (name == null) return TypeTable.VOID;
        return switch (name) {
            case "int" -> TypeTable.INT;
            case "float" -> TypeTable.FLOAT;
            case "void" -> TypeTable.VOID;
            case "bool" -> TypeTable.BOOL;
            case "string" -> TypeTable.STRING;
            default -> TypeTable.VOID;
        };
    }

    // === IR Generation ===

    private static void generateFunction(FunctionDeclContext f) {
        IRFunction irFunc = new IRFunction(f.funcName.getText(), f.retType.getText());
        functionStack.push(irFunc);

        if (f.blockDef != null) {
            generateBlock(f.blockDef);
        }

        List<IRInstruction> instrs = irFunc.instructions;
        if (instrs.isEmpty() || !(instrs.get(instrs.size() - 1) instanceof ReturnIR)) {
            irFunc.emit(new ReturnIR(null));
        }

        functionStack.pop();
        irFunctions.add(irFunc);
    }

    private static void generateBlock(BlockContext block) {
        if (block == null) return;
        for (StatementContext stmt : block.statement()) {
            generateStatement(stmt);
        }
    }

    private static void generateStatement(StatementContext stmt) {
        if (stmt instanceof StatVarDeclContext v) {
            VarDeclContext vd = v.varDecl();
            List<ExprContext> exprs = vd.expr();
            if (!exprs.isEmpty()) {
                // Last expr is the initializer value
                String value = generateExpr(exprs.get(exprs.size() - 1));
                irFunc().emit(new AssignIR(vd.ID().getText(), value));
            }
        } else if (stmt instanceof StatAssignContext a) {
            String rhs = generateExpr(a.expr(1));
            String lhs = generateExpr(a.expr(0));
            irFunc().emit(new AssignIR(lhs, rhs));
        } else if (stmt instanceof StatReturnContext r) {
            var retExpr = r.expr();
            if (retExpr != null) {
                String val = generateExpr(retExpr);
                irFunc().emit(new ReturnIR(val));
            } else {
                irFunc().emit(new ReturnIR(null));
            }
        } else if (stmt instanceof StatBlockContext b) {
            generateBlock(b.block());
        } else if (stmt instanceof ExprStatContext e) {
            generateExpr(e.expr());
        }
    }

    private static String generateExpr(ParseTree expr) {
        IRFunction irf = irFunc();

        if (expr instanceof ExprBinaryContext b) {
            String left = generateExpr(b.expr(0));
            String right = generateExpr(b.expr(1));
            String op = b.o != null ? b.o.getText() : "+";
            String result = irf.freshTemp();
            irf.emit(new BinaryOpIR(result, left, op, right));
            return result;
        }

        if (expr instanceof ExprLogicalAndContext b) {
            String left = generateExpr(b.expr(0));
            String right = generateExpr(b.expr(1));
            String result = irf.freshTemp();
            irf.emit(new BinaryOpIR(result, left, "&&", right));
            return result;
        }

        if (expr instanceof ExprUnaryContext) {
            String operand = generateExpr(expr.getChild(1));
            String op = expr.getChild(0).getText();
            String result = irf.freshTemp();
            irf.emit(new UnaryOpIR(result, op, operand));
            return result;
        }

        if (expr instanceof ExprPrimaryContext p) {
            return p.primary().getText();
        }

        if (expr instanceof ExprGroupContext) {
            return generateExpr(expr.getChild(1));
        }

        if (expr instanceof ExprFuncCallContext fc) {
            // callFunc=expr '(' args ')' — get function name from callFunc
            String funcName = generateExpr(fc.callFunc);
            List<String> args = new ArrayList<>();
            // fc.expr() returns ALL expr children (callFunc + arguments)
            List<ExprContext> allExprs = fc.expr();
            for (int i = 1; i < allExprs.size(); i++) {
                args.add(generateExpr(allExprs.get(i)));
            }
            String result = irf.freshTemp();
            irf.emit(new CallIR(result, funcName, args));
            return result;
        }

        return expr.getText();
    }

    private static IRFunction irFunc() {
        return functionStack.peek();
    }
}
