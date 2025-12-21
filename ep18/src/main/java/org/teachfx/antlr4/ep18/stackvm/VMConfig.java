package org.teachfx.antlr4.ep18.stackvm;

/**
 * VMConfig - 虚拟机配置类
 * 提供虚拟机的各种配置选项
 */
public class VMConfig {
    // 内存配置
    private final int heapSize;
    private final int stackSize;
    private final int maxStackDepth;
    private final int maxFrameCount;
    
    // 性能配置
    private final boolean debugMode;
    private final boolean traceEnabled;
    private final boolean verboseErrors;
    private final int instructionCacheSize;
    
    // 执行配置
    private final int maxExecutionTime;
    private final boolean enableBoundsCheck;
    private final boolean enableTypeCheck;

    // GC配置
    private final boolean enableGC;
    private final String gcType;
    private final int gcHeapSize;
    
    private VMConfig(Builder builder) {
        this.heapSize = builder.heapSize;
        this.stackSize = builder.stackSize;
        this.maxStackDepth = builder.maxStackDepth;
        this.maxFrameCount = builder.maxFrameCount;
        this.debugMode = builder.debugMode;
        this.traceEnabled = builder.traceEnabled;
        this.verboseErrors = builder.verboseErrors;
        this.instructionCacheSize = builder.instructionCacheSize;
        this.maxExecutionTime = builder.maxExecutionTime;
        this.enableBoundsCheck = builder.enableBoundsCheck;
        this.enableTypeCheck = builder.enableTypeCheck;
        this.enableGC = builder.enableGC;
        this.gcType = builder.gcType;
        this.gcHeapSize = builder.gcHeapSize;
    }
    
    // Getters
    public int getHeapSize() {
        return heapSize;
    }
    
    public int getStackSize() {
        return stackSize;
    }
    
    public int getMaxStackDepth() {
        return maxStackDepth;
    }
    
    public int getMaxFrameCount() {
        return maxFrameCount;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public boolean isTraceEnabled() {
        return traceEnabled;
    }
    
    public boolean isVerboseErrors() {
        return verboseErrors;
    }
    
    public int getInstructionCacheSize() {
        return instructionCacheSize;
    }
    
    public int getMaxExecutionTime() {
        return maxExecutionTime;
    }
    
    public boolean isEnableBoundsCheck() {
        return enableBoundsCheck;
    }
    
    public boolean isEnableTypeCheck() {
        return enableTypeCheck;
    }

    // GC配置getters
    public boolean isEnableGC() {
        return enableGC;
    }

    public String getGcType() {
        return gcType;
    }

    public int getGcHeapSize() {
        return gcHeapSize;
    }

    /**
     * 创建构建器的静态工厂方法
     * @return 新的Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 配置构建器
     */
    public static class Builder {
        // 默认值
        private int heapSize = 1024 * 1024; // 1MB
        private int stackSize = 1024; // 1024个操作数
        private int maxStackDepth = 1000; // 最大调用深度
        private int maxFrameCount = 1000; // 最大栈帧数
        private boolean debugMode = false;
        private boolean traceEnabled = false;
        private boolean verboseErrors = false;
        private int instructionCacheSize = 1024; // 指令缓存大小
        private int maxExecutionTime = 60000; // 最大执行时间（毫秒）
        private boolean enableBoundsCheck = true;
        private boolean enableTypeCheck = true;

        // GC配置默认值
        private boolean enableGC = true;
        private String gcType = "reference-counting";
        private int gcHeapSize = 1024 * 1024; // 1MB
        
        public Builder() {}
        
        public Builder setHeapSize(int heapSize) {
            if (heapSize <= 0) {
                throw new IllegalArgumentException("Heap size must be positive");
            }
            this.heapSize = heapSize;
            return this;
        }
        
        public Builder setStackSize(int stackSize) {
            if (stackSize <= 0) {
                throw new IllegalArgumentException("Stack size must be positive");
            }
            this.stackSize = stackSize;
            return this;
        }
        
        public Builder setMaxStackDepth(int maxStackDepth) {
            if (maxStackDepth <= 0) {
                throw new IllegalArgumentException("Max stack depth must be positive");
            }
            this.maxStackDepth = maxStackDepth;
            return this;
        }
        
        public Builder setMaxFrameCount(int maxFrameCount) {
            if (maxFrameCount <= 0) {
                throw new IllegalArgumentException("Max frame count must be positive");
            }
            this.maxFrameCount = maxFrameCount;
            return this;
        }
        
        public Builder setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }
        
        public Builder setTraceEnabled(boolean traceEnabled) {
            this.traceEnabled = traceEnabled;
            return this;
        }
        
        public Builder setVerboseErrors(boolean verboseErrors) {
            this.verboseErrors = verboseErrors;
            return this;
        }
        
        public Builder setInstructionCacheSize(int instructionCacheSize) {
            if (instructionCacheSize <= 0) {
                throw new IllegalArgumentException("Instruction cache size must be positive");
            }
            this.instructionCacheSize = instructionCacheSize;
            return this;
        }
        
        public Builder setMaxExecutionTime(int maxExecutionTime) {
            if (maxExecutionTime <= 0) {
                throw new IllegalArgumentException("Max execution time must be positive");
            }
            this.maxExecutionTime = maxExecutionTime;
            return this;
        }
        
        public Builder setEnableBoundsCheck(boolean enableBoundsCheck) {
            this.enableBoundsCheck = enableBoundsCheck;
            return this;
        }
        
        public Builder setEnableTypeCheck(boolean enableTypeCheck) {
            this.enableTypeCheck = enableTypeCheck;
            return this;
        }

        // GC配置setters
        public Builder setEnableGC(boolean enableGC) {
            this.enableGC = enableGC;
            return this;
        }

        public Builder setGcType(String gcType) {
            if (gcType == null || gcType.trim().isEmpty()) {
                throw new IllegalArgumentException("GC type cannot be null or empty");
            }
            this.gcType = gcType;
            return this;
        }

        public Builder setGcHeapSize(int gcHeapSize) {
            if (gcHeapSize <= 0) {
                throw new IllegalArgumentException("GC heap size must be positive");
            }
            this.gcHeapSize = gcHeapSize;
            return this;
        }
        
        public VMConfig build() {
            return new VMConfig(this);
        }
    }
}