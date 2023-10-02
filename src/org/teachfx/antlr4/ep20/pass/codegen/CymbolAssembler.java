package org.teachfx.antlr4.ep20.pass.codegen;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.def.*;
import org.teachfx.antlr4.ep20.ir.expr.*;
import org.teachfx.antlr4.ep20.ir.stmt.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.teachfx.antlr4.ep20.symtab.OperatorType.BinaryOpType.*;


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
    public Void visit(IntVal node) {
        emit("iconst %d".formatted(node.value));
        return null;
    }

    @Override
    public Void visit(BoolVal node) {
        emit("iconst %d".formatted(node.value ? true : false));
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
            case EQ -> emit("if_icmpeq");
            case NE -> emit("if_icmpne");
            case LT -> emit("if_icmplt");
            case GT -> emit("if_icmpgt");
            case LE -> emit("if_icmple");
            case GE -> emit("if_icmpge");
            case AND -> emit("iand");
            case OR -> emit("ior");
        }
        return null;
    }

    @Override
    public Void visit(UnaryExpr node) {
        return null;
    }

    @Override
    public Void visit(CallFunc callFunc) {
        return null;
    }

    @Override
    public Void visit(LabelStmt labelStmt) {
        return null;
    }

    @Override
    public Void visit(JMP jmp) {
        return null;
    }

    @Override
    public Void visit(CJMP cjmp) {
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        return null;
    }

    @Override
    public Void visit(Func func) {
        return null;
    }

    @Override
    public Void visit(Var var) {
        return null;
    }
}
