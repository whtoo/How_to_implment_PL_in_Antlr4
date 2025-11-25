package org.teachfx.antlr4.ep21.ir.mir;

import org.teachfx.antlr4.ep21.ir.IRNode;

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
    public abstract int getComplexityLevel();
    
    /**
     * 检查是否为基本块入口
     */
    public boolean isBasicBlockEntry() {
        return false;
    }
    
    /**
     * 获取依赖的变量集合
     */
    public abstract java.util.Set<String> getUsedVariables();
    
    /**
     * 获取定义的变量集合
     */
    public abstract java.util.Set<String> getDefinedVariables();
    
    /**
     * 接受访问者模式
     */
    public abstract void accept(MIRVisitor<?> visitor);
}