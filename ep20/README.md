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
    E --> H[TAC]
    F --> H[LIR]
    G --> H
    H --> I[Optimizer]
    I --> J[TargetCodes]
```

## DONE
- [x] 通过访问者模式构建抽象语法树
- [x] 增加ASTree printer以检查语法树构建是否合乎期望
- [x] 增加类型实体和相关类型处理内容
- [x] 将作用域和变量及函数的生命周期进行关联
- [x] 编译到[ep18](..%2Fep18)的VM
- [x] 扩展[ep18](..%2Fep18)的VM支持更丰富的[指令实现](../ep18/VM_Design.md)
- [x] 线性化IR和CFG

### 线性化IR
我们的IR本质上是tree模式的，这样一来我们的线性化实际上延迟到了指令生成时。
但是，这样一来我们就无法进行活性分析和很多与TAC表示相关的分析（或者是我没找到
直接对栈代码做分析的例子）。因此，我要对ep20的输出code过程进行线性改造。

### CFG

```mermaid
graph TD
    subgraph L0
        Q112
    end
    subgraph L2
        Q0["t0 =  10 ;"]
        Q1["@0 = t0;"]
        Q2["jmp L4;"]
    end
    subgraph L3
        Q3["t0 = @0;"]
        Q4["t1 =  0 ;"]
        Q5["t0 GT t1;"]
        Q6["jmpIf t0,L5,L6;"]
    end
    subgraph L4
        Q7["t0 = @0;"]
        Q8["t1 =  5 ;"]
        Q9["t0 GT t1;"]
        Q10["jmpIf t0,L7,L8;"]
    end
    subgraph L5
        Q11["t0 = @0;"]
        Q12["call print(args:1);"]
        Q13["t0 = @0;"]
        Q14["t1 =  7 ;"]
        Q15["t0 EQ t1;"]
        Q16["jmpIf t0,L9,L10;"]
    end
    subgraph L6
        Q17["t0 =  7 ;"]
        Q18["jmp L3;"]
    end
    subgraph L7
        Q000
    end
    subgraph L8
        Q19["t0 =  'break' ;"]
        Q20["call print(args:1);"]
        Q21["t0 = @0;"]
        Q22["call dec1(args:1);"]
        Q23["@0 = t0;"]
        Q24["jmp L4;"]
    end
    subgraph L9
        Q25["t0 =  0 ;"]
        Q26["jmp L3;"]
    end
    subgraph L10
        Q27["halt;"]
    end
    
    L0 --> L4
    L4 --> L5
    L4 --> L6
    L5 --> L7
    L5 --> L8
    L7 --> L0
    L7 --> L9
    L9 --> L3
    L9 --> L10
    L10 --> L8
    L8 --> L4
    L8 --> L6
    L6 --> L3
```

