package org.teachfx.antlr4.ep21.ir.mir;

import java.util.HashSet;
import java.util.Set;

/**
 * MIR表达式基类
 */
public abstract class MIRExpr extends MIRNode {
    
    @Override
    public int getComplexityLevel() {
        return 3; // 表达式级别
    }
    
    @Override
    public Set<String> getDefinedVariables() {
        return new HashSet<>(); // 表达式一般不定义变量
    }
    
    public abstract void accept(MIRVisitor<?> visitor);
}