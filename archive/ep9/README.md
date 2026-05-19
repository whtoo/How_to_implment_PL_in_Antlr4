# EP9 - 嵌入式动作与直接求值

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: ANTLR4嵌入式动作、Java代码直接嵌入语法规则、即时求值
- **目标**: 演示如何在语法规则中直接嵌入Java代码实现即时求值（旧式方法）
- **在编译器流水线中的位置**: 直接求值阶段（语法分析时即时计算结果）
- **依赖关系**:
  - 内部依赖: 无
  - 外部依赖: ANTLR4 4.13.2 (语法解析)

### 📁 项目结构
```
ep9/
├── src/main/java/org/teachfx/antlr4/ep9/
│   └── Calc.java          # 计算器入口，演示嵌入式动作
├── src/main/antlr4/org/teachfx/antlr4/ep9/parser/
│   └── Expr.g4           # 表达式语法，含嵌入式Java动作
└── src/test/java/        # 单元测试
```

### 🏗️ 核心组件
- **语法定义**: Expr.g4 — 包含 `@parser::members` 代码块和内联求值动作
- **入口类**: Calc.java — 演示如何使用带嵌入式动作的parser
- **关键机制**: `returns [int v]` 属性语法传递求值结果

### 🔧 构建与测试
```bash
# 进入 EP9 目录
cd ep9

# 构建项目
mvn clean compile

# 运行计算器
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep9.Calc"
```

### 🚀 常用操作
#### 交互式计算
```bash
# 运行后输入表达式，如：1+2*3
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep9.Calc"
```

### 📝 关键注意事项
1. **嵌入式动作**: Java代码直接写在语法规则中，如 `{ $v = eval($a.v,$op.type,$b.v); }`
2. **`@parser::members`**: 用于向生成的parser添加字段和方法（如HashMap memory）
3. **`returns [int v]`**: 规则属性语法，用于从规则返回值
4. **紧耦合**: 动作与语法紧密耦合，不适合大型项目
5. **调试困难**: 嵌入式代码难以调试和维护
6. **非生产式**: 这种方法仅用于学习，现代编译器不推荐使用

### 🔍 调试技巧
1. **ANTLRWorks**: 使用ANTLRWorks工具可视化语法和动作执行
2. **生成的Parser**: 查看target目录下生成的parser代码理解动作插入位置
3. **日志输出**: 在嵌入式动作中添加System.out.println调试
4. **断点调试**: 在Calc.java中设置断点，单步执行

### 🤖 AI Agent 代码开发指南
#### 代码风格
- 遵循 `AGENTS.md` 中的规范
- 包命名: `org.teachfx.antlr4.ep9`
- 类命名: PascalCase

#### 常见任务模式
- **理解嵌入式动作**: 1) 阅读Expr.g4，2) 查看生成的parser，3) 理解动作执行时机
- **修改求值逻辑**: 直接在语法规则中修改Java代码

#### 局限性
- 本EP仅用于教学演示
- 不适合扩展为完整编译器
- 后续EP将展示正确的架构模式

---

## 📚 详细文档
- **Expr.g4语法**: `src/main/antlr4/org/teachfx/antlr4/ep9/parser/Expr.g4`
- **Calc.java入口**: `src/main/java/org/teachfx/antlr4/ep9/Calc.java`

---

## 🔗 相关链接
- **[项目根 README](../README.md)** - 项目整体介绍
- **[AGENTS.md](../AGENTS.md)** - Agent开发指南
- **[EP10 README](../ep10/README.md)** - 后一个EP：CSV数据处理进阶
- **ANTLR4文档**: https://www.antlr.org/

---

*注意：EP9是ANTLR4嵌入式动作的演示模块，展示了旧式求值方法。现代编译器应使用AST Visitor模式（参见EP11-EP12）。*
