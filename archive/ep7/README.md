# EP7 - JSON 解析

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: JSON 数据格式解析
- **目标**: 掌握层次化数据结构的语法定义（对象、数组、嵌套）
- **在编译器流水线中的位置**: ANTLR4 基础学习阶段
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 运行时

### 📁 项目结构
```
ep7/
└── src/main/java/org/teachfx/antlr4/
    ├── JSON.g4     # JSON 语法定义
    └── t.json      # 示例 JSON 文件
```

### 🏗️ 核心组件
- **JSON.g4**: 完整 JSON 语法，支持对象 `{}`、数组 `[]`、字符串、数字、`true`、`false`、`null`
- **无自定义 Java 代码**: 纯语法练习

### 🔧 构建与测试
```bash
cd ep7
mvn clean compile
```

### 🚀 常用操作
```bash
# 解析 JSON 文件并查看语法树
grun JSON json -tree < t.json
# 或 GUI 模式
grun JSON json -gui < t.json
```

### 📝 关键注意事项
1. **纯语法练习**: 无自定义 Java 实现类，仅验证 JSON 语法定义
2. **层次化结构**: JSON 的对象/数组嵌套是理解递归规则的好例子
3. **旧目录结构**: 语法文件在 `src/main/java/` 下
4. **实际应用**: 本 EP 的 JSON 语法可作为更大系统的基础（如 JSON 校验器、转换器）

### 🔍 调试技巧
1. 用 `grun JSON json -tokens` 查看 JSON 的词法 Token 序列
2. 测试各种 JSON 结构：嵌套对象、数组混合、转义字符串

### 🤖 AI Agent 代码开发指南
- **添加语义处理**: 创建 Visitor 将 ParseTree 转换为 Java 的 `Map`/`List` 对象
- **错误恢复**: 在语法中添加错误恢复规则，提升对 malformed JSON 的容错能力

---

*EP7 通过 JSON 解析展示了 ANTLR4 处理层次化数据结构的能力——对象嵌套、数组、多种数据类型。*
