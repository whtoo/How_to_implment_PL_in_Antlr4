# 寄存器分配理论与实现

## 📋 文档目的

本文档详细说明寄存器分配的理论基础、经典算法、现代实现和最佳实践，为EP21模块提供完整的寄存器分配技术参考和实现指南。

---

## 🎯 寄存器分配概述

### 定义

**寄存器分配（Register Allocation）**：将程序中的临时变量和用户变量分配到有限寄存器集合的编译器后端任务。

**核心挑战**：
1. **寄存器数量有限**：现代CPU通常只有16-32个通用寄存器
2. **变量数量无限**：程序可能使用大量临时变量
3. **生存期重叠**：多个变量可能同时存活
4. **性能要求**：最小化寄存器使用和内存访问

**优化目标**：
- ✅ 最小化寄存器使用压力
- ✅ 最大化寄存器利用率
- ✅ 最小化溢出到内存（Spilling）
- ✅ 平衡寄存器分配，减少指令移动

---

## 🔧 理论基础

### 活跃变量分析（Liveness Analysis）

#### 定义

**活跃变量（Live Variable）**：在程序的某个点正在使用的变量，即在该点之后有使用的变量。

**生存期（Live Range）**：变量从第一次定义到最后一次使用之间的程序点序列。

#### 数学定义

**活跃区间（Live Interval）**：
```
对于变量v，其活跃区间表示为：
[v_from, v_to)

其中：
- v_from: 变量第一次定义的位置（指令索引）
- v_to: 变量最后一次使用的位置（指令索引）
```

#### 活跃变量计算算法

**经典算法**：
```
输入: CFG = (N, E, Entry, Exit)

初始化:
  for each basic block B in CFG:
    in[B] = ∅
    out[B] = ∅

迭代:
  do {
    for each basic block B (except Exit):
      // 1. 向后数据流（后向分析）
      out[B] = ∪ in[s] for s in successors(B)
      
      // 2. 计算活跃变量
      in[B] = use[B] ∪ (out[B] - def[B])
      
      // 3. use[B]: 基本块中使用的变量集合
      //    def[B]: 基本块中定义的变量集合
      
  } until 所有 in[] 和 out[] 收敛
```

**关键观察**：
- 活跃变量的计算是迭代的，从退出块向入口块倒推
- 在每个基本块的`in`集合中，包含该块之后会使用的所有变量
- 变量的活跃区间可以通过指令序列确定

---

### 干扰图（Interference Graph）

#### 定义

**干扰图（RIG）**：无向图，其中：
- **顶点**：表示程序中的变量
- **边**：如果两个变量的活跃区间重叠，则存在一条边（表示它们不能使用同一个寄存器）

**形式化定义**：
```
RIG = (V, E)

其中：
- V: 变量集合 {v1, v2, ..., vn}
- E: 干扰边集合 {(vi, vj) | LiveInterval(vi) ∩ LiveInterval(vj) ≠ ∅}
```

#### 图着色问题

**k-着色问题**：给定k种颜色（k个寄存器），将V划分为k个组，使得：
1. 每组内的顶点无边相连
2. 所有顶点都被着色（假设|V| ≤ k）

**NP完全性**：k-着色问题在一般情况下是NP完全问题。

---

## 🚀 寄存器分配算法

### 算法1：线性扫描寄存器分配（Linear Scan）

#### 算法原理

**核心思想**：按照指令顺序线性扫描活跃区间，贪心地分配寄存器。

**算法步骤**：

**步骤1：排序活跃区间**
```
将所有活跃区间按开始位置（v_from）升序排列

例如：
v1: [1, 5]     → 从指令1到指令5
v2: [2, 4, 6]  → 从指令2到指令6
v3: [3, 7]      → 从指令3到指令7
```

**步骤2：维护活跃寄存器集合**
```
初始化: active = ∅

处理每个区间i（按升序）:
  1. 过期旧的活跃区间
  2. 如果 active 不满：
       为 i 分配一个可用寄存器
       将 i 加入 active
  3. 如果 active 已满:
       将 i 标记为溢出到栈
```

**步骤3：处理溢出**
```
当变量需要溢出时：
  1. 分配一个栈帧位置（stack slot）
  2. 生成加载/存储指令（load/store）
```

#### 复杂度分析

| 指标 | 复杂度 |
|--------|---------|
| 时间 | O(n log n) | n为活跃区间数量 |
| 空间 | O(k) | k为寄存器数量 |
| 溢出 | 取决于活跃区间长度 | 最坏情况下O(n) |

#### 优势与劣势

| 特性 | 优势 | 劣势 |
|--------|--------|--------|
| **速度** | 极快，适合JIT编译器 | 分配质量可能低于图着色 |
| **实现简单** | 只需排序和线性扫描 | 难以启发式优化 |
| **无图构建** | 节省内存和时间 | 无法利用全局信息 |

#### EP21 当前实现

```java
// 位于：ep21/src/main/java/org/teachfx/antlr4/ep18r/stackvm/codegen/LinearScanAllocator.java

public class LinearScanAllocator implements IRegisterAllocator {
    private final int registerCount;
    private final Map<String, Integer> allocatedRegs;
    private final Map<String, Integer> spillSlots;
    private int nextSpillSlot;
    
    public LinearScanAllocator(int registerCount) {
        this.registerCount = registerCount;
        this.allocatedRegs = new HashMap<>();
        this.spillSlots = new HashMap<>();
        this.nextSpillSlot = 0;
    }
    
    @Override
    public int allocate(String varName) {
        Integer reg = allocatedRegs.get(varName);
        if (reg != null) {
            // 简单轮询分配（非严格线性扫描）
            for (int r = 1; r <= registerCount; r++) {
                if (isRegAvailable(r)) {
                    allocatedRegs.put(varName, r);
                    allocatedRegs.put(reg, varName);  // 标记寄存器被占用
                    return r;
                }
            }
        }
        
        // 如果没有可用寄存器，溢出到栈
        return spillToStack(varName);
    }
    
    private boolean isRegAvailable(int reg) {
        return !allocatedRegs.containsKey(reg);
    }
    
    @Override
    public int spillToStack(String varName) {
        if (!spillSlots.containsKey(varName)) {
            int slot = nextSpillSlot++;
            spillSlots.put(varName, slot);
            return -1;  // -1表示溢出
        }
        return spillSlots.get(varName);
    }
    
    @Override
    public void reset() {
        allocatedRegs.clear();
        spillSlots.clear();
        nextSpillSlot = 0;
    }
}
```

---

### 算法2：图着色寄存器分配（Graph Coloring）

#### 算法原理

**核心思想**：构建干扰图，然后使用图着色算法为变量分配寄存器。

**算法步骤**：

**步骤1：构建干扰图**
```
基于活跃变量分析结果：
for each variable vi:
  for each variable vj:
    if intervals(vi) ∩ intervals(vj) ≠ ∅:
      add edge (vi, vj) to RIG
```

**步骤2：简化图（Pre-Simplification）**
```
通过以下规则简化干扰图：
1. 移除度数为0的孤立顶点（不可能分配寄存器）
2. 如果某个变量的所有邻居都被分配了相同颜色，则可以合并
3. 应用 Briggs 的度数约束
```

**步骤3：选择着色顺序**
```
使用启发式算法选择顶点处理顺序：
- 优先选择度数 < k 的顶点（可以直接着色）
- 度数大的顶点先处理
```

**步骤4：着色（Coloring）**
```
初始化: color[v] = ∅

for each vertex v in selected_order:
  select color c ∈ {1, 2, ..., k}
  such that for all neighbors u of v:
    color[u] ≠ c or u not colored yet
  color[v] = c
```

#### 子算法对比

| 算法 | 启发式 | 复杂度 | 分配质量 |
|--------|--------|--------|--------|
| **Simple** | degree(v) < k | O(n + k) | 较低 |
| **Briggs** | degree(v) < k + | O(n + k) | 高 |
| **George's** | 对每个邻居t: degree(t) < k 或 t与v冲突 | O(n * k) | 高 |

#### 伪代码

```
function GraphColoring(RIG, k):
    // 步骤1：简化图
    simplified = Simplify(RIG)
    
    // 步骤2：选择着色顺序
    stack = []
    while simplified.nodes not empty:
        node = selectNodeWithDegreeLessThanK(simplified)
        stack.push(node)
        simplified.remove(node)
    
    // 步骤3：着色
    while stack not empty:
        node = stack.pop()
        color = selectColor(node, simplified, k)
        assignColor(node, color)
        simplified.restoreNode(node)
    
    // 步骤4：回填颜色
    for each node in original_RIG:
        if node not assigned_color:
            color[node] = assigned_color[node]
```

---

### 寄存器合并（Register Coalescing）

#### 定义

**寄存器合并**：通过复制传播，将使用相同寄存器的变量合并为一个变量，减少寄存器压力。

#### 合并条件（Briggs 算法）

两个变量x和y可以合并，当且仅当：
```
degree(x) + degree(y) < k

其中：
- degree(v): 变量v在干扰图中的度数
- k: 可用寄存器数量
```

**直观理解**：合并后，所有邻居的总度数仍然小于k，不会导致着色失败。

#### 合并策略

| 策略 | 描述 | 适用场景 |
|--------|--------|--------|----------|
| **激进合并** | 尽可能合并 | 寄存器充足时 |
| **保守合并** | 只合并安全情况 | 寄存器紧张时 |
| **优先合并** | 优先合并频繁使用的变量 | 循环变量 |

---

## 💡 优化策略

### 溢出策略

#### 溢出变量选择

**启发式规则**：
1. **优先溢出活跃区间短的变量**
2. **优先溢出在循环内使用的变量**
3. **优先溢出跨多个基本块的变量**
4. **考虑变量的使用频率**

#### 重用已溢出的寄存器

当一个变量从栈重新加载时，如果其活跃区间与之前相同：
- 重用之前分配的寄存器
- 避免重新分配和额外的load/store指令

### 活跃区间优化

#### 活跃区间分裂（Live Range Splitting）

**问题**：一个变量的活跃区间很长，阻碍其他变量分配寄存器。

**解决方案**：在活跃区间中分割变量，使其成为多个短区间。

**示例**：
```
原始区间: [2, 10]
分裂后:    [2, 4] 和 [6, 10]

好处：
- 变量 v1 在 [2, 4] 期间占用寄存器
- 变量 v2 在 [6, 10] 期间可以重用寄存器
```

---

## 📊 算法对比与选择

### 选择决策树

```
寄存器分配器选择
│
├── 寄存器数量是否充足？
│   ├── 是 (k ≥ 20)
│   │   └── 否 (k < 20)
│
├── 编译速度优先还是分配质量优先？
│   ├── JIT编译器
│   │   └── 线性扫描
│
└── 分配策略选择
    ├── 简单场景（局部变量少）
    │   ├── 线性扫描
    │   └── 图着色
    └── 复杂场景（全局变量多）
        ├── 图着色 + 溢出优化
        └── 混合策略
```

### LLVM 实现对比

| 算法 | LLVM 类 | 复杂度 | 适用场景 |
|--------|--------|--------|--------|----------|
| **Fast** | RegAllocFast | O(n) | 快速编译、调试 |
| **Basic** | RegAllocBasic | O(n log n) | 通用编译 |
| **Greedy** | RegAllocGreedy | O(n²) | 优化编译（O2, O3） |
| **PBQP** | RegAllocPBQP | O(n³) | 最高质量、慢速 |

---

## 🔧 EP21 实现分析

### 当前状态

| 组件 | 状态 | 位置 | 说明 |
|--------|--------|--------|------|----------|
| **线性扫描分配器** | ✅ 已实现 | `LinearScanAllocator.java` | 简单轮询分配，非严格线性扫描 |
| **活跃变量分析** | ✅ 已实现 | `LiveVariableAnalysis.java` | 后向数据流分析 |
| **干扰图构建** | ⏸ 待集成 | 可在`SSAGraph`后集成 |
| **图着色分配器** | ⏸ 未实现 | - 可作为未来扩展 |
| **寄存器合并** | ⏸ 未实现 | - 可作为未来扩展 |
| **溢出管理** | ✅ 已实现 | `LinearScanAllocator.spillToStack()` | 简单栈帧分配 |
| **EP18R集成** | ✅ 已实现 | `EP18RRegisterAllocatorAdapter.java` | String到VariableSymbol的适配器 |

### 实现特点

**优势**：
1. **简洁性**：代码清晰，逻辑直接
2. **可测试性**：独立于复杂的图数据结构
3. **与EP18R兼容**：通过适配器模式无缝集成

**限制**：
1. **算法简单**：非严格线性扫描，可能分配质量较低
2. **缺少优化**：无活跃区间优化、无溢出优化
3. **无图着色**：无法利用图的全局信息进行优化

---

## 📝 参考资源

### 学术论文

| 论文 | 作者 | 年份 | 核心贡献 | 链接 |
|------|--------|--------|--------|--------|----------|
| Linear Scan Register Allocation | Poletto, Sarkar | 1999 | 快速线性扫描算法 | [PDF](https://web.cs.ucla.edu/~palsberg/course/cs132/linearscan.pdf) |
| Register Allocation via Graph Coloring | Chaitin | 1982 | 经典图着色算法 | [PDF](https://dl.acm.org/doi/abs/10.1145/330249.330250) |
| Coloring of Chordal Graphs | Briggs | 1992 | 改进的图着色算法 | [PDF](https://dl.acm.org/doi/abs/10.1145/349568.124432) |
| Efficiently Computing SSA Form | Cytron et al. | 1991 | SSA构造算法 | [PDF](https://www.cs.princeton.edu/~cytron/papers/efficient-ssa.pdf) |

### 教程与课程

| 来源 | 主题 | 链接 |
|--------|--------|--------|----------|
| CMU 15-723: Register Allocation | 寄存器分配算法详解 | [PDF](http://www.cs.cmu.edu/afs/cs.cmu.edu/academic/class/15745-s18/www/lectures/L12-Register-Allocation.pdf) |
| CMU 15-732: SSA and Optimizations | SSA构造和优化 | [PDF](http://www.cs.cmu.edu/afs/cs.cmu.edu/academic/class/15745-s18/www/lectures/) |
| Stanford CS243: Compilers | 编译器综合教程 | [网页](https://cs.stanford.edu/144/) |
| openEuler: Compiler Optimization (5): Register Allocation | 寄存器分配实践 | [博客](https://www.openeuler.org/en/blog/20220822-寄存器分配/寄存器分配) |

### 开源实现

| 项目 | 组件 | 链接 |
|------|--------|--------|----------|
| **LLVM** | RegAllocBase, RegAllocGreedy, RegAllocFast | [GitHub](https://github.com/llvm/llvm-project/blob/main/llvm/lib/CodeGen/RegAllocBase.h) | 工业级实现 |
| **GCC** | Global, Reload, IRA | [GitHub](https://github.com/gcc-mirror/gcc/tree/master/gcc/regalloc) | 经典实现 |
| **V8** | Liftoff, RegAlloc | [GitHub](https://github.com/v8/v8/tree/master/src/lithium/lithium-gen/regalloc) | 高性能实现 |
| **Cranelift** | RegAlloc | [GitHub](https://github.com/bytecodealliance/cranelift/tree/main/cranelift/codegen/src/alloc.rs) | Rust实现 |

---

## 🚀 最佳实践

### 1. 活跃区间表示

**建议**：使用类封装活跃区间，包含开始、结束、长度等元数据。

```java
public class LiveInterval {
    private final String variable;
    private final int start;
    private final int end;
    private final int length;
    
    public LiveInterval(String variable, int start, int end) {
        this.variable = variable;
        this.start = start;
        this.end = end;
        this.length = end - start;
    }
    
    public boolean overlaps(LiveInterval other) {
        return !(this.end < other.start || this.start > other.end);
    }
    
    public int compareTo(LiveInterval other) {
        return Integer.compare(this.start, other.start);
    }
}
```

### 2. 数据流分析集成

**建议**：将活跃变量分析与寄存器分配器紧密集成。

```java
public class IntegratedRegisterAllocation {
    private final LiveVariableAnalysis liveAnalysis;
    private final IRegisterAllocator allocator;
    
    public AllocationResult allocate(CFG cfg) {
        // 步骤1：计算活跃变量
        Map<String, LiveInterval> liveRanges = liveAnalysis.analyze(cfg);
        
        // 步骤2：构建干扰图
        InterferenceGraph rig = buildInterferenceGraph(liveRanges);
        
        // 步骤3：寄存器分配
        Map<String, Integer> allocation = allocator.allocate(rig);
        
        return new AllocationResult(allocation, liveRanges);
    }
}
```

### 3. 测试策略

**单元测试重点**：
1. 活跃区间计算的正确性
2. 干扰图构建的准确性
3. 简单案例（2个变量）的分配
4. 溢出场景的处理

**测试用例示例**：
```java
@Test
void shouldAllocateTwoVariablesToDifferentRegisters() {
    // Given: 两个变量活跃区间不重叠
    LiveInterval v1 = new LiveInterval("x", 1, 3);
    LiveInterval v2 = new LiveInterval("y", 4, 6);
    
    // When: 执行寄存器分配
    AllocationResult result = allocator.allocate(cfg);
    
    // Then: 应分配不同的寄存器
    assertThat(result.getRegister("x")).isNotEqualTo(result.getRegister("y"));
}

@Test
void shouldSpillVariableWhenNoRegistersAvailable() {
    // Given: 活跃区间重叠且无可用寄存器
    List<LiveInterval> intervals = createOverlappingIntervals(20);
    
    // When: 执行寄存器分配
    AllocationResult result = allocator.allocate(cfg);
    
    // Then: 应溢出到栈
    assertThat(result.getRegister("v1")).isEqualTo(-1);
    assertThat(result.getSpillSlot("v1")).isGreaterThan(0);
}
```

---

## 🎯 代码示例

### 示例1：活跃变量分析

**原始代码**：
```c
int example(int a, int b) {
    int x = a + b;
    int y = x * 2;
    return y;
}
```

**活跃区间**：
```
变量 a: [1, 2, 3]  (在指令1、2、3使用)
变量 b: [3, 4]        (在指令3、4使用)
变量 x: [2]          (在指令2使用)
```

### 示例2：干扰图与寄存器分配

**场景**：三个变量活跃区间重叠。

**干扰图**：
```
顶点: {a, b, x}
边:   (a,b), (a,x), (b,x)
```

**图着色结果（k=2）**：
```
寄存器1: {a, x}  // 红色
寄存器2: {b}      // 绿色
```

### 示例3：溢出场景

**场景**：变量数量超过寄存器数量（3个变量，k=2）。

**处理策略**：
1. 活跃区间排序：选择溢出对分配质量影响最小的变量
2. 溢出：为溢出变量分配栈帧位置

**结果**：
```
寄存器1: a → r0
寄存器2: b → r1
溢出:   x → stack[0]  // x溢出到栈位置0
```

---

## 📝 后续优化方向

### 1. 活跃区间优化

- **活跃区间合并**：相邻的短区间合并
- **活跃区间重排序**：减少寄存器压力
- **跨基本块分析**：全局优化活跃区间

### 2. 高级寄存器分配算法

- **迭代图着色**：支持寄存器重用和合并
- **混合算法**：结合线性扫描和图着色的优势
- **SSA感知分配**：利用SSA形式信息优化分配

### 3. 调试工具

- **寄存器分配报告**：生成分配结果的可视化报告
- **活跃区间图示**：绘制所有变量的活跃区间
- **干扰图可视化**：输出干扰图的DOT格式

---

## 🔗 关键技术点

| 技术点 | 状态 | 优先级 | 参考来源 |
|--------|--------|--------|----------|
| 活跃变量分析 | ✅ 已掌握 | 高 | CMU教程、LLVM LiveIntervals |
| 干扰图构建 | ⏸ 待实现 | 中 | openEuler博客、Chaitin论文 |
| 线性扫描算法 | ✅ 已实现 | 高 | Poletto论文、EP18R实现 |
| 图着色算法 | ⏸ 未实现 | 中 | Briggs论文、LLVM RegAllocGreedy |
| 寄存器合并 | ⏸ 未实现 | 中 | openEuler博客 |
| 溢出管理 | ✅ 已实现 | 高 | 自定义策略 |
| 活跃区间优化 | ⏸ 未实现 | 低 | LLVM LiveIntervals优化 |

---

**文档版本**: 1.0-草稿
**创建日期**: 2026-01-14
**适用范围**: EP21模块寄存器分配
**维护者**: EP21模块维护团队
**审核要求**: 需要补充算法复杂度分析、LLVM实现对比、代码示例和最佳实践
