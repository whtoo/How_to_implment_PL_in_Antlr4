package org.teachfx.antlr4.ep19.misc;

import java.util.HashMap;
import java.util.Map;
/** 链式作用域
 * 最简单支持嵌套函数调用（直接递归与间接递归）的实现方式。
 * 它的优化体现在对查询symbol的优化上。
 * | - Key - | - Value - |
 * |    x    |     1     |
 * |    y    |     2     |
 * | ------- | --------- |
 * */
public class MemorySpace {

    private Map<String,Object> memory;
    private String name;
    public static final MemorySpace globalSpace;
    private MemorySpace enclosingSpace;
    static {
        globalSpace = new MemorySpace("global");
    }
    
    public MemorySpace(String name) {
        this(name, null);

    }
    public MemorySpace(String name,MemorySpace enclosingSpace) {
        this.name = name;
        this.enclosingSpace = enclosingSpace;
        this.memory = new HashMap<String,Object>();
    }
    public String getName() {
        return name;
    }


    public void update(String name,Object value) {
        if(memory.containsKey(name)) {
            memory.put(name,value);
        }

        if(this.getEnclosingSpace() != null) {
            this.getEnclosingSpace().update(name, value);
        }
    }

    public void define(String name,Object value) {
        memory.put(name,value);
    }

    
    public Object get(String name) {
        Object value = memory.get(name);
        if(value == null) { 
            if(this.getEnclosingSpace() != null) {
                value = this.getEnclosingSpace().get(name);
            }
        }
        return value;
    }

    public MemorySpace getEnclosingSpace() {
        return enclosingSpace;
    }


}
