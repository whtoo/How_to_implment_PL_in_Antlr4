# IR Expressions and Value Representation

<cite>
**Referenced Files in This Document**   
- [Expr.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/Expr.java)
- [Operand.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/Operand.java)
- [ImmValue.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/ImmValue.java)
- [ConstVal.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/val/ConstVal.java)
- [BinExpr.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/arith/BinExpr.java)
- [UnaryExpr.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/arith/UnaryExpr.java)
- [VarSlot.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/VarSlot.java)
- [OperandSlot.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/addr/OperandSlot.java)
- [IRVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRVisitor.java)
- [CymbolIRBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilder.java)
- [CymbolVMIOperatorEmitter.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolVMIOperatorEmitter.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Expr Class Hierarchy](#expr-class-hierarchy)
3. [Arithmetic Expressions](#arithmetic-expressions)
4. [Constant Value Representation](#constant-value-representation)
5. [Operand Interface and Implementations](#operand-interface-and-implementations)
6. [Expression Decomposition in IR Generation](#expression-decomposition-in-ir-generation)
7. [Type Checking in IR Phase](#type-checking-in-ir-phase)
8. [Temporary Storage and Result Referencing](#temporary-storage-and-result-referencing)
9. [Conclusion](#conclusion)

## Introduction
This document provides a comprehensive analysis of the Intermediate Representation (IR) expression system and value representation in the compiler implementation. It details how computational operations are represented in three-address code format, covering the class hierarchy, arithmetic expressions, constant values, operand handling, and the relationship between expression types and type checking. The documentation also explains how complex AST expressions are decomposed into sequences of IR expressions and how expression results are stored in temporaries for subsequent instruction referencing.

## Expr Class Hierarchy

The IR expression system is built around a hierarchical class structure that represents computational operations in three-address code. At the core of this hierarchy is the `Expr` abstract class, which serves as the base for all expression types in the IR. The `Expr` class extends `IRNode` and defines the visitor pattern interface through its abstract `accept` method, enabling polymorphic processing of different expression types.

The hierarchy follows a clear inheritance structure where specialized expression types derive from more general ones. The `Operand` class extends `Expr` and represents values that can be used as operands in computations. From `Operand`, the hierarchy branches into immediate values (`ImmValue`) and variable slots (`VarSlot`). This design enables type-safe handling of different kinds of values while maintaining a consistent interface for expression processing.

```mermaid
classDiagram
class Expr {
<<abstract>>
+accept(visitor) E
}
class Operand {
<<abstract>>
+accept(visitor) E
}
class ImmValue {
<<abstract>>
}
class VarSlot {
<<abstract>>
}
Expr <|-- Operand
Operand <|-- ImmValue
Operand <|-- VarSlot
ImmValue <|-- ConstVal
VarSlot <|-- FrameSlot
VarSlot <|-- OperandSlot
note right of Expr
Base class for all IR expressions
Implements visitor pattern
end note
note right of Operand
Represents values used as operands
Extends Expr for expression capabilities
end note
```

**Diagram sources**
- [Expr.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/Expr.java#L1-L9)
- [Operand.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/Operand.java#L1-L8)
- [ImmValue.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/ImmValue.java#L1-L6)
- [VarSlot.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/VarSlot.java)

**Section sources**
- [Expr.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/Expr.java#L1-L9)
- [Operand.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/Operand.java#L1-L8)

## Arithmetic Expressions

Arithmetic expressions in the IR system are represented by two main classes: `BinExpr` for binary operations and `UnaryExpr` for unary operations. These classes are located in the `arith` package and extend the base `Expr` class to represent computational operations in three-address code format.

The `BinExpr` class represents binary arithmetic operations with two operands and an operator. It contains fields for the left-hand side (`lhs`) and right-hand side (`rhs`) operands, both of type `VarSlot`, and an `opType` field of type `OperatorType.BinaryOpType` that specifies the operation to be performed. Supported binary operators include arithmetic operations (ADD, SUB, MUL, DIV, MOD), comparison operations (LT, LE, EQ, NE, GT, GE), and logical operations (AND, OR). The class provides getter and setter methods for all fields, as well as a static factory method `with` for convenient construction.

The `UnaryExpr` class represents unary operations with a single operand. It contains an `op` field of type `UnaryOpType` and an `expr` field of type `VarSlot` representing the operand. Supported unary operators include NEG (negation) and NOT (logical negation). Like `BinExpr`, it provides a static factory method `with` for construction.

```mermaid
classDiagram
class BinExpr {
-lhs : VarSlot
-rhs : VarSlot
-opType : BinaryOpType
+getLhs() VarSlot
+setLhs(VarSlot)
+getRhs() VarSlot
+setRhs(VarSlot)
+getOpType() BinaryOpType
+setOpType(BinaryOpType)
+toString() String
+accept(visitor) E
+with(opType, lhs, rhs) BinExpr
}
class UnaryExpr {
-op : UnaryOpType
-expr : VarSlot
+toString() String
+accept(visitor) E
+with(opType, operand) UnaryExpr
}
class OperatorType {
<<enumeration>>
BinaryOpType : ADD, SUB, MUL, DIV, MOD, LT, LE, EQ, NE, GT, GE, AND, OR
UnaryOpType : NEG, NOT
}
Expr <|-- BinExpr
Expr <|-- UnaryExpr
BinExpr --> OperatorType : "uses BinaryOpType"
UnaryExpr --> OperatorType : "uses UnaryOpType"
note right of BinExpr
Represents binary operations in three-address code
Stores operands as VarSlot references
end note
note right of UnaryExpr
Represents unary operations in three-address code
Single operand with unary operator
end note
```

**Diagram sources**
- [BinExpr.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/arith/BinExpr.java#L1-L59)
- [UnaryExpr.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/arith/UnaryExpr.java#L1-L30)
- [OperatorType.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/OperatorType.java)

**Section sources**
- [BinExpr.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/arith/BinExpr.java#L1-L59)
- [UnaryExpr.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/arith/UnaryExpr.java#L1-L30)

## Constant Value Representation

Constant values in the IR system are represented through the `ConstVal` and `ImmValue` classes, which provide a type-safe mechanism for embedding literal values directly in expressions. The `ImmValue` class serves as an abstract base class that extends `Operand`, indicating that immediate values can be used as operands in computations.

The `ConstVal` class is the concrete implementation that extends `ImmValue` and uses Java generics to maintain type safety. It contains a private field `val` of generic type `T` to store the constant value. The class provides a constructor for initialization and a static factory method `valueOf` for convenient creation of constant instances. The generic type parameter allows `ConstVal` to represent constants of various types including integers, booleans, strings, and other primitive types.

The `ConstVal` class implements the `accept` method from the visitor pattern, enabling it to be processed by IR visitors. It also overrides the `toString` method to provide a formatted string representation of the constant value, with special handling for different types (strings, integers, booleans) to ensure proper formatting in the generated IR code.

```mermaid
classDiagram
class ImmValue {
<<abstract>>
}
class ConstVal~T~ {
-val : T
+ConstVal(val)
+valueOf(val) ConstVal~T~
+getVal() T
+setVal(val)
+toString() String
+accept(visitor) E
}
Operand <|-- ImmValue
ImmValue <|-- ConstVal
note right of ImmValue
Abstract base for immediate values
Extends Operand for use in expressions
end note
note right of ConstVal
Generic class for constant values
Type-safe representation of literals
Special formatting for different types
end note
```

**Diagram sources**
- [ImmValue.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/ImmValue.java#L1-L6)
- [ConstVal.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/val/ConstVal.java#L1-L42)

**Section sources**
- [ImmValue.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/ImmValue.java#L1-L6)
- [ConstVal.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/val/ConstVal.java#L1-L42)

## Operand Interface and Implementations

The `Operand` interface and its implementations provide a flexible system for handling different types of operands in IR expressions. The `Operand` abstract class extends `Expr` and defines the contract for all operand types, requiring implementation of the `accept` method for visitor pattern processing.

Two primary implementations of the `VarSlot` abstract class serve as concrete operand types: `FrameSlot` and `OperandSlot`. `FrameSlot` represents variables stored in the function's stack frame at a specific offset, while `OperandSlot` represents temporary values generated during IR construction. The `OperandSlot` class includes a static counter `ordSeq` that generates unique identifiers for temporaries, ensuring that each temporary has a distinct name in the generated IR code.

The `OperandSlot` class provides several static methods for managing temporary values: `genTemp()` creates a new temporary with a unique identifier, `pushStack()` and `popStack()` manage a stack of temporaries (useful for expression evaluation), and `getOrdSeq()` returns the current sequence number. These methods enable efficient management of temporary storage during code generation.

```mermaid
classDiagram
class Operand {
<<abstract>>
+accept(visitor) E
}
class VarSlot {
<<abstract>>
+accept(visitor) E
}
class FrameSlot {
-offset : int
+FrameSlot(offset)
+toString() String
+accept(visitor) E
}
class OperandSlot {
-ord : int
-ordSeq : int
+genTemp() OperandSlot
+pushStack() OperandSlot
+popStack()
+getOrdSeq() int
+getOrd() int
+toString() String
+accept(visitor) E
}
Expr <|-- Operand
Operand <|-- VarSlot
VarSlot <|-- FrameSlot
VarSlot <|-- OperandSlot
note right of Operand
Base class for all operands in expressions
Extends Expr for expression capabilities
end note
note right of VarSlot
Abstract base for variable references
Common interface for different storage types
end note
note right of OperandSlot
Represents temporary values in IR
Automatically generates unique identifiers
Stack-based management for expression evaluation
end note
```

**Diagram sources**
- [Operand.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/Operand.java#L1-L8)
- [VarSlot.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/VarSlot.java)
- [FrameSlot.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/addr/FrameSlot.java)
- [OperandSlot.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/addr/OperandSlot.java#L1-L37)

**Section sources**
- [Operand.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/Operand.java#L1-L8)
- [OperandSlot.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/addr/OperandSlot.java#L1-L37)

## Expression Decomposition in IR Generation

Complex AST expressions are decomposed into sequences of IR expressions through the `CymbolIRBuilder` visitor class, which traverses the AST and generates corresponding three-address code. When visiting a `BinaryExprNode`, the builder first recursively processes the left-hand side expression, then the right-hand side expression, ensuring that any nested expressions are evaluated first and their results stored in temporaries.

For each binary operation, the builder creates a `BinExpr` instance with the appropriate operator type and operand slots. The result of the binary operation is stored in a new temporary, which is then pushed onto the evaluation stack for use by subsequent expressions. This decomposition process transforms complex nested expressions into a linear sequence of simple three-address instructions, making them easier to optimize and translate to target code.

The visitor pattern implementation in `IRVisitor` defines the interface for processing different IR node types, with specific `visit` methods for `BinExpr`, `UnaryExpr`, `ConstVal`, and other expression types. This allows different phases of the compiler (optimization, code generation) to process the IR expressions in a type-safe manner without needing instanceof checks.

```mermaid
sequenceDiagram
participant AST as AST Node
participant Builder as CymbolIRBuilder
participant IR as IR Expression
AST->>Builder : visit(BinaryExprNode)
activate Builder
Builder->>Builder : visit(lhs expression)
Builder->>Builder : get result from evaluation stack
Builder->>Builder : visit(rhs expression)
Builder->>Builder : get result from evaluation stack
Builder->>IR : Create BinExpr with operator and operands
Builder->>Builder : Store result in temporary
Builder->>Builder : Push result to evaluation stack
deactivate Builder
IR->>Builder : Return VarSlot reference
note right of Builder
Recursive processing ensures
proper evaluation order
end note
note right of IR
Three-address code generation
with temporary storage
end note
```

**Diagram sources**
- [CymbolIRBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilder.java#L97-L148)
- [IRVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRVisitor.java#L1-L40)

**Section sources**
- [CymbolIRBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilder.java#L97-L148)

## Type Checking in IR Phase

The relationship between expression types and type checking in the IR phase is managed through the integration of the type system with the IR generation process. While the IR expressions themselves are largely type-agnostic in their structure, the type information from the AST is preserved and used to ensure type safety during code generation.

The `OperatorType` enumeration defines the valid operations for different types, and the IR builder validates that operations are only applied to compatible operand types. For example, arithmetic operations are only permitted on numeric types, while logical operations are restricted to boolean operands. This type checking occurs during the IR generation phase, before optimization and code generation.

The `CymbolVMIOperatorEmitter` class demonstrates the connection between expression types and target code generation, with methods that map high-level operator types to specific virtual machine instructions. The `emitBinaryOp` and `emitUnaryOp` methods use switch statements on the operator type to generate appropriate instruction mnemonics, ensuring that the correct operations are emitted for the given types.

```mermaid
flowchart TD
A["AST Expression"] --> B["Type Checking"]
B --> C{"Valid Types?"}
C --> |Yes| D["Generate IR Expression"]
C --> |No| E["Report Type Error"]
D --> F["Store in Temporary"]
F --> G["Use in Subsequent Expressions"]
G --> H["Code Generation"]
H --> I["Target Instructions"]
style C fill:#f9f,stroke:#333,stroke-width:2px
style E fill:#f88,stroke:#333,stroke-width:2px
click C "Type checking ensures operation compatibility"
click E "Error reporting for type mismatches"
note right of B
Validates operand types
against operator requirements
end note
note right of D
Creates three-address code
with proper temporaries
end note
```

**Diagram sources**
- [CymbolVMIOperatorEmitter.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolVMIOperatorEmitter.java#L1-L63)
- [OperatorType.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/OperatorType.java)

**Section sources**
- [CymbolVMIOperatorEmitter.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolVMIOperatorEmitter.java#L1-L63)

## Temporary Storage and Result Referencing

Expression results are stored in temporaries and referenced by subsequent instructions through the `OperandSlot` mechanism, which provides automatic generation of unique temporary identifiers. When an expression is evaluated, its result is stored in a newly created `OperandSlot` with a unique ordinal number, ensuring that each temporary has a distinct name in the generated IR code.

The `CymbolIRBuilder` maintains an evaluation stack that tracks the results of subexpressions. When a subexpression is evaluated, its result (a `VarSlot`) is pushed onto this stack. Parent expressions then pop the required number of operands from the stack to use as inputs for their operations. This stack-based approach ensures proper evaluation order and enables efficient management of temporary storage.

The `addInstr` method in `CymbolIRBuilder` is responsible for adding IR instructions to the current basic block and returning the result slot. For expression instructions like `BinExpr` and `UnaryExpr`, the result is wrapped in an `Optional<VarSlot>` and can be pushed back onto the evaluation stack for use by subsequent expressions. This mechanism allows complex expressions to be decomposed into sequences of simple instructions while maintaining the correct data flow.

```mermaid
flowchart LR
A["Complex Expression"] --> B["Decompose into Subexpressions"]
B --> C["Evaluate Subexpression 1"]
C --> D["Store Result in t0"]
B --> E["Evaluate Subexpression 2"]
E --> F["Store Result in t1"]
D --> G["Execute Operation"]
F --> G
G --> H["Store Result in t2"]
H --> I["Use t2 in Next Instruction"]
style D fill:#bbf,stroke:#333,stroke-width:1px
style F fill:#bbf,stroke:#333,stroke-width:1px
style H fill:#bbf,stroke:#333,stroke-width:1px
subgraph "Temporary Storage"
D
F
H
end
note right of D
t0 = subexpression1
end note
note right of F
t1 = subexpression2
end note
note right of H
t2 = t0 op t1
end note
```

**Diagram sources**
- [CymbolIRBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilder.java#L97-L148)
- [OperandSlot.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/addr/OperandSlot.java#L1-L37)

**Section sources**
- [CymbolIRBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilder.java#L97-L148)

## Conclusion
The IR expression system provides a robust framework for representing computational operations in three-address code format. The hierarchical class structure centered around the `Expr` base class enables type-safe representation of various expression types, from simple constants to complex arithmetic operations. The system effectively handles constant values through the `ConstVal` and `ImmValue` classes, provides flexible operand handling via the `Operand` interface and its implementations, and supports comprehensive type checking during the IR generation phase.

The decomposition of complex AST expressions into sequences of IR expressions follows a systematic approach that ensures proper evaluation order and efficient use of temporary storage. The integration of the type system with IR generation guarantees type safety while enabling optimization and code generation. The temporary storage mechanism using `OperandSlot` with unique identifiers allows for clear representation of data flow in the generated code, making it easier to analyze and transform during subsequent compilation phases.

This well-structured IR system forms a critical foundation for the compiler's optimization and code generation capabilities, providing a clear separation between high-level language constructs and low-level machine operations while maintaining the necessary information for effective program analysis and transformation.