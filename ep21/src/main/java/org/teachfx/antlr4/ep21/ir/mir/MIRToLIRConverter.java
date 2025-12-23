package org.teachfx.antlr4.ep21.ir.mir;

import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.lir.*;

import java.util.ArrayList;
import java.util.List;

/**
 * MIR到LIR的转换器
 * 将中级IR(MIR)转换为低级IR(LIR)
 */
public class MIRToLIRConverter implements MIRVisitor<Void> {

    private final List<LIRNode> lirInstructions;
    private final MIRToLIRContext context;

    public MIRToLIRConverter() {
        this.lirInstructions = new ArrayList<>();
        this.context = new MIRToLIRContext();
    }

    /**
     * 转换MIRFunction为LIR指令序列
     */
    public List<LIRNode> convert(MIRFunction mirFunction) {
        if (mirFunction == null) {
            throw new IllegalArgumentException("MIRFunction cannot be null");
        }

        lirInstructions.clear();
        context.reset();

        // 转换函数体中的每个MIR语句
        for (MIRStmt stmt : mirFunction.getStatements()) {
            stmt.accept(this);
        }

        return new ArrayList<>(lirInstructions);
    }

    /**
     * 转换单个MIR节点
     */
    public LIRNode convertNode(MIRNode mirNode) {
        if (mirNode == null) {
            throw new IllegalArgumentException("MIRNode cannot be null");
        }

        lirInstructions.clear();

        if (mirNode instanceof MIRStmt stmt) {
            stmt.accept(this);
            return lirInstructions.isEmpty() ? null : lirInstructions.get(0);
        } else if (mirNode instanceof MIRExpr expr) {
            return convertExpression(expr);
        }

        throw new UnsupportedOperationException("Unsupported MIRNode type: " + mirNode.getClass());
    }

    @Override
    public Void visit(MIRNode node) {
        if (node instanceof MIRAssignStmt assignStmt) {
            visit(assignStmt);
        } else {
            throw new UnsupportedOperationException("Unsupported MIRNode: " + node.getClass());
        }
        return null;
    }

    /**
     * 访问MIR赋值语句
     */
    public Void visit(MIRAssignStmt assignStmt) {
        String target = assignStmt.getTarget();
        MIRExpr source = assignStmt.getSource();

        // 转换源表达式为LIR操作数
        Operand sourceOperand = convertExpressionToOperand(source);

        // 创建目标操作数
        Operand targetOperand = createOperandFromVariable(target);

        // 生成LIR赋值指令
        LIRAssign lirAssign = new LIRAssign(targetOperand, sourceOperand, LIRAssign.RegisterType.REGISTER);
        lirInstructions.add(lirAssign);

        return null;
    }

    /**
     * 转换MIR表达式为LIR操作数
     */
    private Operand convertExpressionToOperand(MIRExpr expr) {
        // 简单变量引用
        if (expr.getUsedVariables().size() == 1) {
            String varName = expr.getUsedVariables().iterator().next();
            return createOperandFromVariable(varName);
        }

        // 复杂表达式需要临时变量
        return context.createTempOperand();
    }

    /**
     * 转换MIR表达式为LIR节点
     */
    private LIRNode convertExpression(MIRExpr expr) {
        // 这里可以扩展支持更多表达式类型
        // 目前返回一个占位的赋值指令
        Operand temp = context.createTempOperand();
        Operand constValue = FrameSlot.get(null); // 占位符
        return new LIRAssign(temp, constValue, LIRAssign.RegisterType.IMMEDIATE);
    }

    /**
     * 从变量名创建操作数
     */
    private Operand createOperandFromVariable(String varName) {
        return OperandSlot.genTemp(); // 简化实现
    }

    /**
     * 获取生成的LIR指令列表
     */
    public List<LIRNode> getLIRInstructions() {
        return new ArrayList<>(lirInstructions);
    }

    /**
     * 转换上下文，用于管理临时变量和标签
     */
    private static class MIRToLIRContext {
        private int tempCounter = 0;
        private int labelCounter = 0;

        public void reset() {
            tempCounter = 0;
            labelCounter = 0;
        }

        public Operand createTempOperand() {
            return OperandSlot.genTemp();
        }

        public String createLabel(String prefix) {
            return prefix + "_" + (labelCounter++);
        }
    }
}
