package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.IRVisitor;

/**
 * LIR条件跳转指令
 * 用于表示if/条件跳转操作
 */
public class LIRCondJump extends LIRNode {
    private final Operand condition;
    private final String trueLabel;
    private final String falseLabel;

    public LIRCondJump(Operand condition, String trueLabel, String falseLabel) {
        if (condition == null) {
            throw new NullPointerException("condition cannot be null");
        }
        if (trueLabel == null || trueLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("true label cannot be null or empty");
        }
        if (falseLabel == null || falseLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("false label cannot be null or empty");
        }
        this.condition = condition;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.CONTROL_FLOW;
    }

    @Override
    public int getCost() {
        return 2; // 条件跳转成本稍高
    }

    @Override
    public String toString() {
        return String.format("cjmp %s, %s, %s", condition, trueLabel, falseLabel);
    }

    public Operand getCondition() {
        return condition;
    }

    public String getTrueLabel() {
        return trueLabel;
    }

    public String getFalseLabel() {
        return falseLabel;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        // LIRCondJump暂不支持IRVisitor访问者模式
        return null;
    }
}
