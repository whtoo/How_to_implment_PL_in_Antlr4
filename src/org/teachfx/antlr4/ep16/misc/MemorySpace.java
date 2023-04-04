package org.teachfx.antlr4.ep16.misc;

import java.util.HashMap;
import java.util.Map;

/**
 * 链式作用域
 * 最简单支持嵌套函数调用（直接递归与间接递归）的实现方式。
 * 它的优化体现在对查询symbol的优化上。
 */
public class MemorySpace {

    public static final MemorySpace globalSpace;

    static {
        globalSpace = new MemorySpace("global");
    }

    private final Map<String, Object> memory;
    private final String name;
    private final MemorySpace enclosingSpace;

    public MemorySpace(String name) {
        this(name, null);

    }

    public MemorySpace(String name, MemorySpace enclosingSpace) {
        this.name = name;
        this.enclosingSpace = enclosingSpace;
        this.memory = new HashMap<String, Object>();
    }

    public String getName() {
        return name;
    }


    public void update(String name, Object value) {
        if (memory.containsKey(name)) {
            memory.put(name, value);
        }

        if (this.getEnclosingSpace() != null) {
            this.getEnclosingSpace().update(name, value);
        }
    }

    public void define(String name, Object value) {
        memory.put(name, value);
    }


    public Object get(String name) {
        Object value = memory.get(name);
        if (value == null) {
            if (this.getEnclosingSpace() != null) {
                value = this.getEnclosingSpace().get(name);
            }
        }
        return value;
    }

    public MemorySpace getEnclosingSpace() {
        return enclosingSpace;
    }


}
