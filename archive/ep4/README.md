# EP4 - 标签替代与 Visitor 求值

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 标签替代（labeled alternatives）与 Visitor 模式实现表达式求值
- **目标**: 掌握 `#Label` 语法生成独立 visitor 方法，实现带变量赋值的计算器
- **在编译器流水线中的位置**: ANTLR4 基础学习阶段
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 运行时

### 📁 项目结构
```
ep4/
└── src/main/java/org/teachfx/antlr4/
    ├── LabeledExpr.g4    # 带标签的表达式语法
    ├── Calc.java         # 入口程序
    └── EvalVisitor.java  # Visitor 求值实现
```

### 🏗️ 核心组件
- **LabeledExpr.g4**: 使用 `#printExpr`、`#assign`、`#MulDiv`、`#AddSub` 等标签，为每个替代分支生成独立 visitor 方法
- **EvalVisitor.java**: 实现 `LabeledExprVisitor<Integer>`，对每种语法节点执行求值逻辑
- **Calc.java**: 入口程序，驱动词法分析→语法分析→Visitor 求值流程

### 🔧 构建与测试
```bash
cd ep4
mvn clean compile
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep4.Calc"
```

### 🚀 常用操作
```bash
# 运行计算器（交互式）
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep4.Calc"
# 输入:
#   a = 5
#   b = 3
#   a + b * 2
# 输出: 11
```

### 📝 关键注意事项
1. **标签生成方法**: `#MulDiv` 标签会自动生成 `visitMulDiv()` 方法，避免在一个方法中处理所有分支
2. **变量存储**: EvalVisitor 内部用 `Map<String, Integer>` 存储变量值
3. **visitor vs listener**: 本 EP 使用 visitor 模式（返回值），非 listener 模式（事件驱动）
4. **旧目录结构**: 生成代码在源码目录中

### 🔍 调试技巧
1. 在 `EvalVisitor` 的各 `visit*` 方法中打断点，观察求值过程
2. 使用 `grun LabeledExpr prog -gui` 可视化语法树

### 🤖 AI Agent 代码开发指南
- **添加运算符**: 1) 在 `LabeledExpr.g4` 添加带标签的替代规则，2) 在 `EvalVisitor` 实现对应的 `visit` 方法
- **修改数据类型**: 将 `Integer` 改为 `Double` 需同时修改 visitor 泛型参数和求值逻辑

---

*EP4 是第一个「可运行程序」——通过 Visitor 模式，将语法树转换为可执行的求值逻辑。*
