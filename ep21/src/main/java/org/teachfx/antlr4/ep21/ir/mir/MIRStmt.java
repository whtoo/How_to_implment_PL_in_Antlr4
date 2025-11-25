package org.teachfx.antlr4.ep21.ir.mir;

/**
 * MIR语句基类
 */
public abstract class MIRStmt extends MIRNode {
    
    @Override
    public int getComplexityLevel() {
        return 2; // 基本语句级别
    }
    
    public abstract void accept(MIRVisitor<?> visitor);
}