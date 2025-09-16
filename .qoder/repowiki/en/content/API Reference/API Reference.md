# API Reference

<cite>
**Referenced Files in This Document**   
- [Compiler.java](file://ep14/src/main/java/org/teachfx/antlr4/ep14/Compiler.java)
- [Compiler.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/Compiler.java)
- [Compiler.java](file://ep17/src/main/java/org/teachfx/antlr4/ep17/Compiler.java)
- [Compiler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/Compiler.java)
- [Compiler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java)
- [Compiler.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/Compiler.java)
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java)
- [CymbolASTBuilder.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/ast/CymbolASTBuilder.java)
- [LocalDefine.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalDefine.java)
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java)
- [LocalDefine.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/symtab/LocalDefine.java)
- [LocalDefine.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/symtab/LocalDefine.java)
- [TypeChecker.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/TypeChecker.java)
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeChecker.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/sematic/TypeChecker.java)
- [CymbolAssembler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/CymbolAssembler.java)
- [CymbolAssembler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java)
- [CymbolAssembler.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/codegen/CymbolAssembler.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Compiler Class Overview](#compiler-class-overview)
3. [Compilation Phases](#compilation-phases)
   - [AST Construction](#ast-construction)
   - [Symbol Resolution](#symbol-resolution)
   - [Type Checking](#type-checking)
   - [Code Generation](#code-generation)
4. [Configuration and Builder Patterns](#configuration-and-builder-patterns)
5. [Usage Examples](#usage-examples)
6. [Thread Safety and Lifecycle Management](#thread-safety-and-lifecycle-management)
7. [Version Compatibility](#version-compatibility)

## Introduction
This document provides comprehensive API documentation for the compiler framework's public interfaces. It details the Compiler class and its public methods for programmatic access to compilation functionality. The documentation covers all major compilation phases including AST construction, symbol resolution, type checking, and code generation. This reference is designed for developers who want to integrate the compiler as a library in their applications.

## Compiler Class Overview

The Compiler class serves as the main entry point for the compilation process. It orchestrates the various compilation phases and provides a unified interface for programmatic access to compilation functionality. The class follows a builder pattern for configuration and supports customization of the compilation workflow.

**Section sources**
- [Compiler.java](file://ep14/src/main/java/org/teachfx/antlr4/ep14/Compiler.java)
- [Compiler.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/Compiler.java)
- [Compiler.java](file://ep17/src/main/java/org/teachfx/antlr4/ep17/Compiler.java)
- [Compiler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/Compiler.java)
- [Compiler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java)
- [Compiler.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/Compiler.java)

## Compilation Phases

### AST Construction

The AST construction phase is handled by the CymbolASTBuilder class, which transforms the parse tree into an Abstract Syntax Tree (AST). The builder implements the visitor pattern to traverse the parse tree and construct corresponding AST nodes.

```mermaid
classDiagram
class CymbolASTBuilder {
+CymbolASTBuilder()
+static CymbolASTBuilder build(CymbolParser.CompilationUnitContext ctx)
+ASTNode visitCompilationUnit(CymbolParser.CompilationUnitContext ctx)
+ASTNode visitVarDecl(CymbolParser.VarDeclContext ctx)
+ASTNode visitFunctionDecl(CymbolParser.FunctionDeclContext ctx)
+ASTNode visitBlock(CymbolParser.BlockContext ctx)
+ASTNode visitExprBinary(CymbolParser.ExprBinaryContext ctx)
+ASTNode visitExprUnary(CymbolParser.ExprUnaryContext ctx)
+ASTNode visitExprFuncCall(CymbolParser.ExprFuncCallContext ctx)
}
class ASTNode {
+ParserRuleContext ctx
+accept(ASTVisitor visitor)
}
class CompileUnit {
+VarDeclNode[] varDecls
+FuncDeclNode[] funcDecls
+TypedefDeclNode[] typedefDecls
+StructDeclNode[] structDecls
}
class VarDeclNode {
+VariableSymbol symbol
+ExprNode initializer
+IDExprNode idExprNode
}
class FuncDeclNode {
+TypeNode retType
+String funcName
+VarDeclListNode params
+BlockStmtNode body
}
class ExprNode {
+ParserRuleContext ctx
}
class BinaryExprNode {
+BinaryOpType operator
+ExprNode lhs
+ExprNode rhs
}
class UnaryExprNode {
+UnaryOpType operator
+ExprNode operand
}
class CallFuncNode {
+String funcName
+ExprNode[] args
}
CymbolASTBuilder --> ASTNode : "creates"
CompileUnit --> VarDeclNode : "contains"
CompileUnit --> FuncDeclNode : "contains"
ExprNode <|-- BinaryExprNode : "extends"
ExprNode <|-- UnaryExprNode : "extends"
ExprNode <|-- CallFuncNode : "extends"
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java)
- [CymbolASTBuilder.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/ast/CymbolASTBuilder.java)

**Section sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java)
- [CymbolASTBuilder.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/ast/CymbolASTBuilder.java)

### Symbol Resolution

The symbol resolution phase is implemented by the LocalDefine class, which establishes symbol tables and resolves references between symbols. This phase creates a hierarchical scope structure and binds identifiers to their corresponding symbol definitions.

```mermaid
classDiagram
class LocalDefine {
+ParseTreeProperty~Scope~ scopes
+Scope currentScope
+Deque~Scope~ scopeStack
+LocalDefine()
+ParseTreeProperty~Scope~ getScopes()
+Object visitVarDecl(VarDeclContext ctx)
+Object visitFunctionDecl(FunctionDeclContext ctx)
+Object visitBlock(BlockContext ctx)
+Object visitStructDecl(StructDeclContext ctx)
+void stashScope(ParserRuleContext ctx)
+void pushScope(Scope scope)
+void popScope()
}
class Scope {
+String name
+Scope enclosingScope
+Map~String, Symbol~ symbols
+define(Symbol symbol)
+resolve(String name)
}
class GlobalScope {
+GlobalScope()
}
class LocalScope {
+LocalScope(Scope parent)
}
class Symbol {
+String name
+Type type
+Scope scope
}
class VariableSymbol {
+VariableSymbol(String name, Type type)
}
class MethodSymbol {
+MethodSymbol(String name, Scope scope, ParserRuleContext ctx)
+Symbol[] parameters
+Type returnType
}
class StructSymbol {
+StructSymbol(String name, Scope scope, ParserRuleContext ctx)
+Map~String, VariableSymbol~ fields
+Map~String, MethodSymbol~ methods
}
LocalDefine --> Scope : "manages"
Scope <|-- GlobalScope : "extends"
Scope <|-- LocalScope : "extends"
Scope --> Symbol : "contains"
Symbol <|-- VariableSymbol : "extends"
Symbol <|-- MethodSymbol : "extends"
Symbol <|-- StructSymbol : "extends"
```

**Diagram sources**
- [LocalDefine.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalDefine.java)
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java)
- [LocalDefine.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/symtab/LocalDefine.java)
- [LocalDefine.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/symtab/LocalDefine.java)

**Section sources**
- [LocalDefine.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/visitor/LocalDefine.java)
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java)
- [LocalDefine.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/symtab/LocalDefine.java)
- [LocalDefine.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/symtab/LocalDefine.java)

### Type Checking

The type checking phase validates type compatibility across the program. The TypeChecker class implements static type checking rules for assignments, expressions, function calls, and other language constructs. It ensures type safety and reports type errors during compilation.

```mermaid
classDiagram
class TypeChecker {
+static boolean checkAssignmentCompatibility(Type lhsType, Type rhsType, ParserRuleContext ctx)
+static Type checkBinaryOperationCompatibility(Type leftType, Type rightType, String operator, ParserRuleContext ctx)
+static Type checkUnaryOperationCompatibility(Type operandType, String operator, ParserRuleContext ctx)
+static boolean checkFunctionCallCompatibility(Type[] paramTypes, Type[] argTypes, ParserRuleContext ctx)
+static Type checkStructFieldAccess(Type structType, String fieldName, ParserRuleContext ctx)
+static Type checkStructMethodCall(Type structType, String methodName, Type[] argTypes, ParserRuleContext ctx)
+static Type resolveToActualType(Type type)
}
class Type {
+String name
+boolean isPrimitive()
}
class TypeTable {
+static Type INT
+static Type FLOAT
+static Type BOOLEAN
+static Type STRING
+static Type OBJECT
+static Type VOID
+static Type NULL
+static Type getTypeByName(String name)
}
class TypedefSymbol {
+Type target
+Type getTargetType()
}
class StructSymbol {
+Map~String, VariableSymbol~ fields
+Map~String, MethodSymbol~ methods
}
TypeChecker --> Type : "uses"
TypeChecker --> TypeTable : "references"
TypeChecker --> TypedefSymbol : "resolves"
TypeChecker --> StructSymbol : "validates"
Type <|-- TypedefSymbol : "extends"
Type <|-- StructSymbol : "extends"
```

**Diagram sources**
- [TypeChecker.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/TypeChecker.java)
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeChecker.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/sematic/TypeChecker.java)

**Section sources**
- [TypeChecker.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/TypeChecker.java)
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeChecker.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/sematic/TypeChecker.java)

### Code Generation

The code generation phase transforms the typed AST into executable bytecode. The CymbolAssembler class implements the code generation logic, translating high-level language constructs into stack-based virtual machine instructions.

```mermaid
classDiagram
class CymbolAssembler {
+LinkedList~String~ assembleCmdBuffer
+IOperatorEmitter operatorEmitter
+String getAsmInfo()
+Void visit(IRNode[] linearInstrs)
+Void visit(BinExpr node)
+Void visit(UnaryExpr node)
+Void visit(CallFunc callFunc)
+Void visit(Label label)
+Void visit(JMP jmp)
+Void visit(CJMP cjmp)
+Void visit(Assign assign)
+Void visit(ReturnVal returnVal)
+Void visit(FrameSlot frameSlot)
+Void visit(ConstVal~T~ tConstVal)
+void emit(String cmd)
}
class IOperatorEmitter {
+String emitBinaryOp(BinaryOpType op)
+String emitUnaryOp(UnaryOpType op)
}
class CymbolVMIOperatorEmitter {
+String emitBinaryOp(BinaryOpType op)
+String emitUnaryOp(UnaryOpType op)
}
class IRNode {
+accept(IRVisitor visitor)
}
class Expr {
+accept(IRVisitor visitor)
}
class Stmt {
+accept(IRVisitor visitor)
}
class BinExpr {
+BinaryOpType getOpType()
+Expr getLhs()
+Expr getRhs()
}
class UnaryExpr {
+UnaryOpType op
+Expr expr
}
class CallFunc {
+String getFuncName()
+boolean isBuiltIn()
}
class Assign {
+Operand getLhs()
+Expr getRhs()
}
class ReturnVal {
+Expr getRetVal()
+boolean isMainEntry()
}
CymbolAssembler --> IOperatorEmitter : "uses"
IOperatorEmitter <|-- CymbolVMIOperatorEmitter : "implements"
CymbolAssembler --> IRNode : "visits"
IRNode <|-- Expr : "extends"
IRNode <|-- Stmt : "extends"
Expr <|-- BinExpr : "extends"
Expr <|-- UnaryExpr : "extends"
Expr <|-- CallFunc : "extends"
Stmt <|-- Assign : "extends"
Stmt <|-- ReturnVal : "extends"
```

**Diagram sources**
- [CymbolAssembler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/CymbolAssembler.java)
- [CymbolAssembler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java)
- [CymbolAssembler.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/codegen/CymbolAssembler.java)

**Section sources**
- [CymbolAssembler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/CymbolAssembler.java)
- [CymbolAssembler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java)
- [CymbolAssembler.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/pass/codegen/CymbolAssembler.java)

## Configuration and Builder Patterns

The compiler framework provides flexible configuration options through builder patterns. Developers can customize the compilation workflow by configuring various aspects of the compilation process, including optimization levels, debugging information, and target platform settings.

**Section sources**
- [Compiler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/Compiler.java)
- [Compiler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java)
- [Compiler.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/Compiler.java)

## Usage Examples

The following examples demonstrate how to integrate the compiler as a library and use its public APIs for programmatic compilation.

```mermaid
sequenceDiagram
participant Application as "Application"
participant Compiler as "Compiler"
participant ASTBuilder as "CymbolASTBuilder"
participant SymbolResolver as "LocalDefine"
participant TypeChecker as "TypeChecker"
participant CodeGenerator as "CymbolAssembler"
participant VM as "Virtual Machine"
Application->>Compiler : compile(sourceCode)
Compiler->>ASTBuilder : buildAST(parseTree)
ASTBuilder-->>Compiler : AST
Compiler->>SymbolResolver : resolveSymbols(AST)
SymbolResolver-->>Compiler : Scoped AST
Compiler->>TypeChecker : checkTypes(scopedAST)
TypeChecker-->>Compiler : Type-Checked AST
Compiler->>CodeGenerator : generateCode(typeCheckedAST)
CodeGenerator-->>Compiler : Bytecode
Compiler-->>Application : CompilationResult
Application->>VM : execute(bytecode)
VM-->>Application : ExecutionResult
```

**Diagram sources**
- [Compiler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java)
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java)
- [LocalDefine.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/symtab/LocalDefine.java)
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [CymbolAssembler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java)

**Section sources**
- [Compiler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java)
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java)
- [LocalDefine.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/symtab/LocalDefine.java)
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [CymbolAssembler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java)

## Thread Safety and Lifecycle Management

The compiler components are designed with thread safety considerations in mind. Each compilation unit should use its own instance of the compiler to avoid concurrency issues. The lifecycle of compiler components follows a clear pattern from initialization to cleanup.

**Section sources**
- [Compiler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java)
- [Compiler.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/Compiler.java)

## Version Compatibility

The compiler framework maintains backward compatibility across versions. Deprecated APIs are marked with appropriate annotations and will be supported for at least two major releases before removal. Developers should consult the deprecation policy for migration guidance when upgrading between versions.

**Section sources**
- [Compiler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/Compiler.java)
- [Compiler.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java)
- [Compiler.java](file://ep21/src/main/java/org/teachfx/antlr4/ep21/Compiler.java)