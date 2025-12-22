package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.IRVisitor;

/**
 * LIR (Low-level Intermediate Representation) 节点基类
 * LIR更接近目标机器代码，便于代码生成和优化
 */
public abstract class LIRNode extends IRNode {
    
    /**
     * 获取LIR指令类型
     */
    public abstract InstructionType getInstructionType();
    
    /**
     * 检查是否涉及内存访问
     */
    public boolean hasMemoryAccess() {
        return false;
    }
    
    /**
     * 检查是否涉及寄存器操作
     */
    public boolean hasRegisterOperation() {
        return false;
    }
    
    /**
     * 获取目标架构相关的成本评估
     */
    public abstract int getCost();

    /**
     * 接受访问者模式
     */
    public abstract <S, E> S accept(IRVisitor<S, E> visitor);
    
    /**
     * 指令类型枚举
     */
    public enum InstructionType {
        DATA_TRANSFER,    // 数据传送
        ARITHMETIC,       // 算术运算
        LOGICAL,          // 逻辑运算
        CONTROL_FLOW,     // 控制流
        FUNCTION_CALL,    // 函数调用
        MEMORY_ACCESS,    // 内存访问
        COMPARE           // 比较操作
    }
}