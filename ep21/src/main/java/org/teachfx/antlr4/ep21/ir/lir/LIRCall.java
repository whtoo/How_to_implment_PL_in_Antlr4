package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.IRVisitor;

import java.util.List;

/**
 * LIR函数调用指令
 * 用于表示函数调用操作
 */
public class LIRCall extends LIRNode {
    private final String functionName;
    private final List<Operand> arguments;
    private final Operand result;

    public LIRCall(String functionName, List<Operand> arguments, Operand result) {
        if (functionName == null || functionName.trim().isEmpty()) {
            throw new IllegalArgumentException("function name cannot be null or empty");
        }
        if (arguments == null) {
            throw new NullPointerException("arguments cannot be null");
        }
        this.functionName = functionName;
        this.arguments = List.copyOf(arguments); // 防御性拷贝
        this.result = result; // result可以为null（void函数）
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.FUNCTION_CALL;
    }

    @Override
    public int getCost() {
        // 函数调用成本较高
        return 10 + arguments.size();
    }

    @Override
    public String toString() {
        if (result != null) {
            return String.format("%s = call %s(%s)", result, functionName,
                String.join(", ", arguments.stream().map(Object::toString).toList()));
        } else {
            return String.format("call %s(%s)", functionName,
                String.join(", ", arguments.stream().map(Object::toString).toList()));
        }
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Operand> getArguments() {
        return arguments;
    }

    public Operand getResult() {
        return result;
    }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        // LIRCall暂不支持IRVisitor访问者模式
        return null;
    }
}
