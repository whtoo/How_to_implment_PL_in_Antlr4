# 设计 & 构造解析

## EP19 概述

EP19 实现了一个名为 Cymbol 的编程语言，它是一种类 C 语言，支持结构体、类型定义、函数和基本控制流结构。编译器采用多遍设计，包括词法分析、语法分析、符号定义、符号解析、类型检查和解释执行。

### 编译流程

``` mermaid
flowchart LR
    Input --> CharStream 
    CharStream --> Tokens 
    Tokens --> ParserTree
    ParserTree --LocalDefine--> AnnotatedParserTree
    AnnotatedParserTree --LocalResolver--> TypedParserTree 
    TypedParserTree --TypeCheck--> VerifiedParserTree
    VerifiedParserTree --> Interpreter
```

### 主要组件

EP19 的主要组件包括：

1. **词法分析器和语法分析器**：使用 ANTLR4 生成，基于 Cymbol.g4 语法文件
2. **符号表和作用域系统**：管理变量、函数和类型的定义和查找
3. **类型系统**：支持基本类型、结构体和类型别名
4. **编译器通道**：多个访问器实现不同的编译阶段
5. **解释器**：执行经过验证的 AST
6. **Git 自动提交和推送**：编译完成后自动提交并推送更改到 Git 仓库

### 语言特性

Cymbol 语言支持以下特性：

- 基本类型：int, float, bool, char, string, void
- 变量声明和赋值
- 函数定义和调用
- 结构体定义和实例化
- 结构体成员方法
- 类型别名（typedef）
- 控制流语句（if-else, while）
- 内置函数（如 print）

## 符号表和作用域系统

``` plantuml
@startuml

interface Type {
    + String getName()
}

interface Scope {
    + String getScopeName()
    + Scope getEnclosingScope()
    + void define(Symbol sym)
    + Symbol resolve(String name)
    + Type lookup(String name)
}

abstract class BaseScope implements Scope {
    # Scope enclosingScope
    # Map<String,Symbol> symbols
    + void define(Symbol sym)
    + Symbol resolve(String name)
}

class GlobalScope extends BaseScope {
}

class LocalScope extends BaseScope {
}

class Symbol {
    + String name
    + Type type
    + Scope scope
}

class VariableSymbol extends Symbol {
}

class MethodSymbol extends ScopedSymbol {
    + BlockContext blockStmt
    + boolean builtin
}

class StructSymbol extends ScopedSymbol implements Type {
    + Map<String, Symbol> fields
    + Map<String, MethodSymbol> methods
    + void addField(VariableSymbol field)
    + void addMethod(MethodSymbol method)
    + Symbol resolveMember(String name)
}

class TypedefSymbol extends Symbol implements Type {
    - Type targetType
    + Type getTargetType()
    + void setTargetType(Type type)
}

abstract class ScopedSymbol extends Symbol implements Scope {
    # Map<String, Symbol> members
}

BaseScope o-- Symbol : contains
Symbol o-- Type : has type
Symbol o-- Scope : defined in
ScopedSymbol o-- Symbol : contains

note left of BaseScope
    作用域对象，管理符号表
    1. 存储符号定义
    2. 提供符号查找
    3. 维护作用域层次结构
end note

note right of Symbol
    符号对象
    1. 表示语法对象的符号表示
    2. 关联到定义作用域
    3. 关联到类型信息
end note

@enduml
```

## 编译器通道

EP19 使用访问者模式实现多遍编译。每个通道都是一个访问者，处理 AST 的不同方面：

``` plantuml
@startuml

abstract class CymbolASTVisitor<T> {
    + visit(ParseTree tree): T
}

class LocalDefine extends CymbolASTVisitor<Object> {
    - ParseTreeProperty<Scope> scopes
    - Scope currentScope
    + ParseTreeProperty<Scope> getScopes()
}

class LocalResolver extends CymbolASTVisitor<Object> {
    - ScopeUtil scopes
    + ParseTreeProperty<Type> types
}

class TypeCheckVisitor extends CymbolASTVisitor<Type> {
    - ScopeUtil scopeUtil
    - ParseTreeProperty<Type> types
}

class Interpreter extends CymbolBaseVisitor<Object> {
    - ScopeUtil scopes
    - Stack<MemorySpace> memoryStack
    - MemorySpace currentSpace
    + void interpret(ParseTree context)
}

LocalDefine --> LocalResolver : provides scopes
LocalResolver --> TypeCheckVisitor : provides types
TypeCheckVisitor --> Interpreter : provides verified AST

@enduml
```

## 运行时系统

运行时系统负责管理内存和执行代码：

``` plantuml
@startuml

class MemorySpace {
    - Map<String, Object> memory
    - String name
    - MemorySpace enclosingSpace
    + void define(String name, Object value)
    + Object get(String name)
    + void update(String name, Object value)
}

class FunctionSpace extends MemorySpace {
    - MethodSymbol def
}

class StructInstance extends MemorySpace {
    - StructSymbol symbol
    + Object getField(String fieldName)
    + void setField(String fieldName, Object value)
    + boolean hasField(String fieldName)
    + MethodSymbol getMethod(String methodName)
}

class ReturnValue {
    + Object value
}

MemorySpace <|-- FunctionSpace
MemorySpace <|-- StructInstance
MemorySpace o-- MemorySpace : enclosing space

@enduml
```

## 类型系统

EP19 实现了一个静态类型系统，支持基本类型、结构体和类型别名：

``` plantuml
@startuml

interface Type {
    + String getName()
}

class TypeTable {
    + {static} Type INT
    + {static} Type FLOAT
    + {static} Type BOOLEAN
    + {static} Type CHAR
    + {static} Type STRING
    + {static} Type VOID
    + {static} Type NULL
    + {static} Type OBJECT
    + {static} Type getTypeByName(String name)
}

class TypeChecker {
    + {static} boolean checkAssignmentCompatibility(Type lhs, Type rhs, ParserRuleContext ctx)
    + {static} Type checkBinaryOperationCompatibility(Type left, Type right, String operator, ParserRuleContext ctx)
    + {static} Type checkUnaryOperationCompatibility(Type operand, String operator, ParserRuleContext ctx)
    + {static} void checkFunctionCallCompatibility(Type[] paramTypes, Type[] argTypes, ParserRuleContext ctx)
}

class StructSymbol implements Type {
}

class TypedefSymbol implements Type {
    - Type targetType
}

TypeChecker --> Type : uses
TypeTable --> Type : provides

@enduml
```

## 执行机制

EP19 使用解释器直接执行 AST，而不是生成中间代码或目标代码。解释器维护一个内存空间栈来处理变量作用域和函数调用：

1. **表达式求值**：直接在 AST 上计算表达式的值
2. **变量管理**：使用 MemorySpace 存储和检索变量值
3. **函数调用**：为每个函数调用创建新的 FunctionSpace
4. **结构体**：使用 StructInstance 表示结构体实例
5. **控制流**：通过条件评估和递归访问实现

解释器使用异常机制处理函数返回，通过抛出 ReturnValue 对象从函数中返回值。

## 总结

EP19 实现了一个功能完整的 Cymbol 语言编译器和解释器，展示了编译器设计的关键概念，包括词法分析、语法分析、符号表管理、类型检查和解释执行。它支持结构体、函数、类型别名和基本控制流结构，为理解编译器和解释器的工作原理提供了一个很好的示例。
