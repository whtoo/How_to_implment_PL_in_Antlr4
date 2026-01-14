# EP21 - 高级编译器优化

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 静态单赋值（SSA）形式、数据流分析、尾递归优化（TRO）、分层中间表示（MIR/LIR）、高级优化算法
- **目标**: 实现高级编译器优化技术，包括SSA转换、数据流分析、尾递归优化等，提升代码性能
- **在编译器流水线中的位置**: 高级优化层（在基础编译器之上添加高级优化）
- **依赖关系**: 
  - 内部依赖: ep18 (虚拟机目标), ep18r (增强版虚拟机目标)
  - 外部依赖: JGraphT (图算法库), ANTLR4 4.13.2 (语法解析)

### 📁 项目结构
```
ep21/
├── src/main/java/org/teachfx/antlr4/ep21/
│   ├── ast/             # AST节点定义 (CompileUnit, FuncDeclNode, BinaryExprNode等)
│   ├── ir/              # IR核心 (IRNode, Prog, Expr等)
│   ├── ir/mir/          # 中层中间表示 (MIRNode, MIRFunction, MIRAssignStmt, MIRToLIRConverter)
│   ├── pass/cfg/        # 控制流分析与优化 (ControlFlowAnalysis, TailRecursionOptimizer, ConstantFoldingOptimizer等)
│   └── integration/     # 集成测试相关
├── src/main/antlr4/org/teachfx/antlr4/ep21/Cymbol.g4   # Cymbol语法定义
├── benchmarks/          # 基准测试集 (stanford/, spec/, optimization/)
├── docs/               # 详细文档中心 (核心设计、实现标准、开发计划等)
└── src/test/java/     # 单元测试 (CFGBuilderTest, TailRecursionOptimizerTest等)
```

### 🏗️ 核心组件
- **分层中间表示**: MIRNode (中层IR节点), MIRFunction (MIR函数), MIRToLIRConverter (MIR到LIR转换器)
- **优化Pass**: TailRecursionOptimizer (尾递归优化), ConstantFoldingOptimizer (常量折叠), DeadCodeEliminationOptimizer (死代码消除), CommonSubexpressionEliminationOptimizer (公共子表达式消除)
- **控制流分析**: CFGBuilder (控制流图构建器), ControlFlowAnalysis (控制流分析)
- **数据流分析**: 支持SSA形式的到达定义、活性分析等
- **集成测试**: VMCodeGenerationIntegrationTest (虚拟机代码生成集成测试)

### 🔧 构建与测试
```bash
# 进入 EP21 目录
cd ep21

# 构建项目 (会同时构建依赖的ep18和ep18r)
mvn clean compile

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=CFGBuilderTest
mvn test -Dtest=TailRecursionOptimizerTest
mvn test -Dtest=FibonacciTailRecursionEndToEndTest
mvn test -Dtest=VMCodeGenerationIntegrationTest

# 运行单个测试方法
mvn test -Dtest=TailRecursionOptimizerTest#testSimpleTailRecursion
mvn test -Dtest=VMCodeGenerationIntegrationTest#testFibonacciCompilation
```

### 🚀 常用操作
#### 编译运行示例
```bash
# 运行高级优化编译器（生成EP18虚拟机字节码）
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep21.integration.EP21Compiler" -Dexec.args="src/main/resources/example.cymbol output.vm"

# 运行基准测试
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep21.benchmark.BenchmarkRunner" -Dexec.args="benchmarks/stanford/fib.cymbol"
```

### 05_technical/ - 技术文档（新增）
- **SSA-Construction.md**: SSA形式定义、支配边界算法、PHI节点插入策略、SSA优化机会和销毁流程
- **Register-Allocation.md**: 寄存器分配理论、活跃变量分析、干扰图构建、线性扫描、图着色、寄存器合并和溢出策略
- **Dataflow-Analysis.md**: 数据流分析理论基础、格理论、传递函数、Meet操作、Worklist算法、前向/后向分析和MLIR框架参考
- **Loop-Optimizations.md**: 循环识别和分析、自然循环结构、循环不变代码外提、归纳变量分析、强度削减、循环展开、分块和融合
- **EP21_TDD执行标准.md**: TDD流程定义、红-绿-重构循环、三层测试金字塔、测试覆盖率目标、重构安全网机制

#### 基准测试
```bash
# 查看基准测试集说明
cat benchmarks/README.md

# 运行Stanford基准测试集
cd benchmarks/stanford
./run_all.sh

# 运行优化专项测试
cd benchmarks/optimization
./run_tests.sh
```

### 📝 关键注意事项
1. **SSA形式**: 支持静态单赋值形式的中间表示，便于数据流分析
2. **尾递归优化**: 自动将尾递归转换为循环，提升性能
3. **分层IR**: 使用MIR（中层IR）和LIR（低层IR）分层设计
4. **图算法依赖**: 使用JGraphT库进行CFG和SSA图算法
5. **EP18/EP18R目标**: 依赖EP18和EP18R作为虚拟机目标
6. **基准测试集**: 包含Stanford基准、SPEC风格测试和优化专项测试

### 🔍 调试技巧
1. **MIR输出**: 使用MIRVisitor输出中层IR结构
2. **CFG可视化**: CFGBuilder支持控制流图可视化输出
3. **数据流分析调试**: 数据流分析框架提供详细的调试输出
4. **优化效果验证**: 优化Pass提供优化前后对比输出
5. **基准测试分析**: 基准测试集提供性能对比数据

### 🤖 AI Agent 代码开发指南
#### 代码风格
- 遵循 `AGENTS.md` 中的规范
- 包命名: `org.teachfx.antlr4.ep21.{package}`
- 类命名: PascalCase (如 `TailRecursionOptimizer`, `ConstantFoldingOptimizer`)
- 方法命名: camelCase (如 `convertToSSAForm`, `analyzeDataFlow`)

#### 常见任务模式
- **添加新优化算法**: 1) 实现新的优化Visitor，2) 集成到优化管道，3) 验证优化正确性，4) 性能基准测试
- **扩展数据流分析**: 1) 实现新的数据流分析框架，2) 集成到SSA转换，3) 验证分析精度
- **添加基准测试**: 1) 添加测试程序到benchmarks/，2) 创建测试脚本，3) 验证性能提升

#### 测试开发
- 使用JUnit 5编写测试
- 测试文件放在 `src/test/java/org/teachfx/antlr4/ep21/`
- 测试类名以Test结尾 (如 `TailRecursionOptimizerTest`)
- 集成测试验证端到端功能
- 基准测试验证性能提升

---

## 📚 详细文档
EP21有完整的主题化文档体系，位于 `docs/` 目录：

### 文档中心导航
- **[docs/README.md](docs/README.md)** - 文档中心主页面，按主题组织完整文档体系

### 核心设计文档
- **架构设计规范**: `docs/01_core_design/架构设计规范.md`
  - 编译器分层架构（前端/中端/后端）
  - 分层中间表示（MIR/LIR）设计
  - 控制流分析与数据流分析框架
- **语言规范**: `docs/01_core_design/语言规范.md`
  - Cymbol语言EP21版本完整定义
  - SSA形式转换与优化Pass
  - 尾递归优化（TRO）实现

### 实现与测试标准
- **测试规范整合版**: `docs/02_implementation_standards/EP21_测试规范_整合版.md`
  - 测试策略与架构（单元/集成/系统测试）
  - TDD执行计划与方法论
  - 测试用例详细规范
- **研究生进阶任务**: `docs/02_implementation_standards/研究生进阶任务.md`
  - 高级优化算法实现指南
  - 研究生研究课题

### 开发计划
- **TDD执行计划整合版**: `docs/03_development_plans/EP21_TDD执行计划_整合版.md`
  - TDD方法论在EP21的应用
  - 红-绿-重构循环标准
  - 开发里程碑和交付物
- **改进计划**: `docs/03_development_plans/改进计划.md`
  - 项目改进规划

### 跨EP协调
- **VM目标评估**: `docs/04_cross_ep_coordination/VM_TARGET_EVALUATION.md`
  - EP21→EP18/EP18R编译可行性评估
- **VM目标任务分解**: `docs/04_cross_ep_coordination/VM_TARGET_TASK_BREAKDOWN.md`
  - 跨EP虚拟机适配任务分解
- **EP18R-EP21联动融合计划**: `docs/04_cross_ep_coordination/EP18R-EP21联动融合计划.md`
  - EP18R与EP21的集成计划

### 基准测试
- **基准测试集说明**: `benchmarks/README.md` - 基准测试集详细说明
- **Stanford基准**: `benchmarks/stanford/` - 经典算法测试（fib、matmul、quicksort等）
- **SPEC风格测试**: `benchmarks/spec/` - 标准性能测试
- **优化专项测试**: `benchmarks/optimization/` - 优化算法专项测试

---

## 🔗 相关链接
- **[项目根 README](../README.md)** - 项目整体介绍
- **[AGENTS.md](../AGENTS.md)** - Agent开发指南，包含构建命令、代码风格等
- **[EP20 README](../ep20/README.md)** - 前一个EP：完整编译器架构
- **[EP18 README](../ep18/README.md)** - 目标虚拟机：Cymbol虚拟机与垃圾回收系统
- **[EP18R README](../ep18r/README.md)** - 增强版目标虚拟机

---

*注意：EP21是当前最先进的编译器优化模块，实现SSA形式、数据流分析、尾递归优化等高级优化技术。它依赖EP18/EP18R作为虚拟机目标，是编译器技术的前沿探索。*
