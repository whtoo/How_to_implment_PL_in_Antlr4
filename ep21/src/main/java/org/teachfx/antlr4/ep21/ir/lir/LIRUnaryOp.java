package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

/**
 * LIR一元运算指令
 * 用于表示取反、逻辑非等一元操作
 */
public class LIRUnaryOp extends LIRNode {
    private final OperatorType.UnaryOpType opType;
    private final Operand operand;
    private final Operand result;

    public LIRUnaryOp(OperatorType.UnaryOpType opType, Operand operand, Operand result) {
        if (opType == null) {
            throw new NullPointerException("operator type cannot be null");
        }
        if (operand == null) {
            throw new NullPointerException("operand cannot be null");
        }
        if (result == null) {
            throw new NullPointerException("result operand cannot be null");
        }
        this.opType = opType;
        this.operand = operand;
        this.result = result;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.LOGICAL;
    }

    @Override
    public int getCost() {
        return 1; // 一元运算成本较低
    }

    @Override
    public String toString() {
        return String.format("%s = %s %s", result, opType, operand);
    }

    public OperatorType.UnaryOpType getOpType() {
        return opType;
    }

    public Operand getOperand() {
        return operand;
    }

    public Operand getResult() {
        return result;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        // LIRUnaryOp暂不支持IRVisitor访问者模式
        return null;
    }
}
