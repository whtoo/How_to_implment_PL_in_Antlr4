package org.teachfx.antlr4.ep19.symtab.symbol;

import org.teachfx.antlr4.ep19.symtab.Type;

/**
 * 基本类型符号，表示int、float等基础类型
 */
public class PrimitiveType extends Symbol implements Type {
    
    public PrimitiveType(String name) {
        super(name);
    }
    
    @Override
    public boolean isPrimitive() {
        return true;
    }
    
    @Override
    public String toString() {
        return name;
    }
} 