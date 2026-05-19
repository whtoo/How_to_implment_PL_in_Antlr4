# EP1 - 最小语法与 ANTLR4 入门

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 最小 ANTLR4 语法定义与工具链初体验
- **目标**: 理解 `.g4` 语法文件基本结构，能用 `grun` 验证语法
- **在编译器流水线中的位置**: ANTLR4 基础学习阶段（EP1–EP7 为语法基础）
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 工具（命令行）

### 📁 项目结构
```
ep1/
└── src/main/java/org/teachfx/antlr4/
    └── Hello.g4          # 最小语法定义（4 行）
```

### 🏗️ 核心组件
- **Hello.g4**: 定义 `r : 'hello' ID;` 规则，演示词法规则（`ID`）和语法规则（`r`）
- **无 Java 代码**: 本 EP 纯语法文件，无自定义实现类

### 🔧 构建与测试
```bash
cd ep1
# 注：EP1 未接入根 POM active modules，需手动使用 ANTLR 工具
antlr4 src/main/java/org/teachfx/antlr4/Hello.g4
javac Hello*.java
grun Hello r -tree
# 输入: hello world
# 预期输出: (r hello world)
```

### 🚀 常用操作
```bash
# 生成并查看语法树
grun Hello r -tree
# 生成并查看语法树（GUI）
grun Hello r -gui
```

### 📝 关键注意事项
1. **无 Java 入口类**: 本 EP 只有 `.g4` 文件，没有 `main()` 方法
2. **手动工具链**: 需安装 ANTLR4 命令行工具（`antlr4`、`grun`）
3. **旧目录结构**: 语法文件放在 `src/main/java/` 下（非标准 `src/main/antlr4/`）
4. **无 pom 构建**: 未配置 ANTLR Maven 插件

### 🔍 调试技巧
1. 如果 `grun` 报错，检查 `CLASSPATH` 是否包含 `antlr4-runtime.jar`
2. 使用 `grun Hello r -tokens` 查看词法 Token 流

### 🤖 AI Agent 代码开发指南
- 本 EP 无代码开发任务，纯语法理解练习
- 若修改语法，只需编辑 `Hello.g4` 后重新运行 `antlr4` + `javac`

---

*EP1 是 ANTLR4 的「Hello World」——用 4 行语法定义一门极简语言。*
