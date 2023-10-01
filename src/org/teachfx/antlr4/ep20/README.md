## 编译步骤流程图

```mermaid
---
title: 编译流程图
---
graph LR
    A[ParseTree] --> B[ASTBuilder]
    B --> C[AST]
    C --> D[RevisedAST]
    D -->|LocalDefine| E(LocalResolver)
    E -->|TypeChecker| F(TypeChecker)
    D -->|DataFlowAnalysis| G(DataFlowAnalysis)
    E --> H[IR]
    F --> H
    G --> H
    H --> I[Optimizer]
    I --> J[TargetCodes]
```

## DONE
- [x] 通过访问者模式构建抽象语法树
- [x] 增加ASTree printer以检查语法树构建是否合乎期望

## Removed
~~- [ ] 增加类和接口定义~~