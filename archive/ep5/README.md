# EP5 - Listener 模式与 Java 源码分析

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: ANTLR4 Listener 模式与 Java 源码接口提取
- **目标**: 掌握 Listener 模式（事件驱动树遍历），实现源码到源码的转换
- **在编译器流水线中的位置**: ANTLR4 基础学习阶段
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 运行时

### 📁 项目结构
```
ep5/
└── src/main/java/org/teachfx/antlr4/
    ├── Java.g4                      # 完整 Java 语言语法（大型语法）
    ├── ExtractInterfaceTool.java    # 入口程序
    ├── ExtractInterfaceListener.java # Listener 实现
    └── Demo.java                    # 演示目标文件
```

### 🏗️ 核心组件
- **Java.g4**: 来自 ANTLR 官方语法仓库的完整 Java 语法，涵盖类、接口、方法、字段等全部 Java 语法结构
- **ExtractInterfaceListener.java**: 实现 `JavaBaseListener`，监听 `enterClassDeclaration` 等事件，提取 public 方法签名并生成接口代码
- **ExtractInterfaceTool.java**: 入口程序，使用 `ParseTreeWalker` 遍历语法树并驱动 Listener

### 🔧 构建与测试
```bash
cd ep5
mvn clean compile
# 注：EP1-EP16 为历史代码，未接入根 POM active modules
```

### 🚀 常用操作
```bash
# 从 Java 源文件提取接口
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep5.ExtractInterfaceTool" -Dexec.args="Demo.java"
# 输出: 生成的接口定义
```

### 📝 关键注意事项
1. **Listener vs Visitor**: Listener 是事件驱动（enter/exit），Visitor 是返回值驱动。本 EP 使用 Listener。
2. **ParseTreeWalker**: 负责遍历语法树并触发 Listener 的 enter/exit 方法
3. **完整 Java 语法**: Java.g4 是大型语法文件（数百行），解析速度较慢
4. **源码到源码**: 本 EP 演示的是 transpilation（代码转换），非编译

### 🔍 调试技巧
1. 在 `ExtractInterfaceListener` 的 `enter*`/`exit*` 方法中打断点
2. 使用 `grun Java compilationUnit -tree` 查看 Java 文件的语法树（非常大）

### 🤖 AI Agent 代码开发指南
- **添加新提取逻辑**: 在 `ExtractInterfaceListener` 中覆盖更多 `enter*`/`exit*` 方法
- **修改目标语言**: 替换 `Java.g4` 为其他语言语法，重写 Listener 逻辑

---

*EP5 展示了 Listener 模式的典型应用场景——通过监听语法树事件，实现 Java 源码的接口自动提取。*
