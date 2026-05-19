# EP11 - AST构建与Visitor求值

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: AST构建、Visitor模式、双阶段求值、标准编译器模式
- **目标**: 演示正确分离解析树到AST的构建，以及AST的Visitor求值模式
- **在编译器流水线中的位置**: AST构建阶段 → 求值阶段（标准编译器前端模式）
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 4.13.2 (语法解析)

### 📁 项目结构
```
ep11/
├── src/main/java/org/teachfx/antlr4/ep11/
│   ├── Calc.java              # 计算器入口
│   ├── ExpressionNode.java     # AST节点基类
│   ├── NumberNode.java         # 数字节点
│   ├── NegateNode.java         # 取反节点
│   ├── InfixExpressionNode.java # 中缀表达式节点
│   ├── MultiplicationNode.java  # 乘法节点
│   ├── DivisionNode.java        # 除法节点
│   ├── BuildAstVisitor.java     # 构建AST的Visitor
│   ├── EvalExprVisitor.java     # 求值AST的Visitor
│   └── ASTVisitor.java          # Visitor接口
├── src/main/antlr4/org/teachfx/antlr4/ep11/parser/
│   └── Math.g4                # 数学表达式语法
└── src/test/java/             # 单元测试
```

### 🏗️ 核心组件
- **语法定义**: Math.g4 — 带标签 alternatives 的表达式语法
- **AST节点**: ExpressionNode（基类）、NumberNode、NegateNode、InfixExpressionNode等
- **BuildAstVisitor**: 将ParseTree转换为自定义AST的Visitor
- **EvalExprVisitor**: 对AST进行求值的Visitor
- **双阶段模式**: 构建阶段（ParseTree→AST） + 求值阶段（AST→结果）

### 🔧 构建与测试
```bash
# 进入 EP11 目录
cd ep11

# 构建项目
mvn clean compile

# 运行计算器
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep11.Calc"

# 运行测试
mvn test
```

### 🚀 常用操作
#### 表达式计算
```bash
# 运行后输入表达式，如：1+2*3
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep11.Calc"
```

### 📝 关键注意事项
1. **双阶段设计**: BuildAstVisitor（构建）与EvalExprVisitor（求值）分离
2. **标准模式**: 这是现代编译器的标准模式，清晰且易于维护
3. **标签Alternatives**: Math.g4使用 `expr ('*' | '/') expr` 标签区分不同操作
4. **AST节点组织**: 节点位于root package（EP12才引入ast/子包）
5. **Visitor接口**: ASTVisitor定义accept方法，每个节点实现accept调用visitor
6. **可扩展性**: 添加新节点类型只需实现节点类和更新Visitor

### 🔍 调试技巧
1. **AST可视化**: 打印AST节点结构查看构建结果
2. **Visitor追踪**: 在Visitor方法中添加日志追踪执行流程
3. **解析树对比**: 对比ParseTree和AST结构理解转换逻辑
4. **单元测试**: 使用测试用例验证各节点类型的正确性

### 🤖 AI Agent 代码开发指南
#### 代码风格
- 遵循 `AGENTS.md` 中的规范
- 包命名: `org.teachfx.antlr4.ep11`
- 类命名: PascalCase (如 `BuildAstVisitor`, `EvalExprVisitor`)

#### 常见任务模式
- **添加新节点类型**: 1) 创建新Node类继承ExpressionNode，2) 在BuildAstVisitor中添加创建逻辑，3) 在EvalExprVisitor中添加求值逻辑
- **修改语法**: 1) 更新Math.g4，2) 重新生成parser，3) 更新BuildAstVisitor

#### 测试开发
- 使用JUnit 5编写测试
- 测试文件放在 `src/test/java/org/teachfx/antlr4/ep11/`
- 测试各节点类型的构建和求值正确性

---

## 📚 详细文档
- **Math.g4语法**: `src/main/antlr4/org/teachfx/antlr4/ep11/parser/Math.g4`
- **Calc.java入口**: `src/main/java/org/teachfx/antlr4/ep11/Calc.java`
- **BuildAstVisitor**: `src/main/java/org/teachfx/antlr4/ep11/BuildAstVisitor.java`
- **EvalExprVisitor**: `src/main/java/org/teachfx/antlr4/ep11/EvalExprVisitor.java`

---

## 🔗 相关链接
- **[项目根 README](../README.md)** - 项目整体介绍
- **[AGENTS.md](../AGENTS.md)** - Agent开发指南
- **[EP10 README](../ep10/README.md)** - 前一个EP：CSV数据处理进阶
- **[EP12 README](../ep12/README.md)** - 后一个EP：模块化AST架构
- **[EP20 README](../ep20/README.md)** - 完整编译器（使用同样模式）

---

*注意：EP11是正确编译器架构的起点，展示了ParseTree→AST→求值的标准模式。这为后续EP20+的完整编译器奠定了基础。EP12将在此基础上引入模块化包结构。*
