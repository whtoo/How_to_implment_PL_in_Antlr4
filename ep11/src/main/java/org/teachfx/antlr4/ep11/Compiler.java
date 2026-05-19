package org.teachfx.antlr4.ep11;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.teachfx.antlr4.ep11.ir.*;
import org.teachfx.antlr4.ep11.pass.CodeGenerator;
import org.teachfx.antlr4.ep11.parser.CymbolLexer;
import org.teachfx.antlr4.ep11.parser.CymbolParser;
import org.teachfx.antlr4.ep11.parser.CymbolParser.*;
import org.teachfx.antlr4.ep11.stackvm.*;
import org.teachfx.antlr4.ep11.symtab.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * EP11 — Complete Compiler Pipeline:
 *   Cymbol source → tokens → parse tree → AST → IR → VM bytecode → interpret → output
 */
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

        System.out.println("=== EP11: Cymbol Compiler Pipeline ===\n");

        // --- Phase 0: Parse ---
        CharStream cs = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokens);
        CymbolParser.FileContext tree = parser.file();

        // --- Phase 1: Build symbol table ---
        globalScope = new GlobalScope();
        for (int i = 0; i < tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            if (child instanceof FunctionDeclContext f) {
                MethodSymbol ms = new MethodSymbol(
                    f.funcName.getText(),
                    resolveType(f.retType.getText()),
                    globalScope);
                if (f.params != null) {
                    for (FormalParameterContext p : f.params.formalParameter()) {
                        ms.defineParam(p.ID().getText(), resolveType(p.type().getText()));
                    }
                }
                globalScope.define(ms);
            } else if (child instanceof VarDeclContext v) {
                globalScope.define(new VariableSymbol(
                    v.ID().getText(), resolveType(v.type().getText())));
            }
        }

        // --- Phase 2: Generate IR ---
        System.out.println("=== Phase 2: Generating IR ===\n");
        for (int i = 0; i < tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            if (child instanceof FunctionDeclContext f) {
                generateFunction(f);
            }
        }

        // --- Phase 3: Print IR ---
        System.out.println("=== Phase 3: IR Listing ===");
        for (IRFunction func : irFunctions) {
            func.printIR();
        }

        // --- Phase 4: CodeGen (IR → Bytecode) ---
        System.out.println("=== Phase 4: Code Generation (IR → Bytecode) ===\n");
        ByteCodeAssembler asm = new ByteCodeAssembler(BytecodeDefinition.instructions);
        CodeGenerator codegen = new CodeGenerator(asm, irFunctions);
        codegen.generate();

        // Print disassembly
        System.out.println("Disassembly:");
        System.out.println(asm.disassemble());

        // --- Phase 5: VM Execution ---
        System.out.println("=== Phase 5: VM Execution ===\n");
        VMInterpreter vm = new VMInterpreter();
        vm.init(asm);
        vm.execute();
        Object result = vm.getResult();
        if (result != null) {
            System.out.println("  main() returned: " + result);
        }
        System.out.println();
        System.out.println("=== Pipeline Complete ===");
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
        IRFunction irFunc = new IRFunction(f.funcName.getText(),
            f.retType != null ? f.retType.getText() : "void");
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
        if (block == null || block.statement() == null) return;
        for (StatementContext stmt : block.statement()) {
            generateStatement(stmt);
        }
    }

    private static void generateStatement(StatementContext stmt) {
        if (stmt instanceof StatVarDeclContext v) {
            VarDeclContext vd = v.varDecl();
            String varName = vd.ID().getText();
            // Check for initializer: vd.expr() may have array indices + initializer
            List<ExprContext> exprs = vd.expr();
            if (exprs != null && !exprs.isEmpty()) {
                // Last expression is the initializer value if there's '='
                // But we need to distinguish array dimension from initializer.
                // Simplification: if there's '=' in the text, use the last expression
                String txt = vd.getText();
                if (txt.contains("=")) {
                    String value = generateExpr(exprs.get(exprs.size() - 1));
                    irFunc().emit(new AssignIR(varName, value));
                }
            }
        } else if (stmt instanceof StatAssignContext a) {
            List<ExprContext> exprs = a.expr();
            if (exprs.size() >= 2) {
                String rhs = generateExpr(exprs.get(1));
                String lhs = generateExpr(exprs.get(0));
                irFunc().emit(new AssignIR(lhs, rhs));
            }
        } else if (stmt instanceof StatReturnContext r) {
            ExprContext retExpr = r.expr();
            if (retExpr != null) {
                String val = generateExpr(retExpr);
                irFunc().emit(new ReturnIR(val));
            } else {
                irFunc().emit(new ReturnIR(null));
            }
        } else if (stmt instanceof StatBlockContext b) {
            generateBlock(b.block());
        } else if (stmt instanceof StateConditionContext c) {
            // if (cond) thenStmt [else elseStmt]
            String cond = generateExpr(c.cond);
            String thenLabel = irFunc().freshLabel();
            String elseLabel = irFunc().freshLabel();
            String endLabel = irFunc().freshLabel();

            // Evaluate condition, branch to else if false
            irFunc().emit(new BranchIR(cond, thenLabel, elseLabel));

            // Then block
            irFunc().emit(new LabelIR(thenLabel));
            generateStatement(c.then);

            // Jump to end
            if (c.elseDo != null) {
                irFunc().emit(new JumpIR(endLabel));
            }

            // Else block
            irFunc().emit(new LabelIR(elseLabel));
            if (c.elseDo != null) {
                generateStatement(c.elseDo);
            }
            irFunc().emit(new LabelIR(endLabel));

        } else if (stmt instanceof StateWhileContext w) {
            String condLabel = irFunc().freshLabel();
            String bodyLabel = irFunc().freshLabel();
            String exitLabel = irFunc().freshLabel();

            // Condition label
            irFunc().emit(new LabelIR(condLabel));
            String cond = generateExpr(w.cond);
            irFunc().emit(new BranchIR(cond, bodyLabel, exitLabel));

            // Body
            irFunc().emit(new LabelIR(bodyLabel));
            generateStatement(w.then);
            irFunc().emit(new JumpIR(condLabel));

            // Exit
            irFunc().emit(new LabelIR(exitLabel));

        } else if (stmt instanceof ExprStatContext e) {
            ExprContext expr = e.expr();
            if (expr != null) {
                generateExpr(expr); // side-effects only, discard result
            }
        }
    }

    private static String generateExpr(ParseTree expr) {
        IRFunction irf = irFunc();

        if (expr instanceof ExprBinaryContext b) {
            List<ExprContext> exprs = b.expr();
            if (exprs.size() >= 2) {
                String left = generateExpr(exprs.get(0));
                String right = generateExpr(exprs.get(1));
                String op = b.o != null ? b.o.getText() : "+";
                String result = irf.freshTemp();
                irf.emit(new BinaryOpIR(result, left, op, right));
                return result;
            }
        }

        if (expr instanceof ExprLogicalAndContext b) {
            List<ExprContext> exprs = b.expr();
            if (exprs.size() >= 2) {
                String left = generateExpr(exprs.get(0));
                String right = generateExpr(exprs.get(1));
                String result = irf.freshTemp();
                irf.emit(new BinaryOpIR(result, left, "&&", right));
                return result;
            }
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
            // callFunc=expr '(' args ')'
            String funcName = generateExpr(fc.callFunc);
            List<String> args = new ArrayList<>();
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
