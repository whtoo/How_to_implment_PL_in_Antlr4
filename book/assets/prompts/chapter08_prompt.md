# 第8章提示：完整类型检查与多遍编译架构

## 章节写作任务

请为本书撰写一整章内容。

### 本章基本信息

- **章节标题**: 第8章：完整类型检查与多遍编译架构
- **所在模块**: 模块 2：从解释到编译（EP13–EP16）
- **面向读者**: 已理解符号表和类型系统基础（第7章），学习完整编译器前端的工程师
- **读者前置知识**: 符号表设计、类型系统基础、访问者模式、ParseTree 结构
- **本章对应仓库 EP 范围**: EP16（完整类型检查、多作用域符号表、多遍编译）
- **本章在全书中的位置**:
  - 前一章：第7章：符号解析与类型系统
  - 后一章：第9章：从解释到编译的转换（模块 2 终章）
- **本章完成后，读者应该能**:
  - 理解完整类型系统设计和类型推导算法
  - 掌握多作用域符号表的设计和作用域管理
  - 实现完整的类型检查器（int、float、char、bool 类型）
  - 理解真正的多遍编译架构（替代 Parser Actions）

---

## 内容结构要求（必须按顺序给出）

### 1. 本章概述

本章聚焦于完整类型检查和多遍编译架构，它是编译器流水线中的完整前端阶段。通过学习本章，你将掌握类型推导算法、多作用域符号表设计、以及真正的多遍编译架构，这是现代编译器前端的标准实践。

【你现在站在哪】:
... → AST 构建 → 符号定义 → ✅ **完整类型检查** → 代码生成 → ...

### 2. 动机与真实场景

**真实场景**：假设你正在开发一个完整的编译器，需要支持完整的 Cymbol 语言（函数、嵌套作用域、类型推导）。单遍编译的 Parser Actions 已无法满足需求，需要真正的多遍编译架构。

**具体问题**：
- 函数参数和局部变量需要在函数作用域中管理
- 需要支持嵌套作用域（if 块、while 块）
- 类型推导需要遍历 AST，无法在解析时完成
- 需要为后续优化和代码生成提供类型信息

如果缺少本章的能力，你将面临：
- 无法正确处理嵌套作用域（变量遮蔽问题）
- 无法实现完整的类型检查（表达式类型推导）
- 无法为后续阶段提供完整的类型信息
- 编译器架构混乱，难以扩展

本章将教你如何：设计多作用域符号表，实现完整类型检查器，构建真正的多遍编译架构。

### 3. 人类工程师线：技术与实现

#### 3.1 核心概念

**核心概念 1：多遍编译架构（Multi-pass Compilation Architecture）**

通俗解释：多遍编译架构是指将编译过程分为多个独立的阶段（遍），每个阶段专注于特定任务，通过访问者模式遍历 AST，后一阶段利用前一阶段的结果。

[图1：完整多遍编译架构]
```
第1遍：AST 构建
ParseTree → BuildAstVisitor → AST

第2遍：符号定义（LocalDefine）
AST → LocalDefine → 为每个节点标记作用域
  - 遍历 AST，创建作用域
  - 将符号添加到对应作用域
  - 建立作用域链

第3遍：类型推导（LocalResolver）
AST → LocalResolver → 为每个节点标记类型
  - 遍历 AST，查找符号
  - 推导表达式类型
  - 标记每个节点的类型

第4遍：类型检查（后续章节）
AST → TypeChecker → 验证类型正确性
  - 检查类型兼容性
  - 报告类型错误
  - 为代码生成提供类型信息

优势：
- 每遍专注一个任务，逻辑清晰
- 支持前向引用（函数调用在定义之前）
- 便于扩展（添加新的编译阶段）
```

图示说明：
- 多遍编译将编译过程分解为独立的阶段
- 每个阶段通过访问者模式遍历 AST
- 后一阶段利用前一阶段的结果（作用域、类型）

类比理解：
- 多遍编译就像"工厂流水线"：
  - 第1道工序：铸造零件（AST 构建）
  - 第2道工序：组装零件（符号定义）
  - 第3道工序：打磨零件（类型推导）
  - 第4道工序：质量检查（类型检查）

**核心概念 2：多作用域符号表（Multi-Scope Symbol Table）**

通俗解释：多作用域符号表是层次化的符号管理结构，每个作用域包含自己的符号，形成作用域链，符号查找从内向外进行。

[图2：多作用域符号表结构]
```
作用域链示例：
GlobalScope（全局作用域）
  ├─ int (TypeSymbol)
  ├─ float (TypeSymbol)
  ├─ main (MethodScope)
  │   ├─ x (VariableSymbol: int)
  │   ├─ y (VariableSymbol: float)
  │   └─ LocalScope (if 块)
  │       └─ z (VariableSymbol: int)
  └─ factorial (MethodScope)
      ├─ n (VariableSymbol: int)
      ├─ result (VariableSymbol: int)
      └─ LocalScope (if 块)
          └─ temp (VariableSymbol: int)

符号查找示例：
在 factorial 函数的 if 块中引用 "x"：
1. 当前作用域（LocalScope）查找 x → 未找到
2. 父作用域（factorial MethodScope）查找 x → 未找到
3. 父作用域（GlobalScope）查找 x → 找到！
返回 GlobalScope.x

变量遮蔽示例：
GlobalScope: x (int)
main 函数: x (float)
在 main 函数中引用 "x" → 返回 main.x（遮蔽全局 x）
```

图示说明：
- 作用域链形成层次结构，从内向外查找符号
- 内层作用域可以遮蔽外层作用域的同名符号
- 每个函数有自己的作用域，包含参数和局部变量

类比理解：
- 作用域链就像"多级文件夹"：
  - 全局文件夹（根目录）包含所有文件
  - 函数文件夹（子目录）包含函数的文件
  - 块文件夹（孙目录）包含块的文件
  - 查找文件时，先查当前目录，再查父目录

**核心概念 3：类型推导（Type Inference）**

通俗理解：类型推导是指根据表达式的结构和操作数的类型，自动推导出整个表达式的类型。

[图3：类型推导示例]
```
表达式：2 + 3 * 4

AST 结构：
      AdditionNode
     /            \
NumberNode(2)  MultiplicationNode
                /             \
          NumberNode(3)   NumberNode(4)

类型推导过程：
1. NumberNode(2) → int
2. NumberNode(3) → int
3. NumberNode(4) → int
4. MultiplicationNode(int, int) → int (int * int → int)
5. AdditionNode(int, int) → int (int + int → int)
最终类型：int

表达式：2.5 + 3
AST 结构：
      AdditionNode
     /            \
NumberNode(2.5)  NumberNode(3)

类型推导过程：
1. NumberNode(2.5) → float
2. NumberNode(3) → int
3. AdditionNode(float, int) → float (float + int → float)
最终类型：float（隐式转换）

类型推导规则：
- int + int → int
- float + float → float
- int + float → float（隐式转换）
- float + int → float（隐式转换）
- int * int → int
- float * float → float
- int * float → float（隐式转换）
- float / float → float
- int / int → int（注意：整数除法）
```

图示说明：
- 类型推导从叶子节点（常量、变量）开始
- 向上推导父节点的类型
- 根据类型兼容性规则确定最终类型

类比理解：
- 类型推导就像"计算器"：
  - 输入：2（int）、3（int）
  - 计算：2 + 3 = 5
  - 输出：5（int）
  - 如果输入：2.5（float）、3（int）
  - 计算：2.5 + 3 = 5.5（float）

**核心概念 4：类型检查（Type Checking）**

通俗解释：类型检查是指在编译时验证程序中的类型操作是否符合类型系统的规则，报告类型错误。

[图4：类型检查示例]
```
类型检查规则：

1. 变量赋值：
   int x;
   x = 5;          ✅ int → int
   x = 3.14;       ❌ float → int（需要显式转换）

2. 函数参数：
   void f(int x) { ... }
   f(5);           ✅ int → int
   f(3.14);        ❌ float → int（需要显式转换）

3. 返回值：
   int g() {
       return 5;    ✅ int → int
       return 3.14; ❌ float → int（需要显式转换）
   }

4. 运算符：
   int a = 5, b = 3;
   int c = a + b;   ✅ int + int → int
   float d = a + b; ✅ int + int → int → float（隐式转换）

类型错误示例：
int x = 5;
float y = x + "hello";  ❌ int + string（类型不兼容）

int z = x / 0;         ❌ 除零错误（运行时错误，但类型检查可以部分检测）
```

图示说明：
- 类型检查验证程序中的类型操作是否合法
- 类型错误在编译时被检测，避免运行时错误
- 类型兼容性规则决定了哪些操作是合法的

类比理解：
- 类型检查就像"安全检查员"：
  - 检查每个操作是否符合安全规则
  - 不允许苹果和橙子相加
  - 要求明确类型转换（如削皮苹果）

**相关概念**：
- **类型兼容性**：类型之间是否可以相互操作
- **隐式转换**：自动将一种类型转换为另一种类型
- **显式转换**：明确指定类型转换（如 `(int)3.14`）
- **类型推导算法**：从表达式推导类型的算法

#### 3.2 与仓库 EP 的对应关系

对应 EP：EP16

目录结构：
```
ep16/
├── src/main/java/org/teachfx/antlr4/ep16/
│   ├── Compiler.java                    // 主程序（演示多遍编译）
│   ├── parser/
│   │   ├── CymbolLexer.java            // ANTLR4 生成的词法分析器
│   │   ├── CymbolParser.java           // ANTLR4 生成的解析器
│   │   └── CymbolBaseVisitor.java      // ANTLR4 基础访问者
│   ├── visitor/
│   │   ├── CymbolASTVisitor.java       // AST 访问者基类
│   │   ├── LocalDefine.java           // 第2遍：符号定义（标记作用域）
│   │   ├── LocalResolver.java          // 第3遍：类型推导（标记类型）
│   │   └── Interpreter.java            // 解释器（基于类型信息）
│   ├── symtab/
│   │   ├── Symbol.java                // 符号基类
│   │   ├── VariableSymbol.java       // 变量符号
│   │   ├── MethodSymbol.java         // 方法符号
│   │   ├── ScopedSymbol.java         // 带作用域的符号
│   │   ├── Type.java                 // 类型基类
│   │   ├── TypeTable.java            // 类型表
│   │   ├── Scope.java               // 作用域接口
│   │   ├── BaseScope.java           // 作用域基类
│   │   ├── GlobalScope.java         // 全局作用域
│   │   ├── LocalScope.java          // 局部作用域
│   │   └── BuiltInTypeSymbol.java  // 内置类型符号
│   └── misc/
│       ├── ScopeUtil.java            // 作用域工具类
│       ├── Util.java                // 工具类
│       ├── CompilerLogger.java      // 编译器日志
│       └── MemorySpace.java        // 内存空间
├── src/main/antlr4/
│   └── Cymbol.g4                   // Cymbol 语言语法
└── src/test/java/
    └── (测试文件)
```

**关键类/方法说明**：

**Symbol** - 符号基类（增强版）
```java
package org.teachfx.antlr4.ep16.symtab;

import org.teachfx.antlr4.ep16.misc.MemorySpace;

/**
 * 符号基类
 * 表示程序中的名称（变量、函数、类型等）
 *
 * 职责：
 * - 存储符号名称
 * - 存储符号类型
 * - 存储符号所在作用域
 * - 存储符号的内存空间（后续章节使用）
 */
public class Symbol {
    static Type UNDEFINED; // 未定义类型
    public Type type;     // 符号类型
    public Scope scope;   // 符号所在作用域
    public MemorySpace space; // 内存空间（后续章节）
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
        String s = "";
        if (scope != null) s = scope.getScopeName() + ".";
        if (type != null) return '<' + s + getName() + ":" + type + '>';
        return s + getName();
    }
}
```

**ScopedSymbol** - 带作用域的符号
```java
package org.teachfx.antlr4.ep16.symtab;

import java.util.HashMap;
import java.util.Map;

/**
 * 带作用域的符号
 * 表示函数、方法等可以包含其他符号的符号
 *
 * 职责：
 * - 存储成员符号（参数、局部变量）
 * - 实现作用域接口
 */
public class ScopedSymbol extends Symbol implements Scope {
    Map<String, Symbol> members = new HashMap<>();

    public ScopedSymbol(String name, Scope enclosingScope) {
        super(name);
        this.scope = enclosingScope;
    }

    @Override
    public String getScopeName() {
        return name;
    }

    @Override
    public Scope getEnclosingScope() {
        return scope;
    }

    @Override
    public void define(Symbol sym) {
        members.put(sym.name, sym);
    }

    @Override
    public Symbol resolve(String name) {
        Symbol s = members.get(name);
        if (s != null) return s;
        // 如果当前作用域未找到，向父作用域查找
        if (getEnclosingScope() != null) {
            return getEnclosingScope().resolve(name);
        }
        return null;
    }

    @Override
    public Type lookup(String name) {
        Symbol s = resolve(name);
        return s != null ? s.type : null;
    }
}
```

**MethodSymbol** - 方法符号
```java
package org.teachfx.antlr4.ep16.symtab;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * 方法符号
 * 表示程序中的函数或方法
 */
public class MethodSymbol extends ScopedSymbol {
    public ParserRuleContext blockStmt; // 函数体
    public ParserRuleContext callee;     // 调用点
    public boolean builtin;             // 是否为内置函数

    public MethodSymbol(String name, Scope enclosingScope, ParserRuleContext ctx) {
        super(name, enclosingScope);
        this.blockStmt = null;
        this.callee = null;
        this.builtin = false;
    }
}
```

**LocalScope** - 局部作用域
```java
package org.teachfx.antlr4.ep16.symtab;

import java.util.HashMap;
import java.util.Map;

/**
 * 局部作用域
 * 表示函数、代码块等局部作用域
 */
public class LocalScope implements Scope {
    Scope enclosingScope; // 父作用域
    Map<String, Symbol> symbols = new HashMap<>();

    public LocalScope(Scope enclosingScope) {
        this.enclosingScope = enclosingScope;
    }

    @Override
    public String getScopeName() {
        return "local_" + hashCode();
    }

    @Override
    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public void define(Symbol sym) {
        symbols.put(sym.name, sym);
    }

    @Override
    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if (s != null) return s;
        // 向父作用域查找
        if (getEnclosingScope() != null) {
            return getEnclosingScope().resolve(name);
        }
        return null;
    }

    @Override
    public Type lookup(String name) {
        Symbol s = resolve(name);
        return s != null ? s.type : null;
    }
}
```

**GlobalScope** - 全局作用域
```java
package org.teachfx.antlr4.ep16.symtab;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局作用域
 * 表示程序的全局作用域，包含所有全局符号
 */
public class GlobalScope implements Scope {
    Map<String, Symbol> symbols = new HashMap<>();

    public GlobalScope() {
        initTypeSystem(); // 初始化内置类型
    }

    /**
     * 初始化类型系统
     * 添加内置类型：int、float、char、bool、void
     */
    private void initTypeSystem() {
        symbols.put("int", new BuiltInTypeSymbol("int"));
        symbols.put("float", new BuiltInTypeSymbol("float"));
        symbols.put("char", new BuiltInTypeSymbol("char"));
        symbols.put("bool", new BuiltInTypeSymbol("bool"));
        symbols.put("void", new BuiltInTypeSymbol("void"));
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
        Symbol s = resolve(name);
        return s != null ? s.type : null;
    }
}
```

**LocalDefine** - 符号定义（第2遍）
```java
package org.teachfx.antlr4.ep16.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep16.misc.Util;
import org.teachfx.antlr4.ep16.parser.CymbolParser.*;
import org.teachfx.antlr4.ep16.symtab.*;

/**
 * 符号定义（第2遍）
 * 遍历 AST，为每个节点标记作用域，定义符号
 *
 * 职责：
 * - 创建作用域（全局、函数、局部）
 * - 将符号添加到对应作用域
 * - 为每个 ParseTree 节点标记作用域
 */
public class LocalDefine extends CymbolASTVisitor<Object> {
    private Scope currentScope = null; // 当前作用域
    private final ParseTreeProperty<Scope> scopes; // 节点 → 作用域映射

    public LocalDefine() {
        BaseScope globalScope = new GlobalScope();
        currentScope = globalScope;

        // 添加内置函数 print
        MethodSymbol printFuncSymbol = new MethodSymbol("print", globalScope, null);
        printFuncSymbol.builtin = true;
        globalScope.define(printFuncSymbol);

        scopes = new ParseTreeProperty<>();
    }

    public ParseTreeProperty<Scope> getScopes() {
        return scopes;
    }

    @Override
    public Object visitCompilationUnit(CompilationUnitContext ctx) {
        stashScope(ctx);
        super.visitCompilationUnit(ctx);
        return null;
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        // 创建函数作用域
        MethodSymbol methodScope = new MethodSymbol(Util.name(ctx), currentScope, ctx);
        methodScope.blockStmt = ctx.blockDef;
        methodScope.callee = (ParserRuleContext) ctx.parent;
        currentScope.define(methodScope);
        stashScope(ctx);

        // 进入函数作用域
        pushScope(methodScope);
        super.visitFunctionDecl(ctx);
        popScope();
        return null;
    }

    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        stashScope(ctx);
        super.visitVarDecl(ctx);
        return null;
    }

    @Override
    public Object visitBlock(BlockContext ctx) {
        // 创建局部作用域
        Scope local = new LocalScope(currentScope);
        stashScope(ctx);

        pushScope(local);
        super.visitBlock(ctx);
        popScope();
        return null;
    }

    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        stashScope(ctx);
        return super.visitExprBinary(ctx);
    }

    // ... 其他 visit 方法类似，都调用 stashScope(ctx)

    /**
     * 保存当前作用域到节点
     */
    public void stashScope(ParserRuleContext ctx) {
        scopes.put(ctx, currentScope);
    }

    /**
     * 进入新作用域
     */
    public void pushScope(Scope scope) {
        currentScope = scope;
    }

    /**
     * 退出当前作用域
     */
    public void popScope() {
        currentScope = currentScope.getEnclosingScope();
    }
}
```

**LocalResolver** - 类型推导（第3遍）
```java
package org.teachfx.antlr4.ep16.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep16.misc.CompilerLogger;
import org.teachfx.antlr4.ep16.misc.ScopeUtil;
import org.teachfx.antlr4.ep16.misc.Util;
import org.teachfx.antlr4.ep16.parser.CymbolParser.*;
import org.teachfx.antlr4.ep16.symtab.*;

/**
 * 类型推导（第3遍）
 * 遍历 AST，为每个节点标记类型
 *
 * 职责：
 * - 查找符号（变量、函数）
 * - 推导表达式类型
 * - 为每个 ParseTree 节点标记类型
 */
public class LocalResolver extends CymbolASTVisitor<Object> {
    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    public ParseTreeProperty<Type> types; // 节点 → 类型映射
    private final ScopeUtil scopes; // 作用域工具（从 LocalDefine 获取）

    public LocalResolver(ScopeUtil scopes) {
        this.scopes = scopes;
        this.types = new ParseTreeProperty<>();
    }

    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        super.visitVarDecl(ctx);

        // 查找类型（从作用域）
        Type type = scopes.lookup(ctx.type());
        VariableSymbol var = new VariableSymbol(Util.name(ctx), type);

        if (type == null) {
            CompilerLogger.error(ctx, "Unknown type when declaring variable: " + var);
        }

        // 将变量添加到作用域（LocalDefine 已完成，这里只标记类型）
        Scope scope = scopes.get(ctx);
        scope.define(var);

        // 标记变量声明的类型
        stashType(ctx, type);
        return null;
    }

    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        super.visitExprBinary(ctx);

        // 推导二元表达式的类型
        // 根据左右操作数的类型推导表达式类型
        Type leftType = types.get(ctx.expr(LEFT));
        Type rightType = types.get(ctx.expr(RIGHT));

        // 类型兼容性规则（简化版）
        // 实际编译器需要更复杂的规则
        Type resultType = inferBinaryExprType(leftType, rightType);

        // 标记表达式类型
        stashType(ctx, resultType);
        return null;
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        super.visitPrimaryID(ctx);

        // 查找变量类型
        Scope scope = scopes.get(ctx);
        Symbol s = scope.resolve(ctx.ID().getText());

        if (s == null) {
            CompilerLogger.error(ctx, "Unknown variable: " + ctx.ID().getText());
        } else {
            stashType(ctx, s.type);
        }
        return null;
    }

    @Override
    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        stashType(ctx, TypeTable.INT);
        return null;
    }

    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        stashType(ctx, TypeTable.FLOAT);
        return null;
    }

    /**
     * 推导二元表达式类型
     */
    private Type inferBinaryExprType(Type left, Type right) {
        // 简化版：如果任一操作数是 float，结果是 float
        if (left == TypeTable.FLOAT || right == TypeTable.FLOAT) {
            return TypeTable.FLOAT;
        }
        // 否则结果是 int
        return TypeTable.INT;
    }

    /**
     * 保存类型到节点
     */
    private void stashType(ParserRuleContext ctx, Type type) {
        if (types.get(ctx) != null) return; // 已有类型，不覆盖
        types.put(ctx, type);
    }

    /**
     * 复制类型（从子节点到父节点）
     */
    private void copyType(ParserRuleContext from, ParserRuleContext to) {
        Type type = types.get(from);
        types.put(to, type);
    }
}
```

**Compiler.java** - 主程序（演示多遍编译）
```java
package org.teachfx.antlr4.ep16;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep16.misc.ScopeUtil;
import org.teachfx.antlr4.ep16.parser.CymbolLexer;
import org.teachfx.antlr4.ep16.parser.CymbolParser;
import org.teachfx.antlr4.ep16.visitor.Interpreter;
import org.teachfx.antlr4.ep16.visitor.LocalDefine;
import org.teachfx.antlr4.ep16.visitor.LocalResolver;

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
 * 4. 符号定义 → LocalDefine → 作用域标记（第2遍）
 * 5. 类型推导 → LocalResolver → 类型标记（第3遍）
 * 6. 执行 → Interpreter → 运行结果
 */
public class Compiler {

    public static void main(String[] args) throws IOException {
        String fileName = null;
        fileName = "src/main/resources/t.cymbol";
        if (args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if (fileName != null) is = new FileInputStream(fileName);

        // 词法分析
        CharStream charStream = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);

        // 语法分析（第1遍）
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();

        // 符号定义（第2遍）
        LocalDefine localDefine = new LocalDefine();
        parseTree.accept(localDefine);

        // 创建作用域工具（封装作用域信息）
        ScopeUtil scopeUtil = new ScopeUtil(localDefine.getScopes());

        // 类型推导（第3遍）
        LocalResolver localResolver = new LocalResolver(scopeUtil);
        parseTree.accept(localResolver);

        // 执行（基于作用域和类型信息）
        Interpreter interpreter = new Interpreter(scopeUtil);
        interpreter.interpret(parseTree);
    }
}
```

**关键设计模式**：多遍编译 + 访问者模式

EP16 实现了真正的多遍编译架构，使用访问者模式遍历 AST：

- 第1遍：ANTLR4 Parser 构建 ParseTree
- 第2遍：LocalDefine 标记作用域
- 第3遍：LocalResolver 推导类型
- 第4遍：Interpreter 执行程序（或后续的类型检查、代码生成）

优势：
- 每遍独立，逻辑清晰
- 支持前向引用
- 便于扩展新的编译阶段

类/接口关系：
```
Scope (接口)
  ↑
GlobalScope (全局作用域)
LocalScope (局部作用域)
  ↑
ScopedSymbol (带作用域的符号)
  ↑
MethodSymbol (方法符号)

Symbol (抽象基类)
  ↑
VariableSymbol (变量符号)
BuiltInTypeSymbol (内置类型)

访问者模式：
CymbolASTVisitor<Object> (抽象基类)
  ↑
LocalDefine (第2遍：符号定义)
LocalResolver (第3遍：类型推导)
Interpreter (第4遍：执行)
```

#### 3.3 实战流程

实战步骤：构建多遍编译器，完成符号解析和类型推导

步骤1：编译 EP16 项目
```bash
# 进入 EP16 目录
cd ep16

# 编译项目
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

步骤2：创建测试输入文件
```bash
# 创建测试输入
cat > src/main/resources/test_multi_pass.txt << EOF
int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

void main() {
    int result = factorial(5);
    print(result);
}
EOF
```

步骤3：运行主程序
```bash
# 运行主程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep16.Compiler" \
    -Dexec.args="src/main/resources/test_multi_pass.txt"

# 预期输出：
# 120
```

验证方法：
- 检查点1：确认函数递归正确执行
- 检查点2：确认作用域嵌套正确（factorial 函数的 n 参数、局部变量）
- 检查点3：确认类型推导正确（int 类型）

步骤4：添加调试输出（可选）
```bash
# 在 LocalDefine 中添加作用域打印
# 修改 LocalDefine.java，添加：
public void stashScope(ParserRuleContext ctx) {
    scopes.put(ctx, currentScope);
    System.out.println("Stash scope for " + ctx.getClass().getSimpleName() +
                       " -> " + currentScope.getScopeName());
}

# 重新编译运行
mvn clean compile
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep16.Compiler" \
    -Dexec.args="src/main/resources/test_multi_pass.txt"

# 预期输出（包含调试信息）：
# Stash scope for CompilationUnitContext -> global
# Stash scope for FunctionDeclContext -> factorial
# Stash scope for VarDeclContext -> factorial
# Stash scope for BlockContext -> local_xxx
# ...
# 120
```

故障排查：

**问题1：编译错误 "cannot find symbol: class ScopeUtil"**
- 原因：ScopeUtil 类未正确导入或未创建
- 解决方法：
  ```bash
  # 检查 ScopeUtil.java 是否存在
  ls ep16/src/main/java/org/teachfx/antlr4/ep16/misc/ScopeUtil.java

  # 确认导入语句正确
  cat ep16/src/main/java/org/teachfx/antlr4/ep16/Compiler.java | grep ScopeUtil
  ```

**问题2：运行时异常 "NullPointerException"**
- 原因：某个节点的类型或作用域未正确标记
- 解决方法：
  - 在 LocalDefine 和 LocalResolver 中添加调试输出
  - 确认每个 visit 方法都调用了 stashScope 或 stashType
  - 检查 ParseTree 节点是否被正确处理

**问题3：类型推导错误**
- 原因：类型推导逻辑不完整或不正确
- 解决方法：
  - 添加调试输出，打印每个节点的类型
  - 检查 inferBinaryExprType 方法的实现
  - 确认类型兼容性规则是否正确

进阶技巧：

1. **可视化作用域链**：在 LocalDefine 中添加打印方法，以树形结构显示作用域链
2. **可视化类型标记**：在 LocalResolver 中添加打印方法，显示每个表达式的类型
3. **添加更多类型检查规则**：检查函数参数类型、返回值类型等

### 4. AI 协作线：Context Engineering 视角

#### 4.1 上下文设计

为了让 AI 帮助完成本章任务，我们需要精心设计上下文。

上下文文件列表：

**源码文件**（按阅读顺序）：
1. `ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/Symbol.java`
   - 作用：符号基类，定义所有符号的共同接口
   - 关键方法：`getName()`、`toString()`

2. `ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/Scope.java`
   - 作用：作用域接口，定义作用域的核心操作
   - 关键方法：`define()`、`resolve()`、`lookup()`

3. `ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/LocalScope.java`
   - 作用：局部作用域，表示函数、代码块的作用域
   - 关键方法：构造函数、`resolve()`

4. `ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/GlobalScope.java`
   - 作用：全局作用域，表示程序的全局作用域
   - 关键方法：`initTypeSystem()`、`resolve()`

5. `ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalDefine.java`
   - 作用：符号定义（第2遍），为每个节点标记作用域
   - 关键方法：`stashScope()`、`pushScope()`、`popScope()`

6. `ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java`
   - 作用：类型推导（第3遍），为每个节点标记类型
   - 关键方法：`stashType()`、`inferBinaryExprType()`

7. `ep16/src/main/java/org/teachfx/antlr4/ep16/Compiler.java`
   - 作用：主程序，演示多遍编译流水线
   - 关键流程：第1遍 → 第2遍 → 第3遍 → 执行

8. `ep16/src/main/java/org/teachfx/antlr4/ep16/misc/ScopeUtil.java`
   - 作用：作用域工具类，封装作用域信息
   - 关键方法：`get()`、`lookup()`、`resolve()`

**文档文件**：
1. `ep16/README.md`（如果有）
   - 作用：EP16 模块概述和使用说明
   - 关键章节：多遍编译架构、符号表设计、类型系统

2. `AGENTS.md`
   - 作用：代码规范和最佳实践
   - 相关部分：多遍编译模式、访问者模式、符号表设计模式

**测试文件**：
1. `ep16/src/test/java/org/teachfx/antlr4/ep16/`（如果有）
   - 作用：测试多遍编译功能
   - 关键测试方法：`testSymbolDefinition`、`testTypeInference`

**示例输入/输出**：
1. `ep16/src/main/resources/t.cymbol`
   - 作用：测试输入 Cymbol 程序
   - 内容：函数定义、变量声明、表达式求值

上下文组织说明：

这些文件按照"符号表 → 访问者 → 主程序"组织：

1. **符号表核心在前**：先提供 Symbol、Scope、LocalScope、GlobalScope 实现，让 AI 理解作用域设计
2. **访问者在后**：接着提供 LocalDefine、LocalResolver 实现，展示如何遍历 AST 标记作用域和类型
3. **工具类作为桥梁**：提供 ScopeUtil，封装作用域信息，供多个访问者使用
4. **主程序作为整合**：最后提供 Compiler.java，展示完整的多遍编译流程

为什么这样组织：
- AI 可以先理解作用域的数据结构设计，再理解如何在遍历 AST 时使用作用域
- 完整的上下文确保 AI 理解多遍编译的设计模式和实现方式
- 工具类帮助 AI 理解如何在多个访问者之间传递作用域信息

#### 4.2 Prompt 模板（给 AI 用）

**类型 A: 添加函数参数类型检查 Prompt 模板**
```
请在 LocalResolver 中添加函数参数类型检查，确保实际参数类型与形式参数类型兼容。

任务目标：
- 在处理函数调用时，检查实际参数类型是否匹配形式参数类型
- 如果类型不兼容，报告错误

具体要求：
1. 修改 `ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java`
   - 在 `visitExprFuncCall` 方法中添加类型检查逻辑
   - 获取函数符号（MethodSymbol）
   - 遍历实际参数和形式参数
   - 检查类型是否兼容
   - 如果不兼容，使用 `CompilerLogger.error()` 报告错误

2. 定义类型兼容性规则：
   - int 参数可以匹配 int、float 形式参数（隐式转换）
   - float 参数只能匹配 float 形式参数
   - 类型不匹配时报告错误

参考上下文文件：
- 源码：
  - `ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java`
  - `ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/MethodSymbol.java`
  - `ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/Type.java`
  - `ep16/src/main/java/org/teachfx/antlr4/ep16/misc/CompilerLogger.java`
- 文档：
  - `AGENTS.md`（代码规范）
  - `ep16/src/main/antlr4/Cymbol.g4`（语法文件）

约束条件：
- 不修改函数调用的解析逻辑
- 保持与现有类型推导的兼容性
- 错误信息清晰，指出类型不匹配的参数

期望输出：
1. 修改后的 LocalResolver.java（标注新增部分）
2. 类型兼容性规则的实现逻辑
3. 测试示例：类型正确和类型错误的函数调用及预期输出
```

**类型 B: 添加 return 语句类型检查 Prompt 模板**
```
请在 LocalResolver 中添加 return 语句类型检查，确保返回值类型与函数声明类型兼容。

任务目标：
- 在处理 return 语句时，检查返回值类型是否匹配函数声明类型
- 如果类型不兼容，报告错误

具体要求：
1. 修改 `ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java`
   - 在 `visitReturnStmt` 方法中添加类型检查逻辑
   - 获取返回值类型（从表达式的类型标记）
   - 获取函数声明类型（从函数符号）
   - 检查类型是否兼容
   - 如果不兼容，使用 `CompilerLogger.error()` 报告错误

2. 定义返回值类型兼容性规则：
   - void 函数不能返回值
   - 非 void 函数必须返回值
   - 返回值类型可以隐式转换为函数声明类型

参考上下文文件：
- 源码：
  - `ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java`
  - `ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/MethodSymbol.java`
  - `ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/Type.java`
  - `ep16/src/main/java/org/teachfx/antlr4/ep16/misc/CompilerLogger.java`
- 文档：
  - `AGENTS.md`（代码规范）
  - `ep16/src/main/antlr4/Cymbol.g4`（语法文件）

约束条件：
- 不修改 return 语句的解析逻辑
- 保持与现有类型推导的兼容性
- 错误信息清晰，指出返回值类型不匹配

期望输出：
1. 修改后的 LocalResolver.java（标注新增部分）
2. 返回值类型兼容性规则的实现逻辑
3. 测试示例：类型正确和类型错误的 return 语句及预期输出
```

#### 4.3 AI 应该做 / 不该做

在让 AI 帮助完成本章任务时，我们需要明确 AI 的职责边界。

**✅ AI 允许做的事情**：

1. **扩展类型检查规则**
   - 可以：添加函数参数类型检查
   - 可以：添加 return 语句类型检查
   - 可以：添加赋值语句类型检查
   - 不能：修改类型系统的核心设计（Type 基类）

2. **增强作用域管理**
   - 可以：添加作用域嵌套的调试输出
   - 可以：添加作用域查找的辅助方法
   - 不能：修改作用域的查找逻辑（resolve 方法）

3. **增强类型推导**
   - 可以：添加更复杂的类型推导规则
   - 可以：添加类型转换规则
   - 不能：修改类型推导的核心算法

4. **生成测试用例**
   - 可以：生成类型检查的测试用例
   - 可以：生成作用域嵌套的测试用例
   - 不能：删除或破坏现有测试

5. **添加错误处理**
   - 可以：添加更详细的错误消息
   - 可以：添加错误位置信息（行号、列号）
   - 不能：改变错误处理的策略（如从异常改为静默失败）

**❌ AI 禁止做的事情**：

1. **修改多遍编译架构**
   - 不允许：删除或合并编译阶段（如删除 LocalDefine）
   - 不允许：改变访问者模式的设计
   - 原因：多遍编译架构是编译器前端的基础

2. **修改作用域的核心查找逻辑**
   - 不允许：修改 Scope.resolve() 的实现
   - 不允许：改变作用域链的构建方式
   - 原因：作用域查找是符号解析的核心

3. **删除内置类型**
   - 不允许：从 GlobalScope.initTypeSystem() 中删除内置类型
   - 不允许：修改内置类型的名称
   - 原因：内置类型是语言的基础

4. **引入新的外部依赖**
   - 不允许：在 pom.xml 中添加新库
   - 不允许：使用第三方类型推导库
   - 原因：保持项目技术栈一致性

5. **破坏访问者模式**
   - 不允许：将 CymbolASTVisitor 改为抽象类
   - 不允许：删除 visit 方法的重载机制
   - 原因：访问者模式是多遍编译的核心

**🤝 灰色区域（需谨慎处理）**：

1. **优化类型推导性能**
   - 可以：添加类型缓存
   - 需谨慎：确保缓存一致性

2. **增强类型系统但不破坏兼容性**
   - 可以：添加新的类型（如 long、double）
   - 需谨慎：确保新类型与现有类型兼容

如果 AI 提议超出范围的操作，请：
- 明确拒绝并说明原因
- 要求 AI 在当前范围内完成任务
- 保留超出范围的建议，待后续章节讨论

#### 4.4 验证与回滚策略

在 AI 完成修改后，我们必须进行严格的验证才能将更改合并到主分支。

## 自动化验证

**步骤1：运行相关测试**
```bash
# 进入 EP16 目录
cd ep16

# 运行所有测试（如果有）
mvn test

# 或者运行特定测试类
mvn test -Dtest={测试类名}Test

# 预期输出：
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
```

**关键测试**：
- `LocalDefineTest`（如果有）- 验证符号定义
- `LocalResolverTest`（如果有）- 验证类型推导

**验证标准**：
- ✅ 所有测试通过（Failures: 0, Errors: 0）
- ✅ 没有新的编译警告
- ✅ 作用域和类型标记正确

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
int add(int a, int b) {
    return a + b;
}

void main() {
    int result = add(3, 5);
    print(result);
}
EOF

# 运行主程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep16.Compiler" \
    -Dexec.args="src/main/resources/test_ai.txt"

# 预期输出：
# 8
```

## 手工检查点

即使所有测试通过，我们仍需手工检查以下关键点：

**检查1：代码风格符合规范**
- [ ] 包命名：`org.teachfx.antlr4.ep16.visitor`、`org.teachfx.antlr4.ep16.symtab`
- [ ] 类命名：PascalCase（如 `LocalDefine`、`LocalResolver`）
- [ ] 方法命名：camelCase（如 `stashScope`、`inferBinaryExprType`）
- [ ] 导入顺序：ANTLR4 → 外部库 → 内部 → Java stdlib

**检查2：没有引入新的编译错误**
- [ ] 查看项目根目录的 `mvn clean compile` 输出
- [ ] 确认没有新的 ERROR 或 WARNING
- [ ] 检查 AI 修改的类是否能正常编译

**检查3：没有破坏现有功能**
- [ ] 运行原有测试（如果有），确保不失败
- [ ] 验证多遍编译流水线仍能正确执行
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
git stash push -m "AI changes for type checking"

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
git checkout HEAD~1 -- ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java
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
git checkout -b ai-experiment-type-checking

# 让 AI 在新分支工作
# 完成后合并回主分支
git checkout main
git merge ai-experiment-type-checking
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
  2. 检查类型推导逻辑是否正确
  3. 确认作用域查找是否正确
- 解决：根据失败信息修改 AI 代码，或调整测试用例

**问题2：编译错误**
- 现象：`[ERROR] COMPILATION ERROR`
- 排查：
  1. 检查 AI 是否使用了错误的包名
  2. 确认类名是否正确
  3. 验证继承关系是否正确
- 解决：清理 target 目录重新编译，或修正代码

**问题3：类型推导错误**
- 现象：表达式类型推导不正确
- 排查：
  1. 添加调试输出，打印每个节点的类型
  2. 检查 inferBinaryExprType 方法的实现
  3. 确认类型兼容性规则是否正确
- 解决：根据调试信息修正代码

### 5. 练习题

本章练习题

练习1：添加除零检查（手工实现版）

难度：⭐⭐☆☆☆
预计时间：30–45 分钟

题目描述：
在 LocalResolver 中添加除零检查，当除数为常量 0 时报告错误。

要求：
- 完全手工实现，不依赖 AI
- 在除法表达式中检查右操作数是否为 0
- 如果是常量 0，报告错误

验收标准：
- [ ] 在 visitExprBinary 中添加除零检查
- [ ] 检测到 `x / 0` 时报告错误
- [ ] 代码能编译通过
- [ ] 测试除零时报错

**💡 解题思路提示**：
- 在 visitExprBinary 中检查运算符是否为除法
- 检查右操作数是否为 PrimaryINT 且值为 0
- 使用 CompilerLogger.error() 报告错误

---

练习2：添加数组类型支持（AI 协作版）

难度：⭐⭐⭐☆☆
预计时间：45–60 分钟

题目描述：
在类型系统中添加数组类型，支持数组类型声明和访问。

AI 协作要求：
1. 设计上下文：列出需要提供给 AI 的文件和说明
2. 设计 Prompt：参考本章的 Prompt 模板，设计适合此任务的 Prompt
3. 验证 AI 输出：使用本章的验证策略
4. 理解 AI 代码：确保你能解释 AI 生成的每一部分

验收标准：
- [ ] AI 生成的代码能编译通过
- [ ] 在类型系统中添加数组类型（如 `int[]`）
- [ ] 支持数组类型声明（`int arr[10];`）
- [ ] 支持数组类型访问（`arr[0]`）
- [ ] 你能解释 AI 代码的实现逻辑

**💡 解题思路提示**：
- 上下文：提供 Type.java、LocalResolver.java、Cymbol.g4
- Prompt：参考"类型 A: 添加函数参数类型检查 Prompt 模板"
- 验证：运行示例程序，检查数组类型是否正确处理

---

练习3：实现类型推导可视化（综合挑战）

难度：⭐⭐⭐⭐☆
预计时间：60–90 分钟

题目描述：
为 LocalResolver 添加类型推导可视化，以树形结构显示表达式的类型推导过程。

要求：
- 可以选择手工实现或 AI 协作
- 如果选择 AI 协作，需要详细记录协作过程
- 提交时说明 AI 参与部分和人工修改部分

验收标准：
- [ ] 可视化显示表达式的 AST 结构
- [ ] 标记每个节点的类型
- [ ] 清晰显示类型推导过程
- [ ] 功能完整，性能合理
- [ ] 代码可读性良好

**💡 解题思路提示**：
- 在 LocalResolver 中添加可视化方法
- 递归遍历 AST，打印节点类型
- 使用缩进表示节点层级
- 在 visit 方法中收集类型信息

---

练习4：实现完整的类型检查器（进阶练习 - 可选）

难度：⭐⭐⭐⭐⭐
预计时间：90–120 分钟

题目描述：
实现完整的类型检查器，检查函数参数、返回值、赋值语句等所有类型操作。

适合人群：
- 想深入理解类型系统的高级读者
- 有志于实现完整编译器类型检查的高级读者

**💡 解题思路提示**：
- 在 LocalResolver 中添加完整的类型检查逻辑
- 检查函数调用参数类型
- 检查 return 语句类型
- 检查赋值语句类型
- 报告所有类型错误

### 6. 本章小结与下一章预告

## 本章小结

通过本章的学习，你已经掌握了：

1. **完整类型系统设计**
   - 理解了类型系统的组成（类型、类型规则、类型推导）
   - 掌握了类型推导算法（从表达式推导类型）
   - 学会了类型检查的实现（验证类型兼容性）

2. **多作用域符号表设计**
   - 理解了作用域链的层次结构（全局 → 函数 → 局部）
   - 掌握了符号查找机制（从内向外）
   - 能够独立设计多作用域符号表

3. **真正的多遍编译架构**
   - 理解了多遍编译的设计原则（每遍专注一个任务）
   - 掌握了访问者模式在多遍编译中的应用
   - 学会了如何设计多遍编译流水线

4. **编译器前端完整实践**
   - 熟练使用了 LocalDefine（符号定义）
   - 掌握了 LocalResolver（类型推导）
   - 理解了多遍编译的优势（支持前向引用、便于扩展）

【你现在站在哪】:
```
... → AST 构建 → 符号定义 → ✅ **完整类型检查** → [代码生成] → ...
```

**当前在编译器流水线的位置**：
- 本章实现了编译器流水线的完整前端阶段
- 构建了多作用域符号表、完整类型系统、多遍编译架构

**与下一章的衔接**：
- 本章的多遍编译架构和类型系统将在下一章被总结
- 编译器前端的知识将为学习编译器后端（IR、优化、代码生成）奠定基础
- 模块 2（从解释到编译）将在下一章完成总结

## 下一章预告

**第9章：从解释到编译的转换**

在下一章，我们将学习：
- 单遍解释器到多遍编译器的演变过程
- 编译器前端设计模式总结
- 解释器和编译器的区别和联系
- 编译器流水线的完整回顾

你将能够：
- 理解从解释到编译的技术演进
- 掌握编译器前端的设计模式和最佳实践
- 为学习编译器后端做好准备
- 深入理解现代编译器的架构思想

**准备**：为了学习下一章，建议：
- [ ] 复习模块 2 的所有章节（第6-8章）
- [ ] 运行完整的编译器示例，加深理解
- [ ] 对比解释器和编译器的实现差异

继续加油！下一章将完成模块 2 的学习，带你进入编译器的完整世界。
