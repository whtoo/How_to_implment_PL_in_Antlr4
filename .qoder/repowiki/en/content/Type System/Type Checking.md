# Type Checking

<cite>
**Referenced Files in This Document**   
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [OperatorType.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/OperatorType.java)
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java)
- [TypeTable.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/TypeTable.java)
- [ArrayType.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/ArrayType.java)
- [StructType.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/StructType.java)
- [BuiltInTypeSymbol.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/BuiltInTypeSymbol.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Visitor-Based Type Checking Architecture](#visitor-based-type-checking-architecture)
3. [Core Type System Components](#core-type-system-components)
4. [Type Compatibility Rules](#type-compatibility-rules)
5. [Operator Type Signatures and Overloading](#operator-type-signatures-and-overloading)
6. [Expression Type Checking](#expression-type-checking)
7. [Function Call and Return Type Checking](#function-call-and-return-type-checking)
8. [Type Inference Mechanism](#type-inference-mechanism)
9. [Type Coercion Between Compatible Types](#type-coercion-between-compatible-types)
10. [Error Reporting for Type Mismatches](#error-reporting-for-type-mismatches)

## Introduction
This document provides a comprehensive analysis of the type checking algorithm implemented in the compiler framework. The system employs a visitor-based approach to traverse the Abstract Syntax Tree (AST) and verify type correctness throughout the program. The type checker ensures semantic validity by enforcing type compatibility rules for assignments, expressions, and function calls, while supporting type inference and coercion mechanisms. Built-in operators are defined with specific type signatures that support overloading based on operand types. The error reporting system provides precise source location information for type mismatches, enabling effective debugging.

## Visitor-Based Type Checking Architecture

The type checking system is implemented using the Visitor design pattern, which enables separation of type analysis logic from the AST structure. The `TypeChecker` class extends `ASTBaseVisitor` and implements type verification for each node type in the AST hierarchy.

```mermaid
classDiagram
class ASTBaseVisitor {
+visit(ASTNode node) Void
+visit(ExprNode node) Void
+visit(StmtNode node) Void
}
class TypeChecker {
+visit(IDExprNode node) Void
+visit(VarDeclNode node) Void
+visit(FuncDeclNode node) Void
+visit(BinaryExprNode node) Void
+visit(CallFuncNode node) Void
+visit(ReturnStmtNode node) Void
}
class ASTNode {
<<abstract>>
}
class ExprNode {
<<abstract>>
+setExprType(Type type) void
+getExprType() Type
}
class StmtNode {
<<abstract>>
}
ASTBaseVisitor <|-- TypeChecker
ASTNode <|-- ExprNode
ASTNode <|-- StmtNode
ExprNode <|-- BinaryExprNode
ExprNode <|-- CallFuncNode
ExprNode <|-- IDExprNode
StmtNode <|-- ReturnStmtNode
StmtNode <|-- VarDeclStmtNode
StmtNode <|-- AssignStmtNode
```

**Diagram sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)
- [ASTBaseVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/ASTBaseVisitor.java)

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)

## Core Type System Components

The type system is built around a hierarchy of type representations that define the semantic properties of values in the language. The core interface `Type` serves as the foundation for all type definitions, with implementations for built-in types, arrays, and structures.

```mermaid
classDiagram
class Type {
<<interface>>
+getName() String
+isPreDefined() boolean
+isFunc() boolean
+getFuncType() Type
+getPrimitiveType() Type
+isVoid() boolean
}
class BuiltInTypeSymbol {
+isPreDefined() boolean
+isBuiltIn() boolean
+isVoid() boolean
}
class ArrayType {
+elementType Type
+size int
+getElementType() Type
+getSize() int
}
class StructType {
+name String
+fields Map<String, Symbol>
+addField(name, field) void
+getField(name) Symbol
}
Type <|-- BuiltInTypeSymbol
Type <|-- ArrayType
Type <|-- StructType
BuiltInTypeSymbol ..> TypeTable : contains
StructType ..> Symbol : contains
```

**Diagram sources**
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java#L1-L15)
- [BuiltInTypeSymbol.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/BuiltInTypeSymbol.java#L1-L41)
- [ArrayType.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/ArrayType.java#L1-L54)
- [StructType.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/StructType.java#L1-L75)

**Section sources**
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java#L1-L15)
- [TypeTable.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/TypeTable.java#L1-L21)
- [BuiltInTypeSymbol.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/BuiltInTypeSymbol.java#L1-L41)

## Type Compatibility Rules

The type checking system enforces compatibility rules for various operations including assignments, expression evaluations, and function calls. These rules ensure that operations are performed on compatible types and prevent type errors.

```mermaid
flowchart TD
Start([Type Compatibility Check]) --> AssignmentCheck["Check Assignment Compatibility"]
AssignmentCheck --> ExprEvalCheck["Check Expression Evaluation"]
ExprEvalCheck --> FuncCallCheck["Check Function Call Arguments"]
FuncCallCheck --> ReturnCheck["Check Return Statement Types"]
AssignmentCheck --> |Variable Declaration| VarDeclRule["Variable type must match initializer type or be coercible"]
AssignmentCheck --> |Assignment Statement| AssignRule["LHS and RHS types must be compatible"]
ExprEvalCheck --> |Arithmetic| ArithmeticRule["Operands must be numeric types"]
ExprEvalCheck --> |Boolean Logic| BooleanRule["Operands must be boolean or coercible to boolean"]
FuncCallCheck --> |Argument Matching| ArgMatchRule["Each argument type must match parameter type"]
FuncCallCheck --> |Overloading| OverloadRule["Select function with best matching signature"]
ReturnCheck --> |Return Type| ReturnRule["Return expression type must match function return type"]
```

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java#L1-L15)

## Operator Type Signatures and Overloading

The `OperatorType` class defines type signatures for built-in operators and supports operator overloading through enum-based operator definitions. Each operator has specific type requirements for its operands.

```mermaid
classDiagram
class OperatorType {
<<class>>
}
class BinaryOpType {
+ADD("+")
+SUB("-")
+MUL("*")
+DIV("/")
+MOD("%")
+NE("!=")
+LT("<")
+LE("<=")
+EQ("==")
+GT(">")
+GE(">=")
+AND("&&")
+OR("||")
+getOpRawVal() String
+fromString(op) BinaryOpType
}
class UnaryOpType {
+NEG("-")
+NOT("!")
+getOpRawVal() String
+fromString(op) UnaryOpType
}
OperatorType --> BinaryOpType : contains
OperatorType --> UnaryOpType : contains
```

**Diagram sources**
- [OperatorType.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/OperatorType.java#L1-L60)

**Section sources**
- [OperatorType.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/OperatorType.java#L1-L60)
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)

## Expression Type Checking

The type checker analyzes expressions to ensure type correctness and determines the resulting type of each expression. This includes arithmetic expressions, boolean logic, and other operations.

```mermaid
sequenceDiagram
participant TC as TypeChecker
participant BE as BinaryExprNode
participant LHS as LeftOperand
participant RHS as RightOperand
participant OT as OperatorType
TC->>BE : visit(BinaryExprNode)
BE->>LHS : accept(type checker)
LHS-->>BE : type determined
BE->>RHS : accept(type checker)
RHS-->>BE : type determined
BE->>OT : get operator type
OT-->>BE : BinaryOpType
BE->>BE : check operand compatibility
BE->>BE : determine result type
BE-->>TC : expression type set
```

**Diagram sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)
- [BinaryExprNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/BinaryExprNode.java)

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)

## Function Call and Return Type Checking

The type checker verifies function calls by matching argument types with parameter types and ensures that return statements comply with the function's declared return type.

```mermaid
flowchart TD
Start([Function Call]) --> ResolveFunc["Resolve Function Symbol"]
ResolveFunc --> CheckParams["Check Parameter Types"]
CheckParams --> |Match| SelectFunc["Select Appropriate Function"]
CheckParams --> |No Match| Error["Type Mismatch Error"]
SelectFunc --> EvaluateArgs["Evaluate Argument Expressions"]
EvaluateArgs --> CheckCoercion["Check Type Coercion"]
CheckCoercion --> |Possible| CoerceTypes["Apply Type Coercion"]
CheckCoercion --> |Not Possible| Error
CoerceTypes --> CompleteCall["Complete Function Call"]
subgraph ReturnChecking
ReturnStart([Return Statement]) --> GetReturnType["Get Function Return Type"]
GetReturnType --> CheckExprType["Check Expression Type"]
CheckExprType --> |Compatible| SetReturn["Set Return Type"]
CheckExprType --> |Incompatible| ReturnError["Return Type Mismatch"]
end
```

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)
- [CallFuncNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/CallFuncNode.java)
- [ReturnStmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/ReturnStmtNode.java)

## Type Inference Mechanism

The type system includes a type inference mechanism that determines types for variable declarations and return types when they are not explicitly specified.

```mermaid
flowchart TD
Start([Variable Declaration]) --> HasInitializer["Has Initializer?"]
HasInitializer --> |Yes| InferFromInit["Infer Type from Initializer"]
HasInitializer --> |No| UseDeclaredType["Use Declared Type"]
InferFromInit --> CheckExprType["Analyze Expression Type"]
CheckExprType --> SetVarType["Set Variable Type"]
subgraph ReturnInference
FuncStart([Function Definition]) --> HasReturn["Has Return Statements?"]
HasReturn --> |Yes| AnalyzeReturns["Analyze All Return Expressions"]
AnalyzeReturns --> FindCommonType["Find Most Specific Common Type"]
FindCommonType --> SetReturnType["Set Function Return Type"]
HasReturn --> |No| CheckVoid["Check for Void Function"]
end
```

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)
- [VarDeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/VarDeclNode.java)

## Type Coercion Between Compatible Types

The type checker implements type coercion rules that allow automatic conversion between compatible types, such as numeric promotions and boolean conversions.

```mermaid
graph TD
A[int] --> |promote to| B[float]
B --> |promote to| C[double]
D[char] --> |promote to| A
E[boolean] --> |coerce to| F[int]
F --> |coerce to| B
style A fill:#f9f,stroke:#333
style B fill:#ff9,stroke:#333
style C fill:#9ff,stroke:#333
style D fill:#9f9,stroke:#333
style E fill:#f99,stroke:#333
style F fill:#99f,stroke:#333
note right of A
Implicit numeric
promotion rules
end note
note right of E
Boolean to int
coercion (0/1)
end note
```

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java#L1-L15)

## Error Reporting for Type Mismatches

The type checking system includes comprehensive error reporting that identifies type mismatches and provides precise source location information.

```mermaid
sequenceDiagram
participant TC as TypeChecker
participant Node as ASTNode
participant Error as ErrorIssuer
participant Loc as Location
TC->>Node : visit(node)
Node->>TC : type checking
alt Type Mismatch
TC->>Error : reportError()
Error->>Loc : getLocation(node)
Loc-->>Error : source position
Error->>Error : formatErrorMessage()
Error-->>TC : error reported
else Compatible Types
TC->>Node : setExprType()
end
TC-->>Caller : continue traversal
```

**Diagram sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)
- [ErrorIssuer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/ErrorIssuer.java)

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java#L1-L105)
- [ErrorIssuer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/ErrorIssuer.java)