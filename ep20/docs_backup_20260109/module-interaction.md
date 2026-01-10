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
    D --> H
    E --> H
    F --> H
    G --> H
    
    I[Debug Tools] --> A
    I --> B
    I --> C
    I --> D
    I --> E
    I --> F
    I --> G
    
    J[Error Handling] --> A
    J --> B
    J --> C
    J --> D
    J --> E
    J --> F
    J --> G
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
    G --> H[IntExprNode]
    G --> I[FloatExprNode]
    G --> J[StringExprNode]
    G --> K[BoolExprNode]
    B --> L[CallFuncNode]
    B --> M[IDExprNode]
    
    C --> N[IfStmtNode]
    C --> O[WhileStmtNode]
    C --> P[AssignStmtNode]
    C --> Q[ReturnStmtNode]
    C --> R[BlockStmtNode]
    C --> S[VarDeclStmtNode]
    C --> T[ExprStmtNode]
    C --> U[BreakStmtNode]
    C --> V[ContinueStmtNode]
    
    D --> W[VarDeclNode]
    D --> X[FuncDeclNode]
    D --> Y[VarDeclListNode]
    
    Z[ASTVisitor] --> A
    Z --> B
    Z --> C
    Z --> D
```

## 3. 符号表模块内部交互

```mermaid
graph TD
    A[Scope] --> B[BaseScope]
    B --> C[GlobalScope]
    B --> D[LocalScope]
    
    E[Symbol] --> F[VariableSymbol]
    E --> G[MethodSymbol]
    E --> H[ScopedSymbol]
    
    I[Type] --> J[BuiltInTypeSymbol]
    I --> K[OperatorType]
    
    L[SymbolTable] --> A
    L --> E
    L --> I
    L --> M[TypeTable]
    
    N[LocalDefine] --> L
    O[TypeChecker] --> L
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
    C --> Q[FuncEntryLabel]
    C --> R[ExprStmt]
    
    D --> S[IRVisitor]
    
    T[CymbolIRBuilder] --> A
    T --> B
    T --> C
    T --> D
```

## 5. CFG模块内部交互

```mermaid
graph TD
    A[BasicBlock] --> B[CFG]
    C[LinearIRBlock] --> A
    D[IFlowOptimizer] --> E[ControlFlowAnalysis]
    D --> F[LivenessAnalysis]
    B --> D
    
    G[CFGBuilder] --> A
    G --> B
    G --> H[Loc]
    G --> I[IOrdIdentity]
    
    J[BasicBlockOptimizationTest] --> A
    K[ControlFlowGraphTest] --> B
```

## 6. 代码生成模块内部交互

```mermaid
graph TD
    A[CymbolAssembler] --> B[IOperatorEmitter]
    B --> C[CymbolVMIOperatorEmitter]
    A --> D[IRNode]
    
    E[VMInstructionTest] --> A
    F[FunctionCallTest] --> A
    G[CymbolAssemblerTest] --> A
```

## 7. 测试模块与核心模块交互

```mermaid
graph TD
    A[Test Classes] --> B[AST Tests]
    A --> C[IR Tests]
    A --> D[CFG Tests]
    A --> E[Codegen Tests]
    A --> F[Symbol Tests]
    A --> G[Type Tests]
    
    B --> H[ASTNode]
    B --> I[ExprNode]
    B --> J[StmtNode]
    B --> K[DeclNode]
    
    C --> L[IRNode]
    C --> M[Expr]
    C --> N[Stmt]
    
    D --> O[BasicBlock]
    D --> P[CFG]
    D --> Q[CFGBuilder]
    
    E --> R[CymbolAssembler]
    E --> S[IOperatorEmitter]
    
    F --> T[SymbolTable]
    F --> U[Scope]
    F --> V[Symbol]
    
    G --> W[TypeChecker]
    G --> X[Type]
    
    Y[Mockito] --> A
    Z[JUnit] --> A
    AA[AssertJ] --> A
```

## 8. 调试模块与核心模块交互

```mermaid
graph TD
    A[Debugger] --> B[AST Dumper]
    A --> C[IR Dumper]
    A --> D[CFG Dumper]
    
    B --> E[ASTNode]
    C --> F[IRNode]
    D --> G[CFG]
    
    H[Dumpable] --> E
    H --> F
    H --> G
    
    I[Dumper] --> H
    I --> B
    I --> C
    I --> D
```

## 9. 错误处理模块交互

```mermaid
graph TD
    A[ErrorIssuer] --> B[CymbalError]
    A --> C[Phase]
    
    D[Compiler] --> A
    E[Parser] --> A
    F[ASTBuilder] --> A
    G[TypeChecker] --> A
    H[IRBuilder] --> A
    I[CFGBuilder] --> A
    J[Assembler] --> A
    
    K[Task] --> A
    K --> C
```

## 10. 驱动模块交互

```mermaid
graph TD
    A[Compiler] --> B[Task]
    B --> C[Phase]
    C --> D[AST]
    C --> E[IR]
    C --> F[CFG]
    C --> G[ASM]
    
    H[Driver] --> A
    I[ErrorIssuer] --> A
    
    J[PhaseExecutor] --> B
    J --> K[ASTBaseVisitor]
    J --> L[CymbolIRBuilder]
    J --> M[CFGBuilder]
    J --> N[CymbolAssembler]
```

## 11. 工具模块交互

```mermaid
graph TD
    A[Utils] --> B[Kind]
    A --> C[StreamUtils]
    A --> D[Location]
    
    E[Parser] --> D
    F[ASTNode] --> D
    G[ErrorIssuer] --> D
```

## 12. 类型系统模块交互

```mermaid
graph TD
    A[Type] --> B[BuiltInTypeSymbol]
    A --> C[OperatorType]
    
    D[TypeChecker] --> A
    D --> E[TypeTable]
    
    F[SymbolTable] --> A
    F --> G[Symbol]
    
    H[ExprNode] --> A
    I[IRNode] --> A
```

## 13. 数据流分析模块交互

```mermaid
graph TD
    A[DataFlowAnalysis] --> B[LivenessAnalysis]
    A --> C[ControlFlowAnalysis]
    
    D[CFG] --> A
    E[BasicBlock] --> A
    
    F[Optimizer] --> A
    G[IFlowOptimizer] --> A
```

## 14. 完整编译流程模块交互

```mermaid
graph TD
    A[Source Code] --> B[Lexer]
    B --> C[Parser]
    C --> D[ASTBuilder]
    D --> E[LocalDefine]
    E --> F[TypeChecker]
    F --> G[CymbolIRBuilder]
    G --> H[CFGBuilder]
    H --> I[ControlFlowAnalysis]
    I --> J[LivenessAnalysis]
    J --> K[CymbolAssembler]
    K --> L[VM Instructions]
    
    M[Test Framework] --> C
    M --> D
    M --> E
    M --> F
    M --> G
    M --> H
    M --> I
    M --> J
    M --> K
    
    N[Debug Tools] --> D
    N --> G
    N --> H
    N --> K