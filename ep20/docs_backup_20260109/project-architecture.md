# EP20 Cymbol编译器项目架构文档

## 1. 项目概述

EP20 Cymbol编译器是一个基于ANTLR4的编译器项目，实现了从Cymbol语言源代码到虚拟机指令的完整编译流程。项目采用模块化设计，分为词法分析、语法分析、AST构建、语义分析、中间表示(IR)生成、控制流图(CFG)构建、优化和代码生成等阶段。

## 2. 项目结构

```
ep20/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/teachfx/antlr4/ep20/
│   │   │       ├── ast/                 # 抽象语法树节点
│   │   │       ├── ir/                  # 中间表示(IR)节点
│   │   │       ├── parser/              # ANTLR4生成的解析器
│   │   │       ├── pass/                # 编译器各阶段处理
│   │   │       ├── symtab/              # 符号表实现
│   │   │       ├── debugger/            # 调试工具
│   │   │       ├── driver/              # 编译器驱动
│   │   │       ├── error/               # 错误处理
│   │   │       └── utils/               # 工具类
│   │   └── resources/
│   └── test/
│       └── java/
│           └── org/teachfx/antlr4/ep20/
│               ├── ast/                 # AST节点测试
│               ├── ir/                  # IR节点测试
│               └── pass/                # 编译器各阶段测试
└── docs/                                # 文档
```

## 3. 核心模块设计

### 3.1 AST模块 (Abstract Syntax Tree)

#### 3.1.1 类结构图
```mermaid
classDiagram
    class ASTNode {
        +accept(visitor)
        +getLocation()
        +getText()
    }
    
    class ExprNode {
        +type
        +getType()
    }
    
    class StmtNode {
        +location
        +getLocation()
    }
    
    class DeclNode {
        +name
        +getName()
    }
    
    ASTNode <|-- ExprNode
    ASTNode <|-- StmtNode
    ASTNode <|-- DeclNode
    
    ExprNode <|-- BinaryExprNode
    ExprNode <|-- UnaryExprNode
    ExprNode <|-- LiteralNode
    LiteralNode <|-- IntExprNode
    LiteralNode <|-- FloatExprNode
    LiteralNode <|-- StringExprNode
    LiteralNode <|-- BoolExprNode
    ExprNode <|-- IDExprNode
    ExprNode <|-- CallFuncNode
    ExprNode <|-- NullExprNode
    
    StmtNode <|-- IfStmtNode
    StmtNode <|-- WhileStmtNode
    StmtNode <|-- AssignStmtNode
    StmtNode <|-- ReturnStmtNode
    StmtNode <|-- BlockStmtNode
    StmtNode <|-- VarDeclStmtNode
    StmtNode <|-- ExprStmtNode
    StmtNode <|-- BreakStmtNode
    StmtNode <|-- ContinueStmtNode
    
    DeclNode <|-- VarDeclNode
    DeclNode <|-- FuncDeclNode
    DeclNode <|-- VarDeclListNode
    
    class ASTVisitor {
        +visit(node)
    }
    
    ASTVisitor <|-- ASTBaseVisitor
```

#### 3.1.2 主要类说明
- `ASTNode`: 所有AST节点的基类，实现访问者模式
- `ExprNode`: 表达式节点基类
- `StmtNode`: 语句节点基类
- `DeclNode`: 声明节点基类

### 3.2 符号表模块 (Symbol Table)

#### 3.2.1 类结构图
```mermaid
classDiagram
    class Scope {
        <<interface>>
        +define(symbol)
        +resolve(name)
        +getParentScope()
        +getSymbols()
    }
    
    class Symbol {
        <<interface>>
        +getName()
        +getType()
    }
    
    class BaseScope {
        -symbols
        -enclosingScope
        +define(symbol)
        +resolve(name)
        +getParentScope()
    }
    
    class GlobalScope {
        +GlobalScope()
    }
    
    class LocalScope {
        +LocalScope(parent)
    }
    
    class Symbol {
        -name
        -type
        +getName()
        +getType()
    }
    
    class VariableSymbol {
        -slotIdx
        +getSlot()
    }
    
    class MethodSymbol {
        -args
        -builtIn
        +isBuiltIn()
        +getArgs()
    }
    
    class ScopedSymbol {
        -scope
        +getScope()
    }
    
    class Type {
        <<interface>>
        +isCompatibleWith(other)
        +getName()
    }
    
    class BuiltInTypeSymbol {
        -name
        +getName()
        +isCompatibleWith(other)
    }
    
    class OperatorType {
        -op
        -leftType
        -rightType
        -resultType
    }
    
    class SymbolTable {
        -globalScope
        -currentScope
        +define(symbol)
        +resolve(name)
        +pushScope(scope)
        +popScope()
    }
    
    class TypeTable {
        -types
        +getType(name)
        +registerType(type)
    }
    
    Scope <|.. BaseScope
    BaseScope <|-- GlobalScope
    BaseScope <|-- LocalScope
    
    Symbol <|-- VariableSymbol
    Symbol <|-- MethodSymbol
    Symbol <|-- ScopedSymbol
    
    Type <|.. BuiltInTypeSymbol
    Type <|.. OperatorType
    
    SymbolTable --> Scope
    SymbolTable --> TypeTable
```

### 3.3 中间表示模块 (Intermediate Representation)

#### 3.3.1 类结构图
```mermaid
classDiagram
    class IRNode {
        +accept(visitor)
        +getText()
    }
    
    class Expr {
        +type
        +getType()
    }
    
    class Stmt {
        +location
    }
    
    class Prog {
        -blockList
        +getBlockList()
    }
    
    IRNode <|-- Expr
    IRNode <|-- Stmt
    IRNode <|-- Prog
    
    Expr <|-- BinExpr
    Expr <|-- UnaryExpr
    Expr <|-- CallFunc
    Expr <|-- ConstVal
    Expr <|-- VarSlot
    
    Stmt <|-- Assign
    Stmt <|-- Label
    Stmt <|-- JMP
    Stmt <|-- CJMP
    Stmt <|-- ReturnVal
    Stmt <|-- FuncEntryLabel
    Stmt <|-- ExprStmt
    
    VarSlot <|-- FrameSlot
    VarSlot <|-- OperandSlot
    
    ConstVal <|-- ImmValue
    
    class IRVisitor {
        +visit(node)
    }
```

### 3.4 控制流图模块 (Control Flow Graph)

#### 3.4.1 类结构图
```mermaid
classDiagram
    class BasicBlock {
        -stmts
        -predecessors
        -successors
        -id
        +addStmt(stmt)
        +addPredecessor(block)
        +addSuccessor(block)
        +getStmts()
        +getPredecessors()
        +getSuccessors()
    }
    
    class CFG {
        -blocks
        -entry
        -exit
        +addBlock(block)
        +getBlock(id)
        +getEntryBlock()
        +getExitBlock()
        +getBlocks()
    }
    
    class LinearIRBlock {
        -stmts
        -ord
        +getStmts()
        +getOrd()
    }
    
    class IFlowOptimizer {
        <<interface>>
        +optimize(cfg)
    }
    
    class ControlFlowAnalysis {
        +analyze(cfg)
        +buildControlFlow(cfg)
    }
    
    class LivenessAnalysis {
        +analyze(cfg)
        +computeLiveVars(block)
    }
    
    class CFGBuilder {
        +build(ir)
        +createBasicBlocks(stmts)
        +connectBlocks(blocks)
    }
    
    class Loc {
        -line
        -column
        +getLine()
        +getColumn()
    }
    
    class IOrdIdentity {
        <<interface>>
        +getOrd()
    }
    
    BasicBlock --> CFG
    LinearIRBlock --> BasicBlock
    IFlowOptimizer <|.. ControlFlowAnalysis
    IFlowOptimizer <|.. LivenessAnalysis
    CFGBuilder --> BasicBlock
    CFGBuilder --> CFG
```

### 3.5 代码生成模块 (Code Generation)

#### 3.5.1 类结构图
```mermaid
classDiagram
    class CymbolAssembler {
        -cmdBuffer
        -operatorEmitter
        -currentFunc
        +visit(node)
        +emit(cmd)
        +getInstructions()
    }
    
    class IOperatorEmitter {
        <<interface>>
        +emitBinaryOp(op)
        +emitUnaryOp(op)
        +emitLoad(slot)
        +emitStore(slot)
    }
    
    class CymbolVMIOperatorEmitter {
        +emitBinaryOp(op)
        +emitUnaryOp(op)
        +emitLoad(slot)
        +emitStore(slot)
    }
    
    class FuncEntryLabel {
        -funcName
        +getFuncName()
    }
    
    IOperatorEmitter <|.. CymbolVMIOperatorEmitter
    CymbolAssembler --> IOperatorEmitter
    CymbolAssembler --> IRNode
    FuncEntryLabel --|> Stmt
```

### 3.6 调试模块 (Debug)

#### 3.6.1 类结构图
```mermaid
classDiagram
    class Dumpable {
        <<interface>>
        +dump(dumper)
    }
    
    class Dumper {
        -indent
        -output
        +visit(node)
        +println(text)
        +increaseIndent()
        +decreaseIndent()
    }
    
    Dumpable <|.. ASTNode
    Dumpable <|.. IRNode
    Dumpable <|.. CFG
    
    Dumper --> Dumpable
```

### 3.7 错误处理模块 (Error Handling)

#### 3.7.1 类结构图
```mermaid
classDiagram
    class CymbalError {
        -message
        -location
        -phase
        +getMessage()
        +getLocation()
        +getPhase()
    }
    
    class ErrorIssuer {
        -errors
        +issueError(error)
        +hasErrors()
        +getErrors()
        +reportErrors()
    }
    
    class Phase {
        <<enumeration>>
        AST
        IR
        CFG
        ASM
    }
    
    CymbalError --> Location
    CymbalError --> Phase
    ErrorIssuer --> CymbalError
```

## 4. 编译流程

### 4.1 整体流程图
```mermaid
graph TD
    A[源代码] --> B[词法分析]
    B --> C[语法分析]
    C --> D[AST构建]
    D --> E[符号表构建]
    E --> F[语义分析]
    F --> G[IR生成]
    G --> H[CFG构建]
    H --> I[优化]
    I --> J[代码生成]
    J --> K[虚拟机指令]
```

### 4.2 各阶段详细说明

#### 4.2.1 词法分析和语法分析
- 使用ANTLR4生成词法分析器和语法分析器
- 输入：Cymbol源代码
- 输出：解析树(Parse Tree)
- 关键类：`CymbolLexer`, `CymbolParser`

#### 4.2.2 AST构建
- 类：`CymbolASTBuilder`
- 输入：解析树
- 输出：抽象语法树(AST)
- 实现：访问者模式遍历解析树

#### 4.2.3 符号表构建
- 类：`LocalDefine`
- 输入：AST
- 输出：符号表
- 功能：变量和函数声明的符号注册

#### 4.2.4 语义分析
- 类：`TypeChecker`
- 输入：AST和符号表
- 输出：类型检查结果
- 功能：类型推导和兼容性检查

#### 4.2.5 IR生成
- 类：`CymbolIRBuilder`
- 输入：AST
- 输出：中间表示(IR)
- 特性：地址化表示，三地址码

#### 4.2.6 CFG构建
- 类：`CFGBuilder`
- 输入：IR
- 输出：控制流图(CFG)
- 功能：基本块划分和控制流边建立

#### 4.2.7 优化
- 类：`ControlFlowAnalysis`, `LivenessAnalysis`
- 输入：CFG
- 输出：优化后的CFG
- 优化：跳转优化、空块消除

#### 4.2.8 代码生成
- 类：`CymbolAssembler`
- 输入：优化后的IR
- 输出：虚拟机指令
- 特性：栈式虚拟机指令生成

## 5. 设计模式应用

### 5.1 访问者模式
- 应用于AST、IR等节点的遍历处理
- 优点：解耦数据结构和操作
- 实现：`ASTVisitor`, `IRVisitor`

### 5.2 工厂模式
- 应用于IR节点的创建
- 优点：统一创建接口，便于扩展
- 实现：`IRBuilder`中的节点创建方法

### 5.3 策略模式
- 应用于操作符发射器
- 优点：支持不同的代码生成策略
- 实现：`IOperatorEmitter`接口

### 5.4 单例模式
- 应用于类型表和符号表
- 优点：全局唯一实例
- 实现：`TypeTable`, `SymbolTable`

## 6. 测试架构

### 6.1 测试层次
```
测试/
├── 单元测试/              # 类级别测试
│   ├── ast/              # AST节点测试
│   ├── ir/               # IR节点测试
│   └── pass/             # 编译阶段测试
├── 集成测试/              # 模块间交互测试
└── 端到端测试/            # 完整编译流程测试
```

### 6.2 测试策略
- 使用JUnit 5作为测试框架
- 使用Mockito进行模拟对象测试
- 使用AssertJ进行断言
- 参数化测试验证多种输入情况

## 7. 性能优化

### 7.1 内存优化
- 使用对象池减少对象创建开销
- 及时释放不需要的对象引用
- 避免不必要的中间对象创建

### 7.2 算法优化
- 使用高效的图算法进行CFG构建和优化
- 使用增量更新减少重复计算
- 缓存频繁访问的数据

## 8. 扩展性设计

### 8.1 插件化架构
- 通过接口定义扩展点
- 支持自定义优化器和代码生成器
- 模块间松耦合

### 8.2 配置化
- 通过配置文件控制编译器行为
- 支持不同的目标平台
- 可配置的优化级别

## 9. 错误处理

### 9.1 错误分类
- 词法错误
- 语法错误
- 语义错误
- 类型错误
- 运行时错误

### 9.2 错误报告
- 提供详细的错误位置信息
- 支持多错误同时报告
- 友好的错误信息提示

## 10. 调试支持

### 10.1 调试接口
- 提供AST、IR、CFG的可视化接口
- 支持中间结果的输出和检查
- 实现`Dumpable`接口用于调试输出

### 10.2 日志系统
- 使用SLF4J进行日志记录
- 支持不同级别的日志输出
- 可配置的日志格式和输出目标

## 11. 部署和运行

### 11.1 构建工具
- 使用Maven进行项目构建
- 支持依赖管理和打包
- 集成测试和部署流程

### 11.2 运行环境
- Java 11+运行环境
- ANTLR4运行时库
- 虚拟机执行环境

## 12. 文档和维护

### 12.1 文档结构
- 架构文档：整体设计和模块说明
- API文档：公共接口和类说明
- 用户手册：使用指南和示例
- 开发文档：贡献指南和开发流程

### 12.2 维护策略
- 版本控制和发布管理
- 自动化测试和持续集成
- 代码审查和质量保证
- 性能监控和优化