# 第6章提示：AST 构建与表达式求值

## 章节写作任务

请为本书撰写一整章内容。

### 本章基本信息

- **章节标题**: 第6章：AST 构建与表达式求值
- **所在模块**: 模块 2：从解释到编译（EP13–EP16）
- **面向读者**: 已实现过基础解释器（第1-5章），理解基本语法解析的工程师
- **读者前置知识**: ANTLR4 基础、ParseTree 理解、访问者模式概念
- **本章对应仓库 EP 范围**: EP13（AST 构建、AST 求值器）
- **本章在全书中的位置**:
  - 前一章：第5章：数组与更复杂的特性（模块 1 终章）
  - 后一章：第7章：符号解析与类型系统
- **本章完成后，读者应该能**:
  - 理解 AST（抽象语法树）的设计原理和与 ParseTree 的区别
  - 掌握访问者模式在编译器中的应用
  - 能够从 ParseTree 构建自定义 AST 节点层次
  - 实现 AST 求值器，理解为什么需要 AST 而不是直接解释 ParseTree

---

## 内容结构要求（必须按顺序给出）

### 1. 本章概述

本章聚焦于从 ParseTree 到抽象语法树（AST）的转换，以及基于 AST 的表达式求值，它是编译器流水线中的前端基础阶段。通过学习本章，你将掌握 AST 设计原理和访问者模式的工程实践，这是后续符号解析、类型检查的基础。

【你现在站在哪】:
词法分析 → 语法分析（ParseTree） → ✅ **AST 构建** → 符号解析 → 类型检查 → ...

### 2. 动机与真实场景

**真实场景**：假设你正在开发一个代码分析工具，需要计算数学表达式并生成可视化依赖图。直接使用 ANTLR4 生成的 ParseTree 会遇到很多语法细节（如括号、分号），干扰核心逻辑分析。

**具体问题**：
- ParseTree 包含过多语法噪音，不便于语义分析
- 需要在多个编译阶段（类型检查、优化、代码生成）复用相同的程序结构
- 直接解释 ParseTree 难以扩展新功能（如类型信息、作用域信息）

如果缺少本章的能力，你将面临：
- 每个编译阶段都要重复遍历 ParseTree，效率低下
- 难以添加额外的语义信息（如类型、作用域）
- 代码分析与代码生成逻辑耦合，难以维护

本章将教你如何：设计清晰的 AST 节点层次结构，使用访问者模式分离遍历逻辑，将 ParseTree 转换为语义清晰的 AST。

### 3. 人类工程师线：技术与实现

#### 3.1 核心概念

**核心概念 1：抽象语法树（AST）**

通俗解释：AST 是去除了语法细节（如括号、分号）的语法树，只保留程序的核心语义结构。它就像一张"语义地图"，告诉你程序"做了什么"而不是"怎么写的"。

[图1：ParseTree 与 AST 对比]
```
ParseTree（包含语法细节）：
expr
  ├─ expr
  │   ├─ '2'
  │   └─ '+'
  │   └─ expr
  │       └─ '('
  │       └─ expr
  │           ├─ '3'
  │           └─ '*'
  │           └─ '4'
  │       └─ ')'
  └─ '5'

AST（去除语法细节）：
      AdditionNode
     /            \
NumberNode(2)  MultiplicationNode
                /             \
          NumberNode(3)   NumberNode(4)
```

图示说明：
- 左侧 ParseTree 保留所有 token 和括号信息
- 右侧 AST 只保留运算符和操作数的关系
- AST 更紧凑，适合后续语义分析

类比理解：
- ParseTree 就像"逐字逐句的录音"，包含所有语气词和停顿
- AST 就像"会议纪要"，只记录关键决策和结论

**核心概念 2：访问者模式（Visitor Pattern）**

通俗解释：访问者模式将数据结构（AST）与操作（求值、类型检查）分离。就像给 AST 节点一个"接待员"，根据访客类型执行不同操作。

[图2：访问者模式结构]
```
ASTVisitor<T> (接口)
  ↑
EvalExprVisitor (具体实现)
  ├─ visit(AdditionNode node)
  ├─ visit(MultiplicationNode node)
  └─ visit(NumberNode node)

ASTNode
  ├─ ExpressionNode
  │   ├─ InfixExpressionNode
  │   │   ├─ AdditionNode
  │   │   └─ MultiplicationNode
  │   └─ NumberNode
```

图示说明：
- ASTNode 接受访问者：`node.accept(visitor)`
- 访问者访问节点：`visitor.visit(node)`
- 双重分发（double dispatch）：节点类型 + 访问者类型共同决定行为

类比理解：
- ASTNode 就像"旅游景点"
- Visitor 就像"导游"
- 不同导游（求值器、类型检查器）带领游客走不同路线，看到不同风景

**相关概念**：
- **ParseTree**：ANTLR4 自动生成的语法树，包含完整语法信息
- **AST**：自定义的抽象语法树，只保留语义信息
- **Visitor Pattern**：将操作与数据结构分离的设计模式
- **Double Dispatch**：两次动态分发实现灵活操作

#### 3.2 与仓库 EP 的对应关系

对应 EP：EP13

目录结构：
```
ep13/
├── src/main/java/org/teachfx/antlr4/ep13/
│   ├── ast/                          // AST 节点层次
│   │   ├── ExpressionNode.java       // 表达式基类
│   │   ├── NumberNode.java           // 数字常量节点
│   │   ├── InfixExpressionNode.java   // 中缀表达式基类
│   │   │   ├── AdditionNode.java     // 加法节点
│   │   │   ├── SubtractionNode.java  // 减法节点
│   │   │   ├── MultiplicationNode.java // 乘法节点
│   │   │   └── DivisionNode.java     // 除法节点
│   │   ├── NegateNode.java           // 一元取反节点
│   │   ├── AssignNode.java           // 赋值节点
│   │   └── VarNode.java              // 变量引用节点
│   ├── Calc.java                     // 主程序（演示编译器流水线）
│   ├── ASTVisitor.java               // 访问者接口
│   ├── BuildAstVisitor.java          // 从 ParseTree 构建 AST
│   ├── EvalExprVisitor.java          // AST 求值器
│   ├── MathParser.java               // ANTLR4 生成的解析器
│   ├── MathLexer.java                // ANTLR4 生成的词法分析器
│   ├── MathBaseVisitor.java           // ANTLR4 基础访问者
│   └── MathVisitor.java               // ANTLR4 访问者接口
├── src/main/antlr4/
│   └── Math.g4                       // 数学表达式语法
└── src/test/java/
    └── (测试文件)
```

**关键类/方法说明**：

**ExpressionNode** - AST 节点基类
```java
package org.teachfx.antlr4.ep13.ast;

/**
 * 表达式 AST 节点基类
 * 所有表达式节点（数字、变量、运算等）都继承此类
 */
public abstract class ExpressionNode {
    // 基类可以包含公共字段和方法
    // 例如：位置信息、类型信息（后续章节添加）
}
```

**NumberNode** - 数字常量节点
```java
package org.teachfx.antlr4.ep13.ast;

/**
 * 数字常量节点
 * 表示程序中的数字字面量，如 42、3.14
 */
public class NumberNode extends ExpressionNode {
    public final double value; // 存储数值

    public NumberNode(double value) {
        this.value = value;
    }
}
```

**InfixExpressionNode** - 中缀表达式基类
```java
package org.teachfx.antlr4.ep13.ast;

/**
 * 中缀表达式基类
 * 表示二元运算表达式（a + b, x * y 等）
 * 左右子节点都是表达式，形成递归结构
 */
public abstract class InfixExpressionNode extends ExpressionNode {
    public ExpressionNode left;  // 左操作数
    public ExpressionNode right; // 右操作数
}
```

**AdditionNode** - 加法节点
```java
package org.teachfx.antlr4.ep13.ast;

/**
 * 加法节点
 * 表示二元加法运算
 */
public class AdditionNode extends InfixExpressionNode {
    // 继承 left、right 字段
    // 不需要额外字段，通过类型区分运算符
}
```

**ASTVisitor<T>** - 访问者接口
```java
package org.teachfx.antlr4.ep13;

import org.teachfx.antlr4.ep13.ast.*;

/**
 * AST 访问者接口
 * 泛型 T 表示访问后的返回类型（求值返回 Double，类型检查返回 Type）
 *
 * 设计考虑：
 * - 每种 AST 节点类型对应一个 visit 方法
 * - 使用方法重载实现多态
 * - 具体访问者实现接口，定义具体行为
 */
public interface ASTVisitor<T> {
    // 语句节点
    T visit(AssignNode node);   // 访问赋值语句
    T visit(VarNode node);      // 访问变量引用

    // 表达式节点
    T visit(AdditionNode node);       // 访问加法
    T visit(SubtractionNode node);    // 访问减法
    T visit(MultiplicationNode node); // 访问乘法
    T visit(DivisionNode node);       // 访问除法
    T visit(NegateNode node);         // 访问一元取反
    T visit(NumberNode node);        // 访问数字常量
    T visit(ExpressionNode node);     // 访问通用表达式（用于类型分发）
}
```

**BuildAstVisitor** - AST 构建器
```java
package org.teachfx.antlr4.ep13;

import org.teachfx.antlr4.ep13.MathParser.*;
import org.teachfx.antlr4.ep13.ast.*;

/**
 * 从 ParseTree 构建 AST
 * 继承 ANTLR4 的 MathBaseVisitor，实现语法树到抽象语法树的转换
 *
 * 职责：
 * - 遍历 ParseTree 的每个节点
 * - 创建对应的 AST 节点
 * - 建立 AST 节点之间的父子关系
 * - 忽略语法细节（如括号）
 */
public class BuildAstVisitor extends MathBaseVisitor<ExpressionNode> {
    @Override
    public ExpressionNode visitCompileUnit(CompileUnitContext ctx) {
        // 编译单元：可以是赋值语句或表达式
        if (ctx.assign() != null) {
            return visit(ctx.assign());  // 返回赋值节点
        }
        return visit(ctx.expr());  // 返回表达式节点
    }

    @Override
    public ExpressionNode visitNumberExpr(NumberExprContext ctx) {
        // 数字表达式：创建 NumberNode
        return new NumberNode(Double.parseDouble(ctx.value.getText()));
    }

    @Override
    public ExpressionNode visitParensExpr(ParensExprContext ctx) {
        // 括号表达式：直接返回内部表达式（AST 不保留括号）
        return visit(ctx.expr());
    }

    @Override
    public ExpressionNode visitInfixExpr(InfixExprContext ctx) {
        // 中缀表达式：根据运算符类型创建对应节点
        InfixExpressionNode node;
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
        node.right = visit(ctx.right);
        return node;
    }

    @Override
    public ExpressionNode visitUnaryExpr(UnaryExprContext ctx) {
        // 一元表达式：处理正负号
        switch (ctx.op.getType()) {
            case MathLexer.OP_ADD:
                // 正号：直接返回内部表达式（忽略）
                return visit(ctx.expr());
            case MathLexer.OP_SUB:
                // 负号：创建取反节点
                return new NegateNode(visit(ctx.expr()));
        }
        return null;
    }

    @Override
    public ExpressionNode visitAssignExpr(AssignExprContext ctx) {
        // 赋值表达式：创建赋值节点
        return new AssignNode(ctx.name.getText(), visit(ctx.value));
    }

    @Override
    public ExpressionNode visitVarExpr(VarExprContext ctx) {
        // 变量表达式：创建变量引用节点
        return new VarNode(ctx.var.getText());
    }
}
```

**EvalExprVisitor** - AST 求值器
```java
package org.teachfx.antlr4.ep13;

import org.teachfx.antlr4.ep13.ast.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AST 求值器
 * 遍历 AST 计算表达式结果
 *
 * 职责：
 * - 维护变量内存（Map<String, Double>）
 * - 递归访问 AST 节点，计算结果
 * - 处理赋值语句，更新变量值
 */
public class EvalExprVisitor implements ASTVisitor<Double> {
    // 变量内存：存储变量名到值的映射
    protected Map<String, Double> memory;

    public EvalExprVisitor() {
        this.memory = new HashMap<>();
    }

    // 加法：递归计算左右操作数，返回和
    @Override
    public Double visit(AdditionNode node) {
        return visit(node.left) + visit(node.right);
    }

    // 减法：递归计算左右操作数，返回差
    @Override
    public Double visit(SubtractionNode node) {
        return visit(node.left) - visit(node.right);
    }

    // 乘法：递归计算左右操作数，返回积
    @Override
    public Double visit(MultiplicationNode node) {
        return visit(node.left) * visit(node.right);
    }

    // 除法：递归计算左右操作数，返回商
    @Override
    public Double visit(DivisionNode node) {
        return visit(node.left) / visit(node.right);
    }

    // 取反：递归计算操作数，返回负值
    @Override
    public Double visit(NegateNode node) {
        return visit(node.innerNode) * (-1);
    }

    // 数字：直接返回值
    @Override
    public Double visit(NumberNode node) {
        return node.value;
    }

    // 通用表达式：根据实际类型分发
    @Override
    public Double visit(ExpressionNode node) {
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

    // 赋值：计算右值，存入内存，返回值
    @Override
    public Double visit(AssignNode node) {
        memory.put(node.varName, visit(node.value));
        return memory.get(node.varName);
    }

    // 变量：从内存中取值
    @Override
    public Double visit(VarNode node) {
        return memory.get(node.name);
    }
}
```

**Calc.java** - 主程序演示
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
 * 1. 字符流 → CharStream
 * 2. 词法分析 → Lexer → TokenStream
 * 3. 语法分析 → Parser → ParseTree
 * 4. AST 构建 → BuildAstVisitor → AST
 * 5. 表达式求值 → EvalExprVisitor → Result
 */
public class Calc {
    public static void main(String[] args) throws IOException {
        String fileName = null;
        if (args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if (fileName != null) is = new FileInputStream(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

        String expr = bufferedReader.readLine();
        int line = 1;
        MathParser parser = new MathParser(null);
        ASTVisitor<Double> astVisitor = new EvalExprVisitor();

        while (expr != null) {
            // 步骤1：创建字符流
            CharStream input = CharStreams.fromString(expr + "\n");

            // 步骤2：词法分析
            MathLexer lexer = new MathLexer(input);
            lexer.setLine(line);
            lexer.setCharPositionInLine(0);
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // 步骤3：语法分析
            parser.setInputStream(tokens);
            ParseTree tree = parser.compileUnit();

            // 步骤4：AST 构建
            ExpressionNode exprAST = tree.accept(new BuildAstVisitor());

            // 步骤5：表达式求值
            System.out.println("Result : " + astVisitor.visit(exprAST));

            expr = bufferedReader.readLine();
            line++;
        }
    }
}
```

**关键设计模式**：访问者模式（Visitor Pattern）

访问者模式的核心思想是将"操作"与"数据结构"分离：

- **数据结构**（AST）：ExpressionNode 及其子类
- **操作**（Visitor）：ASTVisitor 接口及其实现（EvalExprVisitor）

优势：
- 新增操作（如类型检查、代码生成）只需新增 Visitor 实现，无需修改 AST 节点
- 不同操作可以独立演化，互不影响
- 符合开闭原则（对扩展开放，对修改封闭）

类/接口关系：
```
ASTVisitor<T> (接口)
  ↑
EvalExprVisitor (具体实现，用于求值)
TypeCheckerVisitor (具体实现，用于类型检查 - 后续章节)
CodeGeneratorVisitor (具体实现，用于代码生成 - 后续章节)

ExpressionNode (抽象基类)
  ↑
  ├─ NumberNode
  ├─ VarNode
  ├─ AssignNode
  └─ InfixExpressionNode
       ↑
       ├─ AdditionNode
       ├─ SubtractionNode
       ├─ MultiplicationNode
       └─ DivisionNode
```

#### 3.3 实战流程

实战步骤：从 ParseTree 构建并求值表达式

步骤1：编译 EP13 项目
```bash
# 进入 EP13 目录
cd ep13

# 编译项目
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

步骤2：运行主程序
```bash
# 创建测试输入文件
cat > test_expr.txt << EOF
2 + 3 * 4
x = 10
x + 5
(2 + 3) * (4 + 5)
EOF

# 运行主程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep13.Calc" \
    -Dexec.args="test_expr.txt"

# 预期输出：
# Result : 14.0
# Result : 10.0
# Result : 15.0
# Result : 45.0
```

验证方法：
- 检查点1：确认表达式计算正确（遵循运算符优先级）
- 检查点2：确认变量赋值和引用正确
- 检查点3：确认括号正确处理（优先级）

步骤3：添加调试输出（可选）
```bash
# 在 BuildAstVisitor 中添加打印，观察 AST 结构
# 修改 BuildAstVisitor.java，添加：
@Override
public ExpressionNode visitInfixExpr(InfixExprContext ctx) {
    InfixExpressionNode node;
    // ... 现有代码 ...
    System.out.println("Created " + node.getClass().getSimpleName() +
                       " with left=" + node.left +
                       " and right=" + node.right);
    return node;
}

# 重新编译运行
mvn clean compile
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep13.Calc" \
    -Dexec.args="test_expr.txt"

# 预期输出（包含调试信息）：
# Created MultiplicationNode with left=NumberNode@... and right=NumberNode@...
# Created AdditionNode with left=NumberNode@... and right=MultiplicationNode@...
# Result : 14.0
# ...
```

故障排查：

**问题1：编译错误 "cannot find symbol: class MathLexer"**
- 原因：ANTLR4 生成的代码未正确生成
- 解决方法：
  ```bash
  # 清理 target 目录
  mvn clean

  # 确认 pom.xml 中 ANTLR4 Maven 插件配置正确
  cat pom.xml | grep -A 20 "antlr4-maven-plugin"

  # 重新编译
  mvn clean compile
  ```

**问题2：运行时异常 "NullPointerException"**
- 原因：BuildAstVisitor 未正确处理某个 ParseTree 节点
- 解决方法：
  - 在 BuildAstVisitor 的每个 visit 方法开头添加调试输出
  - 确认所有 ParseTree 节点类型都有对应的 AST 转换逻辑
  - 检查 Math.g4 语法规则是否覆盖所有情况

**问题3：求值结果不正确**
- 原因：运算符优先级处理错误或变量内存未正确更新
- 解决方法：
  - 添加调试输出，打印每个子表达式的计算结果
  - 确认 BuildAstVisitor 正确处理了括号（去除括号，不改变优先级）
  - 确认 EvalExprVisitor 的 memory 正确更新（赋值语句）

进阶技巧：

1. **可视化 AST**：在 EvalExprVisitor 中添加打印方法，以树形结构显示 AST
2. **性能优化**：缓存重复子表达式的计算结果（记忆化）
3. **错误处理**：在 EvalExprVisitor 中添加除零检查、未定义变量检查

### 4. AI 协作线：Context Engineering 视角

#### 4.1 上下文设计

为了让 AI 帮助完成本章任务，我们需要精心设计上下文。

上下文文件列表：

**源码文件**（按阅读顺序）：
1. `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/ExpressionNode.java`
   - 作用：AST 节点基类，定义所有表达式节点的共同接口
   - 关键方法：无（抽象基类）

2. `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/NumberNode.java`
   - 作用：数字常量节点，表示程序中的数字字面量
   - 关键方法：构造函数 `NumberNode(double value)`

3. `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/InfixExpressionNode.java`
   - 作用：中缀表达式基类，表示二元运算表达式
   - 关键字段：`left`、`right`（ExpressionNode 类型）

4. `ep13/src/main/java/org/teachfx/antlr4/ep13/ASTVisitor.java`
   - 作用：访问者接口，定义遍历 AST 的统一接口
   - 关键方法：`visit(各种节点类型)`

5. `ep13/src/main/java/org/teachfx/antlr4/ep13/BuildAstVisitor.java`
   - 作用：从 ParseTree 构建 AST 的核心逻辑
   - 关键方法：`visitCompileUnit`、`visitInfixExpr`、`visitNumberExpr`

6. `ep13/src/main/java/org/teachfx/antlr4/ep13/EvalExprVisitor.java`
   - 作用：AST 求值器，计算表达式结果
   - 关键方法：`visit(各种节点类型)` 实现计算逻辑

7. `ep13/src/main/java/org/teachfx/antlr4/ep13/Calc.java`
   - 作用：主程序，演示完整的编译器流水线
   - 关键流程：CharStream → Lexer → Parser → ParseTree → AST → Result

**文档文件**：
1. `ep13/README.md`（如果有）
   - 作用：EP13 模块概述和使用说明
   - 关键章节：项目目标、编译运行指南

2. `AGENTS.md`
   - 作用：代码规范和最佳实践
   - 相关部分：第2章代码风格指南、AST/IR 节点模式

**测试文件**：
1. `ep13/src/test/java/org/teachfx/antlr4/ep13/`（如果有）
   - 作用：测试 AST 构建和求值逻辑
   - 关键测试方法：`testSimpleExpression`、`testVariableAssignment`

**示例输入/输出**：
1. `ep13/src/main/resources/test_expr.txt`（需手动创建）
   - 作用：测试输入表达式
   - 内容：各种数学表达式、变量赋值、括号表达式

2. `ep13/src/main/antlr4/Math.g4`
   - 作用：数学表达式语法定义
   - 关键生产规则：`compileUnit`、`expr`、`assign`

上下文组织说明：

这些文件按照"核心实现 → 支撑代码 → 文档 → 测试"组织：

1. **核心实现在前**：先提供 AST 节点定义（ExpressionNode、NumberNode、InfixExpressionNode），让 AI 理解数据结构
2. **访问者模式在后**：接着提供 ASTVisitor 接口和 BuildAstVisitor、EvalExprVisitor 实现，展示如何遍历和操作 AST
3. **主程序作为整合**：最后提供 Calc.java，展示完整的编译器流水线
4. **语法文件作为参考**：提供 Math.g4，说明 ParseTree 的结构，帮助 AI 理解从 ParseTree 到 AST 的转换逻辑

为什么这样组织：
- AI 可以先理解 AST 的数据结构设计，再理解如何构建和遍历 AST
- 完整的上下文确保 AI 生成的代码符合项目架构（访问者模式）
- 语法文件帮助 AI 理解 ParseTree 的结构，避免在转换时出错

#### 4.2 Prompt 模板（给 AI 用）

**类型 A: AST 节点扩展 Prompt 模板**
```
请为数学表达式语言添加新的 AST 节点，支持求幂运算（`**` 运算符）。

任务目标：
- 在现有 AST 层次中添加求幂运算节点
- 实现 BuildAstVisitor 中的求幂表达式构建逻辑
- 实现 EvalExprVisitor 中的求幂计算逻辑

具体要求：
1. 修改 `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/` 目录
   - 创建 `PowerNode.java`，继承 `InfixExpressionNode`
   - 类似 AdditionNode、MultiplicationNode 的设计模式

2. 更新 `ep13/src/main/java/org/teachfx/antlr4/ep13/ASTVisitor.java`
   - 添加 `visit(PowerNode node)` 方法声明

3. 修改 `ep13/src/main/java/org/teachfx/antlr4/ep13/BuildAstVisitor.java`
   - 在 `visitInfixExpr` 方法中添加 `MathLexer.OP_POW` 分支
   - 创建 PowerNode 实例，设置左右子节点

4. 修改 `ep13/src/main/java/org/teachfx/antlr4/ep13/EvalExprVisitor.java`
   - 添加 `visit(PowerNode node)` 方法实现
   - 使用 `Math.pow(visit(node.left), visit(node.right))` 计算结果

5. 更新 `ep13/src/main/antlr4/Math.g4`（如果需要）
   - 在词法规则中添加 `OP_POW: '**';`

参考上下文文件：
- 源码：
  - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/InfixExpressionNode.java`
  - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/MultiplicationNode.java`
  - `ep13/src/main/java/org/teachfx/antlr4/ep13/BuildAstVisitor.java`
  - `ep13/src/main/java/org/teachfx/antlr4/ep13/EvalExprVisitor.java`
- 文档：
  - `AGENTS.md`（代码规范）
  - `ep13/src/main/antlr4/Math.g4`（语法文件）

约束条件：
- 遵循现有 AST 节点设计模式（继承 InfixExpressionNode）
- 不修改其他 AST 节点或访问者方法
- 保持与现有编译器流水线的兼容性
- 所有新增代码必须通过 `mvn clean compile`

期望输出：
1. PowerNode.java 完整代码
2. 修改后的 ASTVisitor.java（标注新增部分）
3. 修改后的 BuildAstVisitor.java（标注新增部分）
4. 修改后的 EvalExprVisitor.java（标注新增部分）
5. 如果需要，修改后的 Math.g4（标注新增部分）
6. 测试示例：运行 `2 ** 3`、`3 ** 2 + 1` 的预期结果
```

**类型 B: AST 可视化 Prompt 模板**
```
请为 EvalExprVisitor 添加一个方法，以树形结构可视化打印 AST。

任务目标：
- 添加一个 `printAST(ExpressionNode node)` 方法
- 递归遍历 AST，打印节点类型和值
- 使用缩进表示节点层级关系

具体要求：
1. 修改 `ep13/src/main/java/org/teachfx/antlr4/ep13/EvalExprVisitor.java`
   - 添加 `printAST(ExpressionNode node, int indent)` 私有方法
   - 添加 `printAST(ExpressionNode node)` 公共方法（调用私有方法）

2. 实现打印逻辑：
   - NumberNode：打印 `NumberNode(value)`
   - VarNode：打印 `VarNode(name)`
   - AssignNode：打印 `AssignNode(varName=..., value=...)`
   - InfixExpressionNode 子类：
     - AdditionNode：打印 `AdditionNode`
     - SubtractionNode：打印 `SubtractionNode`
     - MultiplicationNode：打印 `MultiplicationNode`
     - DivisionNode：打印 `DivisionNode`
   - 每层增加 2 个空格缩进

3. 在 Calc.java 中调用（演示）：
   - 在 `System.out.println("Result : " + ...)` 前添加
   - `astVisitor.printAST(exprAST);`

参考上下文文件：
- 源码：
  - `ep13/src/main/java/org/teachfx/antlr4/ep13/EvalExprVisitor.java`
  - `ep13/src/main/java/org/teachfx/antlr4/ep13/Calc.java`
  - `ep13/src/main/java/org/teachfx/antlr4/ep13/ast/*.java`
- 文档：
  - `AGENTS.md`（代码规范）

约束条件：
- 不修改 EvalExprVisitor 的现有求值逻辑
- 使用递归遍历，保持简洁
- 打印格式清晰易读，便于调试

期望输出：
1. 修改后的 EvalExprVisitor.java（标注新增方法）
2. 修改后的 Calc.java（标注新增调用）
3. 运行示例：输入 `2 + 3 * 4` 时的打印输出：
   ```
   AdditionNode
     NumberNode(2.0)
     MultiplicationNode
       NumberNode(3.0)
       NumberNode(4.0)
   Result : 14.0
   ```
4. 运行命令：`mvn clean compile && mvn exec:java ...`
```

#### 4.3 AI 应该做 / 不该做

在让 AI 帮助完成本章任务时，我们需要明确 AI 的职责边界。

**✅ AI 允许做的事情**：

1. **实现明确界定的 AST 节点**
   - 可以：添加新的 AST 节点类（如 PowerNode、ModuloNode）
   - 可以：在 BuildAstVisitor 中添加对应的转换逻辑
   - 不能：修改现有 AST 节点的核心设计（如 InfixExpressionNode 的 left/right 结构）

2. **实现访问者方法**
   - 可以：在 EvalExprVisitor 中添加新节点类型的求值逻辑
   - 可以：实现新的 Visitor（如 PrintVisitor、OptimizeVisitor）
   - 不能：修改 ASTVisitor 接口的已有方法签名

3. **添加辅助方法**
   - 可以：添加 AST 可视化、打印、调试方法
   - 可以：添加错误检查（除零检查、未定义变量检查）
   - 不能：修改 EvalExprVisitor 的核心求值逻辑

4. **生成测试用例**
   - 可以：生成单元测试，验证 AST 构建正确性
   - 可以：生成集成测试，验证求值结果正确性
   - 不能：删除或破坏现有测试

5. **优化 AST 构建**
   - 可以：添加常量折叠（如 `2 + 3` → `5`）
   - 可以：添加表达式化简（如 `x * 1` → `x`）
   - 不能：改变 AST 的语义（如改变运算符优先级）

**❌ AI 禁止做的事情**：

1. **修改访问者模式的核心设计**
   - 不允许：将 `ASTVisitor<T>` 接口改为抽象类
   - 不允许：移除 `visit(ExpressionNode node)` 方法
   - 原因：访问者模式是后续类型检查、代码生成的基础

2. **改变 AST 节点的继承层次**
   - 不允许：将 `InfixExpressionNode` 的 left/right 字段改为 children 列表
   - 不允许：删除 `ExpressionNode` 基类
   - 原因：AST 层次设计是编译器前端的基础结构

3. **删除内存管理**
   - 不允许：移除 EvalExprVisitor 的 `memory` 字段
   - 不允许：修改变量赋值逻辑
   - 原因：变量内存是表达式求值的核心机制

4. **引入新的外部依赖**
   - 不允许：在 pom.xml 中添加新库
   - 不允许：使用第三方 AST 库（如 Eclipse JDT）
   - 原因：保持项目技术栈一致性，学习纯手工 AST 实现

5. **破坏编译器流水线**
   - 不允许：修改 Calc.java 的流水线顺序
   - 不允许：跳过某个阶段（如不构建 AST，直接解释 ParseTree）
   - 原因：流水线结构是编译器架构的核心

**🤝 灰色区域（需谨慎处理）**：

1. **添加 AST 优化但保持语义等价**
   - 可以：在 BuildAstVisitor 中实现常量折叠
   - 需谨慎：确保优化前后程序行为一致

2. **添加错误处理但不过度复杂**
   - 可以：添加除零检查、未定义变量检查
   - 需谨慎：错误处理不应过于复杂，影响代码可读性

如果 AI 提议超出范围的操作，请：
- 明确拒绝并说明原因
- 要求 AI 在当前范围内完成任务
- 保留超出范围的建议，待后续章节讨论

#### 4.4 验证与回滚策略

在 AI 完成修改后，我们必须进行严格的验证才能将更改合并到主分支。

## 自动化验证

**步骤1：运行相关测试**
```bash
# 进入 EP13 目录
cd ep13

# 运行所有测试（如果有）
mvn test

# 或者运行特定测试类
mvn test -Dtest={测试类名}Test

# 预期输出：
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
```

**关键测试**：
- `ASTBuildingTest`（如果有）- 验证 AST 构建正确性
- `ExpressionEvaluationTest`（如果有）- 验证求值结果正确性
- `IntegrationTest`（如果有）- 验证端到端流程

**验证标准**：
- ✅ 所有测试通过（Failures: 0, Errors: 0）
- ✅ 没有新的编译警告
- ✅ 求值结果符合预期（与手工计算一致）

**步骤2：编译验证**
```bash
# 清理并重新编译
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

**步骤3：运行示例程序**
```bash
# 创建测试输入文件
cat > test_ai.txt << EOF
2 + 3 * 4
x = 10
x + 5
(2 + 3) * (4 + 5)
EOF

# 运行主程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep13.Calc" \
    -Dexec.args="test_ai.txt"

# 预期输出：
# Result : 14.0
# Result : 10.0
# Result : 15.0
# Result : 45.0
```

## 手工检查点

即使所有测试通过，我们仍需手工检查以下关键点：

**检查1：代码风格符合规范**
- [ ] 包命名：`org.teachfx.antlr4.ep13.ast`、`org.teachfx.antlr4.ep13`
- [ ] 类命名：PascalCase（如 `PowerNode`、`BuildAstVisitor`）
- [ ] 方法命名：camelCase（如 `visitInfixExpr`、`printAST`）
- [ ] 导入顺序：ANTLR4 → 外部库 → 内部 → Java stdlib

**检查2：没有引入新的编译错误**
- [ ] 查看项目根目录的 `mvn clean compile` 输出
- [ ] 确认没有新的 ERROR 或 WARNING
- [ ] 检查 AI 修改的类是否能正常编译

**检查3：没有破坏现有功能**
- [ ] 运行原有测试（如果有），确保不失败
- [ ] 验证示例程序仍能正确求值
- [ ] 确认 Git diff 只包含预期修改

**检查4：文档完整性**
- [ ] 新增类/方法有 JavaDoc 注解
- [ ] 关键算法有时间/空间复杂度说明（如适用）
- [ ] 复杂逻辑有行内注释

## 回滚方案

如果 AI 修改后出现问题，使用以下步骤快速恢复：

### 方案1：Git Stash（推荐）

```bash
# 查看当前修改
git status

# 保存 AI 修改到 stash（带备注）
git stash push -m "AI changes for AST power operator"

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
git checkout HEAD~1 -- ep13/src/main/java/org/teachfx/antlr4/ep13/ast/PowerNode.java
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
git checkout -b ai-experiment-ast-power

# 让 AI 在新分支工作
# 完成后合并回主分支
git checkout main
git merge ai-experiment-ast-power
```

## 验证流程总结

```
AI 生成代码
    ↓
自动化验证（mvn test + mvn compile）
    ↓
手工检查（代码风格、功能完整性）
    ↓
运行示例程序
    ↓
✅ 验证通过，合并到主分支
    ❌ 验证失败，执行回滚
```

## 常见问题排查

**问题1：测试失败**
- 现象：`[ERROR] Tests run: X, Failures: Y`
- 排查：
  1. 查看测试失败信息
  2. 检查 AI 生成的 AST 节点是否正确
  3. 确认求值逻辑是否符合预期
- 解决：根据失败信息修改 AI 代码，或调整测试用例

**问题2：编译错误**
- 现象：`[ERROR] COMPILATION ERROR`
- 排查：
  1. 检查 AI 是否使用了错误的包名
  2. 确认类名是否正确
  3. 验证继承关系是否正确
- 解决：清理 target 目录重新编译，或修正代码

**问题3：求值结果不正确**
- 现象：程序输出与预期不符
- 排查：
  1. 添加调试输出，打印 AST 结构
  2. 检查 BuildAstVisitor 是否正确构建 AST
  3. 确认 EvalExprVisitor 的求值逻辑
- 解决：根据调试信息修正代码

### 5. 练习题

本章练习题

练习1：实现取模运算符（手工实现版）

难度：⭐⭐☆☆☆
预计时间：30–45 分钟

题目描述：
在数学表达式语言中添加取模运算符（`%` 运算符），使其支持 `a % b` 语法，计算余数。

要求：
- 完全手工实现，不依赖 AI
- 遵循本章学到的访问者模式
- 参考现有 InfixExpressionNode 子类（如 AdditionNode）的设计

验收标准：
- [ ] 创建 ModuloNode.java，继承 InfixExpressionNode
- [ ] 更新 BuildAstVisitor，处理取模运算符
- [ ] 更新 EvalExprVisitor，实现取模计算逻辑
- [ ] 代码能编译通过
- [ ] 测试 `7 % 3` 输出 `1.0`，`10 % 4` 输出 `2.0`

**💡 解题思路提示**：
- 参考 AdditionNode 的实现，创建 ModuloNode 类
- 在 Math.g4 中添加 `OP_MOD: '%';` 词法规则（如果需要）
- 在 BuildAstVisitor.visitInfixExpr 中添加 `case MathLexer.OP_MOD` 分支
- 在 EvalExprVisitor 中使用 `visit(node.left) % visit(node.right)` 计算余数

---

练习2：实现 AST 可视化（AI 协作版）

难度：⭐⭐⭐☆☆
预计时间：45–60 分钟

题目描述：
为 EvalExprVisitor 添加一个 `printAST(ExpressionNode node)` 方法，以树形结构可视化打印 AST，方便调试。

AI 协作要求：
1. 设计上下文：列出需要提供给 AI 的文件和说明
2. 设计 Prompt：参考本章的 Prompt 模板，设计适合此任务的 Prompt
3. 验证 AI 输出：使用本章的验证策略
4. 理解 AI 代码：确保你能解释 AI 生成的每一部分

验收标准：
- [ ] AI 生成的代码能编译通过
- [ ] 打印格式清晰易读，缩进表示层级
- [ ] 测试 `2 + 3 * 4` 能正确打印 AST 树形结构
- [ ] 你能解释 AI 代码的实现逻辑

**💡 解题思路提示**：
- 上下文：提供 EvalExprVisitor.java、AST 节点类文件
- Prompt：参考本章"类型 B: AST 可视化 Prompt 模板"
- 验证：运行示例程序，检查打印输出是否正确
- 关键点：递归遍历 AST，使用 `indent` 参数控制缩进

---

练习3：实现常量折叠优化（综合挑战）

难度：⭐⭐⭐⭐☆
预计时间：60–90 分钟

题目描述：
在 BuildAstVisitor 中实现常量折叠优化，在构建 AST 时直接计算常量表达式，减少运行时计算开销。

例如：
- `2 + 3` → `NumberNode(5)`（在构建时计算）
- `3 * 4` → `NumberNode(12)`
- 但 `x + 3` 不优化（x 是变量）

要求：
- 可以选择手工实现或 AI 协作
- 如果选择 AI 协作，需要详细记录协作过程
- 提交时说明 AI 参与部分和人工修改部分

验收标准：
- [ ] 优化后 `2 + 3 * 4` 的 AST 只包含一个 `NumberNode(14.0)`
- [ ] 不优化包含变量的表达式（如 `x + 3`）
- [ ] 功能完整，性能合理
- [ ] 代码可读性良好
- [ ] 有完整的测试覆盖

**💡 解题思路提示**：
- 在 BuildAstVisitor 的 visitInfixExpr 方法中添加优化逻辑
- 判断左右子节点是否都是 NumberNode
- 如果是，创建新的 NumberNode，存储计算结果
- 如果否，按原逻辑构建 InfixExpressionNode
- 添加测试用例，验证优化正确性

---

练习4：实现错误检测（进阶练习 - 可选）

难度：⭐⭐⭐⭐⭐
预计时间：90–120 分钟

题目描述：
在 EvalExprVisitor 中添加错误检测机制，检测并报告以下错误：
1. 除零错误（如 `1 / 0`）
2. 未定义变量引用（如访问未赋值的变量）
3. 变量重复赋值（可选）

适合人群：
- 想深入理解表达式求值错误处理的高级读者
- 有志于实现完整编译器前端错误报告的读者

**💡 解题思路提示**：
- 在 EvalExprVisitor 的 visit(DivisionNode) 中添加除零检查
- 在 visit(VarNode) 中添加变量存在性检查
- 定义自定义异常类（如 `EvaluationException`）
- 抛出异常并附带错误信息（包括位置信息）
- 在 Calc.java 中捕获异常，打印友好的错误消息

### 6. 本章小结与下一章预告

## 本章小结

通过本章的学习，你已经掌握了：

1. **抽象语法树（AST）设计**
   - 理解了 AST 与 ParseTree 的区别（去除语法细节，保留语义）
   - 掌握了 AST 节点层次设计（ExpressionNode 基类 → InfixExpressionNode → 具体运算节点）
   - 学会了为什么需要 AST 而不是直接解释 ParseTree

2. **访问者模式实践**
   - 学会了访问者模式的核心思想（操作与数据结构分离）
   - 掌握了双重分发（Double Dispatch）的实现方式
   - 能够独立实现 ASTVisitor 接口和具体访问者（如 EvalExprVisitor）

3. **AST 构建与求值**
   - 熟练使用了 BuildAstVisitor 从 ParseTree 构建 AST
   - 掌握了 EvalExprVisitor 递归求值表达式的方法
   - 理解了编译器流水线的前端基础（CharStream → Lexer → Parser → ParseTree → AST → Result）

【你现在站在哪】:
```
词法分析 → 语法分析（ParseTree） → ✅ **AST 构建** → [符号解析] → [类型检查] → ...
```

**当前在编译器流水线的位置**：
- 本章实现了编译器流水线的 AST 构建和求值阶段
- 生成了语义清晰的 AST，为后续符号解析、类型检查奠定基础

**与下一章的衔接**：
- 本章的 AST 将在下一章被用于符号解析和类型检查
- AST 节点将扩展类型信息（`type` 字段）
- 访问者模式将继续用于实现符号解析器和类型检查器

## 下一章预告

**第7章：符号解析与类型系统**

在下一章，我们将学习：
- 多遍编译架构的必要性（为什么不能一次完成所有分析）
- 符号表设计（如何管理变量和函数的作用域）
- 类型系统基础（如何定义和检查类型）
- Parser Actions（如何在解析过程中嵌入符号表操作）

你将能够：
- 设计完整的符号表层次结构（全局作用域、局部作用域）
- 实现变量声明和引用的符号解析
- 实现基础类型检查（int、float 类型）
- 理解多遍编译的架构设计

**准备**：为了学习下一章，建议：
- [ ] 复习本章的访问者模式实现
- [ ] 运行本章的示例程序，加深对 AST 的理解
- [ ] 阅读下一章的预备材料（符号表、作用域概念）

继续加油！下一章将带你进入编译器前端的语义分析阶段。
