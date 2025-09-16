# Abstract Syntax Tree Construction

<cite>
**Referenced Files in This Document**   
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java)
- [ASTVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTVisitor.java)
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java)
- [BinaryExprNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/BinaryExprNode.java)
- [IfStmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/IfStmtNode.java)
- [CompileUnit.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/CompileUnit.java)
- [TypeNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/type/TypeNode.java)
- [ExprNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/ExprNode.java)
- [StmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/StmtNode.java)
- [VarDeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/VarDeclNode.java)
- [FuncDeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/FuncDeclNode.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [AST Node Hierarchy](#ast-node-hierarchy)
3. [Visitor Pattern Implementation](#visitor-pattern-implementation)
4. [AST Construction Process](#ast-construction-process)
5. [Node Creation Examples](#node-creation-examples)
6. [Design Rationale](#design-rationale)
7. [Extending the AST](#extending-the-ast)
8. [Conclusion](#conclusion)

## Introduction
The Abstract Syntax Tree (AST) construction process in the Cymbol language implementation transforms the ANTLR4 parse tree into a typed, hierarchical representation of program structure. This AST serves as the foundation for subsequent semantic analysis and code generation phases. The design emphasizes type safety, source location tracking, and extensibility through a well-defined visitor pattern. The AST accurately represents Cymbol language constructs including expressions, statements, declarations, and types, enabling robust compiler passes.

## AST Node Hierarchy

The AST node hierarchy is organized around a base `ASTNode` class that provides common functionality for all nodes in the tree. The hierarchy is structured to represent the various language constructs in a type-safe manner.

```mermaid
classDiagram
class ASTNode {
+ParserRuleContext ctx
+Location getLocation()
+void accept(ASTVisitor visitor)
+void dump(Dumper d)
+_dump(Dumper d)
}
class CompileUnit {
+VarDeclNode[] varDecls
+FuncDeclNode[] funcDecls
+TypedefDeclNode[] typedefDecls
+StructDeclNode[] structDecls
+addVarDecl(VarDeclNode)
+addFuncDecl(FuncDeclNode)
}
class ExprNode {
+Type getExprType()
+void accept(ASTVisitor visitor)
}
class StmtNode {
+ScopeType getParentScopeType()
+void setParentScopeType(ScopeType)
+void accept(ASTVisitor visitor)
}
class TypeNode {
+Type getBaseType()
+static TypeNode IntNode
+static TypeNode FloatNode
+static TypeNode BoolNode
+static TypeNode StrNode
+static TypeNode VoidNode
+static TypeNode ObjNode
}
ASTNode <|-- CompileUnit
ASTNode <|-- ExprNode
ASTNode <|-- StmtNode
ASTNode <|-- TypeNode
ASTNode <|-- DeclNode
ExprNode <|-- BinaryExprNode
ExprNode <|-- IDExprNode
ExprNode <|-- IntExprNode
ExprNode <|-- FloatExprNode
ExprNode <|-- BoolExprNode
ExprNode <|-- StringExprNode
ExprNode <|-- NullExprNode
ExprNode <|-- UnaryExprNode
ExprNode <|-- CallFuncNode
ExprNode <|-- ArrayLiteralNode
ExprNode <|-- CastExprNode
ExprNode <|-- FieldAccessNode
ExprNode <|-- ArrayAccessNode
StmtNode <|-- BlockStmtNode
StmtNode <|-- IfStmtNode
StmtNode <|-- WhileStmtNode
StmtNode <|-- ReturnStmtNode
StmtNode <|-- AssignStmtNode
StmtNode <|-- ExprStmtNode
StmtNode <|-- BreakStmtNode
StmtNode <|-- ContinueStmtNode
StmtNode <|-- VarDeclStmtNode
DeclNode <|-- VarDeclNode
DeclNode <|-- FuncDeclNode
DeclNode <|-- StructDeclNode
DeclNode <|-- StructMemberNode
DeclNode <|-- TypedefDeclNode
DeclNode <|-- VarDeclListNode
CompileUnit "1" *-- "0..*" VarDeclNode : contains
CompileUnit "1" *-- "0..*" FuncDeclNode : contains
CompileUnit "1" *-- "0..*" TypedefDeclNode : contains
CompileUnit "1" *-- "0..*" StructDeclNode : contains
FuncDeclNode "1" *-- "0..*" VarDeclNode : parameters
BlockStmtNode "1" *-- "0..*" StmtNode : statements
IfStmtNode "1" --> "1" ExprNode : condition
IfStmtNode "1" --> "1" StmtNode : then
IfStmtNode "1" --> "0..1" StmtNode : else
WhileStmtNode "1" --> "1" ExprNode : condition
WhileStmtNode "1" --> "1" StmtNode : body
AssignStmtNode "1" --> "1" ExprNode : lhs
AssignStmtNode "1" --> "1" ExprNode : rhs
BinaryExprNode "1" --> "1" ExprNode : lhs
BinaryExprNode "1" --> "1" ExprNode : rhs
CallFuncNode "1" *-- "0..*" ExprNode : arguments
ArrayAccessNode "1" --> "1" ExprNode : array
ArrayAccessNode "1" --> "1" ExprNode : index
FieldAccessNode "1" --> "1" ExprNode : object
StructDeclNode "1" *-- "0..*" StructMemberNode : members
```

**Diagram sources**
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java)
- [CompileUnit.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/CompileUnit.java)
- [ExprNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/ExprNode.java)
- [StmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/StmtNode.java)
- [TypeNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/type/TypeNode.java)
- [VarDeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/VarDeclNode.java)
- [FuncDeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/FuncDeclNode.java)

**Section sources**
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L48)
- [CompileUnit.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/CompileUnit.java)
- [ExprNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/expr/ExprNode.java)
- [StmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/StmtNode.java)

## Visitor Pattern Implementation

The visitor pattern is implemented through the `ASTVisitor` interface, which defines visit methods for all AST node types. This enables type-safe traversal and transformation of the AST without requiring instanceof checks or casting in client code.

```mermaid
classDiagram
class ASTVisitor~S,E~ {
+S visit(CompileUnit)
+S visit(VarDeclNode)
+S visit(FuncDeclNode)
+S visit(VarDeclStmtNode)
+S visit(StructDeclNode)
+S visit(StructMemberNode)
+S visit(TypedefDeclNode)
+E visit(TypeNode)
+E visit(ExprNode)
+E visit(BinaryExprNode)
+E visit(IDExprNode)
+E visit(BoolExprNode)
+E visit(CallFuncNode)
+E visit(IntExprNode)
+E visit(FloatExprNode)
+E visit(NullExprNode)
+E visit(StringExprNode)
+E visit(UnaryExprNode)
+E visit(ArrayLiteralNode)
+E visit(CastExprNode)
+E visit(FieldAccessNode)
+S visit(IfStmtNode)
+S visit(ExprStmtNode)
+S visit(BlockStmtNode)
+S visit(ReturnStmtNode)
+S visit(WhileStmtNode)
+S visit(AssignStmtNode)
+S visit(BreakStmtNode)
+S visit(ContinueStmtNode)
+S visit(StmtNode)
}
class CymbolASTBuilder {
+ASTNode visitCompilationUnit(CompilationUnitContext)
+ASTNode visitVarDecl(VarDeclContext)
+ASTNode visitType(TypeContext)
+ASTNode visitPrimaryType(PrimaryTypeContext)
+ASTNode visitFunctionDecl(FunctionDeclContext)
+ASTNode visitFormalParameters(FormalParametersContext)
+ASTNode visitFormalParameter(FormalParameterContext)
+ASTNode visitBlock(BlockContext)
+ASTNode visitStatBlock(StatBlockContext)
+ASTNode visitStatVarDecl(StatVarDeclContext)
+ASTNode visitStatReturn(StatReturnContext)
+ASTNode visitStateCondition(StateConditionContext)
+ASTNode visitStateWhile(StateWhileContext)
+ASTNode visitVisitBreak(VisitBreakContext)
+ASTNode visitVisitContinue(VisitContinueContext)
+ASTNode visitStatAssign(StatAssignContext)
+ASTNode visitExprStat(ExprStatContext)
+ASTNode visitExprBinary(ExprBinaryContext)
+ASTNode visitExprGroup(ExprGroupContext)
+ASTNode visitExprUnary(ExprUnaryContext)
+ASTNode visitExprFuncCall(ExprFuncCallContext)
+ASTNode visitExprArrayAccess(ExprArrayAccessContext)
+ASTNode visitPrimaryID(PrimaryIDContext)
+ASTNode visitPrimaryINT(PrimaryINTContext)
+ASTNode visitPrimaryFLOAT(PrimaryFLOATContext)
+ASTNode visitPrimarySTRING(PrimarySTRINGContext)
+ASTNode visitPrimaryBOOL(PrimaryBOOLContext)
+ASTNode visitArrayInitializer(ArrayInitializerContext)
+ASTNode visitExprCast(ExprCastContext)
+ASTNode visitExprFieldAccess(ExprFieldAccessContext)
+ASTNode visitTypedefDecl(TypedefDeclContext)
+ASTNode visitStructDecl(StructDeclContext)
+ASTNode visitStructMember(StructMemberContext)
}
ASTVisitor <|-- CymbolASTBuilder
ASTVisitor <|-- ASTBaseVisitor
ASTVisitor <|-- TypeChecker
ASTVisitor <|-- CymbolAssembler
ASTNode "1" --> "1" ASTVisitor : accepts
```

**Diagram sources**
- [ASTVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTVisitor.java#L1-L123)
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L318)

**Section sources**
- [ASTVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTVisitor.java#L1-L123)
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L318)

## AST Construction Process

The AST construction process is handled by the `CymbolASTBuilder` class, which extends ANTLR's base visitor to convert the parse tree into a typed AST. The builder maintains parent-child relationships and source location information throughout the construction process.

```mermaid
sequenceDiagram
participant Parser as ANTLR Parser
participant Builder as CymbolASTBuilder
participant AST as AST Nodes
participant Context as ParserRuleContext
Parser->>Builder : visitCompilationUnit(ctx)
activate Builder
Builder->>AST : Create CompileUnit
loop For each child node
Builder->>Builder : visit(childNode)
alt VarDeclContext
Builder->>AST : Create VarDeclNode
Builder->>CompileUnit : addVarDecl()
end
alt FunctionDeclContext
Builder->>AST : Create FuncDeclNode
Builder->>CompileUnit : addFuncDecl()
end
alt TypedefDeclContext
Builder->>AST : Create TypedefDeclNode
Builder->>CompileUnit : addTypedefDecl()
end
alt StructDeclContext
Builder->>AST : Create StructDeclNode
Builder->>CompileUnit : addStructDecl()
end
end
Builder-->>Parser : Return CompileUnit
deactivate Builder
Parser->>Builder : visitVarDecl(ctx)
activate Builder
Builder->>Builder : visit(type())
Builder->>AST : Create TypeNode
Builder->>Builder : visit(expr())
Builder->>AST : Create ExprNode
Builder->>AST : Create IDExprNode
Builder->>AST : Create VarDeclNode
Builder-->>Parser : Return VarDeclNode
deactivate Builder
Parser->>Builder : visitFunctionDecl(ctx)
activate Builder
Builder->>Builder : visit(params)
Builder->>AST : Create VarDeclListNode
Builder->>Builder : visit(retType)
Builder->>AST : Create TypeNode
Builder->>Builder : visit(blockDef)
Builder->>AST : Create BlockStmtNode
Builder->>AST : Create FuncDeclNode
Builder-->>Parser : Return FuncDeclNode
deactivate Builder
Parser->>Builder : visitExprBinary(ctx)
activate Builder
Builder->>Builder : visit(expr(0))
Builder->>AST : Create LHS ExprNode
Builder->>Builder : visit(expr(1))
Builder->>AST : Create RHS ExprNode
Builder->>AST : Create BinaryExprNode
Builder-->>Parser : Return BinaryExprNode
deactivate Builder
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L318)
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L48)

**Section sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L318)

## Node Creation Examples

### Function Declaration
The AST construction for a function declaration involves creating a `FuncDeclNode` with return type, parameter list, and body block.

```mermaid
flowchart TD
Start([Function Declaration]) --> VisitFunctionDecl["visitFunctionDecl(FunctionDeclContext)"]
VisitFunctionDecl --> VisitParams["visit(params) → VarDeclListNode"]
VisitParams --> VisitRetType["visit(retType) → TypeNode"]
VisitRetType --> VisitBlock["visit(blockDef) → BlockStmtNode"]
VisitBlock --> CreateFuncNode["Create FuncDeclNode with parameters"]
CreateFuncNode --> SetContext["Set ParserRuleContext"]
SetContext --> ReturnNode["Return FuncDeclNode"]
ReturnNode --> End([Node Created])
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L100-L120)
- [FuncDeclNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/decl/FuncDeclNode.java)

### Variable Assignment
Variable assignment statements are transformed into `AssignStmtNode` instances with left-hand side and right-hand side expressions.

```mermaid
flowchart TD
Start([Assignment Statement]) --> VisitAssign["visitStatAssign(StatAssignContext)"]
VisitAssign --> VisitLHS["visit(expr(0)) → ExprNode"]
VisitLHS --> VisitRHS["visit(expr(1)) → ExprNode"]
VisitRHS --> CreateAssignNode["Create AssignStmtNode(lhs, rhs)"]
CreateAssignNode --> SetContext["Set ParserRuleContext"]
SetContext --> ReturnNode["Return AssignStmtNode"]
ReturnNode --> End([Node Created])
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L180-L190)
- [AssignStmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/AssignStmtNode.java)

### Control Flow Statement
Control flow statements like if-else are represented with conditional expressions and corresponding statement blocks.

```mermaid
flowchart TD
Start([If-Else Statement]) --> VisitIf["visitStateCondition(StateConditionContext)"]
VisitIf --> VisitCond["visit(cond) → ExprNode"]
VisitCond --> VisitThen["visit(then) → StmtNode"]
VisitThen --> VisitElse["visit(elseDo) → StmtNode"]
VisitElse --> CreateIfNode["Create IfStmtNode(cond, then, else)"]
CreateIfNode --> SetContext["Set ParserRuleContext"]
SetContext --> ReturnNode["Return IfStmtNode"]
ReturnNode --> End([Node Created])
```

**Diagram sources**
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L160-L170)
- [IfStmtNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/stmt/IfStmtNode.java#L1-L60)

## Design Rationale

The AST design follows several key principles that support semantic analysis and code generation:

1. **Type Safety**: Each node carries type information where applicable, enabling early detection of type errors.
2. **Source Location Tracking**: Every node maintains a reference to its parser context, allowing precise error reporting with line and column information.
3. **Extensibility**: The visitor pattern allows new operations to be added without modifying existing node classes.
4. **Immutability**: Once constructed, AST nodes are immutable, ensuring consistency during compiler passes.
5. **Hierarchical Structure**: The tree structure naturally represents program nesting and scope relationships.

The design supports semantic analysis by providing:
- Complete type information through `TypeNode` and `getExprType()` methods
- Symbol references in `IDExprNode` for name resolution
- Scope information through `ScopeType` in statement nodes
- Parent-child relationships for contextual analysis

For code generation, the AST provides:
- A clean separation between syntax and semantics
- Typed expressions that can be directly translated to target instructions
- Control flow structures that map directly to intermediate representation
- Declaration information for symbol table generation

**Section sources**
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L48)
- [ASTVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTVisitor.java#L1-L123)
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L318)

## Extending the AST

To extend the AST for new language features, follow these steps:

1. **Add New Node Classes**: Create new classes extending the appropriate base node type (ExprNode, StmtNode, etc.)

2. **Update ASTVisitor Interface**: Add new visit methods to the `ASTVisitor` interface

3. **Implement in CymbolASTBuilder**: Add corresponding visit methods to handle the new parse tree contexts

4. **Update Type System**: If needed, extend the type system in `TypeTable` and `OperatorType`

Example for adding a switch statement:

```mermaid
flowchart TD
AddNodeClass["Add SwitchStmtNode extends StmtNode"] --> UpdateVisitor["Add visit(SwitchStmtNode) to ASTVisitor"]
UpdateVisitor --> ImplementBuilder["Implement visitSwitchStatement() in CymbolASTBuilder"]
ImplementBuilder --> UpdateTypeSystem["Update TypeChecker if new type rules"]
UpdateTypeSystem --> Test["Add test cases"]
```

When adding new expression types, also consider:
- Operator precedence and associativity
- Type coercion rules
- Constant folding opportunities
- Code generation patterns

The modular design ensures that extensions can be made with minimal impact on existing code, maintaining the integrity of the compiler architecture.

**Section sources**
- [ASTVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTVisitor.java#L1-L123)
- [CymbolASTBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java#L1-L318)
- [ASTNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java#L1-L48)

## Conclusion
The AST construction process in the Cymbol compiler transforms the ANTLR4 parse tree into a rich, typed representation that serves as the foundation for all subsequent compiler phases. The hierarchical node structure accurately represents language constructs while the visitor pattern enables flexible traversal and transformation. Source location tracking supports precise error reporting, and the extensible design allows for easy addition of new language features. This well-structured AST enables robust semantic analysis and efficient code generation, forming a critical component of the compiler architecture.