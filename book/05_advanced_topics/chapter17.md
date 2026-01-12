# 第17章：SSA和数据流基础

## 本章概述

本章聚焦于静态单赋值（SSA）形式和数据流分析，它们是现代编译器高级优化阶段的核心技术。通过学习本章，你将掌握SSA形式的构建方法、支配关系计算和数据流分析框架的实现，这是第18章全局优化的基础。

【你现在站在哪】:
... → [IR生成] → [CFG构建] → [基础优化] → ✅ **SSA转换与数据流分析** → [全局优化] → [代码生成] → ...

## 动机与真实场景

想象你正在优化一个科学计算库中的矩阵运算函数。函数内部有一个多层嵌套的循环，计算了大量中间结果。在性能分析时，你发现其中某些中间变量在每次循环迭代中都被重复计算，这些计算实际上在多个迭代中产生相同的结果。

如果缺少本章的能力，你将面临：
- 无法识别跨基本块的变量使用
- 难以优化跨分支的冗余计算
- 无法精确定位优化机会

本章将教你如何：
- 将控制流图转换为SSA形式
- 计算支配关系和支配边界
- 实现数据流分析框架
- 为后续的全局优化奠定基础

## 人类工程师线：技术与实现

### 核心概念

#### 静态单赋值（SSA）形式

通俗解释：SSA形式是一种中间表示，其中每个变量在程序中只被赋值一次。当控制流汇合时，引入特殊的Phi函数来选择不同来源的值。

[图1：SSA转换示意图]
原始代码：
```
x = 1;        // Block1
if (condition) {
    x = 2;    // Block2
} else {
    x = 3;    // Block3
}
print(x);      // Block4 (汇聚点)
```

SSA形式：
```
x_1 = 1;      // Block1
if (condition) {
    x_2 = 2;  // Block2
} else {
    x_3 = 3;  // Block3
}
x_4 = φ(x_2, x_3);  // Block4: Phi函数选择x_2或x_3
print(x_4);    // Block4: 使用唯一的x_4
```

#### 支配关系与支配边界

通俗解释：节点A支配节点B，意味着从入口节点到B的所有路径都必须经过A。支配边界则是一个节点的"影响范围"。

#### 数据流分析

通俗解释：数据流分析通过追踪数据在程序中的流动，分析每个程序点的变量状态。

### 与仓库 EP 的对应关系

对应 EP：EP21

目录结构：
```
ep21/
├── src/main/java/org/teachfx/antlr4/ep21/
│   ├── analysis/ssa/
│   │   ├── DominatorAnalysis.java     // 支配关系分析器
│   │   └── SSAGraph.java             // SSA图构建器
│   ├── analysis/dataflow/
│   │   ├── AbstractDataFlowAnalysis.java  // 数据流分析抽象基类
│   │   ├── ReachingDefinitionAnalysis.java // 到达定义分析
│   │   └── LiveVariableAnalysis.java       // 活跃变量分析
│   ├── pass/cfg/
│   │   ├── CFG.java                  // 控制流图
│   │   └── BasicBlock.java           // 基本块
```

关键类/方法说明：

**DominatorAnalysis** - 支配关系分析器
```java
public class DominatorAnalysis<I extends IRNode> {
    private final CFG<I> cfg;                        // 控制流图
    private final Map<Integer, Set<Integer>> dom;    // 支配集合
    private final Map<Integer, Integer> idom;       // 直接支配者
    private final Map<Integer, Set<Integer>> df;      // 支配边界

    /**
     * 执行完整的支配关系分析
     * 1. 计算支配集合（dominance sets）
     * 2. 构建支配树（dominator tree）
     * 3. 计算支配边界（dominance frontier）
     */
    public void analyze() {
        computeDominators();
        computeDominatorTree();
        computeDominanceFrontier();
    }

    /**
     * 计算支配集合（迭代算法）
     * 算法：对于所有节点n：
     *   dom[n] = {n} ∪ ∩(dom[p] for p in predecessors(n))
     * 迭代直到收敛。
     */
    private void computeDominators() {
        // 初始化：所有节点支配自己
        for (BasicBlock<I> block : cfg.nodes) {
            Set<Integer> selfSet = new HashSet<>();
            selfSet.add(block.getId());
            dom.put(block.getId(), selfSet);
        }

        // 入口基本块（假设ID为0）只支配自己
        Set<Integer> entryDom = new HashSet<>();
        entryDom.add(0);
        dom.put(0, entryDom);

        boolean changed = true;
        int iteration = 0;
        int maxIterations = cfg.nodes.size() * cfg.nodes.size();

        do {
            changed = false;

            // 按拓扑顺序处理节点
            for (BasicBlock<I> block : cfg.nodes) {
                int blockId = block.getId();
                if (blockId == 0) continue;

                Set<Integer> oldSet = dom.get(blockId);
                Set<Integer> newSet = new HashSet<>();

                // 初始化为所有节点的集合
                for (BasicBlock<I> b : cfg.nodes) {
                    newSet.add(b.getId());
                }

                // 与前驱支配集合求交集
                Set<Integer> predecessors = cfg.getFrontier(blockId);
                if (!predecessors.isEmpty()) {
                    for (int predId : predecessors) {
                        Set<Integer> predDom = dom.get(predId);
                        if (predDom != null) {
                            newSet.retainAll(predDom);
                        }
                    }
                    newSet.add(blockId);
                }

                if (!newSet.equals(oldSet)) {
                    dom.put(blockId, newSet);
                    changed = true;
                }
            }

            iteration++;
            if (iteration > maxIterations) {
                throw new IllegalStateException("支配集合计算未收敛");
            }

        } while (changed);
    }

    /**
     * 计算支配边界
     * 支配边界DF[n] = {x | n支配x的某个前驱，但不严格支配x}
     */
    private void computeDominanceFrontier() {
        // 初始化
        for (BasicBlock<I> block : cfg.nodes) {
            df.put(block.getId(), new HashSet<>());
        }

        // 第一步：计算局部支配边界
        for (BasicBlock<I> block : cfg.nodes) {
            int n = block.getId();
            Set<Integer> successors = cfg.getSucceed(n);

            for (int s : successors) {
                if (idom.get(s) == null || idom.get(s) != n) {
                    df.get(n).add(s);
                }
            }
        }

        // 第二步：传播支配边界
        boolean changed;
        do {
            changed = false;
            for (BasicBlock<I> block : cfg.nodes) {
                int n = block.getId();
                Set<Integer> currentDF = df.get(n);
                Set<Integer> newDF = new HashSet<>(currentDF);

                for (int c : currentDF) {
                    Set<Integer> dfOfC = df.get(c);
                    if (dfOfC != null) {
                        newDF.addAll(dfOfC);
                    }
                }

                if (!newDF.equals(currentDF)) {
                    df.put(n, newDF);
                    changed = true;
                }
            }
        } while (changed);
    }
}
```

**AbstractDataFlowAnalysis** - 数据流分析抽象基类
```java
public abstract class AbstractDataFlowAnalysis<T, I extends IRNode> implements DataFlowAnalysis<T, I> {

    protected final CFG<I> cfg;
    protected final Map<Integer, T> in;
    protected final Map<Integer, T> out;
    protected final Map<I, T> instrIn;
    protected final Map<I, T> instrOut;

    /**
     * 数据流分析的核心迭代求解器
     */
    @Override
    public void analyze() {
        boolean changed = true;
        int iteration = 0;

        while (changed && iteration < 1000) {
            changed = false;
            iteration++;

            if (isForward()) {
                changed = forwardIteration();
            } else {
                changed = backwardIteration();
            }
        }

        if (iteration >= 1000) {
            System.err.println("警告: 数据流分析未在1000次迭代内收敛");
        }
    }

    /**
     * 前向分析迭代（如到达定义分析）
     */
    private boolean forwardIteration() {
        boolean changed = false;

        List<BasicBlock<I>> order = getForwardOrder();
        for (BasicBlock<I> block : order) {
            int blockId = block.getId();

            // 计算in[block] = meet(out[p]) for all predecessors p
            T newIn = getInitialValue();
            for (Integer predId : cfg.getFrontier(blockId)) {
                T predOut = out.get(predId);
                newIn = meet(newIn, predOut);
            }

            if (!newIn.equals(in.get(blockId))) {
                in.put(blockId, newIn);
                changed = true;
            }

            // 计算基本块内指令的数据流
            T current = newIn;
            for (I instr : getInstructions(block)) {
                T newOut = transfer(instr, current);
                if (!newOut.equals(instrOut.get(instr))) {
                    instrOut.put(instr, newOut);
                    instrIn.put(instr, current);
                    changed = true;
                }
                current = newOut;
            }

            if (!current.equals(out.get(blockId))) {
                out.put(blockId, current);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * 后向分析迭代（如活跃变量分析）
     */
    private boolean backwardIteration() {
        boolean changed = false;

        List<BasicBlock<I>> order = getBackwardOrder();
        for (BasicBlock<I> block : order) {
            int blockId = block.getId();

            T newOut = getInitialValue();
            for (Integer succId : cfg.getSucceed(blockId)) {
                T succIn = in.get(succId);
                newOut = meet(newOut, succIn);
            }

            if (!newOut.equals(out.get(blockId))) {
                out.put(blockId, newOut);
                changed = true;
            }

            List<I> instructions = getInstructions(block);
            T current = newOut;
            for (int i = instructions.size() - 1; i >= 0; i--) {
                I instr = instructions.get(i);
                T newIn = transfer(instr, current);
                if (!newIn.equals(instrIn.get(instr))) {
                    instrIn.put(instr, newIn);
                    instrOut.put(instr, current);
                    changed = true;
                }
                current = newIn;
            }

            if (!current.equals(in.get(blockId))) {
                in.put(blockId, current);
                changed = true;
            }
        }

        return changed;
    }

    // 抽象方法：子类必须实现
    public abstract T meet(T a, T b);
    public abstract T transfer(I instr, T input);
    public abstract T getInitialValue();
    public abstract boolean isForward();
}
```

**ReachingDefinitionAnalysis** - 到达定义分析
```java
public class ReachingDefinitionAnalysis extends AbstractDataFlowAnalysis<Set<Operand>, IRNode> {

    public ReachingDefinitionAnalysis(CFG<IRNode> cfg) {
        super(cfg);
    }

    @Override
    public boolean isForward() {
        return true;
    }

    @Override
    public Set<Operand> meet(Set<Operand> a, Set<Operand> b) {
        Set<Operand> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    @Override
    public Set<Operand> transfer(IRNode instr, Set<Operand> input) {
        Set<Operand> gen = computeGen(instr);
        Set<Operand> kill = computeKill(instr);

        Set<Operand> result = new HashSet<>(gen);
        result.addAll(input);
        result.removeAll(kill);
        return result;
    }

    @Override
    public Set<Operand> getInitialValue() {
        return new HashSet<>();
    }

    private Set<Operand> computeGen(IRNode instr) {
        Set<Operand> gen = new HashSet<>();
        if (instr instanceof Assign) {
            gen.add(((Assign) instr).getLhs());
        }
        return gen;
    }

    private Set<Operand> computeKill(IRNode instr) {
        Set<Operand> kill = new HashSet<>();
        if (instr instanceof Assign) {
            kill.add(((Assign) instr).getLhs());
        }
        return kill;
    }
}
```

**SSAGraph** - SSA图构建器
```java
public class SSAGraph {
    private final CFG<IRNode> originalCFG;
    private final Map<String, Integer> versionMap;
    private final Map<IRNode, IRNode> renamedNodes;
    private DominatorAnalysis<IRNode> dominatorAnalysis;
    private final Map<String, Stack<Integer>> varStacks;
    private final Map<String, Integer> currentVersion;

    /**
     * 构建SSA图
     * 核心算法：
     * 1. 插入Phi函数到汇聚节点（基于支配边界）
     * 2. 变量重命名（为每个定义分配唯一版本号）
     */
    public SSAGraph buildSSA() {
        insertPhiFunctions();
        renameVariables();
        return this;
    }

    /**
     * 插入Phi函数到汇聚节点
     */
    private void insertPhiFunctions() {
        Map<String, Set<Integer>> varDefs = collectVariableDefinitions();
        for (Map.Entry<String, Set<Integer>> entry : varDefs.entrySet()) {
            String varName = entry.getKey();
            Set<Integer> defBlocks = entry.getValue();
            insertPhiFunctionsForVariable(varName, defBlocks);
        }
    }

    /**
     * 变量重命名
     */
    private void renameVariables() {
        varStacks.clear();
        currentVersion.clear();
        Map<Integer, List<Integer>> domChildren = buildDominatorChildren();
        renameInBlock(0, domChildren);
    }
}
```

### 实战流程

```bash
# 进入 EP21 目录
cd ep21

# 编译项目
mvn clean compile

# 运行所有数据流分析测试
mvn test -Dtest=*DataFlow*Test

# 预期输出：
# [INFO] Tests run: 5, Failures: 0, Errors: 0
# [INFO] BUILD SUCCESS
```

## AI 协作线

### 上下文设计

**源码文件**（按阅读顺序）：
1. `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/ssa/DominatorAnalysis.java`
2. `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/AbstractDataFlowAnalysis.java`
3. `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/ReachingDefinitionAnalysis.java`
4. `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/LiveVariableAnalysis.java`
5. `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/ssa/SSAGraph.java`

### Prompt 模板

**类型 A: 数据流分析实现 Prompt 模板**
```
请为 CFG 实现一个新的数据流分析：{分析类型}。

任务目标：
- 实现前向/后向数据流分析算法
- 正确计算 gen、kill 集合
- 实现 meet 和 transfer 函数

具体要求：
1. 创建新的数据流分析类
   - 继承 `AbstractDataFlowAnalysis<T, IRNode>`
   - 实现 `getInitialValue()`, `meet()`, `transfer()` 方法
   - 在 `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/` 目录下

2. 实现核心算法
   - 计算基本块入口和出口的数据流信息
   - 迭代直到收敛
   - 处理基本块内指令的数据流

3. 添加单元测试
   - 覆盖正常情况、循环、多个基本块
   - 使用 JUnit 5 和 AssertJ

期望输出：
1. 新增的数据流分析类代码（含完整注释）
2. 完整的测试类代码
3. 算法复杂度分析
```

## 练习题

### 练习1：实现可用表达式分析（手工实现版）

**难度**：⭐⭐☆☆☆

**题目描述**：实现可用表达式分析（Available Expressions），一种前向数据流分析，用于追踪哪些表达式在每个程序点可用。

### 练习2：实现非常量传播分析（AI 协作版）

**难度**：⭐⭐⭐☆☆

**题目描述**：实现条件常量传播分析（Constant Propagation），使用格理论追踪常量在程序中的流动。

## 本章小结

通过本章的学习，你已经掌握了：
1. SSA形式的构建方法
2. 支配关系和支配边界的计算
3. 数据流分析框架的实现
4. Phi函数的放置算法

**下一章预告**：第18章将聚焦于全局优化技术，包括尾递归优化、循环不变代码外提和常量传播。

【你现在站在】:
```
... → [IR生成] → [CFG构建] → [基础优化] → ✅ [SSA和数据流分析] → [全局优化] → ...
```

继续加油！SSA和数据流分析是现代编译器优化的基础！
