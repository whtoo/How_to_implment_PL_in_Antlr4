# EP16 - Cymbol 多遍编译器前端

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 完整 Cymbol 语言多遍编译器/解释器前端
- **目标**: 实现词法分析→语法分析→符号定义→符号解析→解释执行的完整流水线
- **在编译器流水线中的位置**: 完整前端（EP16 是 EP1–EP16 阶段的集大成者，为 EP17+ 现代编译器架构奠基）
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 4.13.2

### 📁 项目结构
```
ep16/
├── src/main/antlr4/Cymbol.g4                     # Cymbol 完整语法定义
├── src/main/resources/t.cymbol                   # 示例 Cymbol 程序
└── src/main/java/org/teachfx/antlr4/ep16/
    ├── Compiler.java                             # 入口：编排多遍流水线
    ├── visitor/
    │   ├── LocalDefine.java                      # Pass 1: 符号定义
    │   ├── LocalResolver.java                    # Pass 2: 符号解析
    │   └── Interpreter.java                      # Pass 3: 解释执行
    ├── symtab/
    │   ├── Symbol.java / Scope.java              # 符号/作用域基类
    │   ├── TypeTable.java / Type.java            # 类型系统
    │   ├── BuiltInTypeSymbol.java              # 内置类型
    │   ├── VariableSymbol.java / MethodSymbol.java # 变量/方法符号
    │   ├── GlobalScope.java / LocalScope.java    # 全局/局部作用域
    │   └── BaseScope.java / ScopedSymbol.java
    └── misc/
        ├── MemorySpace.java                      # 运行时内存空间
        ├── FunctionSpace.java                    # 函数调用空间
        ├── ScopeUtil.java                        # 作用域工具
        └── CompilerLogger.java                   # 编译日志
```

### 🏗️ 核心组件
- **Cymbol.g4**: 完整 Cymbol 语法，支持函数、变量、if/else、while、表达式、多种类型（int/float/char/string/bool）
- **Compiler.java**: 编排三阶段流水线：`ParseTree → LocalDefine → LocalResolver → Interpreter`
- **LocalDefine (Pass 1)**: 遍历 ParseTree，在符号表中定义所有变量和函数
- **LocalResolver (Pass 2)**: 解析所有符号引用，建立变量→定义的链接
- **Interpreter (Pass 3)**: 执行程序，使用 MemorySpace/FunctionSpace 管理运行时状态

### 🔧 构建与测试
```bash
cd ep16
mvn clean compile
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep16.Compiler" -Dexec.args="src/main/resources/t.cymbol"
```

### 🚀 常用操作
```bash
# 运行 Cymbol 解释器
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep16.Compiler" -Dexec.args="src/main/resources/t.cymbol"

# 编写自己的 Cymbol 程序
# Cymbol 语法支持：
#   int add(int a, int b) { return a + b; }
#   void main() { print(add(3, 4)); }
```

### 📝 关键注意事项
1. **首个完整语言**: EP16 是项目中第一个完整的编程语言实现（Cymbol）
2. **多遍架构**: `Define → Resolve → Execute` 是经典的多遍编译器模式
3. **MemorySpace**: 运行时内存模型，支持嵌套作用域的变量存储
4. **FunctionSpace**: 函数调用管理，支持参数传递和返回值
5. **类型系统**: 包含 TypeTable 和 BuiltInTypeSymbol，为 EP17+ 的类型检查打下基础
6. **旧代码结构**: 使用 `accept()` 而非标准 visitor，是历史遗留风格

### 🔍 调试技巧
1. 在 `Compiler.main()` 中逐行跟踪三阶段执行流程
2. 在 `LocalDefine` 和 `LocalResolver` 中打印符号表状态
3. 在 `Interpreter.interpret()` 中观察 MemorySpace 的变化

### 🤖 AI Agent 代码开发指南
- **添加新语句**: 1) 在 `Cymbol.g4` 添加语法规则，2) 在 `LocalDefine` 定义新符号，3) 在 `Interpreter` 实现执行逻辑
- **添加新类型**: 在 `symtab/` 添加新 `BuiltInTypeSymbol`，更新 `TypeTable`
- **错误处理**: `CompilerLogger` 提供日志基础设施，可扩展为错误报告系统

---

*EP16 是 EP1–EP16 阶段的集大成者——从「Hello.g4」到完整的 Cymbol 解释器，展示了多遍编译器前端的核心模式。它是 EP17+ 现代编译器架构的直接前驱。*
