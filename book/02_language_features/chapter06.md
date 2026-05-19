# 第6章：AST 构建与表达式求值

## 1. 本章概述

本章聚焦于**抽象语法树（AST）的构建与求值**，它是编译器流水线中的**前端语义表示**阶段。通过学习本章，你将掌握 AST 与 ParseTree 的区别、访问者模式的应用，以及如何从语法分析阶段过渡到语义分析阶段，这是后续符号解析和类型检查的基础。

【你现在站在哪】:
```
词法分析 → 语法分析（ParseTree） → ✅ **AST 构建** → [符号解析] → [类型检查] → ...
```

## 2. 动机与真实场景

**真实场景：你需要构建一个代码分析工具**

想象你在开发一个代码质量分析平台。用户需要的功能包括：
- 计算复杂表达式的值（如 `2 + 3 * 4 - x`）
- 检测未定义的变量使用
- 提供语法高亮和智能提示
- 实现代码重构建议（如提取公共子表达式）

**具体问题和挑战**

如果直接使用 ANTLR4 生成的 ParseTree，你将面临：
- ParseTree 包含大量语法细节（括号、分号、空白符），干扰语义分析
- 无法方便地添加类型信息、作用域信息等元数据
- 遍历 ParseTree 时需要处理大量的节点类型（每个语法规则对应一个 Context 类）
- 难以实现多次遍历（类型检查、优化、代码生成等需要在不同阶段访问树）

**缺失本章能力的痛点**

1. **语义信息难以添加**：ParseTree 是语法分析器的直接输出，无法在不修改解析器的情况下添加类型、作用域等语义信息
2. **多遍编译无法实现**：现代编译器需要多次遍历中间表示进行类型检查、优化等阶段，ParseTree 不适合这种场景
3. **代码复用困难**：语法规则的变更会影响大量 Context 类，而 AST 节点的设计更加稳定和抽象

**本章将教你如何：**

- 设计清晰的 AST 节点层次结构，去除语法细节，只保留语义信息
- 使用访问者模式将 ParseTree 转换为自定义 AST
- 实现基于 AST 的表达式求值器，理解为什么需要 AST 而不是直接解释 ParseTree
- 为后续的符号解析、类型检查奠定基础

---

## 3. 人类工程师线：技术与实现

### 3.1 核心概念

#### 核心概念 1：AST（抽象语法树）vs ParseTree（解析树）

**通俗解释**：

ParseTree 是语法分析器的**直接产物**，完整记录了程序文本的语法结构，包括所有语法细节（如括号、分号、空格等）。AST 是 ParseTree 的**抽象和简化版本**，只保留程序的核心语义信息，去除冗余的语法细节。

打个比方：
- ParseTree 就像**逐字稿**：完整记录了演讲者说的每一个字、每一次停顿
- AST 就像**要点总结**：只保留演讲的核心观点和逻辑关系

[图1：ParseTree 与 AST 对比]

**ParseTree（包含语法细节）**：
```
compileUnit
    └─ varSlot
        └─ infixExpr (OP_ADD)
            ├─ left: varSlot
            │   └─ infixExpr (OP_MUL)
            │       ├─ left: varSlot
            │       │   └─ numberExpr: NUM('2')
            │       └─ right: varSlot
            │           └─ numberExpr: NUM('3')
            └─ right: varSlot
                └─ numberExpr: NUM('4')
```

**AST（去除语法细节，保留语义）**：
```
AdditionNode
    ├── left: MultiplicationNode
    │   ├── left: NumberNode(2.0)
    │   └── right: NumberNode(3.0)
    └── right: NumberNode(4.0)
```

**图示说明**：
- 左侧 ParseTree 包含 `varSlot`、`infixExpr`、`numberExpr` 等语法规则节点
- 右侧 AST 只包含运算符（`AdditionNode`、`MultiplicationNode`）和操作数（`NumberNode`）
- ParseTree 显示了完整的语法推导路径
- AST 直接反映了语义结构（乘法优先级高于加法）

**类比理解**：

想象你在阅读一篇技术文档：
- ParseTree 就像**文档的 HTML 源码**：包含所有标签、缩进、注释等格式信息
- AST 就像**文档的目录结构**：只包含章节标题和逻辑关系

相关概念：
- **语法分析**：将字符流转换为 ParseTree
- **语义分析**：将 ParseTree 转换为 AST，添加语义信息
- **抽象化**：去除不重要的细节，保留核心信息

[图2：AST 与 ParseTree 的转换流程]

```
源代码: "2 + 3 * 4"
    ↓
[词法分析] CharStream → TokenStream
Tokens: ['2', '+', '3', '*', '4']
    ↓
[语法分析] TokenStream → ParseTree (ANTLR4 生成)
ParseTree: 完整的语法结构（包含节点类型和推导路径）
    ↓
[AST 构建] ParseTree → AST (BuildAstVisitor)
AST: 简化的语义结构（只包含运算符和操作数）
    ↓
[语义分析] AST → 增强的 AST
增强的 AST: 添加类型、作用域等语义信息
```

**图示说明**：
- ParseTree 是 ANTLR4 根据语法规则自动生成的
- AST 是我们自定义的数据结构，可以灵活设计
- AST 构建是转换过程，使用访问者模式遍历 ParseTree
- AST 可以多次遍历，实现多遍编译（类型检查、优化、代码生成等）

**为什么需要 AST 而不是直接使用 ParseTree？**

1. **更简洁的表示**：AST 节点数量通常比 ParseTree 少 30-50%，遍历更高效
2. **易于添加语义信息**：可以在 AST 节点中添加类型、作用域、位置信息等字段
3. **平台无关**：AST 不绑定特定的解析器实现，可以来自不同的语法（如 ANTLR4、JavaCC、Handwritten Parser）
4. **适合多次遍历**：现代编译器需要多次遍历中间表示（类型检查、优化、代码生成），AST 的稳定结构更适合
5. **更好的错误恢复**：AST 可以包含错误恢复信息，而 ParseTree 在语法错误时会部分构建失败

---

#### 核心概念 2：访问者模式（Visitor Pattern）

**通俗解释**：

访问者模式将**数据结构（AST）**与**操作（求值、类型检查、代码生成）**分离。就像给 AST 节点一个"接待员"，根据访客类型执行不同操作。

打个比方：
- AST 节点就像**博物馆的展品**
- Visitor 就像**导游**
- 不同导游（求值器导游、类型检查导游、代码生成导游）带领游客走不同路线，看到不同风景
- 展品（AST 节点）不需要知道具体导游是谁，只需要提供 `accept()` 方法

[图3：访问者模式结构]

```
ASTVisitor<T> (接口)
    ↑
    ├── visit(AdditionNode node) → T
    ├── visit(SubtractionNode node) → T
    ├── visit(MultiplicationNode node) → T
    ├── visit(DivisionNode node) → T
    ├── visit(NegateNode node) → T
    ├── visit(NumberNode node) → T
    ├── visit(VarNode node) → T
    └── visit(AssignNode node) → T

ConcreteVisitor (具体实现)
    ↑
    ├── EvalExprVisitor (求值器)
    │   ├── visit(AdditionNode node) → left + right
    │   ├── visit(NumberNode node) → value
    │   └── ...
    │
    ├── TypeCheckerVisitor (类型检查器)
    │   ├── visit(AdditionNode node) → check types of left/right
    │   ├── visit(NumberNode node) → return type: Double
    │   └── ...
    │
    └── CodeGeneratorVisitor (代码生成器)
        ├── visit(AdditionNode node) → emit "ADD" instruction
        ├── visit(NumberNode node) → emit "PUSH" instruction
        └── ...
```

**图示说明**：
- `ASTVisitor<T>` 是接口，定义了访问各种 AST 节点的契约
- `T` 是泛型返回类型，可以是 `Double`（求值）、`Type`（类型检查）、`String`（代码生成）等
- 具体的 Visitor 实现了 `visit()` 方法，定义具体的操作逻辑
- AST 节点可以接受不同的 Visitor，实现不同的功能

**类比理解**：

想象你在使用**智能手机应用商店**：
- 应用（AST 节点）不知道用户会用它做什么
- 用户（Visitor）可以选择不同的使用方式：
  - 打开应用直接使用（求值器）
  - 给应用评分（类型检查器）
  - 分享应用到社交媒体（代码生成器）
- 应用只需要提供"接受用户操作"的接口

相关概念：
- **Double Dispatch（双重分发）**：第一次分发是调用 `node.accept(visitor)`，第二次分发是 `visitor.visit(node)`
- **Separation of Concerns（关注点分离）**：数据结构与操作逻辑解耦
- **Open/Closed Principle（开闭原则）**：对扩展开放（新增 Visitor），对修改关闭（不修改 AST 节点）

[图4：Double Dispatch 工作原理]

```
步骤 1: 第一次分发（AST 节点选择）
AdditionNode node = ...;
ASTVisitor<Double> visitor = new EvalExprVisitor();
Double result = node.accept(visitor);  // 调用 AdditionNode.accept()

步骤 2: 第二次分发（Visitor 选择）
// 在 AdditionNode.accept() 内部
public Double accept(ASTVisitor<Double> visitor) {
    return visitor.visit(this);  // 调用 EvalExprVisitor.visit(AdditionNode)
}

步骤 3: 执行具体操作
// 在 EvalExprVisitor.visit(AdditionNode) 内部
public Double visit(AdditionNode node) {
    return visit(node.left) + visit(node.right);  // 执行加法
}
```

**图示说明**：
- 第一次分发：AST 节点根据自己的类型调用对应的 `accept()` 方法
- 第二次分发：Visitor 根据节点的实际类型调用对应的 `visit()` 方法
- 最终执行具体操作：Visitor 的 `visit()` 方法中定义了具体逻辑

**为什么使用访问者模式？**

1. **操作与数据结构分离**：可以方便地添加新的操作（新增 Visitor）而无需修改 AST 节点
2. **类型安全**：编译期检查，确保每种 AST 节点都有对应的 `visit()` 方法
3. **易于扩展**：新增 AST 节点时，只需在 `ASTVisitor` 接口中添加新的 `visit()` 方法
4. **清晰的操作边界**：每个 Visitor 聚焦于单一职责（求值、类型检查、代码生成等）

**访问者模式 vs 传统继承方式**

| 特性 | 访问者模式 | 传统继承（在 AST 节点中定义方法） |
|------|-----------|--------------------------------|
| **添加新操作** | 新增 Visitor 类 | 修改所有 AST 节点类 |
| **添加新节点** | 修改所有 Visitor 类 | 新增 AST 节点类 |
| **操作逻辑位置** | 集中在 Visitor 中 | 分散在各节点类中 |
| **类型安全** | 编译期检查 | 编译期检查 |
| **适用场景** | 操作类型多于节点类型 | 节点类型多于操作类型 |

**本章适用场景**：
- AST 节点类型相对固定（加减乘除、变量、数字等）
- 操作类型较多（求值、类型检查、代码生成、优化等）
- 符合访问者模式的最佳使用场景

---

### 3.2 与仓库 EP 的对应关系

#### 对应 EP：EP13

EP13 实现了完整的 AST 构建与表达式求值功能，是理解 AST 和访问者模式的绝佳案例。

**目录结构**：
```
ep13/
├── src/main/java/org/teachfx/antlr4/ep13/
│   ├── ast/                                    // AST 节点层次
│   │   ├── ExpressionNode.java                   // 表达式 AST 节点基类
│   │   ├── NumberNode.java                     // 数字常量节点
│   │   ├── InfixExpressionNode.java             // 中缀表达式节点基类
│   │   ├── AdditionNode.java                   // 加法节点
│   │   ├── SubtractionNode.java                // 减法节点
│   │   ├── MultiplicationNode.java             // 乘法节点
│   │   ├── DivisionNode.java                   // 除法节点
│   │   ├── NegateNode.java                     // 一元取反节点
│   │   ├── AssignNode.java                    // 赋值语句节点
│   │   └── VarNode.java                      // 变量引用节点
│   ├── ASTVisitor.java                         // AST 访问者接口
│   ├── BuildAstVisitor.java                    // AST 构建器
│   ├── EvalExprVisitor.java                    // AST 求值器
│   ├── Calc.java                              // 主程序
│   ├── Math.g4                               // 数学表达式语法
│   ├── MathLexer.java                          // ANTLR4 生成的词法分析器
│   ├── MathParser.java                         // ANTLR4 生成的语法分析器
│   ├── MathBaseVisitor.java                    // ANTLR4 生成的访问者基类
│   └── MathVisitor.java                       // ANTLR4 生成的访问者接口
└── pom.xml                                    // Maven 配置
```

**关键类/方法说明**：

#### ExpressionNode - AST 节点基类

**类的作用**：所有表达式节点的抽象基类，提供统一的类型标记。

```java
package org.teachfx.antlr4.ep13.ast;

/**
 * 表达式 AST 节点基类
 * 所有表达式节点（数字、二元运算、变量）都继承此类
 *
 * 设计考虑：
 * - 抽象类，不能直接实例化
 * - 不包含任何字段，仅作为类型标记
 * - 后续章节可以添加公共字段（如位置信息、类型信息）
 */
public abstract class ExpressionNode {
    // 基类可以包含公共字段和方法
    // 例如：位置信息、类型信息（后续章节添加）
}
```

**设计要点**：
- 当前是空抽象类，仅作为类型标记
- 后续章节可以扩展：添加 `Type type` 字段（类型信息）、`SourceLocation location` 字段（位置信息）
- 所有具体节点都继承自此类，确保类型统一

---

#### NumberNode - 数字常量节点

**类的作用**：表示程序中的数字字面量，如 `42`、`3.14`。

```java
package org.teachfx.antlr4.ep13.ast;

/**
 * 数字常量节点
 * 表示程序中的数字字面量，如 42、3.14
 *
 * 设计考虑：
 * - 存储为 double 类型，支持整数和小数
 * - 使用 final 修饰符，确保值不可变（不可变对象更安全）
 * - 提供构造函数接收 double 值
 */
public class NumberNode extends ExpressionNode {
    public final double value;  // 存储数值

    public NumberNode(double value) {
        this.value = value;
    }
}
```

**设计要点**：
- `value` 字段使用 `final` 修饰符，确保不可变
- 使用 `double` 类型存储，支持整数、小数、科学计数法
- 简洁的构造函数，只接收一个参数

---

#### InfixExpressionNode - 中缀表达式节点基类

**类的作用**：表示二元运算表达式（`a + b`, `x * y` 等），左右子节点都是表达式，形成递归结构。

```java
package org.teachfx.antlr4.ep13.ast;

/**
 * 中缀表达式节点基类
 * 表示二元运算表达式（a + b, x * y 等）
 * 左右子节点都是表达式，形成递归结构
 *
 * 设计考虑：
 * - 抽象类，不能直接实例化
 * - 提供 left 和 right 字段存储左右操作数
 * - 提供有参和无参构造函数（灵活使用）
 * - 具体的运算符节点（AdditionNode 等）继承此类
 */
public abstract class InfixExpressionNode extends ExpressionNode {
    public ExpressionNode left;  // 左操作数
    public ExpressionNode right; // 右操作数

    /**
     * 有参构造函数：同时设置左右操作数
     *
     * @param left  左操作数
     * @param right 右操作数
     */
    public InfixExpressionNode(ExpressionNode left, ExpressionNode right) {
        this.left = left;
        this.right = right;
    }

    /**
     * 无参构造函数：左右操作数为 null，后续手动设置
     * 使用场景：在 BuildAstVisitor 中先创建节点，再设置子节点
     */
    public InfixExpressionNode() {
        this.left = null;
        this.right = null;
    }
}
```

**设计要点**：
- 提供两种构造函数，灵活使用
- `left` 和 `right` 字段不是 `final`，支持后续修改
- 所有二元运算（加减乘除）都继承此类，代码复用

---

#### AdditionNode - 加法节点

**类的作用**：表示二元加法运算。

```java
package org.teachfx.antlr4.ep13.ast;

/**
 * 加法节点
 * 表示二元加法运算
 * 继承 InfixExpressionNode，设置左右子节点
 *
 * 设计考虑：
 * - 仅提供构造函数，不添加额外字段
 * - 所有逻辑都在 EvalExprVisitor 的 visit() 方法中实现
 * - 保持节点类的简洁性
 */
public class AdditionNode extends InfixExpressionNode {

    /**
     * 无参构造函数：左右操作数为 null
     * 使用场景：在 BuildAstVisitor 中先创建节点，再设置子节点
     */
    public AdditionNode() {
        super();
    }

    /**
     * 有参构造函数：同时设置左右操作数
     *
     * @param left  左操作数
     * @param right 右操作数
     */
    public AdditionNode(ExpressionNode left, ExpressionNode right) {
        super(left, right);
    }
}
```

**设计要点**：
- 不添加额外字段，完全继承 `InfixExpressionNode` 的 `left` 和 `right`
- 提供两种构造函数，灵活使用
- 类的职责单一：只表示加法运算，不包含计算逻辑（计算逻辑在 `EvalExprVisitor` 中）

**其他二元运算节点**（SubtractionNode、MultiplicationNode、DivisionNode）的设计与 AdditionNode 类似，不重复展示。

---

#### ASTVisitor<T> - 访问者接口

**接口的作用**：定义访问所有 AST 节点的契约，使用泛型 `T` 表示返回类型。

```java
package org.teachfx.antlr4.ep13;

import org.teachfx.antlr4.ep13.ast.*;

/**
 * AST 访问者接口
 * 泛型 T 表示访问后的返回类型（求值返回 Double，类型检查返回 Type）
 *
 * 设计考虑：
 * - 使用泛型 T，支持不同的返回类型
 * - 每种 AST 节点类型对应一个 visit 方法
 * - 使用方法重载实现多态
 * - 具体访问者实现定义具体行为
 */
public interface ASTVisitor<T> {

    // ========== 语句节点 ==========

    /**
     * 访问赋值节点
     *
     * @param node 赋值节点
     * @return 访问结果（求值时返回变量值，类型检查时返回类型等）
     */
    T visit(AssignNode node);

    /**
     * 访问变量引用节点
     *
     * @param node 变量节点
     * @return 访问结果（求值时返回变量值，类型检查时返回变量类型）
     */
    T visit(VarNode node);

    // ========== 表达式节点 ==========

    T visit(AdditionNode node);
    T visit(SubtractionNode node);
    T visit(MultiplicationNode node);
    T visit(DivisionNode node);
    T visit(NegateNode node);
    T visit(NumberNode node);

    /**
     * 访问通用表达式节点
     * 用于类型分发：根据节点的实际类型调用对应的 visit() 方法
     *
     * @param node 表达式节点
     * @return 访问结果
     */
    T visit(ExpressionNode node);
}
```

**设计要点**：
- 泛型 `T` 让接口非常灵活（可以返回 `Double`、`Type`、`String` 等）
- 每种节点类型对应一个 `visit()` 方法
- `visit(ExpressionNode node)` 用于类型分发，在 `EvalExprVisitor` 中实现
- 语句节点和表达式节点分组，结构清晰

---

#### BuildAstVisitor - AST 构建器

**类的作用**：从 ParseTree 构建 AST，遍历 ParseTree 的每个节点，创建对应的 AST 节点，建立 AST 节点之间的父子关系。

```java
package org.teachfx.antlr4.ep13;

import org.teachfx.antlr4.ep13.MathParser.*;
import org.teachfx.antlr4.ep13.ast.*;

/**
 * 从 ParseTree 构建 AST
 *
 * 职责：
 * - 遍历 ParseTree 的每个节点
 * - 创建对应的 AST 节点
 * - 建立 AST 节点之间的父子关系
 * - 忽略语法细节（如括号）
 *
 * 设计考虑：
 * - 继承 ANTLR4 生成的 MathBaseVisitor<ExpressionNode>
 * - 每个 visitXXX() 方法对应语法中的一个规则
 * - 使用 switch-case 根据运算符类型创建不同的 AST 节点
 * - 递归构建子节点（表达式嵌套）
 */
public class BuildAstVisitor extends MathBaseVisitor<ExpressionNode> {

    /**
     * 访问编译单元（顶层规则）
     *
     * 编译单元可以是赋值语句或表达式：
     * - "x = 5" → AssignNode
     * - "2 + 3" → AdditionNode
     *
     * @param ctx 编译单元上下文
     * @return 对应的 AST 节点
     */
    @Override
    public ExpressionNode visitCompileUnit(CompileUnitContext ctx) {
        if (ctx.assign() != null) {
            return visit(ctx.assign());  // 返回赋值节点
        }
        return visit(ctx.expr());  // 返回表达式节点
    }

    /**
     * 访问数字表达式
     *
     * @param ctx 数字表达式上下文
     * @return NumberNode 节点
     */
    @Override
    public ExpressionNode visitNumberExpr(NumberExprContext ctx) {
        // 解析数字字符串为 double 值
        return new NumberNode(Double.parseDouble(ctx.value.getText()));
    }

    /**
     * 访问括号表达式
     *
     * 设计考虑：AST 不保留括号，直接返回内部表达式
     * 因为运算符优先级已在 AST 结构中体现
     *
     * @param ctx 括号表达式上下文
     * @return 内部表达式对应的 AST 节点
     */
    @Override
    public ExpressionNode visitParensExpr(ParensExprContext ctx) {
        // 直接返回内部表达式（AST 不保留括号）
        return visit(ctx.expr());
    }

    /**
     * 访问中缀表达式（二元运算）
     *
     * 根据运算符类型创建对应的 AST 节点：
     * - '+' → AdditionNode
     * - '-' → SubtractionNode
     * - '*' → MultiplicationNode
     * - '/' → DivisionNode
     *
     * @param ctx 中缀表达式上下文
     * @return 对应的二元运算节点
     */
    @Override
    public ExpressionNode visitInfixExpr(InfixExprContext ctx) {
        InfixExpressionNode node;

        // 根据运算符类型创建对应的节点
        switch (ctx.op.getType()) {
            case MathLexer.OP_ADD:
                node = new AdditionNode();
                break;
            case MathLexer.OP_SUB:
                node = new SubtractionNode();
                break;
            case MathLexer.OP_MUL:
                node = new MultiplicationNode();
                break;
            case MathLexer.OP_DIV:
                node = new DivisionNode();
                break;
            default:
                node = null; // 不应该发生
        }

        // 递归构建左右子节点
        node.left = visit(ctx.left);
        node.right = visit(ctx.right());

        return node;
    }

    /**
     * 访问一元表达式（正负号）
     *
     * @param ctx 一元表达式上下文
     * @return 对应的 AST 节点
     */
    @Override
    public ExpressionNode visitUnaryExpr(UnaryExprContext ctx) {
        switch (ctx.op.getType()) {
            case MathLexer.OP_ADD:
                // 正号：直接返回表达式（无操作）
                return visit(ctx.expr());
            case MathLexer.OP_SUB:
                // 负号：创建取反节点
                return new NegateNode(visit(ctx.expr()));
        }
        return null;
    }

    /**
     * 访问赋值表达式
     *
     * @param ctx 赋值表达式上下文
     * @return AssignNode 节点
     */
    @Override
    public ExpressionNode visitAssignExpr(AssignExprContext ctx) {
        // 提取变量名和值表达式
        return new AssignNode(ctx.name.getText(), visit(ctx.value()));
    }

    /**
     * 访问变量表达式
     *
     * @param ctx 变量表达式上下文
     * @return VarNode 节点
     */
    @Override
    public ExpressionNode visitVarExpr(VarExprContext ctx) {
        // 提取变量名
        return new VarNode(ctx.var.getText());
    }
}
```

**设计要点**：
- 继承 `MathBaseVisitor<ExpressionNode>`，重写所有需要的方法
- 每个方法对应语法中的一个规则（`CompileUnitContext`、`NumberExprContext` 等）
- 使用 `switch-case` 根据运算符类型创建不同的节点
- 递归构建子节点：`visit(ctx.left)` 和 `visit(ctx.right())`
- 括号表达式不保留括号，直接返回内部表达式（体现 AST 的抽象性）

---

#### EvalExprVisitor - AST 求值器

**类的作用**：遍历 AST 节点，计算表达式的值，维护变量内存。

```java
package org.teachfx.antlr4.ep13;

import org.teachfx.antlr4.ep13.ast.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AST 求值器
 *
 * 职责：
 * - 维护变量内存（Map<String, Double>）
 * - 递归访问 AST 节点，计算结果
 * - 处理赋值语句，更新变量值
 *
 * 设计考虑：
 * - 实现 ASTVisitor<Double> 接口
 * - 使用 Map 存储变量名到值的映射
 * - 递归计算表达式（后序遍历）
 * - 处理未定义变量（返回 null 或 0.0）
 */
public class EvalExprVisitor implements ASTVisitor<Double> {
    // 变量内存：存储变量名到值的映射
    protected Map<String, Double> memory;

    /**
     * 构造函数：初始化变量内存
     */
    public EvalExprVisitor() {
        this.memory = new HashMap<>();
    }

    // ========== 赋值语句 ==========

    /**
     * 访问赋值节点
     * 计算右值，存入内存，并返回结果
     *
     * @param node 赋值节点
     * @return 赋值的结果（右值）
     */
    @Override
    public Double visit(AssignNode node) {
        // 计算右值，存入内存
        Double value = visit(node.value());
        memory.put(node.varName, value);
        return value;
    }

    // ========== 变量引用 ==========

    /**
     * 访问变量节点
     * 从内存中取值，如果未定义则返回 null
     *
     * @param node 变量节点
     * @return 变量的值（可能为 null）
     */
    @Override
    public Double visit(VarNode node) {
        return memory.get(node.name);
    }

    // ========== 二元运算 ==========

    /**
     * 访问加法节点
     * 递归计算左右操作数，返回和
     *
     * @param node 加法节点
     * @return 左操作数 + 右操作数
     */
    @Override
    public Double visit(AdditionNode node) {
        return visit(node.left) + visit(node.right);
    }

    /**
     * 访问减法节点
     * 递归计算左右操作数，返回差
     *
     * @param node 减法节点
     * @return 左操作数 - 右操作数
     */
    @Override
    public Double visit(SubtractionNode node) {
        return visit(node.left) - visit(node.right);
    }

    /**
     * 访问乘法节点
     * 递归计算左右操作数，返回积
     *
     * @param node 乘法节点
     * @return 左操作数 * 右操作数
     */
    @Override
    public Double visit(MultiplicationNode node) {
        return visit(node.left) * visit(node.right);
    }

    /**
     * 访问除法节点
     * 递归计算左右操作数，返回商
     *
     * @param node 除法节点
     * @return 左操作数 / 右操作数
     */
    @Override
    public Double visit(DivisionNode node) {
        return visit(node.left) / visit(node.right);
    }

    // ========== 一元运算 ==========

    /**
     * 访问取反节点
     * 递归计算操作数，返回负值
     *
     * @param node 取反节点
     * @return 操作数 * (-1)
     */
    @Override
    public Double visit(NegateNode node) {
        return visit(node.innerNode) * (-1);
    }

    // ========== 字面量 ==========

    /**
     * 访问数字节点
     * 直接返回值
     *
     * @param node 数字节点
     * @return 数字节点的值
     */
    @Override
    public Double visit(NumberNode node) {
        return node.value;
    }

    // ========== 类型分发 ==========

    /**
     * 访问通用表达式节点
     * 根据节点的实际类型分发给对应的 visit() 方法
     *
     * 设计考虑：
     * - 使用 getClass().equals() 进行精确类型匹配
     * - 避免使用 instanceof（避免子类匹配到父类）
     * - 如果未匹配到任何类型，返回 null
     *
     * @param node 表达式节点
     * @return 访问结果
     */
    @Override
    public Double visit(ExpressionNode node) {
        // 根据实际类型分发
        if (node.getClass().equals(AdditionNode.class)) {
            return visit((AdditionNode) node);
        } else if (node.getClass().equals(SubtractionNode.class)) {
            return visit((SubtractionNode) node);
        } else if (node.getClass().equals(MultiplicationNode.class)) {
            return visit((MultiplicationNode) node);
        } else if (node.getClass().equals(DivisionNode.class)) {
            return visit((DivisionNode) node);
        } else if (node.getClass().equals(NegateNode.class)) {
            return visit((NegateNode) node);
        } else if (node.getClass().equals(NumberNode.class)) {
            return visit((NumberNode) node);
        } else if (node.getClass().equals(AssignNode.class)) {
            return visit((AssignNode) node);
        } else if (node.getClass().equals(VarNode.class)) {
            return visit((VarNode) node);
        }
        return null;
    }
}
```

**设计要点**：
- 实现 `ASTVisitor<Double>` 接口，返回 `Double` 类型
- 使用 `Map<String, Double>` 存储变量内存
- 递归计算表达式（后序遍历：先计算子节点，再计算父节点）
- `visit(ExpressionNode node)` 使用类型分发，根据实际类型调用对应的 `visit()` 方法
- 处理未定义变量（`memory.get(node.name)` 返回 `null`）

**变量内存管理**：
- `memory` 字段存储变量名到值的映射
- `AssignNode` 的 `visit()` 方法更新内存
- `VarNode` 的 `visit()` 方法从内存读取
- 变量在赋值前使用会返回 `null`（后续章节会改进，增加未定义变量检查）

---

#### Calc.java - 主程序

**类的作用**：演示完整的编译器流水线，从输入到结果的完整流程。

```java
package org.teachfx.antlr4.ep13;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep13.ast.ExpressionNode;

import java.io.*;

/**
 * 主程序：演示完整的编译器流水线
 *
 * 流水线：
 * CharStream → Lexer → TokenStream → Parser → ParseTree → AST (BuildAstVisitor) → Result (EvalExprVisitor)
 *
 * 设计考虑：
 * - 支持从文件或标准输入读取
 * - 逐行处理表达式
 * - 复用 MathParser 对象（提高性能）
 * - 打印每行的计算结果
 */
public class Calc {
    public static void main(String[] args) throws IOException {
        // 步骤 0：准备输入源
        String fileName = null;
        if (args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if (fileName != null) is = new FileInputStream(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

        // 步骤 1：创建访问者和解析器（可复用）
        MathParser parser = new MathParser(null);  // 初始化为 null，后续设置 InputStream
        ASTVisitor<Double> astVisitor = new EvalExprVisitor();

        // 步骤 2：逐行读取和处理表达式
        String expr = bufferedReader.readLine();
        int line = 1;  // 行号，用于错误定位

        while (expr != null) {
            // 步骤 3：词法分析：字符流 → TokenStream
            CharStream input = CharStreams.fromString(expr + "\n");  // 添加换行符
            MathLexer lexer = new MathLexer(input);
            lexer.setLine(line);  // 设置行号
            lexer.setCharPositionInLine(0);

            // 步骤 4：语法分析：TokenStream → ParseTree
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser.setInputStream(tokens);  // 复用 parser 对象
            ParseTree tree = parser.compileUnit();

            // 步骤 5：AST 构建：ParseTree → AST
            ExpressionNode exprAST = tree.accept(new BuildAstVisitor());

            // 步骤 6：表达式求值：AST → Result
            System.out.println("Result : " + astVisitor.visit(exprAST));

            // 下一行
            expr = bufferedReader.readLine();
            line++;
        }
    }
}
```

**设计要点**：
- 支持从文件或标准输入读取（通过命令行参数 `args[0]` 指定文件）
- 逐行处理表达式，每行独立计算
- 复用 `MathParser` 对象，提高性能（避免重复创建）
- 使用 `bufferedReader.readLine()` 逐行读取
- 添加换行符 `\n` 确保解析器正确处理输入

**编译器流水线**：
1. **输入准备**：从文件或标准输入读取
2. **词法分析**：`CharStream` → `MathLexer` → `TokenStream`
3. **语法分析**：`TokenStream` → `MathParser` → `ParseTree`
4. **AST 构建**：`ParseTree` → `BuildAstVisitor` → `ExpressionNode`（AST）
5. **表达式求值**：`ExpressionNode` → `EvalExprVisitor` → `Double`（结果）

---

### 3.3 实战流程

#### 步骤 1：编译 EP13 项目

进入 EP13 目录，编译项目（包括生成 ANTLR4 代码）。

**操作**：
```bash
# 进入 EP13 目录
cd ep13

# 清理并编译（包括生成 ANTLR4 代码）
mvn clean compile
```

**预期输出**：
```
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

**验证方法**：
- 检查编译输出是否包含 `[INFO] BUILD SUCCESS`
- 检查 `target/generated-sources/antlr4` 目录是否包含生成的 ANTLR4 代码

**故障排查**：

如果出现编译错误，检查：
1. Java 版本是否为 21 或更高（`java -version`）
2. Maven 版本是否为 3.8+（`mvn -version`）
3. ANTLR4 插件是否正确生成代码（检查 `target/generated-sources/antlr4` 目录）

---

#### 步骤 2：准备测试输入文件

创建测试输入文件，包含多个表达式和赋值语句。

**操作**：
```bash
# 创建测试输入文件
cat > test_expr.txt << 'EOF'
2 + 3 * 4
x = 10
x + 5
(2 + 3) * 4
y = x * 2
y / 5
-5 * 3
EOF
```

**文件内容说明**：
- 第 1 行：测试运算符优先级（乘法优先于加法）
- 第 2 行：变量赋值（x = 10）
- 第 3 行：变量引用（x + 5）
- 第 4 行：测试括号优先级
- 第 5 行：变量赋值（y = x * 2）
- 第 6 行：变量引用（y / 5）
- 第 7 行：一元取反（-5 * 3）

---

#### 步骤 3：运行主程序

运行 Calc 主程序，处理测试输入文件。

**操作**：
```bash
# 运行主程序
cd ep13
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.Calc" \
    -Dexec.args="test_expr.txt"
```

**预期输出**：
```
Result : 14.0
Result : 10.0
Result : 15.0
Result : 20.0
Result : 20.0
Result : 4.0
Result : -15.0
```

**验证方法**：

检查每个表达式的计算结果是否正确：

| 表达式 | 预期结果 | 说明 |
|---------|-----------|------|
| `2 + 3 * 4` | 14.0 | 乘法优先于加法：2 + (3 * 4) = 2 + 12 = 14 |
| `x = 10` | 10.0 | 变量赋值，返回右值 10.0 |
| `x + 5` | 15.0 | 引用变量 x（值为 10），计算 10 + 5 = 15 |
| `(2 + 3) * 4` | 20.0 | 括号改变优先级：(2 + 3) * 4 = 5 * 4 = 20 |
| `y = x * 2` | 20.0 | 引用变量 x（值为 10），计算 10 * 2 = 20 |
| `y / 5` | 4.0 | 引用变量 y（值为 20），计算 20 / 5 = 4 |
| `-5 * 3` | -15.0 | 一元取反，计算 (-5) * 3 = -15 |

---

#### 步骤 4：测试交互式输入

测试从标准输入读取表达式（不指定文件）。

**操作**：
```bash
# 运行主程序（不指定文件，从标准输入读取）
cd ep13
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.Calc"

# 然后输入表达式（每行一个）
# 输入:
# 3 + 4
# x = 7
# x * 2
# 按 Ctrl+D 结束输入
```

**预期输出**：
```
Result : 7.0
Result : 7.0
Result : 14.0
```

**验证方法**：
- 每行输入后立即显示结果
- 可以连续输入多行表达式
- 按 `Ctrl+D`（Linux/Mac）或 `Ctrl+Z`（Windows）结束输入

---

#### 故障排查提示

| 问题 | 可能原因 | 解决方法 |
|------|---------|---------|
| 编译错误 "cannot find symbol: ExpressionNode" | ANTLR4 生成的代码未正确生成 | 清理 target 目录重新编译：`mvn clean compile` |
| NullPointerException | 某个节点未正确初始化 | 检查 BuildAstVisitor 的实现逻辑，确保所有节点都正确创建 |
| 求值结果不正确 | 运算符优先级或逻辑错误 | 添加调试输出，打印每个子表达式的计算结果 |
| 变量未定义返回 null | 变量在使用前未赋值 | 在 EvalExprVisitor 中添加未定义变量检查 |
| 输入文件无法读取 | 文件路径错误或权限问题 | 检查文件路径和权限，使用绝对路径或相对路径 |

---

## 4. AI 协作线：Context Engineering 视角

### 4.1 上下文设计

为了让 AI 帮忙完成本章任务，你需要提供以下上下文：

#### 上下文第 1 层：项目宏观上下文

**目的**：让 AI 理解项目的整体结构和技术栈。

**需要提供的文件**：

1. **`pom.xml`**（根 POM）
   - 作用：管理所有 EP 模块的依赖版本
   - 关键信息：Java 版本（21）、ANTLR4 版本（4.13.2）、测试框架（JUnit 5）

2. **`AGENTS.md`**
   - 作用：代码规范和最佳实践
   - 关键信息：包命名规范、类命名规范、方法命名规范、导入顺序

3. **`README.md`**
   - 作用：项目概览和学习路径
   - 关键信息：渐进式学习路径（EP1-EP21）、技术栈、编译器流水线

**组织说明**：
这些文件提供了：
- 项目的多模块组织（ep1-ep21）
- 技术栈版本（Java 21、ANTLR4 4.13.2、Maven 3.8+）
- 代码风格约定（包命名、类命名、方法命名）
- 测试框架和断言库（JUnit 5 + AssertJ）

AI 需要理解：
- 这是一个教育项目，每个 EP 聚焦 1-2 个核心概念
- EP13 的位置：模块 2 的起始阶段（从解释到编译）
- 编译器流水线的阶段：词法分析 → 语法分析 → AST 构建 → 求值

---

#### 上下文第 2 层：模块级上下文

**目的**：让 AI 理解当前 EP 的设计和关键实现。

**需要提供的文件**：

1. **`ep13/pom.xml`**
   - 作用：EP13 的 Maven 配置
   - 关键依赖：ANTLR4 runtime、JUnit 5
   - 关键配置：ANTLR4 插件（生成解析器代码）

2. **`ep13/src/main/java/org/teachfx/antlr4/Math.g4`**
   - 作用：数学表达式语法定义
   - 关键规则：`varSlot`（表达式）、`assign`（赋值）、`compileUnit`（编译单元）

**源码文件**：

3. **AST 节点类**（全部）：
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/ExpressionNode.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/NumberNode.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/InfixExpressionNode.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/AdditionNode.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/SubtractionNode.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/MultiplicationNode.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/DivisionNode.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/NegateNode.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/AssignNode.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/VarNode.java`

4. **访问者相关类**：
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/ASTVisitor.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/BuildAstVisitor.java`
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/EvalExprVisitor.java`

5. **主程序**：
   - `ep13/src/main/java/org/teachfx/antlr4/ep13/Calc.java`

**测试文件**：
- `ep13/src/test/java/org/teachfx/antlr4/ep13/`（如果有）

**组织说明**：
这些文件提供了 EP13 完整的 AST 实现：
- AST 节点层次结构（基类 → 中缀表达式 → 具体运算节点）
- 访问者接口和实现
- AST 构建器（将 ParseTree 转换为 AST）
- AST 求值器（计算表达式结果）
- 主程序演示完整流水线

AI 需要理解：
- AST 节点的继承层次：`ExpressionNode` → `InfixExpressionNode` → 具体运算节点
- 访问者模式的使用：`ASTVisitor<T>` 接口 → `BuildAstVisitor` 和 `EvalExprVisitor`
- 编译器流水线的完整流程：`CharStream → Lexer → Parser → ParseTree → AST → Result`

---

#### 上下文第 3 层：任务级上下文

**目的**：让 AI 理解本章的具体任务目标。

**需要提供的信息**：

1. **任务目标**：实现 AST 构建器，将 ParseTree 转换为 AST
2. **具体要求**：
   - 忽略语法细节（括号、分号）
   - 只保留语义信息（运算符、操作数）
   - 使用访问者模式遍历 ParseTree
3. **设计原则**：
   - 参考现有的 `ExpressionNode`、`InfixExpressionNode` 类设计模式
   - 遵循访问者模式的设计原则
   - 保持 AST 节点的简洁性（只包含必要字段）

**组织说明**：
从高层到低层的信息流：
- 数学表达式语法定义（Math.g4）
- ParseTree 结构（ANTLR4 生成的 Context 类）
- AST 节点类设计（ExpressionNode、InfixExpressionNode 等）
- 访问者模式实现（ASTVisitor 接口、BuildAstVisitor、EvalExprVisitor）
- 求值器实现（EvalExprVisitor）

AI 需要理解：
- ParseTree 和 AST 的区别（语法细节 vs 语义信息）
- 为什么需要 AST（多次遍历、添加语义信息、更好的抽象）
- 访问者模式的实现方式（Double Dispatch）

---

#### 上下文第 4 层：实现细节上下文

**目的**：让 AI 理解关键实现细节。

**需要提供的代码片段**：

1. **BuildAstVisitor 的关键方法**：
   - `visitCompileUnit()` - 处理编译单元
   - `visitNumberExpr()` - 处理数字表达式
   - `visitParensExpr()` - 处理括号表达式
   - `visitInfixExpr()` - 处理中缀表达式
   - `visitUnaryExpr()` - 处理一元表达式
   - `visitAssignExpr()` - 处理赋值表达式
   - `visitVarExpr()` - 处理变量表达式

2. **EvalExprVisitor 的关键方法**：
   - `visit(AdditionNode node)` - 加法求值
   - `visit(MultiplicationNode node)` - 乘法求值
   - `visit(AssignNode node)` - 赋值求值
   - `visit(VarNode node)` - 变量引用求值
   - `visit(ExpressionNode node)` - 类型分发

3. **AST 节点类的设计模式**：
   - `NumberNode` - 数字常量节点
   - `InfixExpressionNode` - 中缀表达式节点基类
   - `AdditionNode` - 加法节点
   - `AssignNode` - 赋值节点

**组织说明**：
这些代码片段展示了：
- AST 构建的遍历逻辑（递归、switch-case）
- 求值的递归计算模式（后序遍历）
- 变量内存管理机制（Map<String, Double>）
- 表达式节点的设计模式（继承、多态）

AI 需要理解：
- 如何使用 ANTLR4 的 `MathBaseVisitor` 构建 AST
- 如何实现类型分发（`getClass().equals()`）
- 如何递归遍历 AST（后序遍历）

---

### 4.2 Prompt 模板（给 AI 用）

#### 模板类型 A：扩展 AST 节点 Prompt 模板

**适用场景**：为数学表达式语言添加新的 AST 节点，支持新的运算符（如求幂运算 `**`、取模运算 `%`）。

**Prompt 模板**：
```
任务：为数学表达式语言添加求幂运算（** 运算符）的 AST 支持

背景：
我正在学习编译器实现，使用 Java 21 + ANTLR4 4.13.2。
项目是一个渐进式学习的编译器项目，每个 EP 聚焦 1-2 个核心概念。
当前在 EP13，已经实现了加减乘除和取负运算的 AST 求值器。

任务目标：
在 EP13 中添加求幂运算符（**）的完整支持，包括 AST 节点、AST 构建器和求值器。

约束条件：
1. 遵循现有 AST 节点设计模式（继承 InfixExpressionNode）
2. 保持与现有编译器流水线的兼容性
3. 所有新增代码必须通过 mvn clean compile
4. 确保类型安全（使用 Double 类型）
5. 不破坏现有 EP 模块边界

参考文件：

源码：
- ep13/src/main/java/org/teachfx/antlr4/ep13/ast/InfixExpressionNode.java
- ep13/src/main/java/org/teachfx/antlr4/ep13/ast/AdditionNode.java
- ep13/src/main/java/org/teachfx/antlr4/ep13/BuildAstVisitor.java
- ep13/src/main/java/org/teachfx/antlr4/ep13/EvalExprVisitor.java
- ep13/src/main/java/org/teachfx/antlr4/ep13/ASTVisitor.java

文档：
- ep13/src/main/java/org/teachfx/antlr4/Math.g4（语法文件）

测试：
- ep13/src/test/java/org/teachfx/antlr4/ep13/（如果有）

期望输出：
1. PowerNode.java 完整代码（继承 InfixExpressionNode）
2. 修改后的 Math.g4（添加 OP_POW 词法规则）
3. 修改后的 BuildAstVisitor.java（标注新增部分：visitInfixExpr 中的 case 分支）
4. 修改后的 EvalExprVisitor.java（标注新增部分：visit(PowerNode node) 方法）
5. 修改后的 ASTVisitor.java（标注新增部分：visit(PowerNode node) 方法声明）
6. 测试用例示例（2 ** 3 预期输出 8.0，3 ** 4 预期输出 81.0）

验证方法：
1. 运行 mvn clean compile，确保编译成功
2. 运行 mvn test -Dtest=ASTBuildingTest（如果有）
3. 运行示例程序验证 2 ** 3 = 8.0
4. 检查求幂运算的优先级（应高于加法，低于乘法）

设计要求：
- 求幂运算符合数学定义：2 ** 3 = 2³ = 8
- 在 BuildAstVisitor.visitInfixExpr 中添加 case MathLexer.OP_POW 分支
- 在 EvalExprVisitor 中使用 Math.pow(base, exponent) 实现求幂
```

**Prompt 设计说明**：
- 明确任务目标：添加求幂运算符支持
- 列出具体约束条件（5 条）
- 提供参考文件列表（源码、文档、测试）
- 说明期望输出（6 项：代码文件、修改标注、测试用例）
- 说明验证方法（4 步：编译、测试、运行示例、检查优先级）
- 提供设计要求（3 项：数学定义、AST 构建、求值实现）

---

#### 模板类型 B：AST 可视化 Prompt 模板

**适用场景**：为 EvalExprVisitor 添加一个方法，以树形结构可视化打印 AST，方便调试和理解 AST 结构。

**Prompt 模板**：
```
任务：为 EvalExprVisitor 添加 AST 可视化打印方法

背景：
我正在学习编译器实现，使用 Java 21 + ANTLR4 4.13.2。
项目是一个渐进式学习的编译器项目，每个 EP 聚焦 1-2 个核心概念。
当前在 EP13，已经实现了加减乘除和取负运算的 AST 求值器。

任务目标：
在 EvalExprVisitor 中添加 printAST() 方法，以树形结构可视化打印 AST，方便调试。

约束条件：
1. 不修改 EvalExprVisitor 的现有求值逻辑
2. 使用递归遍历，保持简洁
3. 打印格式清晰易读，便于调试
4. 使用缩进表示节点层级关系
5. 显示节点类型和值（如 NumberNode(2.0), AdditionNode）

参考文件：

源码：
- ep13/src/main/java/org/teachfx/antlr4/ep13/EvalExprVisitor.java
- ep13/src/main/java/org/teachfx/antlr4/ep13/ast/ExpressionNode.java

文档：
- ep13/src/main/java/org/teachfx/antlr4/Calc.java（主程序）

测试：
- ep13/src/test/java/org/teachfx/antlr4/ep13/（如果有）

期望输出：
1. 修改后的 EvalExprVisitor.java（新增 printAST 方法，标注新增部分）
2. 示例输入的打印输出（如下所示）：

示例输入：2 + 3 * 4
预期输出：
```
AdditionNode
  NumberNode(2.0)
  MultiplicationNode
    NumberNode(3.0)
    NumberNode(4.0)
```

示例输入：(2 + 3) * 4
预期输出：
```
MultiplicationNode
  AdditionNode
    NumberNode(2.0)
    NumberNode(3.0)
  NumberNode(4.0)
```

示例输入：x = 10 + 5
预期输出：
```
AssignNode(x)
  AdditionNode
    NumberNode(10.0)
    NumberNode(5.0)
```

验证方法：
1. 运行 mvn clean compile，确保编译成功
2. 修改 Calc.java，在求值前调用 printAST(exprAST)
3. 运行示例程序，观察打印输出
4. 检查打印格式是否清晰，缩进是否正确

设计要求：
- printAST(ExpressionNode node, int indent) 方法签名
- indent 参数控制缩进级别（每级缩进 2 个空格）
- 节点类型使用类名（如 AdditionNode）
- NumberNode 显示数值（如 NumberNode(2.0)）
- VarNode 显示变量名（如 VarNode(x)）
- AssignNode 显示变量名（如 AssignNode(x)）
- NegateNode 显示前缀 -（如 NegateNode(-5)）
```

**Prompt 设计说明**：
- 明确任务目标：添加 AST 可视化打印方法
- 列出具体约束条件（5 条）
- 提供参考文件列表（源码、文档、测试）
- 说明期望输出（2 项：修改后的代码、示例输出）
- 说明验证方法（4 步：编译、修改主程序、运行示例、检查格式）
- 提供设计要求（7 项：方法签名、缩进控制、节点类型显示等）

---

### 4.3 AI 应该做 / 不该做

#### 功能实现任务：

**✅ AI 允许做的事情**：

1. **实现明确界定的 AST 节点类**（如 PowerNode、ModuloNode）
   - 遵循现有设计模式（继承 InfixExpressionNode）
   - 添加必要的字段和构造函数
   - 保持节点类的简洁性

2. **在 BuildAstVisitor 中添加对应的转换逻辑**
   - 在 visitInfixExpr 中添加新的 case 分支
   - 正确处理运算符优先级
   - 递归构建左右子节点

3. **在 EvalExprVisitor 中添加新节点类型的求值逻辑**
   - 实现 visit(PowerNode node) 方法
   - 使用 Math.pow() 或自定义算法
   - 正确处理边界情况（如 0 的负数次幂）

4. **生成测试用例和辅助代码**
   - 生成 JUnit 5 测试类
   - 使用 AssertJ 的流式断言
   - 覆盖正常情况和边界情况

5. **生成代码注释和文档**
   - 添加 JavaDoc 注解
   - 添加行内注释解释关键逻辑
   - 说明设计考虑和实现细节

6. **添加错误处理和边界检查**
   - 检查除零错误（DivisionNode）
   - 检查未定义变量（VarNode）
   - 添加友好的错误消息

**❌ AI 禁止做的事情**：

1. **修改 InfixExpressionNode 的核心设计**
   - 如将 left/right 字段改为 children 列表
   - 这会破坏现有代码的兼容性

2. **修改 ASTVisitor 接口的方法签名**
   - 如改变返回类型 T 为具体类型
   - 这会影响所有 Visitor 实现

3. **改变 ExpressionNode 基类的设计**
   - 如修改为接口
   - 这会影响所有 AST 节点的继承关系

4. **删除 EvalExprVisitor 的 memory 字段**
   - 这会破坏变量存储机制
   - 导致变量赋值和引用无法工作

5. **引入新的外部依赖**
   - 除非明确要求，不添加新的库
   - 保持项目依赖的简洁性

6. **破坏现有 EP 模块边界**
   - 如修改 EP14、EP15 的代码
   - 只在 EP13 范围内工作

---

#### 测试生成任务：

**✅ AI 允许做的事情**：

1. **生成全面的测试用例**
   - 正常情况：`2 + 3`, `x = 5`, `x * 2`
   - 边界情况：`0 / 1`, `1 / 0`, `-5 * 3`
   - 错误情况：未定义变量、除零错误

2. **使用 JUnit 5 + AssertJ 的流式断言**
   - `assertThat(result).isEqualTo(14.0)`
   - `assertThat(exception).isInstanceOf(DivisionByZeroException.class)`

3. **添加 @DisplayName 注解的中英文描述**
   - `@DisplayName("测试加法运算")`
   - `@DisplayName("Test addition operation")`

4. **生成测试辅助方法和测试固件**
   - `@BeforeEach` 初始化 EvalExprVisitor
   - `@AfterEach` 清理内存

5. **覆盖表达式求值的所有场景**
   - 加减乘除取反
   - 变量赋值和引用
   - 运算符优先级
   - 括号改变优先级

**❌ AI 禁止做的事情**：

1. **删除或修改现有测试**
   - 保留现有的测试用例
   - 只添加新的测试，不删除旧的

2. **生成没有断言的测试（空测试）**
   - 所有测试必须有断言
   - 避免 "假阳性" 测试

3. **生成无法编译的测试**
   - 确保所有测试代码语法正确
   - 确保所有依赖都已导入

4. **降低测试覆盖率**
   - 不删除现有的测试
   - 保持或提高测试覆盖率

5. **引入新的测试框架**
   - 除非明确要求，不使用其他测试框架
   - 保持使用 JUnit 5 和 AssertJ

---

### 4.4 验证与回滚策略

#### 自动化验证流程

**第 1 层：编译验证**

```bash
# 进入 EP 目录
cd ep13

# 清理并重新编译
mvn clean compile

# 预期输出：
# [INFO] ------------------------------------------------------------------------
# [INFO] BUILD SUCCESS
# [INFO] ------------------------------------------------------------------------
```

**验证要点**：
- 检查编译输出是否包含 `[INFO] BUILD SUCCESS`
- 检查是否有新的编译错误或警告
- 确保所有新增文件都正确编译

---

**第 2 层：单元测试验证**

```bash
# 进入 EP 目录
cd ep13

# 运行所有测试
mvn test

# 预期输出：
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
# [INFO] ------------------------------------------------------------------------
# [INFO] BUILD SUCCESS
```

**验证要点**：
- 检查测试通过数量（Failures 和 Errors 应为 0）
- 检查是否有新的测试失败
- 确保所有新增的测试都通过

---

**第 3 层：运行示例程序验证**

```bash
# 进入 EP 目录
cd ep13

# 编译并运行示例程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.Calc" \
    -Dexec.args="test_expr.txt"

# 预期输出：
# Result : 14.0
# Result : 10.0
# ...
```

**验证要点**：
- 检查输出是否与预期一致
- 检查是否所有表达式都正确计算
- 检查变量赋值和引用是否正确

---

#### 编译验证

```bash
# 清理并重新编译
cd ep13
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

**验证要点**：
- 检查是否编译成功
- 检查是否有新的编译错误
- 确保所有依赖都已正确解析

---

#### 手工检查点

即使所有测试通过，我们仍需手工检查：

**1. 代码风格符合规范**

- 包命名：`org.teachfx.antlr4.ep13.*`
- 类命名：PascalCase（如 `PowerNode`、`ModuloNode`）
- 方法命名：camelCase（如 `visit(PowerNode node)`）
- 导入顺序：
  ```java
  // 1. ANTLR4 imports
  import org.antlr.v4.runtime.CharStreams;
  import org.antlr.v4.runtime.CommonTokenStream;

  // 2. External library imports
  import java.util.HashMap;
  import java.util.Map;

  // 3. Internal project imports
  import org.teachfx.antlr4.ep13.ast.ExpressionNode;
  import org.teachfx.antlr4.ep13.ast.PowerNode;
  ```

**2. 没有引入新的编译错误**

- 查看项目根目录的 `mvn clean compile` 输出
- 确认没有新的 ERROR 或 WARNING
- 确保所有新增代码都正确编译

**3. 没有破坏现有功能**

- 运行 EP 依赖的所有前置测试
- 验证其他章节的示例程序仍能运行
- 确认 Git diff 只包含预期修改

**4. 文档完整性**

- 新增类/方法有 JavaDoc 注解
- 关键算法有时间/空间复杂度说明
- 复杂逻辑有行内注释
- 说明设计考虑和实现细节

---

#### 回滚方案

如果 AI 修改后出现问题，使用以下步骤快速恢复：

**方案 1：Git Stash（推荐）**

```bash
# 保存 AI 修改到 stash（带备注）
git stash push -m "AI changes for chapter 6: AST power operator"

# 如果出现问题，恢复到修改前状态
git stash pop

# 或者完全丢弃 AI 修改
git stash drop
```

**优点**：
- 安全，不会丢失修改
- 可以保留 AI 修改用于学习
- 适合实验性修改

**缺点**：
- 需要手动管理 stash
- 不能确定 stash 的先后顺序

**使用场景**：
- 小规模修改（新增一个 AST 节点）
- 验证性修改（测试 AI 的能力）
- 实验性修改（尝试不同的实现方案）

---

**方案 2：Git Checkout（硬恢复）**

```bash
# 查看修改历史
git log --oneline -5

# 恢复到修改前的提交
git checkout {commit_hash}

# 或者恢复特定文件
git checkout HEAD~1 -- ep13/src/main/java/org/teachfx/antlr4/ep13/ast/PowerNode.java
```

**优点**：
- 快速，适合小规模修改
- 明确的回滚点

**缺点**：
- 可能丢失未提交的修改
- 不适合实验性修改

**使用场景**：
- 小规模修改（只修改 1-2 个文件）
- 明确知道要恢复到哪个提交
- 不需要保留 AI 修改

---

**方案 3：创建新分支实验**

```bash
# 从干净状态创建新分支
git checkout -b ai-experiment-chapter6

# 让 AI 在新分支工作
# 完成后合并回主分支
git checkout book-writing-20260112
git merge ai-experiment-chapter6
```

**优点**：
- 隔离主分支，最安全
- 可以对比 AI 修改和原代码
- 适合复杂实验

**缺点**：
- 需要管理多个分支
- 合并时可能产生冲突

**使用场景**：
- 大规模修改（新增多个 AST 节点）
- 实验性修改（尝试不同的设计方案）
- 需要对比不同实现

---

**方案 4：Git Revert（撤销提交）**

```bash
# 查看提交历史
git log --oneline -5

# 撤销最新的提交（保留修改）
git revert HEAD

# 撤销特定的提交
git revert {commit_hash}
```

**优点**：
- 记录撤销历史
- 适合已提交的修改
- 可以保留撤销前的提交

**缺点**：
- 如果修改未提交，无法使用
- 可能产生冲突

**使用场景**：
- AI 修改已提交到 Git
- 需要保留撤销历史
- 多人协作场景

---

**选择建议**：

| 修改规模 | 推荐方案 | 说明 |
|---------|-----------|------|
| 小规模修改（1-2 个文件） | Git Stash 或 Checkout | 快速、简单 |
| 验证性修改（测试 AI 能力） | Git Stash | 可以保留 AI 修改用于学习 |
| 大规模修改（多个文件） | 创建新分支 | 隔离主分支，最安全 |
| 实验性修改（尝试不同方案） | 创建新分支 | 可以对比不同实现 |
| 已提交的修改 | Git Revert | 记录撤销历史 |

---

## 5. 练习题

### 练习 1：实现取模运算符 %（手工实现版）

**难度**：⭐⭐⭐☆☆
**预计时间**：45–60 分钟

**题目描述**：
在数学表达式语言中添加取模运算符（`%`），使其支持 `a % b` 语法，计算余数。

**要求**：

1. 完全手工实现，不依赖 AI
2. 参考本章的 AdditionNode 等节点设计模式
3. 在 InfixExpressionNode 中添加 ModuloNode 节点
4. 在 BuildAstVisitor 中实现取模表达式构建
5. 在 EvalExprVisitor 中实现取模计算逻辑
6. 测试 `10 % 3` 应该输出 `1.0`（`10` 除以 `3` 的余数）

**验收标准**：

- [ ] 创建了 ModuloNode.java 类，继承 InfixExpressionNode
- [ ] 更新了 BuildAstVisitor，支持取模构建（visitInfixExpr 中添加 case 分支）
- [ ] 更新了 EvalExprVisitor，支持取模计算（visit(ModuloNode node) 方法）
- [ ] 更新了 ASTVisitor，添加 visit(ModuloNode node) 方法声明
- [ ] 所有代码通过 `mvn clean compile`
- [ ] 测试 `10 % 3` 输出 `1.0`
- [ ] 测试 `15 % 4` 输出 `3.0`
- [ ] 测试 `7 % 2` 输出 `1.0`

**💡 解题思路提示**：

1. **创建 ModuloNode 类**：
   - 参考 AdditionNode 的实现模式
   - 继承 InfixExpressionNode
   - 提供无参和有参构造函数

2. **修改 BuildAstVisitor**：
   - 在 Math.g4 中添加 `OP_MOD: '%'` 词法规则（如果需要）
   - 在 visitInfixExpr 中添加 `case MathLexer.OP_MOD` 分支
   - 创建 ModuloNode 节点

3. **修改 EvalExprVisitor**：
   - 添加 visit(ModuloNode node) 方法
   - 使用 `%` 运算符：`visit(node.left) % visit(node.right)`
   - 注意处理除零错误（返回 null 或抛出异常）

4. **修改 ASTVisitor**：
   - 添加 `T visit(ModuloNode node)` 方法声明

5. **测试**：
   - 编译：`mvn clean compile`
   - 运行：`mvn exec:java -Dexec.args="test_modulo.txt"`
   - 创建测试文件：`10 % 3`

---

### 练习 2：设计 AI 协作上下文（AI 协作版）

**难度**：⭐⭐⭐⭐☆
**预计时间**：60–90 分钟

**题目描述**：
假设你需要让 AI 帮你在 EP13 中实现一个优化——**常量折叠优化**（在构建 AST 时直接计算常量表达式）。

**AI 协作要求**：

1. **设计上下文**：列出源码文件、文档、测试文件
   - 至少包含 5 个源码文件
   - 至少包含 1 个文档文件
   - 至少包含 1 个测试文件（如果有）

2. **设计 Prompt**：定义任务目标、约束条件、期望输出
   - 任务目标：实现常量折叠优化
   - 约束条件：至少 5 条
   - 期望输出：至少 4 项
   - 验证方法：至少 4 步

3. **设计验证策略**：说明如何验证 AI 生成的代码
   - 编译验证
   - 测试验证
   - 运行示例程序验证
   - 手工检查点

4. **参考 4.1 节和 4.2 节的 Prompt 模板**

**验收标准**：

- [ ] 上下文文件列表完整（源码、文档、测试）
- [ ] Prompt 包含所有 6 个要素（任务背景、任务目标、约束条件、参考文件、期望输出、验证方法）
- [ ] 你能解释为什么选择这些文件作为上下文（至少 100 字）
- [ ] 你能解释为什么设计这样的 Prompt（至少 100 字）
- [ ] 验证策略完整（4 层：编译、测试、运行示例、手工检查）
- [ ] Prompt 格式清晰，易于复制给 AI

**💡 解题思路提示**：

1. **从 4.1 节开始**：
   - 参考"上下文设计"部分的 4 个层次
   - 项目宏观上下文 → 模块级上下文 → 任务级上下文 → 实现细节上下文

2. **参考本章的上下文设计示例**：
   - 4.1.1 → 4.1.2 → 4.1.3 → 4.1.4
   - 理解为什么要提供这些文件

3. **思考常量折叠优化需要哪些上下文**：
   - BuildAstVisitor（在哪里实现优化）
   - NumberNode（检测常量）
   - AdditionNode 等运算节点（检测二元运算）
   - Math.g4（理解语法结构）

4. **参考 4.2 节的 Prompt 模板设计**：
   - 任务背景、任务目标、约束条件、参考文件、期望输出、验证方法
   - 每个部分都要详细说明

---

### 练习 3：实现常量折叠优化（综合挑战）

**难度**：⭐⭐⭐⭐⭐
**预计时间**：90–120 分钟

**题目描述**：
在 BuildAstVisitor 中实现常量折叠优化：在构建 AST 时，直接计算 `2 + 3` 为 `NumberNode(5)`，而不是在运行时计算。

**要求**：

1. 可以选择手工实现或 AI 协作版
2. 如果选择 AI 协作版，需要：
   - 设计完整的上下文（参考练习 2）
   - 设计详细的 Prompt（参考练习 2）
   - 记录 AI 协作过程
   - 验证优化后代码的正确性
3. 常量折叠规则：
   - 只优化二元运算的两个操作数都是常量（NumberNode）
   - 不优化包含变量的表达式（如 `x + 3 * 4` 中的 `3 * 4` 不优化）
   - 括号内的常量表达式也要优化（如 `(2 + 3) * 4` 中的 `2 + 3` 优化为 `NumberNode(5)`）

**验收标准**：

- [ ] 修改了 BuildAstVisitor，添加常量折叠检测
- [ ] 优化正确（`2 + 3` 创建一个 NumberNode(5)，而不是 AdditionNode）
- [ ] 不优化其他情况（如 `x + 3` 不优化，`x + 3 * 4` 中的 `3 * 4` 不优化）
- [ ] 所有代码通过编译（`mvn clean compile`）
- [ ] 测试：输入 `2 + 3 * 4` 和 `(2 + 3) * 4` 输出相同结果（20.0）
- [ ] 测试：输入 `x = 5; x + 10` 输出 `15.0`（变量表达式不优化）
- [ ] （AI 协作版）记录了 AI 协作过程（上下文、Prompt、AI 输出、验证结果）

**💡 解题思路提示**：

1. **在 visitInfixExpr 中添加常量折叠检测**：
   - 在创建运算符节点前，检查左右子节点是否都是 NumberNode
   - 如果都是 NumberNode，直接计算结果，返回 NumberNode
   - 如果不是，正常创建运算符节点

2. **参考 AdditionNode 等现有节点的设计模式**：
   - 使用 instanceof 检测节点类型
   - 使用 NumberNode.value 获取常量值
   - 根据运算符类型计算结果

3. **实现逻辑示例**：
   ```java
   @Override
   public ExpressionNode visitInfixExpr(InfixExprContext ctx) {
       // 先递归构建左右子节点
       ExpressionNode left = visit(ctx.left);
       ExpressionNode right = visit(ctx.right);

       // 常量折叠检测
       if (left instanceof NumberNode && right instanceof NumberNode) {
           double leftValue = ((NumberNode) left).value;
           double rightValue = ((NumberNode) right).value;
           double result;

           // 根据运算符计算结果
           switch (ctx.op.getType()) {
               case MathLexer.OP_ADD:
                   result = leftValue + rightValue;
                   break;
               // ... 其他运算符
           }

           return new NumberNode(result);
       }

       // 非常量表达式，正常创建运算符节点
       // ... 原有逻辑
   }
   ```

4. **在 ASTVisitor 中创建常量节点工厂方法**（可选）：
   - 提取常量计算逻辑到独立方法
   - 提高代码复用性

5. **测试**：
   - 常量表达式：`2 + 3` → `NumberNode(5)`
   - 变量表达式：`x + 3` → `AdditionNode(VarNode(x), NumberNode(3))`
   - 混合表达式：`x + 2 + 3` → `AdditionNode(AdditionNode(VarNode(x), NumberNode(2)), NumberNode(3))`
   - 括号表达式：`(2 + 3) * 4` → `MultiplicationNode(NumberNode(5), NumberNode(4))`

---

## 6. 本章小结与下一章预告

### 本章小结

通过本章的学习，你已经掌握了：

**1. 抽象语法树（AST）的设计原理和优势**

- 理解了 AST 与 ParseTree 的区别（去除语法细节，保留语义）
- 掌握了 AST 节点层次结构设计（基类 → 中缀表达式 → 具体运算节点）
- 学会了为什么需要 AST 而不是直接解释 ParseTree：
  - 更简洁的表示（节点数量减少 30-50%）
  - 易于添加语义信息（类型、作用域等）
  - 适合多次遍历（类型检查、优化、代码生成）
  - 平台无关（不绑定特定解析器）

**2. 访问者模式在编译器中的应用**

- 理解了访问者模式的核心思想（操作与数据结构分离）
- 掌握了 Double Dispatch 的实现方式（两次动态分发）
- 学会了如何通过访问者接口实现不同的操作：
  - 求值器（EvalExprVisitor）：计算表达式结果
  - 类型检查器（后续章节）：验证类型正确性
  - 代码生成器（后续章节）：生成目标代码
- 理解了访问者模式的扩展性（新增访问者无需修改 AST 节点）

**3. AST 构建与求值的完整流程**

- 学会了从 ParseTree 构建 AST（BuildAstVisitor）
  - 继承 ANTLR4 的 `MathBaseVisitor<ExpressionNode>`
  - 重写 `visitXXX()` 方法，创建对应的 AST 节点
  - 递归构建子节点
- 学会了基于 AST 的表达式求值（EvalExprVisitor）
  - 实现 `ASTVisitor<Double>` 接口
  - 递归访问 AST 节点，计算结果
  - 使用 Map 存储变量内存
- 理解了编译器前端流水线的 AST 构建阶段

**4. 工程实践经验**

- 学会了如何设计清晰的 AST 节点层次结构
  - 基类（ExpressionNode）提供类型标记
  - 中间层（InfixExpressionNode）统一处理二元运算
  - 具体节点（AdditionNode 等）表示具体运算
- 掌握了递归算法和访问者模式的结合使用
  - 后序遍历（先计算子节点，再计算父节点）
  - 类型分发（`getClass().equals()`）
- 理解了如何使用 ANTLR4 的 BaseVisitor 简化遍历逻辑
  - `MathBaseVisitor<ExpressionNode>` 提供默认实现
  - 只需重写需要的方法
- 积累了项目编码经验：
  - 类型安全（使用泛型 T）
  - 错误处理（未定义变量、除零错误）
  - 调试技巧（打印 AST 结构）

**【你现在站在哪】**：

```
词法分析 → 语法分析（ParseTree） → ✅ **AST 构建** → [符号解析] → [类型检查] → ...
```

**当前在编译器流水线的位置**：

- 本章位于编译器前端流水线的起始阶段（AST 构建阶段）
- 你现在已经能够构建语义清晰的 AST，为后续符号解析和类型检查奠定了基础
- 掌握了访问者模式，可以方便地扩展新的操作（类型检查、代码生成等）

---

### 下一章预告

**第7章：符号解析与类型系统**

在下一章，我们将学习：

- 如何设计完整的符号表层次结构（全局作用域、局部作用域、作用域链）
- 如何实现变量声明和引用的符号解析
- 如何设计类型系统的基础结构（类型定义、类型推导）
- 如何在 AST 中扩展类型信息，为静态类型检查做准备

**你将能够**：

- 设计清晰的符号表，管理变量和函数的可见性
- 实现符号解析器，处理变量声明和引用
- 实现基础的类型检查器，验证表达式和语句的类型正确性
- 检测未定义变量、重复声明等编译错误

**准备**：

- [ ] 完成本章的所有练习题（取模运算、AI 协作上下文设计、常量折叠优化）
- [ ] 理解 AST 节点类型设计（特别是 ExpressionNode 和 AssignNode）
- [ ] 熟悉访问者模式的使用（EvalExprVisitor 的实现）
- [ ] 理解变量内存的管理机制（Map<String, Double>）
- [ ] 准备好与下一章符号表的集成

**继续加油！** 🚀

你已经掌握了编译器前端的核心技术——AST 构建与访问者模式。下一章将带你进入符号解析的世界，实现变量声明和引用的语义分析！
