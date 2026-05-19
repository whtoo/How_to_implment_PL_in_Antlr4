# 第7章提示：符号解析与类型系统

## 章节写作任务

请为本书撰写一整章内容。

### 本章基本信息

- **章节标题**: 第7章：符号解析与类型系统
- **所在模块**: 模块 2：从解释到编译（EP13–EP16）
- **面向读者**: 已理解 AST 和访问者模式（第6章），学习编译器前端语义分析的工程师
- **读者前置知识**: AST 节点层次、访问者模式、多遍编译概念
- **本章对应仓库 EP 范围**: EP14（符号表、类型系统、Parser Actions）
- **本章在全书中的位置**:
  - 前一章：第6章：AST 构建与表达式求值
  - 后一章：第8章：完整类型检查与多遍编译架构
- **本章完成后，读者应该能**:
  - 理解多遍编译架构的必要性和设计原则
  - 掌握符号表的设计和作用域管理
  - 实现基础类型系统（int、float 类型）
  - 理解 Parser Actions 在编译器中的应用

---

## 内容结构要求（必须按顺序给出）

### 1. 本章概述

本章聚焦于符号表和类型系统，它是编译器流水线中的语义分析基础阶段。通过学习本章，你将掌握多遍编译架构的设计原则、符号表的层次结构、以及类型系统的基础实现，这是静态分析和类型检查的基石。

【你现在站在哪】:
... → AST 构建 → ✅ **符号解析** → 类型检查 → ...

### 2. 动机与真实场景

**真实场景**：假设你正在开发一个静态代码分析工具，需要检测未定义变量引用、类型不匹配等错误。直接在单遍解析中完成所有分析会面临"前向引用"问题（变量在使用后才声明）。

**具体问题**：
- 程序中可能在使用变量后才声明（如 C 语言允许前向引用）
- 需要在多个阶段分别处理不同语义（符号解析、类型检查）
- 单遍分析难以处理复杂的类型推导和作用域嵌套

如果缺少本章的能力，你将面临：
- 无法正确处理前向引用（`f() { ... }` 在 `void f()` 之前调用）
- 无法检测类型错误（如将 `int` 赋值给 `float`）
- 变量作用域管理混乱，全局和局部变量冲突

本章将教你如何：设计多遍编译架构，构建层次化符号表，实现基础类型系统，使用 Parser Actions 嵌入语义分析。

### 3. 人类工程师线：技术与实现

#### 3.1 核心概念

**核心概念 1：多遍编译（Multi-pass Compilation）**

通俗解释：多遍编译是指将编译过程分成多个阶段（遍），每个阶段完成特定任务，后一阶段利用前一阶段的结果。

[图1：单遍 vs 多遍编译对比]
```
单遍编译（单阶段）：
源代码
  ↓
解析 + 符号解析 + 类型检查（全部在解析时完成）
  ↓
问题：遇到前向引用时无法解析（如调用未声明的函数）

多遍编译（多阶段）：
源代码
  ↓
解析（第1遍） → ParseTree
  ↓
符号定义（第2遍） → 填充符号表
  ↓
符号解析（第3遍） → 为变量引用查找符号
  ↓
类型检查（第4遍） → 验证类型正确性
  ↓
优势：每遍专注一个任务，可以前向引用，便于扩展
```

图示说明：
- 左侧：单遍编译在解析时完成所有分析，遇到前向引用无法处理
- 右侧：多遍编译分阶段完成，第2遍定义符号，第3遍解析引用

类比理解：
- 单遍编译就像"一边读小说一边写书评"，读到一半才知道谁是主角
- 多遍编译就像"先通读小说，再写书评"，了解全貌后再分析

**核心概念 2：符号表（Symbol Table）**

通俗解释：符号表是编译器管理程序中名称（变量、函数、类型）的数据结构，就像一本"程序字典"，记录每个名称的含义。

[图2：符号表层次结构]
```
符号表层次结构：
GlobalScope（全局作用域）
  ├─ int (TypeSymbol)
  ├─ float (TypeSymbol)
  ├─ main (MethodSymbol)
  │   └─ x (VariableSymbol: int)
  │   └─ y (VariableSymbol: float)
  └─ print (MethodSymbol)
      └─ value (VariableSymbol: float)

作用域链（嵌套作用域）：
GlobalScope
  ├─ main (MethodScope)
  │   ├─ x (VariableSymbol: int)
  │   └─ y (VariableSymbol: float)
  │   └─ LocalScope（if 块）
  │       └─ z (VariableSymbol: int)
  └─ print (MethodScope)
      └─ value (VariableSymbol: float)
```

图示说明：
- 全局作用域包含所有函数和全局类型
- 每个函数有自己的作用域，包含参数和局部变量
- 代码块（如 if、while）创建局部作用域
- 作用域链：从内向外查找符号

类比理解：
- 符号表就像"公司组织架构"：
  - 公司（全局作用域）包含所有部门（函数）
  - 每个部门有员工（变量）
  - 子部门（代码块）有临时工（局部变量）

**核心概念 3：类型系统（Type System）**

通俗解释：类型系统是一组规则，定义程序中值和表达式的类型，以及类型如何相互操作。

[图3：类型系统基础]
```
类型层次结构：
Type（类型基类）
  ├─ BuiltInTypeSymbol（内置类型）
  │   ├─ int (整数类型)
  │   ├─ float (浮点类型)
  │   ├─ char (字符类型)
  │   └─ void (无返回值)
  └─ (用户自定义类型 - 后续章节)

类型规则示例：
1. 类型兼容性：
   - int + int → int
   - float + float → float
   - int + float → float (隐式转换)

2. 类型检查：
   - int 变量 = float 字面量 ❌ (需要显式转换)
   - float 变量 = int 字面量 ✅ (隐式转换)

3. 类型推导：
   - int x; // x 的类型为 int
   - int x = 5; // x 的类型为 int，初始值为 5
```

图示说明：
- 类型系统定义了所有可用类型
- 类型规则决定了哪些操作是合法的
- 类型检查在编译时验证类型正确性

类比理解：
- 类型系统就像"仓库分类规则"：
  - 整数（苹果）只能和整数放在一起
  - 浮点数（橙子）可以和整数混放（但有规则）
  - 类型检查就像检查仓库分类是否正确

**核心概念 4：Parser Actions（解析器动作）**

通俗解释：Parser Actions 是在 ANTLR4 语法规则中嵌入的代码，在解析过程中执行，将语法分析与语义分析结合。

[图4：Parser Actions 示例]
```
语法规则（不含 Parser Actions）：
varDeclaration
    : type ID ';'
    ;

语法规则（含 Parser Actions）：
varDeclaration
    : type ID {
          // 解析到类型和变量名时，执行以下代码
          Type varType = $type.ctx.type;
          String varName = $ID.text;
          VariableSymbol var = new VariableSymbol(varName, varType);
          currentScope.define(var); // 将变量添加到当前作用域
      }
    ';'
    ;
```

图示说明：
- 左侧：纯语法规则，不包含语义分析
- 右侧：在规则中嵌入代码，定义变量时立即添加到符号表

类比理解：
- Parser Actions 就像"边读书边做笔记"：
  - 不含 Actions：读完再整理笔记（多遍编译）
  - 含 Actions：边读边写笔记（单遍编译）

**相关概念**：
- **Symbol（符号）**：程序中的名称（变量、函数、类型）
- **Scope（作用域）**：符号的可见范围
- **Symbol Resolution（符号解析）**：为每个名称查找对应的符号
- **Type Inference（类型推导）**：从表达式推导类型

#### 3.2 与仓库 EP 的对应关系

对应 EP：EP14

目录结构：
```
ep14/
├── src/main/java/org/teachfx/antlr4/ep14/
│   ├── Compiler.java                 // 主程序（演示多遍编译）
│   ├── compiler/
│   │   ├── MathExprLexer.java        // ANTLR4 生成的词法分析器
│   │   ├── MathExprParser.java       // ANTLR4 生成的解析器（含 Parser Actions）
│   │   └── MathExprBaseListener.java // ANTLR4 基础监听器
│   └── symtab/
│       ├── Symbol.java               // 符号基类
│       ├── SymbolTable.java          // 全局符号表（单作用域）
│       ├── Scope.java               // 作用域接口
│       ├── BaseScope.java           // 作用域基类
│       ├── VariableSymbol.java      // 变量符号
│       ├── BuiltIntTypeSymbol.java  // 内置类型符号
│       └── Type.java               // 类型基类
├── src/main/antlr4/
│   └── MathExpr.g4                 // 数学表达式语法（含 Parser Actions）
└── src/test/java/
    └── (测试文件)
```

**关键类/方法说明**：

**Symbol** - 符号基类
```java
package org.teachfx.antlr4.ep14.symtab;

/**
 * 符号基类
 * 表示程序中的名称（变量、函数、类型等）
 *
 * 职责：
 * - 存储符号名称
 * - 存储符号类型（int、float、char 等）
 * - 存储符号所在作用域
 */
public class Symbol {
    static Type UNDEFINED; // 未定义类型
    public Type type;     // 符号类型
    String name;          // 符号名称

    public Symbol(String name) {
        this.name = name;
        this.type = UNDEFINED;
    }

    public Symbol(String name, Type type) {
        this(name);
        this.type = type != null ? type : UNDEFINED;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        if (type != null) return '<' + getName() + ":" + type + '>';
        return getName();
    }
}
```

**VariableSymbol** - 变量符号
```java
package org.teachfx.antlr4.ep14.symtab;

/**
 * 变量符号
 * 表示程序中的变量（包括参数和局部变量）
 */
public class VariableSymbol extends Symbol {
    public VariableSymbol(String name, Type type) {
        super(name, type);
    }
}
```

**Scope** - 作用域接口
```java
package org.teachfx.antlr4.ep14.symtab;

/**
 * 作用域接口
 * 定义作用域的核心操作
 *
 * 职责：
 * - 定义符号（define）
 * - 解析符号（resolve）
 * - 查找类型（lookup）
 * - 管理作用域层次（getEnclosingScope）
 */
public interface Scope {
    String getScopeName();              // 获取作用域名称
    Scope getEnclosingScope();          // 获取父作用域
    void define(Symbol sym);            // 定义符号（添加到当前作用域）
    Symbol resolve(String name);         // 解析符号（从当前作用域向上查找）
    Type lookup(String name);           // 查找类型（返回符号的类型）
}
```

**SymbolTable** - 全局符号表（单作用域）
```java
package org.teachfx.antlr4.ep14.symtab;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局符号表
 * EP14 实现的是单作用域符号表（简化版本）
 * EP16 将扩展为多作用域符号表
 *
 * 职责：
 * - 存储全局符号
 * - 初始化内置类型（int、float）
 * - 提供符号定义和解析接口
 */
public class SymbolTable implements Scope {
    static Type UNDEFINED;
    Map<String, Symbol> symbols; // 符号存储

    public SymbolTable() {
        symbols = new HashMap<>();
        initTypeSystem(); // 初始化内置类型
    }

    /**
     * 初始化类型系统
     * 添加内置类型：int、float
     */
    private void initTypeSystem() {
        symbols.put("int", new BuiltIntTypeSymbol("int"));
        symbols.put("float", new BuiltIntTypeSymbol("float"));
    }

    @Override
    public String getScopeName() {
        return "global";
    }

    @Override
    public Scope getEnclosingScope() {
        return null; // 全局作用域没有父作用域
    }

    @Override
    public void define(Symbol sym) {
        symbols.put(sym.name, sym);
    }

    @Override
    public Symbol resolve(String name) {
        return symbols.get(name);
    }

    @Override
    public Type lookup(String name) {
        return resolve(name).type;
    }

    @Override
    public String toString() {
        return getScopeName() + ":" + symbols;
    }
}
```

**BuiltIntTypeSymbol** - 内置类型符号
```java
package org.teachfx.antlr4.ep14.symtab;

/**
 * 内置类型符号
 * 表示语言内置的类型（int、float、char 等）
 */
public class BuiltIntTypeSymbol extends Symbol {
    public BuiltIntTypeSymbol(String name) {
        super(name);
    }
}
```

**Compiler.java** - 主程序（演示多遍编译）
```java
package org.teachfx.antlr4.ep14;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.teachfx.antlr4.ep14.compiler.MathExprParser;
import org.teachfx.antlr4.ep14.compiler.MathExprLexer;
import org.teachfx.antlr4.ep14.symtab.SymbolTable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 主程序：演示多遍编译架构
 *
 * 流水线：
 * 1. 字符流 → CharStream
 * 2. 词法分析 → Lexer → TokenStream
 * 3. 语法分析 → Parser → ParseTree（第1遍）
 * 4. 符号定义 → Parser Actions（在解析时完成）
 *
 * 注意：EP14 的 Parser Actions 是在解析过程中完成符号定义
 *      这是单遍编译的简化版本
 *      EP16 将扩展为真正的多遍编译
 */
public class Compiler {
    public static void main(String[] args) throws IOException {
        String fileName = "src/main/resources/t.math";
        if (args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if (fileName != null) is = new FileInputStream(fileName);

        // 创建符号表（包含内置类型 int、float）
        SymbolTable symTb = new SymbolTable();

        // 词法分析
        CharStream inputStream = CharStreams.fromStream(is);
        MathExprLexer lexer = new MathExprLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 语法分析（Parser Actions 在此阶段完成符号定义）
        MathExprParser parser = new MathExprParser(tokens);
        parser.compileUnit(symTb); // 传递符号表

        // 打印符号表
        System.out.println("Symbol Table: " + symTb);
    }
}
```

**MathExpr.g4** - 语法文件（含 Parser Actions）
```antlr4
// ANTLR4 语法文件：数学表达式
// 含有 Parser Actions，在解析时完成符号定义

grammar MathExpr;

@header {
    package org.teachfx.antlr4.ep14.compiler;
    import org.teachfx.antlr4.ep14.symtab.*;
}

@parser::members {
    // 在解析器中持有符号表引用
    Scope currentScope;
}

// 编译单元：变量声明或表达式
compileUnit
    :   varDeclaration EOF
    |   expr EOF
    ;

// 变量声明（含 Parser Actions）
varDeclaration
    :   type ID {
            // Parser Action：在解析到类型和变量名时
            // 1. 获取类型（从符号表查找）
            Type varType = $type.ctx.type;
            // 2. 创建变量符号
            VariableSymbol var = new VariableSymbol($ID.text, varType);
            // 3. 将变量添加到当前作用域
            currentScope.define(var);
        }
        ';'
    ;

// 类型（返回 Type 对象）
type returns [Type type]
    :   'int'   { $type = currentScope.resolve("int").type; }
    |   'float' { $type = currentScope.resolve("float").type; }
    ;

// 表达式（简化版，第8章扩展）
expr
    :   expr op=('*'|'/') expr
    |   expr op=('+'|'-') expr
    |   ID
    |   INT
    |   FLOAT
    |   '(' expr ')'
    ;

// 词法规则
ID  :   [a-zA-Z_][a-zA-Z_0-9]*;
INT :   [0-9]+;
FLOAT:   [0-9]+ '.' [0-9]+;
WS  :   [ \t\r\n]+ -> skip;
```

**关键设计模式**：单遍编译（简化版）vs 多遍编译（完整版）

EP14 实现的是简化版单遍编译（Parser Actions），EP16 将扩展为多遍编译。

单遍编译（EP14）：
- 优点：简单高效，一次解析完成所有语义分析
- 缺点：难以处理前向引用（如调用未声明的函数）

多遍编译（EP16）：
- 优点：灵活强大，可以处理复杂的语义分析
- 缺点：需要多次遍历 ParseTree，稍显复杂

类/接口关系：
```
Scope (接口)
  ↑
SymbolTable (具体实现，全局作用域)

Symbol (抽象基类)
  ↑
VariableSymbol (具体实现，变量符号)
BuiltIntTypeSymbol (具体实现，内置类型)
```

#### 3.3 实战流程

实战步骤：构建符号表，解析变量声明

步骤1：编译 EP14 项目
```bash
# 进入 EP14 目录
cd ep14

# 编译项目
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

步骤2：创建测试输入文件
```bash
# 创建测试输入
cat > src/main/resources/test_symbol.txt << EOF
int x;
float y;
int z;
EOF
```

步骤3：运行主程序
```bash
# 运行主程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" \
    -Dexec.args="src/main/resources/test_symbol.txt"

# 预期输出：
# Symbol Table: global:{int=int, float=float, x=<x:int>, y=<y:float>, z=<z:int>}
```

验证方法：
- 检查点1：确认内置类型（int、float）已添加到符号表
- 检查点2：确认变量声明已正确添加到符号表
- 检查点3：确认变量类型正确（x:int, y:float, z:int）

步骤4：修改测试输入，观察错误（可选）
```bash
# 创建包含重复声明的测试输入
cat > src/main/resources/test_duplicate.txt << EOF
int x;
int x;
EOF

# 运行主程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" \
    -Dexec.args="src/main/resources/test_duplicate.txt"

# 预期输出（取决于是否添加了重复声明检查）：
# Symbol Table: global:{int=int, float=float, x=<x:int>}
# 注意：第二次声明覆盖了第一次（简化版未检查重复）
```

故障排查：

**问题1：编译错误 "cannot find symbol: class VariableSymbol"**
- 原因：Parser Actions 中使用了错误的类名
- 解决方法：
  ```bash
  # 检查 MathExpr.g4 中的 import 语句
  cat src/main/antlr4/MathExpr.g4 | grep import

  # 确认包含正确的 import：
  # import org.teachfx.antlr4.ep14.symtab.*;
  ```

**问题2：运行时异常 "NullPointerException"**
- 原因：Parser Actions 在解析时访问了未初始化的符号表
- 解决方法：
  - 确认 Compiler.java 中正确传递了符号表
  - 确认 MathExprParser 中定义了 `Scope currentScope` 字段

**问题3：变量类型解析失败**
- 原因：type 规则未正确返回 Type 对象
- 解决方法：
  - 检查 MathExpr.g4 中 type 规则的 Parser Actions
  - 确认 `$type = currentScope.resolve("int").type;` 正确

进阶技巧：

1. **添加重复声明检查**：在 SymbolTable.define() 中添加检查，拒绝重复符号
2. **添加未定义变量检测**：在表达式中使用变量时，检查符号表是否存在
3. **扩展类型系统**：添加 char、bool 等类型

### 4. AI 协作线：Context Engineering 视角

#### 4.1 上下文设计

为了让 AI 帮助完成本章任务，我们需要精心设计上下文。

上下文文件列表：

**源码文件**（按阅读顺序）：
1. `ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/Symbol.java`
   - 作用：符号基类，定义所有符号的共同接口
   - 关键方法：`getName()`、`toString()`

2. `ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/VariableSymbol.java`
   - 作用：变量符号，表示程序中的变量
   - 关键方法：构造函数

3. `ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/Scope.java`
   - 作用：作用域接口，定义作用域的核心操作
   - 关键方法：`define()`、`resolve()`、`lookup()`

4. `ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java`
   - 作用：全局符号表，管理所有符号
   - 关键方法：`initTypeSystem()`、`define()`、`resolve()`

5. `ep14/src/main/java/org/teachfx/antlr4/ep14/Compiler.java`
   - 作用：主程序，演示多遍编译流水线
   - 关键流程：创建符号表 → 词法分析 → 语法分析 → 符号定义

6. `ep14/src/main/antlr4/MathExpr.g4`
   - 作用：数学表达式语法，含 Parser Actions
   - 关键规则：`varDeclaration`、`type`、`expr`

**文档文件**：
1. `ep14/README.md`（如果有）
   - 作用：EP14 模块概述和使用说明
   - 关键章节：多遍编译架构、符号表设计

2. `AGENTS.md`
   - 作用：代码规范和最佳实践
   - 相关部分：符号表设计模式、Parser Actions 使用规范

**测试文件**：
1. `ep14/src/test/java/org/teachfx/antlr4/ep14/`（如果有）
   - 作用：测试符号表和类型系统
   - 关键测试方法：`testSymbolDefinition`、`testTypeResolution`

**示例输入/输出**：
1. `ep14/src/main/resources/test_symbol.txt`
   - 作用：测试输入变量声明
   - 内容：各种类型的变量声明

上下文组织说明：

这些文件按照"符号表实现 → 解析器 → 主程序"组织：

1. **符号表核心在前**：先提供 Symbol、VariableSymbol、Scope、SymbolTable 实现，让 AI 理解符号表设计
2. **解析器在后**：接着提供 MathExpr.g4，展示如何在语法中嵌入 Parser Actions
3. **主程序作为整合**：最后提供 Compiler.java，展示完整的编译流程
4. **示例文件作为验证**：提供测试输入，用于验证符号表正确性

为什么这样组织：
- AI 可以先理解符号表的数据结构设计，再理解如何在解析时使用符号表
- 完整的上下文确保 AI 理解 Parser Actions 的作用和实现方式
- 示例文件帮助验证 AI 生成的代码是否正确

#### 4.2 Prompt 模板（给 AI 用）

**类型 A: 添加重复声明检查 Prompt 模板**
```
请在 SymbolTable 中添加重复声明检查，拒绝定义已存在的符号。

任务目标：
- 修改 SymbolTable.define() 方法，检测重复声明
- 如果符号已存在，抛出异常或打印错误信息

具体要求：
1. 修改 `ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java`
   - 在 `define(Symbol sym)` 方法中添加重复检查
   - 如果 `symbols.containsKey(sym.name)`，抛出 `IllegalStateException` 或打印错误

2. 创建自定义异常类（可选）
   - `DuplicateSymbolException.java`，继承 `RuntimeException`
   - 包含符号名称和位置信息

参考上下文文件：
- 源码：
  - `ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java`
  - `ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/Symbol.java`
- 文档：
  - `AGENTS.md`（代码规范）
  - `ep14/src/main/antlr4/MathExpr.g4`（语法文件）

约束条件：
- 不修改其他符号表方法
- 保持与现有编译器流水线的兼容性
- 错误信息清晰，包含符号名称

期望输出：
1. 修改后的 SymbolTable.java（标注新增部分）
2. 如果创建了 DuplicateSymbolException.java，提供完整代码
3. 测试示例：运行重复声明时的预期输出
```

**类型 B: 添加类型检查 Prompt 模板**
```
请为表达式添加基础类型检查，确保操作数类型兼容。

任务目标：
- 在 MathExprParser 中添加类型检查逻辑
- 验证二元运算的操作数类型是否兼容（int + int ✅, int + float ✅, float + float ✅）
- 拒绝类型不兼容的操作（如 string + int）

具体要求：
1. 修改 `ep14/src/main/antlr4/MathExpr.g4`
   - 在 expr 规则中添加类型检查的 Parser Actions
   - 获取左右操作数的类型（从符号表查找）
   - 验证类型兼容性
   - 如果不兼容，抛出异常或打印错误

2. 定义类型兼容性规则：
   - int + int → int
   - float + float → float
   - int + float → float（隐式转换）
   - float + int → float（隐式转换）

参考上下文文件：
- 源码：
  - `ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java`
  - `ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/Type.java`
  - `ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/Symbol.java`
- 文档：
  - `ep14/src/main/antlr4/MathExpr.g4`（语法文件）
  - `AGENTS.md`（代码规范）

约束条件：
- 只在 expr 规则中添加类型检查
- 不修改其他语法规则
- 错误信息清晰，指出类型不匹配

期望输出：
1. 修改后的 MathExpr.g4（标注新增部分）
2. 类型兼容性规则的实现逻辑
3. 测试示例：类型正确和类型错误的输入及预期输出
```

#### 4.3 AI 应该做 / 不该做

在让 AI 帮助完成本章任务时，我们需要明确 AI 的职责边界。

**✅ AI 允许做的事情**：

1. **扩展符号表功能**
   - 可以：添加重复声明检查、未定义变量检测
   - 可以：添加符号表的辅助方法（如 `printSymbols()`）
   - 不能：修改符号表的核心设计（如从 HashMap 改为其他数据结构）

2. **扩展类型系统**
   - 可以：添加新的内置类型（如 char、bool）
   - 可以：添加类型兼容性检查逻辑
   - 不能：修改 Type 基类的设计

3. **增强 Parser Actions**
   - 可以：在语法规则中添加更多的 Parser Actions
   - 可以：添加错误检测和报告
   - 不能：修改语法规则的结构（如改变运算符优先级）

4. **生成测试用例**
   - 可以：生成符号表功能的测试用例
   - 可以：生成类型检查的测试用例
   - 不能：删除或破坏现有测试

5. **添加错误处理**
   - 可以：添加自定义异常类
   - 可以：添加友好的错误消息
   - 不能：改变错误处理的策略（如从异常改为静默失败）

**❌ AI 禁止做的事情**：

1. **修改符号表的核心数据结构**
   - 不允许：将 `HashMap<String, Symbol>` 改为其他数据结构
   - 不允许：修改 Scope 接口的方法签名
   - 原因：符号表设计是编译器前端的基础

2. **改变 Parser Actions 的位置**
   - 不允许：将 `currentScope.define(var)` 移出 varDeclaration 规则
   - 不允许：删除现有的 Parser Actions
   - 原因：Parser Actions 的位置决定了符号定义的时机

3. **删除内置类型**
   - 不允许：从 `initTypeSystem()` 中删除 int 或 float
   - 不允许：修改内置类型的名称
   - 原因：内置类型是语言的基础

4. **引入新的外部依赖**
   - 不允许：在 pom.xml 中添加新库
   - 不允许：使用第三方符号表库
   - 原因：保持项目技术栈一致性

5. **破坏多遍编译架构**
   - 不允许：将符号表改为静态全局变量（避免传递）
   - 不允许：删除符号表引用传递
   - 原因：多遍编译需要显式传递符号表

**🤝 灰色区域（需谨慎处理）**：

1. **优化符号表查找性能**
   - 可以：添加符号查找缓存
   - 需谨慎：确保缓存一致性

2. **增强类型系统但不破坏兼容性**
   - 可以：添加类型转换规则
   - 需谨慎：确保类型转换规则符合语义

如果 AI 提议超出范围的操作，请：
- 明确拒绝并说明原因
- 要求 AI 在当前范围内完成任务
- 保留超出范围的建议，待后续章节讨论

#### 4.4 验证与回滚策略

在 AI 完成修改后，我们必须进行严格的验证才能将更改合并到主分支。

## 自动化验证

**步骤1：运行相关测试**
```bash
# 进入 EP14 目录
cd ep14

# 运行所有测试（如果有）
mvn test

# 或者运行特定测试类
mvn test -Dtest={测试类名}Test

# 预期输出：
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
```

**关键测试**：
- `SymbolTableTest`（如果有）- 验证符号表功能
- `TypeSystemTest`（如果有）- 验证类型检查

**验证标准**：
- ✅ 所有测试通过（Failures: 0, Errors: 0）
- ✅ 没有新的编译警告
- ✅ 符号表包含正确的符号和类型

**步骤2：编译验证**
```bash
# 清理并重新编译
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

**步骤3：运行示例程序**
```bash
# 创建测试输入
cat > src/main/resources/test_ai.txt << EOF
int x;
float y;
int z;
EOF

# 运行主程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" \
    -Dexec.args="src/main/resources/test_ai.txt"

# 预期输出：
# Symbol Table: global:{int=int, float=float, x=<x:int>, y=<y:float>, z=<z:int>}
```

## 手工检查点

即使所有测试通过，我们仍需手工检查以下关键点：

**检查1：代码风格符合规范**
- [ ] 包命名：`org.teachfx.antlr4.ep14.symtab`
- [ ] 类命名：PascalCase（如 `SymbolTable`、`VariableSymbol`）
- [ ] 方法命名：camelCase（如 `define`、`resolve`）
- [ ] 导入顺序：ANTLR4 → 外部库 → 内部 → Java stdlib

**检查2：没有引入新的编译错误**
- [ ] 查看项目根目录的 `mvn clean compile` 输出
- [ ] 确认没有新的 ERROR 或 WARNING
- [ ] 检查 AI 修改的类是否能正常编译

**检查3：没有破坏现有功能**
- [ ] 运行原有测试（如果有），确保不失败
- [ ] 验证符号表仍能正确定义和解析符号
- [ ] 确认 Git diff 只包含预期修改

**检查4：文档完整性**
- [ ] 新增类/方法有 JavaDoc 注解
- [ ] 关键算法有时间/空间复杂度说明
- [ ] 复杂逻辑有行内注释

## 回滚方案

如果 AI 修改后出现问题，使用以下步骤快速恢复：

### 方案1：Git Stash（推荐）

```bash
# 查看当前修改
git status

# 保存 AI 修改到 stash（带备注）
git stash push -m "AI changes for symbol table"

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
git checkout HEAD~1 -- ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java
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
git checkout -b ai-experiment-symbol-table

# 让 AI 在新分支工作
# 完成后合并回主分支
git checkout main
git merge ai-experiment-symbol-table
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
  2. 检查符号表是否正确添加符号
  3. 确认类型检查逻辑是否符合预期
- 解决：根据失败信息修改 AI 代码，或调整测试用例

**问题2：编译错误**
- 现象：`[ERROR] COMPILATION ERROR`
- 排查：
  1. 检查 AI 是否使用了错误的包名
  2. 确认类名是否正确
  3. 验证继承关系是否正确
- 解决：清理 target 目录重新编译，或修正代码

**问题3：符号表解析失败**
- 现象：变量无法在符号表中找到
- 排查：
  1. 检查 Parser Actions 是否正确调用 `currentScope.define()`
  2. 确认符号名称拼写正确
  3. 验证符号表引用是否正确传递
- 解决：根据调试信息修正代码

### 5. 练习题

本章练习题

练习1：添加未定义变量检测（手工实现版）

难度：⭐⭐☆☆☆
预计时间：30–45 分钟

题目描述：
在 MathExprParser 中添加未定义变量检测，当表达式中使用了未声明的变量时，报错。

要求：
- 完全手工实现，不依赖 AI
- 在表达式的 ID 规则中添加 Parser Actions
- 检查符号表中是否存在该变量

验收标准：
- [ ] 在 expr 规则的 ID 分支添加检查逻辑
- [ ] 如果变量未定义，打印错误信息
- [ ] 代码能编译通过
- [ ] 测试未定义变量时报错

**💡 解题思路提示**：
- 在 expr 规则的 ID 分支添加 Parser Actions
- 使用 `currentScope.resolve($ID.text)` 查找变量
- 如果返回 null，说明变量未定义，打印错误

---

练习2：添加 char 类型（AI 协作版）

难度：⭐⭐⭐☆☆
预计时间：45–60 分钟

题目描述：
在类型系统中添加 char 类型，支持字符类型的变量声明。

AI 协作要求：
1. 设计上下文：列出需要提供给 AI 的文件和说明
2. 设计 Prompt：参考本章的 Prompt 模板，设计适合此任务的 Prompt
3. 验证 AI 输出：使用本章的验证策略
4. 理解 AI 代码：确保你能解释 AI 生成的每一部分

验收标准：
- [ ] AI 生成的代码能编译通过
- [ ] 在符号表中添加 char 类型
- [ ] 在语法中支持 char 类型声明
- [ ] 测试 `char c;` 能正确添加到符号表
- [ ] 你能解释 AI 代码的实现逻辑

**💡 解题思路提示**：
- 上下文：提供 SymbolTable.java、Type.java、MathExpr.g4
- Prompt：参考"类型 A: 添加重复声明检查 Prompt 模板"
- 验证：运行示例程序，检查符号表是否包含 char 类型

---

练习3：实现类型推导（综合挑战）

难度：⭐⭐⭐⭐☆
预计时间：60–90 分钟

题目描述：
为表达式添加类型推导功能，在解析时计算表达式的类型。

要求：
- 可以选择手工实现或 AI 协作
- 如果选择 AI 协作，需要详细记录协作过程
- 提交时说明 AI 参与部分和人工修改部分

验收标准：
- [ ] 表达式的类型推导正确（int + int → int, float + float → float）
- [ ] 隐式类型转换正确（int + float → float）
- [ ] 类型不匹配时报错（如 string + int）
- [ ] 功能完整，性能合理
- [ ] 代码可读性良好

**💡 解题思路提示**：
- 在 expr 规则中添加类型推导的 Parser Actions
- 返回类型（使用 `returns [Type type]`）
- 根据左右操作数的类型推导表达式类型
- 添加类型转换规则

---

练习4：实现作用域嵌套（进阶练习 - 可选）

难度：⭐⭐⭐⭐⭐
预计时间：90–120 分钟

题目描述：
将单作用域符号表扩展为多作用域符号表，支持嵌套作用域（如函数内部有局部作用域）。

适合人群：
- 想深入理解符号表的高级读者
- 有志于实现完整编译器前端的高级读者

**💡 解题思路提示**：
- 在 Scope 接口中添加 `getEnclosingScope()` 方法
- 在 SymbolTable 中添加父作用域引用
- 在解析函数和代码块时创建新作用域
- 实现作用域链查找（从当前作用域向上查找）

### 6. 本章小结与下一章预告

## 本章小结

通过本章的学习，你已经掌握了：

1. **多遍编译架构**
   - 理解了单遍和多遍编译的区别
   - 掌握了多遍编译的设计原则（每遍专注一个任务）
   - 学会了为什么需要多遍编译（处理前向引用、复杂语义分析）

2. **符号表设计**
   - 理解了符号表的层次结构（全局作用域、局部作用域）
   - 掌握了符号的定义和解析逻辑
   - 能够独立设计符号表接口和实现

3. **类型系统基础**
   - 理解了类型系统的组成（类型、类型规则）
   - 掌握了内置类型的定义（int、float、char）
   - 学会了基础类型检查和类型兼容性

4. **Parser Actions 实践**
   - 熟练使用了 Parser Actions 在解析时完成语义分析
   - 掌握了如何在语法规则中嵌入代码
   - 理解了 Parser Actions 的优势和局限

【你现在站在哪】:
```
... → AST 构建 → ✅ **符号解析** → [类型检查] → [多遍编译架构] → ...
```

**当前在编译器流水线的位置**：
- 本章实现了编译器流水线的符号解析阶段
- 构建了符号表和类型系统，为类型检查奠定基础

**与下一章的衔接**：
- 本章的符号表和类型系统将在下一章被扩展
- Parser Actions 将被替换为真正的多遍编译（使用访问者模式）
- 单作用域符号表将扩展为多作用域符号表

## 下一章预告

**第8章：完整类型检查与多遍编译架构**

在下一章，我们将学习：
- 完整的类型系统设计（类型推导、类型检查）
- 多作用域符号表管理（全局作用域、局部作用域、嵌套作用域）
- 真正的多遍编译架构（使用访问者模式，替代 Parser Actions）
- 静态作用域和动态作用域的区别

你将能够：
- 设计完整的类型检查器
- 实现多作用域符号表
- 理解多遍编译的完整流程
- 掌握编译器前端的设计模式

**准备**：为了学习下一章，建议：
- [ ] 复习本章的符号表和类型系统设计
- [ ] 运行本章的示例程序，加深对符号解析的理解
- [ ] 阅读下一章的预备材料（类型推导、作用域嵌套）

继续加油！下一章将带你进入完整的编译器前端架构。
