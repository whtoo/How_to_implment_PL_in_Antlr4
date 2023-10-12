package org.teachfx.antlr4.ep20.pass.codegen;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.Prog;
import org.teachfx.antlr4.ep20.ir.def.Func;
import org.teachfx.antlr4.ep20.ir.expr.*;
import org.teachfx.antlr4.ep20.ir.stmt.*;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep20.symtab.symbol.VariableSymbol;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


public class CymbolAssembler implements IRVisitor<Void,Void> {
    protected PrintWriter printWriter;
    protected StringWriter stringWriter;

    protected boolean emitted = false;

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
    }

    @Override
    public Void visit(Prog prog) {
        prog.defuncList.forEach(this::visit);
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

    @Override
    public Void visit(BinExpr node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
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
        node.expr.accept(this);

        switch (node.op) {
            case NEG -> emit("ineg");
            case NOT -> emit("inot");
        }

        return null;
    }

    @Override
    public Void visit(CallFunc callFunc) {

        callFunc.getArgs().forEach(expr -> expr.accept(this));
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
        emitNoPadding("%s :".formatted(label.toSource()));
        return null;
    }

    @Override
    public Void visit(JMP jmp) {
        emit("br %s".formatted(jmp.label.toSource()));
        return null;
    }

    @Override
    public Void visit(CJMP cjmp) {
        cjmp.cond.accept(this);
        emit("brt %s".formatted(cjmp.thenLabel.toSource()));
        emit("br %s".formatted(cjmp.elseLabel.toSource()));
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        var var = assign.getLhs();
        var expr = assign.getRhs();
        expr.accept(this);
        emit(var.toSource(false));
        return null;
    }

    @Override
    public Void visit(Func func) {
        emitNoPadding(func.toSource());
        func.getBody().forEach(this::visit);
        func.retHook.accept(this);
        return null;
    }

    @Override
    public Void visit(Stmt stmt) {
        stmt.accept(this);
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
        returnVal.retFuncLabel.accept(this);
        Optional.ofNullable(returnVal.getRetVal()).ifPresent(x -> x.accept(this));
        if (returnVal.isMainEntry()) { emit("halt"); }
        else { emit("ret"); }
        return null;
    }

    @Override
    public Void visit(ExprStmt exprStmt) {
        exprStmt.getExpr().accept(this);
        return null;
    }

    public void saveToFile(String filePath) throws IOException {
        Path path = null;
        try {
            // 创建多级文件夹
            path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            System.out.println("文件夹创建成功!");
        } catch (IOException e) {
            System.out.println("文件夹创建失败: " + e.getMessage());
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        var savedFile = new File(filePath);

        if (savedFile.exists()) {
            var os = new FileOutputStream(savedFile);
            var osw = new OutputStreamWriter(os);
            var pw = new PrintWriter(osw);
            pw.write(flushCode());
            pw.flush();
            pw.close();
        }
    }
}
