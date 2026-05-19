# EP6 - CSV 解析与数据处理

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: CSV（逗号分隔值）文件解析
- **目标**: 掌握结构化数据格式解析，实现数据提取与处理
- **在编译器流水线中的位置**: ANTLR4 基础学习阶段
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 运行时

### 📁 项目结构
```
ep6/
└── src/main/java/org/teachfx/antlr4/
    ├── CSV.g4          # CSV 语法定义
    ├── CSVReader.java  # CSV 数据读取器
    └── Data.csv        # 示例数据文件
```

### 🏗️ 核心组件
- **CSV.g4**: 极简 CSV 语法：`file : header row+; row : field (',' field)* '\n'; field : TEXT | STRING;`
- **CSVReader.java**: 使用生成的解析器读取 CSV 文件，提取字段数据
- **Data.csv**: 示例 CSV 数据用于测试

### 🔧 构建与测试
```bash
cd ep6
mvn clean compile
```

### 🚀 常用操作
```bash
# 读取并解析 CSV 文件
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep6.CSVReader" -Dexec.args="Data.csv"
```

### 📝 关键注意事项
1. **文本与字符串**: `TEXT` 匹配非特殊字符，`STRING` 匹配双引号包围的内容
2. **无 Visitor/Listener**: 本 EP 仅解析，无后续处理逻辑
3. **实用场景**: CSV 解析是数据工程中的常见任务
4. **旧目录结构**: 语法文件在 `src/main/java/` 下

### 🔍 调试技巧
1. 用 `grun CSV file -tree` 查看 CSV 文件的语法树
2. 检查 Data.csv 格式是否符合语法预期

### 🤖 AI Agent 代码开发指南
- **扩展数据处理**: 在 `CSVReader.java` 中添加字段转换、过滤、聚合逻辑
- **支持更多格式**: 修改 `CSV.g4` 添加制表符分隔（TSV）支持

---

*EP6 展示了 ANTLR4 在日常数据处理中的应用——解析 CSV 这种最常见的结构化数据格式。*
