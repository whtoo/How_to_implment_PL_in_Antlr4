# EP14 - 符号表系统

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 符号表（Symbol Table）设计与实现——编译器的第一个语义分析阶段
- **目标**: 掌握符号、作用域、类型的数据结构，实现符号收集与查找
- **在编译器流水线中的位置**: 语义分析前端（EP14 是首个「真正的」编译器组件）
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 运行时

### 📁 项目结构
```
ep14/
└── src/main/java/org/teachfx/antlr4/ep14/
    ├── Compiler.java                    # 入口程序
    ├── compiler/
    │   ├── MathExpr.g4                  # 数学表达式语法
    │   ├── MathExprParser.java          # 生成的解析器
    │   └── MathExprLexer.java           # 生成的词法分析器
    └── symtab/
        ├── Symbol.java                  # 符号基类
        ├── Scope.java                   # 作用域接口
        ├── BaseScope.java               # 作用域基类
        ├── SymbolTable.java             # 符号表管理器
        ├── Type.java                    # 类型接口
        ├── BuiltInTypeSymbol.java       # 内置类型符号
        ├── VariableSymbol.java          # 变量符号
        ├── ScopedSymbol.java            # 有作用域的符号
        └── SymbolCollector.java         # 符号收集 Visitor
```

### 🏗️ 核心组件
- **Symbol / Scope 体系**: `Symbol` 表示命名实体（变量、类型），`Scope` 表示作用域（命名空间）
- **SymbolTable**: 管理全局符号表，维护符号名到 Symbol 的映射
- **BuiltInTypeSymbol**: 内置类型（int、float 等）的符号表示
- **VariableSymbol**: 变量的符号表示，关联类型信息
- **SymbolCollector**: Visitor，遍历 ParseTree 收集所有符号到 SymbolTable

### 🔧 构建与测试
```bash
cd ep14
mvn clean compile
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" -Dexec.args="src/main/resources/t.math"
```

### 🚀 常用操作
```bash
# 编译并运行符号收集
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep14.Compiler" -Dexec.args="src/main/resources/t.math"
```

### 📝 关键注意事项
1. **首个语义分析**: 本 EP 是项目中第一个「语义分析」阶段——之前的 EP 只有语法分析
2. **符号 vs 节点**: AST 节点（EP11-EP13）描述「语法结构」，符号（本 EP）描述「语义实体」
3. **作用域链**: `BaseScope` 支持嵌套作用域，通过 `getEnclosingScope()` 形成作用域链
4. **为后续铺垫**: 符号表是类型检查（EP15+）、代码生成（EP19+）的基础数据结构

### 🔍 调试技巧
1. 在 `SymbolCollector` 的 `visit*` 方法中打断点，观察符号如何被注册
2. 打印 `SymbolTable` 内容，验证所有符号和作用域是否正确建立

### 🤖 AI Agent 代码开发指南
- **添加新符号类型**: 继承 `Symbol` 创建 `FunctionSymbol`，为函数定义做准备
- **扩展作用域**: 在 `BaseScope` 中添加局部作用域嵌套支持

---

*EP14 是编译器开发的里程碑——从「语法分析」跨入「语义分析」。符号表是所有后续编译阶段的基础数据结构。*
