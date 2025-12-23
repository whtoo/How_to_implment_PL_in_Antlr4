package org.teachfx.antlr4.ep21.ir.mir;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.IRVisitor;

import java.util.Set;

/**
 * MIR (Medium-level Intermediate Representation) 节点基类
 * MIR更接近源代码抽象，保留了程序的高级结构信息
 */
public abstract class MIRNode extends IRNode {

    /**
     * 获取MIR节点的复杂度级别
     * 0: 最高级抽象（函数、类等）
     * 1: 控制结构（if、while等）
     * 2: 基本语句（赋值、调用等）
     * 3: 表达式
     */
    @Override
    public abstract int getComplexityLevel();

    /**
     * 检查是否为基本块入口
     */
    @Override
    public boolean isBasicBlockEntry() {
        return false;
    }

    /**
     * 获取依赖的变量集合
     */
    @Override
    public abstract Set<String> getUsedVariables();

    /**
     * 获取定义的变量集合
     */
    @Override
    public abstract Set<String> getDefinedVariables();

    /**
     * 接受MIR访问者模式
     */
    public abstract void accept(MIRVisitor<?> visitor);

    /**
     * 提供默认的accept方法以支持IRVisitor接口
     * MIR节点主要使用MIRVisitor，但也提供基本的IRVisitor支持
     */
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        // MIR节点不支持通用的IRVisitor
        // 这里提供默认实现，子类可以覆盖
        return null;
    }
}