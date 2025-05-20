package org.teachfx.antlr4.ep19.symtab.symbol;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep19.symtab.Type;
import org.teachfx.antlr4.ep19.symtab.scope.Scope;
import org.teachfx.antlr4.ep19.symtab.scope.ScopedSymbol;

import java.util.LinkedHashMap;
import java.util.Map;

public class StructSymbol extends ScopedSymbol implements Type {

    // 结构体字段
    private Map<String, Symbol> fields = new LinkedHashMap<>();
    
    // 结构体方法
    private Map<String, MethodSymbol> methods = new LinkedHashMap<>();

    public StructSymbol(String name, Scope parent,
                        ParserRuleContext tree) {
        super(name, parent, tree);
    }

    /**
     * For a.b, only look in fields to resolve b, not up scope tree
     */
    public Symbol resolveMember(String name) {
        // 先检查字段
        Symbol field = fields.get(name);
        if (field != null) {
            return field;
        }
        
        // 再检查方法
        return methods.get(name);
    }

    /**
     * 添加结构体字段
     * @param fieldSymbol 字段符号
     */
    public void addField(Symbol fieldSymbol) {
        fields.put(fieldSymbol.getName(), fieldSymbol);
        define(fieldSymbol); // 同时添加到作用域中
    }
    
    /**
     * 添加结构体方法
     * @param methodSymbol 方法符号
     */
    public void addMethod(MethodSymbol methodSymbol) {
        methods.put(methodSymbol.getName(), methodSymbol);
        define(methodSymbol); // 同时添加到作用域中
    }
    
    /**
     * 获取结构体所有字段
     */
    public Map<String, Symbol> getFields() {
        return fields;
    }
    
    /**
     * 获取结构体所有方法
     */
    public Map<String, MethodSymbol> getMethods() {
        return methods;
    }

    @Override
    public Map<String, Symbol> getMembers() {
        // 合并字段和方法
        Map<String, Symbol> allMembers = new LinkedHashMap<>(fields);
        allMembers.putAll(methods);
        return allMembers;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("struct ").append(name).append(" {\n");
        
        // 添加字段信息
        for (Symbol field : fields.values()) {
            sb.append("  ").append(field.type).append(" ").append(field.getName()).append(";\n");
        }
        
        // 添加方法信息
        for (MethodSymbol method : methods.values()) {
            sb.append("  ").append(method.type).append(" ").append(method.getName()).append("();\n");
        }
        
        sb.append("}");
        return sb.toString();
    }
}
