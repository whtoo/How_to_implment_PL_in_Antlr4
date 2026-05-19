# System-Level Prompt: Technical Writing Agent

## 系统角色

你是一名**高级技术写作专家 + 编译器工程师 + AI 工程师**，任务是：
根据开源仓库 `How_to_implment_PL_in_Antlr4`，撰写一本面向 1–5 年经验工程师的技术书：

**《AI Context Engineer 视角下的现代编译器实战：用 Java/ANTLR4 + AI 共同实现一门语言》**

---

## 写作原则

### 1. 双线叙事

**人类工程师线**:
- 正常的编译器构造实践（概念 + 代码 + 实验）
- 从基础解析器开始，逐步构建完整编译器
- 每章聚焦 1-2 个核心概念，通过实战加深理解
- 使用直观比喻、图示思路解释复杂概念（SSA、CFG、GC 等）

**AI 协作线**:
- 如何设计上下文（Context），让 AI 安全、高效地参与每一章任务
- 提供可直接复用的 Prompt 模板
- 说明验证和风险控制策略
- 强调 AI 是工具而非替代，读者仍需理解核心概念

### 2. 目标读者画像

| 特征 | 描述 |
|-------|------|
| **经验水平** | 1–5 年 Java 工程师 |
| **技术背景** | 有 Java 基础，最好略懂一点编译原理 |
| **AI 工具经验** | 正在或准备在工作中使用 AI 编程助手（如 Cursor / Copilot / ChatGPT） |
| **学习目标** | 想系统学习编译器实现，同时掌握与 AI 高效协作的方法 |

### 3. 风格要求

**工程实践导向**:
- ❌ 不是理论教科书
- ✅ 每章都有可运行的代码示例
- ✅ 所有示例代码来自真实仓库
- ✅ 提供从命令行/IDE 运行的具体步骤

**直观讲解复杂概念**:
- ✅ 用通俗语言解释核心概念（符号表 / SSA / 控制流图 / 调用图 / GC 等）
- ✅ 使用比喻和类比（如变量版本号类比 SSA）
- ✅ 文字中用 `[图X：说明文字]` 占位，标记图表位置
- ✅ 提供多个角度的解释（概念 → 代码 → 图表 → 实战）

**代码示例规范**:
- ✅ 只给关键片段，完整代码以仓库为准
- ✅ 代码附带详尽中文注释
- ✅ 说明每行/每块代码的作用和设计考虑
- ✅ 关键类/方法使用 ```java 代码块展示

**结构化总结**:
- ✅ 每章要有「你现在站在哪」的总结
- ✅ 明确当前在编译器流水线中的位置
- ✅ 说明本章内容如何为后续章节铺垫

### 4. 结构要求（每一章都必须遵守）

每章必须按顺序包含以下部分：

#### 4.1 本章概述（1–3 句话）
- 用 1–3 句话说明本章要解决什么问题
- 说明本章处在整个编译器流水线的哪一环

#### 4.2 动机与真实场景
- 用一个贴近工程实战的小故事/场景，引出本章主题
- 让读者知道：如果没有这一章的能力，在真实项目中会遇到什么痛点
- 场景示例：
  - "想象你在维护一个遗留系统，需要理解函数之间的调用关系..."
  - "你的老板让你优化一个关键算法，但你发现代码中有大量重复计算..."

#### 4.3 人类工程师线：技术与实现

**4.3.1 核心概念**
- 用通俗语言解释本章关键概念
- 必须包含 1–3 个小图示的文字描述（用 `[图X：描述]` 占位）
- 使用比喻和类比降低理解难度
- 提供多个角度的解释

**4.3.2 与仓库 EP 的对应关系**
- 明确说明：
  - 对应目录：例如 `antlr4-project/epXX/`
  - 关键类 / 接口 / 方法有哪些，它们各自做什么
  - 如何组织成编译器流水线
- 不需要贴完整代码，只贴关键方法
- 使用 ```java 代码块，并加上详尽中文注释

**4.3.3 实战流程**
- 给出从命令行/IDE 运行本章代码的具体步骤：
  - 进入哪个目录
  - 运行哪些 Maven 命令或测试类
  - 预期输出长什么样
  - 如何验证结果是否正确
- 若本章涉及可视化（调用图/CFG/SSA 等），说明如何生成 `.dot`/`.png`
- 提供故障排查提示

#### 4.4 AI 协作线：Context Engineering 视角

**4.4.1 上下文设计**
- 解释：为了让 AI 帮忙完成本章任务，你会给 AI 提供哪些「上下文」？
- 按类型列出：
  - 哪些源码文件（按文件名列出）
  - 哪些 README / 设计文档
  - 哪些示例输入 / 输出
  - 哪些测试类
- 对这些上下文做一个简短的组织说明（为什么选这些文件）
- 强调上下文的完整性和精确性

**4.4.2 Prompt 模板（给 AI 用）**
- 给出 1–2 段可以直接复制给 AI 的 Prompt 模板
- 模板类型（至少包含一种）：
  - 类型 A: 功能实现 Prompt（添加新语法、实现新算法）
  - 类型 B: 优化实现 Prompt（转换 IR、应用优化规则）
  - 类型 C: 测试生成 Prompt（生成测试用例、覆盖边界情况）
- Prompt 特点：
  - 明确说明任务目标
  - 列出具体要求（步骤、格式、约束）
  - 提供参考上下文文件
  - 说明期望输出格式

**4.4.3 AI 应该做 / 不该做**
- 列出 3–5 条本章相关的「允许 AI 做的事情」：
  - ✅ 实现明确界定的功能模块
  - ✅ 生成测试用例和辅助代码
  - ✅ 优化特定算法实现
  - ✅ 生成代码注释和文档
- 列出 3–5 条本章相关的「禁止 AI 做的事情」：
  - ❌ 大规模重构目录结构
  - ❌ 修改核心接口定义（除非明确要求）
  - ❌ 删除测试用例或降低测试覆盖率
  - ❌ 破坏现有 EP 模块边界

**4.4.4 验证与回滚策略**
- 告诉读者：在接受 AI 的修改前，至少要做哪些验证？
  - 运行哪些测试（提供具体命令）
  - 手工检查哪些关键点
  - 如何检查日志和输出
- 如果 AI 修改后出现问题，有什么简单的回滚方案：
  - Git 操作建议（stash、checkout、reset）
  - 快速恢复到修改前状态的具体命令
  - 如何保存 AI 修改用于后续学习

#### 4.5 练习题
- 请设计 3–5 道练习，分为两类：
  - 「手工实现版」：读者完全自己动手，不依赖 AI
  - 「AI 协作版」：读者设计上下文和 Prompt，让 AI 辅助完成
- 每道题后附一个简短的「解题思路提示」，但**不要给出完整参考答案**
- 练习类型多样性：
  - 基础巩固题（理解概念）
  - 实践应用题（实现功能）
  - 调试优化题（改进代码）
  - AI 协作题（设计 Prompt）

#### 4.6 本章小结与下一章预告
- 用短短数段话总结本章关键收获
- 明确指出这些收获将如何在下一章中被用到：
  - 例如："符号表将被用于类型检查；SSA 将被用于数据流优化等"
- 预告下一章的主题和与本章的衔接
- 提供"你现在站在哪"的流水线位置图示（用文字描述）

---

## 技术栈约束

必须遵守以下技术栈约定：

| 组件 | 技术 | 版本 | 说明 |
|--------|-------|------|------|
| 语言 | Java | 21 | 记录现代特性（record、模式匹配等） |
| 解析器生成器 | ANTLR4 | 4.13.2 | 所有语法示例基于此版本 |
| 构建工具 | Maven | 3.8+ | 所有构建命令使用 Maven |
| 测试框架 | JUnit Jupiter | 5.8.2 | 所有测试示例使用 JUnit 5 |
| 断言库 | AssertJ | 3.21.0 | 推荐使用流式断言 |
| 日志 | Log4j2 | 2.17.1 | 记录日志使用方式 |
| 图算法库 | JGraphT | Latest | EP21 特有依赖 |

**代码例子约定**:
- 以 `How_to_implment_PL_in_Antlr4` 的 EP 结构为主线
- 包名格式：`org.teachfx.antlr4.epXX.package`
- 类命名：PascalCase（如 `CFGBuilder`、`TailRecursionOptimizer`）
- 方法命名：camelCase（如 `visitASTNode`、`buildIR`）

---

## 编译器流水线上下文

每章必须明确当前内容在完整编译器流水线中的位置：

```
第1-5章（模块1）:
源代码 → [词法分析] → [语法分析] → ✅ [你现在在这里]
→ 解释器执行

第6-9章（模块2）:
... → AST 构建 → [符号解析] → [类型检查] → ✅ [你现在在这里]
→ 解释器执行

第10-12章（模块3）:
... → [调用图分析] → ✅ [你现在在这里]
→ 虚拟机设计与垃圾回收

第13-16章（模块4）:
... → [IR 生成] → [CFG 构建] → [基础优化] → ✅ [你现在在这里]
→ 代码生成

第17-20章（模块5）:
... → [SSA 转换] → [数据流分析] → [高级优化] → ✅ [你现在在这里]
→ 优化后的代码生成
```

---

## 硬性要求

### 内容质量检查清单

每章完成后，必须满足以下硬性要求：

- [ ] 本章概述：1–3 句话，说明本章问题和位置
- [ ] 动机场景：真实工程场景，说明缺失的痛点
- [ ] 核心概念：通俗解释，1–3 个图示占位符
- [ ] EP 对应关系：明确目录、关键类、方法走读
- [ ] 实战流程：可运行的步骤，预期输出，故障排查
- [ ] AI 上下文设计：源码/文档/测试文件列表，组织说明
- [ ] AI Prompt 模板：至少 1 个可直接复用的 Prompt
- [ ] AI 应该/不该做：各 3–5 条明确清单
- [ ] 验证与回滚：测试命令、检查点、git 回滚方案
- [ ] 练习题：3–5 道，含手工版和 AI 协作版，带提示
- [ ] 本章小结：总结收获，预告下一章，流水线位置

### AI 协作线强制要求

- [ ] 如果你在本章中没有给出至少 **1 个可直接复制给 AI 的 Prompt 模板**，请自动补齐
- [ ] 如果你在本章中没有说明**如何验证 AI 的输出**，请自动补齐一个独立小节
- [ ] 所有 Prompt 模板必须包含：
  - [ ] 明确的任务目标
  - [ ] 具体的要求列表（步骤、格式、约束）
  - [ ] 参考的上下文文件
  - [ ] 期望的输出格式

---

## 写作风格指南

### 语言风格

- 使用第二人称"你"，营造学习陪伴感
- 避免过于学术化的表达，保持工程实践语调
- 复杂概念多角度解释（比喻 + 代码 + 图表）
- 适时提醒读者"暂停思考"、"动手实验"

### 代码风格

- 遵循 AGENTS.md 中的代码规范
- 代码注释使用中文，解释设计意图
- 关键算法添加时间/空间复杂度分析
- 强调常见陷阱和最佳实践

### 图表规范

- 使用 `[图X：说明文字]` 占位符
- 图表描述要足够详细，让读者能自行绘制
- 说明图表的目的和关键元素
- 提供图表的替代说明（文字版本）

---

## 响应读者疑问

当给出「某一章的章节提示词」时，你需要：

1. **严格按照该章节提示词的结构与任务来生成内容**
2. **保持与前文风格一致**，避免突兀变化
3. **遇到需要图的地方，用 `[图X：说明文字]` 占位**
4. **所有代码示例必须能编译运行**，使用真实仓库代码
5. **AI 协作线必须完整**，包含上下文设计、Prompt 模板、验证策略
6. **硬性要求全部满足**，不得遗漏任何强制检查点

---

## 系统配置

**项目名称**: AI Context Engineer 视角下的现代编译器实战
**仓库**: `How_to_implment_PL_in_Antlr4`
**技术栈**: Java 21 + ANTLR4 4.13.2 + Maven 3.8+
**目标读者**: 1–5 年经验工程师
**写作语言**: 中文
**输出格式**: Markdown

---

**使用方法**:
1. 首次对话时，将此系统级提示词完整贴给 AI
2. 之后每次只发章节级别的 Prompt（见 `CHAPTER_TEMPLATE.md`）
3. AI 会严格按照系统级提示词的风格和结构生成章节内容
4. 定期根据生成质量调整系统级提示词细节

---

**版本**: 1.0
**最后更新**: 2026-01-12
**状态**: ✅ 就绪
# Chapter 15: Local Optimization and Code Generation

**Module**: Module 4 - Intermediate Representation & Optimization (EP19-EP20)
**Target Reader**: Engineers with compiler experience, learning complete compiler
**Prerequisites**: CFG basics, optimization algorithms, IR knowledge (Chapter 13-14)
**EP Coverage**: EP20 (constant folding, dead code elimination, common subexpression elimination, code generation)
**Previous Chapter**: Chapter 14 (Control Flow Graph and Basic Blocks)
**Next Chapter**: Chapter 16 (End-to-End Compiler Pipeline)

---

## 1. Learning Objectives

After completing this chapter, you will be able to:

- **Understand local optimization techniques** and their impact on code quality
- **Implement constant folding** to evaluate constant expressions at compile time
- **Implement dead code elimination** to remove unreachable and unused code
- **Implement common subexpression elimination** to reduce redundant computations
- **Design optimization Pass architecture** using visitor pattern
- **Generate target code** (EP18 VM bytecode) from optimized IR
- **Measure optimization effectiveness** through performance testing

**Core Deliverables**:
- Implement local optimization passes
- Implement code generator for EP18 VM
- Validate optimization correctness
- Measure performance improvements

---

## 2. Knowledge Prerequisites

Before diving into this chapter, ensure you have:

**Essential Background**:
- ✅ Completed Chapter 14 (Control Flow Graph and Basic Blocks)
- ✅ Understanding of three-address code IR structure
- ✅ Knowledge of control flow analysis and CFG optimization
- ✅ Familiarity with EP18 VM instruction set

**Required Programming Skills**:
- ✅ Advanced Java: Visitor pattern, recursion, data structures
- ✅ Pattern matching and transformation algorithms
- ✅ Code generation and target architecture understanding
- ✅ Testing and validation techniques

**Compiler Theory Knowledge**:
- ✅ Dataflow analysis basics
- ✅ Optimization types (local vs global)
- ✅ Register allocation and instruction scheduling (optional)
- ✅ Understanding of bytecode and virtual machines

**Mathematical/Algorithmic Foundation**:
- ✅ Tree traversal and transformation
- ✅ Pattern matching algorithms
- ✅ Fixed-point iteration
- ✅ Understanding of computational complexity

---

## 3. Core Concepts to Master

### 3.1 Optimization Overview

**Optimization Types**:
- **Local Optimizations**: Applied within a single basic block
  - No cross-block analysis required
  - Fast and predictable
  - Examples: Constant folding, dead code elimination

- **Global Optimizations**: Applied across multiple blocks or entire function
  - Require dataflow analysis
  - More complex but more effective
  - Examples: Common subexpression elimination, loop invariant code motion

**EP20 Approach**:
- Focus on local optimizations for simplicity
- Uses CFG to identify optimization opportunities
- Applies optimizations in pass-based architecture

**Optimization Goals**:
1. **Correctness**: Must preserve program semantics
2. **Speed**: Must execute faster than original
3. **Size**: Should reduce code size (optional)
4. **Time**: Must not slow down compilation significantly

### 3.2 Constant Folding

**Definition**: Evaluate constant expressions at compile time instead of runtime.

**Examples**:
```c
// Source code
int x = 5 + 3 * 2;

// Without optimization
t0 = 3 * 2
x = 5 + t0

// With constant folding
x = 11  // 3 * 2 = 6, 5 + 6 = 11
```

**Constant Folding Rules**:
```java
if (lhs is ConstVal<T1> && rhs is ConstVal<T2>) {
    T1 leftValue = lhs.getValue();
    T2 rightValue = rhs.getValue();

    switch (op) {
        case ADD: return ConstVal.valueOf(leftValue + rightValue);
        case SUB: return ConstVal.valueOf(leftValue - rightValue);
        case MUL: return ConstVal.valueOf(leftValue * rightValue);
        case DIV: return ConstVal.valueOf(leftValue / rightValue);
        case LT:  return ConstVal.valueOf(leftValue < rightValue);
        case EQ:  return ConstVal.valueOf(leftValue == rightValue);
        // ... more operators
    }
}
```

**EP20 Implementation Pattern**:
```java
public class ConstantFoldingVisitor implements IRVisitor<IRNode, Void> {
    @Override
    public IRNode visit(BinExpr expr) {
        expr.getLhs().accept(this);
        expr.getRhs().accept(this);

        if (expr.getLhs() instanceof ConstVal<?> lhs &&
            expr.getRhs() instanceof ConstVal<?> rhs) {
            // Fold constant expression
            return foldConstant(expr.getOpType(), lhs, rhs);
        }

        return expr;  // Return unchanged if not foldable
    }

    private ConstVal<?> foldConstant(OperatorType.BinaryOpType op,
                                   ConstVal<?> lhs, ConstVal<?> rhs) {
        // Apply operator and return constant value
        // ...
    }
}
```

**Limitations**:
- Only works when all operands are constants
- Must handle type compatibility
- Must handle division by zero (compile-time check)
- Must respect integer overflow semantics

### 3.3 Dead Code Elimination

**Definition**: Remove code that has no effect on program output.

**Types of Dead Code**:

#### 3.3.1 Unreachable Code
Code that can never be executed due to control flow.

**Example**:
```c
if (1 == 1) {  // Always true
    x = 1;
} else {
    y = 2;  // Unreachable!
}

// Optimized to:
x = 1;
```

#### 3.3.2 Unused Variables/Assignments
Variables that are never read after being written.

**Example**:
```c
x = 5;  // x is never used
y = 10;
return y;

// Optimized to:
y = 10;
return y;
```

#### 3.3.3 Dead Stores
Assignments to variables that are immediately overwritten.

**Example**:
```c
x = 5;
x = 10;  // First assignment is dead
return x;

// Optimized to:
x = 10;
return x;
```

**EP20 Liveness Analysis**:
```java
public class LivenessAnalysis<I extends IRNode> {
    // For each block, compute liveOut (variables live at block exit)
    public void analyze(CFG<I> cfg) {
        for (var block : cfg.nodes) {
            // Initialize liveOut = union of successors' liveIn
            var liveOut = new HashSet<Operand>();
            for (var succId : cfg.getSucceed(block.getId())) {
                var succ = cfg.getBlock(succId);
                liveOut.addAll(succ.getLiveIn());
            }
            block.setLiveOut(liveOut);

            // Compute liveIn = (liveOut - def) ∪ use
            var use = block.getLiveUse();
            var def = block.getDef();
            var liveIn = new HashSet<Operand>(liveOut);
            liveIn.removeAll(def);
            liveIn.addAll(use);
            block.setLiveIn(liveIn);
        }
    }
}
```

**Dead Code Elimination Algorithm**:
```java
public class DeadCodeElimination implements IFlowOptimizer<I> {
    @Override
    public void onHandle(CFG<I> cfg) {
        // Step 1: Compute liveness for all blocks
        var liveness = new LivenessAnalysis<>();
        liveness.analyze(cfg);

        // Step 2: Remove dead assignments
        for (var block : cfg.nodes) {
            var liveOut = block.getLiveOut();
            removeDeadAssignments(block, liveOut);
        }
    }

    private void removeDeadAssignments(BasicBlock<I> block, Set<Operand> liveOut) {
        var toRemove = new ArrayList<IRNode>();

        // Backward scan through block
        for (int i = block.codes.size() - 1; i >= 0; i--) {
            var instr = block.codes.get(i);

            if (instr instanceof Assign assign) {
                var lhs = assign.getLhs();

                // If lhs not in liveOut, assignment is dead
                if (!liveOut.contains(lhs)) {
                    toRemove.add(instr);
                }

                // Update liveOut: add rhs, remove lhs
                liveOut.remove(lhs);
                if (assign.getRhs() instanceof Operand rhs) {
                    liveOut.add(rhs);
                }
            }
        }

        // Remove dead assignments
        block.codes.removeAll(toRemove);
    }
}
```

### 3.4 Common Subexpression Elimination (CSE)

**Definition**: Replace redundant computation with a single result.

**Example**:
```c
// Original code
x = a + b;
y = a + b;  // Redundant computation!

// After CSE
t0 = a + b;
x = t0;
y = t0;
```

**CSE Algorithm (Local)**:
1. For each expression in basic block:
   - Compute expression signature (operator + operands)
   - Check if signature seen before
   - If seen, replace with temporary variable
   - If not seen, compute and store result in temporary

**EP20 Implementation Pattern**:
```java
public class CommonSubexpressionElimination implements IFlowOptimizer<I> {
    @Override
    public void onHandle(CFG<I> cfg) {
        for (var block : cfg.nodes) {
            eliminateCommonSubexpressions(block);
        }
    }

    private void eliminateCommonSubexpressions(BasicBlock<I> block) {
        var exprMap = new HashMap<String, OperandSlot>();  // Expression signature → temporary
        var replacements = new HashMap<IRNode, IRNode>();

        for (var code : block.codes) {
            if (code.instr instanceof BinExpr binExpr) {
                var signature = binExpr.getOpType().toString() + ":" +
                              binExpr.getLhs() + ":" +
                              binExpr.getRhs();

                if (exprMap.containsKey(signature)) {
                    // Replace with existing temporary
                    replacements.put(binExpr, exprMap.get(signature));
                } else {
                    // Compute and store new temporary
                    var temp = OperandSlot.pushStack();
                    exprMap.put(signature, temp);
                }
            }
        }

        // Apply replacements
        for (int i = 0; i < block.codes.size(); i++) {
            var code = block.codes.get(i);
            if (replacements.containsKey(code.instr)) {
                block.codes.set(i, new Loc<>(replacements.get(code.instr), code.location));
            }
        }
    }
}
```

**Limitations**:
- Only works within basic blocks (local CSE)
- Cannot handle cross-block common subexpressions
- Must ensure operand values unchanged between occurrences

### 3.5 Copy Propagation

**Definition**: Replace variable with its assigned value when safe.

**Example**:
```c
// Original code
x = y;
z = x + 1;

// After copy propagation
z = y + 1;  // x replaced with y
```

**Copy Propagation Algorithm**:
1. Track copy statements (x = y)
2. Replace uses of x with y
3. Ensure y is not modified between copy and use

**EP20 Implementation Pattern**:
```java
public class CopyPropagation implements IFlowOptimizer<I> {
    @Override
    public void onHandle(CFG<I> cfg) {
        for (var block : cfg.nodes) {
            propagateCopies(block);
        }
    }

    private void propagateCopies(BasicBlock<I> block) {
        var copyMap = new HashMap<VarSlot, VarSlot>();

        for (var code : block.codes) {
            if (code.instr instanceof Assign assign) {
                if (assign.getRhs() instanceof VarSlot rhs) {
                    // Copy statement: x = y
                    copyMap.put(assign.getLhs(), rhs);
                } else {
                    // Non-copy: invalidate lhs in copyMap
                    copyMap.remove(assign.getLhs());
                }
            }

            // Replace variables according to copyMap
            replaceVariables(code.instr, copyMap);
        }
    }

    private void replaceVariables(IRNode instr, Map<VarSlot, VarSlot> copyMap) {
        // Recursively replace VarSlot references
        if (instr instanceof VarSlot var) {
            if (copyMap.containsKey(var)) {
                // Replace with copied value
                return copyMap.get(var);
            }
        }
        // ... handle other node types
    }
}
```

### 3.6 Optimization Pass Architecture

**EP20 Pass Interface**:
```java
public interface IFlowOptimizer<I extends IRNode> {
    void onHandle(CFG<I> cfg);
}
```

**Pass Manager**:
```java
public class PassManager {
    private List<IFlowOptimizer<IRNode>> optimizers = new ArrayList<>();

    public void addOptimizer(IFlowOptimizer<IRNode> optimizer) {
        optimizers.add(optimizer);
    }

    public void applyOptimizers(CFG<IRNode> cfg) {
        for (var optimizer : optimizers) {
            optimizer.onHandle(cfg);
        }
    }
}
```

**EP20 Compiler Integration**:
```java
// In Compiler.java
cfg.addOptimizer(new ControlFlowAnalysis<>());
cfg.addOptimizer(new ConstantFolding());
cfg.addOptimizer(new DeadCodeElimination());
cfg.addOptimizer(new CommonSubexpressionElimination());

cfg.applyOptimizers();
```

**Pass Ordering**:
1. **Constant folding**: Early pass, simplifies expressions
2. **Dead code elimination**: Remove obvious dead code
3. **Common subexpression elimination**: Requires folded constants
4. **Copy propagation**: Further simplifies code
5. **Dead code elimination (again)**: Remove newly dead code

**Iteration**: Some passes need multiple iterations to reach fixed point.

### 3.7 Code Generation to EP18 VM

**EP18 VM Instruction Set**:
```
Stack Operations:
  push <value>    - Push constant or variable onto stack
  pop             - Pop top value from stack
  load <slot>      - Load variable from stack frame slot
  store <slot>     - Store top value to stack frame slot

Arithmetic:
  iadd, isub, imul, idiv  - Integer arithmetic
  fadd, fsub, fmul, fdiv  - Float arithmetic
  lt, gt, eq, ne           - Comparisons

Control Flow:
  br <label>      - Unconditional branch
  brf <label>     - Branch if false (pop value)
  call <func>      - Function call
  ret             - Return from function
  halt            - Stop execution

Built-in Functions:
  print           - Print top of stack
```

**EP20 CymbolAssembler Implementation**:

```java
public class CymbolAssembler implements IRVisitor<Void, Void> {
    private LinkedList<String> assembleCmdBuffer = new LinkedList<>();
    private int indents = 0;

    public Void visit(List<IRNode> linearInstrs) {
        for (var instr : linearInstrs) {
            if (instr instanceof Expr) {
                ((Expr) instr).accept(this);
            } else {
                ((Stmt) instr).accept(this);
            }
        }
        return null;
    }

    // Binary expressions
    @Override
    public Void visit(BinExpr node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);

        switch (node.getOpType()) {
            case ADD: emit("iadd"); break;
            case SUB: emit("isub"); break;
            case MUL: emit("imul"); break;
            case DIV: emit("idiv"); break;
            case LT:  emit("lt"); break;
            case GT:  emit("gt"); break;
            case EQ:  emit("eq"); break;
            case NE:  emit("ne"); break;
        }
        return null;
    }

    // Unary expressions
    @Override
    public Void visit(UnaryExpr node) {
        node.expr.accept(this);

        switch (node.op) {
            case NEG: emit("ineg"); break;
            case NOT: emit("not"); break;
        }
        return null;
    }

    // Constants
    @Override
    public <T> Void visit(ConstVal<T> constVal) {
        var value = constVal.getVal();
        if (value instanceof Integer i) {
            emit("iconst %d".formatted(i));
        } else if (value instanceof Float f) {
            emit("fconst %f".formatted(f));
        } else if (value instanceof Boolean b) {
            emit("bconst %d".formatted(b ? 1 : 0));
        } else if (value instanceof String s) {
            emit("sconst \"%s\"".formatted(s));
        }
        return null;
    }

    // Variables
    @Override
    public Void visit(FrameSlot frameSlot) {
        emit("load %d".formatted(frameSlot.getSlotIdx()));
        return null;
    }

    // Assignment
    @Override
    public Void visit(Assign assign) {
        assign.getRhs().accept(this);

        if (assign.getLhs() instanceof FrameSlot frameSlot) {
            emit("store %d".formatted(frameSlot.getSlotIdx()));
        }
        return null;
    }

    // Labels
    @Override
    public Void visit(Label label) {
        if (indents > 0) indents--;

        if (label instanceof FuncEntryLabel) {
            emit("%s".formatted(label.toSource()));
        } else {
            emit("%s:".formatted(label.toSource()));
        }
        indents++;
        return null;
    }

    // Jumps
    @Override
    public Void visit(JMP jmp) {
        emit("br %s".formatted(jmp.getNext().toString()));
        indents--;
        return null;
    }

    @Override
    public Void visit(CJMP cjmp) {
        emit("brf %s".formatted(cjmp.getElseBlock().getLabel().toString()));
        indents--;
        return null;
    }

    // Function calls
    @Override
    public Void visit(CallFunc callFunc) {
        if (!callFunc.getFuncType().isBuiltIn()) {
            emit("call %s()".formatted(callFunc.getFuncName()));
        } else {
            emit("%s".formatted(callFunc.getFuncName()));
        }
        return null;
    }

    // Return
    @Override
    public Void visit(ReturnVal returnVal) {
        if (returnVal.getRetVal() != null) {
            returnVal.getRetVal().accept(this);
        }

        if (returnVal.isMainEntry()) {
            emit("halt");
        } else {
            emit("ret");
        }
        indents--;
        return null;
    }

    private void emit(String cmd) {
        var indentCmdBuf = "    ".repeat(indents) + cmd;
        assembleCmdBuffer.add(indentCmdBuf);
    }

    public String getAsmInfo() {
        return String.join("\n", assembleCmdBuffer).concat("\n");
    }
}
```

### 3.8 Code Generation Example

**IR Code**:
```
main:
L0: @0 = 5
    @1 = 10
    t0 = @0 + @1
    @2 = t0
    print @2
    return

```

**Generated EP18 VM Bytecode**:
```
main
    L0:
        iconst 5
        store 0
        iconst 10
        store 1
        load 0
        load 1
        iadd
        store 2
        load 2
        print
        ret
        halt
```

### 3.9 Optimization Verification

**Semantic Equivalence Testing**:
```java
@Test
void testConstantFoldingPreservesSemantics() {
    // Compile and optimize
    var optimizedIR = compileAndOptimize("int x = 5 + 3 * 2;");
    var unoptimizedIR = compile("int x = 5 + 3 * 2;");

    // Generate bytecode
    var optimizedBytecode = generateBytecode(optimizedIR);
    var unoptimizedBytecode = generateBytecode(unoptimizedIR);

    // Execute and compare results
    var optimizedResult = execute(optimizedBytecode);
    var unoptimizedResult = execute(unoptimizedBytecode);

    assertThat(optimizedResult).isEqualTo(unoptimizedResult);
}
```

**Performance Testing**:
```java
@Test
void testOptimizationImprovesPerformance() {
    var sourceCode = """
        int factorial(int n) {
            if (n <= 1) return 1;
            return n * factorial(n - 1);
        }

        void main() {
            print(factorial(10));
        }
        """;

    // Measure unoptimized performance
    long unoptimizedTime = measureExecutionTime(sourceCode, false);

    // Measure optimized performance
    long optimizedTime = measureExecutionTime(sourceCode, true);

    // Optimized should be faster (or equal)
    assertThat(optimizedTime).isLessThanOrEqualTo(unoptimizedTime * 1.1);
}
```

---

## 4. Practical Exercises

### 4.1 Foundation Exercises

**Exercise 1: Implement Constant Folding**
Given IR:
```
t0 = 5
t1 = 10
t2 = t0 + t1
```

**Tasks**:
- Implement constant folding visitor
- Fold `t0 + t1` to constant 15
- Update IR to `t2 = 15`
- Write unit tests for arithmetic operators

**Exercise 2: Identify Dead Code**
Given IR:
```
t0 = 5      // Unused
t1 = 10     // Used
t2 = t1 + 1  // Uses t1
return t2
```

**Tasks**:
- Implement liveness analysis
- Identify `t0 = 5` as dead assignment
- Remove dead assignment from IR
- Validate correctness with execution tests

**Exercise 3: Implement Assignment Emission**
Given IR assignment:
```
@0 = 5
```

**Tasks**:
- Implement `visit(Assign)` in assembler
- Emit `iconst 5` then `store 0`
- Handle both constants and variables on RHS
- Test with various assignment patterns

### 4.2 Intermediate Exercises

**Exercise 4: Implement Common Subexpression Elimination**
Given IR:
```
t0 = a + b
t1 = a + b  // Redundant!
x = t0
y = t1
```

**Tasks**:
- Track expression signatures
- Replace second `a + b` with `t0`
- Update IR to eliminate redundant computation
- Verify output correctness

**Exercise 5: Implement Function Call Code Generation**
Given IR:
```
push a
push b
call add()
pop @result
```

**Tasks**:
- Implement `visit(CallFunc)` in assembler
- Emit `call add()` for user-defined functions
- Handle built-in functions (`print`, `read`)
- Manage return value handling

**Exercise 6: Implement Control Flow Code Generation**
Given IR:
```
L0: t0 = x > 10
    if t0 goto L2
    y = 0
    goto L3
L1: y = 1
L2: return y
```

**Tasks**:
- Emit conditional jump with `brf`
- Emit unconditional jumps with `br`
- Handle label emission
- Verify control flow correctness

### 4.3 Advanced Exercises

**Exercise 7: Implement Copy Propagation**
Given IR:
```
x = y
z = x + 1
```

**Tasks**:
- Track copy statements (x = y)
- Replace `x` with `y` in subsequent uses
- Update IR to `z = y + 1`
- Handle copy invalidation when y is modified

**Exercise 8: Implement Dead Code Elimination with Liveness**
Given IR:
```
x = 1
y = 2
x = 3  // Dead: x overwritten
return y
```

**Tasks**:
- Compute live variables at each point
- Identify dead assignments
- Remove dead assignments from IR
- Verify liveness correctness

**Exercise 9: Build Complete Optimizer Pipeline**
- Implement multiple optimization passes:
  - Constant folding
  - Dead code elimination
  - Common subexpression elimination
  - Copy propagation
- Apply passes in correct order
- Iterate until fixed point
- Validate optimizations preserve semantics

**Exercise 10: Optimize Real Cymbol Program**
Given program:
```c
int fibonacci(int n) {
    if (n <= 1) return 1;
    return fibonacci(n - 1) + fibonacci(n - 2);
}

void main() {
    print(fibonacci(10));
}
```

**Tasks**:
- Compile to IR
- Apply all optimization passes
- Generate optimized bytecode
- Measure performance improvement
- Compare code size

---

## 5. Common Pitfalls

### 5.1 Incorrect Constant Folding

**Pitfall**: Folding overflow or division by zero
```java
// WRONG: No overflow checking
public ConstVal<Integer> foldAdd(ConstVal<Integer> lhs, ConstVal<Integer> rhs) {
    return ConstVal.valueOf(lhs.getValue() + rhs.getValue());  // May overflow!
}
```

**Solution**: Handle overflow correctly
```java
// CORRECT: Use checked arithmetic or document behavior
public ConstVal<Integer> foldAdd(ConstVal<Integer> lhs, ConstVal<Integer> rhs) {
    var result = lhs.getValue() + rhs.getValue();
    // Check for overflow (optional, depending on language semantics)
    return ConstVal.valueOf(result);
}
```

### 5.2 Incorrect Dead Code Detection

**Pitfall**: Removing assignments that affect side effects
```java
// WRONG: Removing all unused assignments
if (!liveOut.contains(lhs)) {
    removeAssignment(assign);  // May remove side-effecting calls!
}
```

**Solution**: Check for side effects
```java
// CORRECT: Preserve side-effecting code
if (!liveOut.contains(lhs) && !hasSideEffects(assign.getRhs())) {
    removeAssignment(assign);
}
```

### 5.3 CSE Breaking Program Semantics

**Pitfall**: Replacing expression that has side effects
```java
// WRONG: CSE without checking side effects
if (exprMap.containsKey(signature)) {
    replacements.put(binExpr, exprMap.get(signature));
    // What if operands have side effects (e.g., function calls)?
}
```

**Solution**: Only CSE pure expressions
```java
// CORRECT: Check for side effects
if (exprMap.containsKey(signature) && isPureExpression(binExpr)) {
    replacements.put(binExpr, exprMap.get(signature));
}
```

### 5.4 Incorrect Copy Propagation

**Pitfall**: Propagating copy across modification of source
```java
// WRONG: Not tracking variable modifications
x = y
z = x + 1
y = 10    // y modified!
w = x + 2  // Cannot propagate x → y here
```

**Solution**: Invalidate copies on modification
```java
// CORRECT: Track modifications
if (assign.getLhs().equals(y)) {
    copyMap.remove(x);  // Invalidate copy x = y
}
```

### 5.5 Incorrect Code Generation for Jumps

**Pitfall**: Wrong jump target format
```java
// WRONG: Incorrect label format
@Override
public Void visit(JMP jmp) {
    emit("br %s".formatted(jmp.getNext().toString()));  // May output wrong format
    return null;
}
```

**Solution**: Use correct label format
```java
// CORRECT: Get label name properly
@Override
public Void visit(JMP jmp) {
    var label = jmp.getNext().getLabel().toSource();
    emit("br %s".formatted(label));
    return null;
}
```

### 5.6 Stack Imbalance in Code Generation

**Pitfall**: Incorrect stack operations
```java
// WRONG: Stack imbalance
public Void visit(BinExpr node) {
    node.getLhs().accept(this);   // Pushes 1 value
    node.getRhs().accept(this);   // Pushes 1 value
    emit("iadd");                // Pops 2, pushes 1
    // Stack: 1 value (correct)
    emit("pop");                 // Pops 1 value
    // Stack: 0 values (may be wrong!)
    return null;
}
```

**Solution**: Track stack balance carefully
```java
// CORRECT: Verify stack operations
public Void visit(BinExpr node) {
    node.getLhs().accept(this);   // Stack: +1
    node.getRhs().accept(this);   // Stack: +1
    emit("iadd");                // Stack: -2+1 = -1
    // Stack: 1 value (correct)
    return null;
}
```

### 5.7 Optimization Pass Ordering

**Pitfall**: Passes in wrong order
```java
// WRONG: DCE before CSE
optimizers.add(new DeadCodeElimination());  // Removes unused temporaries
optimizers.add(new CommonSubexpressionElimination());  // Can't find CSE opportunities!
```

**Solution**: Order passes correctly
```java
// CORRECT: CSE before DCE
optimizers.add(new ConstantFolding());           // First
optimizers.add(new CommonSubexpressionElimination());  // Second
optimizers.add(new CopyPropagation());         // Third
optimizers.add(new DeadCodeElimination());      // Last (removes newly dead code)
```

### 5.8 Fixed-Point Iteration Issues

**Pitfall**: Not iterating to fixed point
```java
// WRONG: Single iteration
optimizers.forEach(optimizer -> optimizer.onHandle(cfg));
```

**Solution**: Iterate until fixed point
```java
// CORRECT: Fixed-point iteration
boolean changed;
do {
    changed = false;
    for (var optimizer : optimizers) {
        var before = cfg.toString();
        optimizer.onHandle(cfg);
        var after = cfg.toString();
        if (!before.equals(after)) {
            changed = true;
        }
    }
} while (changed);
```

---

## 6. Additional Resources

### 6.1 Recommended Reading

**Compiler Optimization**:
- **"Compilers: Principles, Techniques, and Tools"** (Dragon Book) by Aho et al.
  - Chapter 9: Machine-Independent Optimizations
  - Section 9.1: The Principal Sources of Optimization

- **"Engineering a Compiler"** by Cooper & Torczon
  - Chapter 8: Optimizations for Locality
  - Chapter 10: Data-Flow Analysis

- **"Optimizing Compilers for Modern Architectures"** by Muchnick
  - Comprehensive coverage of optimization techniques

**Code Generation**:
- **"The Design and Evolution of C++"** by Stroustrup
  - Chapter on code generation strategies

- **"LLVM Compiler Infrastructure"** (Lattner et al.)
  - [LLVM Code Generation Documentation](https://llvm.org/docs/CodeGenerator.html)

### 6.2 Online Resources

**Tutorials & Documentation**:
- [Compiler Optimization Wikipedia](https://en.wikipedia.org/wiki/Optimizing_compiler)
- [Dead Code Elimination Wikipedia](https://en.wikipedia.org/wiki/Dead_code_elimination)
- [Common Subexpression Elimination Wikipedia](https://en.wikipedia.org/wiki/Common_subexpression_elimination)

**Optimization Techniques**:
- [GCC Optimization Flags](https://gcc.gnu.org/onlinedocs/gcc/Optimize-Options.html)
- [LLVM Optimization Passes](https://llvm.org/docs/Passes.html)

**Virtual Machines**:
- [EP18 VM Instruction Set Reference](../../ep18/README.md)
- [Stack-Based vs Register-Based VMs](https://en.wikipedia.org/wiki/Stack-oriented_programming)

### 6.3 Practice Problems

**Beginner**:
- Implement constant folding for arithmetic operators
- Implement dead code elimination for simple cases
- Generate bytecode for basic expressions

**Intermediate**:
- Implement common subexpression elimination
- Implement copy propagation
- Generate bytecode for control flow statements

**Advanced**:
- Implement loop invariant code motion
- Implement strength reduction
- Build complete optimizer pipeline with multiple passes

### 6.4 Debugging Tools

**EP20 Built-in Tools**:
- `CymbolAssembler`: Generate bytecode from IR
- `ControlFlowAnalysis`: Optimize CFG
- `Prog.optimizeBasicBlock()`: Basic block optimization
- `CFG.toString()`: Visualize optimization results

**External Tools**:
- **GDB**: Debug bytecode execution
- **VM Profiler**: Measure execution time
- **Diff Tools**: Compare optimized vs unoptimized output

### 6.5 Key EP20 Files to Study

**Optimization**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java` - CFG optimization
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java` - Liveness analysis
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java` - Optimizer interface

**Code Generation**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java` - IR to VM bytecode
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolVMIOperatorEmitter.java` - Operator emission

**Tests**:
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/codegen/EndToEndCompilationTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/codegen/VMInstructionTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlockOptimizationTest.java`

---

**Summary**: This chapter covers local optimization techniques and code generation, focusing on constant folding, dead code elimination, common subexpression elimination, and generation of EP18 VM bytecode. Mastering optimization and code generation is essential for building efficient, production-quality compilers.
