# EP20寄存器分配API设计文档

## 1. 概述

本文档定义EP20编译器寄存器分配模块的API接口设计，确保各组件间的清晰接口和松耦合架构。

## 2. 核心接口设计

### 2.1 RegisterAllocator (主入口接口)

```java
/**
 * 寄存器分配器主接口
 */
public interface RegisterAllocator {
    
    /**
     * 执行寄存器分配
     * @param irNodes 优化后的IR指令序列
     * @param registerCount 可用物理寄存器数量
     * @return 分配后的IR指令序列
     */
    List<IRNode> allocate(List<IRNode> irNodes, int registerCount);
    
    /**
     * 获取分配结果详细信息
     */
    AllocationResult getAllocationResult();
    
    /**
     * 设置溢出处理策略
     */
    void setSpillStrategy(SpillStrategy strategy);
    
    /**
     * 设置调试输出级别
     */
    void setDebugLevel(DebugLevel level);
}
```

### 2.2 LivenessAnalyzer (活性分析接口)

```java
/**
 * 活性分析器接口
 */
public interface LivenessAnalyzer {
    
    /**
     * 分析IR指令序列的活性信息
     */
    LivenessResult analyze(List<IRNode> irNodes);
    
    /**
     * 获取变量的活性区间
     */
    LiveRange getLiveRange(Operand operand);
    
    /**
     * 获取基本块的入口/出口活性集合
     */
    Set<Operand> getLiveIn(BasicBlock block);
    Set<Operand> getLiveOut(BasicBlock block);
}
```

### 2.3 ConflictGraph (冲突图接口)

```java
/**
 * 冲突图接口
 */
public interface ConflictGraph {
    
    /**
     * 添加冲突边
     */
    void addConflict(Operand op1, Operand op2);
    
    /**
     * 移除节点
     */
    void removeNode(Operand operand);
    
    /**
     * 获取节点度
     */
    int getDegree(Operand operand);
    
    /**
     * 获取邻居节点
     */
    Set<Operand> getNeighbors(Operand operand);
    
    /**
     * 图可视化输出
     */
    String toGraphviz();
}
```

## 3. 数据结构设计

### 3.1 AllocationResult (分配结果)

```java
/**
 * 寄存器分配结果
 */
public class AllocationResult {
    private final Map<Operand, Integer> registerMap;
    private final List<Operand> spilledOperands;
    private final int spillCost;
    private final boolean success;
    private final String errorMessage;
    
    // 构造函数、getter方法和辅助方法
    public int getRegister(Operand op);
    public boolean isSpilled(Operand op);
    public boolean isSuccess();
}
```

### 3.2 LivenessResult (活性分析结果)

```java
/**
 * 活性分析结果
 */
public class LivenessResult {
    private final Map<Operand, LiveRange> liveRanges;
    private final Map<BasicBlock, Set<Operand>> liveInMap;
    private final Map<BasicBlock, Set<Operand>> liveOutMap;
    private final Set<Operand> allOperands;
    
    public LiveRange getLiveRange(Operand op);
    public Set<Operand> getLiveIn(BasicBlock block);
    public Set<Operand> getLiveOut(BasicBlock block);
}
```

### 3.3 LiveRange (活性区间)

```java
/**
 * 变量活性区间
 */
public class LiveRange {
    private final Operand operand;
    private final int startPoint;
    private final int endPoint;
    private final Set<Integer> usePoints;
    private final Set<Integer> defPoints;
    
    public boolean overlaps(LiveRange other);
    public boolean contains(int point);
}
```

## 4. 策略模式接口

### 4.1 SpillStrategy (溢出策略)

```java
/**
 * 溢出处理策略接口
 */
public interface SpillStrategy {
    
    /**
     * 选择要溢出的变量
     */
    Operand chooseSpillCandidate(ConflictGraph graph, 
                               LivenessResult liveness);
    
    /**
     * 计算溢出成本
     */
    int calculateSpillCost(Operand operand, 
                          LivenessResult liveness);
}
```

### 4.2 ColoringStrategy (着色策略)

```java
/**
 * 图着色策略接口
 */
public interface ColoringStrategy {
    
    /**
     * 执行图着色
     */
    Map<Operand, Integer> colorGraph(ConflictGraph graph, 
                                   int registerCount,
                                   Set<Operand> precolored);
}
```

## 5. 异常处理设计

### 5.1 异常类体系

```java
/**
 * 寄存器分配异常基类
 */
public class RegisterAllocationException extends RuntimeException {
    public RegisterAllocationException(String message);
    public RegisterAllocationException(String message, Throwable cause);
}

/**
 * 图着色失败异常
 */
public class GraphColoringException extends RegisterAllocationException {
    private final ConflictGraph conflictGraph;
    private final int registerCount;
}

/**
 * 溢出处理异常
 */
public class SpillHandlingException extends RegisterAllocationException {
    private final Operand spillCandidate;
    private final int spillCost;
}
```

## 6. 配置和调优参数

### 6.1 配置类设计

```java
/**
 * 寄存器分配配置
 */
public class AllocationConfig {
    
    // 寄存器数量配置
    private int generalPurposeRegisters = 6;
    private int reservedRegisters = 2;
    
    // 算法参数
    private int maxSpillIterations = 10;
    private double spillCostThreshold = 0.8;
    
    // 调试配置
    private boolean enableVisualization = false;
    private DebugLevel debugLevel = DebugLevel.INFO;
    
    // Getter和Setter方法
}
```

## 7. 监控和统计接口

### 7.1 统计信息收集

```java
/**
 * 分配统计信息
 */
public class AllocationStatistics {
    
    private long allocationTime;
    private int totalVariables;
    private int spilledVariables;
    private int iterationCount;
    private int maxConflictDegree;
    private double averageDegree;
    
    // 性能指标
    private int memoryAccessReduction;
    private int instructionCountReduction;
    
    public void printStatistics();
    public String toJson();
}
```

## 8. 集成点设计

### 8.1 编译器主流程集成

```java
// 在Compiler.java中的集成点
public class Compiler {
    
    public static void main(String[] args) {
        // ... 现有编译流程
        
        // 插入寄存器分配阶段
        RegisterAllocator allocator = createRegisterAllocator();
        List<IRNode> allocatedIR = allocator.allocate(irNodes, 8);
        
        // 继续代码生成
        CymbolAssembler assembler = new CymbolAssembler();
        assembler.visit(allocatedIR);
    }
    
    private static RegisterAllocator createRegisterAllocator() {
        AllocationConfig config = new AllocationConfig();
        config.setDebugLevel(DebugLevel.DEBUG);
        return new DefaultRegisterAllocator(config);
    }
}
```

## 9. 扩展性设计

### 9.1 插件式架构支持

```java
/**
 * 寄存器分配扩展点
 */
public interface AllocationExtension {
    
    /**
     * 在分配前调用
     */
    default void preAllocation(List<IRNode> irNodes) {}
    
    /**
     * 在分配后调用
     */
    default void postAllocation(List<IRNode> irNodes, 
                              AllocationResult result) {}
    
    /**
     * 在溢出处理时调用
     */
    default boolean onSpill(Operand operand, int cost) { return true; }
}
```

## 10. 版本兼容性

### 10.1 向后兼容保证
- 保持现有FrameSlot接口不变
- 提供兼容模式开关
- 确保未启用优化时代码行为一致

### 10.2 迁移路径
- 分阶段启用优化功能
- 提供性能对比工具
- 支持逐步迁移策略

---

*本文档定义了EP20寄存器分配模块的完整API接口，为后续实现提供清晰的契约和设计指导。*