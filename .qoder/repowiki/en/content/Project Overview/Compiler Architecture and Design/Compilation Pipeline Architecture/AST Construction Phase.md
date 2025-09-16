# AST Construction Phase

<cite>
**Referenced Files in This Document**   
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java)
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java)
- [ExprNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/ExprNode.java)
- [StmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/StmtNode.java)
- [DeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/DeclNode.java)
- [TypeNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/type/TypeNode.java)
- [CompileUnit.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/CompileUnit.java)
- [ASTVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTVisitor.java)
- [ASTBaseVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/ASTBaseVisitor.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [ASTNode Hierarchy](#astnode-hierarchy)
3. [AST Construction Process](#ast-construction-process)
4. [Expression Node Types](#expression-node-types)
5. [Statement Node Types](#statement-node-types)
6. [Declaration Node Types](#declaration-node-types)
7. [Source Location Preservation](#source-location-preservation)
8. [AST Benefits Over ParseTree](#ast-benefits-over-parsetree)
9. [AST Visitor Pattern Implementation](#ast-visitor-pattern-implementation)
10. [Code Generation Examples](#code-generation-examples)
11. [Future Transformations and Optimizations](#future-transformations-and-optimizations)

## Introduction
The AST construction phase transforms ANTLR4's ParseTree into a typed Abstract Syntax Tree (AST) using the visitor pattern. This process creates a structured representation of the source code that is optimized for semantic analysis and code generation. The CymbolASTBuilder.java class implements this transformation, visiting each node of the ParseTree and constructing corresponding AST nodes that preserve type information and source location data.

**Section sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)

## ASTNode Hierarchy
The AST node hierarchy is organized around a base ASTNode class that serves as the foundation for all AST elements. This hierarchy includes specialized node types for expressions, statements, declarations, and types, each inheriting from the base class and adding specific functionality.

```mermaid
classDiagram
class ASTNode {
+ParserRuleContext ctx
+Location getLocation()
+void accept(ASTVisitor visitor)
+void dump(Dumper d)
-abstract _dump(Dumper d)
}
class ExprNode {
+TypeNode exprType
+boolean isLValue
+Type getExprType()
+void setExprType(TypeNode exprType)
}
class StmtNode {
+Scope scope
+Scope getScope()
+void setScope(Scope scope)
}
class DeclNode {
+String declName
+Symbol refSymbol
+String getDeclName()
+Symbol getRefSymbol()
+void setRefSymbol(Symbol refSymbol)
}
class TypeNode {
+Type baseType
+int dim
+Type getBaseType()
+int getDim()
+boolean isEqual(TypeNode obj)
}
ASTNode <|-- ExprNode
ASTNode <|-- StmtNode
ASTNode <|-- DeclNode
ASTNode <|-- TypeNode
ASTNode <|-- CompileUnit
```

**Diagram sources**
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L49)
- [ExprNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/ExprNode.java#L1-L42)
- [StmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/StmtNode.java#L1-L23)
- [DeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/DeclNode.java#L1-L37)
- [TypeNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/type/TypeNode.java#L1-L71)

**Section sources**
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L49)
- [ExprNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/ExprNode.java#L1-L42)
- [StmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/StmtNode.java#L1-L23)
- [DeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/DeclNode.java#L1-L37)
- [TypeNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/type/TypeNode.java#L1-L71)

## AST Construction Process
The AST construction process is implemented in the CymbolASTBuilder class, which extends ANTLR4's CymbolBaseVisitor and implements the CymbolVisitor interface. The builder traverses the ParseTree and creates corresponding AST nodes for each grammar rule encountered. The process begins with the compilation unit and recursively processes all child nodes, building a complete AST representation of the source code.

```mermaid
sequenceDiagram
participant Compiler
participant CymbolASTBuilder
participant ParseTree
participant ASTNode
Compiler->>CymbolASTBuilder : build(compilationUnitContext)
CymbolASTBuilder->>CymbolASTBuilder : visitCompilationUnit()
CymbolASTBuilder->>ParseTree : traverse children
loop For each child node
ParseTree->>CymbolASTBuilder : visit(childNode)
alt Node is declaration
CymbolASTBuilder->>ASTNode : create DeclNode
else Node is statement
CymbolASTBuilder->>ASTNode : create StmtNode
else Node is expression
CymbolASTBuilder->>ASTNode : create ExprNode
end
CymbolASTBuilder->>ASTNode : set source context
end
CymbolASTBuilder->>Compiler : return AST
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)

**Section sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)

## Expression Node Types
The AST supports various expression node types that represent different kinds of expressions in the source language. These include binary expressions, unary expressions, function calls, array access, field access, and literal expressions. Each expression node type contains specific fields and methods relevant to its operation.

```mermaid
classDiagram
class ExprNode {
<<abstract>>
}
class BinaryExprNode {
+BinaryOpType opType
+ExprNode lhs
+ExprNode rhs
}
class UnaryExprNode {
+UnaryOpType opType
+ExprNode valExpr
}
class CallFuncNode {
+String funcName
+ExprNode[] argsNode
}
class ArrayAccessNode {
+ExprNode array
+ExprNode index
}
class FieldAccessNode {
+ExprNode object
+String fieldName
}
class CastExprNode {
+TypeNode targetType
+ExprNode expr
}
class LiteralNode {
<<abstract>>
}
class IntExprNode {
+int value
}
class FloatExprNode {
+double value
}
class BoolExprNode {
+boolean value
}
class StringExprNode {
+String value
}
class NullExprNode {
}
ExprNode <|-- BinaryExprNode
ExprNode <|-- UnaryExprNode
ExprNode <|-- CallFuncNode
ExprNode <|-- ArrayAccessNode
ExprNode <|-- FieldAccessNode
ExprNode <|-- CastExprNode
ExprNode <|-- LiteralNode
LiteralNode <|-- IntExprNode
LiteralNode <|-- FloatExprNode
LiteralNode <|-- BoolExprNode
LiteralNode <|-- StringExprNode
LiteralNode <|-- NullExprNode
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)
- [ExprNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/ExprNode.java#L1-L42)

**Section sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)
- [ExprNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/ExprNode.java#L1-L42)

## Statement Node Types
Statement nodes represent executable statements in the source code. The AST includes various statement types such as assignment statements, control flow statements (if, while), return statements, and block statements. Each statement node type captures the specific structure and components of the corresponding language construct.

```mermaid
classDiagram
class StmtNode {
<<abstract>>
}
class AssignStmtNode {
+ExprNode lhs
+ExprNode rhs
}
class IfStmtNode {
+ExprNode condNode
+StmtNode thenBlock
+StmtNode elseBlock
}
class WhileStmtNode {
+ExprNode condNode
+BlockStmtNode thenBlock
}
class ReturnStmtNode {
+ExprNode retVal
}
class BlockStmtNode {
+StmtNode[] stmtList
+ScopeType parentScopeType
}
class VarDeclStmtNode {
+VarDeclNode varDeclNode
}
class BreakStmtNode {
}
class ContinueStmtNode {
}
class ExprStmtNode {
+ExprNode exprNode
}
StmtNode <|-- AssignStmtNode
StmtNode <|-- IfStmtNode
StmtNode <|-- WhileStmtNode
StmtNode <|-- ReturnStmtNode
StmtNode <|-- BlockStmtNode
StmtNode <|-- VarDeclStmtNode
StmtNode <|-- BreakStmtNode
StmtNode <|-- ContinueStmtNode
StmtNode <|-- ExprStmtNode
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)
- [StmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/StmtNode.java#L1-L23)

**Section sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)
- [StmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/StmtNode.java#L1-L23)

## Declaration Node Types
Declaration nodes represent various declarations in the source code, including variable declarations, function declarations, struct declarations, and typedef declarations. These nodes capture the structure and components of declarations, including type information, names, and initialization expressions.

```mermaid
classDiagram
class DeclNode {
<<abstract>>
+String declName
+Symbol refSymbol
}
class VarDeclNode {
+VariableSymbol symbol
+ExprNode assignNode
+IDExprNode idExprNode
}
class FuncDeclNode {
+TypeNode retType
+String funcName
+VarDeclListNode paramSlots
+BlockStmtNode bodyStmt
}
class StructDeclNode {
+String structName
+StructMemberNode[] members
}
class StructMemberNode {
+TypeNode memberType
+String memberName
+int arraySize
}
class TypedefDeclNode {
+TypeNode originalType
+String aliasName
}
class VarDeclListNode {
+VarDeclNode[] varDeclNodeList
}
DeclNode <|-- VarDeclNode
DeclNode <|-- FuncDeclNode
DeclNode <|-- StructDeclNode
DeclNode <|-- TypedefDeclNode
DeclNode <|-- VarDeclListNode
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)
- [DeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/DeclNode.java#L1-L37)

**Section sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)
- [DeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/DeclNode.java#L1-L37)

## Source Location Preservation
The AST preserves source location information by storing the ParserRuleContext for each node. This context contains information about the token range in the original source code, enabling accurate error reporting and debugging. The getLocation() method extracts line and column information from the context to provide precise source positioning.

```mermaid
flowchart TD
Start([AST Node Creation]) --> ExtractContext["Extract ParserRuleContext"]
ExtractContext --> CheckContext{"Context Exists?"}
CheckContext --> |Yes| ExtractLocation["Extract Start/Stop Positions"]
CheckContext --> |No| SetNull["Set Location to Null"]
ExtractLocation --> CreateLocation["Create Location Object"]
CreateLocation --> StoreLocation["Store in AST Node"]
SetNull --> StoreLocation
StoreLocation --> End([Node Complete])
style Start fill:#f9f,stroke:#333
style End fill:#f9f,stroke:#333
```

**Diagram sources**
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L49)

**Section sources**
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L49)

## AST Benefits Over ParseTree
The AST provides several advantages over the raw ParseTree for semantic analysis and code generation. It eliminates syntactic noise, preserves only essential semantic information, includes type information, and provides a cleaner structure for traversal and transformation. The typed nature of the AST enables more efficient and accurate semantic analysis compared to the generic ParseTree.

```mermaid
graph TD
subgraph ParseTree
PT[ParseTree]
PT --> Syntax[Syntactic Elements]
PT --> Tokens[Raw Tokens]
PT --> Rules[Grammar Rules]
end
subgraph AST
AT[Abstract Syntax Tree]
AT --> Semantics[Semantic Elements]
AT --> Types[Type Information]
AT --> Structure[Clean Hierarchy]
AT --> Location[Source Locations]
end
PT --> |Transformation| AT
style PT fill:#f9f,stroke:#333
style AT fill:#bbf,stroke:#333
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L49)

## AST Visitor Pattern Implementation
The AST uses the visitor pattern to enable traversal and operations on the tree structure. The ASTVisitor interface defines visit methods for each node type, allowing for type-safe processing of AST nodes. The ASTBaseVisitor provides default implementations that can be overridden for specific processing needs.

```mermaid
classDiagram
class ASTVisitor~S,E~ {
<<interface>>
S visit(CompileUnit rootNode)
S visit(VarDeclNode varDeclNode)
S visit(FuncDeclNode funcDeclNode)
S visit(VarDeclStmtNode varDeclStmtNode)
E visit(TypeNode typeNode)
E visit(ExprNode node)
E visit(BinaryExprNode binaryExprNode)
E visit(IDExprNode idExprNode)
E visit(BoolExprNode boolExprNode)
E visit(CallFuncNode callExprNode)
E visit(IntExprNode intExprNode)
E visit(FloatExprNode floatExprNode)
E visit(NullExprNode nullExprNode)
S visit(StmtNode node)
S visit(AssignStmtNode assignStmtNode)
S visit(IfStmtNode ifStmtNode)
S visit(WhileStmtNode whileStmtNode)
S visit(ReturnStmtNode returnStmtNode)
S visit(BlockStmtNode blockStmtNode)
}
class ASTBaseVisitor {
+Void visit(CompileUnit rootNode)
+Void visit(VarDeclNode varDeclNode)
+Void visit(FuncDeclNode funcDeclNode)
+Void visit(VarDeclStmtNode varDeclStmtNode)
+Void visit(TypeNode typeNode)
+Void visit(BinaryExprNode binaryExprNode)
+Void visit(IDExprNode idExprNode)
+Void visit(BoolExprNode boolExprNode)
+Void visit(CallFuncNode callExprNode)
+Void visit(IntExprNode intExprNode)
+Void visit(FloatExprNode floatExprNode)
+Void visit(NullExprNode nullExprNode)
+Void visit(AssignStmtNode assignStmtNode)
+Void visit(IfStmtNode ifStmtNode)
+Void visit(WhileStmtNode whileStmtNode)
+Void visit(ReturnStmtNode returnStmtNode)
+Void visit(BlockStmtNode blockStmtNode)
}
class ASTNode {
+void accept(ASTVisitor visitor)
}
ASTVisitor <|-- ASTBaseVisitor
ASTNode --> ASTVisitor : "accepts"
```

**Diagram sources**
- [ASTVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTVisitor.java#L1-L40)
- [ASTBaseVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/ASTBaseVisitor.java#L1-L94)

**Section sources**
- [ASTVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTVisitor.java#L1-L40)
- [ASTBaseVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/ASTBaseVisitor.java#L1-L94)

## Code Generation Examples
The AST construction process handles various language constructs, creating appropriate node types for each. Examples include function definitions, control flow statements, and expressions, each transformed into their corresponding AST representations with preserved type and location information.

```mermaid
flowchart TD
subgraph Function Definition
FD[functionDecl] --> RT[retType]
FD --> FN[funcName]
FD --> PP[params]
FD --> BD[blockDef]
RT --> TypeNode
PP --> VarDeclListNode
BD --> BlockStmtNode
end
subgraph Control Flow
IF[stateCondition] --> COND[cond]
IF --> THEN[then]
IF --> ELSE[elseDo]
COND --> ExprNode
THEN --> StmtNode
ELSE --> StmtNode
WHILE[stateWhile] --> WCOND[cond]
WHILE --> WTHEN[then]
WCOND --> ExprNode
WTHEN --> BlockStmtNode
end
subgraph Expressions
BIN[exprBinary] --> LHS[expr(0)]
BIN --> OP[o]
BIN --> RHS[expr(1)]
LHS --> ExprNode
RHS --> ExprNode
UNARY[exprUnary] --> UOP[o]
UNARY --> UEXPR[expr]
UEXPR --> ExprNode
CALL[exprFuncCall] --> FUNC[expr(0)]
CALL --> ARGS[expr(1..n)]
FUNC --> IDExprNode
ARGS --> ExprNode
end
style FD fill:#f9f,stroke:#333
style IF fill:#f9f,stroke:#333
style WHILE fill:#f9f,stroke:#333
style BIN fill:#f9f,stroke:#333
style UNARY fill:#f9f,stroke:#333
style CALL fill:#f9f,stroke:#333
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)

**Section sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)

## Future Transformations and Optimizations
The AST structure supports future transformations and optimizations by providing a clean, typed representation of the source code. This enables various analysis and transformation passes, including type checking, constant folding, dead code elimination, and other optimizations that require semantic understanding of the code structure.

```mermaid
graph TD
AST[Abstract Syntax Tree] --> TypeCheck[Type Checking]
AST --> ConstantFold[Constant Folding]
AST --> DeadCode[Dead Code Elimination]
AST --> LoopOpt[Loop Optimization]
AST --> Inlining[Function Inlining]
AST --> RegisterAlloc[Register Allocation]
TypeCheck --> OptimizedAST[Optimized AST]
ConstantFold --> OptimizedAST
DeadCode --> OptimizedAST
LoopOpt --> OptimizedAST
Inlining --> OptimizedAST
RegisterAlloc --> OptimizedAST
OptimizedAST --> CodeGen[Code Generation]
style AST fill:#f9f,stroke:#333
style OptimizedAST fill:#bbf,stroke:#333
style CodeGen fill:#9f9,stroke:#333
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L49)

**Section sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L319)
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L49)