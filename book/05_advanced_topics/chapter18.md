# 第18章：全局优化技术

## 本章概述

本章聚焦于全局优化技术，它是编译器流水线中的高级优化阶段。通过学习本章，你将掌握尾递归优化、循环不变代码外提和常量传播等核心技术，这些是代码生成前提升程序性能的关键手段。

【你现在站在哪】:
... → [SSA转换] → [数据流分析] → ✅ [全局优化技术] → [代码生成] → [虚拟机执行] → ...

## 动机与真实场景

真实场景：想象你是一名性能工程师，正在优化一个关键算法库。其中一个核心的递归算法（如计算斐波那契数列）在处理大规模数据时频繁崩溃，抛出 `StackOverflowError`。同时，另一个包含循环的数值计算函数运行缓慢， profiler 显示大量重复的计算开销。

具体问题：
- 递归深度问题：fib(50) 直接栈溢出
- 循环效率低下：循环内部存在大量不变计算
- 代码冗余：死代码和不必要的计算占用资源

如果缺少本章的能力，你将面临：
- 无法避免递归导致的栈溢出
- 循环中的不变代码重复执行
- 无法通过编译器自动消除死代码

本章将教你如何：
- 自动检测并优化尾递归调用
- 识别并外提循环不变代码
- 追踪常量值传播，消除冗余计算

## 人类工程师线：技术与实现

### 核心概念

#### 尾递归优化

通俗解释：尾递归优化（TRO）将尾递归调用转换为循环，避免递归调用带来的栈空间消耗。

[图1：尾递归 vs 非尾递归]
```
尾递归（可优化）：
int factorial_tr(int n, int acc) {
    if (n <= 1) return acc;
    return factorial_tr(n - 1, n * acc);  // 尾调用
}

非尾递归（不可优化）：
int fibonacci(int n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2);  // 不是尾调用
}
```

#### 循环不变代码外提

通俗解释：循环不变代码外提（LICM）将循环体内不依赖于循环变量的计算移到循环外。

[图2：循环不变代码外提]
```
优化前：
for (int i = 0; i < n; i++) {
    sum = sum + z + i;  // z 在循环中不变
}

优化后：
int z_copy = z;  // 外提到循环前
for (int i = 0; i < n; i++) {
    sum = sum + z_copy + i;
}
```

#### 常量传播

通俗解释：常量传播追踪程序中变量的值，用常量值替换变量引用。

```
原始代码：
int x = 10;
int y = 20;
int z = x + y;
int result = z * 2;

常量传播后：
int x = 10;
int y = 20;
int z = 30;
int result = 60;
```

### 与仓库 EP 的对应关系

对应 EP：EP21

目录结构：
```
ep21/
├── src/main/java/org/teachfx/antlr4/ep21/
│   ├── pass/cfg/
│   │   ├── TailRecursionOptimizer.java     // 尾递归优化器
│   │   ├── ConstantFoldingOptimizer.java    // 常量折叠优化器
│   │   ├── DeadCodeEliminationOptimizer.java // 死代码消除优化器
│   │   └── IFlowOptimizer.java             // 优化器接口
│   ├── analysis/dataflow/
│   │   ├── LoopAnalysis.java               // 循环分析器
│   │   ├── ConditionConstantPropagation.java // 条件常量传播
│   │   └── NaturalLoop.java                // 自然循环表示
```

关键类/方法说明：

**TailRecursionOptimizer** - 尾递归优化器
```java
public class TailRecursionOptimizer implements IFlowOptimizer<IRNode> {
    
    private int functionsOptimized = 0;
    private int tailCallsDetected = 0;
    private Set<String> optimizedFunctions = new HashSet<>();

    @Override
    public void onHandle(CFG<IRNode> cfg) {
        functionsOptimized = 0;
        tailCallsDetected = 0;
        optimizedFunctions = new HashSet<>();

        List<BasicBlock<IRNode>> functionEntries = collectFunctionEntries(cfg);

        for (BasicBlock<IRNode> entryBlock : functionEntries) {
            optimizeFunction(entryBlock);
        }

        logger.info("尾递归优化完成: 优化了 {} 个函数", functionsOptimized);
    }

    /**
     * 检测直接尾递归调用
     */
    private List<TailCallInfo> detectDirectTailCalls() {
        List<TailCallInfo> tailCalls = new ArrayList<>();

        for (BasicBlock<IRNode> block : currentCFG) {
            List<IRNode> instructions = block.getIRNodes().collect(Collectors.toList());

            for (int i = instructions.size() - 1; i >= 0; i--) {
                IRNode node = instructions.get(i);

                if (node instanceof ReturnVal returnVal) {
                    if (i > 0 && instructions.get(i - 1) instanceof CallFunc call) {
                        if (call.getFuncName().equals(currentFunctionName)) {
                            tailCalls.add(new TailCallInfo(block, returnVal, call, i));
                        }
                    }
                    break;
                }
            }
        }

        return tailCalls;
    }
}
```

**LoopAnalysis** - 循环分析器
```java
public class LoopAnalysis<I extends IRNode> {
    
    private final List<NaturalLoop<I>> loops;
    private final Map<Integer, Set<Integer>> backEdges;
    private final Map<Integer, Set<Integer>> dominance;

    /**
     * 执行循环分析
     */
    public void analyze(CFG<I> cfg) {
        computeDominance(cfg);
        identifyBackEdges(cfg);
        buildNaturalLoops(cfg);
    }

    /**
     * 计算支配关系
     */
    private void computeDominance(CFG<I> cfg) {
        Set<Integer> allNodes = new HashSet<>();
        for (BasicBlock<I> block : cfg) {
            allNodes.add(block.getId());
        }

        BasicBlock<I> entry = cfg.getBlock(0);
        Set<Integer> entryDom = new HashSet<>();
        entryDom.add(entry.getId());
        dominance.put(entry.getId(), entryDom);

        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock<I> block : cfg) {
                int nodeId = block.getId();
                if (nodeId == 0) continue;

                Set<Integer> newDom = new HashSet<>(allNodes);
                Set<Integer> predDom = null;
                for (Integer predId : cfg.getFrontier(nodeId)) {
                    if (predDom == null) {
                        predDom = new HashSet<>(dominance.getOrDefault(predId, allNodes));
                    } else {
                        predDom.retainAll(dominance.getOrDefault(predId, allNodes));
                    }
                }

                if (predDom != null) {
                    predDom.add(nodeId);
                    Set<Integer> oldDom = dominance.getOrDefault(nodeId, new HashSet<>());
                    if (!predDom.equals(oldDom)) {
                        dominance.put(nodeId, predDom);
                        changed = true;
                    }
                }
            }
        }

        computeImmediateDominators(cfg);
    }

    /**
     * 识别回边
     * 回边定义：边 n -> d，当且仅当 d 支配 n
     */
    private void identifyBackEdges(CFG<I> cfg) {
        for (BasicBlock<I> block : cfg) {
            int sourceId = block.getId();
            for (Integer succId : cfg.getSucceed(sourceId)) {
                if (dominates(succId, sourceId)) {
                    backEdges.computeIfAbsent(sourceId, k -> new HashSet<>()).add(succId);
                }
            }
        }
    }
}
```

**ConditionConstantPropagation** - 条件常量传播
```java
public class ConditionConstantPropagation extends AbstractDataFlowAnalysis<Map<VarSlot, LatticeValue>, IRNode> {

    public sealed interface LatticeValue permits UNDEF, KnownConstant, UNKNOWN {
        boolean isConstant();
        ConstVal<?> getConstant();
    }

    public record UNDEF() implements LatticeValue { }
    public record KnownConstant(ConstVal<?> value) implements LatticeValue { }
    public record UNKNOWN() implements LatticeValue { }

    @Override
    public Map<VarSlot, LatticeValue> meet(Map<VarSlot, LatticeValue> a, Map<VarSlot, LatticeValue> b) {
        Map<VarSlot, LatticeValue> result = new HashMap<>();

        Set<VarSlot> allVars = new HashSet<>();
        allVars.addAll(a.keySet());
        allVars.addAll(b.keySet());

        for (VarSlot var : allVars) {
            LatticeValue valA = a.getOrDefault(var, new UNDEF());
            LatticeValue valB = b.getOrDefault(var, new UNDEF());
            result.put(var, meetSingle(valA, valB));
        }

        return result;
    }

    @Override
    public Map<VarSlot, LatticeValue> transfer(IRNode instr, Map<VarSlot, LatticeValue> input) {
        Map<VarSlot, LatticeValue> result = new HashMap<>(input);

        if (instr instanceof Assign assign) {
            VarSlot target = assign.getLhs();
            Expr rhs = assign.getRhs();

            if (rhs instanceof ConstVal<?> constVal) {
                result.put(target, new KnownConstant(constVal));
            } else if (rhs instanceof VarSlot varSlot) {
                LatticeValue srcVal = input.getOrDefault(varSlot, new UNDEF());
                if (srcVal instanceof KnownConstant known) {
                    result.put(target, known);
                } else if (srcVal instanceof UNKNOWN) {
                    result.put(target, new UNKNOWN());
                }
            }
        }

        return result;
    }

    public boolean isConstant(VarSlot var, int blockId) {
        Map<VarSlot, LatticeValue> in = getIn(blockId);
        LatticeValue val = in.getOrDefault(var, new UNDEF());
        return val instanceof KnownConstant;
    }

    @Override
    public Map<VarSlot, LatticeValue> getInitialValue() {
        return new HashMap<>();
    }

    @Override
    public boolean isForward() {
        return true;
    }
}
```

### 实战流程

```bash
# 运行尾递归优化测试
mvn test -Dtest=TailRecursionOptimizerTest

# 预期输出：
# [TailRecursionOptimizer] 开始尾递归优化...
# [TailRecursionOptimizer] 检测到Fibonacci模式: fib
# [TailRecursionOptimizer] 优化完成: 优化了 1 个函数
# [INFO] Tests run: 5, Failures: 0, Errors: 0
```

## AI 协作线

### 上下文设计

**源码文件**（按阅读顺序）：
1. `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/TailRecursionOptimizer.java`
2. `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/LoopAnalysis.java`
3. `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/ConditionConstantPropagation.java`
4. `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/IFlowOptimizer.java`

### Prompt 模板

**类型 A：尾递归优化实现**
```
请为 EP21 编译器实现尾递归优化（Tail Recursion Optimization）。

任务目标：
- 检测尾递归调用
- 将尾递归转换为循环
- 确保语义等价性

具体要求：
1. 实现尾递归优化器
   - 创建 `TailRecursionOptimizer` 类
   - 实现 `IFlowOptimizer<IRNode>` 接口

2. 实现检测算法
   - 识别尾调用位置
   - 检测是否为递归调用
   - 识别 Fibonacci 模式

3. 添加单元测试
   - 测试简单尾递归
   - 测试 Fibonacci 模式
   - 测试非尾递归情况
```

## 练习题

### 练习1：手工实现尾递归检测

**难度**：⭐⭐☆☆☆

**任务**：手工实现一个简单的尾递归检测器。

### 练习2：AI辅助实现循环不变代码外提

**难度**：⭐⭐⭐☆☆

**任务**：使用AI辅助实现循环不变代码外提优化器。

### 练习3：实现常量折叠优化器

**难度**：⭐⭐⭐⭐☆

**任务**：实现完整的常量折叠优化器。

## 本章小结

通过本章的学习，你已经掌握了：

1. **全局优化技术核心概念**
   - 尾递归优化的原理和应用场景
   - 循环不变代码外提的检测和转换方法
   - 常量传播的数据流分析框架

2. **关键技术实现**
   - 使用支配关系识别循环
   - 格理论在常量传播中的应用
   - 基础优化 Pass 的实现

3. **实践技能**
   - 使用数据流分析框架
   - 优化 Pass 的测试和验证方法
   - 与 AI 协作设计上下文和 Prompt

**下一章预告**：第19章将聚焦于优化器架构与跨模块集成，学习如何组织多个优化 Pass 实现可扩展的优化系统。

【你现在站在】:
```
... → [SSA转换] → [数据流分析] → ✅ [全局优化技术] → [优化器架构] → [代码生成] → ...
```

继续加油！全局优化是提升程序性能的关键！
