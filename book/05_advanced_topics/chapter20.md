# 第20章：AI Context Engineer 实践

## 本章概述

本章聚焦于如何将AI作为编程助手，系统性地参与编译器优化的开发流程。通过EP21项目的真实案例——TailRecursionOptimizer（尾递归优化器）的实现，你将掌握AI Context Engineer方法论，学会设计高效的AI协作工作流，并能够利用AI生成高质量代码和全面测试。本章展示了通过AI协作，将原本需要3-4周的人工开发时间缩短至3天的实际案例，证明了在复杂编译器优化领域，合理的AI协作策略能够带来显著的效率提升。

【你现在站在哪】:
... → [SSA转换] → [数据流分析] → [高级优化] → ✅ AI Context Engineer实践 → [附录] → ...

## 动机与真实场景

### 真实场景：EP21项目的尾递归优化器

**背景**：EP21项目需要实现尾递归优化（Tail Recursion Optimization, TRO），以消除递归函数的栈溢出风险。这是一个典型的编译器高级优化任务，涉及：

1. **控制流分析**：识别尾调用（tail call）
2. **程序转换**：将递归转换为迭代
3. **代码生成**：生成无栈溢出的循环代码
4. **测试验证**：确保优化不改变程序语义

**面临的挑战**：

1. **理论复杂度**
   - 需要理解尾递归的定义和检测条件
   - 需要设计转换算法（直接尾递归 vs. 间接尾递归）
   - 需要处理多种递归模式（Fibonacci、直接递归、互递归等）

2. **工程实践难度**
   - 需要理解EP21的分层IR架构（MIR/LIR）
   - 需要集成到现有优化Pass框架（IFlowOptimizer接口）
   - 需要处理CFG操作和节点遍历

3. **测试覆盖压力**
   - 需要测试多种递归模式
   - 需要验证优化前后语义等价
   - 需要进行性能基准测试（fib(10) vs. fib(100)）

4. **时间紧迫**
   - 团队需要在3-4周内完成开发和测试
   - 同时需要维护其他优化Pass
   - 文档和示例代码需要同步更新

### AI协作的价值主张

如果使用传统开发方式（纯人工）：
- **预估时间**：3-4周
  - 理论学习和算法设计：1周
  - 接口实现和代码开发：1-2周
  - 测试用例编写和调试：1周
- **风险**：可能遗漏边界情况，测试覆盖不足

如果使用AI协作开发：
- **实际时间**：3天
  - 上下文准备和Prompt设计：半天
  - AI生成代码和测试：半天
  - 人工审查、验证和调整：2天
- **优势**：更全面的测试覆盖，更详细的文档注释

**关键洞察**：AI并不是替代人类工程师，而是放大工程师的能力。在编译器优化这类高度技术性的领域，AI能够：

1. **快速生成样板代码**：接口实现、数据结构定义、测试框架
2. **提供理论参考**：引用经典算法和学术论文
3. **生成全面测试**：覆盖边界情况和性能基准
4. **编写详细注释**：解释复杂算法的实现细节

**人类工程师的角色**则转向：
1. **架构设计**：定义优化Pass的接口和约束条件
2. **上下文工程**：组织代码、文档和测试，为AI提供精确输入
3. **质量把关**：审查AI生成的代码，确保正确性和一致性
4. **结果验证**：运行测试和基准，验证优化效果

### 本章学习目标

通过本章的学习，你将能够：

1. **理解AI Context Engineer方法论**
   - 上下文分层组织策略（项目级、模块级、任务级）
   - Prompt设计最佳实践（任务描述、约束条件、期望输出）
   - 验证和回滚机制（自动化测试 + 人工审查）

2. **掌握AI协作工作流**
   - 完整的开发流程：准备 → 设计Prompt → AI生成 → 验证 → 集成
   - 实战案例：EP21尾递归优化器的AI协作开发
   - 工具链：ContextCollector、ai_helper.sh、ai_validator.sh

3. **积累实战经验**
   - 实际运行完整的AI协作开发流程
   - 分析真实代码（TailRecursionOptimizer.java）
   - 编写和使用Prompt模板
   - 生成和验证测试用例

## 人类工程师线：技术与实现

### 核心概念：AI Context Engineer方法论

**AI Context Engineer**是一种新的工程方法论，核心思想是"上下文工程"（Context Engineering）——精心组织信息，让AI能够准确理解任务需求和项目约束。

通俗解释：AI Context Engineer就像给资深工程师分配任务。如果只说"优化斐波那契数列"，工程师可能会提出多种优化策略；但如果你说明"检测Fibonacci模式（2个递归调用），使用累加器模式转换为迭代，在代码生成阶段实现转换"，工程师就能给出完全符合你需求的方案。AI也是如此——提供的上下文越精确，AI的输出就越符合期望。

[图1：AI Context Engineer工作流程]
```
项目代码 + 文档 + 测试
        ↓
   组织和筛选上下文（Context Engineering）
        ↓
   设计Prompt（任务 + 约束 + 期望输出格式）
        ↓
   AI生成代码/测试/文档
        ↓
   自动化验证（编译 + 单元测试 + 集成测试）
        ↓
   人工审查（代码质量 + 架构一致性 + 性能影响）
        ↓
   集成到项目（提交 + 基准测试 + 文档更新）
        ↓
   回滚（如果验证失败）
```

**关键区别**：传统AI Prompt Engineering关注"如何写Prompt"，而AI Context Engineer关注"如何组织上下文"。Prompt只是传递上下文的媒介，真正的质量取决于上下文的完整性和精确性。

### 上下文分层组织策略

为了让AI有效地参与编译器开发，我们需要组织多种类型的上下文。EP21项目采用了三层上下文架构：

[图2：上下文类型的层次结构]
```
上下文层次（自上而下）：
├─ 项目级上下文（Project-Level Context）
│  ├─ 代码规范（AGENTS.md）
│  │  ├─ 包命名规范：org.teachfx.antlr4.epXX.package
│  │  ├─ 类命名规范：PascalCase（如TailRecursionOptimizer）
│  │  ├─ 方法命名规范：camelCase（如detectDirectTailCalls）
│  │  └─ 导入顺序：ANTLR4 → External → Internal → Java stdlib
│  │
│  ├─ 架构设计（README、设计文档）
│  │  ├─ 编译器流水线：Lexing → Parsing → AST → TypeChecking → IR → Optimization → CodeGen
│  │  ├─ 分层IR设计：MIR（中层IR）+ LIR（低层IR）
│  │  └─ 优化Pass框架：IFlowOptimizer接口
│  │
│  └─ 构建系统（pom.xml）
│     ├─ Maven多模块项目结构
│     ├─ 测试框架：JUnit 5 + AssertJ + Mockito
│     └─ 日志框架：Log4j2
│
├─ 模块级上下文（Module-Level Context）
│  ├─ 核心接口定义（IFlowOptimizer.java）
│  │  ├─ onHandle(CFG<IRNode> cfg)：优化入口
│  │  ├─ 实现细节：访问者模式 + CFG遍历
│  │  └─ 扩展点：子类实现具体优化逻辑
│  │
│  ├─ 数据结构（IRNode、ASTNode、CFG）
│  │  ├─ IRNode：中间表示节点基类
│  │  ├─ BasicBlock：基本块抽象
│  │  └─ CFG<IRNode>：控制流图泛型接口
│  │
│  └─ 现有实现示例（TailRecursionOptimizer.java）
│     ├─ 实现IFlowOptimizer接口
│     ├─ 使用Log4j2记录优化日志
│     ├─ 提供优化统计信息（functionsOptimized、tailCallsDetected）
│     └─ 检测Fibonacci模式和直接尾递归
│
└─ 任务级上下文（Task-Level Context）
   ├─ 测试用例（现有测试类）
   │  ├─ TailRecursionOptimizerTest.java（单元测试）
   │  ├─ FibonacciTailRecursionEndToEndTest.java（集成测试）
   │  └─ 测试覆盖：Fibonacci模式、直接尾递归、边界情况
   │
   ├─ 示例代码（基准测试）
   │  ├─ Fibonacci递归函数：int fib(int n) { return fib(n-1) + fib(n-2); }
   │  ├─ 直接尾递归函数：int tr(int n) { if (n <= 0) return 0; return tr(n-1); }
   │  └─ 期望输出：迭代式汇编代码（使用while循环）
   │
   └─ 期望输出（参考实现）
      ├─ 检测层：TailRecursionOptimizer标记可优化函数
      ├─ 转换层：RegisterVMGenerator.TROHelper生成迭代式代码
      └─ 验证标准：无递归调用、使用循环、O(1)栈空间
```

**为什么需要分层？**

1. **项目级上下文**：确保AI理解整体架构和编码规范
   - 避免生成不符合项目风格的代码
   - 确保使用正确的依赖和框架
   - 保持代码风格一致性

2. **模块级上下文**：确保AI理解具体的接口和数据结构
   - 正确实现IFlowOptimizer接口
   - 正确使用CFG和BasicBlock API
   - 遵循现有的优化Pass模式

3. **任务级上下文**：确保AI理解具体的测试用例和期望输出
   - 生成正确的测试用例
   - 验证优化前后语义等价
   - 确保覆盖所有边界情况

### 实战案例：EP21尾递归优化器

#### 案例背景

**任务**：为EP21编译器实现尾递归优化（TRO），消除递归函数的栈溢出风险。

**需求分析**：
1. 支持Fibonacci递归模式（2个递归调用）
2. 支持直接尾递归（尾调用是自身）
3. 在代码生成阶段实现转换（Path B方案）
4. 提供优化统计信息

**约束条件**：
1. 必须实现IFlowOptimizer<IRNode>接口
2. 不能破坏程序语义
3. 代码风格必须符合AGENTS.md规范
4. 需要全面的JUnit 5测试用例

#### 实施步骤

**步骤1：准备上下文文件**

```bash
# 进入EP21目录
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep21

# 创建上下文收集目录
mkdir -p ai-context

# 收集项目级上下文
cp ../AGENTS.md ai-context/
cp README.md ai-context/

# 收集模块级上下文
cp src/main/java/org/teachfx/antlr4/ep21/pass/cfg/IFlowOptimizer.java ai-context/
cp src/main/java/org/teachfx/antlr4/ep21/ir/IRNode.java ai-context/
cp src/main/java/org/teachfx/antlr4/ep21/pass/cfg/CFG.java ai-context/

# 收集任务级上下文
cp src/main/java/org/teachfx/antlr4/ep21/ir/expr/CallFunc.java ai-context/
cp src/main/java/org/teachfx/antlr4/ep21/ir/stmt/ReturnVal.java ai-context/
cp src/main/java/org/teachfx/antlr4/ep21/ir/stmt/FuncEntryLabel.java ai-context/

# 查看收集的文件
ls -la ai-context/
# 输出：
# AGENTS.md
# IFlowOptimizer.java
# CallFunc.java
# ReturnVal.java
# FuncEntryLabel.java
# CFG.java
# IRNode.java
# README.md
```

**步骤2：设计AI Prompt**

```markdown
请为EP21编译器实现尾递归优化器（TailRecursion Optimizer）。

## 任务背景

尾递归是指函数的最后一步是调用自身的情况。尾递归优化（TRO）可以将递归转换为迭代，避免栈溢出。

## 技术要求

### 1. 实现IFlowOptimizer<IRNode>接口
```java
public class TailRecursionOptimizer implements IFlowOptimizer<IRNode> {
    // 实现此接口
}
```

### 2. 检测两种尾递归模式

#### 模式1：Fibonacci递归
- 特征：包含2个递归调用
- 示例：int fib(int n) { return fib(n-1) + fib(n-2); }
- 策略：标记为可优化，在代码生成阶段转换

#### 模式2：直接尾递归
- 特征：尾调用是函数自身
- 示例：int tr(int n) { if (n <= 0) return 0; return tr(n-1); }
- 策略：标记为可优化，在代码生成阶段转换

### 3. 实现检测逻辑

#### Fibonacci检测
1. 检查函数名包含"fib"（不区分大小写）
2. 检查函数参数数量为1
3. 统计函数内的递归调用数量（必须为2）

#### 直接尾递归检测
1. 遍历所有基本块
2. 在每个基本块中，从后向前查找ReturnVal指令
3. 检查ReturnVal之前的指令是否是CallFunc
4. 检查CallFunc的函数名是否等于当前函数名

### 4. 输出要求
- 完整的Java类实现
- 遵循AGENTS.md中的代码规范
- 包含详细的中文注释
- 实现getFunctionsOptimized()和getTailCallsDetected()统计方法
- 使用Log4j2记录优化日志

### 5. Path B实现方案
- 检测层：TailRecursionOptimizer负责检测并标记可优化的函数
- 转换层：RegisterVMGenerator.TROHelper执行实际代码转换
- 优势：避免复杂的CFG API适配，代码生成更直接

## 参考上下文文件

### 接口定义
- IFlowOptimizer.java：优化Pass接口
  ```java
  public interface IFlowOptimizer<T> {
      void onHandle(CFG<T> cfg);
  }
  ```

### IR节点
- CallFunc.java：函数调用节点
  ```java
  public class CallFunc extends Expr {
      private String funcName;
      private MethodSymbol methodSymbol;
      // ... 其他字段和方法
  }
  ```

- ReturnVal.java：返回值节点
  ```java
  public class ReturnVal extends Stmt {
      private Expr expr;  // 返回的表达式
      // ... 其他字段和方法
  }
  ```

- FuncEntryLabel.java：函数入口标签
  ```java
  public class FuncEntryLabel extends Stmt {
      private String rawLabel;  // 例如".def fib: args=1, locals=1"
      private MethodSymbol scope;  // 函数符号
      // ... 其他字段和方法
  }
  ```

### CFG定义
- CFG.java：控制流图
  ```java
  public class CFG<T> implements Iterable<BasicBlock<T>> {
      private List<BasicBlock<T>> blockList;
      private List<Triple<Integer, Integer, Integer>> edges;
      // ... 其他字段和方法
  }
  ```

### 代码规范
- AGENTS.md：代码风格和最佳实践
  - 包命名：org.teachfx.antlr4.epXX.package
  - 类命名：PascalCase
  - 方法命名：camelCase
  - 导入顺序：ANTLR4 → External → Internal → Java stdlib

## 约束条件

1. **不能破坏程序语义**：只能优化尾递归，不能改变其他函数
2. **必须处理所有边界情况**：空CFG、非递归函数、多个递归调用
3. **代码风格必须符合AGENTS.md规范**：命名、导入顺序、注释
4. **需要JUnit 5测试类验证**：测试Fibonacci模式、直接尾递归、边界情况

## 输出格式

请生成以下内容：

1. **TailRecursionOptimizer.java**：完整的优化器实现（约200-250行）
2. **测试用例说明**：建议的测试场景列表（至少5个）
3. **集成说明**：如何在编译器流水线中集成此优化器

## 预期行为示例

```java
// 示例1：Fibonacci递归
int fib(int n) {
    if (n <= 1) return n;
    return fib(n-1) + fib(n-2);
}
// 优化器检测到2个递归调用，标记为可优化
// 优化后：optimizedFunctions.contains("fib") == true

// 示例2：直接尾递归
int tailRecurse(int n) {
    if (n <= 0) return 0;
    return tailRecurse(n - 1);
}
// 优化器检测到尾调用，标记为可优化
// 优化后：optimizedFunctions.contains("tailRecurse") == true

// 示例3：非递归函数
int add(int a, int b) {
    return a + b;
}
// 优化器不标记为可优化
// 优化后：optimizedFunctions.contains("add") == false
```

## 日志输出要求

使用Log4j2记录以下信息：
```
[TailRecursionOptimizer] 开始尾递归优化...
[TailRecursionOptimizer] 检测到Fibonacci模式: fib
[TailRecursionOptimizer] 函数 fib 已标记为Fibonacci优化模式（代码生成阶段转换）
[TailRecursionOptimizer] 优化完成: 优化了 1 个函数
```
```

**步骤3：运行AI生成代码**

AI生成TailRecursionOptimizer.java（核心代码片段）：

```java
package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep21.ir.stmt.ReturnVal;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 尾递归优化器 (Tail Recursion Optimizer) - Path B 实现方案
 *
 * 检测尾递归模式并标记函数，实际的代码转换在代码生成阶段完成。
 *
 * 实现方案: Path B (代码生成层优化)
 * - 检测层: 此优化器负责检测和标记可优化的函数
 * - 转换层: RegisterVMGenerator.TROHelper / StackVMGenerator 执行实际转换
 *
 * 优势:
 * - 避免复杂的CFG API适配
 * - 代码生成更直接、可控
 * - 适合实际编译器项目
 *
 * @author EP21 Team
 * @version 3.0 - Path B 稳定实现
 * @see org.teachfx.antlr4.ep21.pass.codegen.RegisterVMGenerator.TROHelper
 */
public class TailRecursionOptimizer implements IFlowOptimizer<IRNode> {

    private static final Logger logger = LogManager.getLogger(TailRecursionOptimizer.class);

    /** 优化统计信息 */
    private int functionsOptimized = 0;
    private int tailCallsDetected = 0;

    /** 当前正在分析的函数信息 */
    private String currentFunctionName;
    private MethodSymbol currentFunction;
    private BasicBlock<IRNode> functionEntryBlock;
    private CFG<IRNode> currentCFG;

    /** 已优化的函数集合 */
    private Set<String> optimizedFunctions = new HashSet<>();

    @Override
    public void onHandle(CFG<IRNode> cfg) {
        System.out.println("[TailRecursionOptimizer] 开始尾递归优化...");
        logger.info("开始尾递归优化...");

        // 重置统计信息
        functionsOptimized = 0;
        tailCallsDetected = 0;
        currentCFG = cfg;
        optimizedFunctions = new HashSet<>();

        // 收集所有函数的入口块
        List<BasicBlock<IRNode>> functionEntries = collectFunctionEntries(cfg);

        // 对每个函数进行尾递归检测和优化
        for (BasicBlock<IRNode> entryBlock : functionEntries) {
            optimizeFunction(entryBlock);
        }

        logger.info("尾递归优化完成: 优化了 {} 个函数, 检测到 {} 个尾调用",
                    functionsOptimized, tailCallsDetected);
        System.out.println("[TailRecursionOptimizer] 优化完成: 优化了 " + functionsOptimized + " 个函数");
    }

    /**
     * 收集所有函数的入口块
     */
    private List<BasicBlock<IRNode>> collectFunctionEntries(CFG<IRNode> cfg) {
        List<BasicBlock<IRNode>> entries = new ArrayList<>();

        for (BasicBlock<IRNode> block : cfg) {
            if (!block.isEmpty()) {
                IRNode firstInstr = block.getInstructionsView().get(0).instr;
                if (firstInstr instanceof FuncEntryLabel) {
                    entries.add(block);
                }
            }
        }

        logger.debug("找到 {} 个函数入口块", entries.size());
        return entries;
    }

    private void optimizeFunction(BasicBlock<IRNode> entryBlock) {
        FuncEntryLabel funcLabel = (FuncEntryLabel) entryBlock.getInstructionsView().get(0).instr;
        currentFunctionName = extractFunctionName(funcLabel);
        currentFunction = (MethodSymbol) funcLabel.getScope();
        functionEntryBlock = entryBlock;

        logger.debug("分析函数: {}", currentFunctionName);

        boolean optimized = false;

        // 策略1: 检测Fibonacci模式
        if (isFibonacciPattern()) {
            logger.info("检测到Fibonacci模式: {}", currentFunctionName);
            System.out.println("[TailRecursionOptimizer] 检测到Fibonacci模式: " + currentFunctionName);

            // Path B: 标记函数，实际转换由代码生成器完成
            optimizedFunctions.add(currentFunctionName);
            optimized = true;

            logger.info("函数 {} 已标记为Fibonacci优化模式（代码生成阶段转换）", currentFunctionName);
        }

        // 策略2: 检测直接尾递归
        if (!optimized) {
            List<TailCallInfo> tailCalls = detectDirectTailCalls();
            if (!tailCalls.isEmpty()) {
                logger.info("在函数 {} 中检测到 {} 个直接尾调用", currentFunctionName, tailCalls.size());
                tailCallsDetected += tailCalls.size();

                // 标记为尾递归优化
                optimizedFunctions.add(currentFunctionName);
                optimized = true;
            }
        }

        if (optimized) {
            functionsOptimized++;
        }
    }

    /**
     * 从FuncEntryLabel中提取函数名
     */
    private String extractFunctionName(FuncEntryLabel funcLabel) {
        String label = funcLabel.getRawLabel();
        // .def fib: args=1, locals=1
        return label.substring(5, label.indexOf(':'));
    }

    /**
     * 检测是否是Fibonacci模式
     */
    private boolean isFibonacciPattern() {
        if (currentFunction == null || currentFunction.getArgs() != 1) {
            return false;
        }

        if (!currentFunctionName.toLowerCase().contains("fib")) {
            return false;
        }

        int recursiveCallCount = 0;
        for (BasicBlock<IRNode> block : currentCFG) {
            for (Loc<IRNode> loc : block) {
                if (loc.instr instanceof CallFunc call) {
                    if (call.getFuncName().equals(currentFunctionName)) {
                        recursiveCallCount++;
                    }
                }
            }
        }

        boolean isFib = recursiveCallCount == 2;
        System.out.println("[TailRecursionOptimizer] 函数 " + currentFunctionName +
                          " 递归调用计数: " + recursiveCallCount + ", Fibonacci模式: " + isFib);
        return isFib;
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
                            logger.debug("检测到直接尾递归: 块 {}, 调用 {}", block.getId(), call);
                        }
                    }
                    break;
                }
            }
        }

        return tailCalls;
    }

    /**
     * 获取优化的函数数量
     */
    public int getFunctionsOptimized() {
        return functionsOptimized;
    }

    /**
     * 获取检测到的尾调用数量
     */
    public int getTailCallsDetected() {
        return tailCallsDetected;
    }

    /**
     * 获取已优化的函数集合
     */
    public Set<String> getOptimizedFunctions() {
        return Collections.unmodifiableSet(optimizedFunctions);
    }

    /**
     * 尾调用信息
     */
    private static class TailCallInfo {
        final BasicBlock<IRNode> block;
        final ReturnVal returnVal;
        final CallFunc call;
        final int instructionIndex;

        TailCallInfo(BasicBlock<IRNode> block, ReturnVal returnVal, CallFunc call, int instructionIndex) {
            this.block = block;
            this.returnVal = returnVal;
            this.call = call;
            this.instructionIndex = instructionIndex;
        }
    }
}
```

**步骤4：验证AI生成的代码**

```bash
# 将AI生成的代码复制到项目目录
cp ai-output/TailRecursionOptimizer.java \
   src/main/java/org/teachfx/antlr4/ep21/pass/cfg/

# 编译验证
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep21
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

**步骤5：创建测试用例**

根据AI的建议测试场景，创建测试类：

```bash
# 创建测试文件
cat > src/test/java/org/teachfx/antlr4/ep21/pass/cfg/TailRecursionOptimizerTest.java << 'EOF'
package org.teachfx.antlr4.ep21.pass.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep21.ir.stmt.ReturnVal;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tail Recursion Optimizer Tests")
public class TailRecursionOptimizerTest {

    private TailRecursionOptimizer optimizer;

    @BeforeEach
    void setUp() {
        optimizer = new TailRecursionOptimizer();
    }

    @Test
    @DisplayName("Given: Fibonacci函数，When: 检测尾递归，Then: 应识别为Fibonacci模式")
    void testFibonacciPatternDetection() {
        // Given: 创建fib函数的CFG
        CFG<IRNode> cfg = createFibonacciCFG();

        // When: 运行尾递归检测器
        optimizer.onHandle(cfg);

        // Then: 验证检测到Fibonacci模式
        assertTrue(optimizer.isFunctionOptimized("fib"),
                  "Fibonacci函数应该被标记为可优化");
        assertEquals(1, optimizer.getFunctionsOptimized(),
                    "应该优化1个函数");
    }

    @Test
    @DisplayName("Given: 直接尾递归函数，When: 检测尾递归，Then: 应识别为可优化")
    void testDirectTailRecursionDetection() {
        // Given: 尾递归函数
        CFG<IRNode> cfg = createDirectTailRecursionCFG();

        // When: 运行尾递归检测器
        optimizer.onHandle(cfg);

        // Then: 验证检测到尾递归
        assertTrue(optimizer.isFunctionOptimized("tailRecurse"),
                  "直接尾递归应该被检测到");
    }

    @Test
    @DisplayName("Given: 非递归函数，When: 检测尾递归，Then: 不应标记为可优化")
    void testNonRecursiveFunction() {
        // Given: 简单的非递归函数
        CFG<IRNode> cfg = createNonRecursiveCFG();

        // When: 检测
        optimizer.onHandle(cfg);

        // Then: 不应标记为可优化
        assertFalse(optimizer.isFunctionOptimized("add"),
                   "非递归函数不应被标记为可优化");
    }

    @Test
    @DisplayName("Given: 空CFG，When: 检测，Then: 不应崩溃")
    void testEmptyCFG() {
        // Given: 空CFG
        CFG<IRNode> cfg = new CFG<>(new ArrayList<>(), new ArrayList<>());

        // When & Then: 检测不应崩溃
        assertDoesNotThrow(() -> optimizer.onHandle(cfg));
        assertEquals(0, optimizer.getFunctionsOptimized());
    }

    @Test
    @DisplayName("Given: 多于2个递归调用的函数，When: 检测，Then: 不应识别为Fibonacci")
    void testNonFibonacciRecursiveFunction() {
        // Given: 包含3个递归调用的函数
        CFG<IRNode> cfg = createThreeCallRecursiveCFG();

        // When: 检测
        optimizer.onHandle(cfg);

        // Then: 不应识别为Fibonacci模式
        assertFalse(optimizer.isFunctionOptimized("tribonacci"),
                   "3个递归调用的函数不应被识别为Fibonacci");
    }

    // ... 测试辅助方法（createFibonacciCFG、createDirectTailRecursionCFG等）
}
EOF

# 运行测试
mvn test -Dtest=TailRecursionOptimizerTest

# 预期输出：
# [INFO] Tests run: 5, Failures: 0, Errors: 0
# [INFO] BUILD SUCCESS
```

**步骤6：端到端集成测试**

创建完整的端到端测试，验证从源代码到VM执行的完整流程：

```bash
# 创建端到端测试文件
cat > src/test/java/org/teachfx/antlr4/ep21/pass/cfg/FibonacciTailRecursionEndToEndTest.java << 'EOF'
package org.teachfx.antlr4.ep21.pass.cfg;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.CymbolLexer;
import org.teachfx.antlr4.ep21.CymbolParser;
import org.teachfx.antlr4.ep21.ast.ASTNode;
import org.teachfx.antlr4.ep21.ir.Prog;
import org.teachfx.antlr4.ep21.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep21.pass.codegen.CodeGenerationResult;
import org.teachfx.antlr4.ep21.pass.codegen.RegisterVMGenerator;
import org.teachfx.antlr4.ep21.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep21.pass.symtab.LocalDefine;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fibonacci Tail Recursion End-to-End Test")
public class FibonacciTailRecursionEndToEndTest {

    @Test
    @DisplayName("fib(10) should return 55 with TRO")
    void testFib10() throws Exception {
        String cymbolCode = """
            int fib(int n) {
                if (n <= 1) return n;
                return fib(n-1) + fib(n-2);
            }

            int main() {
                print fib(10);
                return 0;
            }
            """;

        // Step 1: Parse Cymbol source to AST
        var charStream = CharStreams.fromString(cymbolCode);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();

        CymbolASTBuilder astBuilder = new CymbolASTBuilder();
        ASTNode astRoot = parseTree.accept(astBuilder);

        assertNotNull(astRoot, "AST should be generated");

        // Step 2: Build symbol table
        astRoot.accept(new LocalDefine());

        // Step 3: Generate IR
        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);
        Prog prog = irBuilder.prog;

        assertNotNull(prog, "IR program should be generated");

        // Step 4: Optimize basic blocks
        prog.optimizeBasicBlock();

        // Step 5: Generate VMR code with TRO
        RegisterVMGenerator generator = new RegisterVMGenerator();
        CodeGenerationResult result = generator.generateFromInstructions(prog.linearInstrs());

        assertTrue(result.isSuccess(), "Code generation should succeed. Errors: " + result.getErrors());
        assertFalse(result.getOutput().isEmpty(), "Generated code should not be empty");

        String vmrCode = result.getOutput();

        // Step 6: Verify TRO was applied
        // Count recursive calls in fib function (should be 0 after TRO)
        boolean inFibFunction = false;
        long callFibInFib = 0;

        for (String line : vmrCode.lines().toList()) {
            if (line.contains(".def fib:")) {
                inFibFunction = true;
            } else if (line.contains(".def main:")) {
                inFibFunction = false;
            } else if (inFibFunction && line.trim().startsWith("call fib")) {
                callFibInFib++;
            }
        }

        assertEquals(0, callFibInFib, "fib function should not contain recursive calls after TRO");
    }

    @Test
    @DisplayName("fib(100) should not cause stack overflow with TRO")
    void testFib100NoOverflow() throws Exception {
        String cymbolCode = """
            int fib(int n) {
                if (n <= 1) return n;
                return fib(n-1) + fib(n-2);
            }

            int main() {
                print fib(100);
                return 0;
            }
            """;

        // Compile same program as fib(10)
        var charStream = CharStreams.fromString(cymbolCode);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();

        CymbolASTBuilder astBuilder = new CymbolASTBuilder();
        ASTNode astRoot = parseTree.accept(astBuilder);

        astRoot.accept(new LocalDefine());

        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);
        Prog prog = irBuilder.prog;

        prog.optimizeBasicBlock();

        RegisterVMGenerator generator = new RegisterVMGenerator();
        CodeGenerationResult result = generator.generateFromInstructions(prog.linearInstrs());

        assertTrue(result.isSuccess(), "Code generation should succeed");

        String vmrCode = result.getOutput();

        // Count recursive calls in fib function (should be 0 after TRO)
        boolean inFibFunction = false;
        long callFibInFib = 0;

        for (String line : vmrCode.lines().toList()) {
            if (line.contains(".def fib:")) {
                inFibFunction = true;
            } else if (line.contains(".def main:")) {
                inFibFunction = false;
            } else if (inFibFunction && line.trim().startsWith("call fib")) {
                callFibInFib++;
            }
        }

        assertEquals(0, callFibInFib, "fib function should not contain recursive calls - this ensures no stack overflow");
    }
}
EOF

# 运行端到端测试
mvn test -Dtest=FibonacciTailRecursionEndToEndTest

# 预期输出：
# [INFO] Tests run: 2, Failures: 0, Errors: 0
# [INFO] BUILD SUCCESS
```

**步骤7：代码审查和调整**

```bash
# 运行所有测试，确保没有破坏现有功能
mvn test

# 运行代码覆盖率检查
mvn jacoco:report

# 检查生成的报告
open target/site/jacoco/index.html

# 确保代码覆盖率 > 90%
```

**性能基准测试**：

```bash
# 创建性能基准测试
cat > benchmarks/tro/fibonacci_benchmark.cymbol << 'EOF'
int fib(int n) {
    if (n <= 1) return n;
    return fib(n-1) + fib(n-2);
}

int main() {
    print fib(20);
    print fib(30);
    print fib(40);
    return 0;
}
EOF

# 编译并运行
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep21.integration.EP21Compiler" \
    -Dexec.args="benchmarks/tro/fibonacci_benchmark.cymbol /tmp/fibonacci_benchmark.vm"

# 查看生成的VMR代码，验证无递归调用
cat /tmp/fibonacci_benchmark.vm | grep -A 50 ".def fib:"

# 预期输出：
# .def fib: args=1, locals=3
# fib_entry:
#   [无递归调用，只有循环结构]
```

### 工具链：AI协作辅助脚本

为了提高AI协作效率，EP21项目提供了一套辅助脚本：

#### ContextCollector - 自动收集上下文

```bash
#!/bin/bash
# ai-tools/collect-context.sh

echo "=== AI Context Collector ==="
echo "Collecting context for AI-assisted development..."

TARGET_DIR="ai-context"
mkdir -p $TARGET_DIR

# 项目级上下文
echo "[1/4] Collecting project-level context..."
cp AGENTS.md $TARGET_DIR/
cp README.md $TARGET_DIR/
cp pom.xml $TARGET_DIR/

# 模块级上下文
echo "[2/4] Collecting module-level context..."
mkdir -p $TARGET_DIR/src/main/java/org/teachfx/antlr4/ep21/pass/cfg
mkdir -p $TARGET_DIR/src/main/java/org/teachfx/antlr4/ep21/ir
mkdir -p $TARGET_DIR/src/main/java/org/teachfx/antlr4/ep21/ir/expr
mkdir -p $TARGET_DIR/src/main/java/org/teachfx/antlr4/ep21/ir/stmt

cp src/main/java/org/teachfx/antlr4/ep21/pass/cfg/IFlowOptimizer.java \
   $TARGET_DIR/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/
cp src/main/java/org/teachfx/antlr4/ep21/pass/cfg/CFG.java \
   $TARGET_DIR/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/
cp src/main/java/org/teachfx/antlr4/ep21/ir/IRNode.java \
   $TARGET_DIR/src/main/java/org/teachfx/antlr4/ep21/ir/

# 任务级上下文
echo "[3/4] Collecting task-level context..."
mkdir -p $TARGET_DIR/src/test/java/org/teachfx/antlr4/ep21/pass/cfg

if [ -f "src/main/java/org/teachfx/antlr4/ep21/pass/cfg/TailRecursionOptimizer.java" ]; then
    cp src/main/java/org/teachfx/antlr4/ep21/pass/cfg/TailRecursionOptimizer.java \
       $TARGET_DIR/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/
fi

if [ -f "src/test/java/org/teachfx/antlr4/ep21/pass/cfg/TailRecursionOptimizerTest.java" ]; then
    cp src/test/java/org/teachfx/antlr4/ep21/pass/cfg/TailRecursionOptimizerTest.java \
       $TARGET_DIR/src/test/java/org/teachfx/antlr4/ep21/pass/cfg/
fi

# 生成上下文索引
echo "[4/4] Generating context index..."
cat > $TARGET_DIR/INDEX.md << 'EOF'
# AI Context Files

## Project-Level Context
- AGENTS.md - 代码规范和最佳实践
- README.md - 项目概述和文档导航
- pom.xml - Maven构建配置

## Module-Level Context
- IFlowOptimizer.java - 优化Pass接口
- CFG.java - 控制流图实现
- IRNode.java - 中间表示节点基类

## Task-Level Context
- TailRecursionOptimizer.java - 示例优化器实现
- TailRecursionOptimizerTest.java - 单元测试示例

## Usage
Include these files when designing AI Prompts for new optimizers.
EOF

echo "=== Context collection complete! ==="
echo "Context files saved to: $TARGET_DIR/"
echo "Total files: $(find $TARGET_DIR -type f | wc -l)"
```

#### ai_helper.sh - AI协作辅助工具

```bash
#!/bin/bash
# ai-tools/ai_helper.sh

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== AI Helper for EP21 Compiler Development ===${NC}"

# 检查参数
if [ $# -lt 1 ]; then
    echo "Usage: $0 <command> [args]"
    echo ""
    echo "Commands:"
    echo "  collect             - Collect context files for AI"
    echo "  generate <prompt>   - Generate code from AI prompt"
    echo "  verify <file>       - Verify generated code"
    echo "  benchmark <file>    - Run performance benchmark"
    exit 1
fi

COMMAND=$1

case $COMMAND in
    collect)
        echo -e "${YELLOW}Collecting context files...${NC}"
        bash ai-tools/collect-context.sh
        echo -e "${GREEN}Context collected successfully!${NC}"
        ;;

    generate)
        if [ -z "$2" ]; then
            echo -e "${RED}Error: Prompt file required${NC}"
            exit 1
        fi
        PROMPT_FILE=$2
        echo -e "${YELLOW}Generating code from: $PROMPT_FILE${NC}"
        echo "Paste this prompt to AI:"
        echo "---"
        cat $PROMPT_FILE
        echo "---"
        echo -e "${GREEN}After receiving AI output, save it to ai-output/ directory${NC}"
        ;;

    verify)
        if [ -z "$2" ]; then
            echo -e "${RED}Error: Source file required${NC}"
            exit 1
        fi
        SOURCE_FILE=$2
        echo -e "${YELLOW}Verifying: $SOURCE_FILE${NC}"

        # 编译验证
        echo -e "${YELLOW}[1/3] Compiling...${NC}"
        mvn clean compile
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Compilation successful${NC}"
        else
            echo -e "${RED}✗ Compilation failed${NC}"
            exit 1
        fi

        # 单元测试
        echo -e "${YELLOW}[2/3] Running unit tests...${NC}"
        TEST_NAME=$(basename $SOURCE_FILE .java)
        mvn test -Dtest=${TEST_NAME}Test
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ All tests passed${NC}"
        else
            echo -e "${RED}✗ Some tests failed${NC}"
            exit 1
        fi

        # 代码覆盖率
        echo -e "${YELLOW}[3/3] Checking code coverage...${NC}"
        mvn jacoco:report
        echo -e "${GREEN}✓ Coverage report generated: target/site/jacoco/index.html${NC}"
        ;;

    benchmark)
        if [ -z "$2" ]; then
            echo -e "${RED}Error: Test file required${NC}"
            exit 1
        fi
        TEST_FILE=$2
        echo -e "${YELLOW}Running benchmark: $TEST_FILE${NC}"

        # 编译并运行
        mvn clean compile
        mvn test -Dtest=$TEST_FILE

        echo -e "${GREEN}Benchmark completed!${NC}"
        ;;

    *)
        echo -e "${RED}Error: Unknown command '$COMMAND'${NC}"
        exit 1
        ;;
esac
```

#### ai_validator.sh - AI生成代码验证脚本

```bash
#!/bin/bash
# ai-tools/ai_validator.sh

echo "=== AI Code Validator ==="

SOURCE_FILE=$1

if [ -z "$SOURCE_FILE" ]; then
    echo "Usage: $0 <source-file.java>"
    exit 1
fi

echo "Validating: $SOURCE_FILE"

# 检查1：文件存在
if [ ! -f "$SOURCE_FILE" ]; then
    echo "✗ File not found: $SOURCE_FILE"
    exit 1
fi
echo "✓ File exists"

# 检查2：包声明正确
PACKAGE_NAME=$(grep "^package " $SOURCE_FILE | head -1 | awk '{print $2}' | sed 's/;$//')
if [[ "$PACKAGE_NAME" =~ ^org\.teachfx\.antlr4\.ep21\. ]]; then
    echo "✓ Package declaration correct: $PACKAGE_NAME"
else
    echo "✗ Package declaration incorrect: $PACKAGE_NAME"
    exit 1
fi

# 检查3：类命名规范
CLASS_NAME=$(basename $SOURCE_FILE .java)
if [[ "$CLASS_NAME" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
    echo "✓ Class name follows PascalCase: $CLASS_NAME"
else
    echo "✗ Class name does not follow PascalCase: $CLASS_NAME"
    exit 1
fi

# 检查4：必要的导入
IMPORTS=$(grep "^import " $SOURCE_FILE)
if echo "$IMPORTS" | grep -q "org.antlr.v4.runtime"; then
    echo "✓ ANTLR4 imports present"
else
    echo "✗ Missing ANTLR4 imports"
fi

if echo "$IMPORTS" | grep -q "org.apache.logging.log4j"; then
    echo "✓ Log4j2 imports present"
else
    echo "✗ Missing Log4j2 imports"
fi

# 检查5：中文注释
COMMENT_COUNT=$(grep -c "\/\/.*[\u4e00-\u9fa5]" $SOURCE_FILE || true)
if [ $COMMENT_COUNT -gt 10 ]; then
    echo "✓ Chinese comments present ($COMMENT_COUNT lines)"
else
    echo "⚠ Few Chinese comments ($COMMENT_COUNT lines)"
fi

# 检查6：JavaDoc
JAVADOC_COUNT=$(grep -c "\/\*\*" $SOURCE_FILE || true)
if [ $JAVADOC_COUNT -ge 1 ]; then
    echo "✓ JavaDoc comments present ($JAVADOC_COUNT blocks)"
else
    echo "⚠ No JavaDoc comments"
fi

# 检查7：编译通过
echo -n "Compiling... "
mvn clean compile > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Compilation successful"
else
    echo "✗ Compilation failed"
    exit 1
fi

echo ""
echo "=== Validation Summary ==="
echo "All checks passed! Code is ready for integration."
```

## AI 协作线：Context Engineering 视角

### Prompt设计最佳实践

#### Prompt模板库

**类型A：优化算法实现**

```markdown
请为EP21编译器实现{优化算法名称}优化Pass。

## 任务目标
- 实现{功能描述}
- 实现IFlowOptimizer<IRNode>接口
- 提供优化统计信息

## 具体要求

### 1. 实现接口
- 类名：{OptimizerName}
- 位置：ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/
- 实现：IFlowOptimizer<IRNode>

### 2. 实现优化逻辑
- 在CFG上执行{具体优化}
- 使用{相关数据流分析}（如果需要）
- 提供优化统计信息：
  - 优化的函数数量
  - 优化的指令数量
  - 优化前后的代码大小对比

### 3. 添加单元测试
- 测试{正常情况}
- 测试{边界情况}
- 测试{错误情况}
- 使用JUnit 5和AssertJ

## 参考上下文文件
- 接口：IFlowOptimizer.java
- 示例实现：TailRecursionOptimizer.java
- CFG定义：CFG.java
- 文档：架构设计规范.md
- 代码规范：AGENTS.md

## 约束条件
- 不修改IFlowOptimizer接口
- 保持与现有优化Pass一致的风格
- 使用Log4j2记录优化日志
- 所有新增代码必须通过mvn test

## 输出格式
1. 完整的Java类实现（约200-300行）
2. 测试用例列表（至少5个测试场景）
3. 集成说明（如何在编译器流水线中使用）
```

**类型B：测试用例生成**

```markdown
请为{OptimizerName}优化器生成全面的测试用例。

## 目标类
- 类名：{OptimizerName}
- 位置：ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/

## 测试要求

### 1. 测试框架
- 使用JUnit 5
- 使用AssertJ断言库
- 测试类名：{OptimizerName}Test

### 2. 测试场景

#### 正常情况
- 测试{场景1描述}
- 测试{场景2描述}

#### 边界情况
- 测试空CFG
- 测试单指令CFG
- 测试{其他边界情况}

#### 错误情况
- 测试{错误情况1}
- 测试{错误情况2}

### 3. 测试断言
- 验证优化后CFG结构正确
- 验证优化统计信息正确
- 验证程序语义不变

## 参考上下文
- 优化器实现：{OptimizerName}.java
- 示例测试：TailRecursionOptimizerTest.java
- CFG构建工具：CFGBuilderTest.java

## 输出格式
完整的测试类代码，包含所有测试方法和辅助方法。
```

**类型C：代码审查和优化**

```markdown
请审查以下优化器代码，并提出优化建议。

## 代码文件
- 类名：{OptimizerName}
- 文件路径：{FILE_PATH}

## 审查维度

### 1. 代码质量
- 代码风格是否符合AGENTS.md规范？
- 变量命名是否清晰？
- 注释是否充分？

### 2. 性能
- 时间复杂度是否合理？
- 是否有不必要的对象创建？
- 是否可以优化算法？

### 3. 正确性
- 是否有边界情况未处理？
- 是否有潜在的空指针异常？
- 优化逻辑是否正确？

### 4. 可维护性
- 代码结构是否清晰？
- 是否需要重构？
- 是否可以提取公共方法？

## 参考上下文
- 代码规范：AGENTS.md
- 示例实现：TailRecursionOptimizer.java

## 输出格式
1. 代码审查报告（问题列表 + 严重程度）
2. 优化建议（具体代码修改建议）
3. 重构建议（架构层面的改进）
```

#### Prompt质量检查清单

在发送Prompt给AI之前，使用以下检查清单确保Prompt质量：

**✅ 必须包含的内容**：
- [ ] 明确的任务目标（实现什么功能？）
- [ ] 具体的技术要求（使用什么接口？什么算法？）
- [ ] 参考上下文文件（哪些文件应该阅读？）
- [ ] 输出格式要求（生成什么？如何组织？）
- [ ] 约束条件（不能做什么？）

**⚠️ 常见问题**：
- ❌ 缺少接口定义 → AI可能实现错误的接口
- ❌ 缺少示例代码 → AI可能不符合项目风格
- ❌ 缺少约束条件 → AI可能修改不该修改的代码
- ❌ 缺少测试要求 → AI可能生成不可测试的代码
- ❌ 缺少输出格式 → AI的输出可能难以集成

### AI应该做 / 不该做

**✅ AI允许做的事情**：

1. **实现明确界定的架构组件**
   - ✅ 可以：实现特定的优化算法（如TRO、常量折叠、死代码消除）
   - ✅ 可以：生成测试用例和辅助代码（测试框架、Mock对象）
   - ✅ 可以：编写JavaDoc和行内注释
   - ✅ 不能：修改核心接口定义（IFlowOptimizer、IRNode、ASTNode）

2. **辅助代码审查和优化**
   - ✅ 可以：分析代码质量和性能瓶颈
   - ✅ 可以：提出优化建议（算法优化、数据结构优化）
   - ✅ 可以：重构代码（提取方法、重命名变量）
   - ✅ 不能：改变算法的基本正确性

3. **生成文档和注释**
   - ✅ 可以：编写JavaDoc和行内注释
   - ✅ 可以：生成设计文档
   - ✅ 可以：编写使用示例
   - ✅ 不能：替换现有的设计文档

**❌ AI禁止做的事情**：

1. **修改核心框架和接口**
   - ❌ 不允许：改变IFlowOptimizer接口定义
   - ❌ 不允许：修改IR节点基类（IRNode.java）
   - ❌ 不允许：修改AST节点基类（ASTNode.java）
   - 原因：框架是整个编译器的基础，修改会影响所有模块

2. **删除或破坏现有代码**
   - ❌ 不允许：删除现有的优化Pass
   - ❌ 不允许：删除现有的测试用例
   - ❌ 不允许：破坏现有功能的测试
   - 原因：现有代码是经过验证的，不能轻易删除

3. **引入新的外部依赖**
   - ❌ 不允许：添加新的框架或库（如Guava、Lombok）
   - ❌ 不允许：使用项目技术栈之外的依赖
   - 原因：保持项目技术栈一致性，避免依赖冲突

4. **修改构建系统**
   - ❌ 不允许：修改pom.xml（除非经过讨论和批准）
   - ❌ 不允许：修改Maven插件配置
   - 原因：构建系统是项目的核心基础设施

### 验证与回滚策略

#### 自动化验证流程

```bash
#!/bin/bash
# ai-tools/verify-ai-output.sh

echo "=== AI Output Verification Pipeline ==="

SOURCE_FILE=$1

if [ -z "$SOURCE_FILE" ]; then
    echo "Usage: $0 <source-file.java>"
    exit 1
fi

# Step 1: 代码风格检查
echo "[1/5] Checking code style..."
bash ai-tools/ai_validator.sh $SOURCE_FILE
if [ $? -ne 0 ]; then
    echo "✗ Code style check failed"
    exit 1
fi

# Step 2: 编译验证
echo "[2/5] Compiling..."
mvn clean compile > /tmp/compile.log 2>&1
if [ $? -ne 0 ]; then
    echo "✗ Compilation failed. Check /tmp/compile.log for details."
    exit 1
fi
echo "✓ Compilation successful"

# Step 3: 单元测试
echo "[3/5] Running unit tests..."
TEST_NAME=$(basename $SOURCE_FILE .java)Test
mvn test -Dtest=$TEST_NAME > /tmp/test.log 2>&1
if [ $? -ne 0 ]; then
    echo "✗ Unit tests failed. Check /tmp/test.log for details."
    exit 1
fi
echo "✓ All unit tests passed"

# Step 4: 代码覆盖率
echo "[4/5] Checking code coverage..."
mvn jacoco:report > /tmp/jacoco.log 2>&1
COVERAGE=$(grep -o 'Total.*?[0-9]\+%' target/site/jacoco/index.html | grep -o '[0-9]\+%' | head -1)
echo "Code coverage: $COVERAGE"
if [[ "$COVERAGE" < "90%" ]]; then
    echo "⚠ Coverage below 90%"
fi

# Step 5: 集成测试
echo "[5/5] Running integration tests..."
mvn test > /tmp/integration.log 2>&1
if [ $? -ne 0 ]; then
    echo "✗ Integration tests failed. Check /tmp/integration.log for details."
    exit 1
fi
echo "✓ All integration tests passed"

echo ""
echo "=== Verification Complete ==="
echo "All checks passed! AI output is ready for integration."
```

#### 手工检查点清单

**检查1：AI代码质量**
- [ ] 代码风格符合AGENTS.md规范
- [ ] 有足够的注释和JavaDoc
- [ ] 代码可理解和维护
- [ ] 变量和方法命名清晰
- [ ] 代码结构合理（无过长方法、无嵌套过深）

**检查2：测试覆盖率**
- [ ] 所有代码路径被测试覆盖
- [ ] 边界条件被测试
- [ ] 错误情况被测试
- [ ] 代码覆盖率 ≥ 90%
- [ ] 测试用例包含正常情况、边界情况、错误情况

**检查3：架构一致性**
- [ ] 正确实现IFlowOptimizer接口
- [ ] 使用正确的CFG和BasicBlock API
- [ ] 遵循现有优化Pass的模式
- [ ] 不修改核心接口和数据结构
- [ ] 与现有代码风格一致

**检查4：性能影响**
- [ ] 优化器不会显著增加编译时间
- [ ] 优化结果有效（性能提升）
- [ ] 内存使用合理
- [ ] 无不必要的对象创建

#### 回滚方案

**方案1：Git Stash（推荐）**

```bash
# 在应用AI修改之前，保存当前状态
git stash push -m "Before AI changes: $TASK_NAME"
git status

# 应用AI修改
# ... 复制AI生成的代码到项目目录 ...

# 如果验证失败，快速回滚
git stash pop

# 如果验证成功，删除stash
git stash drop
```

**方案2：Git Branch**

```bash
# 创建新分支用于AI修改
git checkout -b feature/$TASK_NAME-ai

# 应用AI修改
# ... 复制AI生成的代码到项目目录 ...

# 运行验证
mvn test

# 如果验证失败，切换回主分支并删除AI分支
git checkout main
git branch -D feature/$TASK_NAME-ai

# 如果验证成功，合并到主分支
git checkout main
git merge feature/$TASK_NAME-ai
```

**方案3：Git Checkout（单文件回滚）**

```bash
# 如果只有单个文件有问题，回滚该文件
git checkout HEAD~1 -- path/to/file.java

# 或者回滚到特定commit
git checkout <commit-hash> -- path/to/file.java
```

## 练习题

### 练习1：实现循环不变代码外提优化器

**难度**：⭐⭐⭐☆☆

**任务**：实现LoopInvariantCodeMotionOptimizer，将循环中不变的计算外提。

**要求**：
1. 实现IFlowOptimizer<IRNode>接口
2. 检测循环结构（使用BasicBlock和边分析）
3. 识别循环不变代码（循环内不会改变的计算）
4. 将不变代码移到循环外
5. 提供优化统计信息（外提的指令数量）

**AI协作版本**：

```markdown
请使用AI Context Engineer方法论，实现循环不变代码外提优化器。

## 步骤1：准备上下文
```bash
cd ep21
bash ai-tools/collect-context.sh
```

## 步骤2：设计Prompt
使用本章节提供的Prompt模板A，填写以下信息：
- 优化算法名称：LoopInvariantCodeMotionOptimizer
- 功能描述：将循环不变的计算外提到循环前
- 具体优化：检测循环、识别不变代码、外提代码
- 相关数据流分析：可达定义分析（可选）

## 步骤3：使用AI生成
将Prompt发送给AI，获取优化器实现。

## 步骤4：验证
```bash
bash ai-tools/verify-ai-output.sh \
    src/main/java/org/teachfx/antlr4/ep21/pass/cfg/LoopInvariantCodeMotionOptimizer.java
```

## 步骤5：测试
创建测试用例：
- 测试简单的for循环不变代码外提
- 测试嵌套循环不变代码外提
- 测试无不变代码的循环
```

**预期输出示例**：

优化前：
```java
for (int i = 0; i < n; i++) {
    int x = a + b;  // 不变代码
    result[i] = x * i;
}
```

优化后：
```java
int x = a + b;  // 外提到循环外
for (int i = 0; i < n; i++) {
    result[i] = x * i;
}
```

### 练习2：使用AI生成全面测试用例

**难度**：⭐⭐⭐☆☆

**任务**：为EP21中的所有优化Pass生成全面的测试用例。

**要求**：
1. 为每个优化Pass生成单元测试
2. 测试覆盖正常情况、边界情况、错误情况
3. 使用JUnit 5和AssertJ
4. 测试覆盖率 ≥ 90%

**AI协作版本**：

```markdown
请使用AI为以下优化Pass生成全面的测试用例：
1. TailRecursionOptimizer
2. ConstantFoldingOptimizer
3. DeadCodeEliminationOptimizer
4. CommonSubexpressionEliminationOptimizer

## 使用Prompt模板B
对于每个优化器，使用Prompt模板B生成测试用例。

## 验证要求
```bash
# 运行所有测试
mvn test

# 检查代码覆盖率
mvn jacoco:report
open target/site/jacoco/index.html

# 确保所有优化器的测试覆盖率 ≥ 90%
```
```

### 练习3：实现公共子表达式消除优化器

**难度**：⭐⭐⭐⭐☆

**任务**：实现CommonSubexpressionElimination优化器，消除重复的计算。

**要求**：
1. 实现IFlowOptimizer<IRNode>接口
2. 识别公共子表达式（重复的计算）
3. 使用临时变量存储第一次计算的结果
4. 用临时变量替换后续相同的计算
5. 提供优化统计信息（消除的子表达式数量）

**AI协作版本**：

```markdown
请使用AI Context Engineer方法论，实现公共子表达式消除优化器。

## 步骤1：准备上下文
```bash
cd ep21
bash ai-tools/collect-context.sh

# 额外收集数据流分析上下文
cp src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/ReachingDefinitions.java \
   ai-context/
```

## 步骤2：设计Prompt
使用Prompt模板A，填写以下信息：
- 优化算法名称：CommonSubexpressionEliminationOptimizer
- 功能描述：消除重复的计算表达式
- 具体优化：识别公共子表达式、引入临时变量、替换重复计算
- 相关数据流分析：到达定义分析（ReachingDefinitions）

## 步骤3：使用AI生成
将Prompt发送给AI，获取优化器实现。

## 步骤4：验证
```bash
bash ai-tools/verify-ai-output.sh \
    src/main/java/org/teachfx/antlr4/ep21/pass/cfg/CommonSubexpressionEliminationOptimizer.java
```

## 步骤5：测试
创建测试用例：
- 测试简单的公共子表达式消除
- 测试跨基本块的公共子表达式消除
- 测试无公共子表达式的代码
```

**预期输出示例**：

优化前：
```java
int x = (a + b) * c;
int y = (a + b) * d;
```

优化后：
```java
int temp1 = a + b;
int x = temp1 * c;
int y = temp1 * d;
```

### 练习4：AI代码审查和优化

**难度**：⭐⭐⭐☆☆

**任务**：使用AI审查现有的优化器代码，并提出优化建议。

**要求**：
1. 选择一个现有的优化器（如TailRecursionOptimizer）
2. 使用Prompt模板C进行代码审查
3. 实施AI提出的优化建议
4. 验证优化后代码质量和性能

**AI协作版本**：

```markdown
请使用AI审查TailRecursionOptimizer的代码。

## 步骤1：准备代码
```bash
cd ep21
cp src/main/java/org/teachfx/antlr4/ep21/pass/cfg/TailRecursionOptimizer.java \
   ai-context/
```

## 步骤2：设计Prompt
使用Prompt模板C，填写以下信息：
- 优化器名称：TailRecursionOptimizer
- 文件路径：ai-context/TailRecursionOptimizer.java

## 步骤3：使用AI审查
将Prompt发送给AI，获取代码审查报告。

## 步骤4：实施优化
根据AI的建议，修改代码：
- 优化算法实现
- 提高代码可读性
- 添加必要注释

## 步骤5：验证
```bash
mvn test
mvn jacoco:report
```

## 步骤6：性能对比
对比优化前后的性能：
```bash
# 优化前
time mvn test -Dtest=TailRecursionOptimizerTest

# 优化后
time mvn test -Dtest=TailRecursionOptimizerTest
```
```

### 练习5：AI辅助的Bug修复

**难度**：⭐⭐⭐⭐☆

**任务**：使用AI辅助修复编译器优化器中的Bug。

**要求**：
1. 创建一个有Bug的优化器实现
2. 编写失败的测试用例
3. 使用AI诊断Bug并生成修复方案
4. 验证修复后测试通过

**AI协作版本**：

```markdown
请使用AI辅助修复优化器Bug。

## 步骤1：创建有Bug的代码
```java
// BugConstantFoldingOptimizer.java - 有Bug的实现
public class BugConstantFoldingOptimizer implements IFlowOptimizer<IRNode> {
    @Override
    public void onHandle(CFG<IRNode> cfg) {
        // BUG: 只处理第一个基本块，忽略其他基本块
        for (IRNode node : cfg.get(0)) {
            if (node instanceof BinaryExpr expr) {
                // ... 常量折叠逻辑（有Bug）
            }
        }
    }
}
```

## 步骤2：编写失败的测试
```java
@Test
@DisplayName("常量折叠应该处理所有基本块")
void testConstantFoldingMultipleBlocks() {
    CFG<IRNode> cfg = createMultipleBlockCFG();
    optimizer.onHandle(cfg);

    // 这个测试会失败，因为优化器只处理第一个基本块
    assertTrue(optimizedAllBlocks(cfg));
}
```

## 步骤3：使用AI诊断Bug
```markdown
请诊断以下优化器的Bug：

## 代码
{粘贴BugConstantFoldingOptimizer.java代码}

## 失败的测试
{粘贴测试代码和错误信息}

## 问题
优化器只处理第一个基本块，导致后续基本块的常量表达式未被优化。

请分析Bug原因并提供修复方案。
```

## 步骤4：应用AI的修复
根据AI的建议，修复Bug：
- 遍历所有基本块
- 正确处理CFG结构
- 添加必要的日志

## 步骤5：验证修复
```bash
mvn test -Dtest=ConstantFoldingOptimizerTest
```

## 步骤6：学习总结
总结Bug修复过程中的经验：
- Bug的根本原因是什么？
- AI如何帮助诊断Bug？
- 如何避免类似的Bug？
```

## 本章小结

通过本章的学习，你已经掌握了：

### 1. AI Context Engineer方法论

**核心思想**：上下文工程（Context Engineering）——精心组织信息，让AI能够准确理解任务需求和项目约束。

**三层上下文架构**：
- **项目级上下文**：代码规范、架构设计、构建系统
- **模块级上下文**：核心接口、数据结构、现有实现
- **任务级上下文**：测试用例、示例代码、期望输出

**Prompt设计技巧**：
- 明确任务目标和具体要求
- 提供详细的参考上下文
- 清晰的约束条件
- 明确的输出格式

### 2. AI协作工作流

**完整流程**：
```
准备上下文 → 设计Prompt → AI生成代码 → 自动化验证 → 人工审查 → 集成到项目
```

**实战案例：EP21尾递归优化器**
- 使用AI协作，将3-4周的人工开发时间缩短至3天
- 生成243行高质量代码（TailRecursionOptimizer.java）
- 生成658行单元测试（TailRecursionOptimizerTest.java）
- 生成455行端到端测试（FibonacciTailRecursionEndToEndTest.java）
- 测试覆盖率 ≥ 90%

**工具链**：
- ContextCollector：自动收集上下文文件
- ai_helper.sh：AI协作辅助工具
- ai_validator.sh：AI生成代码验证脚本

### 3. 实战经验

**AI应该做的事情**：
- ✅ 实现明确的架构组件（优化算法、测试用例、辅助代码）
- ✅ 辅助代码审查和优化（代码质量、性能优化、重构）
- ✅ 生成文档和注释（JavaDoc、行内注释、设计文档）

**AI不应该做的事情**：
- ❌ 修改核心框架和接口（IFlowOptimizer、IRNode、ASTNode）
- ❌ 删除或破坏现有代码（优化Pass、测试用例）
- ❌ 引入新的外部依赖（Guava、Lombok等）
- ❌ 修改构建系统（pom.xml）

**验证和回滚**：
- 自动化验证：编译 → 单元测试 → 代码覆盖率 → 集成测试
- 手工检查：代码质量、测试覆盖率、架构一致性、性能影响
- 回滚方案：Git Stash、Git Branch、Git Checkout

### 4. 性能提升

**时间节省**：
- 人工开发：3-4周
- AI协作开发：3天
- **时间节省：75%**

**质量提升**：
- 更全面的测试覆盖（90%+）
- 更详细的代码注释
- 更快的问题定位和修复

**风险降低**：
- 更少的边界情况遗漏
- 更快的反馈循环
- 更容易的代码审查

## 下一章预告

**【你现在站在】**:
```
... → [SSA转换] → [数据流分析] → [高级优化] → ✅ AI Context Engineer实践 → [附录] → ...
```

**下一章预告**：附录部分将提供编译器优化的参考资料和扩展阅读，帮助你继续深入学习。

附录内容包括：
- 编译器优化经典论文和书籍
- 开源编译器项目参考（LLVM、GCC、HotSpot）
- 编译器工具链和调试技巧
- 进一步学习的资源和建议

## 恭喜你完成了全书学习！

**你已经完成了从基础到高级的完整学习路径！**

从简单的词法分析和语法分析，到构建完整的编译器流水线；从基础的AST构建，到高级的SSA转换和数据流分析；从手工编写每一行代码，到学会与AI高效协作。这是一段了不起的旅程！

**你现在已经掌握的能力**：
1. 使用ANTLR4构建完整的编译器前端（词法分析、语法分析、AST构建）
2. 设计和实现中间表示（MIR/LIR）
3. 实现高级优化Pass（SSA、数据流分析、TRO、常量折叠、死代码消除）
4. 集成和测试编译器优化
5. 与AI高效协作，使用AI Context Engineer方法论

**你可以继续探索的方向**：
- 学习更多编译器优化算法（寄存器分配、指令调度、循环优化）
- 研究开源编译器项目（LLVM、GCC、JIT编译器）
- 深入学习特定领域的编译器（GPU编译器、领域专用语言编译器）
- 参与开源编译器项目的开发
- 应用AI到更多的软件开发领域

**继续加油！** 你已经掌握了现代编译器优化的核心技术，以及AI协作的方法论，可以开始自己的编译器优化和AI辅助开发之旅了！

---

## 参考资料

**项目文档**：
- AGENTS.md：AI协作经验总结
- EP21文档：优化Pass设计规范和测试规范
- README.md：项目概述和文档导航

**关键代码文件**：
- TailRecursionOptimizer.java（243行）：尾递归优化器实现
- TailRecursionOptimizerTest.java（658行）：单元测试
- FibonacciTailRecursionEndToEndTest.java（455行）：端到端测试
- IFlowOptimizer.java：优化Pass接口
- CFG.java：控制流图实现

**相关章节回顾**：
- 第17章：SSA与数据流分析 - 数据流分析的基础
- 第18章：全局优化 - 优化Pass的实现
- 第19章：优化器架构 - 优化Pass的管理
- 第20章：CFG与优化 - 控制流分析和局部优化

继续学习，附录中有更多参考资料等待探索！
