# Type System

<cite>
**Referenced Files in This Document**   
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java)
- [TypeTable.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/TypeTable.java)
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/type/TypeNode.java)
- [TypeSystemTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/TypeSystemTest.java)
- [TypeCheckerTest.java](file://ep20/src/test/java/org/teachfx/antlr4/ep20/pass/sematic/TypeCheckerTest.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Type Hierarchy and Built-in Types](#type-hierarchy-and-built-in-types)
3. [Composite Types: Arrays and Structs](#composite-types-arrays-and-structs)
4. [TypeTable: Type Definition and Lookup Management](#typetable-type-definition-and-lookup-management)
5. [Type Checking Algorithm](#type-checking-algorithm)
6. [Type Inference, Compatibility, and Coercion](#type-inference-compatibility-and-coercion)
7. [Type Checking Examples](#type-checking-examples)
8. [Function Signatures and Return Types](#function-signatures-and-return-types)
9. [Error Reporting for Type Mismatches](#error-reporting-for-type-mismatches)
10. [Type System Evolution Across Episodes](#type-system-evolution-across-episodes)

## Introduction
The type system in this compiler implementation provides a robust framework for static type checking, ensuring type safety and correctness in the language. It supports both primitive and composite types, manages type definitions through a centralized type table, and performs comprehensive type checking during semantic analysis. The system has evolved significantly across implementation episodes, expanding from basic type support to advanced features like arrays, structs, and function types.

## Type Hierarchy and Built-in Types

The type system is built around the `Type` interface, which defines the contract for all type representations in the compiler. Built-in types are predefined in the `TypeTable` class as static instances of `BuiltInTypeSymbol`, providing a centralized registry of fundamental types.

```mermaid
classDiagram
class Type {
<<interface>>
+String getName()
+boolean isPreDefined()
+boolean isFunc()
+Type getFuncType()
+Type getPrimitiveType()
+boolean isVoid()
}
class BuiltInTypeSymbol {
-String name
+BuiltInTypeSymbol(name)
+String getName()
+boolean isPreDefined()
+boolean isFunc()
+Type getFuncType()
+Type getPrimitiveType()
+boolean isVoid()
}
Type <|-- BuiltInTypeSymbol
class TypeTable {
+static BuiltInTypeSymbol INT
+static BuiltInTypeSymbol FLOAT
+static BuiltInTypeSymbol DOUBLE
+static BuiltInTypeSymbol CHAR
+static BuiltInTypeSymbol VOID
+static BuiltInTypeSymbol NULL
+static BuiltInTypeSymbol BOOLEAN
+static BuiltInTypeSymbol OBJECT
+static BuiltInTypeSymbol STRING
+static Integer TRUE
+static Integer FALSE
}
BuiltInTypeSymbol --> TypeTable : "instances"
```

**Diagram sources**
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java)
- [TypeTable.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/TypeTable.java)

**Section sources**
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java#L1-L14)
- [TypeTable.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/TypeTable.java#L1-L19)

## Composite Types: Arrays and Structs

The type system supports composite types through specialized type representations. Arrays are represented with dimension information, while structs are handled through struct type definitions that contain member type information.

```mermaid
classDiagram
class Type {
<<interface>>
+String getName()
+boolean isPreDefined()
+boolean isFunc()
+Type getFuncType()
+Type getPrimitiveType()
+boolean isVoid()
}
class ArrayType {
-Type elementType
-int dimensions
+ArrayType(elementType, dimensions)
+Type getElementType()
+int getDimensions()
+String getName()
+Type getPrimitiveType()
}
class StructType {
-String structName
-Map<String, Type> members
+StructType(name)
+void addMember(name, type)
+Type getMemberType(name)
+Set<String> getMemberNames()
+String getName()
+Type getPrimitiveType()
}
Type <|-- ArrayType
Type <|-- StructType
Type <|-- BuiltInTypeSymbol
class TypeNode {
-Type baseType
-int dim
+Type getBaseType()
+void setBaseType(type)
+int getDim()
+void setDim(dim)
}
TypeNode --> Type : "references"
```

**Diagram sources**
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java)
- [TypeNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/type/TypeNode.java)

**Section sources**
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java#L1-L14)
- [TypeNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/type/TypeNode.java#L1-L47)

## TypeTable: Type Definition and Lookup Management

The `TypeTable` class serves as a centralized registry for all type definitions in the system. It provides static references to built-in types and supports type lookup operations. The table is initialized with fundamental types and can be extended with user-defined types during compilation.

```mermaid
classDiagram
class TypeTable {
+static BuiltInTypeSymbol INT
+static BuiltInTypeSymbol FLOAT
+static BuiltInTypeSymbol DOUBLE
+static BuiltInTypeSymbol CHAR
+static BuiltInTypeSymbol VOID
+static BuiltInTypeSymbol NULL
+static BuiltInTypeSymbol BOOLEAN
+static BuiltInTypeSymbol OBJECT
+static BuiltInTypeSymbol STRING
+static Integer TRUE
+static Integer FALSE
}
class BuiltInTypeSymbol {
-String name
+BuiltInTypeSymbol(name)
+String getName()
+boolean isPreDefined()
+boolean isFunc()
+Type getFuncType()
+Type getPrimitiveType()
+boolean isVoid()
}
class Symbol {
-String name
-Type type
-Scope scope
+Symbol(name, type, scope)
+String getName()
+Type getType()
+Scope getScope()
}
class VariableSymbol {
+VariableSymbol(name, type, scope)
}
class MethodSymbol {
-Type returnType
-List<Type> paramTypes
+MethodSymbol(name, returnType, paramTypes, scope)
+Type getReturnType()
+List<Type> getParamTypes()
}
BuiltInTypeSymbol --> TypeTable : "instances"
Symbol --> Type : "has-a"
VariableSymbol --> Symbol : "extends"
MethodSymbol --> Symbol : "extends"
```

**Diagram sources**
- [TypeTable.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/TypeTable.java)
- [BuiltInTypeSymbol.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/BuiltInTypeSymbol.java)

**Section sources**
- [TypeTable.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/TypeTable.java#L1-L19)

## Type Checking Algorithm

The type checking algorithm is implemented in the `TypeChecker` class, which traverses the abstract syntax tree (AST) to verify type correctness. The algorithm uses a visitor pattern to process different AST node types and performs type checking for expressions, assignments, control flow statements, and function calls.

```mermaid
flowchart TD
Start([Type Checking Start]) --> VisitNode["Visit AST Node"]
VisitNode --> NodeType{"Node Type?"}
NodeType --> |Expression| CheckExpression["Check Expression Type"]
CheckExpression --> InferType["Infer Expression Type"]
InferType --> ValidateType["Validate Against Context"]
ValidateType --> ReturnExprType["Return Expression Type"]
NodeType --> |Assignment| CheckAssignment["Check Assignment"]
CheckAssignment --> GetLHS["Get Left-Hand Side Type"]
GetLHS --> GetRHS["Get Right-Hand Side Type"]
GetRHS --> CheckCompatibility["Check Type Compatibility"]
CheckCompatibility --> |Compatible| Continue["Continue Processing"]
CheckCompatibility --> |Incompatible| ReportError["Report Type Mismatch Error"]
NodeType --> |Function Call| CheckFunctionCall["Check Function Call"]
CheckFunctionCall --> ResolveFunction["Resolve Function Symbol"]
ResolveFunction --> CheckParams["Check Parameter Types"]
CheckParams --> |Match| Continue
CheckParams --> |Mismatch| ReportError
NodeType --> |Return Statement| CheckReturn["Check Return Statement"]
CheckReturn --> GetReturnType["Get Function Return Type"]
GetReturnType --> CheckValue["Check Return Value Type"]
CheckValue --> |Compatible| Continue
CheckValue --> |Incompatible| ReportError
ReturnExprType --> End([Type Checking Complete])
Continue --> End
ReportError --> End
```

**Diagram sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)

## Type Inference, Compatibility, and Coercion

The type system implements type inference for expressions, determining the resulting type based on operand types. Type compatibility rules define when types can be used interchangeably, and limited coercion is supported for certain type conversions.

```mermaid
graph TD
subgraph "Type Inference Rules"
A["int + int"] --> B["Result: int"]
C["float + int"] --> D["Result: float"]
E["int + float"] --> F["Result: float"]
G["String + any"] --> H["Result: String"]
I["bool op bool"] --> J["Result: bool"]
end
subgraph "Type Compatibility"
K["int"] --> L["Compatible with: float"]
M["float"] --> N["Compatible with: none (wider only)"]
O["bool"] --> P["Compatible with: bool only"]
Q["String"] --> R["Compatible with: String only"]
S["void"] --> T["Compatible with: no value"]
end
subgraph "Coercion Rules"
U["int to float"] --> V["Allowed: implicit"]
W["float to int"] --> X["Not allowed: potential data loss"]
Y["bool to int"] --> Z["Not allowed: different semantics"]
AA["String to any"] --> AB["Not allowed: one-way concatenation"]
end
B --> End
D --> End
F --> End
H --> End
J --> End
L --> End
N --> End
P --> End
R --> End
T --> End
V --> End
X --> End
Z --> End
AB --> End
```

**Diagram sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeSystemTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/TypeSystemTest.java)

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeSystemTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/TypeSystemTest.java#L1-L149)

## Type Checking Examples

The type system handles various type checking scenarios, including expression evaluation, assignments, and control flow statements. The examples below illustrate common type checking patterns.

```mermaid
sequenceDiagram
participant Source as "Source Code"
participant Parser as "Parser"
participant AST as "AST"
participant TypeChecker as "TypeChecker"
participant TypeTable as "TypeTable"
participant ErrorReporter as "Error Reporter"
Source->>Parser : int a; float b; a = 5; b = a;
Parser->>AST : Build AST
AST->>TypeChecker : Accept TypeChecker
TypeChecker->>TypeChecker : Visit varDecl 'a'
TypeChecker->>TypeTable : Lookup 'int' type
TypeChecker->>TypeChecker : Store type for 'a'
TypeChecker->>TypeChecker : Visit varDecl 'b'
TypeChecker->>TypeTable : Lookup 'float' type
TypeChecker->>TypeChecker : Store type for 'b'
TypeChecker->>TypeChecker : Visit assignment a=5
TypeChecker->>TypeChecker : Infer type of 5 as int
TypeChecker->>TypeChecker : Check compatibility (int = int)
TypeChecker->>TypeChecker : Success
TypeChecker->>TypeChecker : Visit assignment b=a
TypeChecker->>TypeChecker : Get type of 'a' as int
TypeChecker->>TypeChecker : Check compatibility (float = int)
TypeChecker->>TypeChecker : Allow implicit coercion
TypeChecker->>TypeChecker : Success
TypeChecker-->>Source : No errors
```

```mermaid
sequenceDiagram
participant Source as "Source Code"
participant Parser as "Parser"
participant AST as "AST"
participant TypeChecker as "TypeChecker"
participant TypeTable as "TypeTable"
participant ErrorReporter as "Error Reporter"
Source->>Parser : int a; float b; b = 5.0; a = b;
Parser->>AST : Build AST
AST->>TypeChecker : Accept TypeChecker
TypeChecker->>TypeChecker : Visit varDecl 'a'
TypeChecker->>TypeTable : Lookup 'int' type
TypeChecker->>TypeChecker : Store type for 'a'
TypeChecker->>TypeChecker : Visit varDecl 'b'
TypeChecker->>TypeTable : Lookup 'float' type
TypeChecker->>TypeChecker : Store type for 'b'
TypeChecker->>TypeChecker : Visit assignment b=5.0
TypeChecker->>TypeChecker : Infer type of 5.0 as float
TypeChecker->>TypeChecker : Check compatibility (float = float)
TypeChecker->>TypeChecker : Success
TypeChecker->>TypeChecker : Visit assignment a=b
TypeChecker->>TypeChecker : Get type of 'b' as float
TypeChecker->>TypeChecker : Check compatibility (int = float)
TypeChecker->>ErrorReporter : Report type mismatch error
ErrorReporter-->>Source : "类型不兼容 : 不能将 float 类型赋值给 int 类型"
```

**Diagram sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeSystemTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/TypeSystemTest.java)

**Section sources**
- [TypeSystemTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/TypeSystemTest.java#L1-L149)

## Function Signatures and Return Types

The type system handles function signatures by checking parameter types and return types. Function symbols store signature information, and calls are validated against these signatures.

```mermaid
classDiagram
class MethodSymbol {
-String name
-Type returnType
-List<Type> paramTypes
-Scope scope
+MethodSymbol(name, returnType, paramTypes, scope)
+Type getReturnType()
+List<Type> getParamTypes()
+boolean isCompatibleWith(argTypes)
+String getSignature()
}
class TypeChecker {
-ParseTreeProperty<Type> types
+visitFunctionDecl()
+visitFunctionCall()
+visitReturnStmt()
+checkTypeCompatibility()
+inferExpressionType()
}
class Type {
<<interface>>
+String getName()
+boolean isPreDefined()
+boolean isFunc()
+Type getFuncType()
+Type getPrimitiveType()
+boolean isVoid()
}
MethodSymbol --> Type : "returnType"
MethodSymbol --> Type : "paramTypes [*]"
TypeChecker --> MethodSymbol : "resolves"
TypeChecker --> Type : "uses"
TypeChecker --> ParseTreeProperty : "types"
```

**Diagram sources**
- [MethodSymbol.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/symbol/MethodSymbol.java)
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeSystemTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/TypeSystemTest.java#L1-L149)

## Error Reporting for Type Mismatches

Type mismatches are reported through the compiler's error reporting framework, providing descriptive messages that help users identify and fix type-related issues. Errors are integrated with the overall compilation process and include source location information.

```mermaid
flowchart TD
Start([Type Checking]) --> Operation["Perform Type Operation"]
Operation --> Compatible{"Types Compatible?"}
Compatible --> |Yes| Success["Continue Processing"]
Compatible --> |No| CreateError["Create Error Message"]
CreateError --> ClassifyError{"Error Type?"}
ClassifyError --> |Assignment| AssignmentError["'类型不兼容: 不能将 {srcType} 类型赋值给 {targetType} 类型'"]
ClassifyError --> |Function Call| CallError["'函数参数类型不匹配'"]
ClassifyError --> |Return Statement| ReturnError["'返回值类型 {actual} 与函数返回类型 {expected} 不兼容'"]
ClassifyError --> |Condition| ConditionError["'{stmt}条件表达式必须是布尔类型'"]
AssignmentError --> Report["Report to Error Issuer"]
CallError --> Report
ReturnError --> Report
ConditionError --> Report
Report --> AttachLocation["Attach Source Location"]
AttachLocation --> QueueError["Add to Error Queue"]
QueueError --> End([Error Reported])
Success --> End
```

**Diagram sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [CymbalError.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/error/CymbalError.java)

**Section sources**
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeSystemTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/TypeSystemTest.java#L1-L149)

## Type System Evolution Across Episodes

The type system has evolved significantly from earlier episodes to the current implementation, adding support for increasingly sophisticated type features and improving type checking accuracy.

```mermaid
graph LR
subgraph "Episode Progression"
EP14["ep14: Basic Type System"] --> EP16["ep16: Scope Integration"]
EP16 --> EP19["ep19: Comprehensive Type Checking"]
EP19 --> EP20["ep20: Advanced Composite Types"]
end
subgraph "ep14 Features"
EP14 --> BuiltIn["Built-in Types"]
EP14 --> TypeInterface["Type Interface"]
EP14 --> SymbolType["Symbol Type Field"]
end
subgraph "ep16 Enhancements"
EP16 --> ScopeIntegration["Scope Integration"]
EP16 --> LocalResolver["LocalResolver with Type Tracking"]
EP16 --> MethodSymbol["MethodSymbol with Return Type"]
end
subgraph "ep19 Improvements"
EP19 --> FullTypeChecker["Complete TypeChecker"]
EP19 --> ExpressionTypes["Expression Type Inference"]
EP19 --> AssignmentChecking["Assignment Compatibility"]
EP19 --> ControlFlow["Control Flow Type Checking"]
EP19 --> ReturnValidation["Return Statement Validation"]
EP19 --> ComprehensiveTests["Comprehensive Test Suite"]
end
subgraph "ep20 Advancements"
EP20 --> Arrays["Array Type Support"]
EP20 --> Structs["Struct Type Support"]
EP20 --> TypeAliases["Typedef Support"]
EP20 --> EnhancedCoercion["Improved Coercion Rules"]
EP20 --> BetterErrorMessages["Enhanced Error Reporting"]
end
```

**Diagram sources**
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java)
- [TypeTable.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/TypeTable.java)
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeSystemTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/TypeSystemTest.java)

**Section sources**
- [Type.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java#L1-L14)
- [TypeTable.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/TypeTable.java#L1-L19)
- [TypeChecker.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/TypeChecker.java)
- [TypeSystemTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/TypeSystemTest.java#L1-L149)