package org.teachfx.antlr4.ep20.pass.cfg;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.Prog;
import org.teachfx.antlr4.ep20.ir.expr.CallFunc;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep20.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep20.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep20.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep20.ir.stmt.*;
import org.teachfx.antlr4.ep20.ir.expr.Operand;

import java.util.HashSet;
import java.util.Set;

public class LivenessAnalysis implements IRVisitor<Void, Void> {
    // 用于跟踪当前的使用和定义集合
    private Set<Operand> currentUse = new HashSet<>();
    private Set<Operand> currentDef = new HashSet<>();

    public Set<Operand> getCurrentUse() {
        return currentUse;
    }

    public Set<Operand> getCurrentDef() {
        return currentDef;
    }

    public void reset() {
        currentUse.clear();
        currentDef.clear();
    }

    @Override
    public Void visit(BinExpr node) {
        // 二元表达式使用左右操作数
        reset();
        node.getLhs().accept(this);
        Set<Operand> lhsUse = new HashSet<>(currentUse);
        node.getRhs().accept(this);
        Set<Operand> rhsUse = new HashSet<>(currentUse);
        
        currentUse.addAll(lhsUse);
        currentUse.addAll(rhsUse);
        return null;
    }

    @Override
    public Void visit(UnaryExpr node) {
        // 一元表达式使用操作数
        reset();
        node.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(CallFunc callFunc) {
        // 函数调用使用所有参数
        reset();
        // 假设callFunc有方法获取参数，这里需要根据实际实现调整
        // 暂时置空use集，实际应根据参数计算
        currentUse = new HashSet<>();
        return null;
    }

    @Override
    public Void visit(Label label) {
        // 标签没有使用或定义
        reset();
        return null;
    }

    @Override
    public Void visit(JMP jmp) {
        // 跳转指令没有使用或定义
        reset();
        return null;
    }

    @Override
    public Void visit(CJMP cjmp) {
        // 条件跳转使用条件表达式
        reset();
        cjmp.cond.accept(this);
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        // 赋值语句：定义左侧，使用右侧
        reset();
        // 先访问右侧表达式获取use
        assign.getRhs().accept(this);
        Set<Operand> rhsUse = new HashSet<>(currentUse);
        
        // 左侧是定义，直接添加到def集合（不通过访问，因为访问会将其添加到use）
        reset();
        currentDef.add(assign.getLhs());
        
        currentUse = rhsUse;
        // currentDef已经设置
        return null;
    }

    @Override
    public Void visit(ReturnVal returnVal) {
        // 返回值使用返回表达式
        reset();
        if (returnVal.getRetVal() != null) {
            returnVal.getRetVal().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Prog prog) {
        // 程序节点遍历所有基本块
        reset();
        // 这里应该遍历所有函数或基本块，但Prog结构可能需要调整
        // 暂时返回null
        return null;
    }

    @Override
    public Void visit(OperandSlot operandSlot) {
        // 操作数槽是使用
        reset();
        currentUse.add(operandSlot);
        return null;
    }

    @Override
    public Void visit(FrameSlot frameSlot) {
        // 帧槽是使用
        reset();
        currentUse.add(frameSlot);
        return null;
    }

    @Override
    public <T> Void visit(ConstVal<T> tConstVal) {
        // 常量值没有使用或定义
        reset();
        return null;
    }
}
