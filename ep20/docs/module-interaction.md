# EP20模块交互图

## 1. 核心模块依赖关系

```mermaid
graph TD
    A[Parser] --> B[AST]
    B --> C[SymbolTable]
    C --> D[IR]
    D --> E[CFG]
    E --> F[Optimizer]
    F --> G[Assembler]
    
    H[Test Framework] --> A
    H --> B
    H --> C
    H --> D
    H --> E
    H --> F
    H --> G
    
    I[Debug Tools] --> A
    I --> B
    I --> C
    I --> D
    I --> E
    I --> F
    I --> G
```

## 2. AST模块内部交互

```mermaid
graph TD
    A[ASTNode] --> B[ExprNode]
    A --> C[StmtNode]
    A --> D[DeclNode]
    
    B --> E[BinaryExprNode]
    B --> F[UnaryExprNode]
    B --> G[LiteralNode]
    B --> H[CallFuncNode]
    
    C --> I[IfStmtNode]
    C --> J[WhileStmtNode]
    C --> K[AssignStmtNode]
    C --> L[ReturnStmtNode]
    
    D --> M[VarDeclNode]
    D --> N[FuncDeclNode]
    
    O[ASTVisitor] --> A
```

## 3. 符号表模块内部交互

```mermaid
graph TD
    A[Scope] --> B[BaseScope]
    B --> C[GlobalScope]
    B --> D[LocalScope]
    
    E[Symbol] --> F[VariableSymbol]
    E --> G[MethodSymbol]
    E --> H[Type]
    H --> I[BuiltInTypeSymbol]
    
    J[SymbolTable] --> A
    J --> E
```

## 4. IR模块内部交互

```mermaid
graph TD
    A[IRNode] --> B[Expr]
    A --> C[Stmt]
    A --> D[Prog]
    
    B --> E[BinExpr]
    B --> F[UnaryExpr]
    B --> G[CallFunc]
    B --> H[ConstVal]
    B --> I[VarSlot]
    
    I --> J[FrameSlot]
    I --> K[OperandSlot]
    
    C --> L[Assign]
    C --> M[Label]
    C --> N[JMP]
    C --> O[CJMP]
    C --> P[ReturnVal]
    
    D --> Q[IRVisitor]
```

## 5. CFG模块内部交互

```mermaid
graph TD
    A[BasicBlock] --> B[CFG]
    C[LinearIRBlock] --> A
    D[IFlowOptimizer] --> E[ControlFlowAnalysis]
    D --> F[LivenessAnalysis]
    B --> D
```

## 6. 代码生成模块内部交互

```mermaid
graph TD
    A[CymbolAssembler] --> B[IOperatorEmitter]
    B --> C[CymbolVMIOperatorEmitter]
    A --> D[IRNode]
```

## 7. 测试模块与核心模块交互

```mermaid
graph TD
    A[Test Classes] --> B[AST Tests]
    A --> C[IR Tests]
    A --> D[CFG Tests]
    A --> E[Codegen Tests]
    
    B --> F[ASTNode]
    C --> G[IRNode]
    D --> H[BasicBlock]
    D --> I[CFG]
    E --> J[CymbolAssembler]
    
    K[Mockito] --> A
    L[JUnit] --> A
    M[AssertJ] --> A