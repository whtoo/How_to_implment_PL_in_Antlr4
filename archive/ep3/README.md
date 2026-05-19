# EP3 - 表达式语法与公共词法规则

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 表达式语法定义与可复用词法规则库
- **目标**: 掌握语法模块化（grammar imports）和公共词法规则提取
- **在编译器流水线中的位置**: ANTLR4 基础学习阶段
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 运行时

### 📁 项目结构
```
ep3/
└── src/main/java/org/teachfx/antlr4/
    ├── CommonLexRules.g4    # 公共词法规则（ID、INT、WS）
    └── LibExpr.g4           # 表达式语法（导入 CommonLexRules）
```

### 🏗️ 核心组件
- **CommonLexRules.g4**: 提取公共词法规则（`ID`、`INT`、`WS`），供多个语法复用
- **LibExpr.g4**: 数学表达式语法，通过 `import CommonLexRules;` 复用词法规则

### 🔧 构建与测试
```bash
cd ep3
mvn clean compile
# 注：EP1-EP16 为历史代码，未接入根 POM active modules
```

### 🚀 常用操作
```bash
# 使用 grun 测试表达式解析
grun LibExpr stat -tree
# 输入: 1 + 2 * 3
```

### 📝 关键注意事项
1. **无自定义 Java 代码**: 纯语法练习，无实现类
2. **Grammar Import**: `LibExpr.g4` 使用 `import CommonLexRules;` 演示语法模块化
3. **`@header` 声明**: 语法文件中使用 `@header { package ...; }` 指定生成代码的包名
4. **旧目录结构**: 语法文件放在 `src/main/java/` 下

### 🔍 调试技巧
1. 如果 import 失败，确保两个 `.g4` 文件在同一目录
2. 使用 `grun LibExpr stat -tokens` 验证词法分析结果

### 🤖 AI Agent 代码开发指南
- **扩展表达式**: 在 `LibExpr.g4` 中添加新的运算符或优先级层级
- **复用词法规则**: 新建语法文件时通过 `import CommonLexRules;` 复用

---

*EP3 展示了 ANTLR4 的语法模块化能力——将公共词法规则提取到独立文件，供多个语法复用。*
