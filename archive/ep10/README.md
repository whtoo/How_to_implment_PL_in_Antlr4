# EP10 - CSV数据处理进阶

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: CSV数据解析、数据计算、数据处理管道
- **目标**: 将ANTLR4语法解析应用于实际数据处理场景
- **在编译器流水线中的位置**: 数据解析与计算层
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 4.13.2 (语法解析)

### 📁 项目结构
```
ep10/
├── src/main/java/org/teachfx/antlr4/ep10/
│   └── Calc.java          # CSV数据处理器入口
├── src/main/antlr4/org/teachfx/antlr4/ep10/parser/
│   └── CSV.g4            # CSV语法定义（与EP6相同）
└── src/test/java/        # 单元测试
```

### 🏗️ 核心组件
- **语法定义**: CSV.g4 — CSV格式解析规则
- **入口类**: Calc.java — 读取CSV数据并执行计算
- **处理逻辑**: 数据转换、聚合计算、统计分析

### 🔧 构建与测试
```bash
# 进入 EP10 目录
cd ep10

# 构建项目
mvn clean compile

# 运行CSV处理器
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep10.Calc"
```

### 🚀 常用操作
#### CSV数据处理
```bash
# 处理CSV文件并执行计算
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep10.Calc" -Dexec.args="data.csv"
```

### 📝 关键注意事项
1. **复用CSV语法**: 使用与EP6相同的CSV.g4语法
2. **数据处理焦点**: 本EP重点不在语法创新，而在数据处理逻辑
3. **解析计算分离**: CSV解析与数据计算逻辑分离
4. **实用场景**: 演示ANTLR4在数据处理领域的实际应用
5. **可扩展性**: 可扩展支持更复杂的数据格式和计算
6. **管道思想**: 体现解析→处理→输出的数据管道概念

### 🔍 调试技巧
1. **CSV格式验证**: 确认输入CSV格式正确
2. **解析树查看**: 使用 `-Ps` 查看生成的解析树
3. **计算逻辑调试**: 在Calc.java中添加调试输出
4. **边界情况**: 测试空值、特殊字符、大数值等边界情况

### 🤖 AI Agent 代码开发指南
#### 代码风格
- 遵循 `AGENTS.md` 中的规范
- 包命名: `org.teachfx.antlr4.ep10`
- 类命名: PascalCase

#### 常见任务模式
- **扩展计算功能**: 在Calc.java中添加新的计算方法
- **支持新CSV格式**: 修改CSV.g4语法以支持新格式
- **添加错误处理**: 增强CSV解析的错误恢复能力

#### 测试开发
- 使用JUnit 5编写测试
- 测试文件放在 `src/test/java/org/teachfx/antlr4/ep10/`

---

## 📚 详细文档
- **CSV.g4语法**: `src/main/antlr4/org/teachfx/antlr4/ep10/parser/CSV.g4`
- **Calc.java入口**: `src/main/java/org/teachfx/antlr4/ep10/Calc.java`

---

## 🔗 相关链接
- **[项目根 README](../README.md)** - 项目整体介绍
- **[AGENTS.md](../AGENTS.md)** - Agent开发指南
- **[EP9 README](../ep9/README.md)** - 前一个EP：嵌入式动作与直接求值
- **[EP11 README](../ep11/README.md)** - 后一个EP：AST构建与Visitor求值
- **[EP6 README](../ep6/README.md)** - 基础CSV语法

---

*注意：EP10是ANTLR4数据处理应用的演示，展示了将语法解析技术应用于现实世界数据处理问题的能力。*
