# Chapter 18 Prompt: Global Optimization Techniques

## Chapter Writing Task

Please write a complete chapter for this book.

### Chapter Basic Information

- **Chapter Title**: 第18章：全局优化技术
- **Module**: 模块 5：高级优化与AI协作（EP21）
- **Target Reader**: 有编译器经验的工程师，学习高级优化算法
- **Prerequisites**: SSA基础、优化理论
- **Repository EP Range**: EP21 (尾递归优化、循环不变代码外提、常量传播)
- **Position in Book**:
  - Previous Chapter: 第17章：SSA和数据流基础
  - Next Chapter: 第19章：优化器架构与跨模块集成
- **After completing this chapter, readers should be able to**:
  - 理解全局优化技术的基本原理
  - 掌握尾递归优化算法
  - 掌握循环不变代码外提技术
  - 理解常量传播在SSA中的应用

---

## Content Structure Requirements

### 1. Chapter Overview

**要求**:
- 用 1–3 句话说明本章要解决什么问题、处在整个编译器流水线哪一环
- 明确本章的学习价值

**模板**:
```
本章聚焦于 {核心问题}，它是编译器流水线中的 {阶段名称} 阶段。
通过学习本章，你将掌握 {核心概念}，这是 {下一阶段} 的基础。

【你现在站在哪】:
... → [已完成的阶段] → ✅ {本章名称} → [后续阶段] → ...
```

### 2. Motivation and Real-World Scenarios

**要求**:
- 用一个贴近工程实战的小故事/场景，引出本章主题
- 说明如果没有这一章的能力，在真实项目中会遇到什么痛点

**模板**:
```
真实场景：{场景描述}

{具体问题或挑战}

如果缺少本章的能力，你将面临：
- {痛点1}
- {痛点2}
- {痛点3}

本章将教你如何：{解决方案概述}
```

### 3. Human Engineer Line: Technology and Implementation

#### 3.1 Core Concepts

**要求**:
- 用通俗语言解释本章关键概念
- 必须包含 1–3 个小图示的文字描述（用 `[图X：描述]` 占位）
- 使用比喻和类比降低理解难度

**模板**:
```
核心概念：{概念名称}

通俗解释：{通俗描述}

[图1：{图示说明文字}]
{图示详细描述，说明关键组成部分和关系}

类比理解：{比喻或类比}
{用日常生活中的例子类比技术概念}

相关概念：
- {相关概念1}：{简短说明}
- {相关概念2}：{简短说明}
```

**如果是复杂概念（如 SSA、CFG、数据流分析）**:
```
[图2：{复杂概念的多角度图示}]

角度1：数据流视角
{说明数据如何在程序中流动}

角度2：控制流视角
{说明程序执行路径如何组织}

角度3：依赖关系视角
{说明变量和操作之间的依赖}
```

#### 3.2 Correspondence with Repository EPs

**要求**:
- 明确说明：对应目录、关键类/接口/方法、它们各自做什么
- 不需要贴完整代码，只贴关键方法
- 使用 ```java 代码块，并加上详尽中文注释

**模板**:
```
对应 EP：{EP 编号}

目录结构：
```
{ep_number}/
├── src/main/java/org/teachfx/antlr4/ep{ep_number}/
│   ├── {关键包1}/
│   │   ├── {关键类1}.java     // {类的作用}
│   │   └── {关键类2}.java     // {类的作用}
│   └── {关键包2}/
│       └── {关键接口}.java       // {接口的作用}
├── src/main/antlr4/
│   └── {语法文件}.g4              // {语法的作用}
└── src/test/java/
    └── {测试包}/
        └── {测试类}Test.java     // {测试覆盖内容}
```

关键类/方法说明：

**{类名1}** - {类的作用}
```java
public class {类名1} {
    // {类的职责说明}

    /**
     * {方法的作用说明}
     *
     * @param {参数说明}
     * @return {返回值说明}
     */
    public {返回类型} {方法名}({参数列表}) {
        // 核心逻辑说明
        // {设计考虑或边界情况处理}

        // 关键步骤1：{说明}
        {关键代码块}

        // 关键步骤2：{说明}
        {关键代码块}

        // 返回值处理
        return {返回表达式};
    }

    /**
     * {另一个方法的作用说明}
     * 时间复杂度：{时间复杂度}
     * 空间复杂度：{空间复杂度}
     */
    public {返回类型} {另一个方法名}({参数列表}) {
        // 简要说明实现思路
    }
}
```

**{接口名}** - {接口的作用}
```java
public interface {接口名} {
    /**
     * {方法1的作用说明}
     * @param {参数说明}
     * @return {返回值说明}
     */
    {返回类型} {方法名}({参数列表});

    /**
     * {方法2的作用说明}
     * 此方法是可选的，{具体说明}
     */
    default {返回类型} {方法名}({参数列表}) {
        // 默认实现说明
        return {默认值};
    }
}
```

**关键设计模式**：{设计模式名称}

{设计模式的说明和在当前场景中的应用}

类/接口关系：
```
{接口}
   ↑
{抽象类}
   ↑
{具体实现类1}    {具体实现类2}
```

#### 3.3 Practical Workflow

**要求**:
- 给出从命令行/IDE 运行本章代码的具体步骤
- 说明预期输出和验证方法
- 若涉及可视化，说明如何生成图表
- 提供故障排查提示

**模板**:
```
实战步骤：{章节实战任务}

步骤1：{步骤1描述}
```bash
# {命令或操作}
# {说明}
cd {目录路径}
mvn {maven 命令}
```

{预期输出}：
```
{输出示例}
```

{验证方法}：
- 检查点1：{如何检查}
- 检查点2：{如何检查}

步骤2：{步骤2描述}
```bash
# {命令或操作}
# {说明}
```

{预期输出}：
```
{输出示例}
```

步骤3：{步骤3描述 - 如果涉及可视化}
```bash
# 生成可视化图表
cd {目录路径}
dot -Tpng {input.dot} -o {output.png}
```

{预期结果}：
- [图3：{生成的可视化图表}]
  {图表说明：节点代表什么、边代表什么、颜色编码说明等}

故障排查：

**问题1：{常见问题和现象}**
- 原因：{问题原因}
- 解决方法：{具体解决步骤}

**问题2：{另一个常见问题}**
- 原因：{问题原因}
- 解决方法：{具体解决步骤}

进阶技巧：

1. {技巧1}
2. {技巧2}
```

### 4. AI Collaboration Line: Context Engineering Perspective

#### 4.1 Context Design

**要求**:
- 解释让 AI 帮忙需要哪些上下文
- 按类型列出源码、文档、测试文件
- 说明上下文的组织逻辑

**模板**:
```
为了让 AI 帮助完成 {本章任务}，我们需要精心设计上下文。

上下文文件列表：

**源码文件**（按阅读顺序）：
1. `ep{ep_number}/src/main/java/org/teachfx/antlr4/ep{ep_number}/{包路径}/{关键类1}.java`
   - 作用：{文件作用}
   - 关键方法：{方法列表}

2. `ep{ep_number}/src/main/java/org/teachfx/antlr4/ep{ep_number}/{包路径}/{关键类2}.java`
   - 作用：{文件作用}
   - 关键方法：{方法列表}

3. `ep{ep_number}/src/main/antlr4/{语法文件}.g4`
   - 作用：{语法文件作用}
   - 关键生产规则：{规则列表}

**文档文件**：
1. `ep{ep_number}/docs/{相关文档}.md`
   - 作用：{文档作用}
   - 关键章节：{章节列表}

2. `AGENTS.md`
   - 作用：代码规范和最佳实践
   - 相关部分：{具体规范引用}

**测试文件**：
1. `ep{ep_number}/src/test/java/org/teachfx/antlr4/ep{ep_number}/{测试类}Test.java`
   - 作用：{测试覆盖内容}
   - 关键测试方法：{测试方法列表}

**示例输入/输出**：
1. `ep{ep_number}/benchmarks/{目录}/{测试文件}.cym`
   - 作用：{示例文件作用}

上下文组织说明：

这些文件按照{组织原则}组织：

1. **核心实现在前**：先提供核心算法和数据结构的代码
2. **支撑文件在后**：再提供测试、文档等辅助材料
3. **测试用例作为验证**：最后提供测试文件，用于验证 AI 生成代码的正确性
4. **文档作为参考**：提供设计文档，说明架构决策和约束条件

为什么这样组织：
- AI 可以先理解核心逻辑，再对照文档验证理解
- 测试用例让 AI 知道预期行为
- 完整的上下文确保 AI 的输出不会偏离项目架构
```

#### 4.2 Prompt Templates (for AI)

**要求**:
- 给出 1–2 个可直接复制给 AI 的 Prompt 模板
- 必须包含任务目标、具体要求、参考上下文、期望输出

**类型 A: 尾递归优化实现 Prompt 模板**
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
   - 在 `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/` 目录下

2. 实现检测算法
   - 识别尾调用位置（函数返回前的最后操作）
   - 检测是否为递归调用（调用自身）
   - 识别 Fibonacci 模式（双递归）

3. 添加单元测试
   - 测试简单尾递归（如 factorial）
   - 测试 Fibonacci 模式
   - 测试非尾递归情况
   - 使用 JUnit 5 和 AssertJ

参考上下文文件：
- 源码：
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/TailRecursionOptimizer.java`
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/IFlowOptimizer.java`
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/ir/stmt/ReturnVal.java`
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/ir/expr/CallFunc.java`
- 文档：
  - `AGENTS.md`（代码规范）
  - `ep21/docs/01_core_design/语言规范.md`（尾递归优化定义）
- 测试：
  - `ep21/src/test/java/org/teachfx/antlr4/ep21/pass/cfg/TailRecursionOptimizerTest.java`
  - `ep21/benchmarks/stanford/fib.cym`（Fibonacci 测试用例）

约束条件：
- 不能改变程序的语义
- 必须处理循环不变代码移动的情况
- 优化后的代码性能应优于原始递归
- 遵循 AGENTS.md 中的代码风格规范

期望输出：
1. 尾递归优化器完整实现
2. 测试用例代码
3. 优化前后的性能对比
4. 算法复杂度分析
```

**类型 B: 循环不变代码外提 Prompt 模板**
```
请为 EP21 编译器实现循环不变代码外提（Loop Invariant Code Motion）。

任务目标：
- 识别循环不变代码
- 将不变代码外提到循环外
- 确保安全性（不改变程序语义）

具体要求：
1. 实现循环不变代码分析器
   - 分析指令是否依赖循环变化的变量
   - 使用数据流分析确定不变性
   - 在 `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/` 目录下

2. 实现循环不变代码外提优化器
   - 识别循环外提机会
   - 执行代码外提
   - 维护 SSA 形式
   - 实现 `IFlowOptimizer<IRNode>` 接口

3. 添加单元测试
   - 测试简单循环不变代码
   - 测试嵌套循环
   - 测试有副作用的情况
   - 使用 JUnit 5 和 AssertJ

参考上下文文件：
- 源码：
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/LoopAnalysis.java`
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/AbstractDataFlowAnalysis.java`
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/IFlowOptimizer.java`
- 文档：
  - `AGENTS.md`（代码规范）
  - `ep21/docs/01_core_design/架构设计规范.md`（优化 Pass 框架）
- 测试：
  - `ep21/src/test/java/org/teachfx/antlr4/ep21/analysis/dataflow/LoopAnalysisTest.java`
  - `ep21/benchmarks/optimization/loop_invariant.cym`（循环不变代码测试用例）

约束条件：
- 不能将有副作用的代码外提
- 必须考虑异常安全
- 外提后的代码必须在循环外可达
- 保持 SSA 形式

期望输出：
1. 循环不变代码分析器实现
2. 循环不变代码外提优化器实现
3. 测试用例代码
4. 优化效果分析
```

#### 4.3 AI Should/Should Not Do

**要求**:
- 列出 3–5 条允许和禁止 AI 做的事情
- 明确边界，避免 AI 超出范围

**模板**:
```
在让 AI 帮助完成本章任务时，我们需要明确 AI 的职责边界。

**✅ AI 允许做的事情**：

1. **实现明确界定的优化算法**
   - 可以：实现尾递归优化、循环不变代码外提
   - 可以：应用优化规则、改进性能
   - 不能：改变优化算法的基本正确性

2. **生成优化 Pass 代码**
   - 可以：编写优化 Visitor
   - 可以：实现 `IFlowOptimizer` 接口
   - 不能：修改 `IFlowOptimizer` 接口定义

3. **添加优化测试用例**
   - 可以：编写优化算法测试
   - 可以：生成性能对比测试
   - 不能：删除现有优化测试

4. **生成代码注释和文档**
   - 可以：添加优化策略说明
   - 可以：生成性能分析报告
   - 不能：替换现有设计文档

5. **优化代码结构**
   - 可以：提取方法、减少重复
   - 可以：添加辅助类
   - 不能：改变公开接口签名

**❌ AI 禁止做的事情**：

1. **修改优化 Pass 框架**
   - 不允许：改变 `IFlowOptimizer` 接口
   - 不允许：修改 CFG 的接口
   - 原因：多个优化 Pass 依赖于这个框架

2. **破坏程序语义**
   - 不允许：优化改变程序输出
   - 不允许：忽略安全性约束
   - 原因：优化必须保持语义等价性

3. **删除优化测试**
   - 不允许：移除现有优化测试
   - 不允许：降低测试覆盖率
   - 原因：测试是优化正确性的保证

4. **跨 EP 模块修改**
   - 不允许：修改 EP20 或其他 EP 的代码
   - 不允许：引入新的外部依赖
   - 原因：保持 EP 模块独立性

5. **忽略 SSA 形式约束**
   - 不允许：破坏 SSA 形式
   - 不允许：改变 φ 函数的定义
   - 原因：SSA 形式是全局优化的基础
```

#### 4.4 Verification and Rollback Strategy

**要求**:
- 说明验证步骤、测试命令、检查点
- 提供简单的 Git 回滚方案

**模板**:
```
在 AI 完成修改后，我们必须进行严格的验证才能将更改合并到主分支。

## 自动化验证

**步骤1：运行相关测试**
```bash
# 进入 EP 目录
cd ep21

# 运行所有优化测试
mvn test -Dtest=*Optimizer*Test

# 或者运行特定测试类
mvn test -Dtest=TailRecursionOptimizerTest

# 预期输出：
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
```

**关键测试**：
- `TailRecursionOptimizerTest.java` - 验证尾递归优化
- `ConstantFoldingOptimizerTest.java` - 验证常量折叠
- `CommonSubexpressionEliminationOptimizerTest.java` - 验证公共子表达式消除
- `DeadCodeEliminationOptimizerTest.java` - 验证死代码消除

**验证标准**：
- ✅ 所有测试通过（Failures: 0, Errors: 0）
- ✅ 优化后的代码语义正确
- ✅ 优化带来性能提升

**步骤2：编译验证**
```bash
# 清理并重新编译
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

**步骤3：运行基准测试**
```bash
# 运行优化专项基准测试
cd ep21/benchmarks/optimization
./run_tests.sh

# 预期结果：优化后的程序性能优于原始程序
```

## 手工检查点

即使所有测试通过，我们仍需手工检查以下关键点：

**检查1：优化语义正确性**
- [ ] 优化后的程序输出与原始程序一致
- [ ] 尾递归转换不改变函数行为
- [ ] 循环不变代码外提不改变程序语义

**检查2：代码风格符合规范**
- [ ] 包命名：`org.teachfx.antlr4.ep21.package`
- [ ] 类命名：PascalCase（如 `TailRecursionOptimizer`）
- [ ] 方法命名：camelCase（如 `detectTailRecursion`）

**检查3：没有引入新的编译错误**
- [ ] 查看项目根目录的 `mvn clean compile` 输出
- [ ] 确认没有新的 ERROR 或 WARNING

**检查4：文档完整性**
- [ ] 新增类/方法有 JavaDoc 注解
- [ ] 优化策略有详细说明
- [ ] 复杂逻辑有行内注释

## 回滚方案

如果 AI 修改后出现问题，使用以下步骤快速恢复：

### 方案1：Git Stash（推荐）
```bash
# 查看当前修改
git status

# 保存 AI 修改到 stash（带备注）
git stash push -m "AI changes for {章节名称}"

# 如果出现问题，恢复到修改前状态
git stash pop

# 或者完全丢弃 AI 修改
git stash drop
```

### 方案2：Git Checkout（硬恢复）
```bash
# 查看修改历史
git log --oneline -5

# 恢复到修改前的提交
git checkout {commit_hash}

# 或者恢复特定文件
git checkout HEAD~1 -- {文件路径}
```

### 方案3：Git Reset（危险，谨慎使用）
```bash
# 软重置（保留 AI 修改在 staging）
git reset --soft HEAD~1

# 硬重置（完全丢弃 AI 修改）
git reset --hard HEAD~1

# 混合重置（保留 AI 修改在未跟踪）
git reset --mixed HEAD~1
```

### 方案4：创建新分支实验
```bash
# 从干净状态创建新分支
git checkout -b ai-experiment-{章节名称}

# 让 AI 在新分支工作
# 完成后合并回主分支
git checkout main
git merge ai-experiment-{章节名称}
```

## 验证流程总结
```
AI 生成代码
    ↓
自动化验证（mvn test）
    ↓
手工检查（语义正确性、代码风格）
    ↓
基准测试验证（性能提升）
    ↓
✅ 验证通过，合并到主分支
    ❌ 验证失败，执行回滚
```

## 常见问题排查

**问题1：优化后程序行为不正确**
- 现象：优化后程序输出与原始程序不一致
- 排查：
  1. 检查优化是否改变了程序语义
  2. 验证 SSA 形式是否保持
  3. 确认循环不变代码是否真的不变
- 解决：根据失败信息修正优化逻辑

**问题2：性能未提升**
- 现象：优化后程序性能没有改善
- 排查：
  1. 检查优化是否真正执行
  2. 验证测试用例是否适合该优化
  3. 分析优化开销是否抵消了收益
- 解决：调整优化策略或改进实现

**问题3：测试失败**
- 现象：`[ERROR] Tests run: X, Failures: Y`
- 排查：
  1. 查看 `target/surefire-reports/` 下的测试报告
  2. 检查失败测试的断言信息
  3. 确认 AI 生成的代码与预期行为一致
- 解决：根据失败信息修改 AI 代码，或调整测试用例
```

### 5. Exercises

**要求**:
- 设计 3–5 道练习，分为手工版和 AI 协作版
- 每题后附简短提示，不给出完整答案

**模板**:
```
本章练习题

练习1：{练习标题}（手工实现版）

难度：⭐⭐☆☆☆
预计时间：30–45 分钟

题目描述：
{练习的详细描述}

要求：
- 完全手工实现，不依赖 AI
- 遵循本章学到的概念和方法
- 可参考仓库代码，但不能直接复制

验收标准：
- [ ] 代码能编译通过
- [ ] 基本功能正确
- [ ] 代码风格符合规范

**💡 解题思路提示**：
{提示方向，不给出完整答案}

---

练习2：{练习标题}（AI 协作版）

难度：⭐⭐⭐☆☆
预计时间：45–60 分钟

题目描述：
{练习的详细描述}

AI 协作要求：
1. 设计上下文：列出需要提供给 AI 的文件和说明
2. 设计 Prompt：参考本章的 Prompt 模板，设计适合此任务的 Prompt
3. 验证 AI 输出：使用本章的验证策略
4. 理解 AI 代码：确保你能解释 AI 生成的每一部分

验收标准：
- [ ] AI 生成的代码能编译通过
- [ ] 功能正确且符合要求
- [ ] 通过所有测试
- [ ] 你能解释 AI 代码的实现逻辑

**💡 解题思路提示**：
{提示方向，包括：如何组织上下文、Prompt 设计要点、验证关键点}

---

练习3：{练习标题}（综合挑战）

难度：⭐⭐⭐⭐☆
预计时间：60–90 分钟

题目描述：
{练习的详细描述，需要综合运用多个概念}

要求：
- 可以选择手工实现或 AI 协作
- 如果选择 AI 协作，需要详细记录协作过程
- 提交时说明 AI 参与部分和人工修改部分

验收标准：
- [ ] 功能完整
- [ ] 性能合理（如适用）
- [ ] 代码可读性良好
- [ ] 有完整的测试覆盖

**💡 解题思路提示**：
{提示方向，包括：多个概念的融合方式、关键设计决策}

---

练习4：{进阶练习 - 可选}

难度：⭐⭐⭐⭐⭐
预计时间：90–120 分钟

题目描述：
{进阶练习描述，涉及更复杂的问题}

适合人群：
- 想深入理解本章概念的高级读者
- 有志于研究编译器优化的读者

**💡 解题思路提示**：
{高阶提示，包括：研究思路、参考资料方向}
```

### 6. Chapter Summary and Next Chapter Preview

**要求**:
- 总结关键收获，说明如何用到下一章
- 预告下一章主题和衔接

**模板**:
```
## 本章小结

通过本章的学习，你已经掌握了：

1. **{核心概念}**
   - 理解了{概念的定义和作用}
   - 掌握了{概念在编译器中的应用}

2. **{关键技术}**
   - 学会了{技术的基本用法}
   - 能够独立实现{相关功能}

3. **{实践技能}**
   - 熟练使用了{工具/框架}
   - 掌握了{调试和验证方法}

【你现在站在哪】:
```
... → [已完成的阶段1] → [已完成的阶段2] → ✅ {本章名称} → [下一章主题] → [后续阶段] → ...
```

**当前在编译器流水线的位置**：
- 本章实现了编译器流水线的 {阶段名称} 阶段
- 生成了{中间产物}，为下一阶段的{下一产物}奠定基础

**与下一章的衔接**：
- 本章的{核心成果}将在下一章被用于{下一章的目标}
- 例如："优化 Pass 将被组织成优化器架构；跨模块集成将使用适配器模式"

## 下一章预告

**第{next_chapter_number}章：{下一章标题}**

在下一章，我们将学习：
- {下一章核心概念1}
- {下一章核心技术2}
- {下一章实践目标3}

你将能够：
- {下一章能力1}
- {下一章能力2}
- {下一章能力3}

**准备**：为了学习下一章，建议：
- [ ] 复习本章的{相关概念}
- [ ] 运行本章的示例程序，加深理解
- [ ] 阅读下一章的预备材料（如果有）

继续加油！下一章将带你进入编译器的{下一阶段描述}。
```

---

**File Version**: 1.0
**Last Updated**: 2026-01-12
**Status**: ✅ Ready
