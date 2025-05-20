package org.teachfx.antlr4.ep19.symtab.symbol;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep19.symtab.Type;

/**
 * 类型别名符号，类似C语言的typedef
 */
public class TypedefSymbol extends Symbol implements Type {
    
    /**
     * 引用的类型
     */
    private Type targetType;
    
    public TypedefSymbol(String name, Type targetType) {
        super(name);
        this.targetType = targetType;
    }
    
    public TypedefSymbol(String name, Type targetType, ParserRuleContext ctx) {
        super(name);
        this.targetType = targetType;
    }
    
    /**
     * 获取别名指向的实际类型
     */
    public Type getTargetType() {
        // 处理嵌套的typedef
        if (targetType instanceof TypedefSymbol) {
            return ((TypedefSymbol) targetType).getTargetType();
        }
        return targetType;
    }
    
    /**
     * 设置别名指向的目标类型
     * @param targetType 目标类型
     */
    public void setTargetType(Type targetType) {
        this.targetType = targetType;
    }
    
    @Override
    public boolean isPrimitive() {
        Type actualType = getTargetType();
        // 如果目标类型未定义，返回false
        return actualType != null && actualType.isPrimitive();
    }
    
    @Override
    public String toString() {
        return name + " -> " + targetType;
    }
} 