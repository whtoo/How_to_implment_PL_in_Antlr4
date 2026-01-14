# 循环优化技术

## 📋 文档目的

本文档详细说明循环优化的理论基础、识别算法、优化技术和实现指南，为EP21模块提供完整的循环优化技术参考和实现规范。

---

## 🎯 循环概述

### 定义

**循环（Loop）**：程序中重复执行的一组指令序列。

**优化目标**：
1. **减少循环开销**：减少循环控制指令（跳转、比较等）的执行次数
2. **提高数据局部性**：使数据在循环内局部化，提高缓存命中率
3. **增加迭代机会**：通过循环变换，使其他优化更容易应用

### 循环类型

| 类型 | 特征 | 典型优化技术 |
|------|--------|--------------|---------------------|
| **计数循环**（Counted Loop） | 明确的循环次数、固定迭代次数 | 循环展开、不变代码外提 |
| **while循环**（While Loop） | 基于条件判断的循环 | 循环展开、条件优化 |
| **for循环**（For Loop） | 基于计数器/迭代器的循环 | 归纳变量、强度削减 |
| **自然循环**（Natural Loop） | 无显式计数器的循环 | 归纳分析、递归优化 |

---

## 🔧 循环识别算法

### 基础概念

#### 自然循环（Natural Loop）

**定义**：从CFG中识别回边（Back Edge），形成循环体。

**识别算法**：
```
输入: CFG
输出: Set<NaturalLoop>

算法:
    1. 使用深度优先搜索（DFS）识别所有回边
    2. 每找到一条回边，记录当前节点为循环头
    3. 使用支配分析验证循环结构
    4. 消除回边，避免重复识别

伪代码:
    function findNaturalLoops(cfg):
        loops = []
        visited = []
        
        # DFS遍历
        for each node in cfg.nodes:
            if node not in visited:
                for each successor in cfg.getSucceed(node):
                    if successor is a back edge to node:
                        # 找到循环入口
                        if node not already in loops:
                            # 创建新循环
                            loop = new NaturalLoop()
                            # BFS遍历构建循环体
                            loop.body = buildLoopBody(node, successor)
                            # 添加回边检测
                            loop.addBackEdges(successor)
                            loops.add(loop)
                            break
                visited.add(node)
        return loops
    
    # 辅助函数
    function buildLoopBody(head, start_node):
        body = []
        queue = [start_node]
        while queue not empty:
            current = queue.pop()
            body.add(current)
            for each successor in cfg.getSucceed(current):
                if not hasBackEdge(current, successor):
                    queue.add(successor)
        return body
```

#### 嵌套循环（Nested Loop）

**定义**：一个循环完全包含在另一个循环内。

**识别方法**：
- 使用支配关系检测嵌套
- 如果循环A的头节点被循环B支配，则A嵌套在B中
- 分析支配深度来确定嵌套层级

**当前实现**：
```java
// 位于：ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/LoopAnalysis.java
public class LoopAnalysis {
    
    private CFG<I> cfg;
    private DominatorAnalysis<I> dominatorAnalysis;
    
    public List<NaturalLoop> analyze(CFG<I> cfg) {
        this.cfg = cfg;
        this.dominatorAnalysis = new DominatorAnalysis<>(cfg);
        this.dominatorAnalysis.analyze();
        
        List<NaturalLoop> loops = new ArrayList<>();
        Set<Integer> loopHeaders = identifyLoopHeaders();
        
        for (int headerId : loopHeaders) {
            NaturalLoop loop = new NaturalLoop();
            loop.setHeaderId(headerId);
            loop.setBodyNodes(collectLoopBody(headerId));
            loop.setNestingLevel(calculateNestingLevel(headerId));
            loops.add(loop);
        }
        
        return loops;
    }
    
    private boolean isLoopHeader(int blockId) {
        Set<Integer> successors = cfg.getSucceed(blockId);
        return successors.stream().anyMatch(s -> {
            Set<Integer> sDoms = dominatorAnalysis.getStrictDominators(blockId);
            // 判断：如果s的所有严格支配者中，没有一个是blockId的后继
            // 则blockId是循环头
            return sDoms.stream().allMatch(d -> {
                Set<Integer> dSucc = dominatorAnalysis.getStrictDominators(d);
                return dSucc.stream().anyMatch(succ -> {
                    Set<Integer> dSuccDoms = dominatorAnalysis.getStrictDominators(succ);
                    return !dSuccDoms.isEmpty();
                });
            });
        });
    }
    
    private Set<Integer> collectLoopBody(int headerId) {
        Set<Integer> body = new HashSet<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(headerId);
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (visited.contains(current)) continue;
            if (isLoopHeader(current)) continue;  // 跳过其他循环头
            body.add(current);
            visited.add(current);
            
            Set<Integer> successors = cfg.getSucceed(current);
            for (int succ : successors) {
                if (!visited.contains(succ) && !isLoopHeader(succ)) {
                    queue.add(succ);
                }
            }
        }
        
        return body;
    }
    
    private int calculateNestingLevel(int headerId) {
        Set<Integer> dominators = dominatorAnalysis.getStrictDominators(headerId);
        int level = 0;
        
        for (int d : dominators) {
            Set<Integer> dDoms = dominatorAnalysis.getStrictDominators(d);
            if (dDoms.stream().anyMatch(dDom -> {
                Set<Integer> dDomDoms = dominatorAnalysis.getStrictDominators(d);
                return !dDomDoms.isEmpty();
            })) {
                level = Math.max(level, calculateNestingLevel(d));
            }
        }
        
        return level;
    }
}
```

### 归纳变量（Induction Variable）

#### 定义

**归纳变量（Induction Variable）**：循环中使用的一个变量，其值在每次迭代中按照可预测的模式更新。

#### 基本归纳变量（Basic Induction Variable）

**示例**：
```c
for (int i = 0; i < n; i++) {
    sum = sum + i;
}

// 归纳变量：i（计数器）
// 关系：sum = i * (i + 1) / 2
```

#### 归纳变量检测算法

**识别方法**：
1. **简单循环模式匹配**：检测 `for i = 0; i < n; i++`
2. **循环不变量分析**：识别在循环内保持不变的变量
3. **形式化验证**：验证归纳关系的正确性

---

## 🚀 循环优化技术

### 1. 循环展开（Loop Unrolling）

#### 基本展开（Basic Unrolling）

**原理**：将循环体复制多次，减少循环控制开销。

**示例**：
```c
// 展开前
for (int i = 0; i < 10; i++) {
    result[i] = compute();
}

// 展开后（4次）
for (int i = 0; i < 10; i += 4) {
    result[i] = compute();
    result[i + 1] = compute();
    result[i + 2] = compute();
    result[i + 3] = compute();
}
```

**优势**：
- ✅ 减少循环控制指令（减少75%的循环跳转）
- ✅ 增加指令级并行性
- ✅ 提高指令流水线效率

**劣势**：
- ❌ 代码大小增加
- ❌ 寄存器压力增加
- ❌ 指令缓存未命中

#### 部分展开（Partial Unrolling）

**策略**：不完全展开，保留循环控制逻辑。

**适用场景**：
- 循环次数可变时
- 循环体较大时

### 2. 循环不变代码外提（Loop-Invariant Code Motion）

#### 原理

**循环不变量（Loop Invariant）**：在循环体内值不改变的变量。

**示例**：
```c
int example(int n, int m) {
    int result = 0;
    for (int i = 0; i < n; i++) {
        result += m;  // m是循环不变量
    }
    return result;
}
```

#### 识别算法

**步骤**：
1. 分析数据流，识别不变量
2. 验证在循环前驱中的值
3. 在循环前基本块中计算不变量

**不变量类型**：
| 类型 | 说明 | 示例 |
|------|--------|------|--------|
| **算术不变量** | 表达式中的计算 | `x + y` 在循环内恒定 |
| **条件不变量** | 条件表达式的真值 | `if (x > 0)` 在循环内恒定 |
| **数组边界不变量** | 数组访问模式 | `a[i] < limit` 在循环内恒定 |

### 3. 强度削减（Strength Reduction）

#### 定义

**强度削减**：用更快的操作替换昂贵的操作。

#### 常见模式

| 昂贵操作 | 优化为 | 加速比 |
|-----------|--------|----------|---------|
| **乘法** | 移位/加法 | `x * 2` → `x << 1` | ~2x-10x |
| **除法** | 移位/乘法 | `x / 8` → `x >> 3` | ~3x-5x |
| **模运算** | 使用位运算 | `x % 8` → `x & 7` | ~3x-2x |

#### 实现示例

```java
// StrengthReductionOptimizer.java
public class StrengthReductionOptimizer implements IFlowOptimizer<IRNode> {
    
    @Override
    public BasicBlock<I> optimize(BasicBlock<I> block) {
        BasicBlock<I> optimized = new BasicBlock<>();
        
        for (IRNode instr : block.getInstructions()) {
            if (instr instanceof BinaryOp binaryOp) {
                IRNode optimized = reduceStrength(binaryOp);
                optimized.add(optimized);
            } else {
                optimized.add(instr);
            }
        }
        
        return optimized;
    }
    
    private IRNode reduceStrength(BinaryOp binaryOp) {
        String op = binaryOp.getOperator();
        
        // 乘2的幂优化
        if (isPowerOfTwo(binaryOp.getLeft())) {
            return optimizePowerOfTwo(binaryOp);
        }
        
        // 乘法转移位
        if (op.equals("*") && isConstant(binaryOp.getRight())) {
            return new BinaryOp(binaryOp.getLeft(), "<<", getShiftAmount(binaryOp.getRight()));
        }
        
        // 其他优化...
    }
    
    private int getShiftAmount(IRNode node) {
        // 解析常量值，计算移位位数
        // 返回移位位数
        return calculateShiftAmount(node);
    }
}
```

### 4. 循环分块和融合（Loop Fission）

#### 原理

**循环分块（Loop Fission）**：将一个大循环分解为多个小循环，提高缓存局部性。

#### 分块策略

| 策略 | 适用场景 | 预期效果 |
|--------|--------|----------|---------|
| **按迭代分块** | 固定迭代次数 | 减少循环体大小 |
| **按依赖分块** | 分析数据依赖 | 避免伪依赖 |
| **按数据范围分块** | 数组/内存访问 | 提高缓存命中率 |

---

## 📊 循环优化实现

### 当前EP21状态

| 功能 | 状态 | 位置 | 说明 |
|------|--------|--------|------|----------|
| **循环识别** | ✅ 已实现 | `LoopAnalysis.java` | 支持自然循环、嵌套检测、归纳变量识别 |
| **自然循环结构分析** | ⏸ 未实现 | - | 需要增强归纳变量分析 |
| **循环展开** | ⏸ 未实现 | - | 可作为新优化Pass添加 |
| **不变代码外提** | ⏸ 未实现 | - | 可作为新优化Pass添加 |
| **强度削减** | ⏸ 未实现 | - | 可作为新优化Pass添加 |
| **循环分块和融合** | ⏸ 未实现 | - | 可作为未来优化 |

### 实现架构

```
循环优化Pass管理器
    LoopAnalysis
    ├── LoopRecognition     // 循环识别
    ├── InvariantAnalysis      // 不变量分析
    └── InductionVariableAnalysis  // 归纳变量
    LoopOptimizer
    ├── LoopUnroller        // 循环展开
    ├── InvariantMotion      // 不变量代码外提
    ├── StrengthReducer      // 强度削减
    └── LoopFission        // 循环分块和融合
```

---

## 📚 参考资源

### 学术论文

| 论文 | 作者 | 年份 | 主题 | 链接 |
|------|--------|--------|--------|--------|----------|
| Optimizing for Loops and Parallelism | Wolfe | 1982 | 循环展开、并行化 | [PDF](https://dl.acm.org/doi/10.1145/5938.1982.236) |
| Compiler Optimations | Muchnick | 1997 | 循环优化综合 | [Book](https://www.amazon.com/Compiler-Optimizations-Reference/Muchnick/dp/0672372360) |
| Loop Optimations | Allen & Kennedy | 2001 | 循环优化理论基础 | [Book](https://www.amazon.com/Compilers-Principles-Techniques-Tools/Loop-Otimizations/dp/02X2001) |
| Induction Variable Analysis | Carruth & Zaragora | 1999 | 归纳变量分析 | [PDF](https://dl.acm.org/doi/10.1145/35408.2000) |

### 开源实现

| 项目 | 组件 | 链接 | 核心贡献 |
|------|--------|--------|--------|----------|
| **LLVM** | LoopUnrollPass, LoopVectorize | [GitHub](https://github.com/llvm/llvm-project/blob/main/llvm/Transforms/Scalar/LoopUnrollPass.cpp) | 现代循环展开实现 |
| **GCC** | -loop-distribute | [GitHub](https://github.com/gcc-mirror/gcc/blob/master/gcc/tree-ssa/loop-distribute.c) | 循环分块 |
| **JIT** | HotSpot | LoopOpts | [OpenJDK](https://github.com/openjdk/jdk/blob/master/hotspot/src/share/vm/interpreter/bytecodeInterpreter.cpp) | JIT编译器循环优化 |

---

## 🎯 最佳实践

### 1. 循环分析优先级

**高优先级**：
1. **自然循环识别**：必须正确识别所有循环结构
2. **嵌套循环检测**：准确确定嵌套关系和深度
3. **归纳变量分析**：支持简单归纳变量和复杂归纳模式

**中优先级**：
1. **循环展开决策**：根据循环特征决定是否展开
2. **不变量外提**：保证正确性和收益

**低优先级**：
1. **循环分块**：复杂优化，需要谨慎应用
2. **强度削减**：需要全面测试，避免引入bug

### 2. 优化Pass设计

**接口设计**：
```java
public interface ILoopOptimizer {
    /**
     * 优化包含循环的代码块
     * @param block 要优化的基本块
     * @return 优化后的基本块
     */
    BasicBlock<I> optimize(BasicBlock<I> block);
}

public abstract class AbstractLoopOptimizer implements ILoopOptimizer {
    
    protected LoopAnalysis loopAnalysis;
    protected DominatorAnalysis dominatorAnalysis;
    
    /**
     * 检查是否是循环头
     */
    protected boolean isLoopHeader(int blockId) {
        Set<Integer> successors = getCFG().getSucceed(blockId);
        if (successors == null) return false;
        
        Set<Integer> strictDoms = getDominatorAnalysis().getStrictDominators(blockId);
        return strictDoms.stream().noneMatch(d -> {
            Set<Integer> dSucc = getDominatorAnalysis().getStrictDominators(d);
            return !dSuccDoms.isEmpty();
        });
    }
}
```

### 3. 调试技巧

1. **循环结构可视化**：使用CFG.toDOT()查看循环结构
2. **归纳变量追踪**：打印每次迭代中归纳变量的值变化
3. **优化效果验证**：对比优化前后的循环次数

---

## 📝 后续优化方向

### 短期（1-2个月）

1. **完成归纳变量分析**：支持复杂的归纳模式
2. **实现循环展开Pass**：支持部分展开和完全展开
3. **实现不变代码外提**：基本的循环不变量外提

### 中期（3-4个月）

1. **实现强度削减**：完成所有常见模式的优化
2. **添加循环分块优化**：支持按依赖和数据范围分块
3. **与SSA集成**：利用SSA形式优化循环

### 长期（5-6个月）

1. **循环分块和融合**：高级循环优化
2. **并行化优化**：利用现代CPU多核特性
3. **机器学习优化**：基于性能数据自动选择优化策略

---

**文档版本**: 1.0-草稿
**创建日期**: 2026-01-14
**适用范围**: EP21模块循环优化
**维护者**: EP21模块维护团队
**审核要求**: 需要补充归纳变量分析算法、循环展开决策逻辑、优化效果评估方法
