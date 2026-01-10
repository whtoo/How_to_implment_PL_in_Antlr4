# EP19 - Cymbol 编译器改进版

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 编译器管道重构、类型检查增强、符号表完善、结构体方法调用、错误恢复
- **目标**: 改进Cymbol编译器前端，修复关键Bug，提升测试通过率，增强类型系统和符号表功能
- **在编译器流水线中的位置**: 前端改进（语法分析、语义分析、符号表管理）
- **依赖关系**: 
  - 内部依赖: 无
  - 外部依赖: Log4j2 (日志), ANTLR4 4.13.2 (语法解析)

### 📁 项目结构
```
ep19/
├── src/main/java/org/teachfx/antlr4/ep19/
│   ├── symtab/          # 符号表与类型系统 (TypeTable, StructSymbol, TypedefSymbol)
│   ├── pass/            # 编译器阶段 (LocalDefine, LocalResolver, TypeCheckVisitor, Interpreter, CymbolAssembler)
│   ├── pipeline/        # 编译器管道 (ConfigurableCompilerPipeline, DefaultCompilerPipeline)
│   └── runtime/         # 运行时结构 (StructInstance, MemorySpace)
├── src/main/antlr4/org/teachfx/antlr4/ep19/parser/Cymbol.g4   # Cymbol语法定义
├── docs/                # 详细文档中心 (按主题组织)
└── src/test/java/      # 单元测试 (StructAndTypedefTest, TypeSystemTest, PerformanceBenchmarkTest等)
```

### 🏗️ 核心组件
- **编译器管道**: ConfigurableCompilerPipeline (可配置管道), DefaultCompilerPipeline (默认实现)
- **符号表与类型系统**: TypeTable (类型表), StructSymbol (结构体符号), TypedefSymbol (类型定义符号), ArrayType (数组类型)
- **语义分析Pass**: LocalDefine (局部定义), LocalResolver (局部解析), TypeCheckVisitor (类型检查访问者)
- **运行时结构**: StructInstance (结构体实例), MemorySpace (内存空间), FunctionSpace (函数空间)

### 🔧 构建与测试
```bash
# 进入 EP19 目录
cd ep19

# 构建项目
mvn clean compile

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=StructAndTypedefTest
mvn test -Dtest=TypeSystemTest
mvn test -Dtest=PerformanceBenchmarkTest

# 运行单个测试方法
mvn test -Dtest=TypeSystemTest#testStructTypeEquality
```

### 🚀 常用操作
#### 编译运行示例
```bash
# 运行编译器管道处理Cymbol源码
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep19.pipeline.DefaultCompilerPipeline" -Dexec.args="src/main/resources/example.cymbol"

# 运行解释器执行Cymbol程序
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep19.pass.Interpreter" -Dexec.args="src/main/resources/example.cymbol"
```

### 📝 关键注意事项
1. **测试通过率提升**: 从43%提升至53%，修复了关键Bug
2. **函数调用NPE修复**: 解决了函数调用时的空指针异常问题
3. **作用域检测修复**: 修复了return语句与函数的关联问题
4. **解释器输出修复**: 实现了print函数，修复解释器输出系统
5. **结构体方法调用**: 实现了结构体方法调用语法支持
6. **参数处理标准化**: 统一了函数参数处理逻辑

### 🔍 调试技巧
1. **日志配置**: 使用Log4j2配置详细日志输出，查看编译器各阶段执行过程
2. **管道调试**: ConfigurableCompilerPipeline支持阶段间调试输出
3. **符号表查看**: TypeTable提供类型信息查询和验证
4. **错误恢复**: 增强的错误恢复机制提供更详细的错误信息

### 🤖 AI Agent 代码开发指南
#### 代码风格
- 遵循 `AGENTS.md` 中的规范
- 包命名: `org.teachfx.antlr4.ep19.{package}`
- 类命名: PascalCase (如 `ConfigurableCompilerPipeline`, `TypeCheckVisitor`)
- 方法命名: camelCase (如 `resolveLocalSymbols`, `typeCheckExpression`)

#### 常见任务模式
- **添加新语法特性**: 1) 更新Cymbol.g4语法文件，2) 更新AST节点，3) 更新相关Visitor，4) 添加测试
- **扩展类型系统**: 1) 在TypeTable中添加类型定义，2) 更新类型检查逻辑，3) 添加类型转换支持
- **添加优化Pass**: 1) 实现新的Visitor类，2) 集成到ConfigurableCompilerPipeline，3) 添加测试验证

#### 测试开发
- 使用JUnit 5编写测试
- 遵循TDD方法论：红-绿-重构循环
- 测试文件放在 `src/test/java/org/teachfx/antlr4/ep19/`
- 测试类名以Test结尾 (如 `StructAndTypedefTest`)

---

## 📚 详细文档
EP19有完整的主题化文档体系，位于 `docs/` 目录：

### 文档中心导航
- **[docs/README.md](docs/README.md)** - 文档中心主页面，按主题组织完整文档体系

### 核心设计文档
- **核心改进总结**: `docs/01_core_design/EP19_编译器改进总结_技术版.md`
  - 函数调用系统重大修复（NPE问题解决）
  - 函数作用域检测修复（return语句关联）
  - 解释器输出系统修复（print函数实现）
  - 结构体方法调用语法实现
  - 参数处理标准化统一
  - 测试结果改进分析（43%→53%成功率）

### 实现与测试标准
- **开发任务与技术标准**: `docs/02_implementation_standards/EP19_开发任务与技术标准_整合版.md`
  - 架构改进任务清单（编译器管道、嵌套结构体等）
  - 代码质量标准（异常处理、命名规范、输入验证）
  - 测试改进计划（集成测试、错误恢复、性能基准）

### 开发计划
- **待补充**: `docs/03_development_plans/` - TDD方法论应用、开发里程碑

### 问题与改进
- **待补充**: `docs/04_issues_improvements/` - 已知问题、性能优化机会

### 跨EP协调
- **待补充**: `docs/05_cross_ep_coordination/` - 跨EP功能依赖、数据结构共享

---

## 🔗 相关链接
- **[项目根 README](../README.md)** - 项目整体介绍
- **[AGENTS.md](../AGENTS.md)** - Agent开发指南，包含构建命令、代码风格等
- **[EP18 README](../ep18/README.md)** - 前一个EP：Cymbol虚拟机与垃圾回收系统
- **[EP20 README](../ep20/README.md)** - 后一个EP：完整编译器架构与优化

---

*注意：EP19是Cymbol编译器的改进版本，专注于修复关键Bug、提升测试通过率和增强编译器前端功能。它为后续的EP20完整编译器架构奠定了基础。*
