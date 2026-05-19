# EP12 - 模块化AST架构

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 包结构模块化、AST/Visitor/Parser分离、代码组织最佳实践
- **目标**: 展示与EP11相同功能但使用更优包结构的代码组织方式
- **在编译器流水线中的位置**: AST构建与求值（与EP11相同，但组织更好）
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 4.13.2 (语法解析)

### 📁 项目结构
```
ep12/
├── src/main/java/org/teachfx/antlr4/ep12/
│   ├── Calc.java                 # 计算器入口
│   ├── ast/                      # AST节点包（NEW: 模块化组织）
│   │   ├── ExpressionNode.java  # AST节点基类
│   │   ├── NumberNode.java      # 数字节点
│   │   ├── NegateNode.java      # 取反节点
│   │   ├── InfixExpressionNode.java # 中缀表达式节点
│   │   ├── MultiplicationNode.java  # 乘法节点
│   │   ├── DivisionNode.java     # 除法节点
│   │   └── VarNode.java          # 变量节点
│   └── visitor/                  # Visitor包（NEW: 模块化组织）
│       ├── ASTVisitor.java       # Visitor接口
│       ├── BuildAstVisitor.java  # 构建AST的Visitor
│       └── EvalExprVisitor.java  # 求值AST的Visitor
├── src/main/antlr4/org/teachfx/antlr4/ep12/parser/
│   └── Math.g4                  # 数学表达式语法（与EP11相同）
└── src/test/java/               # 单元测试
```

### 🏗️ 核心组件
- **语法定义**: Math.g4 — 与EP11相同的语法
- **ast/包**: ExpressionNode、NumberNode、NegateNode、InfixExpressionNode、MultiplicationNode、DivisionNode、VarNode
- **visitor/包**: ASTVisitor、BuildAstVisitor、EvalExprVisitor
- **入口**: Calc.java — 相同功能但调用方式适应新包结构
- **重构重点**: 相同功能，优的包结构

### 🔧 构建与测试
```bash
# 进入 EP12 目录
cd ep12

# 构建项目
mvn clean compile

# 运行计算器
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep12.Calc"

# 运行测试
mvn test
```

### 🚀 常用操作
#### 表达式计算
```bash
# 运行后输入表达式，如：x=5;x+3
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep12.Calc"
```

### 📝 关键注意事项
1. **重构而非重写**: EP11功能完全保留，只是重新组织包结构
2. **标准包布局**: 引入 `ast/` 和 `visitor/` 子包，符合现代编译器架构
3. **新增VarNode**: 相比EP11增加了变量支持（VarNode）
4. **可发现性**: 代码结构更清晰，易于定位和导航
5. **可扩展性**: 模块化结构便于添加新功能而不影响现有代码
6. **教学价值**: 展示如何将单体文件重构为模块化架构

### 🔍 调试技巧
1. **包结构查看**: 按包查看代码，理解模块边界
2. **AST追踪**: 通过visitor追踪AST构建和求值过程
3. **依赖关系**: 理解各包之间的依赖关系（visitor依赖ast）
4. **对比EP11**: 与EP11对比学习重构前后的差异

### 🤖 AI Agent 代码开发指南
#### 代码风格
- 遵循 `AGENTS.md` 中的规范
- 包命名: `org.teachfx.antlr4.ep12.{ast|visitor}`
- 类命名: PascalCase

#### 包结构设计原则
- **ast/包**: 只包含AST节点类型，无业务逻辑
- **visitor/包**: 包含所有visitor实现，与ast解耦
- **parser/包**: ANTLR生成，保持独立
- **根包**: 仅包含入口类和配置

#### 重构模式
- 从EP11到EP12的演进展示了代码组织重构的最佳实践
- 添加新功能时应保持包结构清晰

#### 测试开发
- 使用JUnit 5编写测试
- 测试文件放在 `src/test/java/org/teachfx/antlr4/ep12/`
- 测试各节点和visitor的正确性

---

## 📚 详细文档
- **Math.g4语法**: `src/main/antlr4/org/teachfx/antlr4/ep12/parser/Math.g4`
- **Calc.java入口**: `src/main/java/org/teachfx/antlr4/ep12/Calc.java`
- **AST节点**: `src/main/java/org/teachfx/antlr4/ep12/ast/`
- **Visitor实现**: `src/main/java/org/teachfx/antlr4/ep12/visitor/`

---

## 🔗 相关链接
- **[项目根 README](../README.md)** - 项目整体介绍
- **[AGENTS.md](../AGENTS.md)** - Agent开发指南
- **[EP11 README](../ep11/README.md)** - 前一个EP：AST构建与Visitor求值（重构前）
- **[EP13 README](../ep13/README.md)** - 后一个EP：符号表基础
- **[EP20 README](../ep20/README.md)** - 完整编译器（使用同样包结构）

---

*注意：EP12是重构演示模块，展示了如何将EP11的代码重新组织为更优的模块化结构。它为后续EP（如EP20）的完整编译器提供了标准的包布局参考。*
