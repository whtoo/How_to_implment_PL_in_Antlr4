# EP3: 解析树构建与遍历 - 教学文档

**编译器构造阶段**: 前端编译（语法分析）
**难度等级**: ⭐ 初级
**预计学时**: 4小时
**前置知识**: EP1（词法分析）、EP2（语法分析）

---

## 🏗️ 第一层：架构全景

### 1.1 EP3在编译管线中的位置

```
┌─────────────────────────────────────────────────────────┐
│                    完整编译管线                           │
├─────────────────────────────────────────────────────────┤
│  EP1: 词法分析  │  EP2: 语法分析  │  EP3: 解析树构建       │
│  (字符→Token)  │  (Token→规则)   │  (规则→ParseTree)     │
├─────────────────────────────────────────────────────────┤
│                     EP3的位置                            │
│            ┌──────────────────────────┐                 │
│  Token流   │   ANTLR4框架自动生成      │   ParseTree      │
│  ────────→ │   (Lexer + Parser)       │  ─────────→      │
│            │   + 遍历模式扩展          │                 │
│            └──────────────────────────┘                 │
└─────────────────────────────────────────────────────────┘
```

**关键特点**:
- **框架主导**: EP3的核心代码由ANTLR4从语法规则自动生成
- **扩展点**: 通过继承BaseListener/BaseVisitor实现业务逻辑
- **数据流**: Token流 → Parser → ParseTree → 遍历处理

### 1.2 四层架构设计

```
┌──────────────────────────────────────────────────────┐
│  Layer 4: 应用编排层 (Application)                    │
│  ┌────────────────────────────────────────────────┐  │
│  │  ExprJoyRide: 演示完整的解析流程                │  │
│  │  - 创建Lexer/Parser                            │  │
│  │  - 调用解析方法                                │  │
│  │  - 使用Listener/Visitor遍历                    │  │
│  └────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────┤
│  Layer 3: 遍历模式扩展层 (Traversal Extension)        │
│  ┌────────────────────────────────────────────────┐  │
│  │  CustomListener/CustomVisitor: 业务逻辑实现     │  │
│  │  - 符号表构建 (Listener)                       │  │
│  │  - 表达式求值 (Visitor)                        │  │
│  │  - 语义分析                                    │  │
│  └────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────┤
│  Layer 2: 数据结构层 (Data Structure)                 │
│  ┌────────────────────────────────────────────────┐  │
│  │  ParseTree: 语法分析输出                       │  │
│  │  - 记录完整的语法匹配过程                      │  │
│  │  - 包含所有语法细节                            │  │
│  │  - 支持遍历操作                                │  │
│  └────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────┤
│  Layer 1: 框架层 (Framework - ANTLR4 Generated)      │
│  ┌────────────────────────────────────────────────┐  │
│  │  LibExprLexer: 词法分析器                       │  │
│  │  LibExprParser: 语法分析器                      │  │
│  │  LibExprBaseListener: Listener模板             │  │
│  │  LibExprBaseVisitor: Visitor模板               │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

### 1.3 构造逻辑

```
┌──────────────────────────────────────────────────────────┐
│              EP3的四阶段构造逻辑                           │
└──────────────────────────────────────────────────────────┘

阶段1: 语法规则定义 (Grammar Definition)
    ↓
    定义 LibExpr.g4:
    - 词法规则 (ID, INT, NEWLINE)
    - 语法规则 (prog, stat, expr)
    - 运算符优先级处理 (规则顺序 + 标签)
    ↓
阶段2: ANTLR4代码生成 (Framework Generation)
    ↓
    执行: antlr4 LibExpr.g4 -visitor -no-listener
    生成:
    - LibExprLexer.java (词法分析器)
    - LibExprParser.java (语法分析器)
    - LibExprBaseListener.java (Listener模板)
    - LibExprBaseVisitor.java (Visitor模板)
    ↓
阶段3: 遍历模式扩展 (Traversal Extension)
    ↓
    继承模板类实现业务逻辑:
    - SymbolTableBuilder extends LibExprBaseListener
    - ExpressionEvaluator extends LibExprBaseVisitor<Integer>
    ↓
阶段4: 应用层组装 (Application Orchestration)
    ↓
    ExprJoyRide.java:
    1. 创建Lexer: new LibExprLexer(input)
    2. 创建Parser: new LibExprParser(tokens)
    3. 解析程序: parser.prog()
    4. 遍历ParseTree: walker.walk() / visitor.visit()
```

### 1.4 学习路径导航

```
学习顺序 (按依赖关系):

Topic 1: ANTLR4语法规则与解析器生成
    ↓ 框架提供
Topic 2: ParseTree数据结构
    ↓ 框架提供
Topic 3: Listener遍历模式
    ↓ 继承扩展
Topic 4: Visitor遍历模式
    ↓ 继承扩展
Topic 5: 综合应用 - 完整的解析流程
```

---

## 📚 理论教材索引

### 对应《编译原理》（龙书第二版）

**对应章节**: 第2章：一个简单的语法制导翻译器 + 第5章：语法制导翻译

**关键理论概念**:
- **语法制导定义（SDD）**: 将语法规则和语义动作关联的形式化方法
- **抽象语法树（AST）**: 去除语法细节的语法树，保留语义信息
- **综合属性与继承属性**: 属性文法中属性的计算和传递规则
- **语法制导翻译方案（SDT）**: 嵌入语法规则中的语义动作
- **访问者模式**: 遍历和操作树形结构的设计模式

**理论实践对照**:
| 龙书概念 | 本EP实现 | 学习要点 |
|---------|---------|---------|
| 语法制导定义 | ANTLR4的语法规则 + Listener/Visitor | 理论：属性文法 → 实践：Visitor模式 |
| 抽象语法树 | ParseTree（ANTLR4自动生成） | 理论：语义抽象 → 实践：框架自动构建 |
| 综合属性 | Visitor的返回值 | 理论：自底向上计算 → 实践：递归返回值 |
| 继承属性 | Visitor的上下文参数 | 理论：自顶向下传递 → 实践：构造函数注入 |
| 语义动作 | Listener的enter/exit方法 | 理论：语法规则中的动作 → 实践：回调函数 |

**推荐学习路径**:
1. 阅读龙书第2.1-2.3节（2小时）- 理解语法制导翻译的基本概念
2. 阅读龙书第5.1-5.3节（3小时）- 深入理解属性文法和SDD
3. 完成本EP实践（4小时）- 使用ANTLR4实现Listener和Visitor
4. 对比总结（1小时）- 理论属性文法 vs 实践Visitor模式

**补充阅读**:
- **ANTLR4权威指南**: 第4章-第6章（Listener和Visitor的详细实现）
- **设计模式**: 访问者模式（GoF设计模式书籍）
- **相关论文**: "Tree-Based Pattern Matching and Tree Substitution" - 理解树操作的数学基础

**学习提示**:
- 龙书第2章提供了语法制导翻译的理论基础，但使用的是伪代码
- 本EP的实践展示了如何使用现代工具（ANTLR4）实现这些理论
- 注意：龙书中的"语法树"对应本EP的ParseTree，"抽象语法树"将在EP11中实现
- Visitor模式是属性文法的面向对象实现，理解这个映射关系是关键

---

## 📚 第二层：主题单元

### 主题1: ANTLR4语法规则与解析器生成

#### 📍 在EP3中的位置
```
Layer 1: Framework Layer
┌────────────────────────────┐
│  Topic 1: 语法规则与生成     │
│  ├─ LibExpr.g4             │
│  ├─ LibExprLexer           │
│  └─ LibExprParser          │
└────────────────────────────┘
         ↓ 提供基础
   Layer 2: ParseTree (Topic 2)
```

#### 🔗 依赖关系
- **前置依赖**: EP1 (词法规则)、EP2 (文法规则)
- **后续应用**: Topic 2 (ParseTree构建)、Topic 3 (Listener使用)、Topic 4 (Visitor使用)

#### 1.1 核心概念

**概念1: 语法定义的四要素**

1. **词法规则 (Lexer Rules)**: 定义如何将字符流转换为Token
   ```antlr
   ID  : [a-zA-Z]+ ;      // 标识符
   INT : [0-9]+ ;         // 整数
   WS  : [ \t\r\n]+ -> skip ;  // 跳过空白
   ```

2. **语法规则 (Parser Rules)**: 定义Token如何组合成语法结构
   ```antlr
   prog: stat+ ;           // 程序 = 一条或多条语句
   stat: expr | ID '=' expr | NEWLINE ;
   ```

3. **运算符优先级**: 通过**规则顺序**和**标签**实现
   ```antlr
   expr: expr ('*'|'/') expr   # MultDivExpr  // 优先级高
       | expr ('+'|'-') expr   # AddSubExpr   // 优先级低
       | INT                   # IntExpr
       | ID                    # IdExpr
       | '(' expr ')'          # ParenExpr
       ;
   ```

4. **标签 (#Label)**: ANTLR4为每种备选生成独立的Context类
   - `# MultDivExpr` → 生成 `MultDivExprContext`
   - `# IntExpr` → 生成 `IntExprContext`

**概念2: ANTLR4的生成策略**

ANTLR4使用**递归下降解析**和**自适应LL(*)算法**:
- **递归下降**: 每个语法规则对应一个方法
- **自适应预测**: 动态决定采用哪种备选分支
- **错误恢复**: 自动同步和恢复机制

#### 1.2 实现原理

**阶段一: 定义语法规则**

**关键代码** (`LibExpr.g4`):

```antlr
grammar LibExpr;

// 导入公共词法规则
import CommonLexRules;

// 语法规则
prog:   stat+ ;                   // 程序: 多条语句

stat:   expr                      // 语句可以是表达式
    |   ID '=' expr               // 或赋值语句
    |   NEWLINE                   // 或空行
    ;

expr:   expr ('*'|'/') expr       # MultDivExpr  // 乘除优先级高
    |   expr ('+'|'-') expr       # AddSubExpr   // 加减优先级低
    |   INT                       # IntExpr
    |   ID                        # IdExpr
    |   '(' expr ')'              # ParenExpr
    ;
```

**代码解析**:
- **第3行**: `import` 导入公共词法规则（ID、INT、NEWLINE、WS）
- **第6行**: `prog` 是起始规则，`stat+` 表示一条或多条语句
- **第9-12行**: `stat` 规则定义三种语句形式
- **第14-20行**: `expr` 规则使用**标签**处理运算符优先级

`★ Insight ─────────────────────────────────────`
**运算符优先级的秘密**:
1. 乘除规则 `expr '*' expr` 排在加减前面 → **优先级更高**
2. 括号规则 `# ParenExpr` → **改变优先级**
3. 标签 `# MultDivExpr` → ANTLR4生成独立的Context类，无需手工编写复杂的优先级逻辑！
`─────────────────────────────────────────────────`

**阶段二: 运行ANTLR4生成代码**

**生成命令**:
```bash
antlr4 LibExpr.g4 -visitor -no-listener
javac LibExpr*.java
```

**输出文件**:
- `LibExprLexer.java` - 词法分析器
- `LibExprParser.java` - 语法分析器
- `LibExprBaseVisitor.java` - Visitor基类
- `LibExprVisitor.java` - Visitor接口

**阶段三: 解析器工作原理**

**关键代码** (`LibExprParser.java:158-193`):

```java
// prog() 规则对应的解析方法
public final ProgContext prog() throws RecognitionException {
    ProgContext _localctx = new ProgContext(_ctx, State);
    enterRule(_localctx, 0, RULE_prog);
    try {
        int _alt;
        enterOuterAlt(_localctx, 1);
        // 解析一条或多条语句
        {
        setState(8);
        stat();                    // 解析第一条语句
        setState(13);
        _errHandler.sync(this);    // 错误恢复同步点
        _alt = getInterpreter().adaptivePredict(_input,0);
        // 循环解析后续语句
        while ( _alt!=2 && _alt!=org.antlr.v4.runtime.Recognizer.EOF ) {
            if ( _alt==1 ) {
                {
                setState(9);
                stat();            // 解析下一条语句
                }
            }
            setState(15);
            _errHandler.sync(this);
            _alt = getInterpreter().adaptivePredict(_input,0);
        }
        }
    }
    catch (RecognitionException re) {
        _localctx.exception = re;
        _errHandler.reportError(this, re);
        _errHandler.recover(this, re);
    }
    finally {
        exitRule();                 // 触发Listener的exitProg()事件
    }
    return _localctx;              // 返回ProgContext节点
}
```

**代码解析**:
- **第8-15行**: 使用 `adaptivePredict` 实现循环，解析 `stat+`
- **第13行**: `_errHandler.sync(this)` 实现错误恢复
- **第21行**: `exitRule()` 触发Listener的 `exitProg()` 事件
- **设计模式**: **模板方法模式** - 解析流程固定，错误处理可扩展

#### 1.3 实践练习

**练习: 定义一个支持变量的表达式语法**

**任务**: 扩展 `LibExpr.g4`，支持变量声明和引用

**语法规则**:
```antlr
stat:   'var' ID '=' expr          # VarDeclStmt     // 变量声明
    |   ID '=' expr                # AssignStmt      // 变量赋值
    |   expr                       # ExprStmt        // 表达式语句
    |   NEWLINE                    # EmptyStmt       // 空行
    ;
```

**测试输入**:
```
var x = 10
var y = 20
x + y
```

**预期ParseTree结构**:
```
prog
├── stat (VarDeclStmt: var x = 10)
├── stat (VarDeclStmt: var y = 20)
└── stat (ExprStmt: x + y)
    └── expr (AddSubExpr)
        ├── expr (IdExpr: x)
        ├── '+'
        └── expr (IdExpr: y)
```

---

#### 🔄 从本主题到下一主题

**连接代码**:

```java
// Topic 1 提供: 解析器框架
LibExprLexer lexer = new LibExprLexer(CharStreams.fromString("3 + 5 * 2"));
CommonTokenStream tokens = new CommonTokenStream(lexer);
LibExprParser parser = new LibExprParser(tokens);

// → Topic 2 使用: 生成ParseTree数据结构
ParseTree parseTree = parser.prog();  // 返回ProgContext
```

**关系说明**:
- Topic 1 的 `LibExprParser` 提供解析方法
- Topic 2 的 `ParseTree` 是解析方法的输出
- 每个语法规则方法返回对应的Context对象（`ProgContext`, `ExprContext`等）

---

### 主题2: ParseTree数据结构

#### 📍 在EP3中的位置
```
Layer 2: Data Structure Layer
┌────────────────────────────┐
│  Topic 2: ParseTree        │
│  ├─ ProgContext            │
│  ├─ StatContext            │
│  └─ ExprContext子类        │
└────────────────────────────┘
    ↑ 由Topic 1生成
         ↓ 被Topic 3, 4遍历
Topic 3/4: Listener/Visitor
```

#### 🔗 依赖关系
- **前置依赖**: Topic 1 (ANTLR4生成)
- **后续应用**: Topic 3 (Listener遍历)、Topic 4 (Visitor遍历)

#### 2.1 核心概念

**概念1: ParseTree vs 抽象语法树(AST)**

| 维度 | ParseTree (解析树) | AST (抽象语法树) |
|------|-------------------|------------------|
| **定义** | 语法分析器的直接输出 | 抽象掉语法细节的语法树 |
| **内容** | 完整记录语法匹配过程 | 只保留语义信息 |
| **节点** | 包含所有语法细节（括号、标点） | 紧凑，去掉噪音 |
| **用途** | 中间表示，用于后续转换 | 语义分析、优化、代码生成 |
| **EP3范围** | ✅ 当前EP的内容 | ❌ EP11的内容 |

```
示例: 表达式 "3 + 5 * 2"

ParseTree (EP3):
        prog
         |
        expr
       / | \
      3  '+' expr
            / | \
           5  '*' 2

AST (EP11):
       AdditionNode
      /            \
  NumberNode    MultiplicationNode
    (3)         /        \
           NumberNode  NumberNode
              (5)        (2)
```

**概念2: ParseTree的节点类型**

每个语法规则对应的Context类:
- `ProgContext`: `prog()` 规则返回
- `StatContext`: `stat()` 规则返回（实际是子类）
- `MultDivExprContext`: `expr # MultDivExpr` 返回
- `AddSubExprContext`: `expr # AddSubExpr` 返回
- `IntExprContext`: `expr # IntExpr` 返回

#### 2.2 实现原理

**ParseTree的层次结构**

```java
// ParseTree接口
public interface ParseTree extends SyntaxTree {
    ParseTree getParent();              // 获取父节点
    ParseTree getChild(int i);          // 获取子节点
    String getText();                   // 获取文本内容
    // ...
}

// RuleNode接口 (所有规则节点实现)
public interface RuleNode extends ParseTree {
    RuleContext getRuleContext();       // 获取规则上下文
}

// 具体的Context类 (ANTLR4生成)
public static class MultDivExprContext extends ExprContext {
    public ExprContext expr(int i) {    // 访问左右子表达式
        return getRuleContext(ExprContext.class, i);
    }
    public TerminalNode op() {          // 访问操作符
        return getToken(LibExprParser.MUL, LibExprParser.DIV);
    }
}
```

**ParseTree的遍历接口**

```java
// ParseTree支持两种遍历方式:
// 1. Listener模式: 自动遍历
ParseTreeWalker walker = new ParseTreeWalker();
walker.walk(listener, parseTree);       // 自动深度优先遍历

// 2. Visitor模式: 手动控制
T result = visitor.visit(parseTree);    // 必须显式调用visit()
```

#### 2.3 实践练习

**练习: 手动遍历ParseTree**

**任务**: 打印ParseTree的结构

**代码框架**:
```java
public class TreePrinter {
    public static void printTree(ParseTree tree, int level) {
        // 打印缩进
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }

        // 打印节点类型和内容
        System.out.println(tree.getClass().getSimpleName() +
                          ": " + tree.getText());

        // 递归打印子节点
        for (int i = 0; i < tree.getChildCount(); i++) {
            printTree(tree.getChild(i), level + 1);
        }
    }

    public static void main(String[] args) {
        LibExprLexer lexer = new LibExprLexer(CharStreams.fromString("3 + 5 * 2"));
        LibExprParser parser = new LibExprParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.prog();
        printTree(tree, 0);
    }
}
```

**预期输出**:
```
ProgContext: 3 + 5 * 2
  StatContext: 3 + 5 * 2
    ExprContext: 3 + 5 * 2
      MultDivExprContext: 5 * 2
        ExprContext: 5
          IntExprContext: 5
        TerminalNodeImpl: *
        ExprContext: 2
          IntExprContext: 2
```

---

#### 🔄 从本主题到下一主题

**连接代码**:

```java
// Topic 2 提供: ParseTree数据结构
ParseTree parseTree = parser.prog();

// → Topic 3 使用: Listener模式遍历
ParseTreeWalker walker = new ParseTreeWalker();
SymbolTableBuilder builder = new SymbolTableBuilder();
walker.walk(builder, parseTree);  // 自动遍历整棵树

// → Topic 4 使用: Visitor模式遍历
ExpressionEvaluator evaluator = new ExpressionEvaluator();
Integer result = evaluator.visit(parseTree);  // 手动控制遍历
```

**关系说明**:
- Topic 2 的 `ParseTree` 是遍历的目标
- Topic 3 (Listener) 和 Topic 4 (Visitor) 提供不同的遍历策略
- 两种模式都支持对 `ParseTree` 的完整访问

---

### 主题3: Listener遍历模式

#### 📍 在EP3中的位置
```
Layer 3: Traversal Extension Layer
┌────────────────────────────┐
│  Topic 3: Listener模式     │
│  ├─ LibExprBaseListener    │
│  └─ CustomListener扩展     │
└────────────────────────────┘
    ↑ 继承Topic 1的模板
         ↓ 遍历Topic 2的ParseTree
Topic 2: ParseTree
```

#### 🔗 依赖关系
- **前置依赖**: Topic 1 (BaseListener模板)、Topic 2 (ParseTree数据结构)
- **后续应用**: Topic 5 (完整应用示例)

#### 3.1 核心概念

**概念1: Listener模式的特点**

- **自动遍历**: `ParseTreeWalker` 自动深度优先遍历整棵树
- **事件驱动**: 在进入/退出节点时触发 `enterXxx()` / `exitXxx()` 回调
- **无返回值**: Listener方法返回 `void`，适合构建数据结构
- **被动处理**: 不需要显式控制遍历流程

**概念2: Listener vs Visitor**

| 维度 | Listener | Visitor |
|------|----------|---------|
| **遍历方式** | 自动 | 手动 |
| **返回值** | void | 泛型 `<T>` |
| **适用场景** | 构建数据结构 | 计算/求值 |
| **控制粒度** | 粗粒度（节点级） | 细粒度（可短路） |

**应用场景决策树**:
```
需要构建数据结构？
    是 → 使用 Listener
    否 ↓
    需要返回计算结果？
        是 → 使用 Visitor
        否 ↓
        需要短路计算？
            是 → 使用 Visitor
            否 → 两者皆可
```

#### 3.2 实现原理

**阶段一: 理解BaseListener模板**

**关键代码** (`LibExprBaseListener.java`):

```java
public abstract class LibExprBaseListener implements LibExprListener {
    // 每个规则对应enter/exit方法
    @Override public void enterProg(LibExprParser.ProgContext ctx) { }
    @Override public void exitProg(LibExprParser.ProgContext ctx) { }

    @Override public void enterStat(LibExprParser.StatContext ctx) { }
    @Override public void exitStat(LibExprParser.StatContext ctx) { }

    @Override public void enterAddSubExpr(LibExprParser.AddSubExprContext ctx) { }
    @Override public void exitAddSubExpr(LibExprParser.AddSubExprContext ctx) { }

    // ... 所有规则都有空实现
}
```

**代码解析**:
- **模板方法模式**: `BaseListener` 提供空实现，子类选择性重写
- **命名规则**: `enterXxx()` 在进入节点时调用，`exitXxx()` 在退出时调用

`★ Insight ─────────────────────────────────────`
**Listener的事件触发顺序**:
```
对于表达式 "3 + 5":

enterAddSubExpr
  enterIntExpr (3)
  exitIntExpr
  enterIntExpr (5)
  exitIntExpr
exitAddSubExpr
```
这个顺序类似于**后序遍历**：先处理子节点，再处理父节点。这使得在 `exitAddSubExpr` 时，子表达式已经处理完毕。
`─────────────────────────────────────────────────`

**阶段二: 实现符号表构建Listener**

**关键代码**:

```java
public class SymbolTableBuilder extends LibExprBaseListener {
    private Map<String, Integer> symbolTable = new HashMap<>();

    @Override
    public void exitAssign(LibExprParser.AssignContext ctx) {
        // ID '=' expr
        String varName = ctx.ID().getText();
        // 记录变量定义
        symbolTable.put(varName, null);
    }

    @Override
    public void exitIdExpr(LibExprParser.IdExprContext ctx) {
        // 使用变量
        String varName = ctx.ID().getText();
        if (!symbolTable.containsKey(varName)) {
            System.err.println("Undefined variable: " + varName);
        }
    }

    public Map<String, Integer> getSymbolTable() {
        return symbolTable;
    }
}
```

**代码解析**:
- **第8行**: 在 `exitAssign` 中处理赋值语句（此时右值表达式已处理）
- **第9行**: `ctx.ID()` 访问语法树中的ID Token
- **第10行**: 将变量名记录到符号表
- **第16-19行**: 在 `exitIdExpr` 中检查变量是否已定义

**阶段三: 使用Listener遍历ParseTree**

**关键代码**:

```java
public class ExprJoyRide {
    public static void main(String[] args) throws Exception {
        // 1. 创建Lexer和Parser
        LibExprLexer lexer = new LibExprLexer(CharStreams.fromFileName("input.txt"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LibExprParser parser = new LibExprParser(tokens);

        // 2. 解析程序
        ParseTree tree = parser.prog();

        // 3. 创建Listener
        SymbolTableBuilder builder = new SymbolTableBuilder();

        // 4. 遍历ParseTree
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(builder, tree);  // 自动遍历整棵树

        // 5. 获取结果
        System.out.println(builder.getSymbolTable());
    }
}
```

**代码解析**:
- **第14行**: `ParseTreeWalker.walk()` 自动深度优先遍历
- **第15行**: 遍历过程中自动触发 `enterXxx()` 和 `exitXxx()` 回调
- **第18行**: 遍历完成后获取构建的数据结构

#### 3.3 实践练习

**练习: 实现变量类型收集Listener**

**任务**: 假设扩展语法支持类型声明，收集所有变量的类型信息

**语法规则**:
```antlr
stat:   'int' ID '=' expr    # IntDeclStmt
    |   'str' ID '=' STRING  # StrDeclStmt
    |   ...
    ;
```

**代码框架**:
```java
public class TypeCollector extends LibExprBaseListener {
    private Map<String, String> varTypes = new HashMap<>();

    @Override
    public void exitIntDeclStmt(LibExprParser.IntDeclStmtContext ctx) {
        String varName = ctx.ID().getText();
        varTypes.put(varName, "int");
    }

    @Override
    public void exitStrDeclStmt(LibExprParser.StrDeclStmtContext ctx) {
        String varName = ctx.ID().getText();
        varTypes.put(varName, "string");
    }

    public String getType(String varName) {
        return varTypes.get(varName);
    }
}
```

**测试输入**:
```
int x = 10
str name = "Alice"
int y = 20
```

**预期输出**:
```
{x=int, name=string, y=int}
```

---

#### 🔄 从本主题到下一主题

**关系说明**:
- Topic 3 (Listener) 和 Topic 4 (Visitor) 是**平行的两种遍历策略**
- 都继承自 Topic 1 的模板（`BaseListener` / `BaseVisitor`）
- 都遍历 Topic 2 的 `ParseTree`
- 选择哪种模式取决于任务需求（构建数据结构 vs 计算结果）

**对比代码**:

```java
// Listener模式: 自动遍历，构建数据结构
SymbolTableBuilder builder = new SymbolTableBuilder();
ParseTreeWalker walker = new ParseTreeWalker();
walker.walk(builder, tree);                    // 自动遍历
Map<String, Integer> symbols = builder.getSymbolTable();

// Visitor模式: 手动控制，返回计算结果
ExpressionEvaluator evaluator = new ExpressionEvaluator();
Integer result = evaluator.visit(tree);        // 手动调用visit
```

---

### 主题4: Visitor遍历模式

#### 📍 在EP3中的位置
```
Layer 3: Traversal Extension Layer
┌────────────────────────────┐
│  Topic 4: Visitor模式      │
│  ├─ LibExprBaseVisitor     │
│  └─ CustomVisitor扩展      │
└────────────────────────────┘
    ↑ 继承Topic 1的模板
         ↓ 遍历Topic 2的ParseTree
Topic 2: ParseTree
```

#### 🔗 依赖关系
- **前置依赖**: Topic 1 (BaseVisitor模板)、Topic 2 (ParseTree数据结构)
- **后续应用**: Topic 5 (完整应用示例)

#### 4.1 核心概念

**概念1: Visitor模式的特点**

- **手动遍历**: 必须显式调用 `visit()` 访问子节点
- **返回值**: 支持泛型 `<T>`，可以返回任意类型的结果
- **灵活控制**: 支持短路计算、条件遍历
- **主动计算**: 适合实现表达式求值、类型检查等需要返回值的操作

**概念2: Visitor的泛型机制**

```java
public abstract class LibExprBaseVisitor<T> {
    // 每个规则对应的visit方法，返回类型T
    public T visitProg(LibExprParser.ProgContext ctx) {
        return visitChildren(ctx);  // 默认实现：访问子节点
    }

    public T visitAddSubExpr(LibExprParser.AddSubExprContext ctx) {
        return visitChildren(ctx);
    }

    // ... 所有规则都有默认实现
}
```

**关键点**:
- `<T>` 是返回值的类型（如 `Integer`, `Double`, `String`）
- `visitChildren(ctx)` 遍历所有子节点，返回最后一个子节点的值
- 可以重写方法实现自定义逻辑

#### 4.2 实现原理

**阶段一: 理解BaseVisitor模板**

**关键代码** (`LibExprBaseVisitor.java`):

```java
public abstract class LibExprBaseVisitor<T> implements LibExprVisitor<T> {
    @Override
    public T visitProg(LibExprParser.ProgContext ctx) {
        return visitChildren(ctx);  // 默认：遍历子节点
    }

    @Override
    public T visitStat(LibExprParser.StatContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitAddSubExpr(LibExprParser.AddSubExprContext ctx) {
        return visitChildren(ctx);
    }

    // ... 所有规则都有默认实现

    protected T defaultResult() {
        return null;  // 默认返回值
    }
}
```

**代码解析**:
- **第5行**: `visitChildren(ctx)` 自动访问所有子节点
- **第18行**: `defaultResult()` 定义默认返回值（可重写）

`★ Insight ─────────────────────────────────────`
**Visitor的调用机制**:
```
调用 visitor.visit(tree)
  ↓
根据节点类型动态分发:
  - ProgContext    → visitProg()
  - AddSubExprContext → visitAddSubExpr()
  - IntExprContext → visitIntExpr()
```
这是**双重分发**（Double Dispatch）的典型应用：第一次分发根据节点类型，第二次分发根据Visitor类型。
`─────────────────────────────────────────────────`

**阶段二: 实现表达式求值Visitor**

**关键代码**:

```java
public class ExpressionEvaluator extends LibExprBaseVisitor<Integer> {
    private Map<String, Integer> symbolTable = new HashMap<>();

    @Override
    public Integer visitAddSubExpr(LibExprParser.AddSubExprContext ctx) {
        // expr ('+'|'-') expr
        // 显式访问左右子树
        int left = visit(ctx.expr(0));   // 访问左子表达式
        int right = visit(ctx.expr(1));  // 访问右子表达式

        // 根据操作符执行运算
        if (ctx.getChild(1).getText().equals("+")) {
            return left + right;
        } else {
            return left - right;
        }
    }

    @Override
    public Integer visitMultDivExpr(LibExprParser.MultDivExprContext ctx) {
        int left = visit(ctx.expr(0));
        int right = visit(ctx.expr(1));

        if (ctx.getChild(1).getText().equals("*")) {
            return left * right;
        } else {
            if (right == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return left / right;
        }
    }

    @Override
    public Integer visitIntExpr(LibExprParser.IntExprContext ctx) {
        // INT
        return Integer.valueOf(ctx.INT().getText());
    }

    @Override
    public Integer visitIdExpr(LibExprParser.IdExprContext ctx) {
        // ID
        String varName = ctx.ID().getText();
        if (!symbolTable.containsKey(varName)) {
            throw new RuntimeException("Undefined variable: " + varName);
        }
        return symbolTable.get(varName);
    }

    @Override
    public Integer visitAssign(LibExprParser.AssignContext ctx) {
        // ID '=' expr
        String varName = ctx.ID().getText();
        int value = visit(ctx.expr());
        symbolTable.put(varName, value);
        return value;  // 赋值表达式返回赋的值
    }
}
```

**代码解析**:
- **第5-6行**: 显式调用 `visit()` 访问子表达式（**手动控制遍历**）
- **第10-15行**: 根据操作符执行不同的运算
- **第31行**: `ctx.INT().getText()` 获取Token的文本内容
- **第45行**: 将变量值存入符号表
- **第46行**: 赋值表达式返回赋的值（支持链式赋值）

**阶段三: 使用Visitor计算表达式**

**关键代码**:

```java
public class ExprJoyRide {
    public static void main(String[] args) throws Exception {
        // 1. 创建Lexer和Parser
        LibExprLexer lexer = new LibExprLexer(CharStreams.fromFileName("input.txt"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LibExprParser parser = new LibExprParser(tokens);

        // 2. 解析程序
        ParseTree tree = parser.prog();

        // 3. 创建Visitor
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        // 4. 遍历ParseTree并计算
        Integer result = evaluator.visit(tree);

        // 5. 输出结果
        System.out.println("Result: " + result);
    }
}
```

**代码解析**:
- **第14行**: `evaluator.visit(tree)` 手动触发遍历
- **Visitor内部**: 根据节点类型分发到不同的 `visitXxx()` 方法
- **返回值**: 整个表达式的计算结果

#### 4.3 实践练习

**练习1: 实现常量折叠Visitor**

**任务**: 在编译时计算常量表达式

**输入**:
```
x = 3 + 5 * 2
y = 10 / 2
```

**输出**:
```
x = 13
y = 5
```

**提示**:
```java
public class ConstantFolder extends LibExprBaseVisitor<String> {
    @Override
    public String visitAddSubExpr(LibExprParser.AddSubExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));

        // 如果左右都是常量，直接计算
        if (isConstant(left) && isConstant(right)) {
            int l = Integer.parseInt(left);
            int r = Integer.parseInt(right);
            return String.valueOf(ctx.op.getText().equals("+") ? l + r : l - r);
        }

        // 否则保持原表达式
        return "(" + left + " " + ctx.op.getText() + " " + right + ")";
    }

    private boolean isConstant(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```

**练习2: 实现短路逻辑求值**

**任务**: 实现逻辑与 `&&` 和逻辑或 `||` 的短路求值

**语法规则**:
```antlr
expr: expr '&&' expr   # AndExpr
    | expr '||' expr   # OrExpr
    | '!' expr         # NotExpr
    | ...
    ;
```

**提示**:
```java
@Override
public Boolean visitAndExpr(LibExprParser.AndExprContext ctx) {
    boolean left = visit(ctx.expr(0));
    // 短路: 如果左边为false，不计算右边
    if (!left) {
        return false;
    }
    return visit(ctx.expr(1));  // 计算右边
}
```

---

#### 🔄 从本主题到综合应用

**连接代码**:

```java
// Topic 1 提供: 解析器框架
LibExprParser parser = new LibExprParser(tokens);

// Topic 2 提供: ParseTree数据结构
ParseTree tree = parser.prog();

// Topic 3 使用: Listener构建数据结构
SymbolTableBuilder builder = new SymbolTableBuilder();
ParseTreeWalker walker = new ParseTreeWalker();
walker.walk(builder, tree);

// Topic 4 使用: Visitor计算表达式
ExpressionEvaluator evaluator = new ExpressionEvaluator();
evaluator.setSymbolTable(builder.getSymbolTable());  // 共享符号表
Integer result = evaluator.visit(tree);

// → Topic 5: 综合应用
```

**关系说明**:
- Topic 3 (Listener) 和 Topic 4 (Visitor) 可以**组合使用**
- Listener 构建符号表，Visitor 使用符号表进行求值
- 这种分离关注点的设计使得每个组件职责单一

---

### 主题5: 综合应用 - 完整的解析流程

#### 📍 在EP3中的位置
```
Layer 4: Application Layer
┌────────────────────────────┐
│  Topic 5: 综合应用         │
│  └─ ExprJoyRide           │
└────────────────────────────┘
    ↑ 组合Topic 1-4
         ↓ 完整应用
```

#### 🔗 依赖关系
- **前置依赖**: Topic 1-4 (所有主题)
- **功能**: 整合所有组件，展示完整的解析流程

#### 5.1 核心概念

**概念1: 编译器前端的完整管线**

```
源代码
  ↓ [Lexer]
Token流
  ↓ [Parser]
ParseTree
  ↓ [Listener]
符号表
  ↓ [Visitor]
计算结果
```

**概念2: 关注点分离**

- **Lexer**: 字符 → Token（词法分析）
- **Parser**: Token → ParseTree（语法分析）
- **Listener**: ParseTree → 符号表（语义分析第一阶段）
- **Visitor**: ParseTree → 结果（语义分析第二阶段）

#### 5.2 实现原理

**完整应用示例**

```java
public class ExprJoyRide {
    public static void main(String[] args) throws Exception {
        // ========== 阶段1: 词法和语法分析 ==========
        System.out.println("=== 阶段1: 词法和语法分析 ===");

        // 1.1 创建Lexer
        LibExprLexer lexer = new LibExprLexer(
            CharStreams.fromString("x = 10\ny = 20\nx + y * 2\n")
        );
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 1.2 创建Parser
        LibExprParser parser = new LibExprParser(tokens);

        // 1.3 解析程序
        ParseTree tree = parser.prog();
        System.out.println("ParseTree构建完成");

        // ========== 阶段2: 符号表构建 ==========
        System.out.println("\n=== 阶段2: 符号表构建 ===");

        SymbolTableBuilder builder = new SymbolTableBuilder();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(builder, tree);

        Map<String, Integer> symbolTable = builder.getSymbolTable();
        System.out.println("符号表: " + symbolTable);

        // ========== 阶段3: 表达式求值 ==========
        System.out.println("\n=== 阶段3: 表达式求值 ===");

        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        evaluator.setSymbolTable(symbolTable);  // 注入符号表

        Integer result = evaluator.visit(tree);
        System.out.println("最终结果: " + result);
    }
}
```

**代码解析**:
- **第10-13行**: 创建Lexer和Parser
- **第17行**: 解析生成ParseTree
- **第24-26行**: 使用Listener构建符号表
- **第34行**: 将符号表注入到Evaluator
- **第36行**: 使用Visitor计算表达式

#### 5.3 实践练习

**综合练习: 实现完整的计算器语言**

**任务**: 整合Listener和Visitor，实现支持变量的计算器

**功能要求**:
1. [ ] 支持变量定义和赋值
2. [ ] 支持四则运算
3. [ ] 支持括号改变优先级
4. [ ] 检测未定义变量
5. [ ] 检测除零错误

**测试输入**:
```
x = 10
y = 20
z = x + y * 2
w = z / 0
```

**预期输出**:
```
符号表: {x=10, y=20, z=50}
ArithmeticException: Division by zero
```

**实现框架**:
```java
public class Calculator {
    public static void main(String[] args) throws Exception {
        // TODO: 完整实现
        // 1. 创建Lexer和Parser
        // 2. 解析程序
        // 3. 构建符号表
        // 4. 计算表达式
        // 5. 处理错误
    }
}
```

---

## 🎯 第三层：综合实战项目

### 项目: 构建支持变量和函数的表达式解释器

**项目描述**: 综合运用EP3的所有知识，构建一个完整的表达式解释器

**功能需求**:
1. 变量定义和引用
2. 四则运算和括号
3. 函数定义和调用
4. 错误检测（未定义变量、未定义函数、除零错误）

**技术要求**:
- 使用ANTLR4定义语法规则
- 使用Listener构建符号表和函数表
- 使用Visitor实现表达式求值
- 支持递归函数调用

**实现步骤**:

**阶段1: 扩展语法规则**
```antlr
prog:   stat+ ;

stat:   'def' ID '(' paramList? ')' '=' expr   # FuncDef
    |   ID '(' argList? ')'                    # FuncCall
    |   ID '=' expr                            # Assign
    |   expr                                   # ExprStmt
    ;

paramList: ID (',' ID)* ;
argList: expr (',' expr)* ;

expr:   expr ('*'|'/') expr    # MultDiv
    |   expr ('+'|'-') expr    # AddSub
    |   ID '(' argList? ')'    # CallExpr
    |   INT                    # Int
    |   ID                     # Var
    |   '(' expr ')'           # Paren
    ;
```

**阶段2: 实现函数表构建Listener**
```java
public class FunctionTableBuilder extends LibExprBaseListener {
    private Map<String, FunctionSymbol> functionTable = new HashMap<>();

    @Override
    public void exitFuncDef(LibExprParser.FuncDefContext ctx) {
        String funcName = ctx.ID().getText();
        List<String> params = extractParams(ctx.paramList());

        FunctionSymbol func = new FunctionSymbol(funcName, params);
        functionTable.put(funcName, func);
    }

    private List<String> extractParams(ParamListContext ctx) {
        // TODO: 提取参数列表
    }
}
```

**阶段3: 实现函数调用Visitor**
```java
public class FunctionEvaluator extends LibExprBaseVisitor<Integer> {
    private Map<String, FunctionSymbol> functionTable;
    private Map<String, Integer> variableTable;

    @Override
    public Integer visitCallExpr(LibExprParser.CallExprContext ctx) {
        String funcName = ctx.ID().getText();
        FunctionSymbol func = functionTable.get(funcName);

        // 创建新的变量作用域
        Map<String, Integer> localScope = new HashMap<>();

        // 绑定参数
        List<Integer> args = evaluateArgs(ctx.argList());
        for (int i = 0; i < func.getParams().size(); i++) {
            localScope.put(func.getParams().get(i), args.get(i));
        }

        // 调用函数
        return evaluateFunction(func, localScope);
    }
}
```

**阶段4: 整合和测试**
```java
public class ExpressionInterpreter {
    public static void main(String[] args) throws Exception {
        // 完整实现
    }
}
```

**测试用例**:
```
// 测试1: 变量和基本运算
x = 10
y = 20
x + y * 2
// 预期: 50

// 测试2: 函数定义和调用
def square(n) = n * n
square(5)
// 预期: 25

// 测试3: 递归函数
def factorial(n) = n <= 1 ? 1 : n * factorial(n - 1)
factorial(5)
// 预期: 120
```

---

## 📖 设计模式总结

本EP涉及的核心设计模式：

1. **Template Method Pattern**:
   - `LibExprBaseListener`/`LibExprBaseVisitor` 提供模板方法
   - 子类重写具体行为

2. **Observer Pattern**:
   - Listener模式观察ParseTree构建过程
   - 在节点进入/退出时触发回调

3. **Visitor Pattern**:
   - 分离算法和对象结构
   - 支持多种遍历操作

4. **Strategy Pattern**:
   - Listener和Visitor是不同的遍历策略
   - 根据任务需求选择合适策略

5. **Builder Pattern**:
   - ANTLR4逐步构建ParseTree
   - Listener逐步构建符号表

---

## ✅ 检查点

完成本EP后，你应该能够：

- [ ] 解释ParseTree和AST的区别
- [ ] 编写ANTLR4语法规则（包括运算符优先级）
- [ ] 理解ANTLR4如何生成解析器代码
- [ ] 使用Listener模式构建数据结构
- [ ] 使用Visitor模式实现表达式求值
- [ ] 理解递归下降解析的工作原理
- [ ] 整合Listener和Visitor构建完整应用
- [ ] 理解四层架构的设计思想

---

## 🎓 下一步

- **继续学习**: [EP4_教学文档.md](./EP4_教学文档.md) - 类型检查
- **跳到中端**: [EP11_教学文档.md](./EP11_教学文档.md) - 自定义AST构建
- **完整项目**: 尝试实现一个简单的计算器语言（EP1-3）

---

*版本: v3.0-three-layer | EP3教学文档 | 2025-12-27*
