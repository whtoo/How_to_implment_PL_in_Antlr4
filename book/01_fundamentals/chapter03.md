# 第3章：语句与控制流

## 本章概述

本章聚焦于如何为编程语言添加控制流能力（if/else 条件分支、while 循环），这是从表达式求值走向完整语言的关键一步。通过学习本章，你将掌握递归下降解析技术、控制流在语法树中的表示，以及如何在解释器中执行条件分支和循环语句。

【你现在站在哪】:
```
[表达式求值] → [第2章:运算与变量] → ✅ 第3章(语句与控制流) → [第4章:符号表与作用域] → ...
```

---

## 动机与真实场景

### 真实场景：数据处理的条件逻辑

**故事**：小王正在开发一个数据处理系统，需要实现一个简单的脚本语言来配置数据过滤规则。客户提出了这样的需求：

```c
// 如果数据值大于 100，标记为高风险
if (value > 100) {
    risk_level = "high";
} else {
    risk_level = "low";
}

// 持续处理数据，直到所有数据都被处理完
while (data_remaining > 0) {
    process(data);
    data_remaining = data_remaining - 1;
}
```

**小王面临的挑战**：

1. **没有条件分支**：无法根据数据特征执行不同的处理逻辑
2. **没有循环机制**：无法批量处理数据，必须手动写重复代码
3. **语句与表达式混用**：不清楚 `if` 是语句还是表达式，如何统一处理
4. **解析复杂度激增**：条件语句可能嵌套（if 里套 if，while 里套 if），如何正确解析？

**如果缺少本章的能力，你将面临**：
- ❌ 实现的业务逻辑过于简单，无法处理真实场景的复杂性
- ❌ 代码重复严重，同样的处理逻辑要写很多遍
- ❌ 语法设计混乱，语句和表达式边界不清
- ❌ 嵌套结构的解析困难，容易出现"悬挂 else" 等经典问题

### 本章将教你如何：

✅ 理解语句（Statement）和表达式（Expression）的区别与联系
✅ 使用 ANTLR4 定义 if/else 和 while 语句的语法
✅ 实现递归下降解析，正确处理嵌套的控制流结构
✅ 在解释器中执行条件分支和循环，完成控制流语义

---

## 人类工程师线：技术与实现

### 3.1 核心概念

#### 概念 1：语句 vs 表达式

**通俗解释**：

想象你在和朋友聊天：

- **表达式**：像回答问题，有明确的"值"
  - "今天几号？" → "2026年1月12日"（答案是一个值）
  - 在代码中：`x + 5`、`a > b` 都是表达式，它们计算出结果

- **语句**：像发出指令，完成某个动作，不一定有返回值
  - "请坐下"、"把门关上"（执行一个动作）
  - 在代码中：`if (x > 5) { ... }`、`while (x > 0) { ... }` 都是语句

**关键区别**：

| 特性 | 表达式 | 语句 |
|------|--------|------|
| **返回值** | 总是有返回值 | 通常没有返回值 |
| **副作用** | 可能产生副作用（如函数调用） | 通常产生副作用（赋值、输出） |
| **嵌套能力** | 可以作为表达式的一部分 | 可以包含表达式但不能被嵌套在表达式中 |
| **例子** | `x + 5`、`a > b`、`func(10)` | `if (...) {...}`、`while (...) {...}`、`x = 5;` |

[图1：表达式和语句的关系]
```
语句
    ├─ 包含表达式
    │   └─ if (x > 5) { ... }
    │          ^^^^^  这是一个表达式
    ├─ 可能是表达式语句
    │   └─ x + 5;  ← 表达式加分号变成语句
    └─ 可能是块语句
        └─ { int x; if (x > 0) { ... } }
```

**编译器中的处理方式**：

在 Cymbol 语言中，我们采用**严格区分**的设计：
- `expression` 规则：定义所有能计算值的结构（加法、比较、函数调用等）
- `statement` 规则：定义所有执行动作的结构（if、while、赋值、return 等）

这样做的好处：
- ✅ **语义清晰**：表达式计算值，语句执行动作
- ✅ **解析简单**：不会出现"if 是表达式还是语句"的歧义
- ✅ **优化友好**：可以单独优化表达式计算而不影响语句执行

#### 概念 2：控制流语句

**通俗解释**：

控制流就像你在开车时的路线选择：

- **if/else 语句**：遇到岔路口时，根据条件选择走哪条路
  - "如果下雨，走高速；否则，走乡间小路"
  - 代码执行时只走其中一条分支

- **while 循环**：绕圈跑，直到满足某个条件才停止
  - "绕着操场跑，直到累跑不动为止"
  - 代码执行时可能重复执行多次（甚至零次或无限次）

[图2：if/else 的执行流程]
```
          开始
            ↓
     ┌─────┴─────┐
   条件：x > 5 ?
     ↓           ↓
   是           否
     ↓           ↓
 分支A        分支B
     ↓           ↓
   (print A)  (print B)
     ↓           ↓
   └─────┬─────┘
         ↓
       结束
```

[图3：while 循环的执行流程]
```
      开始
        ↓
   ┌────┴─────┐
 条件：i > 0 ?
     ↓         否
    是        ↓
     ↓     跳出循环
  执行循环体
  (print i;
   i = i - 1)
     ↓
   回到条件判断
```

**经典陷阱：悬挂 else 问题**

这是编程语言设计中的一个经典问题：

```c
if (a > 0)
    if (b > 0)
        print("a > 0 and b > 0");
else
    print("a <= 0");  // ← 这个 else 属于哪个 if？
```

在大多数语言（包括 Cymbol）中，`else` 默认与最近的 `if` 配对。为了避免歧义，我们始终使用花括号：
```c
if (a > 0) {
    if (b > 0) {
        print("a > 0 and b > 0");
    } else {
        print("a > 0 but b <= 0");
    }
} else {
    print("a <= 0");
}
```

#### 概念 3：递归下降解析

**通俗解释**：

递归下降是一种"自顶向下"的解析方法，就像组装家具：

1. **从整体开始**：先看说明书（语法规则），知道要组装一个"书柜"
2. **分解任务**：书柜由"侧板"、"隔板"、"门板"等部件组成
3. **递归处理**：每个部件又可以分解成更小的零件
4. **下降到最底层**：一直分解到"螺丝"、"木钉"这样的原子单元

在编译器中：
```c
statement 规则可以包含 if、while、赋值
    if 规则又包含 statement（可以嵌套）
        递归下降：解析 statement 时遇到 if，就进入 if 规则
                    if 规则中又遇到 statement，再次调用 statement 规则
```

**为什么叫"递归下降"**：

- **递归**：规则 A 可以包含规则 A 自己（statement 包含 statement）
- **下降**：从高层规则（如 file）开始，逐步下降到低层规则（如 ID、INT）

**ANTRL4 中的实现**：

ANTLR4 会自动生成递归下降解析器，你只需要在 `.g4` 文件中声明规则：
```antlr
statement
    :   'if' '(' expr ')' statement ('else' statement)?
    |   'while' '(' expr ')' statement
    |   expr '=' expr ';'
    |   ...
    ;
```

ANTLR4 生成的解析器会自动处理递归调用，你不需要手写递归逻辑。

### 3.2 与仓库 EP 的对应关系

**说明**：EP5-EP6 在当前仓库中是历史示例（Java 接口提取、CSV 解析），不是控制流语句的实现。本章的控制流语法和语义主要参考 **EP21** 的完整实现。

对应目录：`ep21/src/main/antlr4/org/teachfx/antlr4/ep21/`

**关键文件清单**：

```
ep21/
├── src/main/antlr4/org/teachfx/antlr4/ep21/
│   └── Cymbol.g4                          # Cymbol 语法定义（包含 if/while 规则）
├── src/main/java/org/teachfx/antlr4/ep21/
│   ├── ast/                                # AST 节点定义
│   │   ├── expr/                          # 表达式节点
│   │   └── stmt/                          # 语句节点
│   │       ├── IfStmtNode.java             # if 语句 AST 节点
│   │       ├── WhileStmtNode.java          # while 循环 AST 节点
│   │       ├── BlockStmtNode.java          # 块语句 AST 节点
│   │       └── ReturnStmtNode.java        # return 语句 AST 节点
│   └── parser/                            # 解析相关（ANTLR4 生成 + 自定义）
│       ├── CymbolParser.java              # ANTLR4 生成的语法分析器
│       └── CymbolLexer.java               # ANTLR4 生成的词法分析器
└── src/test/resources/
    └── t.cymbol                          # 包含 if/while 的测试示例
```

**关键语法规则说明**（来自 `Cymbol.g4`）：

```antlr
// [Cymbol.g4] 语句规则定义
statement:   varDecl             #statVarDecl           // 变量声明语句
    |   'return' expr? ';' #statReturn                   // return 语句
    |   'if' '(' cond=expr ')' then=statement          // if 语句
        ('else' elseDo=statement)? #stateCondition       // 可选的 else 分支
    |   'while' '(' cond=expr ')' then=statement #stateWhile  // while 循环
    |   'break' ';' #visitBreak                         // break 语句
    |   'continue' ';' #visitContinue                   // continue 语句
    |   expr '=' expr ';' #statAssign               // 赋值语句
    |   expr ';'       #exprStat                    // 表达式语句（函数调用等）
    |   block               #statBlock               // 块语句
    ;

// 块语句：用花括号括起来的语句序列
block:  '{' stmts=statement* '}' ;
```

**语法规则详解**：

1. **if 语句规则**：
   ```antlr
   'if' '(' cond=expr ')' then=statement ('else' elseDo=statement)?
   ```
   - `'if' '('`：关键字 `if` 后跟左括号
   - `cond=expr`：条件表达式（括号内的表达式），命名为 `cond`
   - `')'`：右括号
   - `then=statement`：if 为真时执行的语句，命名为 `then`
   - `('else' elseDo=statement)?`：可选的 else 分支，`?` 表示可选
     - `elseDo=statement`：else 分支执行的语句，命名为 `elseDo`

2. **while 循环规则**：
   ```antlr
   'while' '(' cond=expr ')' then=statement
   ```
   - `'while' '('`：关键字 `while` 后跟左括号
   - `cond=expr`：循环条件表达式
   - `')'`：右括号
   - `then=statement`：每次循环执行的语句

3. **块语句规则**：
   ```antlr
   block:  '{' stmts=statement* '}' ;
   ```
   - `'{'`：左花括号
   - `stmts=statement*`：零个或多个语句，命名为 `stmts`
   - `'}'`：右花括号

**标签的作用**（`#statVarDecl`、`#stateCondition` 等）：

这些标签告诉 ANTLR4 为每个备选分支生成对应的访问者（Visitor）方法。例如：
- `#statVarDecl` → 生成 `visitStatVarDecl` 方法
- `#stateCondition` → 生成 `visitStateCondition` 方法
- `#stateWhile` → 生成 `visitStateWhile` 方法

**AST 节点类说明**（概念说明，非实际代码）：

```java
// 概念性代码：IfStmtNode 类的设计思路
public class IfStmtNode extends StmtNode {
    private ExprNode cond;       // 条件表达式
    private StmtNode thenBranch; // if 为真时执行的语句
    private StmtNode elseBranch; // else 分支（可为 null）

    // 构造方法
    public IfStmtNode(ExprNode cond, StmtNode thenBranch, StmtNode elseBranch) {
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    // Getter 方法
    public ExprNode getCond() { return cond; }
    public StmtNode getThenBranch() { return thenBranch; }
    public StmtNode getElseBranch() { return elseBranch; }
}
```

```java
// 概念性代码：WhileStmtNode 类的设计思路
public class WhileStmtNode extends StmtNode {
    private ExprNode cond;       // 循环条件
    private StmtNode body;       // 循环体语句

    // 构造方法
    public WhileStmtNode(ExprNode cond, StmtNode body) {
        this.cond = cond;
        this.body = body;
    }

    // Getter 方法
    public ExprNode getCond() { return cond; }
    public StmtNode getBody() { return body; }
}
```

**访问者模式在控制流中的应用**：

```java
// 概念性代码：AST 解释器中的控制流处理
public class Interpreter extends CymbolBaseVisitor<Object> {

    // 解释器环境：存储变量值
    private Map<String, Object> variables = new HashMap<>();

    @Override
    public Object visitStateCondition(CymbolParser.StateConditionContext ctx) {
        // 1. 评估条件表达式
        Object condValue = visit(ctx.cond);
        boolean condition = toBoolean(condValue);

        // 2. 根据条件选择执行分支
        if (condition) {
            return visit(ctx.then);  // 执行 then 分支
        } else if (ctx.elseDo != null) {
            return visit(ctx.elseDo); // 执行 else 分支
        }
        // 如果没有 else 且条件为假，什么都不做
        return null;
    }

    @Override
    public Object visitStateWhile(CymbolParser.StateWhileContext ctx) {
        // while 循环：反复评估条件并执行循环体
        while (true) {
            // 1. 评估循环条件
            Object condValue = visit(ctx.cond);
            boolean condition = toBoolean(condValue);

            // 2. 如果条件为假，跳出循环
            if (!condition) {
                break;
            }

            // 3. 执行循环体
            visit(ctx.then);
        }

        return null;
    }
}
```

**关键设计模式**：访问者模式

访问者模式将数据结构（AST）与操作（解释、类型检查、代码生成）分离：
- **AST 节点**（如 `IfStmtNode`、`WhileStmtNode`）只表示数据结构
- **访问者**（如 `Interpreter`、`TypeChecker`）实现各种操作
- 每个访问者通过 `accept` 方法访问 AST 节点，实现不同功能

```
访问者模式：
         ┌─────────────────┐
         │   AST 节点      │
         ├─────────────────┤
         │ IfStmtNode      │
         │ WhileStmtNode   │
         │ AssignStmtNode  │
         │ ...            │
         └────────┬────────┘
                  │ accept(visitor)
         ┌────────┴──────────────────────┐
         │                               │
    ┌────▼────┐                   ┌─────▼─────┐
    │ 解释器   │                   │ 类型检查器  │
    │ (执行)   │                   │ (验证)    │
    └─────────┘                   └───────────┘
```

### 3.3 实战流程

#### 步骤1：编译并运行包含控制流的示例程序

```bash
# 进入 EP21 目录
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep21

# 编译项目
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
# [INFO] Total time: XX s
```

**验证点**：检查编译是否成功，没有错误信息。

#### 步骤2：查看 Cymbol 语法文件中的控制流规则

```bash
# 查看语法文件中的 statement 规则
cat src/main/antlr4/org/teachfx/antlr4/ep21/Cymbol.g4 | grep -A 15 "^statement:"
```

**预期输出**：
```
statement:   varDecl             #statVarDecl
    |   'return' expr? ';' #statReturn
    |   'if' '(' cond=expr ')' then=statement ('else' elseDo=statement)? #stateCondition
    |   'while' '(' cond=expr ')' then=statement #stateWhile
    |   'break' ';' #visitBreak
    |   'continue' ';' #visitContinue
    |   expr '=' expr ';' #statAssign
    |   expr ';'       #exprStat
    |   block               #statBlock
    ;
```

**验证点**：确认语法文件中正确定义了 `if` 和 `while` 规则。

#### 步骤3：查看包含控制流的测试示例

```bash
# 查看测试示例文件
cat src/main/resources/t.cymbol
```

**预期输出**：
```
int dec1(int x) {
    return x - 1;
}

int main() {
    int i = 10;
    int j = 11;
    int x = 0;

    while(i > 0) {
        if (i > 5) {
            print(i);
            if(i == 7) {
                return 7;
            }
        }

        print("break");

        i = dec1(i);
    }

   return 0;
}
```

**程序分析**：
1. **外层 while 循环**：`while(i > 0)` - 当 `i` 大于 0 时持续循环
2. **内层 if 语句**：`if (i > 5)` - 当 `i` 大于 5 时打印
3. **嵌套 if 语句**：`if (i == 7)` - 当 `i` 等于 7 时返回 7
4. **递归调用**：`i = dec1(i)` - 调用函数让 `i` 减 1

**预期执行流程**：
- `i` 从 10 开始，每次循环减 1
- 当 `i` 在 6~10 时，打印 `i` 的值
- 当 `i` 等于 7 时，直接返回 7，终止程序
- 如果 `i` 小于等于 5，只打印 "break"

#### 步骤4：运行测试验证控制流解析

```bash
# 运行特定测试（假设有控制流相关的测试）
mvn test -Dtest=*ControlFlow*Test

# 或者运行所有测试
mvn test
```

**预期输出**：
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.teachfx.antlr4.ep21.*
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

**验证点**：确认所有测试通过，没有失败或错误。

#### 步骤5：编写简单的控制流程序并测试

创建一个新的测试文件 `simple_controlflow.cymbol`：

```c
int max(int a, int b) {
    if (a > b) {
        return a;
    } else {
        return b;
    }
}

int main() {
    int result = max(10, 20);
    print(result);

    int i = 0;
    while (i < 5) {
        print(i);
        i = i + 1;
    }

    return 0;
}
```

**手动验证预期行为**：
1. `max(10, 20)` 应该返回 20（因为 20 > 10）
2. `print(result)` 应该输出 20
3. `while` 循环应该打印 0, 1, 2, 3, 4

**保存到文件**：
```bash
cat > simple_controlflow.cymbol << 'EOF'
int max(int a, int b) {
    if (a > b) {
        return a;
    } else {
        return b;
    }
}

int main() {
    int result = max(10, 20);
    print(result);

    int i = 0;
    while (i < 5) {
        print(i);
        i = i + 1;
    }

    return 0;
}
EOF
```

#### 步骤6：调试技巧 - 输出解析后的 AST

如果你在开发中遇到问题，可以输出解析后的 AST 来调试：

```bash
# 假设项目有 AST 输出工具（概念示例）
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep21.debugger.ast.ASTDumper" \
    -Dexec.args="simple_controlflow.cymbol"
```

**概念性输出**（如果 AST Dumper 存在）：
```
[CompileUnit]
  [FunctionDecl: max]
    [Param: a]
    [Param: b]
    [BlockStmt]
      [IfStmt]
        [BinaryExpr: >]
          [ID: a]
          [ID: b]
        [ThenBranch]
          [ReturnStmt]
            [ID: a]
        [ElseBranch]
          [ReturnStmt]
            [ID: b]
  [FunctionDecl: main]
    [BlockStmt]
      [VarDecl: result = [FuncCall: max(10, 20)]]
      [ExprStmt: [FuncCall: print(result)]]
      [VarDecl: i = 0]
      [WhileStmt]
        [BinaryExpr: <]
          [ID: i]
          [Int: 5]
        [Body]
          [ExprStmt: [FuncCall: print(i)]]
          [AssignStmt: i = [BinaryExpr: +][ID: i][Int: 1]]
      [ReturnStmt: [Int: 0]]
```

[图4：控制流程序的 AST 结构]
```
CompileUnit (编译单元)
    ├── FunctionDecl: max (函数定义)
    │   ├── Params: a, b (参数列表)
    │   └── Body:
    │       └── IfStmt (if 语句)
    │           ├── Cond: a > b (条件)
    │           ├── Then: return a (真分支)
    │           └── Else: return b (假分支)
    └── FunctionDecl: main (主函数)
        └── Body:
            ├── VarDecl: result (变量声明)
            ├── ExprStmt: print(result) (打印)
            ├── VarDecl: i (变量声明)
            ├── WhileStmt (while 循环)
            │   ├── Cond: i < 5 (循环条件)
            │   └── Body:
            │       ├── ExprStmt: print(i)
            │       └── AssignStmt: i = i + 1
            └── ReturnStmt: return 0
```

#### 故障排查

**问题1：编译错误 "statement 语法不匹配"**

**现象**：
```
[ERROR] error(123): Cymbol.g4:24:0: syntax error: mismatched input 'if'
```

**原因**：`.g4` 文件中的语法规则写错，可能是括号、标签、分号等符号缺失或位置错误。

**解决方法**：
1. 仔细检查 `.g4` 文件中的 `statement` 规则
2. 确保每个备选分支都有对应的标签（如 `#statVarDecl`）
3. 检查括号是否成对（`(` 和 `)`、`{` 和 `}`）
4. 使用 ANTLR4 的测试工具验证语法：
   ```bash
   # 在 IDEA 中安装 ANTLR4 插件，可以可视化语法树
   ```

**问题2：测试失败 "控制流执行结果不正确"**

**现象**：
```
[ERROR] testIfStatement() expected: <20> but was: <10>
```

**原因**：解释器在执行 if/else 语句时，条件评估或分支选择逻辑有误。

**解决方法**：
1. 检查条件表达式的评估逻辑（`toBoolean` 方法）
2. 确认 `visitStateCondition` 方法中的分支选择逻辑：
   ```java
   if (condition) {
       return visit(ctx.then);
   } else if (ctx.elseDo != null) {
       return visit(ctx.elseDo);
   }
   ```
3. 添加调试输出，打印条件值和选择的分支：
   ```java
   System.out.println("Condition: " + condition);
   System.out.println("Then branch: " + ctx.then);
   System.out.println("Else branch: " + ctx.elseDo);
   ```

**问题3：无限循环或循环执行次数不对**

**现象**：
- 程序一直运行不退出（无限循环）
- 或者循环次数比预期少或多

**原因**：
- 无限循环：循环条件永远为真
- 次数错误：循环体中的变量更新逻辑有误

**解决方法**：
1. 检查循环条件是否正确（如 `i > 0` 应该是 `i >= 0`？）
2. 确认循环体中正确更新了循环变量：
   ```java
   // 错误：忘记更新循环变量
   while (i < 5) {
       print(i);
       // 缺少 i = i + 1;
   }

   // 正确
   while (i < 5) {
       print(i);
       i = i + 1;
   }
   ```
3. 添加循环计数器，防止无限循环（仅用于调试）：
   ```java
   int maxIterations = 1000;
   int iterations = 0;
   while (condition && iterations < maxIterations) {
       // 循环体
       iterations++;
   }
   ```

#### 进阶技巧

**技巧1：使用块语句避免悬挂 else**

始终使用花括号，即使只有一条语句：
```c
// 好的做法
if (a > 0) {
    if (b > 0) {
        print("a > 0 and b > 0");
    } else {
        print("a > 0 but b <= 0");
    }
} else {
    print("a <= 0");
}

// 避免：可能引起歧义
if (a > 0)
    if (b > 0)
        print("a > 0 and b > 0");
    else
        print("a <= 0");  // ← 这个 else 属于哪个 if？
```

**技巧2：优化嵌套的控制流**

过深的嵌套会降低代码可读性，可以考虑提前返回：
```c
// 嵌套版本
void process(int x, int y) {
    if (x > 0) {
        if (y > 0) {
            if (x > y) {
                print("x is larger positive");
            } else {
                print("y is larger positive");
            }
        } else {
            print("x positive, y non-positive");
        }
    } else {
        print("x non-positive");
    }
}

// 提前返回版本（更清晰）
void process(int x, int y) {
    if (x <= 0) {
        print("x non-positive");
        return;
    }
    if (y <= 0) {
        print("x positive, y non-positive");
        return;
    }
    if (x > y) {
        print("x is larger positive");
    } else {
        print("y is larger positive");
    }
}
```

**技巧3：使用循环不变量优化（编译器视角）**

编译器可以识别循环不变量（在循环中每次计算结果相同的表达式）并移出循环：
```c
int compute() {
    int x = 10;
    int y = 20;
    int z = x * y + 100;  // 循环不变量

    int i, sum = 0;
    for (i = 0; i < 1000; i = i + 1) {
        sum = sum + z + i;  // 每次循环都使用 z
    }

    return sum;
}
```

编译器优化后（概念性）：
```c
int compute() {
    int x = 10;
    int y = 20;
    int z = x * y + 100;  // 提前计算

    int i, sum = 0;
    for (i = 0; i < 1000; i = i + 1) {
        sum = sum + i;      // 循环中不再重复计算 z
    }

    return sum + z * 1000;  // 最后一次性加上 z 的总和
}
```

**暂停思考**：
- ✅ 你能说出语句和表达式的区别吗？
- ✅ 为什么需要使用花括号来避免悬挂 else？
- ✅ 在解释器中，while 循环是如何实现的？
- ✅ 递归下降解析是如何处理嵌套的控制流结构的？

---

## AI 协作线：Context Engineering 视角

### 4.1 上下文设计

为了让 AI 帮助完成控制流相关的任务，我们需要精心设计上下文。

**上下文文件列表**：

**源码文件**（按阅读顺序）：

1. `ep21/src/main/antlr4/org/teachfx/antlr4/ep21/Cymbol.g4`
   - **作用**：Cymbol 语言的语法定义文件，包含控制流语句规则
   - **关键规则**：`statement`、`block`、`expr`（用于条件表达式）
   - **重点部分**：
     ```antlr
     statement:   'if' '(' cond=expr ')' then=statement ('else' elseDo=statement)? #stateCondition
         |   'while' '(' cond=expr ')' then=statement #stateWhile
         |   block #statBlock
         |   ...
         ;
     ```

2. `ep21/src/main/java/org/teachfx/antlr4/ep21/parser/CymbolParser.java`（ANTLR4 生成）
   - **作用**：ANTLR4 自动生成的语法分析器，包含解析控制流的方法
   - **关键方法**：
     - `statement()`: 解析语句（分发到具体规则）
     - `stateCondition()`: 解析 if/else 语句
     - `stateWhile()`: 解析 while 循环
     - `block()`: 解析块语句
   - **用途**：理解 ANTLR4 如何处理递归下降解析

3. `ep21/src/main/java/org/teachfx/antlr4/ep21/ast/stmt/`（假设存在）
   - **作用**：控制流语句的 AST 节点定义
   - **关键类**：
     - `IfStmtNode`: if 语句节点
     - `WhileStmtNode`: while 循环节点
     - `BlockStmtNode`: 块语句节点
   - **字段**：条件表达式、分支语句、语句列表等

**文档文件**：

1. `AGENTS.md`
   - **作用**：代码风格规范和最佳实践
   - **相关部分**：
     - 包命名：`org.teachfx.antlr4.ep21.stmt`
     - 类命名：PascalCase（如 `IfStmtNode`、`WhileStmtNode`）
     - 方法命名：camelCase（如 `getCond()`、`getThenBranch()`）

2. `ep21/docs/01_core_design/语言规范.md`（假设存在）
   - **作用**：Cymbol 语言规范的详细说明
   - **相关章节**：控制流语句的语义定义、执行规则

**测试文件**：

1. `ep21/src/test/resources/t.cymbol`
   - **作用**：包含 if/while 的测试示例
   - **测试场景**：嵌套 if、while 循环、函数调用中的控制流

2. `ep21/src/test/java/org/teachfx/antlr4/ep21/*Test.java`
   - **作用**：控制流相关的单元测试
   - **关键测试方法**：
     - `testIfStatement()`: 测试 if 语句的解析和执行
     - `testWhileLoop()`: 测试 while 循环的解析和执行
     - `testNestedControlFlow()`: 测试嵌套的控制流结构

**示例输入/输出**：

1. `ep21/src/main/resources/t.cymbol`
   - **作用**：完整的控制流示例程序
   - **预期行为**：嵌套 if/else、while 循环、return 语句

**上下文组织说明**：

这些文件按照以下原则组织：

1. **语法定义在前**：先提供 `.g4` 文件，让 AI 理解语言的语法结构
2. **解析器实现随后**：展示 ANTLR4 生成的解析器代码
3. **AST 节点定义**：说明控制流在 AST 中的表示
4. **测试用例作为验证**：最后提供测试文件，用于验证 AI 生成代码的正确性
5. **文档作为参考**：提供设计文档，说明语义规则和约束条件

**为什么这样组织**：

- AI 可以先理解语法规则，再了解如何解析和执行
- 测试用例让 AI 知道预期的行为
- 完整的上下文确保 AI 的输出符合项目架构和风格规范

### 4.2 Prompt 模板（给 AI 用）

#### 类型 A：添加新的控制流语句（for 循环）

```
请在不破坏现有语法的前提下，为 Cymbol 语言添加 for 循环语句。

任务目标：
- 实现从 while 循环到 for 循环的语法扩展
- 确保与现有的控制流语句（if、while）兼容
- 支持基本的 for 循环语法：`for (init; cond; update) { body }`

具体要求：

1. 修改 `ep21/src/main/antlr4/org/teachfx/antlr4/ep21/Cymbol.g4` 语法文件
   - 在 `statement` 规则中添加 for 循环的备选分支
   - 语法格式：`'for' '(' init=statement? ';' cond=expr? ';' update=statement? ')' body=statement`
   - 添加标签 `#stateFor` 以生成对应的 Visitor 方法
   - 确保与现有规则（if、while、block）的兼容性

2. 更新 AST 节点类（如果项目使用 AST）
   - 在 `ep21/src/main/java/org/teachfx/antlr4/ep21/ast/stmt/` 目录下
   - 创建 `ForStmtNode` 类
   - 包含以下字段：
     * `initStmt`: 初始化语句（可为 null）
     * `condExpr`: 循环条件表达式（可为 null）
     * `updateStmt`: 更新语句（可为 null）
     * `bodyStmt`: 循环体语句

3. 实现对应的 Visitor/解释器方法
   - 在 `ep21/src/main/java/org/teachfx/antlr4/ep21/` 目录下的解释器类中
   - 实现 `visitStateFor` 方法
   - 语义：等价于以下 while 循环
     ```
     init_stmt;
     while (cond_expr) {
         body_stmt;
         update_stmt;
     }
     ```

4. 添加测试用例验证功能
   - 在 `ep21/src/test/java/org/teachfx/antlr4/ep21/` 目录下
   - 创建 `ForLoopTest.java` 测试类
   - 测试场景：
     * 基本的 for 循环（for (int i = 0; i < 10; i = i + 1) { print(i); }）
     * 缺省初始化（for (; i < 10; i = i + 1) { ... }）
     * 缺省条件（for (int i = 0; ; i = i + 1) { ... } // 无限循环）
     * 缺省更新（for (int i = 0; i < 10; ) { ... }）
     * 嵌套循环

参考上下文文件：
- 源码：
  - `ep21/src/main/antlr4/org/teachfx/antlr4/ep21/Cymbol.g4`（语法文件）
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/parser/CymbolParser.java`（解析器）
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/ast/stmt/WhileStmtNode.java`（参考 while 的实现）
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/Interpreter.java`（解释器）
- 文档：
  - `AGENTS.md`（代码规范）
  - `ep21/docs/01_core_design/语言规范.md`（语言语义）
- 测试：
  - `ep21/src/test/resources/t.cymbol`（控制流示例）
  - `ep21/src/test/java/org/teachfx/antlr4/ep21/*ControlFlow*Test.java`（现有测试）

约束条件：
- 不修改其他 EP 模块的代码
- 保持与现有控制流语句（if、while）的语义一致性
- 所有新增代码必须通过 `mvn test`
- 遵循 AGENTS.md 中的代码风格规范

期望输出：
1. 修改后的 `Cymbol.g4` 文件（标注 for 循环新增部分）
2. `ForStmtNode` 类的完整代码
3. `visitStateFor` 方法的实现代码
4. `ForLoopTest` 测试类代码（包含多个测试用例）
5. 运行测试的命令和预期输出

示例 for 循环程序：
```c
int main() {
    for (int i = 0; i < 5; i = i + 1) {
        print(i);
    }
    return 0;
}
```

预期输出：
```
0
1
2
3
4
```
```

#### 类型 B：生成控制流测试用例

```
请为 if/else 和 while 循环生成一组完整的测试用例，覆盖以下场景：

功能模块：控制流语句（if/else、while 循环）
核心功能：根据条件选择执行分支，根据条件重复执行语句

测试覆盖要求：

1. **正常情况测试**（至少 3 个）
   - 测试1：简单的 if 语句（条件为真）
     * 输入：if (10 > 5) { print("yes"); }
     * 预期输出：yes

   - 测试2：if/else 语句（条件为假）
     * 输入：if (10 < 5) { print("yes"); } else { print("no"); }
     * 预期输出：no

   - 测试3：while 循环（执行多次）
     * 输入：int i = 0; while (i < 3) { print(i); i = i + 1; }
     * 预期输出：0, 1, 2

2. **边界情况测试**（至少 3 个）
   - 测试4：条件为 0 或 1
     * 验证：0 为假，非零值为真（如果是 C 风格）
     * 输入：if (0) { print("true"); } else { print("false"); }
     * 预期输出：false

   - 测试5：循环执行 0 次
     * 验证：循环条件初始为假，循环体不执行
     * 输入：int i = 10; while (i < 5) { print(i); }
     * 预期输出：（无输出）

   - 测试6：没有 else 分支
     * 验证：条件为假时，什么都不做
     * 输入：if (false) { print("yes"); }
     * 预期输出：（无输出）

3. **错误情况测试**（至少 2 个）
   - 测试7：类型不匹配（如果语言是强类型的）
     * 验证：条件表达式必须是布尔类型
     * 输入：if ("string") { print("yes"); }
     * 预期输出：编译错误

   - 测试8：悬挂 else（可选，测试解析器是否正确处理）
     * 验证：else 与最近的 if 配对
     * 输入：
       ```c
       if (a > 0)
           if (b > 0)
               print("both");
           else
               print("which?");
       ```
     * 预期：else 与内层 if 配对

4. **嵌套结构测试**（至少 2 个）
   - 测试9：嵌套 if 语句
     * 验证：多层 if/else 正确配对
     * 输入：if (a > 0) { if (b > 0) { print("both"); } else { print("a only"); } } else { print("none"); }

   - 测试10：while 循环中嵌套 if
     * 验证：循环体中的条件分支正确执行
     * 输入：
       ```c
       int i = 0;
       while (i < 5) {
           if (i % 2 == 0) {
               print("even");
           } else {
               print("odd");
           }
           i = i + 1;
       }
       ```
     * 预期输出：even, odd, even, odd, even

具体要求：
1. 使用 JUnit 5 和 AssertJ
2. 提供 `@DisplayName` 注解的中英文双语描述
3. 包含参数化测试用例（`@ParameterizedTest`）
4. 使用 AssertJ 的流式断言（`assertThat(actual).isEqualTo(expected)`）
5. 每个测试方法包含：
   - `@Test` 注解
   - `@DisplayName` 注解
   - 清晰的测试步骤注释
   - 断言语句
   - 必要时的辅助方法

参考上下文文件：
- 源码：
  - `ep21/src/main/java/org/teachfx/antlr4/ep21/Interpreter.java`（解释器实现）
  - `ep21/src/main/antlr4/org/teachfx/antlr4/ep21/Cymbol.g4`（语法规则）
- 文档：
  - `AGENTS.md`（测试规范）
  - `ep21/docs/02_implementation_standards/EP21_测试规范_整合版.md`（测试规范）
- 现有测试：
  - `ep21/src/test/java/org/teachfx/antlr4/ep21/*Test.java`（现有测试模式）

测试文件结构要求：
```java
package org.teachfx.antlr4.ep21;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

public class ControlFlowTest {

    @Test
    @DisplayName("正常情况：if 语句条件为真 | Normal: if statement with true condition")
    public void testIfTrueCondition() {
        // Given: 准备测试源码
        String source = "if (10 > 5) { print(\"yes\"); }";

        // When: 执行解释器
        String output = execute(source);

        // Then: 验证输出
        assertThat(output).isEqualTo("yes");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, 100})
    @DisplayName("边界情况：不同条件的真假值 | Edge case: boolean value of different conditions")
    public void testBooleanValueOfCondition(int condition) {
        // Given: 准备测试源码
        String source = "if (" + condition + ") { print(\"true\"); } else { print(\"false\"); }";

        // When: 执行解释器
        String output = execute(source);

        // Then: 验证输出（0 为假，非零为真）
        if (condition == 0) {
            assertThat(output).isEqualTo("false");
        } else {
            assertThat(output).isEqualTo("true");
        }
    }

    // 辅助方法：执行 Cymbol 源码并返回输出
    private String execute(String source) {
        // 实现解释器执行逻辑
        // 返回标准输出
    }
}
```

期望输出：
1. 完整的测试类代码（符合上述结构）
2. 每个测试用例的详细说明（目的、输入、预期输出）
3. 运行测试的 Maven 命令
4. 测试覆盖率报告（如果适用）
```

### 4.3 AI 应该做 / 不该做

在让 AI 帮助完成本章任务时，我们需要明确 AI 的职责边界。

**✅ AI 允许做的事情**：

1. **添加新的控制流语法**
   - 可以：添加 for 循环、switch-case、do-while 等新语法
   - 可以：扩展现有语法规则（如添加循环控制变量）
   - 不能：改变 if/while 语句的语义（除非明确要求）

2. **生成控制流测试用例**
   - 可以：编写单元测试、集成测试、参数化测试
   - 可以：生成测试辅助方法、测试固件
   - 不能：删除或破坏现有测试

3. **优化控制流解释实现**
   - 可以：改进条件评估的性能
   - 可以：添加循环优化提示（如检测无限循环）
   - 不能：改变控制流的语义（如 if 的条件判断规则）

4. **生成 AST 节点和访问者代码**
   - 可以：创建新的 AST 节点类
   - 可以：实现新的 Visitor 方法
   - 可以：添加注释说明节点结构和访问逻辑
   - 不能：改变现有的 AST 层次结构

5. **解释语法规则和设计决策**
   - 可以：添加 JavaDoc、行内注释、设计说明
   - 可以：生成 README 片段、使用示例
   - 可以：说明为什么选择递归下降解析
   - 不能：替换现有设计文档

**❌ AI 禁止做的事情**：

1. **修改核心语法规则的结构**
   - 不允许：改变语句和表达式的区分方式
   - 不允许：删除 if/while 的语法规则
   - 除非：明确要求重构语法设计

2. **改变控制流的语义**
   - 不允许：将 `if (condition)` 的判断逻辑从"非零为真"改为其他
   - 不允许：改变 else 的配对规则（除非要求修复悬挂 else）
   - 不允许：修改 while 循环的执行顺序（条件在前 vs 后执行）

3. **删除测试用例或降低测试覆盖率**
   - 不允许：移除现有的控制流测试
   - 不允许：将断言改为总是通过
   - 原因：测试是质量的保证，不能破坏

4. **破坏语法规则的兼容性**
   - 不允许：修改语法后导致现有代码无法解析
   - 不允许：引入歧义语法（如相同的字符串可以解析为不同的规则）
   - 原因：保证向后兼容性，避免破坏已有代码

5. **引入新的外部依赖**
   - 不允许：在 pom.xml 中添加新库
   - 不允许：使用项目技术栈之外的框架
   - 原因：保持项目技术栈一致性，降低学习成本

**🤝 灰色区域（需谨慎处理）**：

1. **性能优化但不改变语义**
   - 可以：优化条件评估的效率（如缓存常量表达式结果）
   - 需谨慎：添加 JIT 优化、循环展开等可能引入 bug 的优化

2. **语法糖扩展**
   - 可以：添加更简洁的语法（如 `a ? b : c` 三元运算符）
   - 需谨慎：确保语法糖等价于基本的 if/else 语句

如果 AI 提议超出范围的操作，请：
- 明确拒绝并说明原因
- 要求 AI 在当前范围内完成任务
- 保留超出范围的建议，待后续章节讨论

### 4.4 验证与回滚策略

在 AI 完成修改后，我们必须进行严格的验证才能将更改合并到主分支。

## 自动化验证

**步骤1：运行相关测试**

```bash
# 进入 EP21 目录
cd ep21

# 运行所有测试
mvn test

# 或者运行特定的控制流测试类
mvn test -Dtest=ControlFlowTest
mvn test -Dtest=WhileLoopTest
mvn test -Dtest=IfStatementTest
```

**预期输出**：
```
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**关键测试**：
- `ControlFlowTest.java` - 验证控制流的基本功能
- `IfStatementTest.java` - 验证 if/else 语句
- `WhileLoopTest.java` - 验证 while 循环
- `NestedControlFlowTest.java` - 验证嵌套控制流

**验证标准**：
- ✅ 所有测试通过（Failures: 0, Errors: 0）
- ✅ 测试覆盖率不降低
- ✅ 没有新的编译警告

**步骤2：编译验证**

```bash
# 清理并重新编译
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

**步骤3：运行示例程序**

```bash
# 运行控制流示例（假设有解释器）
cd ep21
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep21.Interpreter" \
    -Dexec.args="src/main/resources/t.cymbol"
```

**预期输出**：
```
10
9
8
7
return 7
```

**验证点**：确认输出与预期一致（while 循环打印 10、9、8、7，在 i=7 时返回）。

## 手工检查点

即使所有测试通过，我们仍需手工检查以下关键点：

**检查1：语法规则正确性**
- [ ] if/else 语句的语法规则正确
  - 条件表达式在括号内：`'(' cond=expr ')'`
  - then 和 else 分支都是 statement 类型
  - else 分支可选：`('else' elseDo=statement)?`
- [ ] while 循环的语法规则正确
  - 条件表达式在括号内：`'(' cond=expr ')'`
  - 循环体是 statement 类型
- [ ] 块语句的语法规则正确
  - 花括号包围：`'{' stmts=statement* '}'`
  - 支持零个或多个语句：`statement*`

**检查2：语义执行正确性**
- [ ] if 语句的条件评估正确
  - 使用 `toBoolean()` 将表达式转换为布尔值
  - 条件为真时执行 then 分支
  - 条件为假时执行 else 分支（如果存在）
- [ ] while 循环的执行逻辑正确
  - 先评估条件，条件为假时跳过循环体
  - 每次执行完循环体后重新评估条件
  - 循环体中正确更新循环变量（否则无限循环）
- [ ] 嵌套结构的配对正确
  - else 与最近的 if 配对（解决悬挂 else）
  - 嵌套的 while 循环正确执行
  - 块语句的作用域正确处理

**检查3：没有引入新的语法错误**
- [ ] 查看项目根目录的 `mvn clean compile` 输出
- [ ] 确认没有新的 ERROR 或 WARNING
- [ ] 检查 ANTLR4 生成的代码是否正确（Lexer 和 Parser）
- [ ] 确认语法文件没有歧义或左递归错误

**检查4：代码风格符合规范**
- [ ] 包命名：`org.teachfx.antlr4.ep21.stmt`（AST 节点）
- [ ] 类命名：PascalCase（如 `IfStmtNode`、`WhileStmtNode`）
- [ ] 方法命名：camelCase（如 `visitStateCondition`、`getCond`）
- [ ] 导入顺序：ANTLR4 → 外部库 → 内部 → Java stdlib

## 回滚方案

如果 AI 修改后出现问题，使用以下步骤快速恢复：

### 方案1：Git Stash（推荐）

```bash
# 查看当前修改
git status

# 保存 AI 修改到 stash（带备注）
git stash push -m "AI changes for Chapter 3: control flow"

# 如果出现问题，恢复到修改前状态
git checkout .

# 如果需要查看 AI 修改
git stash show -p stash@{0}

# 或者恢复 AI 修改
git stash pop

# 如果确认 AI 修改有问题，丢弃它
git stash drop
```

### 方案2：Git Checkout（硬恢复）

```bash
# 查看修改历史
git log --oneline -5

# 恢复到修改前的提交
git checkout {commit_hash}

# 或者恢复特定文件
git checkout HEAD~1 -- ep21/src/main/antlr4/org/teachfx/antlr4/ep21/Cymbol.g4
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
git checkout -b ai-experiment-chapter3

# 让 AI 在新分支工作
# 完成后验证
mvn test

# 如果验证通过，合并回主分支
git checkout main
git merge ai-experiment-chapter3

# 如果验证失败，删除实验分支
git branch -D ai-experiment-chapter3
```

## 验证流程总结

```
AI 生成代码
    ↓
自动化验证（mvn test）
    ↓
手工检查（语法规则、语义执行、代码风格）
    ↓
运行示例程序
    ↓
✅ 验证通过，合并到主分支
    ❌ 验证失败，执行回滚
```

## 常见问题排查

**问题1：测试失败 - 控制流执行不正确**

- **现象**：`[ERROR] testIfStatement() expected: <20> but was: <10>`
- **排查**：
  1. 查看 `target/surefire-reports/` 下的测试报告
  2. 检查失败测试的断言信息
  3. 确认 AI 生成的代码与预期行为一致
- **解决**：
  1. 检查条件评估逻辑（`toBoolean` 方法）
  2. 确认 if/else 的分支选择逻辑
  3. 添加调试输出，打印条件值和执行的分支

**问题2：编译错误 - 语法规则错误**

- **现象**：`[ERROR] error(123): Cymbol.g4:24:0: syntax error: mismatched input 'if'`
- **排查**：
  1. 检查 `.g4` 文件中的语法规则
  2. 确认括号、标签、分号等符号是否正确
  3. 检查是否有左递归或歧义
- **解决**：
  1. 清理 `target` 目录重新编译：`mvn clean compile`
  2. 使用 ANTLR4 工具验证语法
  3. 修正语法规则中的错误

**问题3：运行时异常 - 无限循环**

- **现象**：程序一直运行不退出，输出大量日志
- **排查**：
  1. 检查 while 循环的条件是否正确
  2. 确认循环体中是否更新了循环变量
  3. 查看是否有"永远为真"的条件
- **解决**：
  1. 添加循环计数器（仅用于调试）
  2. 修正循环条件或循环体逻辑
  3. 添加超时机制（防止无限循环）

---

## 练习题

### 练习1：手动解析嵌套 if/else（手工实现版）

**难度**：⭐⭐☆☆☆
**预计时间**：30–45 分钟

**题目描述**：

给定以下 Cymbol 代码，手工绘制其对应的 AST 结构（类似本章中的图4）：

```c
int compute(int a, int b, int c) {
    if (a > 0) {
        if (b > 0) {
            if (c > 0) {
                return a + b + c;
            } else {
                return a + b;
            }
        } else {
            return a;
        }
    } else {
        return 0;
    }
}

int main() {
    int result = compute(5, 10, -3);
    print(result);
    return 0;
}
```

**要求**：
- 完全手工绘制，不使用 AI
- 参考本章中的 AST 图示风格
- 标注每个节点的类型和关键字段
- 说明程序的执行流程（哪些分支被执行）

**验收标准**：
- [ ] AST 结构清晰，层次分明
- [ ] 每个节点标注了类型（IfStmtNode、ReturnStmtNode 等）
- [ ] 执行流程描述正确（`compute(5, 10, -3)` 返回 15）

**💡 解题思路提示**：
1. 从最外层的 `if (a > 0)` 开始绘制
2. 递归处理每个嵌套的 if/else
3. 使用缩进表示嵌套层次
4. 标注条件表达式和分支语句

---

### 练习2：实现 for 循环语法（AI 协作版）

**难度**：⭐⭐⭐☆☆
**预计时间**：45–60 分钟

**题目描述**：

为 Cymbol 语言添加 for 循环语句，语法如下：

```c
for (init; cond; update) {
    body
}
```

示例代码：
```c
int main() {
    for (int i = 0; i < 5; i = i + 1) {
        print(i);
    }
    return 0;
}
```

预期输出：0, 1, 2, 3, 4

**AI 协作要求**：
1. **设计上下文**：列出需要提供给 AI 的文件和说明
   - 语法文件（`.g4`）
   - AST 节点定义
   - 解释器/访问者实现
   - 现有测试用例

2. **设计 Prompt**：参考本章的"类型 A" Prompt 模板，设计适合此任务的 Prompt
   - 明确语法规则修改
   - 说明 AST 节点设计
   - 定义语义（for 循环等价于 while 循环）

3. **验证 AI 输出**：使用本章的验证策略
   - 运行测试：`mvn test`
   - 手工检查语法规则和语义
   - 运行示例程序验证输出

4. **理解 AI 代码**：确保你能解释 AI 生成的每一部分
   - 语法规则的标签含义
   - AST 节点的字段用途
   - 解释器中的执行逻辑

**验收标准**：
- [ ] AI 生成的代码能编译通过
- [ ] 功能正确且符合要求（for 循环等价于 while 循环）
- [ ] 通过所有测试（包括边界情况）
- [ ] 你能解释 AI 代码的实现逻辑

**💡 解题思路提示**：
1. 参考本章中的"类型 A：添加新的控制流语句（for 循环）" Prompt 模板
2. for 循环的语义等价于：
   ```c
   init;
   while (cond) {
       body;
       update;
   }
   ```
3. 在语法文件中，`init` 和 `update` 是 statement（可能是变量声明或赋值），`cond` 是 expr（可能是布尔表达式）
4. AST 节点需要包含四个字段：`initStmt`、`condExpr`、`updateStmt`、`bodyStmt`

---

### 练习3：调试控制流 bug（综合挑战）

**难度**：⭐⭐⭐⭐☆
**预计时间**：60–90 分钟

**题目描述**：

以下解释器的 while 循环实现有 bug，导致循环执行次数不正确。请找出 bug 并修复。

```java
// 有 bug 的 while 循环实现
@Override
public Object visitStateWhile(CymbolParser.StateWhileContext ctx) {
    // Bug 1: 先执行循环体，再检查条件（错误！）
    visit(ctx.then);

    // 评估循环条件
    Object condValue = visit(ctx.cond);
    boolean condition = toBoolean(condValue);

    // 根据条件决定是否继续循环
    while (condition) {
        visit(ctx.then);  // 重复执行循环体
        condValue = visit(ctx.cond);  // 重新评估条件
        condition = toBoolean(condValue);
    }

    return null;
}
```

**测试代码**：
```c
int main() {
    int i = 0;
    while (i < 3) {
        print(i);
        i = i + 1;
    }
    return 0;
}
```

**预期输出**：0, 1, 2
**实际输出**：0, 0, 0, 1, 1, 1, 2, 2, 2

**要求**：
- 可以选择手工实现或 AI 协作
- 如果选择 AI 协作，需要详细记录协作过程
- 提交时说明：
  - Bug 的根本原因是什么？
  - 修复后的代码是什么样的？
  - 为什么修复后的代码是正确的？

**验收标准**：
- [ ] Bug 原因分析清晰
- [ ] 修复后的代码正确（循环执行 3 次，输出 0, 1, 2）
- [ ] 通过所有测试用例
- [ ] 代码可读性良好，有注释说明

**💡 解题思路提示**：
1. 对比正确的 while 循环语义：先评估条件，条件为真才执行循环体
2. Bug 1：代码在 while 循环之前就执行了一次循环体
3. Bug 2：while 循环体内又执行了一次循环体（每次循环实际执行两次）
4. 修复方法：删除多余的 `visit(ctx.then)` 调用

---

### 练习4：实现 do-while 循环（进阶练习 - 可选）

**难度**：⭐⭐⭐⭐⭐
**预计时间**：90–120 分钟

**题目描述**：

为 Cymbol 语言添加 do-while 循环语句，语法如下：

```c
do {
    body
} while (cond);
```

与 while 循环的区别：
- while 循环：先评估条件，可能执行 0 次
- do-while 循环：先执行一次循环体，再评估条件，至少执行 1 次

示例代码：
```c
int main() {
    int i = 10;
    do {
        print(i);
        i = i - 1;
    } while (i > 0);
    return 0;
}
```

预期输出：10, 9, 8, 7, 6, 5, 4, 3, 2, 1

**适合人群**：
- 想深入理解控制流语义的读者
- 有志于研究编译器后端优化的读者

**要求**：
1. 修改语法文件，添加 do-while 规则
2. 创建 AST 节点类 `DoWhileStmtNode`
3. 实现解释器方法 `visitStateDoWhile`
4. 编写测试用例，验证：
   - 循环至少执行 1 次（即使初始条件为假）
   - 循环执行次数正确
   - 嵌套的 do-while 循环

**验收标准**：
- [ ] 语法规则正确
- [ ] AST 节点设计合理
- [ ] 解释器实现符合语义（至少执行 1 次）
- [ ] 通过所有测试用例
- [ ] 有完整的文档注释

**💡 解题思路提示**：
1. 语法规则：`'do' body=statement 'while' '(' cond=expr ')' ';'`
2. 语义：先执行循环体，再评估条件，条件为真时继续循环
3. 实现逻辑：
   ```java
   do {
       visit(ctx.body);
   } while (toBoolean(visit(ctx.cond)));
   ```
4. 测试用例：
   - 初始条件为真，循环多次
   - 初始条件为假，仍然执行 1 次
   - 循环体中更新条件，循环正常结束

---

## 本章小结

通过本章的学习，你已经掌握了：

1. **控制流语句的核心概念**
   - 理解了语句（Statement）和表达式（Expression）的区别
   - 掌握了 if/else 条件分支和 while 循环的语义
   - 学会了递归下降解析技术如何处理嵌套的控制流结构

2. **ANTLR4 语法设计**
   - 学会了在 `.g4` 文件中定义控制流语句规则
   - 理解了标签（如 `#stateCondition`）的作用
   - 掌握了如何处理可选语法（如 `else` 分支）

3. **AST 和解释器实现**
   - 学会了设计控制流 AST 节点（IfStmtNode、WhileStmtNode）
   - 掌握了在解释器中执行条件分支和循环的逻辑
   - 理解了访问者模式在控制流中的应用

**【你现在站在哪】**：
```
[表达式求值] → [变量管理] → ✅ 第3章(语句与控制流) → [第4章:符号表与作用域] → ...
```

**当前在编译器流水线的位置**：

- 本章实现了编译器前端解析阶段的**控制流语句解析**
- 生成了**包含控制流节点的 AST**，为下一阶段的语义分析奠定基础

**与下一章的衔接**：

- 本章的**控制流 AST**将在下一章被用于：
  - **符号表构建**：在控制流中识别变量声明和使用
  - **作用域分析**：处理嵌套块语句中的变量作用域
  - **静态检查**：验证控制流中的变量使用是否合法（未声明、未初始化等）

**关键收获总结**：

✅ **控制流是语言的灵魂**：没有控制流，语言无法表达复杂的业务逻辑
✅ **递归下降解析**：简单、直观、易于理解和调试
✅ **访问者模式**：将 AST 结构与操作（解释、检查、优化）分离
✅ **AI 协作**：通过精心设计的上下文和 Prompt，让 AI 帮助实现控制流功能

---

## 下一章预告

**第4章：符号表与作用域**

在下一章，我们将学习：

- **符号表设计**：如何存储和管理程序中的变量、函数等符号
- **作用域管理**：如何处理嵌套的块语句和函数的作用域
- **符号解析**：如何在编译时查找变量的声明和类型信息

你将能够：
- 理解编译器如何跟踪程序中的所有符号
- 设计多级作用域的符号表
- 实现变量查找和类型推断

**准备**：为了学习下一章，建议：

- [ ] 复习本章的控制流 AST 结构
- [ ] 运行本章的示例程序，加深理解
- [ ] 思考：如何在嵌套的 if/while 中正确查找变量声明？

继续加油！下一章将带你进入编译器的**符号管理**世界，这是静态分析和类型检查的基础。
