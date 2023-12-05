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


- Original CFG
```mermaid
graph TD
subgraph L9
Q0["t0 =  7 ;"]
Q1["jmp L3;"]
end
subgraph L8
Q2["t0 =  'break' ;"]
Q3["call print(args:1);"]
Q4["t0 = @0;"]
Q5["call dec1(args:1);"]
Q6["@0 = t0;"]
Q7["jmp L4;"]
end
subgraph L7
Q8["t0 = @0;"]
Q9["call print(args:1);"]
Q10["t0 = @0;"]
Q11["t1 =  7 ;"]
Q12["t0 EQ t1;"]
Q13["jmpIf t0,L9,L8;"]
end
subgraph L6
Q14["t0 =  0 ;"]
Q15["jmp L3;"]
end
subgraph L5
Q16["t0 = @0;"]
Q17["t1 =  5 ;"]
Q18["t0 GT t1;"]
Q19["jmpIf t0,L7,L8;"]
end
subgraph L4
Q20["t0 = @0;"]
Q21["t1 =  0 ;"]
Q22["t0 GT t1;"]
Q23["jmpIf t0,L5,L6;"]
end
subgraph L3
Q24["halt;"]
end
subgraph L2
Q25[".def main: args=0 ,locals=1;"]
Q26["t0 =  10 ;"]
Q27["@0 = t0;"]
Q28["jmp L4;"]
end
L2 --> L4
L2 --> L4
L4 --> L6
L4 --> L5
L5 --> L8
L5 --> L7
L7 --> L8
L7 --> L9
L9 --> L3
L9 --> L8
L8 --> L4
L8 --> L6
L6 --> L3
L6 --> L3
```

- Optimized CFG
```mermaid
graph TD
subgraph L9
Q0["t0 =  7 ;"]
Q1["jmp L3;"]
end
subgraph L8
Q2["t0 =  'break' ;"]
Q3["call print(args:1);"]
Q4["t0 = @0;"]
Q5["call dec1(args:1);"]
Q6["@0 = t0;"]
Q7["jmp L4;"]
end
subgraph L7
Q8["t0 = @0;"]
Q9["call print(args:1);"]
Q10["t0 = @0;"]
Q11["t1 =  7 ;"]
Q12["t0 EQ t1;"]
Q13["jmpIf t0,L9,L8;"]
end
subgraph L6
Q14["t0 =  0 ;"]
end
subgraph L5
Q15["t0 = @0;"]
Q16["t1 =  5 ;"]
Q17["t0 GT t1;"]
Q18["jmpIf t0,L7,L8;"]
end
subgraph L4
Q19["t0 = @0;"]
Q20["t1 =  0 ;"]
Q21["t0 GT t1;"]
Q22["jmpIf t0,L5,L6;"]
end
subgraph L3
Q23["halt;"]
end
subgraph L2
Q24[".def main: args=0 ,locals=1;"]
Q25["t0 =  10 ;"]
Q26["@0 = t0;"]
end
L2 --> L4
L4 --> L6
L4 --> L5
L5 --> L8
L5 --> L7
L7 --> L8
L7 --> L9
L9 --> L3
L9 --> L8
L8 --> L4
L8 --> L6
L6 --> L3
```
