package org.teachfx.antlr4.ep16.misc;

import java.util.*;

public class MemorySpace {

    private static final Map<String,Object> memory;
    private String name;
    public static final MemorySpace globalSpace;
    private MemorySpace enclosingSpace;
    static { 
        memory = new HashMap<String,Object>();
        globalSpace = new MemorySpace("global");
    }
    
    public MemorySpace(String name) {
        this(name, null);
    }
    public MemorySpace(String name,MemorySpace enclosingSpace) {
        this.name = name;
        this.enclosingSpace = enclosingSpace;
    }
    public String getName() {
        return name;
    }

    public void define(String name,Object value) {
        memory.put(name,value);
    }

    public Object get(String name) {
        return memory.get(name);
    }

    public MemorySpace getEnclosingSpace() {
        return enclosingSpace;
    }


}
