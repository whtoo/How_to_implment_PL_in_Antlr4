# EP13 - AST 节点扩展与赋值支持

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 扩展 AST 节点体系，支持赋值语句和更多二元运算
- **目标**: 构建更完整的表达式语言 AST，为符号表和类型系统做准备
- **在编译器流水线中的位置**: AST 构建阶段（EP11–EP13 为 AST 深化）
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 运行时

### 📁 项目结构
```
ep13/
└── src/main/java/org/teachfx/antlr4/
    ├── Math.g4              # 数学表达式语法
    ├── Calc.java            # 入口程序
    ├── BuildAstVisitor.java # AST 构建 Visitor
    ├── EvalExprVisitor.java # AST 求值 Visitor
    ├── ASTVisitor.java      # Visitor 接口
    └── ep12/ast/            # AST 节点类
        ├── ExpressionNode.java
        ├── NumberNode.java
        ├── NegateNode.java
        ├── InfixExpressionNode.java
        ├── MultiplicationNode.java
        ├── DivisionNode.java
        ├── VarNode.java
        ├── AssignNode.java      # 新增：赋值节点
        ├── AdditionNode.java    # 新增：加法节点
        └── SubtractionNode.java # 新增：减法节点
```

### 🏗️ 核心组件
- **AssignNode**: 新增 AST 节点，表示变量赋值操作（`x = expr`）
- **AdditionNode / SubtractionNode**: 将加法和减法从通用的 `InfixExpressionNode` 中分离，形成更具体的节点类型
- **BuildAstVisitor / EvalExprVisitor**: 更新以支持新的节点类型

### 🔧 构建与测试
```bash
cd ep13
mvn clean compile
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep13.Calc"
```

### 🚀 常用操作
```bash
# 运行支持赋值的计算器
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep13.Calc"
# 输入:
#   x = 10
#   y = x + 5
#   y
# 输出: 15
```

### 📝 关键注意事项
1. **AST 增长**: 相比 EP12，新增了 3 个节点类型（AssignNode、AdditionNode、SubtractionNode）
2. **赋值语义**: AssignNode 需要存储变量名和右侧表达式，求值时更新变量环境
3. **节点细化**: 从通用 InfixExpressionNode 转向具体的二元运算节点，是类型系统的前奏
4. **无包结构优化**: 仍使用 EP12 的 ast/ 包结构

### 🔍 调试技巧
1. 打印 AST 结构时特别关注 AssignNode 的子树形状
2. 验证赋值后变量是否正确存入环境 Map

### 🤖 AI Agent 代码开发指南
- **添加语句类型**: 新增 StatementNode 基类，让 AssignNode 继承，为后续 if/while 做准备
- **环境管理**: 将变量环境从 EvalExprVisitor 中提取为独立类

---

*EP13 扩展了 AST 的表达能力——赋值节点的引入意味着 AST 不再只是表达式，开始具备「语句」的概念。*
