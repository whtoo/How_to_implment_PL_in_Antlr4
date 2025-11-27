package org.teachfx.antlr4.ep21.ir.lir;

import org.teachfx.antlr4.ep21.ir.expr.Operand;

/**
 * LIR赋值指令，低层表示，便于平台相关优化
 */
public class LIRAssign extends LIRNode {
    private final Operand target;
    private final Operand source;
    private final RegisterType registerType;
    
    public enum RegisterType {
        REGISTER, MEMORY, IMMEDIATE
    }
    
    public LIRAssign(Operand target, Operand source, RegisterType type) {
        if (target == null) {
            throw new NullPointerException("target operand cannot be null");
        }
        if (source == null) {
            throw new NullPointerException("source operand cannot be null");
        }
        if (type == null) {
            throw new NullPointerException("register type cannot be null");
        }
        System.out.println("DEBUG LIRAssign: target=" + target + ", source=" + source + ", type=" + type);
        this.target = target;
        this.source = source;
        this.registerType = type;
    }
    
    @Override
    public InstructionType getInstructionType() {
        return InstructionType.DATA_TRANSFER;
    }
    
    @Override
    public boolean hasMemoryAccess() {
        return registerType == RegisterType.MEMORY;
    }
    
    @Override
    public boolean hasRegisterOperation() {
        return registerType == RegisterType.REGISTER;
    }
    
    @Override
    public int getCost() {
        // 成本评估：寄存器操作 < 内存操作 < 立即数
        switch (registerType) {
            case REGISTER: return 1;
            case MEMORY: return 2;
            case IMMEDIATE: return 0;
            default: return 1;
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s = %s", target, source);
    }
    
    public Operand getTarget() {
        return target;
    }
    
    public Operand getSource() {
        return source;
    }
    
    public RegisterType getRegisterType() {
        return registerType;
    }
}