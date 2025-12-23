package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

/**
 * LIR二元运算指令
 * 用于表示算术和逻辑运算操作
 */
public class LIRBinaryOp extends LIRNode {
    private final OperatorType.BinaryOpType opType;
    private final Operand left;
    private final Operand right;
    private final Operand result;

    public LIRBinaryOp(OperatorType.BinaryOpType opType, Operand left, Operand right, Operand result) {
        if (opType == null) {
            throw new NullPointerException("operator type cannot be null");
        }
        if (left == null) {
            throw new NullPointerException("left operand cannot be null");
        }
        if (right == null) {
            throw new NullPointerException("right operand cannot be null");
        }
        if (result == null) {
            throw new NullPointerException("result operand cannot be null");
        }
        this.opType = opType;
        this.left = left;
        this.right = right;
        this.result = result;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.ARITHMETIC;
    }

    @Override
    public int getCost() {
        // 算术运算成本评估
        return switch (opType) {
            case MUL, DIV, MOD -> 3;  // 乘除法成本较高
            case ADD, SUB -> 1;       // 加减法成本低
            default -> 2;
        };
    }

    @Override
    public String toString() {
        return String.format("%s = %s %s %s", result, left, opType, right);
    }

    public OperatorType.BinaryOpType getOpType() {
        return opType;
    }

    public Operand getLeft() {
        return left;
    }

    public Operand getRight() {
        return right;
    }

    public Operand getResult() {
        return result;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        // LIRBinaryOp暂不支持IRVisitor访问者模式
        return null;
    }
}
