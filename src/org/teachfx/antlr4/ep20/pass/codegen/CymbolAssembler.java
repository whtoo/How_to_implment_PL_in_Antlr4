package org.teachfx.antlr4.ep20.pass.codegen;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.Prog;
import org.teachfx.antlr4.ep20.ir.def.*;
import org.teachfx.antlr4.ep20.ir.expr.*;
import org.teachfx.antlr4.ep20.ir.stmt.*;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep20.symtab.symbol.VariableSymbol;

import java.io.*;
import java.io.OutputStreamWriter;


public class CymbolAssembler implements IRVisitor<Void,Void> {
    protected PrintWriter printWriter;
    protected StringWriter stringWriter;

    protected boolean emitted = false;

    private CymbolAssembler(PrintWriter printWriter) throws FileNotFoundException {
        this.printWriter = printWriter;
    };

    public CymbolAssembler() {
        this.stringWriter = new StringWriter();
        this.printWriter = new PrintWriter(stringWriter);
    }

    protected void emit(String str) {
        this.printWriter.write("    %s\n".formatted(str));
    }

    protected void emitNoPadding(String str) {
        this.printWriter.write("%s\n".formatted(str));
    }

    public String flushCode() {
        if(!emitted) {
            this.printWriter.flush();
            this.printWriter.close();
            emitted = true;
            return this.stringWriter.toString();
        }

        return this.stringWriter.toString();
    };

    @Override
    public Void visit(Prog prog) {
        prog.defuncs.forEach(this::visit);
        return null;
    }

    @Override
    public Void visit(IntVal node) {
        emit("iconst %d".formatted(node.value));
        return null;
    }

    @Override
    public Void visit(BoolVal node) {
        emit("iconst %b".formatted(node.value));
        return null;
    }

    @Override
    public Void visit(StringVal node) {
        emit("sconst %s".formatted(node.value));
        return null;
    }

    protected void visit(Expr expr) {
        if (expr instanceof BinExpr) {
            visit((BinExpr) expr);
        } else if (expr instanceof UnaryExpr) {
            visit((UnaryExpr) expr);
        } else if (expr instanceof CallFunc) {
            visit((CallFunc) expr);
        } else if (expr instanceof IntVal) {
            visit((IntVal) expr);
        } else if (expr instanceof BoolVal) {
            visit((BoolVal) expr);
        } else if (expr instanceof StringVal) {
            visit((StringVal) expr);
        } else {
            visit((Var) expr);
        }
    }
    @Override
    public Void visit(BinExpr node) {
        visit(node.getLhs());
        visit(node.getRhs());
        switch (node.getOpType()) {
            case ADD -> emit("iadd");
            case SUB -> emit("isub");
            case MUL -> emit("imul");
            case DIV -> emit("idiv");
            case MOD -> emit("irem");
            case EQ -> emit("ieq");
            case NE -> emit("ine");
            case LT -> emit("ilt");
            case GT -> emit("igt");
            case LE -> emit("ile");
            case GE -> emit("ige");
            case AND -> emit("iand");
            case OR -> emit("ior");
        }
        return null;
    }

    @Override
    public Void visit(UnaryExpr node) {
        visit(node.expr);

        switch (node.op) {
            case NEG -> emit("ineg");
            case NOT -> emit("inot");
        }

        return null;
    }

    @Override
    public Void visit(CallFunc callFunc) {

        callFunc.getArgs().forEach(this::visit);
        var varDef =  callFunc.getFuncExpr();
        var methodSymbol = (MethodSymbol) varDef.getSymbol();
        if(!methodSymbol.isPreDefined()) {
            emit("call %s()".formatted(varDef.getDeclName()));
        } else {
            emit("%s".formatted(varDef.getDeclName()));
        }
        return null;
    }

    @Override
    public Void visit(Label label) {
        emitNoPadding(label.toSource());
        return null;
    }

    @Override
    public Void visit(JMP jmp) {
        return null;
    }

    @Override
    public Void visit(CJMP cjmp) {
        visit(cjmp.cond);
        emit("brt %s".formatted(cjmp.thenLabel));
        visit(cjmp.elseLabel);
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        var var = assign.getLhs();
        var expr = assign.getRhs();
        visit(expr);
        emit(var.toSource(false));
        return null;
    }

    @Override
    public Void visit(Func func) {
        emitNoPadding(func.toSource());
        func.getBody().forEach(this::visit);
        return null;
    }

    @Override
    public Void visit(Stmt stmt) {
        switch (stmt.getStmtType()) {
            case ASSIGN -> visit((Assign) stmt);
            case JMP -> visit((JMP) stmt);
            case CJMP -> visit((CJMP) stmt);
            case LABEL -> visit((Label) stmt);
            case EXPR -> visit((ExprStmt) stmt);
            case RETURN -> visit((ReturnVal) stmt);
        }
        return null;
    }

    @Override
    public Void visit(Var var) {
        if(var.getSymbol() instanceof VariableSymbol) {
            emit(var.toSource(true));
        }
        return null;
    }

    @Override
    public Void visit(ArrayAccessExpr arrayAccessExpr) {
        return null;
    }

    @Override
    public Void visit(ClassAccessExpr classAccessExpr) {
        return null;
    }

    @Override
    public Void visit(ReturnVal returnVal) {
        visit(returnVal.getRetVal());
        emit("ret");
        return null;
    }

    @Override
    public Void visit(ExprStmt exprStmt) {
        visit(exprStmt.getExpr());
        return null;
    }

    public void saveToFile(File savedFile) throws FileNotFoundException {
        var os = new FileOutputStream(savedFile);
        var osw = new OutputStreamWriter(os);
        var pw = new PrintWriter(osw);
        pw.write(flushCode());
        pw.flush();
        pw.close();
    }
}
