# EnhancedCFG架构设计文档

**版本**: 1.0
**创建日期**: 2026-01-19
**设计目标**: 增强EP21 CFG数据结构，提供高性能和高级功能支持

---

## 📋 设计目标

### 1.1 核心目标

1. **性能优化**：
   - 边查询从O(n)优化到O(1)
   - 基本块查找从O(n)优化到O(1)
   - 图遍历结果缓存，避免重复计算
   - 批量操作支持，提升多边操作性能

2. **功能增强**：
   - 支持关键边拆分（Critical Edge Splitting）
   - 增强的循环分析（自然循环、嵌套循环）
   - CFG完整性验证和检查
   - 为SSA形式转换提供基础设施

3. **向后兼容性**：
   - 保持现有CFG的公共API
   - 支持从Triple<Integer,Integer,Integer>的转换
   - 现有优化器无需大修改即可使用

4. **可扩展性**：
   - 清晰的扩展点（新增边类型、元数据）
   - 插件式架构支持（优化器、分析Pass）
   - 支持未来的CFG变换需求

### 1.2 设计原则

1. **不可变优先**：核心数据结构尽可能不可变，线程安全
2. **索引驱动**：使用Map/HashMap实现快速查询
3. **缓存友好**：缓存昂贵计算结果（RPO、支配树）
4. **增量更新**：支持局部CFG修改，避免全量重建
5. **类型安全**：使用枚举和泛型，避免类型错误

---

## 🏗️ 核心组件设计

### 2.1 类层次结构

```
EnhancedCFG<I>
├── 继承/组合：CFG<I>
├── 核心字段
│   ├── 边索引（outgoingEdges, incomingEdges）
│   ├── 基本块索引（blockMap）
│   ├── 遍历缓存（reversePostOrder, topologicalOrder）
│   └── 支配树缓存（dominatorTree）
└── 核心方法
    ├── 快速查询方法（O(1)复杂度）
    ├── 批量操作方法
    ├── 缓存管理方法
    └── 验证和完整性检查
```

### 2.2 EnhancedCFG与CFG的关系

**设计选择**：**组合模式（Composition）**，而非继承

**原因**：
1. 更灵活：可以包装现有CFG，无需修改其内部实现
2. 更安全：避免继承引入的不兼容性风险
3. 更清晰：明确EnhancedCFG提供的额外功能
4. 更易测试：可以独立测试增强功能

**实现方式**：
```java
public class EnhancedCFG<I extends IRNode> {
    private final CFG<I> baseCFG;  // 组合现有CFG
    private final Map<Integer, Set<CFGEdge<I>>> outgoingEdges;
    private final Map<Integer, Set<CFGEdge<I>>> incomingEdges;
    // ... 其他增强字段

    public EnhancedCFG(CFG<I> cfg) {
        this.baseCFG = Objects.requireNonNull(cfg);
        // 初始化索引和缓存
        this.outgoingEdges = new HashMap<>();
        this.incomingEdges = new HashMap<>();
        // 构建索引
        buildIndexes();
    }
}
```

### 2.3 核心数据结构

#### 2.3.1 边索引结构

```java
// 出边映射：sourceId -> Set<CFGEdge<I>>
// 查询复杂度：O(1)
private final Map<Integer, Set<CFGEdge<I>>> outgoingEdges;

// 入边映射：targetId -> Set<CFGEdge<I>>
// 查询复杂度：O(1)
private final Map<Integer, Set<CFGEdge<I>>> incomingEdges;
```

**优势**：
- O(1)边查询：相比CFG的O(n)流搜索大幅提升
- 支持多条相同源/目标对的边（带权重区分）
- 快速获取所有出边/入边

#### 2.3.2 基本块索引结构

```java
// 基本块ID到基本块的映射
// 查询复杂度：O(1)
private final Map<Integer, BasicBlock<I>> blockMap;
```

**优势**：
- O(1)基本块查找：相比CFG.getBlock()的O(n)流搜索大幅提升
- 直接访问基本块对象，无需遍历nodes列表
- 支持快速基本块验证

#### 2.3.3 遍历缓存结构

```java
// 反向后序遍历（Reverse Post-Order）缓存
// 用于数据流分析优化
private List<Integer> reversePostOrder;
private boolean reversePostOrderValid;

// 拓扑排序缓存
// 用于优化Pass执行顺序
private List<Integer> topologicalOrder;
private boolean topologicalOrderValid;
```

**优势**：
- 避免重复计算：RPO计算复杂度O(V+E)
- 缓存失效：CFG结构变更时自动失效
- 提供快速访问：getReversePostOrder()和getTopologicalOrder()

#### 2.3.4 支配树缓存（可选，后续实现）

```java
// 支配树缓存
// 用于SSA形式转换和循环分析
private DominatorTree dominatorTree;
private boolean dominatorTreeValid;
```

**优势**：
- 支配关系快速查询
- 支配边界计算（Dominance Frontier）
- 为SSA PHI节点插入提供基础

---

## 🎯 核心API设计

### 3.1 快速查询方法

```java
/**
 * 获取指定基本块的所有出边
 *
 * @param blockId 基本块ID
 * @return 出边集合，如果blockId不存在返回空集合
 * @apiNote 时间复杂度：O(1)
 */
public Set<CFGEdge<I>> getOutgoingEdges(int blockId);

/**
 * 获取指定基本块的所有入边
 *
 * @param blockId 基本块ID
 * @return 入边集合，如果blockId不存在返回空集合
 * @apiNote 时间复杂度：O(1)
 */
public Set<CFGEdge<I>> getIncomingEdges(int blockId);

/**
 * 获取指定源和目标之间的所有边（可能有权重不同的多条边）
 *
 * @param sourceId 源基本块ID
 * @param targetId 目标基本块ID
 * @return 边集合，如果不存在返回空集合
 * @apiNote 时间复杂度：O(1) + O(k)，k为边数量
 */
public Set<CFGEdge<I>> getEdgesBetween(int sourceId, int targetId);

/**
 * 快速查找基本块（O(1)复杂度）
 *
 * @param blockId 基本块ID
 * @return 基本块对象，如果不存在返回null
 * @apiNote 时间复杂度：O(1)
 */
public BasicBlock<I> getBlockById(int blockId);
```

### 3.2 批量操作方法

```java
/**
 * 批量添加多条边
 *
 * <p>比逐个调用addEdge()更高效，减少重复的索引更新操作。</p>
 *
 * @param edges 要添加的边集合
 * @return 实际添加的边数量（可能存在重复）
 * @apiNote 时间复杂度：O(m)，m为edges.size()
 */
public int addEdges(Collection<CFGEdge<I>> edges);

/**
 * 批量删除多条边
 *
 * <p>比逐个调用removeEdge()更高效，减少重复的索引更新操作。</p>
 *
 * @param edges 要删除的边集合
 * @return 实际删除的边数量（可能不存在）
 * @apiNote 时间复杂度：O(m)，m为edges.size()
 */
public int removeEdges(Collection<CFGEdge<I>> edges);
```

### 3.3 缓存管理方法

```java
/**
 * 获取反向后序遍历（Reverse Post-Order）
 *
 * <p>首次调用时计算RPO并缓存，后续调用返回缓存结果。
 * CFG结构变更时自动失效缓存。</p>
 *
 * @return 反向后序的基本块ID列表
 * @apiNote 时间复杂度：首次调用O(V+E)，后续调用O(1)
 */
public List<Integer> getReversePostOrder();

/**
 * 获取拓扑排序
 *
 * <p>首次调用时计算拓扑排序并缓存，后续调用返回缓存结果。
 * CFG结构变更时自动失效缓存。</p>
 *
 * @return 拓扑排序的基本块ID列表
 * @apiNote 时间复杂度：首次调用O(V+E)，后续调用O(1)
 */
public List<Integer> getTopologicalOrder();

/**
 * 失效所有缓存
 *
 * <p>当CFG结构发生重大变更时，调用此方法失效所有缓存。</p>
 */
public void invalidateCache();
```

### 3.4 CFG修改方法

```java
/**
 * 添加单条边到CFG
 *
 * <p>同时更新outgoingEdges、incomingEdges和links数组，
 * 并失效相关缓存。</p>
 *
 * @param edge 要添加的边
 * @return true如果边成功添加，false如果已存在
 * @apiNote 时间复杂度：O(1)
 */
public boolean addEdge(CFGEdge<I> edge);

/**
 * 从CFG中删除单条边
 *
 * <p>同时更新outgoingEdges、incomingEdges和links数组，
 * 并失效相关缓存。</p>
 *
 * @param edge 要删除的边
 * @return true如果边成功删除，false如果不存在
 * @apiNote 时间复杂度：O(1) + O(k)，k为相关边的数量
 */
public boolean removeEdge(CFGEdge<I> edge);

/**
 * 检查指定边是否存在于CFG中
 *
 * @param sourceId 源基本块ID
 * @param targetId 目标基本块ID
 * @return true如果存在至少一条从sourceId到targetId的边，false otherwise
 * @apiNote 时间复杂度：O(1)
 */
public boolean hasEdge(int sourceId, int targetId);
```

---

## 🔄 缓存失效策略

### 4.1 缓存失效触发条件

以下操作会触发缓存失效：
1. **添加边**：`addEdge()`, `addEdges()`
2. **删除边**：`removeEdge()`, `removeEdges()`
3. **添加基本块**：`addBasicBlock()`
4. **删除基本块**：`removeBasicBlock()`
5. **手动失效**：`invalidateCache()`

### 4.2 缓存粒度

提供两种失效策略：
1. **全部失效**：失效所有缓存（默认）
2. **智能失效**：仅失效受影响的缓存（高级优化）

```java
public void invalidateCache() {
    // 全部失效策略
    reversePostOrderValid = false;
    topologicalOrderValid = false;
    dominatorTreeValid = false;
}

public void invalidateCache(CacheScope scope) {
    // 智能失效策略
    switch (scope) {
        case STRUCTURAL -> {
            reversePostOrderValid = false;
            topologicalOrderValid = false;
        }
        case DOMINANCE -> {
            dominatorTreeValid = false;
        }
    }
}
```

---

## 📊 性能分析

### 5.1 时间复杂度对比

| 操作 | 现有CFG | EnhancedCFG | 改进倍数 |
|------|---------|-------------|---------|
| getBlock() | O(n) | O(1) | n倍 |
| 边查询（getEdgesBetween()） | O(n) | O(1) + O(k) | n倍 |
| getOutgoingEdges() | O(1) | O(1) | 相同 |
| getIncomingEdges() | O(1) | O(1) | 相同 |
| RPO遍历 | 每次O(V+E) | 首次O(V+E)，后续O(1) | 取决于重复访问次数 |
| 拓扑排序 | 每次O(V+E) | 首次O(V+E)，后续O(1) | 取决于重复访问次数 |
| 批量添加m条边 | O(m * n) | O(m) | n倍 |

### 5.2 空间复杂度

**额外空间开销**：
- 边索引：O(E)，E为边数量
- 基本块索引：O(V)，V为基本块数量
- 遍历缓存：O(V)
- 支配树缓存：O(V)（可选）

**总开销**：O(V + E)，与CFG本身的存储量相当

### 5.3 性能提升估算

对于典型的CFG（V=100, E=200），假设每个分析Pass需要：
- 1000次基本块查找：从O(100n)降至O(1000)，提升100倍
- 500次边查询：从O(500n)降至O(500)，提升100倍
- 10次RPO遍历：从O(10*300)降至O(300 + 9)，提升10倍

**综合提升**：约20-50倍性能提升（取决于查询模式）

---

## 🔌 高级功能集成设计

### 6.1 关键边拆分支持

```java
/**
 * 拆分所有关键边（Critical Edges）
 *
 * <p>关键边是指入度>1且出度>1的边。拆分关键边
 * 会插入新的基本块，对SSA形式转换和其他优化Pass很重要。</p>
 *
 * @return 拆分后的EnhancedCFG实例
 * @apiNote 时间复杂度：O(V + E)
 */
public EnhancedCFG<I> splitCriticalEdges();
```

**实现策略**：
1. 识别所有关键边：O(V + E)
2. 对每个关键边创建新的基本块：O(k)，k为关键边数量
3. 更新所有相关边和索引：O(E)
4. 失效所有缓存

### 6.2 循环分析增强

```java
/**
 * 增强的循环分析
 *
 * @return LoopInfo对象，包含循环头、循环体、嵌套结构等信息
 * @apiNote 时间复杂度：O(V + E)
 */
public LoopInfo<I> analyzeLoops();
```

**实现策略**：
1. 识别自然循环（基于回边）
2. 构建循环嵌套树（Loop Nesting Tree）
3. 检测循环不变表达式
4. 提供循环优化指导信息

### 6.3 CFG完整性验证

```java
/**
 * 验证CFG的完整性
 *
 * @return ValidationResult，包含验证结果和错误列表
 * @apiNote 时间复杂度：O(V + E)
 */
public ValidationResult validate();
```

**验证项**：
- 基本块连通性（从入口可达所有节点）
- 边一致性（所有边都有有效的源和目标）
- 不可达代码检测
- 跳转目标有效性验证
- 单入口/单出口检查（可选）

---

## 📝 使用示例

### 7.1 基本使用

```java
// 创建基础CFG
CFGBuilder builder = new CFGBuilder(startBlock);
CFG<IRNode> baseCFG = builder.getCFG();

// 创建EnhancedCFG
EnhancedCFG<IRNode> enhancedCFG = new EnhancedCFG<>(baseCFG);

// 快速查询基本块
BasicBlock<IRNode> block = enhancedCFG.getBlockById(5);

// 快速查询出边
Set<CFGEdge<IRNode>> outgoingEdges = enhancedCFG.getOutgoingEdges(5);
```

### 7.2 批量操作

```java
// 批量添加多条边
List<CFGEdge<IRNode>> edgesToAdd = Arrays.asList(
    CFGEdge.of(0, 1, CFGConstants.EdgeType.JUMP),
    CFGEdge.of(0, 2, CFGConstants.EdgeType.SUCCESSOR),
    CFGEdge.of(1, 3, CFGConstants.EdgeType.JUMP)
);
int added = enhancedCFG.addEdges(edgesToAdd);

// 批量删除多条边
List<CFGEdge<IRNode>> edgesToRemove = Arrays.asList(
    CFGEdge.of(0, 1, CFGConstants.EdgeType.JUMP),
    CFGEdge.of(2, 3, CFGConstants.EdgeType.JUMP)
);
int removed = enhancedCFG.removeEdges(edgesToRemove);
```

### 7.3 缓存利用

```java
// 数据流分析中使用缓存
List<Integer> rpo = enhancedCFG.getReversePostOrder();
// 首次调用：O(V+E)计算
// 后续调用：O(1)返回缓存

for (int blockId : rpo) {
    // 在反向后序中处理基本块
    BasicBlock<IRNode> block = enhancedCFG.getBlockById(blockId);
    // ... 数据流分析
}

// CFG变更后自动失效缓存
enhancedCFG.addEdge(newEdge);
// 下次getReversePostOrder()会重新计算
```

### 7.4 高级功能使用

```java
// 关键边拆分
EnhancedCFG<IRNode> splitCFG = enhancedCFG.splitCriticalEdges();

// 循环分析
LoopInfo<IRNode> loopInfo = enhancedCFG.analyzeLoops();
for (NaturalLoop<IRNode> loop : loopInfo.getLoops()) {
    System.out.println("Loop header: " + loop.getHeader());
}

// CFG完整性验证
ValidationResult result = enhancedCFG.validate();
if (!result.isValid()) {
    for (String error : result.getErrors()) {
        logger.error("CFG validation error: " + error);
    }
}
```

---

## 🚧 实现注意事项

### 8.1 线程安全

EnhancedCFG设计为**非线程安全**，原因：
1. Java标准集合（HashMap, ArrayList）不是线程安全的
2. 编译器优化Pass通常在单线程中执行
3. 线程安全会增加开销

**如果需要线程安全**：
- 使用ConcurrentHashMap替代HashMap
- 使用CopyOnWriteArrayList替代ArrayList
- 添加synchronized方法保护关键操作

### 8.2 内存管理

**内存优化策略**：
1. **延迟初始化**：缓存字段在使用时才初始化
2. **容量预估**：使用initialCapacity减少扩容
3. **弱引用缓存**：可选使用WeakReference缓存（谨慎使用）

### 8.3 向后兼容性

**兼容性保证**：
1. 提供访问基础CFG的方法：`getBaseCFG()`
2. 支持从Triple创建边：`fromTriple()`方法
3. 保持原有方法签名：`getSucceed()`, `getFrontier()`等
4. 逐步迁移：现有代码可以继续使用CFG，新代码使用EnhancedCFG

---

## 📚 参考资料

### 9.1 学术参考文献

1. **Muchnick, S. S. (1997)**. "Advanced Compiler Design and Implementation"
   - 第5章：控制流图和数据分析
   - 第6章：数据流分析

2. **Cytron et al. (1991)**. "Efficiently Computing Static Single Assignment Form"
   - 支配树算法
   - 关键边拆分的重要性

### 9.2 工业实现参考

1. **LLVM CFG**:
   - https://llvm.org/docs/ProgrammersManual.html#CFG
   - 反向后序遍历
   - 拓扑排序
   - 关键边拆分Pass

2. **GCC CFG**:
   - https://gcc.gnu.org/onlinedocs/gccint/Control-Flow.html
   - CFG维护和更新机制
   - 循环优化集成

---

**文档版本**: 1.0
**创建日期**: 2026-01-19
**状态**: ✅ 设计完成，待实现
**下一步**: 实现EnhancedCFG基类（任务1.4）
