# 数据流分析

## 📋 文档目的

本文档详细说明数据流分析的理论基础、格理论、传递函数、Worklist算法、前向/后向分析对比以及MLIR框架实现，为EP21模块提供完整的数据流分析技术参考和实现指南。

---

## 🎯 数据流分析概述

### 定义

**数据流分析（Dataflow Analysis）**：一种静态分析技术，通过在编译时分析控制流图（CFG）来推导程序的行为属性。

**核心目标**：
1. **编译器优化**：为优化Pass提供必要的信息（如活跃变量、到达定义）
2. **安全检查**：验证程序属性（如未初始化变量检测）
3. **代码转换**：指导代码生成和重构

**分析类型**：
- **前向分析（Forward Analysis）**：信息从入口向出口传播
- **后向分析（Backward Analysis）**：信息从出口向入口传播
- **May分析**：收集"可能"发生的信息（如到达定义）
- **Must分析**：收集"一定"发生的信息（如可用表达式）

---

## 🔧 理论基础

### 格理论（Lattice Theory）

#### 完全格（Complete Lattice）

**定义**：完全格是一个偏序集 $(L, \leq)$，其中：

1. **偏序（Partial Order）**：
   - 自反性：$x \leq x$
   - 反对称性：如果 $x \leq y$ 且 $y \leq x$，则 $x = y$
   - 传递性：如果 $x \leq y$ 且 $y \leq z$，则 $x \leq z$

2. **上确界（Least Upper Bound, LUB/Join, $\sqcup$）**：
   - 对于任意元素 $x, y \in L$，存在元素 $z \in L$ 满足：
     - $x \leq z$ 且 $y \leq z$
     - 对于任何 $z'$，如果 $x \leq z'$ 且 $y \leq z'$，则 $z \leq z'$

3. **下确界（Greatest Lower Bound, GLB/Meet, $\sqcap$）**：
   - 对于任意元素 $x, y \in L$，存在元素 $z \in L$ 满足：
     - $z \leq x$ 且 $z \leq y$
     - 对于任何 $z'$，如果 $z' \leq x$ 且 $z' \leq y$，则 $z' \leq z$

4. **顶元素（Top, $\top$）和底元素（Bottom, $\bot$）**：
   - $\top$：对所有 $x \in L$，$x \leq \top$（最不精确的信息）
   - $\bot$：对所有 $x \in L$，$\bot \leq x$（最精确的信息）

#### 半格（Semi-Lattice）

**定义**：如果集合只有上确界或只有下确界，则称为半格。

- **Join半格**：只有上确界（$\sqcup$）
- **Meet半格**：只有下确界（$\sqcap$）

**示例**：
- **幂集格（Power Set Lattice）**：
  - 偏序：集合包含关系（$\subseteq$）
  - Join：并集（$\cup$）
  - Meet：交集（$\cap$）
  - Top：全集
  - Bottom：空集（$\emptyset$）

#### 格的层次结构

```
              Top (⊤)
            /     \
          /         \
        /             \
      /                 \
    /                     \
  Bottom (⊥)

对于幂集格：
Top = {a, b, c}  (全集)
   /     \
{a,b}   {a,c}
  |   X    |   X
{a}       {c}
  \       /
    {a,c}
      |
    Bottom = ∅
```

---

## 🚀 传递函数与Meet操作

### 传递函数（Transfer Function）

#### 定义

**传递函数（Transfer Function）**：$f: L \rightarrow L$，描述信息如何在一个基本块内传播。

**性质**：
1. **单调性（Monotonicity）**：
   - 如果 $x \leq y$，则 $f(x) \leq f(y)$
   - 确保算法收敛性

2. **可组合性**：
   - 传递函数可以组合：$f_2 \circ f_1$

#### 示例

**常量传播的传递函数**：
```
对于指令: x = a + b

如果:
  - a 是常数 c1
  - b 是常数 c2
则:
  - out = c1 + c2 (常数)
否则:
  - out = top (未知)
```

**活跃变量分析的传递函数**：
```
对于指令: use(x)

如果:
  - x 在 out 中
则:
  - in = out ∪ {x}
否则:
  - in = out
```

### Meet操作

#### 定义与性质

**Meet操作（$\sqcap$）**：在控制流汇聚点合并来自多个前驱的信息。

**性质**：
1. **交换律**：$x \sqcap y = y \sqcap x$
2. **结合律**：$(x \sqcap y) \sqcap z = x \sqcap (y \sqcap z)$
3. **幂等律**：$x \sqcap x = x$

#### Meet操作类型

| 分析类型 | Meet操作 | 含义 | 示例 |
|---------|-----------|--------|--------|
| **May分析** | 并集（$\cup$） | 收集所有可能的信息 |
| **Must分析** | 交集（$\cap$） | 收集一定成立的信息 |
| **Liveness** | 并集（$\cup$） | 合并多个路径的活跃变量 |
| **Available Expressions** | 交集（$\cap$） | 保留所有路径都可用的表达式 |

#### 实现示例

**并集Meet操作（用于Liveness）**：
```java
public class UnionMeet implements MeetOperator<Set<Variable>> {
    @Override
    public Set<Variable> meet(Set<Variable> a, Set<Variable> b) {
        Set<Variable> result = new HashSet<>(a);
        result.addAll(b);  // 并集
        return result;
    }
}
```

**交集Meet操作（用于Available Expressions）**：
```java
public class IntersectionMeet implements MeetOperator<Set<Expression>> {
    @Override
    public Set<Expression> meet(Set<Expression> a, Set<Expression> b) {
        Set<Expression> result = new HashSet<>(a);
        result.retainAll(b);  // 交集
        return result;
    }
}
```

---

## 🔨 Worklist算法

### 算法原理

**Worklist算法**：优化的迭代数据流分析算法，只处理信息发生变化的节点。

**优势**：
- 提高收敛速度（避免不必要的重复计算）
- 支持任意遍历顺序（DFS、BFS、反向后序等）

### 算法步骤

#### 前向分析Worklist算法

**伪代码**：
```
输入: CFG = (N, E, Entry, Exit)
输出: in[n] 和 out[n] 对每个节点 n ∈ N

初始化:
  for each node n in CFG:
    if n == Entry:
      in[n] = InitialValue
    else:
      in[n] = Top

  worklist = all nodes in CFG

迭代:
  while worklist not empty:
    n = worklist.remove()
    old_in = in[n]

    // 1. 计算新的 in 值
    new_in = meet(out[p] for p in predecessors(n))

    // 2. 应用传递函数
    out[n] = transfer(new_in, n)

    // 3. 如果信息变化，更新后继节点
    if new_in != old_in:
      for each successor s in successors(n):
        worklist.add(s)

  // 当 worklist 为空时收敛
```

#### 后向分析Worklist算法

**伪代码**：
```
输入: CFG = (N, E, Entry, Exit)
输出: in[n] 和 out[n] 对每个节点 n ∈ N

初始化:
  for each node n in CFG:
    if n == Exit:
      out[n] = InitialValue
    else:
      out[n] = Top

  worklist = all nodes in CFG (按逆后序)

迭代:
  while worklist not empty:
    n = worklist.remove()
    old_out = out[n]

    // 1. 计算新的 out 值
    new_out = meet(in[s] for s in successors(n))

    // 2. 应用传递函数
    in[n] = transfer(new_out, n)

    // 3. 如果信息变化，更新前驱节点
    if new_out != old_out:
      for each predecessor p in predecessors(n):
        worklist.add(p)
```

### 收敛性保证

**Kildall定理**：如果满足以下条件，算法保证收敛：

1. **格必须是有限高度的**：不存在无限下降链
2. **传递函数必须是单调的**：$x \leq y \Rightarrow f(x) \leq f(y)$
3. **Meet操作必须是单调的**：$x \leq y \Rightarrow x \sqcap z \leq y \sqcap z$

### EP21当前实现

**LivenessAnalysis（后向分析）**：
```java
// 位于：ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/LiveVariableAnalysis.java
public class LiveVariableAnalysis<I extends IRNode> {
    private CFG<I> cfg;
    private Map<Integer, Set<Variable>> liveIn;
    private Map<Integer, Set<Variable>> liveOut;

    public void analyze() {
        // 初始化
        initialize();

        // Worklist算法
        Queue<Integer> worklist = new ArrayDeque<>(cfg.nodes);
        while (!worklist.isEmpty()) {
            int blockId = worklist.poll();

            // 计算新的 liveOut
            Set<Variable> newLiveOut = computeLiveOut(blockId);

            // 应用传递函数
            Set<Variable> newLiveIn = computeLiveIn(newLiveOut, blockId);

            // 如果信息变化，更新前驱
            if (!newLiveIn.equals(liveIn.get(blockId))) {
                liveIn.put(blockId, newLiveIn);
                for (Integer pred : cfg.getPredecessors(blockId)) {
                    if (!worklist.contains(pred)) {
                        worklist.add(pred);
                    }
                }
            }
        }
    }

    private Set<Variable> computeLiveOut(int blockId) {
        // Meet操作：并集
        Set<Variable> result = new HashSet<>();
        for (Integer succ : cfg.getSucceed(blockId)) {
            result.addAll(liveIn.get(succ));
        }
        return result;
    }
}
```

---

## 📊 前向分析 vs 后向分析

### 对比表

| 特性 | 前向分析 | 后向分析 |
|--------|-----------|-----------|
| **信息流向** | 从入口向出口 | 从出口向入口 |
| **典型应用** | 到达定义、常量传播 | 活跃变量、死代码消除 |
| **初始化** | Entry节点 = Initial<br>其他节点 = Top | Exit节点 = Initial<br>其他节点 = Top |
| **Worklist顺序** | 正向遍历（前序、逆后序） | 反向遍历（后序、逆前序） |
| **数据流方程** | $in[n] = \sqcap_{p \in pred(n)} out[p]$<br>$out[n] = f_n(in[n])$ | $out[n] = \sqcap_{s \in succ(n)} in[s]$<br>$in[n] = f_n(out[n])$ |

### 前向分析示例

#### 到达定义（Reaching Definitions）

**定义**：确定哪些变量定义可能到达程序的每个点。

**数据流方程**：
```
Gen[B]: 基本块 B 中生成的定义集合
Kill[B]: 基本块 B 中被kill的定义集合

out[B] = Gen[B] ∪ (in[B] - Kill[B])
in[B] = ⋃ out[p] for p in predecessors(B)
```

**Meet操作**：并集（$\cup$）

**初始化**：
```
in[Entry] = ∅
in[其他节点] = ∅
```

#### 常量传播（Constant Propagation）

**定义**：确定哪些变量在程序的每个点具有常量值。

**数据流方程**：
```
out[B] = f_B(in[B])  // 传递函数
in[B] = ⋃ in[p] for p in predecessors(B)
```

**Meet操作**：并集（$\cup$），但特殊处理常量：
```
meet(c1, c2):
  if c1 == c2: return c1
  else: return Top (未知)
```

**初始化**：
```
in[Entry] = {所有参数 = Top}
in[其他节点] = Top
```

### 后向分析示例

#### 活跃变量分析（Liveness Analysis）

**定义**：确定哪些变量在程序的每个点可能在未来被使用。

**数据流方程**：
```
Use[B]: 基本块 B 中使用的变量集合
Def[B]: 基本块 B 中定义的变量集合

in[B] = Use[B] ∪ (out[B] - Def[B])
out[B] = ⋃ in[s] for s in successors(B)
```

**Meet操作**：并集（$\cup$）

**初始化**：
```
out[Exit] = ∅
out[其他节点] = ∅
```

#### 死代码消除（Dead Code Elimination）

**定义**：删除计算结果从未被使用的代码。

**基于活跃变量分析**：
1. 执行活跃变量分析
2. 对于每个指令 $s$：
   - 如果 $s$ 的结果不在 $in[s]$ 中（即不被使用），则删除 $s$
   - 如果 $s$ 有副作用（如 store, call），则保留 $s$

**伪代码**：
```
for each instruction s in CFG:
  if s.result not in liveIn(s):
    if s.hasSideEffects():
      keep(s)
    else:
      remove(s)
```

### 方向选择指南

| 场景 | 推荐方向 | 原因 |
|--------|-----------|--------|
| **寄存器分配** | 后向 | 需要知道哪些变量在未来会被使用 |
| **常量传播** | 前向 | 常数从定义向使用传播 |
| **死代码消除** | 后向 | 删除不被未来使用的定义 |
| **公共子表达式消除** | 前向 | 跟踪表达式的可用性 |
| **到达定义** | 前向 | 追踪定义的传播 |
| **可用表达式** | 前向 | 确定哪些表达式已被计算 |

---

## 🎨 MLIR数据流分析框架

### ForwardDataFlowAnalysis

#### 核心概念

**LLVM MLIR框架**提供了一个通用的前向数据流分析驱动器：

```
ForwardDataFlowAnalysis<ValueT, AnalysisStateT>
  ├── LatticeElement<ValueT>     // 格元素管理
  ├── ForwardDataFlowAnalysis    // 分析驱动器
  └── visitOperation(...)        // 传递函数钩子
```

#### Lattice实现

**格元素状态**：
```cpp
// Lattice元素的可能状态
enum LatticeState {
    Uninitialized,  // 未初始化
    Defined,        // 已定义（常量）
    Overdefined     // 过度定义（Top/未知）
};

class LatticeElement<ValueT> {
    LatticeState state;
    ValueT value;

    // Join操作
    ChangeResult join(LatticeElement<ValueT> other);

    // 标记悲观不动点
    void markPessimisticFixPoint();
};
```

**自定义Lattice示例**：
```cpp
// 元数据格：追踪字典属性
class MetadataLatticeValue {
    DictionaryValue metadata;  // 字典属性

    // Join操作：保守合并
    ChangeResult join(LatticeElement* other) {
        if (other->isUninitialized()) {
            return ChangeResult::NoChange;
        }
        if (isUninitialized()) {
            setValue(other->getValue());
            return ChangeResult::Change;
        }

        // 合并字典：保留公共键
        auto merged = mergeDictionaries(metadata, other->getValue());
        if (merged != metadata) {
            metadata = merged;
            return ChangeResult::Change;
        }
        return ChangeResult::NoChange;
    }
};
```

#### Transfer函数实现

**通过visitOperation钩子**：
```cpp
class MetadataAnalysis : public ForwardDataFlowAnalysis<DictionaryValue> {
public:
    // 实现传递函数
    ChangeResult visitOperation(Operation *op,
                          ArrayRef<LatticeElement<DictionaryValue> *> operands) {
        // 1. 获取操作数的信息
        DictionaryValue result = joinOperands(operands);

        // 2. 查找操作的元数据属性
        if (op->hasAttr("metadata")) {
            result = op->getAttr("metadata").getValue();
        }

        // 3. 更新操作结果的信息
        setLatticeElement(op->getResult(0), result);

        // 4. 更新块参数（基本块参数）
        for (BlockArgument arg : op->getBlock()->getArguments()) {
            updateBlockArgument(arg, result);
        }

        return ChangeResult::Change;
    }
};
```

#### 分析执行

```cpp
// 运行分析
MetadataAnalysis analysis;
analysis.run(function);

// 查询结果
auto latticeElement = analysis.lookupLatticeElement(value);
if (latticeElement->isDefined()) {
    DictionaryValue metadata = latticeElement->getValue();
    // 使用分析结果
}
```

### MLIR vs 手动实现对比

| 特性 | MLIR框架 | 手动实现 |
|--------|-----------|-----------|
| **Lattice管理** | 自动化 | 手动维护 |
| **Worklist算法** | 内置 | 需要自己实现 |
| **传递函数** | 通过钩子实现 | 手动计算 |
| **收敛检测** | 自动 | 手动跟踪变化 |
| **可扩展性** | 高（模块化） | 低（紧耦合） |

---

## 📊 EP21实现分析

### 当前状态

| 组件 | 状态 | 位置 | 说明 |
|--------|--------|--------|------|
| **抽象数据流分析基类** | ✅ 已实现 | `AbstractDataFlowAnalysis.java` |
| **活跃变量分析** | ✅ 已实现 | `LiveVariableAnalysis.java` |
| **常量传播** | ✅ 已实现 | `ConstantPropagationAnalysis.java` |
| **Worklist算法** | ✅ 已实现 | `AbstractDataFlowAnalysis` 内置 |
| **Meet操作** | ✅ 已实现 | 并集、交集 |
| **前向/后向支持** | ✅ 已实现 | `ForwardDataFlowAnalysis`, `BackwardDataFlowAnalysis` |
| **格理论应用** | ✅ 已实现 | Top/Bottom元素，偏序 |

### 架构设计

```
数据流分析框架
  ├── AbstractDataFlowAnalysis<T>         // 抽象基类
  │   ├── analyze()                      // 分析入口
  │   ├── initialize()                    // 初始化
  │   └── transfer()                     // 传递函数（抽象）
  │
  ├── ForwardDataFlowAnalysis<T>          // 前向分析
  │   ├── getPredecessors()             // 获取前驱
  │   └── propagateForward()             // 前向传播
  │
  ├── BackwardDataFlowAnalysis<T>         // 后向分析
  │   ├── getSuccessors()               // 获取后继
  │   └── propagateBackward()            // 后向传播
  │
  └── 具体分析实现
      ├── LiveVariableAnalysis            // 活跃变量（后向）
      ├── ConstantPropagationAnalysis      // 常量传播（前向）
      └── ReachingDefinitionsAnalysis    // 到达定义（前向）
```

### 关键实现细节

#### 1. Worklist优化

```java
protected void analyze() {
    // 使用ArrayDeque实现FIFO工作列表
    Queue<Integer> worklist = new ArrayDeque<>();

    // 按遍历顺序初始化（前向：逆后序，后向：后序）
    List<Integer> initOrder = getInitializationOrder();
    worklist.addAll(initOrder);

    while (!worklist.isEmpty()) {
        int blockId = worklist.poll();

        // 计算新的数据流值
        T newValue = computeValue(blockId);

        T oldValue = getValue(blockId);

        // 如果信息变化，更新邻居
        if (!newValue.equals(oldValue)) {
            setValue(blockId, newValue);

            // 只添加发生变化的邻居
            Collection<Integer> neighbors = getNeighborsToUpdate(blockId);
            for (Integer neighbor : neighbors) {
                if (!worklist.contains(neighbor)) {
                    worklist.add(neighbor);
                }
            }
        }
    }
}
```

#### 2. Meet操作抽象

```java
public interface MeetOperator<T> {
    T meet(T a, T b);
}

public class UnionMeet<T> implements MeetOperator<Set<T>> {
    @Override
    public Set<T> meet(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<>(a);
        result.addAll(b);  // 并集
        return result;
    }
}

public class IntersectionMeet<T> implements MeetOperator<Set<T>> {
    @Override
    public Set<T> meet(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<>(a);
        result.retainAll(b);  // 交集
        return result;
    }
}
```

#### 3. 传递函数抽象

```java
public abstract class AbstractDataFlowAnalysis<T> {
    protected abstract T transfer(T input, BasicBlock<I> block);

    protected T computeValue(int blockId) {
        BasicBlock<I> block = cfg.getBlock(blockId);
        T inputValue = computeInput(blockId);
        return transfer(inputValue, block);
    }

    protected T computeInput(BasicBlock<I> block) {
        Collection<Integer> neighbors = getNeighbors(block);
        T result = getInitialValue();

        for (int neighborId : neighbors) {
            T neighborValue = getValue(neighborId);
            result = meetOperator.meet(result, neighborValue);
        }
        return result;
    }
}
```

### 与优化Pass的集成

**数据流分析 + 优化Pass管道**：
```
数据流分析阶段
  ├── 活跃变量分析 → 为寄存器分配提供信息
  ├── 常量传播 → 为常量折叠优化提供信息
  └── 到达定义 → 为复制传播提供信息

优化Pass阶段
  ├── ConstantFoldingOptimizer        // 基于常量传播结果
  ├── DeadCodeEliminationOptimizer     // 基于活跃变量分析
  ├── CommonSubexpressionElimination // 基于到达定义
  └── GraphColoringAllocator         // 基于活跃变量分析
```

---

## 📚 参考资源

### 学术论文

| 论文 | 作者 | 年份 | 核心贡献 | 链接 |
|------|--------|--------|----------|--------|
| A Unified Approach to Global Program Optimization | Gary A. Kildall | 1973 | 数据流分析框架的奠基性论文 | [PDF](https://dl.acm.org/doi/10.1145/512927.512945) |
| Efficiently Computing Static Single Assignment Form | Cytron et al. | 1991 | SSA构造、数据流分析 | [PDF](https://www.cs.princeton.edu/~cytron/papers/efficient-ssa.pdf) |

### 教程与课程

| 资源 | 主题 | 链接 |
|--------|--------|--------|
| **CMU 15-410: Introduction to Dataflow Analysis** | 数据流分析理论基础（格理论、传递函数） | [PDF](http://www.cs.cmu.edu/afs/cs.cmu.edu/academic/class/15745-s10/www/lectures/L5-Intro-to-Dataflow-pre-class.pdf) |
| **CMU 15-723: Dataflow Analysis** | 高级数据流分析（Worklist算法、迭代收敛） | [PDF](http://www.cs.cmu.edu/afs/cs.cmu.edu/academic/class/15745-s18/www/lectures/) |
| **MIT 6.820: Dataflow Analysis, Lattices, Fixed Points** | 格理论、不动点计算 | [PDF](https://ocw.mit.edu/courses/6-820-fundamentals-of-program-analysis-fall-2015/4aae8677722746c91c8646d318e1c5e8_MIT6_820F15_L17.pdf) |
| **Cornell CS 6120: Data Flow** | 数据流框架（偏序、meet操作） | [网页](https://www.cs.cornell.edu/courses/cs6120/2020fa/lesson/4/) |
| **UPenn CIS 3410: Dataflow Analysis and Optimizations** | 数据流分析实现（Worklist、DCE、常量传播） | [文档](https://www.seas.upenn.edu/~cis3410/current/hw6/doc/hw6-opt.html) |
| **OpenRewrite Docs: Data Flow** | 开源框架实现指南 | [文档](https://docs.moderne.io/openrewrite-advanced-program-analysis/data-flow/introduction/) |

### 开源实现

| 项目 | 组件 | 链接 | 核心贡献 |
|------|--------|--------|----------|
| **LLVM MLIR** | ForwardDataFlowAnalysis | [文档](https://mlir.llvm.org/docs/Tutorials/DataFlowAnalysis) | 现代数据流分析框架 |
| **LLVM Clang** | BackwardDataflowWorklist | [参考](https://clang.llvm.org/doxygen/structclang_1_1BackwardDataflowWorklist.html) | 后向工作列表实现 |
| **OpenRewrite** | ForwardDataFlowAnalysis | [文档](https://docs.moderne.io/openrewrite-advanced-program-analysis/data-flow/reaching-definitions/) | 实用框架 |
| **LLVM** | Liveness, DeadCodeAnalysis | [参考](https://mlir.llvm.org/doxygen/namespacemlir_1_1dataflow.html) | 工业级实现 |

---

## 🎯 最佳实践

### 1. 分析选择

**决策树**：
```
需要分析什么属性？
  ├── 变量是否在未来被使用？
  │   └── 活跃变量分析（后向）
  ├── 哪些定义可能到达这里？
  │   └── 到达定义（前向）
  ├── 变量是否是常量？
  │   └── 常量传播（前向）
  ├── 哪些表达式可用？
  │   └── 可用表达式（前向）
  └── 哪些变量必须被使用？
      └── 非常忙表达式（前向）
```

### 2. 性能优化

**Worklist优化策略**：
- **遍历顺序**：
  - 前向分析：反向后序（从出口向入口）
  - 后向分析：后序（从入口向出口）
- **去重**：避免重复添加节点到工作列表
- **优先级**：优先处理循环头或高度变化的节点

### 3. 调试技巧

**可视化数据流**：
```java
// 在分析过程中输出数据流状态
protected void debugPrintState(int blockId, T inValue, T outValue) {
    System.out.println("Block " + blockId + ":");
    System.out.println("  In:  " + inValue);
    System.out.println("  Out: " + outValue);
}
```

**收敛验证**：
```java
// 记录迭代次数
protected int iterationCount = 0;
protected boolean changed = true;

while (changed && iterationCount < MAX_ITERATIONS) {
    iterationCount++;
    // ... 分析逻辑 ...
}

if (iterationCount >= MAX_ITERATIONS) {
    log.warn("Dataflow analysis did not converge after " + MAX_ITERATIONS + " iterations");
}
```

### 4. 测试策略

**单元测试重点**：
1. **简单CFG**：线性、分支、循环
2. **Meet操作**：验证并集、交集的正确性
3. **传递函数**：验证信息传播的正确性
4. **收敛性**：验证算法在合理迭代内收敛
5. **边界情况**：空CFG、单节点、不可达节点

**测试用例示例**：
```java
@Test
void shouldComputeLivenessForSimpleBlock() {
    // Given: 单基本块CFG
    CFG<IRNode> cfg = createSimpleCFG();

    // When: 执行活跃变量分析
    LiveVariableAnalysis analysis = new LiveVariableAnalysis(cfg);
    analysis.analyze();

    // Then: 验证活跃变量
    assertThat(analysis.getLiveIn(1)).containsExactly(var("x"), var("y"));
    assertThat(analysis.getLiveOut(2)).containsExactly(var("x"));
}

@Test
void shouldPropagateConstantsForward() {
    // Given: 包含常量定义的CFG
    CFG<IRNode> cfg = createConstantPropagationCFG();

    // When: 执行常量传播
    ConstantPropagationAnalysis analysis = new ConstantPropagationAnalysis(cfg);
    analysis.analyze();

    // Then: 验证常量传播
    assertThat(analysis.isConstant("x")).isTrue();
    assertThat(analysis.getConstantValue("x")).isEqualTo(42);
}
```

---

## 📝 后续优化方向

### 短期（1-2个月）

1. **改进Worklist顺序**：
   - 实现优先级工作列表
   - 支持深度优先优化顺序

2. **增加数据流分析**：
   - 实现到达定义分析
   - 实现可用表达式分析

3. **增强测试覆盖**：
   - 添加更多边界情况测试
   - 添加性能基准测试

### 中期（3-4个月）

1. **稀疏数据流分析**：
   - 使用SSA形式优化分析
   - 减少不必要的计算

2. **过程间分析**：
   - 跨函数边界分析
   - 支持内联优化

3. **增量分析**：
   - 只重分析受影响的节点
   - 支持交互式编译器

### 长期（5-6个月）

1. **并行数据流分析**：
   - 利用多核CPU并行分析
   - 加速大型程序分析

2. **机器学习辅助分析**：
   - 使用历史数据优化分析顺序
   - 自适应Worklist策略

---

**文档版本**: 1.0
**创建日期**: 2026-01-15
**适用范围**: EP21模块数据流分析
**维护者**: EP21模块维护团队
**审核要求**: 需要补充更多实际应用案例、性能对比和最佳实践示例
