# 静态分析基础

## 编译目标变化
换成x86指令的寄存器机器，当然是化简的。

## TAC
本章重点是`三地址码`构造。

### TAC变换

```mermaid
---
title: TAC变换
---
graph LR
    A[Src] --> B[RevisedAST]
    B --> D[TAC]
    D --> E[LIR]
```

## DONE

```mermaid
graph TD;
    A[基本块1] --> B[基本块2]  
    B --> C[基本块3]  
    C -->|条件跳转| D[基本块6]  
    C -->|否则| E[基本块4]  
    E -->|条件跳转| F[基本块7]  
    F --> B  
    E -->|否则| G[基本块5]  
    G -->|条件跳转| F  
    G -->|否则| H[基本块8]  
    H --> I[基本块9]  
    D --> I  
    subgraph dec1  
    A  
    end  
    subgraph main  
        B --> C --> D --> E --> F --> G --> H --> I  
    end
```
