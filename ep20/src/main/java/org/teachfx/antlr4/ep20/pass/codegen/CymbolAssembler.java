package org.teachfx.antlr4.ep20.pass.codegen;

import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.CallFunc;
import org.teachfx.antlr4.ep20.ir.expr.Expr;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep20.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep20.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep20.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep20.ir.stmt.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CymbolAssembler implements IRVisitor<Void,Void> {
    private LinkedList<String> assembleCmdBuffer = new LinkedList<>();
    protected IOperatorEmitter operatorEmitter = new CymbolVMIOperatorEmitter();
    protected void setAssembleCmdBuffer(LinkedList<String> assembleCmdBuffer) {
        this.assembleCmdBuffer = assembleCmdBuffer;
    }

    protected LinkedList<String> getAssembleCmdBuffer() {
        return assembleCmdBuffer;
    }

    public String getAsmInfo() {
        return String.join("\n", assembleCmdBuffer).concat("\n");
    }
    private int indents = 0;

    public Void visit(List<IRNode> linearInstrs) {
        for (var instr: linearInstrs) {
            if (instr instanceof Expr) {
                ((Expr) instr).accept(this);
            } else  {
                ((Stmt) instr).accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visit(BinExpr node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
        emit(operatorEmitter.emitBinaryOp(node.getOpType()));
        return null;
    }

    @Override
    public Void visit(UnaryExpr node) {
        node.expr.accept(this);
        emit(operatorEmitter.emitUnaryOp(node.op));
        return null;
    }

    @Override
    public Void visit(CallFunc callFunc) {
        if (!callFunc.getFuncType().isBuiltIn()){
            emit("call %s()".formatted(callFunc.getFuncName()));
        } else {
            emit("%s".formatted(callFunc.getFuncName()));
        }

        return null;
    }

    @Override
    public Void visit(Label label) {
        // label不存在嵌套可能，下面这种情况必定是空块导致的label悬空。
        if (indents > 0) { indents--; }

        if (label instanceof FuncEntryLabel){
            // reset indent
            indents = 0;
            emit("%s".formatted(label.toSource()));
        } else {
            emit("%s:".formatted(label.toSource()));
        }
        indents++;
        return null;
    }

    @Override
    public Void visit(JMP jmp) {
        emit("br %s".formatted(jmp.getNext().toString()));
        indents--;
        return null;
    }

    @Override
    public Void visit(CJMP cjmp) {
        emit("brf %s".formatted(cjmp.getElseBlock().getLabel().toString()));
        indents--;
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        assign.getRhs().accept(this);

        if (assign.getLhs() instanceof FrameSlot frameSlot) {
            emit("store %d".formatted(frameSlot.getSlotIdx()));
        }

        return null;
    }

    @Override
    public Void visit(ReturnVal returnVal) {
        if(Objects.nonNull(returnVal.getRetVal())) {
            returnVal.getRetVal().accept(this);
        }
        if (returnVal.isMainEntry()){
            emit("halt");
        } else {
            emit("ret");
        }
        indents--;
        return null;
    }

    @Override
    public Void visit(OperandSlot operandSlot) {
        return null;
    }

    @Override
    public Void visit(FrameSlot frameSlot) {
        emit("load %d".formatted(frameSlot.getSlotIdx()));
        return null;
    }

    @Override
    public <T> Void visit(ConstVal<T> tConstVal) {
        if (tConstVal.getVal() instanceof Integer integer) {
            emit("iconst %d".formatted(integer));
        } else if (tConstVal.getVal() instanceof String str) {
            emit("sconst \"%s\"".formatted(str));
        } else if (tConstVal.getVal() instanceof Boolean bool) {
            emit("bconst %d".formatted(bool?1:0));
        }

        return null;
    }

    protected void emit(String cmd) {
        var indentCmdBuf = "    ".repeat(indents)+cmd;
        assembleCmdBuffer.add(indentCmdBuf);
    }
}
