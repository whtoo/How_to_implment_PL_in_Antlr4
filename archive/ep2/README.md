# EP2 - 数组初始化解析

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 嵌套数组初始化语法的解析（如 `{1, 2, {3, 4}}`）
- **目标**: 掌握递归语法规则，理解嵌套结构的解析
- **在编译器流水线中的位置**: ANTLR4 基础学习阶段
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 运行时

### 📁 项目结构
```
ep2/
└── src/main/java/org/teachfx/antlr4/
    ├── ArrayInit.g4              # 数组初始化语法
    ├── ShortToUnicodeString.java # Listener 实现（转 Unicode）
    ├── Translate.java            # Visitor 实现（翻译）
    └── Test.java                 # 测试入口
```

### 🏗️ 核心组件
- **ArrayInit.g4**: 递归规则 `value : INT | '{' value (',' value)* '}'` 解析嵌套数组
- **ShortToUnicodeString.java**: Listener 模式实现，将数组内容转为 Unicode 字符串
- **Translate.java**: Visitor 模式实现，对数组内容进行翻译/转换

### 🔧 构建与测试
```bash
cd ep2
mvn clean compile
# 注：EP1-EP16 为历史代码，未接入根 POM active modules
```

### 🚀 常用操作
```bash
# 运行转换程序
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep2.Translate"
# 输入: {1, 2, {3, 4}}
```

### 📝 关键注意事项
1. **递归规则**: `value` 规则递归引用自身，是处理嵌套结构的核心模式
2. **生成代码已提交**: 旧的提交方式，生成代码直接放在源码目录
3. **build.xml**: 使用 Ant 构建，非 Maven ANTLR 插件
4. **两种模式**: 同时演示了 Listener（ShortToUnicodeString）和 Visitor（Translate）

### 🔍 调试技巧
1. 用 `grun ArrayInit init -tree` 查看嵌套数组的语法树结构
2. 注意递归深度，过深的嵌套可能导致栈溢出

### 🤖 AI Agent 代码开发指南
- **添加新转换逻辑**: 在 `Translate.java` 的 `visit()` 方法中扩展
- **修改语法**: 编辑 `ArrayInit.g4` 后需重新生成解析器

---

*EP2 通过嵌套数组解析，展示了 ANTLR4 递归规则处理层次化数据的能力。*
