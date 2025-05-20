package org.teachfx.antlr4.ep19.runtime;

import org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.Symbol;

public class StructInstance extends MemorySpace {
    StructSymbol symbol;

    public StructInstance(String name, StructSymbol symbol) {
        super(name);
        this.symbol = symbol;
    }

    public StructInstance(String name, MemorySpace enclosingSpace, StructSymbol symbol) {
        super(name, enclosingSpace);
        this.symbol = symbol;
        // 初始化所有字段为默认值
        for (String key : symbol.getMembers().keySet()) {
            define(key, null);
        }
    }

    /**
     * 获取结构体字段值
     * @param fieldName 字段名
     * @return 字段值
     */
    public Object getField(String fieldName) {
        return get(fieldName);
    }
    
    /**
     * 设置结构体字段值
     * @param fieldName 字段名
     * @param value 字段值
     */
    public void setField(String fieldName, Object value) {
        define(fieldName, value);
    }
    
    /**
     * 检查结构体是否有指定字段
     * @param fieldName 字段名
     * @return 如果有该字段返回true，否则返回false
     */
    public boolean hasField(String fieldName) {
        Symbol member = symbol.resolveMember(fieldName);
        return member != null && !(member instanceof MethodSymbol);
    }
    
    /**
     * 获取结构体方法
     * @param methodName 方法名
     * @return 方法符号，如果不存在返回null
     */
    public MethodSymbol getMethod(String methodName) {
        Symbol member = symbol.resolveMember(methodName);
        if (member instanceof MethodSymbol) {
            return (MethodSymbol) member;
        }
        return null;
    }
    
    /**
     * 获取结构体类型符号
     * @return 结构体类型符号
     */
    public StructSymbol getStructSymbol() {
        return symbol;
    }
    
    @Override
    public String toString() {
        return "StructInstance:" + getName() + "(" + symbol.getName() + ")";
    }
}
