package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.IRVisitor;

/**
 * LIR无条件跳转指令
 * 用于表示goto/jump操作
 */
public class LIRJump extends LIRNode {
    private final String targetLabel;

    public LIRJump(String targetLabel) {
        if (targetLabel == null || targetLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("target label cannot be null or empty");
        }
        this.targetLabel = targetLabel;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.CONTROL_FLOW;
    }

    @Override
    public int getCost() {
        return 1; // 跳转指令成本较低
    }

    @Override
    public String toString() {
        return String.format("jmp %s", targetLabel);
    }

    public String getTargetLabel() {
        return targetLabel;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        // LIRJump暂不支持IRVisitor访问者模式
        return null;
    }
}
