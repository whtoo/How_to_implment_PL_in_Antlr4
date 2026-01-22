# EP21 CFG框架与尾递归优化联合增强重构方案

**版本**: v1.0 | **日期**: 2026-01-22 | **状态**: 技术设计

---

## 一、概述

### 1.1 背景与目标

本文档描述EP21编译器中**CFG框架增强**与**尾递归优化（TRO）**的联合增强重构方案。通过参考LLVM和GCC的工业级实现，结合当前EP21的代码架构，设计可实施的增强方案。

**核心目标**：
1. 增强CFG框架，支持复杂CFG变换（预头部创建、块分裂、边操作）
2. 完善尾递归优化，实现从递归到迭代的完整转换
3. 统一架构设计，避免代码重复

### 1.2 参考资源

| 来源 | 链接 | 核心贡献 |
|------|------|----------|
| **LLVM** | `llvm/lib/Transforms/Scalar/TailRecursionElimination.cpp` | 工业级TRO实现，包含完整的CFG变换 |
| **LLVM** | `llvm/lib/Transforms/Scalar/LoopUnrollPass.cpp` | 循环展开，包含预头部创建 |
| **GCC** | `gcc/tree-ssa-loop-manip.c` | GCC循环操作实现 |
| **CMU** | `http://www.cs.cmu.edu/~janh/courses/411/24/lectures/20-tail-calls.pdf` | 尾调用优化理论 |
| **V8** | `https://v8.dev/blog/wasm-tail-call` | WebAssembly尾调用实现 |

---

## 二、当前实现分析

### 2.1 CFG框架现状

**文件**: `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/CFG.java`

**当前能力**：
```java
public class CFG<I extends IRNode> implements Iterable<BasicBlock<I>> {
    public final List<BasicBlock<I>> nodes;              // 节点列表
    public final List<Triple<Integer, Integer, Integer>> edges;  // 边列表
    private final List<Pair<Set<Integer>, Set<Integer>>> links;  // 前驱/后继关系
    
    // 基本操作
    public BasicBlock<I> getBlock(int id);               // 获取节点
    public Set<Integer> getFrontier(int id);             // 前驱集合
    public Set<Integer> getSucceed(int id);              // 后继集合
    public boolean removeEdge(Triple<...> edge);         // 移除边
    public String toDOT();                               // DOT导出
}
```

**当前限制**：
1. ❌ 缺少块分裂操作（splitBlock）
2. ❌ 缺少边插入操作（insertEdge）
3. ❌ 缺少预头部创建（createPreheader）
4. ❌ 缺少支配树分析
5. ❌ 缺少LCSSA变换支持
6. ❌ 边操作后需要手动更新links

### 2.2 尾递归优化现状

**文件**: `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/TailRecursionOptimizer.java`

**当前架构**：
```java
// Path B: 检测层和转换层分离
public class TailRecursionOptimizer implements IFlowOptimizer<IRNode> {
    private Set<String> optimizedFunctions;  // 已优化的函数
    
    // 检测层
    private boolean isFibonacciPattern();    // Fibonacci模式检测
    private List<TailCallInfo> detectDirectTailCalls();  // 尾调用检测
    
    // 转换层（在代码生成器中）
    // RegisterVMGenerator.TROHelper.generateFibonacciIterative()
}
```

**当前限制**：
1. ❌ 检测层完整，但缺少CFG变换能力
2. ❌ 缺少累加器模式检测（`canTransformAccumulatorRecursion`）
3. ❌ 缺少实际循环转换逻辑（`createTailRecurseLoopHeader`）
4. ❌ 缺少PHI节点创建和参数重绑定
5. ❌ 缺少栈帧模拟（StackSimulator）

---

## 三、LLVM TRO实现深度分析

### 3.1 核心算法：markTails()

**位置**: `TailRecursionElimination.cpp` Lines 175-290

**关键算法**：
```cpp
// 两阶段工作列表传播算法
if (!WorklistEscaped.empty()) {
    BB = WorklistEscaped.pop_back_val();
    Escaped = ESCAPED;
} else {
    BB = nullptr;
    while (!WorklistUnescaped.empty()) {
        auto *NextBB = WorklistUnescaped.pop_back_val();
        if (Visited[NextBB] == UNESCAPED) {
            BB = NextBB;
            Escaped = UNESCAPED;
            break;
        }
    }
}
```

**技术要点**：
1. **逃逸点追踪**：使用`AllocaDerivedValueTracker`追踪所有分配值的逃逸
2. **延迟标记**：延迟标记调用直到块逃逸状态已知
3. **Nocapture分析**：识别`nocapture`参数证明栈帧独立性

### 3.2 核心变换：createTailRecurseLoopHeader()

**位置**: `TailRecursionElimination.cpp` Lines 440-495

**变换策略**：

```
[BEFORE]                          [AFTER]
EntryBB                           NewEntry (preheader)
   |                                  |
   +--> ...                     NewEntry --> EntryBB (now "tailrecurse")
                                    |
                                    +--> ... (loop body)
```

**关键步骤**：
```cpp
// 1. 创建新入口块
BasicBlock *NewEntry = BasicBlock::Create(F.getContext(), "", &F, HeaderBB);
NewEntry->takeName(HeaderBB);
HeaderBB->setName("tailrecurse");

// 2. 插入分支回跳
auto *BI = BranchInst::Create(HeaderBB, NewEntry);

// 3. 为每个函数参数创建PHI节点
PHINode *PN = PHINode::Create(I->getType(), 2, I->getName() + ".tr");
PN->insertBefore(InsertPos);
I->replaceAllUsesWith(PN);
PN->addIncoming(&*I, NewEntry);
```

### 3.3 CFG操纵技术

**使用DomTreeUpdater**：
```cpp
// 边插入后更新支配树
DTU.applyUpdates({{DominatorTree::Insert, BB, HeaderBB}});

// 入口块变化时重新计算支配树
DTU.recalculate(*NewEntry->getParent());
```

---

## 四、联合增强设计方案

### 4.1 架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                    CFG框架增强层                              │
├─────────────────────────────────────────────────────────────┤
│  CFGBuilder        │  BlockSplitter  │  EdgeManipulator    │
│  - 构建CFG          │  - splitBlock   │  - addEdge          │
│  - 验证完整性        │  - splitEdge    │  - removeEdge       │
│                    │                 │  - redirectEdge     │
├─────────────────────────────────────────────────────────────┤
│  PreheaderCreator  │  DominatorTree  │  LCSSABuilder       │
│  - createPreheader │  - 计算支配关系   │  - buildLCSSA       │
│  - ensureSinglePred │  - 支配边界      │  - insertPHI        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    尾递归优化增强层                            │
├─────────────────────────────────────────────────────────────┤
│  TailCallMarker    │  LoopHeaderCreator │  AccumulatorTR    │
│  - markTails()     │  - createLoopHeader │ - detectAccum    │
│  - escapeAnalysis  │  - insertPHI       │  - transformAcc   │
├─────────────────────────────────────────────────────────────┤
│  StackSimulator    │  CFGTransformer    │  TROIntegration   │
│  - 栈帧模拟         │  - applyTransform  │  - 端到端TRO流程   │
│  - 帧重用           │  - updateDomTree   │                  │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 CFG框架增强

#### 4.2.1 BlockManipulator类

```java
/**
 * 块操作器 - 提供CFG块的创建、分裂、删除操作
 */
public class BlockManipulator<I extends IRNode> {
    private final CFG<I> cfg;
    
    /**
     * 分裂基本块
     * @param block 要分裂的块
     * @param instructionIndex 分裂点指令索引
     * @return 新创建的块
     */
    public BasicBlock<I> splitBlock(BasicBlock<I> block, int instructionIndex) {
        // 1. 在instructionIndex处创建新块
        BasicBlock<I> newBlock = createBlock();
        
        // 2. 移动instructionIndex后的指令到新块
        List<Loc<I>> tailInstructions = block.codes.subList(instructionIndex, block.codes.size());
        newBlock.codes.addAll(tailInstructions);
        tailInstructions.clear();
        
        // 3. 更新边关系
        redirectEdges(block, newBlock);
        
        // 4. 在block和新块之间插入边
        addEdge(block, newBlock);
        
        return newBlock;
    }
    
    /**
     * 预头部创建
     * @param loopHeader 循环头块
     * @return 预头部块
     */
    public BasicBlock<I> createPreheader(BasicBlock<I> loopHeader) {
        // 1. 创建新块作为预头部
        BasicBlock<I> preheader = createBlock();
        preheader.setLabel(new Label("preheader_" + loopHeader.getId(), null));
        
        // 2. 重新路由所有指向loopHeader的前驱
        Set<Integer> predecessors = cfg.getFrontier(loopHeader.getId());
        for (int predId : predecessors) {
            BasicBlock<I> pred = cfg.getBlock(predId);
            // 移除pred -> loopHeader的边
            removeEdge(pred, loopHeader);
            // 添加pred -> preheader的边
            addEdge(pred, preheader);
        }
        
        // 3. 添加preheader -> loopHeader的边
        addEdge(preheader, loopHeader);
        
        return preheader;
    }
}
```

#### 4.2.2 DominatorTree类

```java
/**
 * 支配树分析器
 */
public class DominatorTree<I extends IRNode> {
    private final CFG<I> cfg;
    private final List<Set<Integer>> dominators;  // 每个节点的支配集合
    
    /**
     * 计算支配树（经典迭代算法）
     * @param entry 入口块ID
     */
    public void compute(int entry) {
        int n = cfg.nodes.size();
        dominators = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            dominators.add(new HashSet<>());
        }
        
        // 初始化
        dominators.get(entry).add(entry);
        for (int i = 0; i < n; i++) {
            if (i != entry) {
                dominators.get(i).addAll(getAllNodes());
            }
        }
        
        // 迭代直到收敛
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < n; i++) {
                if (i == entry) continue;
                
                Set<Integer> newDom = new HashSet<>();
                newDom.add(i);
                
                Set<Integer> preds = cfg.getFrontier(i);
                for (int pred : preds) {
                    newDom.retainAll(dominators.get(pred));
                }
                newDom.add(i);
                
                if (!newDom.equals(dominators.get(i))) {
                    dominators.set(i, newDom);
                    changed = true;
                }
            }
        }
    }
    
    /**
     * 获取支配边界
     */
    public Set<Integer> getDominanceFrontier(int nodeId) {
        Set<Integer> df = new HashSet<>();
        Set<Integer> succs = cfg.getSucceed(nodeId);
        
        for (int succ : succs) {
            if (!dominators.get(succ).contains(nodeId)) {
                df.add(succ);
            }
        }
        
        return df;
    }
}
```

### 4.3 尾递归优化增强

#### 4.3.1 EnhancedTailRecursionOptimizer类

```java
/**
 * 增强版尾递归优化器 - 实现完整的TRO
 */
public class EnhancedTailRecursionOptimizer<I extends IRNode> implements IFlowOptimizer<IRNode> {
    private static final Logger logger = LogManager.getLogger(EnhancedTailRecursionOptimizer.class);
    
    private final BlockManipulator<I> blockManipulator;
    private final DominatorTree<I> dominatorTree;
    
    @Override
    public void onHandle(CFG<I> cfg) {
        logger.info("开始增强尾递归优化...");
        
        // 1. 标记尾调用候选
        markTailCalls(cfg);
        
        // 2. 对每个可优化的函数进行循环转换
        for (FunctionInfo funcInfo : targetFunctions) {
            transformToLoop(cfg, funcInfo);
        }
        
        logger.info("尾递归优化完成");
    }
    
    /**
     * 标记尾调用候选
     */
    private void markTailCalls(CFG<I> cfg) {
        // LLVM风格的两阶段工作列表算法
        Set<BasicBlock<I>> escapedBlocks = new HashSet<>();
        Set<BasicBlock<I>> unescapedBlocks = new HashSet<>();
        
        // 初始化工作列表
        for (BasicBlock<I> block : cfg) {
            if (hasEscapingAllocation(block)) {
                escapedBlocks.add(block);
            } else {
                unescapedBlocks.add(block);
            }
        }
        
        // 传播逃逸状态
        propagateEscapeState(cfg, escapedBlocks, unescapedBlocks);
        
        // 标记可优化的尾调用
        markOptimizableCalls(cfg, escapedBlocks);
    }
    
    /**
     * 转换为循环
     */
    private void transformToLoop(CFG<I> cfg, FunctionInfo funcInfo) {
        BasicBlock<I> entryBlock = funcInfo.entryBlock;
        
        // 1. 创建新的入口块（预头部）
        BasicBlock<I> newEntry = blockManipulator.createPreheader(entryBlock);
        newEntry.setLabel(new Label("tailrecurse_header", null));
        
        // 2. 移动静态分配到新入口块
        moveStaticAllocas(entryBlock, newEntry);
        
        // 3. 创建参数PHI节点
        Map<Operand, PHINode> argumentPHIs = createArgumentPHIs(funcInfo, entryBlock, newEntry);
        
        // 4. 处理累加器（如果适用）
        PHINode accumulatorPHI = null;
        if (funcInfo.hasAccumulatorPattern) {
            accumulatorPHI = createAccumulatorPHI(funcInfo, entryBlock);
        }
        
        // 5. 将尾调用替换为分支
        replaceTailCallWithBranch(funcInfo, entryBlock, argumentPHIs, accumulatorPHI);
        
        // 6. 更新支配树
        dominatorTree.compute(funcInfo.entryId);
    }
    
    /**
     * 创建参数PHI节点
     */
    private Map<Operand, PHINode> createArgumentPHIs(FunctionInfo funcInfo,
                                                      BasicBlock<I> entryBlock,
                                                      BasicBlock<I> newEntry) {
        Map<Operand, PHINode> phis = new HashMap<>();
        
        for (Operand arg : funcInfo.arguments) {
            PHINode phi = new PHINode(arg.getType(), arg.getName() + ".tr");
            phi.addIncoming(arg, newEntry);  // 从新入口进入的原始值
            phi.addIncoming(/* 递归调用的值 */, entryBlock);  // 从循环体进入的值
            
            entryBlock.codes.add(0, phi);
            arg.replaceAllUsesWith(phi);
            phis.put(arg, phi);
        }
        
        return phis;
    }
    
    /**
     * 累加器变换
     */
    private PHINode createAccumulatorPHI(FunctionInfo funcInfo, BasicBlock<I> entryBlock) {
        PHINode phi = new PHINode(
            funcInfo.returnType,
            "accumulator.tr"
        );
        
        // 初始化累加器
        phi.addIncoming(funcInfo.identityValue, entryBlock);
        
        return phi;
    }
}
```

#### 4.3.2 StackSimulator类

```java
/**
 * 栈帧模拟器 - 用于尾递归优化中的栈重用
 */
public class StackSimulator<I extends IRNode> {
    
    /**
     * 模拟栈帧重用
     * @param function 被优化的函数
     * @param allocator 寄存器分配器
     */
    public void simulateStackReuse(FunctionInfo function, IRegisterAllocator allocator) {
        // 1. 分析每个局部变量的生存期
        Map<Operand, LiveInterval> intervals = analyzeLiveIntervals(function);
        
        // 2. 识别可重用的栈槽位
        Map<Operand, Integer> reusableSlots = identifyReusableSlots(intervals);
        
        // 3. 更新指令使用新的栈槽位
        updateInstructionSlots(function, reusableSlots);
        
        // 4. 生成栈重用代码
        generateStackReuseCode(function, reusableSlots);
    }
    
    /**
     * 分析活跃区间
     */
    private Map<Operand, LiveInterval> analyzeLiveIntervals(FunctionInfo function) {
        // 基于数据流分析计算每个变量的活跃区间
        LiveVariableAnalysis analysis = new LiveVariableAnalysis();
        analysis.analyze(function.cfg);
        
        Map<Operand, LiveInterval> intervals = new HashMap<>();
        for (BasicBlock<I> block : function.cfg) {
            // 计算每个变量在块中的活跃区间
            // ...
        }
        return intervals;
    }
    
    /**
     * 识别可重用的栈槽位（区间不重叠的变量）
     */
    private Map<Operand, Integer> identifyReusableSlots(Map<Operand, LiveInterval> intervals) {
        // 按结束位置排序
        List<Map.Entry<Operand, LiveInterval>> sorted = 
            intervals.entrySet().stream()
                .sorted((a, b) -> a.getValue().end - b.getValue().end)
                .toList();
        
        Map<Operand, Integer> reusable = new HashMap<>();
        int nextAvailableSlot = 0;
        
        for (var entry : sorted) {
            if (entry.getValue().start >= nextAvailableSlot) {
                reusable.put(entry.getKey(), nextAvailableSlot);
                nextAvailableSlot++;
            }
        }
        
        return reusable;
    }
}
```

---

## 五、实施计划

### 5.1 第一阶段：CFG框架增强

| 任务 | 文件 | 工作量 | 优先级 |
|------|------|--------|--------|
| 实现BlockManipulator类 | `pass/cfg/BlockManipulator.java` | 3天 | P0 |
| 实现DominatorTree类 | `pass/cfg/DominatorTree.java` | 2天 | P0 |
| 实现PreheaderCreator | `pass/cfg/PreheaderCreator.java` | 2天 | P0 |
| 更新CFG类添加新操作 | `pass/cfg/CFG.java` | 1天 | P0 |
| 添加单元测试 | `pass/cfg/*Test.java` | 2天 | P1 |

### 5.2 第二阶段：尾递归优化增强

| 任务 | 文件 | 工作量 | 优先级 |
|------|------|--------|--------|
| 实现EnhancedTailRecursionOptimizer | `pass/cfg/EnhancedTailRecursionOptimizer.java` | 5天 | P0 |
| 实现StackSimulator | `pass/cfg/StackSimulator.java` | 3天 | P1 |
| 实现AccumulatorTransformer | `pass/cfg/AccumulatorTransformer.java` | 2天 | P1 |
| 更新TRO测试文件 | `pass/cfg/TailRecursionOptimizerTest.java` | 2天 | P1 |

### 5.3 第三阶段：集成与优化

| 任务 | 文件 | 工作量 | 优先级 |
|------|------|--------|--------|
| 集成到Compiler流水线 | `integration/Compiler.java` | 1天 | P0 |
| 端到端测试 | `integration/*Test.java` | 2天 | P1 |
| 性能优化 | 全部 | 2天 | P2 |

---

## 六、关键技术点

### 6.1 预头部创建算法

```
算法: createPreheader(loopHeader)

输入: loopHeader - 循环头块
输出: preheader - 新创建的预头部块

步骤:
1. 创建新块preheader
2. 对于每个loopHeader的前驱pred:
   a. 移除pred -> loopHeader的边
   b. 添加pred -> preheader的边
3. 添加preheader -> loopHeader的边
4. 返回preheader

时间复杂度: O(degree(loopHeader))
```

### 6.2 支配树计算算法

```
算法: computeDominators(entry)

输入: entry - 入口块ID
输出: dominators[] - 每个块的支配集合

初始化:
for each node n:
    if n == entry:
        dominators[n] = {n}
    else:
        dominators[n] = ALL_NODES

迭代:
repeat:
    changed = false
    for each node n != entry:
        newDom = {n} ∪ (∩_{p ∈ pred(n)} dominators[p])
        if newDom != dominators[n]:
            dominators[n] = newDom
            changed = true
until not changed

时间复杂度: O(n² × iterations)
```

### 6.3 尾递归到循环变换

```
变换模板:

原始代码:
def func(n, acc):
    if n == 0:
        return acc
    return func(n-1, acc * n)

变换后:
def func(n, acc):
    while True:  # 新循环
        if n == 0:
            return acc
        # 累加器更新
        acc = acc * n
        n = n - 1
        # 跳回循环头
        continue
```

---

## 七、参考实现链接

### 7.1 LLVM实现

| 文件 | 链接 |
|------|------|
| TailRecursionElimination.cpp | https://github.com/llvm/llvm-project/blob/main/llvm/lib/Transforms/Scalar/TailRecursionElimination.cpp |
| LoopUnrollPass.cpp | https://github.com/llvm/llvm-project/blob/main/llvm/lib/Transforms/Scalar/LoopUnrollPass.cpp |
| DominatorTree.h | https://github.com/llvm/llvm-project/blob/main/llvm/include/llvm/Analysis/DominatorTree.h |

### 7.2 GCC实现

| 文件 | 说明 |
|------|------|
| gcc/tree-ssa-loop-manip.c | 循环操作 |
| gcc/tree-ssa-loop-ivcanon.c | 循环规范化 |
| gcc/tree-ssa-dom.c | 支配分析 |

### 7.3 学术资源

| 资源 | 链接 |
|------|------|
| Cytron SSA论文 | https://doi.org/10.1145/109026.1991.139572 |
| CMU TCO讲义 | http://www.cs.cmu.edu/~janh/courses/411/24/lectures/20-tail-calls.pdf |
| V8 Wasm尾调用 | https://v8.dev/blog/wasm-tail-call |

---

## 八、风险与缓解

### 8.1 技术风险

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|----------|
| CFG变换引入循环依赖 | 低 | 高 | 编写完整性验证测试 |
| 支配树计算性能问题 | 中 | 中 | 使用工作列表优化 |
| 栈重用导致语义错误 | 低 | 高 | 严格活跃区间分析 |

### 8.2 兼容性风险

| 风险 | 缓解措施 |
|------|----------|
| 破坏现有优化器 | 保持IFlowOptimizer接口兼容 |
| 破坏现有测试 | 增量修改，确保测试通过 |

---

## 九、验收标准

### 9.1 功能验收

- [ ] 预头部创建成功率达到100%
- [ ] 支配树计算正确性验证通过
- [ ] 尾递归优化能处理Fibonacci模式
- [ ] 尾递归优化能处理一般尾递归
- [ ] 累加器模式变换正确

### 9.2 性能验收

- [ ] 预头部创建时间复杂度 O(n)
- [ ] 支配树计算支持500+节点的CFG
- [ ] 尾递归优化额外开销 < 5%

### 9.3 测试验收

- [ ] BlockManipulator单元测试覆盖率 > 90%
- [ ] DominatorTree单元测试覆盖率 > 90%
- [ ] EnhancedTailRecursionOptimizer集成测试通过
- [ ] 回归测试全部通过（727个测试）

---

**文档版本**: 1.0
**创建日期**: 2026-01-22
**作者**: Sisyphus AI Agent
**状态**: 技术设计完成，待实施
