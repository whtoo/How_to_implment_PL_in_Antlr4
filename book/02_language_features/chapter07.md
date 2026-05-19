# 第7章：符号解析与类型系统

## 1. 本章概述

本章聚焦于**符号表设计与类型系统实现**，它是编译器流水线中的**语义分析基础阶段**。通过学习本章，你将掌握多遍编译架构的设计原则、符号表的层次结构管理、基础类型系统的实现，以及 Parser Actions 的应用，这是后续完整类型检查和多遍编译的基石。

【你现在站在哪】:
```
AST 构建 → ✅ **符号解析** → [类型检查] → [多遍编译架构] → ...
```

## 2. 动机与真实场景

**真实场景：静态代码分析工具中的符号解析需求**

想象你正在开发一个代码质量分析平台，用户需要以下功能：

- 检测未定义变量引用（`x = 10; y = x;` 中 x 未定义）
- 验证类型兼容性（`int x = 3.14;` 是否合法）
- 识别重复声明（`int x; int x;` 是否报错）
- 支持前向引用（`f(); void f() {}` 在 C 语言中合法）
- 管理嵌套作用域（函数内的局部变量不应与全局变量冲突）

**具体问题和挑战**

如果直接在 AST 构建阶段完成所有语义分析，你将面临：

1. **前向引用无法处理**：函数 `f()` 在定义前被调用时，单遍解析无法找到函数的符号
2. **类型信息缺失**：AST 节点在构建时尚未关联类型信息，无法进行类型检查
3. **作用域管理混乱**：全局变量、局部变量、嵌套作用域的查找逻辑分散在多个地方
4. **错误恢复困难**：语义错误（如类型不匹配）与语法错误混在一起，难以区分

**缺失本章能力的痛点**

1. **无法正确解析符号**：变量、函数在使用时无法确定其声明位置和类型
2. **类型系统缺失**：无法检测类型错误（`int x = "hello";`）
3. **作用域管理混乱**：无法区分不同作用域中的同名变量
4. **无法扩展语义分析**：后续的类型检查、优化等阶段无法利用符号表

**本章将教你如何：**

- 设计符号表接口和实现，支持作用域链查找
- 构建基础类型系统（int、float）
- 使用 Parser Actions 在解析时完成符号定义
- 理解多遍编译的必要性，为后续章节奠定基础

---

## 3. 人类工程师线：技术与实现

### 3.1 核心概念

#### 核心概念 1：多遍编译（Multi-pass Compilation）

**通俗解释**：

多遍编译是指将编译过程分成多个阶段（遍），每个阶段完成特定任务，后一阶段利用前一阶段的结果。就像做饭时，先准备食材（词法分析），再切菜（语法分析），然后烹饪（语义分析），最后装盘（代码生成）。

**为什么要多遍编译？**

- **处理前向引用**：函数 `f()` 在定义前被调用，第一遍收集所有符号定义，第二遍解析引用
- **分离关注点**：每遍专注一个任务，代码更清晰、更易维护
- **支持复杂语义分析**：类型检查、优化等需要完整的符号表

[图1：单遍 vs 多遍编译对比]

**单遍编译（单阶段）**：
```
源代码
  ↓
解析 + 符号定义 + 符号解析 + 类型检查（全部在解析时完成）
  ↓
问题：
- 遇到 f(); 时，void f() {} 还未解析
- 类型信息不完整，无法进行类型检查
- 作用域管理分散在多个地方
```

**多遍编译（多阶段）**：
```
源代码
  ↓
第1遍：解析 → ParseTree（语法结构）
  ↓
第2遍：符号定义 → 填充符号表（收集所有变量、函数、类型）
  ↓
第3遍：符号解析 → 为变量引用查找符号（建立引用与定义的链接）
  ↓
第4遍：类型检查 → 验证类型正确性（int x = 3.14; ❌）
  ↓
优势：
- 第2遍收集所有定义，第3遍可以前向引用
- 每遍专注一个任务，代码清晰
- 完整的符号表，支持复杂语义分析
```

**图示说明**：
- 左侧：单遍编译在解析时完成所有分析，遇到前向引用无法处理
- 右侧：多遍编译分阶段完成，第2遍定义符号，第3遍解析引用

**类比理解**：
- 单遍编译就像"第一次读小说，读到一半才知道谁是主角"
- 多遍编译就像"先通读小说，了解全貌，再写书评"

---

#### 核心概念 2：符号表（Symbol Table）

**通俗解释**：

符号表是编译器管理程序中名称（变量、函数、类型）的数据结构，就像一本"程序字典"，记录每个名称的含义（类型、作用域、位置等）。

**符号表的核心职责**：

1. **定义符号**：遇到 `int x;` 时，在符号表中添加 `x: int`
2. **解析符号**：遇到 `y = x;` 时，在符号表中查找 `x` 的定义
3. **管理作用域**：全局变量、局部变量、嵌套作用域的查找

[图2：符号表层次结构]

**单作用域符号表（EP14 实现）**：
```
SymbolTable（全局作用域）
  ├─ int (BuiltIntTypeSymbol)
  ├─ float (BuiltIntTypeSymbol)
  ├─ x (VariableSymbol: int)
  └─ y (VariableSymbol: float)
```

**多作用域符号表（后续章节）**：
```
GlobalScope（全局作用域）
  ├─ int (TypeSymbol)
  ├─ float (TypeSymbol)
  ├─ main (MethodSymbol)
  │   └─ LocalScope（局部作用域）
  │       ├─ x (VariableSymbol: int)
  │       └─ y (VariableSymbol: float)
  │       └─ BlockScope（if 块作用域）
  │           └─ z (VariableSymbol: int)
  └─ print (MethodSymbol)
      └─ LocalScope
          └─ value (VariableSymbol: float)
```

**图示说明**：
- EP14 实现的是单作用域符号表（简化版本）
- 后续章节扩展为多作用域符号表，支持作用域链查找
- 内置类型（int、float）在全局作用域中预定义

**类比理解**：
- 符号表就像"公司组织架构"：
  - 公司（全局作用域）包含所有部门（函数）
  - 每个部门有员工（变量）
  - 子部门（代码块）有临时工（局部变量）
- 作用域链就像"从当前部门向上查找员工信息"

**符号查找逻辑**：

1. **单作用域**：直接在全局符号表中查找
2. **多作用域**：
   - 先在当前作用域查找
   - 未找到，向父作用域查找
   - 递归直到全局作用域
   - 未找到，报错"未定义符号"

---

#### 核心概念 3：类型系统（Type System）

**通俗解释**：

类型系统是一组规则，定义程序中值和表达式的类型，以及类型如何相互操作。就像仓库的"分类规则"：苹果（整数）只能和苹果放在一起，橙子（浮点数）可以和苹果混放（但有规则）。

**类型系统的组成**：

1. **类型定义**：int、float、char、void 等类型
2. **类型规则**：int + int → int, int + float → float（隐式转换）
3. **类型检查**：int 变量 = float 字面量 ❌（需要显式转换）

[图3：类型系统基础]

**类型层次结构**：
```
Type（类型接口）
  ↑
BuiltIntTypeSymbol（内置类型符号，实现 Type 接口）
  ├─ int (整数类型)
  ├─ float (浮点类型)
  └─ char (字符类型 - 后续章节)
```

**类型规则示例**：

1. **类型兼容性**：
   - int + int → int ✅
   - float + float → float ✅
   - int + float → float ✅（隐式转换：int 提升为 float）

2. **类型检查**：
   - int 变量 = int 字面量 ✅（`int x = 10;`）
   - float 变量 = int 字面量 ✅（`float y = 10;`，隐式转换）
   - int 变量 = float 字面量 ❌（`int x = 3.14;`，需要显式转换）

3. **类型推导**：
   - int x; // x 的类型为 int
   - int x = 5; // x 的类型为 int，初始值为 5
   - var y = 10; // 推导 y 的类型为 int（后续章节）

**图示说明**：
- 类型接口 `Type` 定义了类型的基本行为（getName()、isPrimitive()）
- `BuiltIntTypeSymbol` 实现内置类型（int、float）
- 类型规则定义了类型之间的兼容性和转换规则

**类比理解**：
- 类型系统就像"仓库分类规则"：
  - 整数（苹果）只能和整数放在一起
  - 浮点数（橙子）可以和整数混放（但有规则）
  - 类型检查就像检查仓库分类是否正确

**类型系统的重要性**：

1. **安全**：防止类型错误（如将字符串赋值给整数）
2. **优化**：类型信息可以用于编译器优化（如整数运算比浮点运算快）
3. **文档**：类型即文档，变量 `int count` 比 `var count` 更清晰

---

#### 核心概念 4：Parser Actions（解析器动作）

**通俗解释**：

Parser Actions 是在 ANTLR4 语法规则中嵌入的代码，在解析过程中执行。就像"边读书边做笔记"：阅读时（解析时）立即记录关键信息（符号定义）。

**Parser Actions 的作用**：

- 在解析时完成语义分析（如符号定义）
- 将语法分析与语义分析结合（简化版单遍编译）
- 填充符号表，记录变量声明

[图4：Parser Actions 示例]

**语法规则（不含 Parser Actions）**：
```antlr4
varDeclaration
    : type ID ';'
    ;
```
**解析过程**：只构建 ParseTree，不执行任何语义分析

**语法规则（含 Parser Actions）**：
```antlr4
varDeclaration
    : type ID {
          // Parser Action：在解析到类型和变量名时
          // 1. 获取类型（从符号表查找）
          BuiltIntTypeSymbol sym = (BuiltIntTypeSymbol)symtab.resolve($type.text);
          // 2. 创建变量符号
          VariableSymbol vs = new VariableSymbol($ID.text, sym);
          // 3. 将变量添加到当前作用域
          symtab.define(vs);
      }
      ';'
    ;
```
**解析过程**：构建 ParseTree 的同时，执行 Parser Action，将变量添加到符号表

**图示说明**：
- 左侧：纯语法规则，不包含语义分析
- 右侧：在规则中嵌入代码，定义变量时立即添加到符号表
- `$type.text` 表示 type 规则匹配的文本（"int" 或 "float"）
- `$ID.text` 表示 ID 词法规则匹配的文本（变量名）

**Parser Actions 的位置**：

在 ANTLR4 中，Parser Actions 可以放在以下位置：

1. **规则级别**（规则名后）：
   ```antlr4
   varDeclaration
       : type ID {
           // 在匹配 type ID 后执行
       }
       ';'
       ;
   ```

2. **选择级别**（选择分支后）：
   ```antlr4
   expr
       : INT { /* 整数字面量 */ }
       | FLOAT { /* 浮点字面量 */ }
       | ID { /* 变量引用 */ }
       ;
   ```

3. **后置动作**（规则匹配完成后）：
   ```antlr4
   type returns [Type tsym]
   @after {
       // 规则匹配完成后执行
       System.out.println("ref " + $tsym.getName());
   }
   :   'int' { $tsym = (Type)symtab.resolve("int"); }
   |   'float' { $tsym = (Type)symtab.resolve("float"); }
   ;
   ```

**Parser Actions 的优势**：

- **简化流水线**：在解析时完成符号定义，无需额外遍历
- **性能**：单遍解析，效率高
- **直观**：语法规则和语义逻辑结合，易于理解

**Parser Actions 的局限**：

- **无法处理前向引用**：变量必须在声明后才能使用
- **耦合度高**：语法规则和语义逻辑耦合在一起
- **不适用于复杂场景**：类型检查、优化等需要多次遍历

**类比理解**：
- 不含 Actions：读完小说再整理笔记（多遍编译）
- 含 Actions：边读小说边做笔记（单遍编译）
- 后续章节将用访问者模式替代 Parser Actions，实现真正的多遍编译

---

### 3.2 与仓库 EP 的对应关系

#### 对应 EP：EP14

EP14 实现了单作用域符号表和基础类型系统，是理解符号解析和类型系统的绝佳案例。

**目录结构**：
```
ep14/
├── src/main/java/org/teachfx/antlr4/ep14/
│   ├── Compiler.java                          // 主程序（演示 Parser Actions）
│   ├── compiler/                             // ANTLR4 生成的编译器
│   │   ├── MathExprLexer.java                 // 词法分析器
│   │   ├── MathExprParser.java                // 语法分析器（含 Parser Actions）
│   │   └── MathExprBaseListener.java         // 基础监听器
│   └── symtab/                              // 符号表和类型系统
│       ├── Symbol.java                       // 符号基类
│       ├── SymbolTable.java                  // 全局符号表（单作用域）
│       ├── Scope.java                        // 作用域接口
│       ├── BaseScope.java                    // 作用域基类（预留）
│       ├── VariableSymbol.java                // 变量符号
│       ├── BuiltIntTypeSymbol.java            // 内置类型符号
│       ├── Type.java                         // 类型接口
│       ├── ScopedSymbol.java                 // 有作用域的符号（预留）
│       └── SymbolCollector.java              // 符号收集器（预留）
├── src/main/antlr4/
│   └── MathExpr.g4                          // 数学表达式语法（含 Parser Actions）
└── src/main/resources/
    └── t.math                               // 测试输入文件
```

**关键类/方法说明**：

#### Symbol - 符号基类

**类的作用**：所有符号（变量、函数、类型）的抽象基类，定义符号的共同属性（名称、类型、作用域）。

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
 *
 * 设计考虑：
 * - name 字段：包级访问权限（同一包内可见）
 * - type 字段：public，方便外部访问
 * - scope 字段：public，记录符号定义的作用域
 * - 提供两种构造函数：仅名称、名称+类型
 */
public class Symbol {
    public Type type;     // 符号类型（int、float 等）
    public Scope scope;   // 符号所在作用域
    String name;          // 符号名称

    /**
     * 构造函数 1：仅名称（类型默认为 UNDEFINED）
     *
     * @param name 符号名称
     */
    public Symbol(String name) {
        this.name = name;
        this.type = SymbolTable.UNDEFINED;  // 默认为未定义类型
    }

    /**
     * 构造函数 2：名称 + 类型
     *
     * @param name 符号名称
     * @param type 符号类型
     */
    public Symbol(String name, Type type) {
        this(name);  // 调用构造函数 1
        this.type = type != null ? type : SymbolTable.UNDEFINED;
    }

    /**
     * 辅助方法：去除字符串两端的括号
     * 用于处理某些语法规则中的括号符号
     *
     * @param s 输入字符串（带括号）
     * @return 去除括号后的字符串
     */
    public static String stripBrackets(String s) {
        return s.substring(1, s.length() - 1);
    }

    /**
     * 获取符号名称
     *
     * @return 符号名称
     */
    public String getName() {
        return name;
    }

    /**
     * toString 方法：打印符号信息
     * 格式：<作用域.名称:类型>
     *
     * @return 符号的字符串表示
     */
    public String toString() {
        String s = "";
        if (scope != null) s = scope.getScopeName() + ".";
        if (type != null) return '<' + s + getName() + ":" + type + ">";
        return s + getName();
    }
}
```

**设计要点**：
- `name` 字段使用包级访问权限，同一包内（`symtab`）可以直接访问
- `type` 和 `scope` 字段为 `public`，方便外部访问和修改
- 两种构造函数灵活使用：可以仅设置名称，也可以同时设置类型
- `toString()` 方法格式化输出符号信息，便于调试

---

#### Type - 类型接口

**接口的作用**：定义类型的基本行为，所有类型（int、float）都实现此接口。

```java
package org.teachfx.antlr4.ep14.symtab;

/**
 * 类型接口
 * 定义类型的基本行为
 *
 * 职责：
 * - 获取类型名称
 * - 判断是否为基本类型（primitive）
 *
 * 设计考虑：
 * - 接口，不包含任何实现
 * - 后续章节可以添加更多方法（如类型兼容性检查）
 */
public interface Type {
    /**
     * 获取类型名称
     *
     * @return 类型名称（如 "int"、"float"）
     */
    String getName();

    /**
     * 判断是否为基本类型（primitive）
     * 基本类型：int、float、char、bool 等
     * 非基本类型：数组、结构体、类等（后续章节）
     *
     * @return 是否为基本类型
     */
    boolean isPrimitive();
}
```

**设计要点**：
- 接口设计，便于后续扩展（添加更多类型）
- `getName()` 方法返回类型名称
- `isPrimitive()` 方法区分基本类型和复合类型

---

#### BuiltIntTypeSymbol - 内置类型符号

**类的作用**：表示语言内置的类型（int、float），继承 Symbol 并实现 Type 接口。

```java
package org.teachfx.antlr4.ep14.symtab;

/**
 * 内置类型符号
 * 表示语言内置的类型（int、float、char 等）
 *
 * 职责：
 * - 实现类型接口
 * - 判断为基本类型（primitive）
 *
 * 设计考虑：
 * - 继承 Symbol，拥有名称、类型、作用域属性
 * - 实现 Type 接口，提供类型行为
 * - 内置类型都是基本类型（isPrimitive() 返回 true）
 */
public class BuiltIntTypeSymbol extends Symbol implements Type {

    /**
     * 构造函数
     *
     * @param name 类型名称（如 "int"、"float"）
     */
    public BuiltIntTypeSymbol(String name) {
        super(name);  // 调用 Symbol 构造函数
    }

    /**
     * 实现接口方法：获取类型名称
     *
     * @return 类型名称
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 实现接口方法：判断是否为基本类型
     * 内置类型（int、float）都是基本类型
     *
     * @return true
     */
    @Override
    public boolean isPrimitive() {
        return true;
    }
}
```

**设计要点**：
- 继承 `Symbol`，拥有符号的基本属性（名称、类型、作用域）
- 实现 `Type` 接口，提供类型的基本行为
- `isPrimitive()` 返回 `true`，表示是基本类型

---

#### VariableSymbol - 变量符号

**类的作用**：表示程序中的变量（包括参数和局部变量）。

```java
package org.teachfx.antlr4.ep14.symtab;

/**
 * 变量符号
 * 表示程序中的变量（包括参数和局部变量）
 *
 * 职责：
 * - 继承 Symbol，拥有名称、类型、作用域属性
 * - 泛型参数 T extends Type：约束类型参数必须是 Type 的子类
 *
 * 设计考虑：
 * - 提供两种构造函数：仅名称、名称+类型
 * - 泛型参数 T extends Type：类型安全
 */
public class VariableSymbol<T extends Type> extends Symbol {

    /**
     * 构造函数 1：仅名称（类型默认为 UNDEFINED）
     *
     * @param name 变量名称
     */
    public VariableSymbol(String name) {
        super(name);  // 调用 Symbol 构造函数
    }

    /**
     * 构造函数 2：名称 + 类型
     *
     * @param name 变量名称
     * @param type 变量类型（int、float 等）
     */
    public VariableSymbol(String name, T type) {
        super(name, type);  // 调用 Symbol 构造函数
    }
}
```

**设计要点**：
- 泛型参数 `T extends Type`：约束类型参数必须是 `Type` 的子类（如 `BuiltIntTypeSymbol`）
- 两种构造函数灵活使用

---

#### Scope - 作用域接口

**接口的作用**：定义作用域的核心操作，所有作用域（全局、局部）都实现此接口。

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
 *
 * 设计考虑：
 * - 接口，不包含任何实现
 * - 提供作用域的基本契约
 */
public interface Scope {
    /**
     * 获取作用域名称
     *
     * @return 作用域名称（如 "global"、"main"、"local"）
     */
    String getScopeName();

    /**
     * 获取父作用域（包围当前作用域的作用域）
     *
     * @return 父作用域（全局作用域的父作用域为 null）
     */
    Scope getEnclosingScope();

    /**
     * 定义符号（添加到当前作用域）
     *
     * @param sym 要定义的符号
     */
    void define(Symbol sym);

    /**
     * 解析符号（从当前作用域向上查找）
     * 单作用域符号表：仅在当前作用域查找
     * 多作用域符号表：从当前作用域向上查找，直到全局作用域
     *
     * @param name 符号名称
     * @return 找到的符号（未找到返回 null）
     */
    Symbol resolve(String name);

    /**
     * 查找类型（返回符号的类型）
     *
     * @param name 符号名称
     * @return 符号的类型
     */
    Type lookup(String name);
}
```

**设计要点**：
- 定义了作用域的核心操作：定义符号、解析符号、查找类型
- `resolve()` 方法支持作用域链查找（从当前作用域向上查找）
- `lookup()` 方法直接返回类型（简化版，调用 `resolve()`）

---

#### SymbolTable - 全局符号表

**类的作用**：全局符号表，管理所有符号（变量、类型），单作用域实现。

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
 *
 * 设计考虑：
 * - 实现 Scope 接口
 * - 使用 HashMap 存储符号（名称 -> 符号）
 * - 初始化时添加内置类型（int、float）
 */
public class SymbolTable implements Scope {
    static Type UNDEFINED;  // 未定义类型
    Map<String, Symbol> symbols;  // 符号存储（名称 -> 符号）

    /**
     * 构造函数
     * - 初始化符号存储（HashMap）
     * - 初始化类型系统（添加内置类型）
     */
    public SymbolTable() {
        symbols = new HashMap<>();
        initTypeSystem();  // 初始化内置类型
    }

    /**
     * 初始化类型系统
     * 添加内置类型：int、float
     */
    private void initTypeSystem() {
        symbols.put("int", new BuiltIntTypeSymbol("int"));
        symbols.put("float", new BuiltIntTypeSymbol("float"));
    }

    /**
     * 实现接口方法：获取作用域名称
     *
     * @return 作用域名称（"global"）
     */
    @Override
    public String getScopeName() {
        return "global";
    }

    /**
     * 实现接口方法：获取父作用域
     * 全局作用域没有父作用域
     *
     * @return null
     */
    @Override
    public Scope getEnclosingScope() {
        return null;
    }

    /**
     * 实现接口方法：定义符号（添加到当前作用域）
     *
     * @param sym 要定义的符号
     */
    @Override
    public void define(Symbol sym) {
        symbols.put(sym.name, sym);  // 使用符号名称作为键
    }

    /**
     * 实现接口方法：解析符号（从当前作用域查找）
     * 单作用域符号表：仅在当前作用域查找
     *
     * @param name 符号名称
     * @return 找到的符号（未找到返回 null）
     */
    @Override
    public Symbol resolve(String name) {
        return symbols.get(name);
    }

    /**
     * 实现接口方法：查找类型（返回符号的类型）
     *
     * @param name 符号名称
     * @return 符号的类型
     */
    @Override
    public Type lookup(String name) {
        return resolve(name).type;  // 先解析符号，再获取类型
    }

    /**
     * toString 方法：打印符号表信息
     * 格式：作用域名称:{符号列表}
     *
     * @return 符号表的字符串表示
     */
    @Override
    public String toString() {
        return getScopeName() + ":" + symbols;
    }
}
```

**设计要点**：
- 使用 `HashMap<String, Symbol>` 存储符号，快速查找（O(1)）
- 构造函数调用 `initTypeSystem()`，初始化内置类型
- `define()` 方法直接使用 `HashMap.put()`，如果符号已存在，会覆盖（未检查重复）
- `resolve()` 方法直接使用 `HashMap.get()`，查找失败返回 `null`
- `UNDEFINED` 类型用于表示未定义的符号

---

#### MathExpr.g4 - 语法文件（含 Parser Actions）

**文件的作用**：定义数学表达式的语法规则，并在规则中嵌入 Parser Actions，完成符号定义。

```antlr4
grammar MathExpr;

@lexer::header {
    package org.teachfx.antlr4.ep14.compiler;
}

@parser::header {
    package org.teachfx.antlr4.ep14.compiler;

    import org.teachfx.antlr4.ep14.symtab.*;  // 导入符号表相关类
}

@parser::members {
    SymbolTable symtab;  // 在解析器中持有符号表引用
}

// 编译单元：顶层规则，接收符号表参数
compileUnit[SymbolTable symtab]
    @init {
        this.symtab = symtab;  // 将参数符号表赋值给解析器字段
    }
    : varDelaration+  // 一个或多个变量声明
    ;

// 变量声明规则（含 Parser Actions）
varDelaration
    : vtype=type name=ID ('=' value=varSlot)?  ';'  // 类型 变量名 (= 表达式)? ;
      {
          // Parser Action 1：解析到类型和变量名时执行
          // 1. 从符号表查找类型（int 或 float）
          BuiltIntTypeSymbol sym = (BuiltIntTypeSymbol)symtab.resolve($vtype.text);

          // 2. 创建变量符号
          VariableSymbol vs = new VariableSymbol($name.text, sym);

          // 3. 将变量添加到符号表
          symtab.define(vs);

          // 4. 打印调试信息
          System.out.println($name.text + " ref to " + symtab.resolve($name.text));
      }
    ;

// 表达式规则（简化版，第8章扩展）
varSlot
    : lhs=varSlot op='+' rhs=varSlot  // 加法表达式
    | INT                             // 整数字面量
    | FLOAT                           // 浮点数字面量
    | name=ID                          // 变量引用
      {
          // Parser Action 2：解析到变量引用时执行
          System.out.println("a2 line " + $name.getLine() + " " + $name.text + " : ref to " +
                             symtab.resolve($name.text));
      }
    | '(' varSlot ')'                  // 括号表达式
    ;

// 类型规则（返回 Type 对象）
type returns [Type tsym]
@after {
    // Parser Action 3：规则匹配完成后执行
    System.out.println("a3 line " + $start.getLine() + ": ref " + $tsym.getName());
}
:   'int'   {
        // 匹配 'int'，从符号表查找 int 类型
        $tsym = (Type)symtab.resolve("int");
    }
  |  'float' {
        // 匹配 'float'，从符号表查找 float 类型
        $tsym = (Type)symtab.resolve("float");
    }
;

// 词法规则
OP_ADD: '+';
OP_SUB: '-';
OP_MUL: '*';
OP_DIV: '/';
INT   : '0'..'9'+;
FLOAT : INT '.' [0-9]+;
ID    : [a-zA-Z]+;
WS    : [ \t\r\n] -> channel(HIDDEN);
```

**设计要点**：

1. **Parser Actions 的位置**：
   - `@init`：规则开始匹配时执行（`compileUnit` 规则）
   - `{}` 内：规则匹配到特定位置时执行（`varDelaration` 规则）
   - `@after`：规则匹配完成后执行（`type` 规则）

2. **Parser Actions 的语法**：
   - `$vtype.text`：获取 type 规则匹配的文本（"int" 或 "float"）
   - `$name.text`：获取 ID 词法规则匹配的文本（变量名）
   - `$name.getLine()`：获取 ID 的行号（用于错误定位）

3. **符号表的传递**：
   - `compileUnit[SymbolTable symtab]`：定义参数 `symtab`
   - `@init { this.symtab = symtab; }`：将参数赋值给解析器字段

4. **类型规则的返回值**：
   - `type returns [Type tsym]`：定义返回值 `tsym`（类型为 `Type`）
   - `$tsym = ...`：设置返回值

---

#### Compiler.java - 主程序

**类的作用**：演示完整的编译器流水线，从输入到符号表的完整流程。

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
 * 主程序：演示 Parser Actions 的使用
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
        // 步骤 0：准备输入源
        String fileName = "src/main/resources/t.math";
        if (args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if (fileName != null) is = new FileInputStream(fileName);

        // 步骤 1：创建符号表（包含内置类型 int、float）
        SymbolTable syTb = new SymbolTable();

        // 步骤 2：词法分析：字符流 → TokenStream
        CharStream inputStream = CharStreams.fromStream(is);
        MathExprLexer lexer = new MathExprLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 步骤 3：语法分析：TokenStream → ParseTree（Parser Actions 在此阶段完成符号定义）
        MathExprParser parser = new MathExprParser(tokens);

        // 步骤 4：调用 compileUnit 规则，传递符号表
        parser.compileUnit(syTb);

        // 步骤 5：打印符号表
        System.out.println("Symbol Table: " + syTb);
    }
}
```

**设计要点**：
- 支持从文件或标准输入读取（通过命令行参数 `args[0]` 指定文件）
- 创建 `SymbolTable`，初始化内置类型（int、float）
- 调用 `parser.compileUnit(syTb)`，传递符号表给解析器
- Parser Actions 在解析时完成符号定义（无需额外遍历）

**编译器流水线**：
1. **输入准备**：从文件或标准输入读取
2. **词法分析**：`CharStream` → `MathExprLexer` → `TokenStream`
3. **语法分析**：`TokenStream` → `MathExprParser` → `ParseTree`
4. **符号定义**：Parser Actions 在解析时完成，填充符号表

---

### 3.3 实战流程

#### 步骤 1：编译 EP14 项目

进入 EP14 目录，编译项目（包括生成 ANTLR4 代码）。

**操作**：
```bash
# 进入 EP14 目录
cd ep14

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
- 检查 `target/generated-sources/antlr4` 目录是否包含生成的 ANTLR4 代码（`MathExprParser.java`、`MathExprLexer.java` 等）

**故障排查**：

如果出现编译错误，检查：
1. Java 版本是否为 21 或更高（`java -version`）
2. Maven 版本是否为 3.8+（`mvn -version`）
3. ANTLR4 插件是否正确生成代码（检查 `target/generated-sources/antlr4` 目录）

---

#### 步骤 2：查看测试输入文件

EP14 默认提供了一个测试输入文件 `t.math`。

**操作**：
```bash
# 查看测试输入文件
cat ep14/src/main/resources/t.math
```

**文件内容**：
```math
int x;
float y;
int z;
```

**文件内容说明**：
- 第 1 行：声明 `int` 类型变量 `x`
- 第 2 行：声明 `float` 类型变量 `y`
- 第 3 行：声明 `int` 类型变量 `z`

---

#### 步骤 3：运行主程序

运行 Compiler 主程序，处理测试输入文件。

**操作**：
```bash
# 运行主程序
cd ep14
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" \
    -Dexec.args="src/main/resources/t.math"
```

**预期输出**：
```
a3 line 1: ref int
x ref to <x:int>
a3 line 2: ref float
y ref to <y:float>
a3 line 3: ref int
z ref to <z:int>
Symbol Table: global:{int=int, float=float, x=<x:int>, y=<y:float>, z=<z:int>}
```

**输出解析**：

1. `a3 line 1: ref int`：在类型规则 `type` 的 `@after` 动作中打印（解析到 `int`）
2. `x ref to <x:int>`：在变量声明规则 `varDelaration` 的 Parser Action 中打印（定义变量 `x`）
3. `a3 line 2: ref float`：解析到 `float`
4. `y ref to <y:float>`：定义变量 `y`
5. `a3 line 3: ref int`：解析到 `int`
6. `z ref to <z:int>`：定义变量 `z`
7. `Symbol Table: global:{...}`：打印最终的符号表

**符号表内容**：
- `int=int`：内置类型 `int`
- `float=float`：内置类型 `float`
- `x=<x:int>`：变量 `x`，类型为 `int`
- `y=<y:float>`：变量 `y`，类型为 `float`
- `z=<z:int>`：变量 `z`，类型为 `int`

**验证方法**：

检查每个输出是否符合预期：

| 输出行 | 预期输出 | 说明 |
|--------|---------|------|
| `a3 line 1: ref int` | ✅ | 解析到 `int` 类型 |
| `x ref to <x:int>` | ✅ | 定义变量 `x`，类型为 `int` |
| `a3 line 2: ref float` | ✅ | 解析到 `float` 类型 |
| `y ref to <y:float>` | ✅ | 定义变量 `y`，类型为 `float` |
| `a3 line 3: ref int` | ✅ | 解析到 `int` 类型 |
| `z ref to <z:int>` | ✅ | 定义变量 `z`，类型为 `int` |
| `Symbol Table: ...` | ✅ | 符号表包含所有符号 |

---

#### 步骤 4：创建自定义测试输入

创建自定义测试输入文件，测试更多场景。

**操作**：
```bash
# 创建测试输入
cat > ep14/src/main/resources/test_symbol.txt << EOF
int a;
float b;
int c;
EOF
```

**运行主程序**：
```bash
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" \
    -Dexec.args="src/main/resources/test_symbol.txt"
```

**预期输出**：
```
a3 line 1: ref int
a ref to <a:int>
a3 line 2: ref float
b ref to <b:float>
a3 line 3: ref int
c ref to <c:int>
Symbol Table: global:{int=int, float=float, a=<a:int>, b=<b:float>, c=<c:int>}
```

**验证方法**：
- 确认内置类型（int、float）已添加到符号表
- 确认变量声明（a、b、c）已正确添加到符号表
- 确认变量类型正确（a:int, b:float, c:int）

---

#### 步骤 5：测试变量引用（可选）

修改测试输入文件，添加变量引用。

**操作**：
```bash
# 创建测试输入（包含变量引用）
cat > ep14/src/main/resources/test_ref.txt << EOF
int x;
int y;
x + y;
EOF
```

**运行主程序**：
```bash
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" \
    -Dexec.args="src/main/resources/test_ref.txt"
```

**预期输出**：
```
a3 line 1: ref int
x ref to <x:int>
a3 line 2: ref int
y ref to <y:int>
a2 line 3 x : ref to <x:int>
a2 line 3 y : ref to <y:int>
Symbol Table: global:{int=int, x=<x:int>, y=<y:int>}
```

**输出解析**：
- 第 1-2 行：定义变量 `x` 和 `y`
- 第 3 行：`x + y` 表达式，解析到变量引用 `x` 和 `y`
- `a2 line 3 x : ref to <x:int>`：在表达式规则 `varSlot` 的 Parser Action 中打印（引用变量 `x`）
- `a2 line 3 y : ref to <y:int>`：引用变量 `y`

**验证方法**：
- 确认变量引用成功找到符号（`ref to <x:int>`）
- 确认符号表包含所有符号

---

#### 步骤 6：测试重复声明（可选）

创建测试输入文件，测试重复声明。

**操作**：
```bash
# 创建测试输入（包含重复声明）
cat > ep14/src/main/resources/test_duplicate.txt << EOF
int x;
int x;
EOF
```

**运行主程序**：
```bash
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" \
    -Dexec.args="src/main/resources/test_duplicate.txt"
```

**预期输出**：
```
a3 line 1: ref int
x ref to <x:int>
a3 line 2: ref int
x ref to <x:int>
Symbol Table: global:{int=int, x=<x:int>}
```

**输出解析**：
- 第二次声明 `int x;` 覆盖了第一次声明
- 符号表中只保留一个 `x`
- 注意：EP14 未检查重复声明，后续章节会添加重复声明检查

**验证方法**：
- 观察到第二次声明覆盖了第一次声明
- 符号表中只保留一个 `x`
- 后续章节会添加重复声明检查，报错"重复声明"

---

#### 故障排查提示

| 问题 | 可能原因 | 解决方法 |
|------|---------|---------|
| 编译错误 "cannot find symbol: VariableSymbol" | Parser Actions 中使用了错误的类名 | 检查 MathExpr.g4 中的 import 语句：`import org.teachfx.antlr4.ep14.symtab.*;` |
| 运行时异常 "NullPointerException" | Parser Actions 在解析时访问了未初始化的符号表 | 确认 Compiler.java 中正确传递了符号表：`parser.compileUnit(syTb);` |
| 类型解析失败 | type 规则未正确返回 Type 对象 | 检查 MathExpr.g4 中 type 规则的 Parser Actions：`$tsym = (Type)symtab.resolve("int");` |
| 变量引用失败 | 变量未定义或符号表查找失败 | 检查变量是否已声明，确认符号表包含该变量 |

---

## 4. AI 协作线：Context Engineering 视角

### 4.1 上下文设计

为了让 AI 帮忙完成本章任务，我们需要精心设计上下文。

#### 上下文文件列表

**源码文件**（按阅读顺序）：

1. **`ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/Symbol.java`**
   - 作用：符号基类，定义所有符号的共同接口
   - 关键字段：`name`、`type`、`scope`
   - 关键方法：`getName()`、`toString()`

2. **`ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/Type.java`**
   - 作用：类型接口，定义类型的基本行为
   - 关键方法：`getName()`、`isPrimitive()`

3. **`ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/BuiltIntTypeSymbol.java`**
   - 作用：内置类型符号（int、float）
   - 关键方法：`isPrimitive()`（返回 true）

4. **`ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/VariableSymbol.java`**
   - 作用：变量符号，表示程序中的变量
   - 关键构造函数：`VariableSymbol(String name, T type)`

5. **`ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/Scope.java`**
   - 作用：作用域接口，定义作用域的核心操作
   - 关键方法：`define()`、`resolve()`、`lookup()`

6. **`ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java`**
   - 作用：全局符号表，管理所有符号
   - 关键方法：`initTypeSystem()`、`define()`、`resolve()`

7. **`ep14/src/main/antlr4/MathExpr.g4`**
   - 作用：数学表达式语法，含 Parser Actions
   - 关键规则：`varDelaration`、`type`、`varSlot`
   - 关键 Parser Actions：符号定义、变量引用

8. **`ep14/src/main/java/org/teachfx/antlr4/ep14/Compiler.java`**
   - 作用：主程序，演示 Parser Actions 的使用
   - 关键流程：创建符号表 → 词法分析 → 语法分析 → 符号定义

**文档文件**：

1. **`AGENTS.md`**
   - 作用：代码规范和最佳实践
   - 相关部分：符号表设计模式、Parser Actions 使用规范、代码风格约定

2. **`README.md`**
   - 作用：项目概览和学习路径
   - 相关部分：渐进式学习路径（EP1-EP21）、技术栈、编译器流水线

**测试文件**：

1. **`ep14/src/main/resources/t.math`**（如果有）
   - 作用：测试输入文件
   - 内容：变量声明示例

2. **`ep14/src/test/java/org/teachfx/antlr4/ep14/`**（如果有）
   - 作用：测试符号表和类型系统
   - 关键测试方法：`testSymbolDefinition`、`testTypeResolution`

**示例输入/输出**：

1. **`ep14/src/main/resources/test_symbol.txt`**（自定义创建）
   - 作用：测试输入变量声明
   - 内容：`int x; float y; int z;`

2. **`ep14/src/main/resources/test_ref.txt`**（自定义创建）
   - 作用：测试变量引用
   - 内容：`int x; int y; x + y;`

3. **`ep14/src/main/resources/test_duplicate.txt`**（自定义创建）
   - 作用：测试重复声明
   - 内容：`int x; int x;`

---

#### 上下文组织说明

这些文件按照"符号表核心 → 解析器 → 主程序"组织：

1. **符号表核心在前**：先提供 Symbol、Type、VariableSymbol、Scope、SymbolTable 实现，让 AI 理解符号表设计
2. **解析器在后**：接着提供 MathExpr.g4，展示如何在语法中嵌入 Parser Actions
3. **主程序作为整合**：最后提供 Compiler.java，展示完整的编译流程
4. **示例文件作为验证**：提供测试输入，用于验证符号表正确性

**为什么这样组织**：

- AI 可以先理解符号表的数据结构设计，再理解如何在解析时使用符号表
- 完整的上下文确保 AI 理解 Parser Actions 的作用和实现方式
- 示例文件帮助验证 AI 生成的代码是否正确

---

### 4.2 Prompt 模板（给 AI 用）

#### 模板类型 A：添加重复声明检查 Prompt 模板

**适用场景**：在 SymbolTable 中添加重复声明检查，拒绝定义已存在的符号。

**Prompt 模板**：
```
任务：在 SymbolTable 中添加重复声明检查

背景：
我正在学习编译器实现，使用 Java 21 + ANTLR4 4.13.2。
项目是一个渐进式学习的编译器项目，每个 EP 聚焦 1-2 个核心概念。
当前在 EP14，已经实现了单作用域符号表和基础类型系统。

任务目标：
修改 SymbolTable.define() 方法，检测重复声明，拒绝定义已存在的符号。

具体要求：
1. 修改 ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java
   - 在 define(Symbol sym) 方法中添加重复检查
   - 如果 symbols.containsKey(sym.name)，抛出 IllegalStateException
   - 错误消息包含符号名称："Duplicate symbol: x"

2. 创建自定义异常类（可选）
   - DuplicateSymbolException.java，继承 RuntimeException
   - 包含符号名称和位置信息

约束条件：
1. 不修改其他符号表方法（resolve、lookup）
2. 保持与现有编译器流水线的兼容性
3. 错误信息清晰，包含符号名称
4. 遵循项目代码规范（包命名、类命名、方法命名）

参考文件：

源码：
- ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java
- ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/Symbol.java

文档：
- AGENTS.md（代码规范）
- ep14/src/main/antlr4/MathExpr.g4（语法文件）

测试：
- ep14/src/main/resources/test_duplicate.txt（测试输入）

期望输出：
1. 修改后的 SymbolTable.java（标注新增部分）
2. 如果创建了 DuplicateSymbolException.java，提供完整代码
3. 测试示例：运行重复声明时的预期输出

验证方法：
1. 运行 mvn clean compile，确保编译成功
2. 运行示例程序：mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" -Dexec.args="src/main/resources/test_duplicate.txt"
3. 预期输出：抛出异常 "Duplicate symbol: x" 或类似错误消息
4. 检查符号表是否只包含一个 x

设计要求：
- 重复检查逻辑简单直接：使用 symbols.containsKey(sym.name)
- 错误消息清晰：包含符号名称
- 异常类型合理：使用 IllegalStateException 或自定义异常
```

**Prompt 设计说明**：
- 明确任务目标：添加重复声明检查
- 列出具体要求（4 条）：修改方法、创建异常类（可选）、保持兼容性、错误信息清晰
- 提供参考文件列表（源码、文档、测试）
- 说明期望输出（3 项：修改后的代码、异常类、测试示例）
- 说明验证方法（4 步：编译、运行示例、检查异常、检查符号表）
- 提供设计要求（4 项：检查逻辑、错误消息、异常类型）

---

#### 模板类型 B：添加 char 类型 Prompt 模板

**适用场景**：在类型系统中添加 char 类型，支持字符类型的变量声明。

**Prompt 模板**：
```
任务：在类型系统中添加 char 类型

背景：
我正在学习编译器实现，使用 Java 21 + ANTLR4 4.13.2。
项目是一个渐进式学习的编译器项目，每个 EP 聚焦 1-2 个核心概念。
当前在 EP14，已经实现了 int 和 float 类型。

任务目标：
在类型系统中添加 char 类型，支持字符类型的变量声明。

具体要求：
1. 修改 ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java
   - 在 initTypeSystem() 方法中添加 char 类型
   - 符号名称：char
   - 符号类型：BuiltIntTypeSymbol("char")

2. 修改 ep14/src/main/antlr4/MathExpr.g4
   - 在 type 规则中添加 char 类型分支
   - 返回 Type 对象：$tsym = (Type)symtab.resolve("char");

3. 创建测试输入文件 test_char.txt
   - 内容：char c; int x; float y;

约束条件：
1. 不修改其他符号表方法
2. 保持与现有编译器流水线的兼容性
3. char 类型是基本类型（isPrimitive() 返回 true）
4. 遵循项目代码规范（包命名、类命名、方法命名）

参考文件：

源码：
- ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java
- ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/Type.java
- ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/BuiltIntTypeSymbol.java

文档：
- AGENTS.md（代码规范）
- ep14/src/main/antlr4/MathExpr.g4（语法文件）

期望输出：
1. 修改后的 SymbolTable.java（标注新增部分：initTypeSystem 方法）
2. 修改后的 MathExpr.g4（标注新增部分：type 规则）
3. test_char.txt 文件内容
4. 测试示例：运行 mvn exec:java -Dexec.args="src/main/resources/test_char.txt" 的预期输出

验证方法：
1. 运行 mvn clean compile，确保编译成功
2. 运行示例程序：mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" -Dexec.args="src/main/resources/test_char.txt"
3. 预期输出：符号表包含 char、int、float 类型，以及 c、x、y 变量
4. 检查符号表内容：global:{int=int, float=float, char=char, c=<c:char>, x=<x:int>, y=<y:float>}

设计要求：
- char 类型初始化方式与 int、float 一致：new BuiltIntTypeSymbol("char")
- type 规则添加新分支：'char' { $tsym = (Type)symtab.resolve("char"); }
- 测试输入包含 char、int、float 类型声明
```

**Prompt 设计说明**：
- 明确任务目标：添加 char 类型
- 列出具体要求（4 条）：修改 SymbolTable、修改语法、创建测试、保持兼容性
- 提供参考文件列表（源码、文档）
- 说明期望输出（4 项：修改后的代码、修改后的语法、测试文件、测试示例）
- 说明验证方法（4 步：编译、运行示例、检查符号表）
- 提供设计要求（3 项：初始化方式、语法规则、测试内容）

---

### 4.3 AI 应该做 / 不该做

在让 AI 帮助完成本章任务时，我们需要明确 AI 的职责边界。

#### ✅ AI 允许做的事情

1. **扩展符号表功能**
   - 可以：添加重复声明检查、未定义变量检测
   - 可以：添加符号表的辅助方法（如 `printSymbols()`）
   - 不能：修改符号表的核心设计（如从 HashMap 改为其他数据结构）

2. **扩展类型系统**
   - 可以：添加新的内置类型（如 char、bool）
   - 可以：添加类型兼容性检查逻辑
   - 不能：修改 Type 接口的设计

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

---

#### ❌ AI 禁止做的事情

1. **修改符号表的核心数据结构**
   - 不允许：将 `HashMap<String, Symbol>` 改为其他数据结构
   - 不允许：修改 Scope 接口的方法签名
   - 原因：符号表设计是编译器前端的基础

2. **改变 Parser Actions 的位置**
   - 不允许：将 `symtab.define(var)` 移出 varDelaration 规则
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

---

#### 🤝 灰色区域（需谨慎处理）

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

---

### 4.4 验证与回滚策略

在 AI 完成修改后，我们必须进行严格的验证才能将更改合并到主分支。

#### 自动化验证

**步骤 1：运行编译**
```bash
# 进入 EP14 目录
cd ep14

# 清理并重新编译
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

**验证标准**：
- ✅ 编译成功（BUILD SUCCESS）
- ✅ 没有新的编译错误或警告
- ✅ ANTLR4 代码正确生成（检查 `target/generated-sources/antlr4`）

---

**步骤 2：运行示例程序**
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
# a3 line 1: ref int
# x ref to <x:int>
# a3 line 2: ref float
# y ref to <y:float>
# a3 line 3: ref int
# z ref to <z:int>
# Symbol Table: global:{int=int, float=float, x=<x:int>, y=<y:float>, z=<z:int>}
```

**验证标准**：
- ✅ 程序正常执行，无异常
- ✅ 符号表包含正确的符号和类型
- ✅ 输出格式与预期一致

---

#### 手工检查点

即使所有测试通过，我们仍需手工检查以下关键点：

**检查 1：代码风格符合规范**
- [ ] 包命名：`org.teachfx.antlr4.ep14.symtab`
- [ ] 类命名：PascalCase（如 `SymbolTable`、`VariableSymbol`）
- [ ] 方法命名：camelCase（如 `define`、`resolve`）
- [ ] 导入顺序：ANTLR4 → 外部库 → 内部 → Java stdlib

**检查 2：没有引入新的编译错误**
- [ ] 查看项目根目录的 `mvn clean compile` 输出
- [ ] 确认没有新的 ERROR 或 WARNING
- [ ] 检查 AI 修改的类是否能正常编译

**检查 3：没有破坏现有功能**
- [ ] 运行原有示例程序（`t.math`），确保输出一致
- [ ] 验证符号表仍能正确定义和解析符号
- [ ] 确认 Git diff 只包含预期修改

**检查 4：文档完整性**
- [ ] 新增类/方法有 JavaDoc 注解
- [ ] 关键算法有时间/空间复杂度说明
- [ ] 复杂逻辑有行内注释

---

#### 回滚方案

如果 AI 修改后出现问题，使用以下步骤快速恢复：

##### 方案 1：Git Stash（推荐）

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

---

##### 方案 2：Git Checkout（硬恢复）

```bash
# 查看修改历史
git log --oneline -5

# 恢复到修改前的提交
git checkout {commit_hash}

# 或者恢复特定文件
git checkout HEAD~1 -- ep14/src/main/java/org/teachfx/antlr4/ep14/symtab/SymbolTable.java
```

---

##### 方案 3：Git Reset（危险，谨慎使用）

```bash
# 软重置（保留 AI 修改在 staging）
git reset --soft HEAD~1

# 硬重置（完全丢弃 AI 修改）
git reset --hard HEAD~1

# 混合重置（保留 AI 修改在未跟踪）
git reset --mixed HEAD~1
```

---

##### 方案 4：创建新分支实验

```bash
# 从干净状态创建新分支
git checkout -b ai-experiment-symbol-table

# 让 AI 在新分支工作
# 完成后合并回主分支
git checkout main
git merge ai-experiment-symbol-table
```

---

#### 验证流程总结

```
AI 生成代码
    ↓
自动化验证（mvn clean compile + mvn exec:java）
    ↓
手工检查（代码风格、功能完整性）
    ↓
运行示例程序
    ↓
✅ 验证通过，合并到主分支
    ❌ 验证失败，执行回滚
```

---

#### 常见问题排查

**问题 1：编译失败**
- 现象：`[ERROR] COMPILATION ERROR`
- 排查：
  1. 检查 AI 是否使用了错误的包名
  2. 确认类名是否正确
  3. 验证继承关系是否正确
- 解决：清理 target 目录重新编译，或修正代码

**问题 2：运行时异常**
- 现象：`java.lang.Exception`
- 排查：
  1. 检查 Parser Actions 是否正确调用 `symtab.define()`
  2. 确认符号名称拼写正确
  3. 验证符号表引用是否正确传递
- 解决：根据异常信息修正代码

**问题 3：符号表解析失败**
- 现象：变量无法在符号表中找到
- 排查：
  1. 检查 Parser Actions 是否正确调用 `symtab.define()`
  2. 确认符号名称拼写正确
  3. 验证符号表引用是否正确传递
- 解决：根据调试信息修正代码

---

## 5. 练习题

### 练习 1：添加未定义变量检测（手工实现版）

**难度**：⭐⭐☆☆☆
**预计时间**：30–45 分钟

**题目描述**：
在 MathExprParser 中添加未定义变量检测，当表达式中使用了未声明的变量时，报错。

**要求**：
- 完全手工实现，不依赖 AI
- 在表达式规则 `varSlot` 的 ID 分支中添加 Parser Actions
- 检查符号表中是否存在该变量
- 如果未定义，打印错误信息或抛出异常

**验收标准**：
- [ ] 在 varSlot 规则的 ID 分支添加检查逻辑
- [ ] 如果变量未定义，打印错误信息
- [ ] 代码能编译通过
- [ ] 测试未定义变量时报错

**💡 解题思路提示**：
- 在 `varSlot` 规则的 ID 分支添加 Parser Actions
- 使用 `symtab.resolve($name.text)` 查找变量
- 如果返回 null，说明变量未定义，打印错误或抛出异常
- 错误信息示例："Undefined variable: x at line 3"

---

### 练习 2：添加 char 类型（AI 协作版）

**难度**：⭐⭐⭐☆☆
**预计时间**：45–60 分钟

**题目描述**：
在类型系统中添加 char 类型，支持字符类型的变量声明。

**AI 协作要求**：
1. 设计上下文：列出需要提供给 AI 的文件和说明
2. 设计 Prompt：参考本章的 Prompt 模板，设计适合此任务的 Prompt
3. 验证 AI 输出：使用本章的验证策略
4. 理解 AI 代码：确保你能解释 AI 生成的每一部分

**验收标准**：
- [ ] AI 生成的代码能编译通过
- [ ] 在符号表中添加 char 类型
- [ ] 在语法中支持 char 类型声明
- [ ] 测试 `char c;` 能正确添加到符号表
- [ ] 你能解释 AI 代码的实现逻辑

**💡 解题思路提示**：
- 上下文：提供 SymbolTable.java、Type.java、MathExpr.g4
- Prompt：参考"模板类型 B：添加 char 类型 Prompt 模板"
- 验证：运行示例程序，检查符号表是否包含 char 类型

---

### 练习 3：实现类型推导（综合挑战）

**难度**：⭐⭐⭐⭐☆
**预计时间**：60–90 分钟

**题目描述**：
为表达式添加类型推导功能，在解析时计算表达式的类型。

**要求**：
- 可以选择手工实现或 AI 协作
- 如果选择 AI 协作，需要详细记录协作过程
- 提交时说明 AI 参与部分和人工修改部分

**验收标准**：
- [ ] 表达式的类型推导正确（int + int → int, float + float → float）
- [ ] 隐式类型转换正确（int + float → float）
- [ ] 类型不匹配时报错（如 string + int）
- [ ] 功能完整，性能合理
- [ ] 代码可读性良好

**💡 解题思路提示**：
- 在表达式规则 `varSlot` 中添加类型推导的 Parser Actions
- 返回类型（使用 `returns [Type type]`）
- 根据左右操作数的类型推导表达式类型
- 添加类型转换规则（int + float → float）

---

### 练习 4：实现作用域嵌套（进阶练习 - 可选）

**难度**：⭐⭐⭐⭐⭐
**预计时间**：90–120 分钟

**题目描述**：
将单作用域符号表扩展为多作用域符号表，支持嵌套作用域（如函数内部有局部作用域）。

**适合人群**：
- 想深入理解符号表的高级读者
- 有志于实现完整编译器前端的高级读者

**💡 解题思路提示**：
- 在 Scope 接口中添加 `getEnclosingScope()` 方法（已有）
- 在 SymbolTable 中添加父作用域引用（全局作用域为 null）
- 创建 LocalScope 类，继承 BaseScope
- 在解析函数和代码块时创建新作用域
- 实现作用域链查找（从当前作用域向上查找）

---

## 6. 本章小结与下一章预告

### 本章小结

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

---

### 下一章预告

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
