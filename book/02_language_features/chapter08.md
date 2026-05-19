# 第8章：完整类型检查与多遍编译架构

## 1. 本章概述

本章聚焦于**完整类型检查与多遍编译架构**，它是编译器流水线中的**完整前端阶段**。通过学习本章，你将掌握类型推导算法、多作用域符号表设计、以及真正的多遍编译架构，这是现代编译器前端的标准实践。

【你现在站在哪】:
```
... → AST 构建 → 符号定义 → ✅ **完整类型检查** → [代码生成] → ...
```

## 2. 动机与真实场景

**真实场景：你需要构建一个完整的编译器**

想象你在开发一个面向教育的 C 语言编译器项目。你的编译器需要：

- 支持完整的 Cymbol 语言语法（函数定义、变量声明、控制流语句）
- 正确处理嵌套作用域（全局变量、函数参数、局部变量、块级作用域）
- 进行完整的类型检查（int、float、char、bool 类型推导和验证）
- 支持函数的前向引用（函数调用在函数定义之前）
- 为后续的代码生成阶段提供完整的类型信息

**具体问题和挑战**

如果继续使用单遍编译的 Parser Actions 方式，你将面临：

1. **前向引用无法处理**：
   ```c
   int result = add(3, 5);  // 调用 add 函数

   int add(int a, int b) {  // add 函数定义在后
       return a + b;
   }
   ```
   在单遍编译中，解析到 `add(3, 5)` 时，`add` 函数还未定义，无法正确解析。

2. **嵌套作用域管理复杂**：
   ```c
   int x = 10;  // 全局变量

   void main() {
       int x = 5;  // 遮蔽全局 x
       {
           int y = 3;  // 块级变量
           print(x + y);  // 应该打印 8，使用局部 x
       }
   }
   ```
   需要正确处理变量遮蔽（shadowing）和作用域链查找。

3. **类型推导需要在完整 AST 上进行**：
   ```c
   float result = 2 + 3.5 * 4;  // 类型推导需要遍历表达式树
   ```
   类型推导需要访问完整的表达式结构，无法在解析时完成。

4. **类型检查需要多次遍历**：
   - 第一遍：定义符号（变量、函数）
   - 第二遍：推导表达式类型
   - 第三遍：检查类型兼容性

**缺失本章能力的痛点**

1. **无法正确处理嵌套作用域**：变量遮蔽问题导致错误的符号解析
2. **无法实现完整的类型检查**：表达式类型推导不完整，类型错误无法在编译时发现
3. **无法为后续阶段提供完整的类型信息**：代码生成和优化阶段需要准确的类型信息
4. **编译器架构混乱**：单遍编译方式难以扩展，添加新的编译阶段困难

**本章将教你如何**：

- 设计多作用域符号表，正确处理嵌套作用域和变量遮蔽
- 实现完整的类型推导算法，为表达式计算类型
- 构建真正的多遍编译架构，支持前向引用和类型检查
- 使用访问者模式遍历 ParseTree，完成符号定义和类型推导

---

## 3. 人类工程师线：技术与实现

### 3.1 核心概念

#### 核心概念 1：多遍编译架构（Multi-pass Compilation Architecture）

**通俗解释**：

多遍编译架构是指将编译过程分为多个独立的阶段（遍），每个阶段专注于特定任务，通过访问者模式遍历 ParseTree，后一阶段利用前一阶段的结果。

打个比方：
- 多遍编译就像**工厂流水线**：
  - 第1道工序：铸造零件（AST 构建）
  - 第2道工序：组装零件（符号定义）
  - 第3道工序：打磨零件（类型推导）
  - 第4道工序：质量检查（类型检查）

**优势**：
- 每遍专注一个任务，逻辑清晰
- 支持前向引用（函数调用在定义之前）
- 便于扩展（添加新的编译阶段）

[图1：完整多遍编译架构]

```
第1遍：语法分析（ParseTree 构建）
CharStream → Lexer → TokenStream → Parser → ParseTree
职责：将字符流转换为语法树

第2遍：符号定义（LocalDefine）
ParseTree → LocalDefine → ParseTreeProperty<Scope>
职责：遍历 AST，创建作用域，为每个节点标记作用域
  - 遍历 ParseTree，创建作用域
  - 将符号添加到对应作用域
  - 建立作用域链
  - 使用 ParseTreeProperty<Scope> 存储每个节点的作用域

第3遍：类型推导（LocalResolver）
ParseTree → LocalResolver → ParseTreeProperty<Type>
职责：遍历 AST，查找符号，推导表达式类型
  - 遍历 ParseTree，查找符号
  - 推导表达式类型（二元表达式、函数调用等）
  - 标记每个节点的类型
  - 使用 ParseTreeProperty<Type> 存储每个节点的类型

第4遍：解释执行（Interpreter）
ParseTree → Interpreter → 运行结果
职责：基于作用域和类型信息执行程序
  - 访问变量符号，获取内存中的值
  - 计算表达式值
  - 处理函数调用
```

**图示说明**：
- 多遍编译将编译过程分解为独立的阶段
- 每个阶段通过访问者模式遍历 ParseTree
- 后一阶段利用前一阶段的结果（作用域、类型）
- ParseTreeProperty 用于在遍历之间传递信息

**相关概念**：
- **访问者模式**：分离数据结构（ParseTree）与操作（符号定义、类型推导）
- **ParseTreeProperty**：ANTLR4 提供的工具，用于将额外信息附加到 ParseTree 节点
- **作用域链**：嵌套的作用域结构，用于符号查找

---

#### 核心概念 2：多作用域符号表（Multi-Scope Symbol Table）

**通俗解释**：

多作用域符号表是层次化的符号管理结构，每个作用域包含自己的符号，形成作用域链，符号查找从内向外进行。

打个比方：
- 作用域链就像**多级文件夹**：
  - 全局文件夹（根目录）包含所有公共文件
  - 函数文件夹（子目录）包含函数的私有文件
  - 块文件夹（孙目录）包含块的临时文件
  - 查找文件时，先查当前目录，再查父目录

[图2：多作用域符号表结构]

```
作用域链示例（基于代码）：
int x = 10;  // 全局变量

void main() {
    int x = 5;  // main 函数的局部变量（遮蔽全局 x）
    {
        int y = 3;  // if 块的局部变量
        print(x + y);  // 使用 main.x (5) + y (3) = 8
    }
    print(x);  // 使用 main.x (5)
}

int factorial(int n) {  // 函数参数和返回值
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

作用域结构：
GlobalScope（全局作用域）
  ├─ int (TypeSymbol)
  ├─ float (TypeSymbol)
  ├─ x (VariableSymbol: int, 值: 10)
  ├─ main (MethodScope)
  │   ├─ x (VariableSymbol: int, 值: 5)  // 遮蔽全局 x
  │   └─ LocalScope (if 块)
  │       └─ y (VariableSymbol: int, 值: 3)
  └─ factorial (MethodScope)
      ├─ n (VariableSymbol: int, 参数)
      └─ LocalScope (if 块)

符号查找示例：
1. 在 main 函数的 if 块中引用 "x"：
   - 当前作用域（LocalScope）查找 x → 未找到
   - 父作用域（main MethodScope）查找 x → 找到！返回 main.x
2. 在 main 函数的 if 块中引用 "y"：
   - 当前作用域（LocalScope）查找 y → 找到！返回 y
3. 在 factorial 函数中引用 "x"：
   - 当前作用域（factorial MethodScope）查找 x → 未找到
   - 父作用域（GlobalScope）查找 x → 找到！返回 GlobalScope.x

变量遮蔽示例：
GlobalScope: x (int, 值: 10)
main 函数: x (int, 值: 5)  // 遮蔽全局 x
在 main 函数中引用 "x" → 返回 main.x（遮蔽全局 x）
```

**图示说明**：
- 作用域链形成层次结构，从内向外查找符号
- 内层作用域可以遮蔽外层作用域的同名符号
- 每个函数有自己的作用域，包含参数和局部变量
- 符号查找遵循"最近优先"原则

**相关概念**：
- **作用域**：变量声明的有效区域
- **变量遮蔽（Shadowing）**：内层作用域的同名变量覆盖外层作用域的变量
- **作用域链**：嵌套的作用域结构，用于符号查找

---

#### 核心概念 3：类型推导（Type Inference）

**通俗解释**：

类型推导是指根据表达式的结构和操作数的类型，自动推导出整个表达式的类型。

打个比方：
- 类型推导就像**计算器**：
  - 输入：2（int）、3（int）
  - 计算：2 + 3 = 5
  - 输出：5（int）
  - 如果输入：2.5（float）、3（int）
  - 计算：2.5 + 3 = 5.5（float）

[图3：类型推导示例]

```
表达式 1：2 + 3 * 4

ParseTree 结构（简化）：
       +
      / \
     2   *
        / \
       3   4

类型推导过程（后序遍历）：
1. 访问 NumberNode(2) → int
2. 访问 NumberNode(3) → int
3. 访问 NumberNode(4) → int
4. 访问 MultiplicationNode(int, int) → int (int * int → int)
5. 访问 AdditionNode(int, int) → int (int + int → int)
最终类型：int

表达式 2：2.5 + 3

ParseTree 结构（简化）：
       +
      / \
    2.5  3

类型推导过程：
1. 访问 NumberNode(2.5) → float
2. 访问 NumberNode(3) → int
3. 访问 AdditionNode(float, int) → float (float + int → float，隐式转换)
最终类型：float

表达式 3：5 / 2

类型推导过程：
1. 访问 NumberNode(5) → int
2. 访问 NumberNode(2) → int
3. 访问 DivisionNode(int, int) → int (int / int → int，整数除法)
最终类型：int（结果：2）

表达式 4：5.0 / 2

类型推导过程：
1. 访问 NumberNode(5.0) → float
2. 访问 NumberNode(2) → int
3. 访问 DivisionNode(float, int) → float (float / int → float，浮点除法)
最终类型：float（结果：2.5）

类型推导规则（简化版）：
- int + int → int
- float + float → float
- int + float → float（隐式转换 int → float）
- float + int → float（隐式转换 int → float）
- int * int → int
- float * float → float
- int * float → float（隐式转换 int → float）
- float / float → float
- int / int → int（整数除法，结果为整数）
```

**图示说明**：
- 类型推导从叶子节点（常量、变量）开始
- 向上推导父节点的类型
- 根据类型兼容性规则确定最终类型
- 后序遍历：先计算子节点，再计算父节点

**相关概念**：
- **类型兼容性**：类型之间是否可以相互操作
- **隐式类型转换**：自动将一种类型转换为另一种类型（如 int → float）
- **整数除法**：int 类型除法，结果为整数（舍去小数部分）

---

#### 核心概念 4：类型检查（Type Checking）

**通俗解释**：

类型检查是指在编译时验证程序中的类型操作是否符合类型系统的规则，报告类型错误。

打个比方：
- 类型检查就像**安全检查员**：
  - 检查每个操作是否符合安全规则
  - 不允许苹果和橙子相加（int + string）
  - 要求明确类型转换（如 `(int)3.14`）

[图4：类型检查示例]

```
类型检查规则：

1. 变量赋值：
   int x;
   x = 5;          ✅ int → int
   x = 3.14;       ❌ float → int（需要显式转换：x = (int)3.14;）

2. 函数参数：
   void f(int x) { ... }
   f(5);           ✅ int → int
   f(3.14);        ❌ float → int（需要显式转换：f((int)3.14);）

3. 返回值：
   int g() {
       return 5;    ✅ int → int
       return 3.14; ❌ float → int（需要显式转换：return (int)3.14;）
   }

4. 运算符：
   int a = 5, b = 3;
   int c = a + b;   ✅ int + int → int
   float d = a + b; ✅ int + int → int → float（隐式转换）

类型错误示例：

错误 1：类型不兼容的赋值
int x = 5;
x = "hello";  ❌ string → int（类型不兼容）

错误 2：类型不兼容的函数调用
int add(int a, int b) { return a + b; }
add(3, 3.14);  ❌ float → int（类型不兼容）

错误 3：类型不兼容的返回值
float getFloat() {
    return 5;    ✅ int → float（隐式转换）
    return "hello";  ❌ string → float（类型不兼容）
}

错误 4：未定义的变量
print(undefinedVar);  ❌ 未定义的变量（符号解析错误）

错误 5：除零错误（运行时错误，但类型检查可以部分检测）
int z = x / 0;  ❌ 除零错误（运行时错误，但类型检查可以部分检测）
```

**图示说明**：
- 类型检查验证程序中的类型操作是否合法
- 类型错误在编译时被检测，避免运行时错误
- 类型兼容性规则决定了哪些操作是合法的
- 类型检查器会报告详细的错误信息（行号、列号、错误描述）

**相关概念**：
- **类型系统**：定义类型和类型操作规则的系统
- **类型安全**：程序在编译时或运行时不会发生类型错误
- **强类型 vs 弱类型**：强类型语言有严格的类型检查（如 Java），弱类型语言类型检查宽松（如 JavaScript）

---

### 3.2 与仓库 EP 的对应关系

#### 对应 EP：EP16

EP16 实现了完整的多遍编译架构、多作用域符号表和类型推导，是理解现代编译器前端的绝佳案例。

**目录结构**：
```
ep16/
├── src/main/java/org/teachfx/antlr4/ep16/
│   ├── Compiler.java                    // 主程序（演示多遍编译）
│   ├── parser/
│   │   ├── CymbolLexer.java            // ANTLR4 生成的词法分析器
│   │   ├── CymbolParser.java           // ANTLR4 生成的语法分析器
│   │   ├── CymbolBaseVisitor.java      // ANTLR4 基础访问者
│   │   └── CymbolVisitor.java          // ANTLR4 访问者接口
│   ├── visitor/
│   │   ├── CymbolASTVisitor.java       // AST 访问者基类
│   │   ├── LocalDefine.java           // 第2遍：符号定义（标记作用域）
│   │   ├── LocalResolver.java          // 第3遍：类型推导（标记类型）
│   │   └── Interpreter.java            // 解释器（基于类型信息）
│   ├── symtab/
│   │   ├── Symbol.java                // 符号基类
│   │   ├── VariableSymbol.java        // 变量符号
│   │   ├── MethodSymbol.java          // 方法符号
│   │   ├── ScopedSymbol.java          // 带作用域的符号
│   │   ├── BuiltInTypeSymbol.java     // 内置类型符号
│   │   ├── Type.java                  // 类型接口
│   │   ├── TypeTable.java             // 类型表（int、float、bool 等）
│   │   ├── Scope.java                 // 作用域接口
│   │   ├── BaseScope.java             // 作用域基类
│   │   ├── GlobalScope.java           // 全局作用域
│   │   ├── LocalScope.java            // 局部作用域
│   │   └── ReturnValue.java            // 返回值类型
│   └── misc/
│       ├── ScopeUtil.java             // 作用域工具类
│       ├── Util.java                  // 工具类
│       ├── CompilerLogger.java         // 编译器日志（错误报告）
│       ├── MemorySpace.java           // 内存空间（后续章节使用）
│       └── FunctionSpace.java         // 函数内存空间（后续章节使用）
├── src/main/antlr4/
│   └── Cymbol.g4                      // Cymbol 语言语法
└── src/test/java/
    └── (测试文件)
```

**关键类/方法说明**：

---

#### Symbol - 符号基类

**类的作用**：表示程序中的名称（变量、函数、类型等），是所有符号的基类。

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
 *
 * 设计考虑：
 * - 所有符号（变量、函数、类型）都继承此类
 * - type 字段存储符号的类型（如 int、float、void）
 * - scope 字段存储符号所在的作用域
 * - space 字段存储符号的内存空间（后续章节用于代码生成）
 */
public class Symbol {
    static Type UNDEFINED;  // 未定义类型（用于初始化）

    // 符号的类型（如 int、float、void）
    public Type type;

    // 符号所在的作用域
    public Scope scope;

    // 符号的内存空间（后续章节使用，用于代码生成时的内存分配）
    public MemorySpace space;

    // 符号的名称
    String name;

    /**
     * 构造函数 1：只指定名称
     * 类型初始化为 UNDEFINED
     */
    public Symbol(String name) {
        this.name = name;
        this.type = UNDEFINED;
    }

    /**
     * 构造函数 2：指定名称和类型
     * 类型可以为 null，此时初始化为 UNDEFINED
     */
    public Symbol(String name, Type type) {
        this(name);  // 调用构造函数 1
        this.type = type != null ? type : UNDEFINED;
    }

    /**
     * 去除字符串两端的括号
     * 用于处理数组类型（如 "int[]" → "int"）
     */
    public static String stripBrackets(String s) {
        return s.substring(1, s.length() - 1);
    }

    /**
     * 获取符号名称
     */
    public String getName() {
        return name;
    }

    /**
     * toString 方法：格式化输出符号信息
     * 格式：<作用域名称.符号名称:类型>
     * 示例：<global.main:int>、<factorial.n:int>
     */
    @Override
    public String toString() {
        String s = "";
        if (scope != null) s = scope.getScopeName() + ".";  // 添加作用域前缀
        if (type != null) return '<' + s + getName() + ":" + type + '>';
        return s + getName();
    }
}
```

**设计要点**：
- `type` 字段存储符号的类型（如 `int`、`float`、`void`）
- `scope` 字段存储符号所在的作用域，用于调试和错误报告
- `space` 字段存储符号的内存空间（后续章节用于代码生成）
- `toString()` 方法格式化输出符号信息，格式为 `<作用域.名称:类型>`

---

#### Scope - 作用域接口

**接口的作用**：定义作用域的核心操作，所有作用域类都实现此接口。

```java
package org.teachfx.antlr4.ep16.symtab;

/**
 * 作用域接口
 * 定义作用域的核心操作
 *
 * 职责：
 * - 定义作用域的名称
 * - 获取父作用域（enclosing scope）
 * - 定义符号（define）
 * - 解析符号（resolve）
 * - 查找类型（lookup）
 *
 * 设计考虑：
 * - 接口定义了作用域的统一契约
 * - 所有作用域（GlobalScope、LocalScope、MethodSymbol）都实现此接口
 * - resolve 方法用于符号查找（从当前作用域开始，向外查找）
 * - lookup 方法用于类型查找
 */
public interface Scope {
    /**
     * 获取作用域名称
     * @return 作用域名称（如 "global"、"factorial"、"Local"）
     */
    String getScopeName();

    /**
     * 获取父作用域（enclosing scope）
     * @return 父作用域，如果没有则返回 null
     */
    Scope getEnclosingScope();

    /**
     * 在当前作用域中定义符号
     * @param sym 要定义的符号（变量、函数等）
     */
    void define(Symbol sym);

    /**
     * 解析符号（从当前作用域开始，向外查找）
     * 实现作用域链查找：先在当前作用域查找，找不到则到父作用域查找
     * @param name 符号名称
     * @return 找到的符号，未找到则返回 null
     */
    Symbol resolve(String name);

    /**
     * 查找类型
     * @param name 类型名称
     * @return 找到的类型，未找到则返回 null
     */
    Type lookup(String name);
}
```

**设计要点**：
- `define()` 方法在当前作用域中定义符号
- `resolve()` 方法实现作用域链查找，从当前作用域开始，向外查找
- `getEnclosingScope()` 方法获取父作用域，形成作用域链
- `lookup()` 方法用于类型查找（如 `int`、`float`）

---

#### BaseScope - 作用域基类

**类的作用**：作用域的抽象基类，提供通用的作用域实现。

```java
package org.teachfx.antlr4.ep16.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 作用域基类
 * 提供通用的作用域实现
 *
 * 职责：
 * - 存储符号映射（符号名称 → 符号对象）
 * - 实现作用域接口的通用方法
 * - 初始化内置类型（int、float、void）
 *
 * 设计考虑：
 * - 抽象类，不能直接实例化
 * - 使用 LinkedHashMap 存储符号，保持插入顺序（便于调试）
 * - 构造函数初始化内置类型（int、float、void）
 * - resolve 方法实现作用域链查找
 */
public abstract class BaseScope implements Scope {
    // 父作用域
    Scope enclosingScope;

    // 符号映射：符号名称 → 符号对象
    // 使用 LinkedHashMap 保持插入顺序（便于调试）
    Map<String, Symbol> symbols = new LinkedHashMap<>();

    /**
     * 构造函数
     * @param parent 父作用域
     */
    public BaseScope(Scope parent) {
        this.enclosingScope = parent;

        // 初始化内置类型（int、float、void）
        // 这些类型在每个作用域中都可用
        define(TypeTable.INT);
        define(TypeTable.FLOAT);
        define(TypeTable.VOID);
    }

    /**
     * 查找类型（从当前作用域开始，向外查找）
     */
    @Override
    public Type lookup(String name) {
        return (Type) resolve(name);  // resolve 返回 Symbol，强制转换为 Type
    }

    /**
     * 解析符号（从当前作用域开始，向外查找）
     * 实现作用域链查找：
     * 1. 在当前作用域查找
     * 2. 如果未找到，到父作用域查找
     * 3. 递归直到找到或到达全局作用域
     */
    @Override
    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);  // 在当前作用域查找
        if (s != null) return s;       // 找到了，返回
        if (enclosingScope != null) return enclosingScope.resolve(name);  // 到父作用域查找
        return null;  // 未找到
    }

    /**
     * 在当前作用域中定义符号
     */
    @Override
    public void define(Symbol sym) {
        symbols.put(sym.name, sym);  // 添加符号到映射
        sym.scope = this;  // 设置符号的作用域
    }

    /**
     * 获取父作用域
     */
    @Override
    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    /**
     * toString 方法：格式化输出作用域信息
     */
    @Override
    public String toString() {
        return getScopeName() + symbols.keySet();
    }
}
```

**设计要点**：
- 使用 `LinkedHashMap` 存储符号，保持插入顺序（便于调试）
- 构造函数初始化内置类型（`int`、`float`、`void`）
- `resolve()` 方法实现作用域链查找，递归到父作用域
- `define()` 方法设置符号的作用域字段

---

#### GlobalScope - 全局作用域

**类的作用**：表示程序的全局作用域，包含所有全局符号（函数、全局变量）。

```java
package org.teachfx.antlr4.ep16.symtab;

/**
 * 全局作用域
 * 表示程序的全局作用域，包含所有全局符号
 *
 * 职责：
 * - 存储全局函数
 * - 存储全局变量
 * - 初始化内置类型系统（int、float、char、bool、void）
 *
 * 设计考虑：
 * - 全局作用域没有父作用域（enclosingScope 为 null）
 * - 构造函数初始化内置类型系统
 * - 作用域名称为 "global"
 */
public class GlobalScope extends BaseScope {

    /**
     * 构造函数
     * 父作用域为 null（全局作用域是根作用域）
     */
    public GlobalScope() {
        super(null);  // 父作用域为 null
    }

    /**
     * 获取作用域名称
     */
    @Override
    public String getScopeName() {
        return "gloabl";  // 注意：这里有拼写错误，应该是 "global"
    }
}
```

**设计要点**：
- 全局作用域是根作用域，没有父作用域
- 作用域名称为 "global"
- BaseScope 的构造函数会初始化内置类型

---

#### LocalScope - 局部作用域

**类的作用**：表示函数、代码块等局部作用域，包含局部变量。

```java
package org.teachfx.antlr4.ep16.symtab;

/**
 * 局部作用域
 * 表示函数、代码块等局部作用域
 *
 * 职责：
 * - 存储函数的局部变量
 * - 存储函数的参数
 * - 存储代码块的局部变量
 *
 * 设计考虑：
 * - 局部作用域有父作用域（函数作用域、全局作用域等）
 * - 作用域名称为 "Local"
 * - 继承 BaseScope，自动初始化内置类型
 */
public class LocalScope extends BaseScope {

    /**
     * 构造函数
     * @param parent 父作用域
     */
    public LocalScope(Scope parent) {
        super(parent);  // 调用父类构造函数，初始化内置类型
    }

    /**
     * 获取作用域名称
     */
    @Override
    public String getScopeName() {
        return "Local";
    }
}
```

**设计要点**：
- 局部作用域有父作用域（函数作用域、全局作用域等）
- 作用域名称为 "Local"
- 继承 BaseScope，自动初始化内置类型

---

#### ScopedSymbol - 带作用域的符号

**类的作用**：表示函数、方法等可以包含其他符号的符号（如函数的参数、局部变量）。

```java
package org.teachfx.antlr4.ep16.symtab;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;

/**
 * 带作用域的符号
 * 表示函数、方法等可以包含其他符号的符号
 *
 * 职责：
 * - 存储成员符号（函数参数、局部变量）
 * - 实现作用域接口（继承 Symbol，实现 Scope）
 * - 存储关联的 ParseTree 节点
 *
 * 设计考虑：
 * - 抽象类，不能直接实例化
 * - MethodSymbol 继承此类，表示函数符号
 * - 实现 Scope 接口，可以包含其他符号（参数、局部变量）
 * - tree 字段存储关联的 ParseTree 节点，用于错误报告
 */
public abstract class ScopedSymbol extends Symbol implements Scope {
    // 关联的 ParseTree 节点（用于错误报告）
    public ParserRuleContext tree;

    // 父作用域
    Scope enclosingScope;

    /**
     * 构造函数 1：指定名称、类型、父作用域
     * 用于有返回类型的函数（如 int add(int a, int b)）
     */
    public ScopedSymbol(String name, Type type, Scope enclosingScope) {
        super(name, type);  // 调用父类构造函数
        this.enclosingScope = enclosingScope;
    }

    /**
     * 构造函数 2：指定名称、父作用域、ParseTree 节点
     * 用于无返回类型的函数（如 void print(int value)）
     */
    public ScopedSymbol(String name, Scope enclosingScope, ParserRuleContext tree) {
        super(name);  // 调用父类构造函数
        this.enclosingScope = enclosingScope;
        this.tree = tree;
    }

    /**
     * 查找类型（从当前作用域开始，向外查找）
     */
    @Override
    public Type lookup(String name) {
        return (Type) resolve(name);  // resolve 返回 Symbol，强制转换为 Type
    }

    /**
     * 解析符号（从当前作用域开始，向外查找）
     */
    @Override
    public Symbol resolve(String name) {
        Symbol s = getMemebers().get(name);  // 在当前作用域查找
        if (s != null) return s;             // 找到了，返回
        if (getEnclosingScope() != null) {
            return getEnclosingScope().resolve(name);  // 到父作用域查找
        }
        return null;  // 未找到
    }

    /**
     * 解析类型（从当前作用域开始，向外查找）
     */
    public Symbol resolveType(String name) {
        return resolve(name);
    }

    /**
     * 获取父作用域
     */
    @Override
    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    /**
     * 获取作用域名称
     */
    @Override
    public String getScopeName() {
        return name;  // 使用符号的名称作为作用域名称
    }

    /**
     * 在当前作用域中定义符号
     */
    @Override
    public void define(Symbol sym) {
        getMemebers().put(sym.name, sym);  // 添加符号到映射
        sym.scope = this;                  // 设置符号的作用域
    }

    /**
     * 获取成员符号（抽象方法，由子类实现）
     * @return 成员符号映射
     */
    public abstract Map<String, Symbol> getMemebers();
}
```

**设计要点**：
- 继承 `Symbol`，实现 `Scope` 接口
- 既是符号（继承 Symbol），又是作用域（实现 Scope）
- `getMemebers()` 是抽象方法，由子类实现（如 MethodSymbol）
- `tree` 字段存储关联的 ParseTree 节点，用于错误报告

---

#### MethodSymbol - 方法符号

**类的作用**：表示程序中的函数或方法，包含函数的参数和返回值。

```java
package org.teachfx.antlr4.ep16.symtab;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 方法符号
 * 表示程序中的函数或方法
 *
 * 职责：
 * - 存储函数参数
 * - 存储函数返回值类型
 * - 存储函数体（blockStmt）
 * - 存储函数调用点（callee）
 *
 * 设计考虑：
 * - 继承 ScopedSymbol，既是符号又是作用域
 * - 使用 LinkedHashMap 存储参数，保持插入顺序
 * - builtin 标记是否为内置函数（如 print）
 */
public class MethodSymbol extends ScopedSymbol {
    // 函数体（ParseTree 节点）
    public ParserRuleContext blockStmt;

    // 标记是否为内置函数（如 print）
    public boolean builtin = false;

    // 函数调用点（ParseTree 节点，用于错误报告）
    public ParserRuleContext callee = null;

    // 函数参数（有序映射，保持参数顺序）
    Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();

    /**
     * 构造函数 1：语言定义的函数（有返回类型）
     * 示例：int add(int a, int b)
     * @param name 函数名称
     * @param retType 返回值类型
     * @param parent 父作用域
     * @param tree 关联的 ParseTree 节点
     */
    // Language func
    public MethodSymbol(String name, Type retType, Scope parent,
                        ParserRuleContext tree) {
        super(name, retType, parent);
    }

    /**
     * 构造函数 2：内置函数（无返回类型）
     * 示例：void print(int value)
     * @param name 函数名称
     * @param parent 父作用域
     * @param tree 关联的 ParseTree 节点
     */
    // Native func
    public MethodSymbol(String name, Scope parent,
                        ParserRuleContext tree) {
        super(name, parent, tree);
    }

    /**
     * 获取成员符号（参数映射）
     */
    @Override
    public Map<String, Symbol> getMemebers() {
        return orderedArgs;  // 返回参数映射
    }
}
```

**设计要点**：
- 继承 `ScopedSymbol`，既是符号又是作用域
- `orderedArgs` 存储函数参数，使用 `LinkedHashMap` 保持插入顺序
- `builtin` 标记是否为内置函数（如 `print`）
- `blockStmt` 存储函数体（ParseTree 节点）
- `callee` 存储函数调用点（ParseTree 节点，用于错误报告）

---

#### VariableSymbol - 变量符号

**类的作用**：表示程序中的变量。

```java
package org.teachfx.antlr4.ep16.symtab;

/**
 * 变量符号
 * 表示程序中的变量
 *
 * 职责：
 * - 存储变量名称
 * - 存储变量类型
 *
 * 设计考虑：
 * - 继承 Symbol，继承 name、type、scope 字段
 * - 不添加额外字段，保持简洁
 */
public class VariableSymbol extends Symbol {

    /**
     * 构造函数 1：只指定名称
     * 类型初始化为 UNDEFINED
     */
    public VariableSymbol(String name) {
        super(name);
    }

    /**
     * 构造函数 2：指定名称和类型
     */
    public VariableSymbol(String name, Type type) {
        super(name, type);
    }
}
```

**设计要点**：
- 继承 `Symbol`，继承 `name`、`type`、`scope` 字段
- 不添加额外字段，保持简洁

---

#### Type - 类型接口

**接口的作用**：定义类型的核心操作，所有类型都实现此接口。

```java
package org.teachfx.antlr4.ep16.symtab;

/**
 * 类型接口
 * 定义类型的核心操作
 *
 * 职责：
 * - 获取类型名称
 * - 判断是否为基本类型
 */
public interface Type {
    /**
     * 获取类型名称
     * @return 类型名称（如 "int"、"float"、"bool"）
     */
    String getName();

    /**
     * 判断是否为基本类型
     * @return 如果是基本类型返回 true，否则返回 false
     */
    boolean isPrimitive();
}
```

**设计要点**：
- 定义类型的统一契约
- 所有类型（`BuiltInTypeSymbol`、后续的自定义类型）都实现此接口

---

#### BuiltInTypeSymbol - 内置类型符号

**类的作用**：表示语言的内置类型（如 `int`、`float`、`bool`）。

```java
package org.teachfx.antlr4.ep16.symtab;

/**
 * 内置类型符号
 * 表示语言的内置类型（如 int、float、bool）
 *
 * 职责：
 * - 存储类型名称
 * - 标记为基本类型
 *
 * 设计考虑：
 * - 继承 Symbol，实现 Type 接口
 * - isPrimitive() 返回 true（内置类型都是基本类型）
 * - 不添加额外字段，保持简洁
 */
public class BuiltInTypeSymbol extends Symbol implements Type {

    /**
     * 构造函数
     * @param name 类型名称
     */
    public BuiltInTypeSymbol(String name) {
        super(name);
    }

    /**
     * 获取类型名称
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 判断是否为基本类型
     * 内置类型都是基本类型，返回 true
     */
    @Override
    public boolean isPrimitive() {
        return true;
    }
}
```

**设计要点**：
- 继承 `Symbol`，实现 `Type` 接口
- `isPrimitive()` 返回 `true`（内置类型都是基本类型）

---

#### TypeTable - 类型表

**类的作用**：定义所有内置类型，提供全局访问。

```java
package org.teachfx.antlr4.ep16.symtab;

/**
 * 类型表
 * 定义所有内置类型，提供全局访问
 *
 * 职责：
 * - 定义内置类型（int、float、double、char、bool、void、null、object）
 * - 定义布尔值常量（true、false）
 *
 * 设计考虑：
 * - 使用静态常量定义内置类型
 * - 所有内置类型全局唯一
 * - 便于类型检查和类型推导
 */
public class TypeTable {
    // 定义内置类型
    public static BuiltInTypeSymbol INT = new BuiltInTypeSymbol("int");
    public static BuiltInTypeSymbol FLOAT = new BuiltInTypeSymbol("float");
    public static BuiltInTypeSymbol DOUBLE = new BuiltInTypeSymbol("double");
    public static BuiltInTypeSymbol CHAR = new BuiltInTypeSymbol("char");
    public static BuiltInTypeSymbol VOID = new BuiltInTypeSymbol("void");
    public static BuiltInTypeSymbol NULL = new BuiltInTypeSymbol("null");
    public static BuiltInTypeSymbol BOOLEAN = new BuiltInTypeSymbol("bool");
    public static BuiltInTypeSymbol OBJECT = new BuiltInTypeSymbol("object");

    // 定义布尔值常量（true = 1, false = 0）
    public static Integer TRUE = 1;
    public static Integer FALSE = 0;
}
```

**设计要点**：
- 使用静态常量定义内置类型
- 所有内置类型全局唯一
- 定义布尔值常量（`true = 1`，`false = 0`）

---

#### ScopeUtil - 作用域工具类

**类的作用**：封装作用域操作，提供便捷的符号和类型查找方法。

```java
package org.teachfx.antlr4.ep16.misc;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep16.symtab.Scope;
import org.teachfx.antlr4.ep16.symtab.Symbol;
import org.teachfx.antlr4.ep16.symtab.Type;

/**
 * 作用域工具类
 * 封装作用域操作，提供便捷的符号和类型查找方法
 *
 * 职责：
 * - 封装 ParseTreeProperty<Scope>，提供便捷的作用域访问
 * - 提供符号查找方法（resolve）
 * - 提供类型查找方法（lookup）
 *
 * 设计考虑：
 * - 持有 ParseTreeProperty<Scope> 引用，访问每个节点的作用域
 * - 提供 lookup() 和 resolve() 方法，简化符号和类型查找
 * - 查找失败时，使用 CompilerLogger 报告错误
 */
public class ScopeUtil {

    // ParseTree 节点 → 作用域映射（由 LocalDefine 填充）
    private final ParseTreeProperty<Scope> scopes;

    /**
     * 构造函数
     * @param scopes ParseTree 节点 → 作用域映射（由 LocalDefine 填充）
     */
    public ScopeUtil(ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes;
    }

    /**
     * 查找类型
     * 根据上下文节点查找类型
     * @param ctx ParseTree 节点
     * @return 找到的类型，未找到则报告错误并返回 null
     */
    public Type lookup(ParserRuleContext ctx) {
        String name = Util.name(ctx);  // 提取符号名称
        System.out.println("lookup type is : " + name);
        Scope scope = get(ctx);  // 获取节点所在作用域
        System.out.println("scope is : " + scope);
        Type type = scope.lookup(name);  // 在作用域中查找类型
        if (type == null) {
            String msg = "unknown type: " + name;
            CompilerLogger.error(ctx, msg);  // 报告错误
        }
        return type;
    }

    /**
     * 解析符号
     * 根据上下文节点查找符号（从节点所在作用域开始）
     * @param ctx ParseTree 节点
     * @return 找到的符号，未找到则报告错误并返回 null
     */
    public Symbol resolve(ParserRuleContext ctx) {
        String name = Util.name(ctx);  // 提取符号名称
        System.out.println("lookup func is : " + name);

        Scope scope = get(ctx);  // 获取节点所在作用域
        System.out.println("func scope is : " + scope);

        Symbol symbol = scope.resolve(name);  // 在作用域中查找符号
        if (symbol == null) {
            String msg = "unknown symbol: " + name;
            CompilerLogger.error(ctx, msg);  // 报告错误
        }
        return symbol;
    }

    /**
     * 获取节点所在作用域
     * @param ctx ParseTree 节点
     * @return 节点所在作用域
     */
    public Scope get(ParserRuleContext ctx) {
        return scopes.get(ctx);
    }
}
```

**设计要点**：
- 持有 `ParseTreeProperty<Scope>` 引用，访问每个节点的作用域
- `lookup()` 方法查找类型，查找失败时报告错误
- `resolve()` 方法查找符号，查找失败时报告错误
- `get()` 方法获取节点所在作用域

---

#### LocalDefine - 符号定义（第2遍）

**类的作用**：第2遍编译，遍历 ParseTree，为每个节点标记作用域，定义符号。

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
 *
 * 设计考虑：
 * - 继承 CymbolASTVisitor<Object>，遍历 ParseTree
 * - 使用 ParseTreeProperty<Scope> 存储每个节点的作用域
 * - 使用 currentScope 跟踪当前作用域
 * - pushScope() 和 popScope() 方法用于进入和退出作用域
 * - stashScope() 方法将当前作用域标记到节点
 *
 * @author Arthur.Bltiz
 * @description 变量消解-标记每个ast节点的作用域归属问题
 * @purpose 解决变量的定位问题--属于哪个作用域
 */
public class LocalDefine extends CymbolASTVisitor<Object> {
    // 当前作用域
    private Scope currentScope = null;

    // ParseTree 节点 → 作用域映射
    private final ParseTreeProperty<Scope> scopes;

    /**
     * 构造函数
     * 初始化全局作用域，添加内置函数 print
     */
    public LocalDefine() {
        // 创建全局作用域
        BaseScope globalScope = new GlobalScope();
        currentScope = globalScope;

        // 添加内置函数 print
        MethodSymbol printFuncSymbol = new MethodSymbol("print", globalScope, null);
        printFuncSymbol.builtin = true;
        printFuncSymbol.getMemebers().put("value", TypeTable.OBJECT);  // print 函数有一个参数 value
        globalScope.define(printFuncSymbol);

        // 初始化 ParseTreeProperty<Scope>
        scopes = new ParseTreeProperty<>();
    }

    /**
     * 获取 ParseTree 节点 → 作用域映射
     */
    public ParseTreeProperty<Scope> getScopes() {
        return scopes;
    }

    /**
     * 访问编译单元（顶层节点）
     */
    @Override
    public Object visitCompilationUnit(CompilationUnitContext ctx) {
        stashScope(ctx);  // 标记节点的作用域
        super.visitCompilationUnit(ctx);  // 递归访问子节点
        return null;
    }

    /**
     * 访问变量声明
     */
    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        System.out.println(tab + "enter var decl " + ctx.getText());
        stashScope(ctx);  // 标记节点的作用域
        return super.visitVarDecl(ctx);
    }

    /**
     * 访问语句变量声明
     */
    @Override
    public Object visitStatVarDecl(StatVarDeclContext ctx) {
        System.out.println(tab + "enter stat var decl " + ctx.getText());
        return super.visitStatVarDecl(ctx);
    }

    /**
     * 访问函数声明
     * 创建函数作用域，定义函数符号
     */
    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        // 创建函数作用域
        MethodSymbol methodScope = new MethodSymbol(Util.name(ctx), currentScope, ctx);
        methodScope.blockStmt = ctx.blockDef;  // 保存函数体
        methodScope.callee = (ParserRuleContext) ctx.parent;  // 保存函数调用点
        currentScope.define(methodScope);  // 将函数符号添加到父作用域
        stashScope(ctx);  // 标记节点的作用域

        // 进入函数作用域
        pushScope(methodScope);
        super.visitFunctionDecl(ctx);  // 递归访问子节点（函数体）
        popScope();  // 退出函数作用域

        System.out.println("enter scope with " + currentScope.getScopeName());
        return null;
    }

    /**
     * 访问函数调用
     */
    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        super.visitExprFuncCall(ctx);
        stashScope(ctx);  // 标记节点的作用域
        return null;
    }

    /**
     * 访问形式参数
     */
    @Override
    public Object visitFormalParameter(FormalParameterContext ctx) {
        super.visitFormalParameter(ctx);
        System.out.println(tab + "collect param with " + ctx.getText());
        stashScope(ctx);  // 标记节点的作用域
        return null;
    }

    /**
     * 访问代码块
     * 创建局部作用域，进入和退出作用域
     */
    @Override
    public Object visitBlock(BlockContext ctx) {
        System.out.println(tab + "enter block " + ctx.getText());

        // 创建局部作用域
        Scope local = new LocalScope(currentScope);
        stashScope(ctx);  // 标记节点的作用域

        // 进入局部作用域
        pushScope(local);
        super.visitBlock(ctx);  // 递归访问子节点
        popScope();  // 退出局部作用域

        System.out.println(tab + "exit block " + ctx.getText());

        return null;
    }

    /**
     * 访问二元表达式
     */
    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        System.out.println(tab + "enter binary expr " + ctx.getText());
        stashScope(ctx);  // 标记节点的作用域
        return super.visitExprBinary(ctx);
    }

    /**
     * 访问一元表达式
     */
    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        System.out.println(tab + "enter unary expr " + ctx.getText());
        stashScope(ctx);  // 标记节点的作用域
        return super.visitExprUnary(ctx);
    }

    /**
     * 访问类型节点
     */
    @Override
    public Object visitType(TypeContext ctx) {
        System.out.println(tab + "enter type " + ctx.getText());
        stashScope(ctx);  // 标记节点的作用域
        return null;
    }

    /**
     * 访问 float 常量
     */
    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        System.out.println(tab + "enter float constant");
        stashScope(ctx);  // 标记节点的作用域
        return null;
    }

    /**
     * 访问变量引用
     */
    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        System.out.println(tab + "enter id  " + ctx.getText());

        stashScope(ctx);  // 标记节点的作用域
        return null;
    }

    /**
     * 访问 int 常量
     */
    @Override
    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        System.out.println(tab + "enter int constant");

        stashScope(ctx);  // 标记节点的作用域
        return null;
    }

    /**
     * 保存当前作用域到节点
     * @param ctx ParseTree 节点
     */
    public void stashScope(ParserRuleContext ctx) {
        scopes.put(ctx, currentScope);  // 将当前作用域标记到节点
    }

    /**
     * 进入新作用域
     * @param scope 新作用域
     */
    public void pushScope(Scope scope) {
        currentScope = scope;  // 切换当前作用域
    }

    /**
     * 退出当前作用域
     */
    public void popScope() {
        currentScope = currentScope.getEnclosingScope();  // 切换到父作用域
    }
}
```

**设计要点**：
- 继承 `CymbolASTVisitor<Object>`，遍历 ParseTree
- `currentScope` 跟踪当前作用域
- `ParseTreeProperty<Scope>` 存储每个节点的作用域
- `pushScope()` 和 `popScope()` 方法用于进入和退出作用域
- `stashScope()` 方法将当前作用域标记到节点
- 构造函数初始化全局作用域，添加内置函数 `print`

---

#### LocalResolver - 类型推导（第3遍）

**类的作用**：第3遍编译，遍历 ParseTree，查找符号，推导表达式类型。

```java
package org.teachfx.antlr4.ep16.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep16.misc.CompilerLogger;
import org.teachfx.antlr4.ep16.misc.ScopeUtil;
import org.teachfx.antlr4.ep16.misc.Util;
import org.teachfx.antlr4.ep16.parser.CymbolParser;
import org.teachfx.antlr4.ep16.parser.CymbolParser.*;
import org.teachfx.antlr4.ep16.symtab.*;

/**
 * 类型推导（第3遍）
 * 遍历 AST，查找符号，推导表达式类型
 *
 * 职责：
 * - 查找符号（变量、函数）
 * - 推导表达式类型
 * - 为每个 ParseTree 节点标记类型
 *
 * 设计考虑：
 * - 继承 CymbolASTVisitor<Object>，遍历 ParseTree
 * - 使用 ParseTreeProperty<Type> 存储每个节点的类型
 * - 使用 ScopeUtil 封装作用域操作
 * - setType() 方法设置基本类型的节点类型
 * - copyType() 方法复制类型（从子节点到父节点）
 *
 * @description 给变量分配类型
 * @purpose 给变量确定具体类型
 */
public class LocalResolver extends CymbolASTVisitor<Object> {
    // 二元表达式的左操作数索引
    private static final int LEFT = 0;
    // 二元表达式的右操作数索引
    private static final int RIGHT = 1;

    // 数组表达式的索引
    private static final int ARRAY_EXPR = 0;
    // 函数表达式的索引
    private static final int FUNC_EXPR = 0;
    // 结构体的索引
    private static final int STRUCT = 0;
    private static final int MEMBER_PARENT = 2;
    // 成员的索引
    private static final int MEMBER = 0;

    // ParseTree 节点 → 类型映射
    public ParseTreeProperty<Type> types;

    // 作用域工具（封装作用域操作）
    private final ScopeUtil scopes;

    /**
     * 构造函数
     * @param scopes 作用域工具（从 LocalDefine 获取）
     */
    public LocalResolver(ScopeUtil scopes) {
        this.scopes = scopes;
        this.types = new ParseTreeProperty<>();  // 初始化 ParseTreeProperty<Type>
    }


    /**
     * 访问变量声明
     * 定义变量符号，标记变量类型
     */
    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        super.visitVarDecl(ctx);
        System.out.println(ctx.getClass().toString());

        // 查找变量类型（从作用域）
        Type type = scopes.lookup(ctx.type());
        System.out.println(ctx.getText());

        // 创建变量符号
        VariableSymbol var = new VariableSymbol(Util.name(ctx), type);

        if (type == null) {
            CompilerLogger.error(ctx, "Unknown type when declaring variable: " + var);
        }

        // 将变量添加到作用域（LocalDefine 已完成作用域标记，这里只添加符号）
        Scope scope = scopes.get(ctx);
        scope.define(var);

        return null;
    }

    /**
     * 访问形式参数
     * 定义参数符号，标记参数类型
     */
    @Override
    public Object visitFormalParameter(FormalParameterContext ctx) {
        // 查找参数类型（从作用域）
        Type type = scopes.lookup(ctx.type());
        VariableSymbol var = new VariableSymbol(Util.name(ctx), type);
        Scope scope = scopes.get(ctx);
        scope.define(var);  // 将参数添加到函数作用域
        return null;
    }

    /**
     * 访问函数声明
     * 设置函数返回值类型
     */
    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        super.visitFunctionDecl(ctx);
        // 获取函数符号
        Symbol method = this.scopes.resolve(ctx);
        // 获取返回值类型（从语法树）
        String returnType = ctx.type().getStart().getText();
        // 设置函数返回值类型（从作用域查找类型）
        method.type = method.scope.lookup(returnType);

        return null;
    }

    /**
     * 访问函数调用
     * 标记函数调用表达式的类型为函数返回值类型
     */
    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        super.visitExprFuncCall(ctx);
        // 复制函数名表达式的类型（即函数返回值类型）
        copyType(ctx.expr(FUNC_EXPR), ctx);

        return null;
    }

    /**
     * 访问括号表达式
     * 复制内部表达式的类型（括号不改变类型）
     */
    @Override
    public Object visitExprGroup(ExprGroupContext ctx) {
        super.visitExprGroup(ctx);
        copyType(ctx.expr(), ctx);  // 复制内部表达式的类型
        return null;
    }

    /**
     * 访问二元表达式
     * 标记二元表达式的类型
     */
    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        super.visitExprBinary(ctx);
        System.out.println(tab + "binary operation : " + ctx.getText());
        System.out.println(tab + "operator " + ctx.o.getText());
        System.out.println(tab + "left operand is " + ctx.expr(LEFT).getText() + " right operand is " + ctx.expr(RIGHT).getText());
        copyType(ctx.expr(LEFT), ctx);  // 复制左操作数的类型（简化版）

        return null;
    }

    /**
     * 访问一元表达式
     * 复制操作数的类型
     */
    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        super.visitExprUnary(ctx);
        copyType(ctx.expr(), ctx);  // 复制操作数的类型
        return null;
    }

    /**
     * 访问基本表达式
     * 复制基本节点的类型
     */
    @Override
    public Object visitExprPrimary(ExprPrimaryContext ctx) {
        super.visitExprPrimary(ctx);
        copyType(ctx.primary(), ctx);  // 复制基本节点的类型
        return null;
    }

    /**
     * 访问 bool 常量
     */
    @Override
    public Object visitPrimaryBOOL(PrimaryBOOLContext ctx) {
        setType(ctx);  // 设置类型为 bool
        return null;
    }

    /**
     * 访问 char 常量
     */
    @Override
    public Object visitPrimaryCHAR(PrimaryCHARContext ctx) {
        setType(ctx);  // 设置类型为 char
        return null;
    }

    /**
     * 访问变量引用
     */
    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        setType(ctx);  // 设置类型为变量类型
        return null;
    }

    /**
     * 访问 int 常量
     */
    @Override
    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        setType(ctx);  // 设置类型为 int
        return null;
    }

    /**
     * 访问 float 常量
     */
    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        setType(ctx);  // 设置类型为 float
        return null;
    }

    /**
     * 访问 string 常量
     */
    @Override
    public Object visitPrimarySTRING(PrimarySTRINGContext ctx) {
        setType(ctx);  // 设置类型为 string
        return null;
    }

    /**
     * 设置节点的类型
     * 根据节点类型设置对应的类型（int、float、bool、char、变量等）
     */
    private void setType(ParserRuleContext ctx) {
        // 如果已经有类型，不覆盖（避免重复设置）
        if (types.get(ctx) != null) {
            return;
        }

        int tokenValue = ctx.start.getType();
        String tokenName = ctx.start.getText();

        if (tokenValue == CymbolParser.ID) {
            // 变量引用：从作用域查找变量类型
            Scope scope = scopes.get(ctx);
            Symbol s = scope.resolve(tokenName);

            if (s == null) {
                CompilerLogger.error(ctx, "Unknown type for id: " + tokenName);
            } else {
                stashType(ctx, s.type);  // 设置类型为变量类型
            }

        } else if (tokenValue == CymbolParser.INT ||
                tokenName.equals("int")) {
            stashType(ctx, TypeTable.INT);  // int 常量
        } else if (tokenValue == CymbolParser.FLOAT ||
                tokenName.equals("float")) {
            stashType(ctx, TypeTable.FLOAT);  // float 常量
        } else if (tokenValue == CymbolParser.CHAR ||
                tokenName.equals("char")) {
            stashType(ctx, TypeTable.CHAR);  // char 常量
        } else if (tokenName.equals("true") ||
                tokenName.equals("false") ||
                tokenName.equals("bool")) {
            stashType(ctx, TypeTable.BOOLEAN);  // bool 常量
        } else if (tokenName.equals("void")) {
            stashType(ctx, TypeTable.VOID);  // void 类型
        } else if (tokenName.equals("null")) {
            stashType(ctx, TypeTable.NULL);  // null 类型
        }
    }

    /**
     * 保存类型到节点
     */
    private void stashType(ParserRuleContext ctx, Type type) {
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

**设计要点**：
- 继承 `CymbolASTVisitor<Object>`，遍历 ParseTree
- `ParseTreeProperty<Type>` 存储每个节点的类型
- `ScopeUtil` 封装作用域操作
- `setType()` 方法设置基本类型的节点类型
- `copyType()` 方法复制类型（从子节点到父节点）
- 在 `visitVarDecl()` 中定义变量符号
- 在 `visitFormalParameter()` 中定义参数符号

---

#### Compiler.java - 主程序

**类的作用**：演示完整的多遍编译流水线。

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
        // 准备输入源
        String fileName = null;
        fileName = "src/main/resources/t.cymbol";
        if (args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if (fileName != null) is = new FileInputStream(fileName);

        // 词法分析：字符流 → TokenStream
        CharStream charStream = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);

        // 语法分析（第1遍）：TokenStream → ParseTree
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();

        // 符号定义（第2遍）：ParseTree → 作用域标记
        LocalDefine localDefine = new LocalDefine();
        parseTree.accept(localDefine);

        // 创建作用域工具（封装作用域信息）
        ScopeUtil scopeUtil = new ScopeUtil(localDefine.getScopes());

        // 类型推导（第3遍）：ParseTree → 类型标记
        LocalResolver localResolver = new LocalResolver(scopeUtil);
        parseTree.accept(localResolver);

        // 执行（基于作用域和类型信息）
        Interpreter interpreter = new Interpreter(scopeUtil);
        interpreter.interpret(parseTree);
    }
}
```

**设计要点**：
- 演示完整的多遍编译流水线
- 第1遍：ANTLR4 Parser 构建 ParseTree
- 第2遍：LocalDefine 标记作用域
- 第3遍：LocalResolver 推导类型
- 第4遍：Interpreter 执行程序

---

### 3.3 实战流程

#### 步骤 1：编译 EP16 项目

进入 EP16 目录，编译项目（包括生成 ANTLR4 代码）。

**操作**：
```bash
# 进入 EP16 目录
cd ep16

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

创建测试输入文件，包含函数定义、变量声明、表达式求值。

**操作**：
```bash
# 创建测试输入文件
cat > src/main/resources/test_multi_pass.cymbol << 'EOF'
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

**文件内容说明**：
- 第 1-6 行：定义 `factorial` 函数（递归函数）
- 第 8-11 行：定义 `main` 函数
- 第 9 行：声明变量 `result`，调用 `factorial(5)`
- 第 10 行：打印结果

---

#### 步骤 3：运行主程序

运行 Compiler 主程序，处理测试输入文件。

**操作**：
```bash
# 运行主程序
cd ep16
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep16.Compiler" \
    -Dexec.args="src/main/resources/test_multi_pass.cymbol"
```

**预期输出**：
```
(包含大量的调试输出，最后打印结果)
120
```

**验证方法**：

检查输出是否包含 `120`（factorial(5) = 120）

**调试输出说明**：

LocalDefine 和 LocalResolver 中有大量的 `System.out.println()` 调试输出，包括：
- "enter var decl"、"enter block"、"enter binary expr" 等
- "binary operation"、"operator"、"left operand"、"right operand" 等
- "lookup type is"、"scope is"、"lookup func is"、"func scope is" 等

这些调试输出展示了多遍编译的过程：
- LocalDefine 遍历 ParseTree，标记作用域
- LocalResolver 遍历 ParseTree，查找符号，推导类型

---

#### 步骤 4：添加调试输出（可选）

在 LocalDefine 中添加更详细的作用域打印。

**操作**：
```bash
# 在 LocalDefine.java 的 stashScope 方法中添加调试输出
# 修改 ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalDefine.java

# 在 stashScope 方法中添加：
public void stashScope(ParserRuleContext ctx) {
    scopes.put(ctx, currentScope);
    System.out.println("Stash scope for " + ctx.getClass().getSimpleName() +
                       " -> " + currentScope.getScopeName());
}

# 重新编译运行
mvn clean compile
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep16.Compiler" \
    -Dexec.args="src/main/resources/test_multi_pass.cymbol"
```

**预期输出**：
```
(包含更多的调试输出)
Stash scope for CompilationUnitContext -> gloabl
Stash scope for FunctionDeclContext -> factorial
Stash scope for VarDeclContext -> factorial
Stash scope for BlockContext -> Local
...
120
```

---

#### 步骤 5：测试嵌套作用域

创建测试输入文件，测试嵌套作用域和变量遮蔽。

**操作**：
```bash
# 创建测试输入文件
cat > src/main/resources/test_nested_scope.cymbol << 'EOF'
int x = 10;

void main() {
    int x = 5;
    {
        int y = 3;
        print(x + y);  // 应该打印 8，使用 main.x (5) + y (3)
    }
    print(x);  // 应该打印 5，使用 main.x
}
EOF

# 运行主程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep16.Compiler" \
    -Dexec.args="src/main/resources/test_nested_scope.cymbol"
```

**预期输出**：
```
8
5
```

**验证方法**：
- 第一行输出 `8`（main.x = 5 + y = 3）
- 第二行输出 `5`（main.x = 5）

---

#### 步骤 6：测试前向引用

创建测试输入文件，测试函数的前向引用（函数调用在函数定义之前）。

**操作**：
```bash
# 创建测试输入文件
cat > src/main/resources/test_forward_reference.cymbol << 'EOF'
void main() {
    int result = add(3, 5);
    print(result);
}

int add(int a, int b) {
    return a + b;
}
EOF

# 运行主程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep16.Compiler" \
    -Dexec.args="src/main/resources/test_forward_reference.cymbol"
```

**预期输出**：
```
8
```

**验证方法**：
- 输出 `8`（add(3, 5) = 8）
- 说明前向引用成功（main 函数中调用 add，add 函数定义在后）

---

#### 故障排查提示

| 问题 | 可能原因 | 解决方法 |
|------|---------|---------|
| 编译错误 "cannot find symbol: class ScopeUtil" | ScopeUtil 类未正确导入或未创建 | 检查 `ep16/src/main/java/org/teachfx/antlr4/ep16/misc/ScopeUtil.java` 是否存在 |
| 运行时异常 "NullPointerException" | 某个节点的类型或作用域未正确标记 | 在 LocalDefine 和 LocalResolver 中添加调试输出，确认每个 visit 方法都调用了 stashScope 或 stashType |
| 类型推导错误 | 类型推导逻辑不完整或不正确 | 添加调试输出，打印每个节点的类型，检查 inferBinaryExprType 方法的实现 |
| 作用域查找失败 | 作用域链未正确构建 | 检查 LocalDefine 的 pushScope 和 popScope 方法，确保作用域链正确 |
| 函数前向引用失败 | LocalDefine 未正确处理函数声明 | 确保函数符号在函数声明时添加到父作用域 |

---

#### 进阶技巧

1. **可视化作用域链**：
   - 在 LocalDefine 中添加打印方法，以树形结构显示作用域链
   - 在 stashScope 方法中记录作用域层次

2. **可视化类型标记**：
   - 在 LocalResolver 中添加打印方法，显示每个表达式的类型
   - 在 stashType 方法中记录类型信息

3. **添加更多类型检查规则**：
   - 检查函数参数类型
   - 检查返回值类型
   - 检查赋值语句类型

---

## 4. AI 协作线：Context Engineering 视角

### 4.1 上下文设计

为了让 AI 帮忙完成本章任务，我们需要精心设计上下文。

#### 上下文文件列表

**源码文件**（按阅读顺序）：

1. **`ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/Symbol.java`**
   - 作用：符号基类，定义所有符号的共同接口
   - 关键字段：`name`、`type`、`scope`、`space`
   - 关键方法：`getName()`、`toString()`

2. **`ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/Scope.java`**
   - 作用：作用域接口，定义作用域的核心操作
   - 关键方法：`define()`、`resolve()`、`lookup()`、`getEnclosingScope()`

3. **`ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/BaseScope.java`**
   - 作用：作用域基类，提供通用的作用域实现
   - 关键方法：`resolve()`（作用域链查找）、`define()`

4. **`ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/LocalScope.java`**
   - 作用：局部作用域，表示函数、代码块的作用域
   - 继承 BaseScope，父作用域为函数作用域

5. **`ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/GlobalScope.java`**
   - 作用：全局作用域，表示程序的全局作用域
   - 作用域名称为 "global"

6. **`ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/ScopedSymbol.java`**
   - 作用：带作用域的符号，表示函数、方法等可以包含其他符号的符号
   - 继承 Symbol，实现 Scope 接口

7. **`ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/MethodSymbol.java`**
   - 作用：方法符号，表示程序中的函数或方法
   - 存储函数参数、返回值类型、函数体

8. **`ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/VariableSymbol.java`**
   - 作用：变量符号，表示程序中的变量
   - 继承 Symbol，不添加额外字段

9. **`ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/Type.java`**
   - 作用：类型接口，定义类型的核心操作
   - 关键方法：`getName()`、`isPrimitive()`

10. **`ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/TypeTable.java`**
    - 作用：类型表，定义所有内置类型
    - 内置类型：`int`、`float`、`double`、`char`、`bool`、`void`、`null`、`object`

11. **`ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalDefine.java`**
    - 作用：符号定义（第2遍），为每个节点标记作用域
    - 关键方法：`stashScope()`、`pushScope()`、`popScope()`

12. **`ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java`**
    - 作用：类型推导（第3遍），为每个节点标记类型
    - 关键方法：`stashType()`、`copyType()`、`setType()`

13. **`ep16/src/main/java/org/teachfx/antlr4/ep16/misc/ScopeUtil.java`**
    - 作用：作用域工具类，封装作用域信息
    - 关键方法：`lookup()`、`resolve()`、`get()`

14. **`ep16/src/main/java/org/teachfx/antlr4/ep16/Compiler.java`**
    - 作用：主程序，演示多遍编译流水线
    - 关键流程：第1遍 → 第2遍 → 第3遍 → 执行

15. **`ep16/src/main/java/org/teachfx/antlr4/ep16/misc/CompilerLogger.java`**
    - 作用：编译器日志，用于错误报告
    - 关键方法：`error()`（打印错误信息）

**文档文件**：

1. **`ep16/README.md`**（如果有）
   - 作用：EP16 模块概述和使用说明
   - 关键章节：多遍编译架构、符号表设计、类型系统

2. **`AGENTS.md`**
   - 作用：代码规范和最佳实践
   - 相关部分：多遍编译模式、访问者模式、符号表设计模式

**测试文件**：

1. **`ep16/src/test/java/org/teachfx/antlr4/ep16/`**（如果有）
   - 作用：测试多遍编译功能
   - 关键测试方法：`testSymbolDefinition`、`testTypeInference`

**示例输入/输出**：

1. **`ep16/src/main/resources/t.cymbol`**
   - 作用：测试输入 Cymbol 程序
   - 内容：函数定义、变量声明、表达式求值

#### 上下文组织说明

这些文件按照"符号表 → 作用域 → 访问者 → 主程序"组织：

1. **符号表核心在前**：先提供 Symbol、Type、VariableSymbol、MethodSymbol 实现，让 AI 理解符号系统
2. **作用域设计在后**：接着提供 Scope、BaseScope、LocalScope、GlobalScope、ScopedSymbol 实现，让 AI 理解作用域设计
3. **访问者在后**：然后提供 LocalDefine、LocalResolver 实现，展示如何遍历 AST 标记作用域和类型
4. **工具类作为桥梁**：提供 ScopeUtil、CompilerLogger，封装作用域操作和错误报告
5. **主程序作为整合**：最后提供 Compiler.java，展示完整的多遍编译流程

**为什么这样组织**：

- AI 可以先理解符号和作用域的数据结构设计，再理解如何在遍历 AST 时使用作用域
- 完整的上下文确保 AI 理解多遍编译的设计模式和实现方式
- 工具类帮助 AI 理解如何在多个访问者之间传递作用域信息
- 主程序展示了完整的编译器流水线，帮助 AI 理解各个阶段之间的关系

---

### 4.2 Prompt 模板（给 AI 用）

#### 类型 A：添加函数参数类型检查 Prompt 模板

**适用场景**：在 LocalResolver 中添加函数参数类型检查，确保实际参数类型与形式参数类型兼容。

**Prompt 模板**：
```
任务：在 LocalResolver 中添加函数参数类型检查

背景：
我正在学习编译器实现，使用 Java 21 + ANTLR4 4.13.2。
项目是一个渐进式学习的编译器项目，每个 EP 聚焦 1-2 个核心概念。
当前在 EP16，已经实现了多遍编译架构、符号定义和类型推导。

任务目标：
在 LocalResolver 中添加函数参数类型检查，确保实际参数类型与形式参数类型兼容。

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

参考文件：

源码：
- ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java
- ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/MethodSymbol.java
- ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/Type.java
- ep16/src/main/java/org/teachfx/antlr4/ep16/misc/CompilerLogger.java

文档：
- AGENTS.md（代码规范）
- ep16/src/main/antlr4/Cymbol.g4（语法文件）

约束条件：
- 不修改函数调用的解析逻辑
- 保持与现有类型推导的兼容性
- 错误信息清晰，指出类型不匹配的参数

期望输出：
1. 修改后的 LocalResolver.java（标注新增部分）
2. 类型兼容性规则的实现逻辑
3. 测试示例：类型正确和类型错误的函数调用及预期输出

验证方法：
1. 运行 mvn clean compile，确保编译成功
2. 运行示例程序，验证函数参数类型检查
3. 创建类型错误的测试用例，验证错误报告

设计要求：
- 使用 ScopeUtil.resolve() 获取函数符号
- 遍历实际参数列表和形式参数映射
- 使用 CompilerLogger.error() 报告类型错误
- 错误信息包含函数名、参数索引、期望类型、实际类型
```

**Prompt 设计说明**：
- 明确任务目标：添加函数参数类型检查
- 列出具体要求（2 项）
- 提供参考文件列表（源码、文档）
- 说明约束条件（3 条）
- 说明期望输出（3 项）
- 说明验证方法（3 步）
- 提供设计要求（4 项）

---

#### 类型 B：添加 return 语句类型检查 Prompt 模板

**适用场景**：在 LocalResolver 中添加 return 语句类型检查，确保返回值类型与函数声明类型兼容。

**Prompt 模板**：
```
任务：在 LocalResolver 中添加 return 语句类型检查

背景：
我正在学习编译器实现，使用 Java 21 + ANTLR4 4.13.2。
项目是一个渐进式学习的编译器项目，每个 EP 聚焦 1-2 个核心概念。
当前在 EP16，已经实现了多遍编译架构、符号定义和类型推导。

任务目标：
在 LocalResolver 中添加 return 语句类型检查，确保返回值类型与函数声明类型兼容。

具体要求：
1. 修改 `ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java`
   - 添加 `visitReturnStmt` 方法（如果不存在）
   - 获取返回值类型（从表达式的类型标记）
   - 获取函数声明类型（从函数符号）
   - 检查类型是否兼容
   - 如果不兼容，使用 `CompilerLogger.error()` 报告错误

2. 定义返回值类型兼容性规则：
   - void 函数不能返回值
   - 非 void 函数必须返回值
   - 返回值类型可以隐式转换为函数声明类型

参考文件：

源码：
- ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java
- ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/MethodSymbol.java
- ep16/src/main/java/org/teachfx/antlr4/ep16/symtab/Type.java
- ep16/src/main/java/org/teachfx/antlr4/ep16/misc/CompilerLogger.java

文档：
- AGENTS.md（代码规范）
- ep16/src/main/antlr4/Cymbol.g4（语法文件）

约束条件：
- 不修改 return 语句的解析逻辑
- 保持与现有类型推导的兼容性
- 错误信息清晰，指出返回值类型不匹配

期望输出：
1. 修改后的 LocalResolver.java（标注新增部分）
2. 返回值类型兼容性规则的实现逻辑
3. 测试示例：类型正确和类型错误的 return 语句及预期输出

验证方法：
1. 运行 mvn clean compile，确保编译成功
2. 运行示例程序，验证 return 语句类型检查
3. 创建类型错误的测试用例，验证错误报告

设计要求：
- 需要跟踪当前函数符号（添加 currentMethod 字段）
- 在 visitFunctionDecl 中进入函数时设置 currentMethod
- 在 visitReturnStmt 中使用 currentMethod 获取函数类型
- 使用 CompilerLogger.error() 报告类型错误
- 错误信息包含函数名、期望类型、实际类型
```

**Prompt 设计说明**：
- 明确任务目标：添加 return 语句类型检查
- 列出具体要求（2 项）
- 提供参考文件列表（源码、文档）
- 说明约束条件（3 条）
- 说明期望输出（3 项）
- 说明验证方法（3 步）
- 提供设计要求（6 项）

---

### 4.3 AI 应该做 / 不该做

在让 AI 帮助完成本章任务时，我们需要明确 AI 的职责边界。

#### ✅ AI 允许做的事情

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

---

#### ❌ AI 禁止做的事情

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

---

#### 🤝 灰色区域（需谨慎处理）

1. **优化类型推导性能**
   - 可以：添加类型缓存
   - 需谨慎：确保缓存一致性

2. **增强类型系统但不破坏兼容性**
   - 可以：添加新的类型（如 long、double）
   - 需谨慎：确保新类型与现有类型兼容

**如果 AI 提议超出范围的操作**：
- 明确拒绝并说明原因
- 要求 AI 在当前范围内完成任务
- 保留超出范围的建议，待后续章节讨论

---

### 4.4 验证与回滚策略

在 AI 完成修改后，我们必须进行严格的验证才能将更改合并到主分支。

---

#### 自动化验证

**步骤 1：运行相关测试**

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

---

**步骤 2：编译验证**

```bash
# 清理并重新编译
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

---

**步骤 3：运行示例程序**

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

---

#### 手工检查点

即使所有测试通过，我们仍需手工检查以下关键点：

**检查 1：代码风格符合规范**
- [ ] 包命名：`org.teachfx.antlr4.ep16.visitor`、`org.teachfx.antlr4.ep16.symtab`
- [ ] 类命名：PascalCase（如 `LocalDefine`、`LocalResolver`）
- [ ] 方法命名：camelCase（如 `stashScope`、`inferBinaryExprType`）
- [ ] 导入顺序：ANTLR4 → 外部库 → 内部 → Java stdlib

**检查 2：没有引入新的编译错误**
- [ ] 查看项目根目录的 `mvn clean compile` 输出
- [ ] 确认没有新的 ERROR 或 WARNING
- [ ] 检查 AI 修改的类是否能正常编译

**检查 3：没有破坏现有功能**
- [ ] 运行原有测试（如果有），确保不失败
- [ ] 验证多遍编译流水线仍能正确执行
- [ ] 确认 Git diff 只包含预期修改

**检查 4：文档完整性**
- [ ] 新增类/方法有 JavaDoc 注解
- [ ] 关键算法有时间/空间复杂度说明
- [ ] 复杂逻辑有行内注释

---

#### 回滚方案

如果 AI 修改后出现问题，使用以下步骤快速恢复：

---

##### 方案 1：Git Stash（推荐）

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

---

##### 方案 2：Git Checkout（硬恢复）

```bash
# 查看修改历史
git log --oneline -5

# 恢复到修改前的提交
git checkout {commit_hash}

# 或者恢复特定文件
git checkout HEAD~1 -- ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalResolver.java
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
git checkout -b ai-experiment-type-checking

# 让 AI 在新分支工作
# 完成后合并回主分支
git checkout main
git merge ai-experiment-type-checking
```

---

#### 验证流程总结

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

---

#### 常见问题排查

**问题 1：测试失败**

- 现象：`[ERROR] Tests run: X, Failures: Y`
- 排查：
  1. 查看测试失败信息
  2. 检查类型推导逻辑是否正确
  3. 确认作用域查找是否正确
- 解决：根据失败信息修改 AI 代码，或调整测试用例

---

**问题 2：编译错误**

- 现象：`[ERROR] COMPILATION ERROR`
- 排查：
  1. 检查 AI 是否使用了错误的包名
  2. 确认类名是否正确
  3. 验证继承关系是否正确
- 解决：清理 target 目录重新编译，或修正代码

---

**问题 3：类型推导错误**

- 现象：表达式类型推导不正确
- 排查：
  1. 添加调试输出，打印每个节点的类型
  2. 检查 setType 方法的实现
  3. 确认类型兼容性规则是否正确
- 解决：根据调试信息修正代码

---

## 5. 练习题

### 练习 1：添加除零检查（手工实现版）

**难度**：⭐⭐☆☆☆
**预计时间**：30–45 分钟

**题目描述**：
在 LocalResolver 中添加除零检查，当除数为常量 0 时报告错误。

**要求**：
- 完全手工实现，不依赖 AI
- 在除法表达式中检查右操作数是否为 0
- 如果是常量 0，报告错误

**验收标准**：
- [ ] 在 `visitExprBinary` 中添加除零检查
- [ ] 检测到 `x / 0` 时报告错误
- [ ] 代码能编译通过
- [ ] 测试除零时报错

**💡 解题思路提示**：
- 在 `visitExprBinary` 中检查运算符是否为除法（`/`）
- 检查右操作数是否为 `PrimaryINT` 且值为 `0`
- 使用 `CompilerLogger.error()` 报告错误

---

### 练习 2：添加数组类型支持（AI 协作版）

**难度**：⭐⭐⭐☆☆
**预计时间**：45–60 分钟

**题目描述**：
在类型系统中添加数组类型，支持数组类型声明和访问。

**AI 协作要求**：
1. 设计上下文：列出需要提供给 AI 的文件和说明
2. 设计 Prompt：参考本章的 Prompt 模板，设计适合此任务的 Prompt
3. 验证 AI 输出：使用本章的验证策略
4. 理解 AI 代码：确保你能解释 AI 生成的每一部分

**验收标准**：
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

### 练习 3：实现类型推导可视化（综合挑战）

**难度**：⭐⭐⭐⭐☆
**预计时间**：60–90 分钟

**题目描述**：
为 LocalResolver 添加类型推导可视化，以树形结构显示表达式的类型推导过程。

**要求**：
- 可以选择手工实现或 AI 协作
- 如果选择 AI 协作，需要详细记录协作过程
- 提交时说明 AI 参与部分和人工修改部分

**验收标准**：
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

### 练习 4：实现完整的类型检查器（进阶练习 - 可选）

**难度**：⭐⭐⭐⭐⭐
**预计时间**：90–120 分钟

**题目描述**：
实现完整的类型检查器，检查函数参数、返回值、赋值语句等所有类型操作。

**适合人群**：
- 想深入理解类型系统的高级读者
- 有志于实现完整编译器类型检查的高级读者

**💡 解题思路提示**：
- 在 LocalResolver 中添加完整的类型检查逻辑
- 检查函数调用参数类型
- 检查 return 语句类型
- 检查赋值语句类型
- 报告所有类型错误

---

## 6. 本章小结与下一章预告

### 本章小结

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

---

### 【你现在站在哪】

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

---

### 下一章预告

**第9章：从解释到编译的转换**

在下一章，我们将学习：
- 单遍解释器到多遍编译器的演变过程
- 编译器前端设计模式总结
- 解释器和编译器的区别和联系
- 编译器流水线的完整回顾

**你将能够**：
- 理解从解释到编译的技术演进
- 掌握编译器前端的设计模式和最佳实践
- 为学习编译器后端做好准备
- 深入理解现代编译器的架构思想

---

**准备**：为了学习下一章，建议：
- [ ] 复习模块 2 的所有章节（第6-8章）
- [ ] 运行完整的编译器示例，加深理解
- [ ] 对比解释器和编译器的实现差异

继续加油！下一章将完成模块 2 的学习，带你进入编译器的完整世界。
