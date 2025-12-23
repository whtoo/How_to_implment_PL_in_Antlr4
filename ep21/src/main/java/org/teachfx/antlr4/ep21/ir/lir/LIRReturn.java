package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.IRVisitor;

/**
 * LIR返回指令
 * 用于表示函数返回操作
 */
public class LIRReturn extends LIRNode {
    private final Operand returnValue;

    public LIRReturn(Operand returnValue) {
        // returnValue可以为null（void返回）
        this.returnValue = returnValue;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.CONTROL_FLOW;
    }

    @Override
    public int getCost() {
        return 5; // 返回指令涉及栈帧清理，成本较高
    }

    @Override
    public String toString() {
        if (returnValue != null) {
            return String.format("return %s", returnValue);
        } else {
            return "return";
        }
    }

    public Operand getReturnValue() {
        return returnValue;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        // LIRReturn暂不支持IRVisitor访问者模式
        return null;
    }
}
