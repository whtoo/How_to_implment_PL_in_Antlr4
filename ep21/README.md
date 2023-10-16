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


