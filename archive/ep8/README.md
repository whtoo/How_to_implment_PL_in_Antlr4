# EP8 - 自定义 AST 与向量数学

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 自定义抽象语法树（AST）构建与向量数学表达式求值
- **目标**: 理解 ParseTree → AST 的转换过程，掌握自定义节点类设计
- **在编译器流水线中的位置**: 从 ANTLR4 基础 → Cymbol 编译器的过渡阶段（首个自定义 AST 实现）
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 运行时

### 📁 项目结构
```
ep8/
└── src/main/java/org/teachfx/antlr4/
    ├── VecMath.g4          # 向量数学语法
    ├── ASTNode.java        # AST 基类
    ├── ExprNode.java       # 表达式节点基类
    ├── AddNode.java        # 加法节点
    ├── IntNode.java        # 整数常量节点
    ├── VarNode.java        # 变量引用节点
    ├── StatNode.java       # 语句节点
    ├── BinArithNode.java   # 二元算术节点
    ├── OPType.java         # 运算符枚举
    ├── ExprVisitor.java    # 表达式访问者
    └── VecMathVisitor.java # 语法树访问者
```

### 🏗️ 核心组件
- **VecMath.g4**: 向量数学语法，支持赋值、变量、整数、加减乘除
- **ASTNode / ExprNode**: 自定义 AST 节点继承体系
- **AddNode / IntNode / VarNode**: 具体 AST 节点类型
- **ExprVisitor**: 访问 AST 节点并执行求值
- **VecMathVisitor**: 将 ANTLR ParseTree 转换为自定义 AST

### 🔧 构建与测试
```bash
cd ep8
mvn clean compile
```

### 🚀 常用操作
```bash
# 运行向量数学求值器
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep8.ExprVisitor"
```

### 📝 关键注意事项
1. **首个 AST**: 本 EP 是项目中第一个「自定义 AST」实现，标志着从纯 ANTLR4 教程向编译器开发的过渡
2. **ParseTree → AST**: `VecMathVisitor` 遍历 ParseTree 并手动构建 AST 节点
3. **无标签**: 语法中未使用 `#label`，AST 构建完全在 visitor 中手动处理
4. **求值分离**: AST 构建和求值是两个独立阶段（为后续多遍编译器奠定基础）

### 🔍 调试技巧
1. 在 `VecMathVisitor.visit()` 中打断点，观察 ParseTree 如何转换为 AST
2. 打印 AST 结构（递归遍历节点树）

### 🤖 AI Agent 代码开发指南
- **添加新节点类型**: 1) 创建继承 `ExprNode` 的新类，2) 在 `VecMathVisitor` 中构建该节点，3) 在 `ExprVisitor` 中添加求值逻辑
- **重构为 Visitor 接口**: 可参考 EP11-EP12 的模式，将求值逻辑改为标准 Visitor 模式

---

*EP8 是项目的转折点——从「学习 ANTLR4」转向「构建编译器」。自定义 AST 是后续所有编译器前端的基础。*
