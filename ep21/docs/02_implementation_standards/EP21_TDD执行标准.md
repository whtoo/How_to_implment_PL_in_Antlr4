# EP21 测试驱动开发执行标准

## 📋 文档目的

本文档定义EP21模块的测试驱动开发（Test-Driven Development, TDD）标准、流程和最佳实践，确保高质量、可维护的代码开发和优化Pass实现。

---

## 🎯 TDD 核心原则

### 1. 测试先行（Test First）

**定义**：在编写功能代码之前，先编写测试用例。

**红-绿-重构循环**：
```
Red（红）: 编写失败的测试 → 明确需求
    ↓
Green（绿）: 编写最小化实现 → 使测试通过
    ↓
Refactor（重构）: 优化代码结构，保持测试通过
```

**执行流程**：
1. 编写测试（明确失败行为）
2. 编写最小实现
3. 运行测试（验证通过）
4. 重构优化
5. 运行测试（确认无回归）

### 2. 小步快速迭代（Small Steps, Fast Feedback）

**原则**：保持每个迭代在15-30分钟内完成，快速获得反馈。

**实践**：
- 每次只实现一个最小可测功能
- 持续运行测试套件，确保无回归
- 使用自动测试监视（IDE、Maven）

### 3. 可重复测试（Repeatable Tests）

**要求**：测试应当独立、可重复、无副作用。

**检查清单**：
- [ ] 测试不依赖外部文件或网络
- [ ] 测试不依赖执行顺序
- [ ] 测试可以独立运行
- [ ] 测试结果确定性（多次运行结果一致）

### 4. 明确测试意图（Explicit Intent）

**要求**：每个测试用例应当有明确的测试意图和期望行为。

**测试命名规范**：
```java
testShould<ExpectedBehavior>When<Condition>()
testShould<ExpectedBehavior>For<Scenario>()
testShould<ExpectedBehavior>Given<Context>()
```

**示例**：
```java
@Test
@DisplayName("Should eliminate dead code when variable never used")
public void testShouldEliminateDeadCodeWhenVariableNeverUsed() {
    // Given: 包含未使用定义的IR
    BasicBlock block = createBlockWithDeadCode();

    // When: 执行死代码消除
    BasicBlock optimized = new DeadCodeEliminationOptimizer().optimize(block);

    // Then: 死代码应被删除
    assertThat(optimized.getInstructions())
        .doesNotContain(deadDefinition);
}
```

### 5. 三层测试金字塔

EP21采用经典的测试金字塔结构：

```
         /\
        /E2E1\      ← 端到端测试（少量，慢）
       /------\
      /E1E1\    ← 集成测试（中等，中）
     /--------\
    /E1E1E1\  ← 单元测试（大量，快）
   /----------\
  /E1E1E1E1\  ← 持续集成（极少，慢）
```

**各层特征**：

| 层级 | 测试类型 | 数量 | 执行速度 | 失败原因 | 典型测试工具 |
|-------|---------|--------|-----------|--------------|--------------|
| **E1** | 单元测试（Unit Tests） | ~150+ | 快（<10s/全量） | 逻辑错误、边界条件 | JUnit 5, AssertJ |
| **E2** | 集成测试（Integration Tests） | ~50 | 中等（<30s/全量） | 接口不匹配、配置错误 | JUnit 5, Mock框架 |
| **E3** | 端到端测试（End-to-End Tests） | ~10 | 慢（<2min/个例） | 完整流程错误、性能问题 | JUnit 5, 端到端场景 |

---

## 📊 测试覆盖率目标

### 覆盖率指标

EP21目标覆盖率（继承并扩展EP20标准）：

| 指标 | 目标值 | 测量工具 | 优先级 |
|--------|--------|----------|--------|-----------|
| **行覆盖率**（Line Coverage） | ≥ 85% | JaCoCo | 高 |
| **分支覆盖率**（Branch Coverage） | ≥ 80% | JaCoCo | 高 |
| **方法覆盖率**（Method Coverage） | ≥ 90% | JaCoCo | 中 |
| **类覆盖率**（Class Coverage） | ≥ 85% | JaCoCo | 中 |

### 覆盖率报告生成

**Maven配置**（已在POM中添加）：
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </execution>
    </executions>
</plugin>
```

**命令**：
```bash
# 运行测试并生成覆盖率报告
cd ep21
mvn clean test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html
```

---

## 🏗️ 测试组织结构

### 包结构

```
ep21/src/test/java/org/teachfx/antlr4/ep21/
├── analysis/                  # 数据流和SSA分析测试
│   ├── dataflow/
│   │   ├── AbstractDataFlowAnalysisTest.java
│   │   ├── LiveVariableAnalysisTest.java
│   │   ├── ReachingDefinitionAnalysisTest.java
│   │   ├── ConditionConstantPropagationTest.java
│   │   └── LoopAnalysisTest.java
│   ├── ssa/
│   │   ├── DominatorAnalysisTest.java
│   │   ├── SSAGraphTest.java
│   │   └── SSAValidatorTest.java
├── pass/cfg/                # 优化Pass测试
│   ├── DeadCodeEliminationOptimizerTest.java
│   ├── ConstantFoldingOptimizerTest.java
│   ├── TailRecursionOptimizerTest.java
│   ├── CommonSubexpressionEliminationOptimizerTest.java
│   ├── CFGBuilderTest.java
│   └── OptimizationPassTest.java
└── integration/               # 端到端和跨模块集成测试
    ├── EP21CompilerTest.java
    └── RegisterAllocatorIntegrationTest.java
```

### 测试命名规范

**单元测试类名**：
```
<Functionality>Test.java
例如：
- DeadCodeEliminationOptimizerTest.java
- ConstantFoldingOptimizerTest.java
- LiveVariableAnalysisTest.java
```

**集成测试类名**：
```
<Integration>Test.java
例如：
- EP21CompilerTest.java
- RegisterAllocatorIntegrationTest.java
```

---

## 🧪 测试编写规范

### Given-When-Then模式

**模板**：
```java
@Test
@DisplayName("Should <expected behavior> when <condition>")
public void testShould<Expected>When<Condition>() {
    // Given: 准备测试环境和数据
    <PreparationCode>

    // When: 执行被测操作
    <ActionCode>

    // Then: 验证结果
    <AssertionCode>
}
```

**完整示例**：
```java
@Test
@DisplayName("Should eliminate dead code when variable never used")
public void testShouldEliminateDeadCodeWhenVariableNeverUsed() {
    // Given: 包含未使用定义的IR
    BasicBlock block = createBlockWithDeadCode();

    // When: 执行死代码消除
    BasicBlock optimized = new DeadCodeEliminationOptimizer().optimize(block);

    // Then: 死代码应被删除
    assertThat(optimized.getInstructions())
        .doesNotContain(deadDefinition);
}
```

### 断言库选择

**优先使用AssertJ**：
```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

**示例**：
```java
// ✅ 推荐：assertThat(actual).isEqualTo(expected)
// ✅ 推荐：assertThat(list).containsExactly(elem1, elem2)
// ✅ 推荐：assertThat(obj).isInstanceOf(SomeClass.class)
// ✅ 推荐：assertThatThrownBy(executable).isInstanceOf(expectedException.class)

// ⏸ 避免：JUnit原生断言（assertEquals等）
```

### 测试数据构建

使用Builder模式或工厂方法构建复杂测试数据：

```java
public class TestDataBuilder {
    public static BasicBlockBuilder block() {
        return new BasicBlockBuilder();
    }

    public static IRNodeBuilder ir() {
        return new IRNodeBuilder();
    }
}

// 使用
BasicBlock block = TestDataBuilder.block()
    .addDefinition("x")
    .addUsage("x")
    .addUsage("z")  // z未使用
    .build();
```

---

## 🛡️ Mock和Stub最佳实践

### 何时使用Mock

**适用场景**：
- ✅ 测试与外部系统交互的代码
- ✅ 测试依赖数据库或网络的组件
- ✅ 测试需要控制外部依赖行为的场景
- ✅ 集成测试中隔离特定模块

**不适用场景**：
- ⏸ 测试纯算法逻辑（应直接测试）
- ⏸ 测试简单数据结构操作
- ⏸ 测试可以快速构建的测试数据

### Mock示例

**Mock CFG**：
```java
class LiveVariableAnalysisTest {

    @Mock
    private CFG mockCFG;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldUseDominanceFrontierInLiveAnalysis() {
        // Given: 模拟CFG和支配分析结果
        when(mockCFG.getEntryBlock()).thenReturn(mockEntryBlock);
        when(mockDominatorAnalysis.getDominanceFrontier(any()))
            .thenReturn(createDominanceFrontier());

        // When: 执行活跃变量分析
        LiveVariableAnalysis analysis = new LiveVariableAnalysis();
        analysis.analyze(mockCFG);

        // Then: 应正确使用支配边界
        verify(mockDominatorAnalysis).getDominanceFrontier(any());
        assertThat(analysis.getLiveVariables()).isNotEmpty();
    }
}
```

---

## 🔍 持续集成（CI）

### Git钩子配置

**`.git/hooks/pre-commit`**：
```bash
#!/bin/bash

# Pre-commit hook: 自动运行快速测试
echo "Running pre-commit tests..."

# 运行快速单元测试（<30s）
mvn test -Dtest=DeadCodeEliminationOptimizerTest -q

# 检查测试状态
if [ $? -ne 0 ]; then
    echo "❌ Pre-commit tests failed. Commit aborted."
    exit 1
fi

echo "✅ Pre-commit tests passed."
```

**`.git/hooks/pre-push`**：
```bash
#!/bin/bash

# Pre-push hook: 运行完整测试套件
echo "Running full test suite before push..."

mvn clean test

if [ $? -ne 0 ]; then
    echo "❌ Tests failed. Push aborted."
    exit 1
fi

# 检查覆盖率
mvn jacoco:check
if [ $? -ne 0 ]; then
    echo "⚠️  Coverage below threshold. Push aborted."
    exit 1
fi

echo "✅ All checks passed. Ready to push."
```

---

## 📊 测试数据管理

### 测试资源文件

```
ep21/src/test/resources/
├── dataflow/                # 数据流分析测试用例
│   ├── live_variable/
│   ├── simple_block.cymbol
│   ├── complex_loop.cymbol
│   ├── conditional_branch.cymbol
│   ├── reaching_definitions/
│   └── ssa/
├── optimization/            # 优化Pass测试用例
│   ├── dead_code/
│   │   ├── unused_assignment.cymbol
│   │   ├── unreachable_code.cymbol
│   ├── constant_folding/
│   │   ├── arithmetic.cymbol
│   ├── conditional_branch.cymbol
│   ├── logical_ops.cymbol
│   ├── loop_optimization/
│   │   ├── unrolling/
│   │   ├── strength_reduction/
│   │   └── invariant_motion/
│   └── loop_analysis/
└── integration/            # 端到端测试
    ├── factorial.cymbol
    ├── fibonacci.cymbol
    └── complex_program.cymbol
```

### 测试数据命名

```
<feature>_<scenario>_<variation>_<expected>.cymbol

示例：
- dataflow_live_variable_simple_block.cymbol
- optimization_dead_code_unused_assignment.cymbol
- integration_factorial_program.cymbol
```

---

## 🚧 重构安全网

### 重构触发条件

**不触发重构**：
- ⏸ 测试为红时
- ⏸ 代码格式不正确时（应先修复）
- ⏸ 有明显技术债时（应先记录）

**触发重构前检查清单**：
- [ ] 是否有对应测试覆盖重构代码
- [ ] 是否运行了完整测试套件并全部通过
- [ ] 是否理解重构的完整影响范围

### 重构安全实践

**1. 小步重构**
```
错误做法：
❌ 一次性重构整个类，导致大量测试失败

正确做法：
✅ 识别重构区域
✅ 编写测试确保现有行为
✅ 小步重构
✅ 每步运行测试
```

**2. 重构同时改进测试**
```java
// 原有测试（只测试基本功能）
@Test
void testDeadCodeElimination() {
    BasicBlock block = createBlockWithDeadCode();
    BasicBlock optimized = new DeadCodeEliminationOptimizer().optimize(block);
    assertThat(optimized.hasNoDeadCode()).isTrue();
}

// 重构后：添加更多断言，同时发现新问题
@Test
void testDeadCodeElimination() {
    BasicBlock block = createBlockWithDeadCode();
    BasicBlock optimized = new DeadCodeEliminationOptimizer().optimize(block);

    // 新增：验证优化不改变语义
    assertThat(optimized).isSemanticallyEquivalentTo(block);

    // 新增：验证所有死代码都被消除
    assertThat(optimized).getAllDeadDefinitions()).isNotEmpty();
}
```

**3. 重构提取方法**

```java
class DeadCodeEliminationOptimizerTest {

    @Test
    void testComplexDeadCode() {
        // 原有：内联逻辑
        BasicBlock block = createComplexBlock();
        BasicBlock optimized = optimizer.optimize(block);
        assertDeadCodeEliminated(optimized);
    }

    // 重构：提取断言为可复用方法
    @Test
    void testComplexDeadCode() {
        // 原有：内联逻辑
        BasicBlock block = createComplexBlock();
        BasicBlock optimized = optimizer.optimize(block);
        assertDeadCodeEliminated(optimized);

        // 新增：辅助方法，提升可读性
        verifyOptimizationPreservesSemantics(block, optimized);
    }
}
```

---

## 📝 测试质量检查清单

### 提交前检查

```
### 功能完整性
- [ ] 所有新增功能都有测试覆盖
- [ ] 所有测试用例都有清晰的Given-When-Then结构
- [ ] 测试命名符合规范（testShould<Expected>When<Condition>）
- [ ] 测试数据文件命名清晰

### 代码质量
- [ ] 无JUnit原生断言（assertEquals等），全部使用AssertJ
- [ ] Mock使用正确（仅在必要隔离外部依赖时）
- [ ] 测试数据通过Builder模式构建
- [ ] 无硬编码测试数据（使用资源文件）

### 覆盖率
- [ ] 行覆盖率 ≥ 85%
- [ ] 分支覆盖率 ≥ 80%
- [ ] 方法覆盖率 ≥ 90%
```

### Code Review检查项

```
### 测试设计
- [ ] 测试意图清晰（@DisplayName描述准确）
- [ ] 测试方法职责单一
- [ ] 测试相互独立
- [ ] 断言具有描述性（使用AssertJ的链式断言）

### 测试实现
- [ ] 无重复代码（提取公共方法）
- [ ] 测试辅助方法命名清晰
- [ ] Mock和Stub使用合理
- [ ] 异常处理正确（抛出异常而非吞没）
```

---

## 🎯 测试驱动开发工作流

### 典型迭代流程

```
1️⃣ 编写测试（红） → 最小化实现
   ↓
2️⃣ 运行测试（绿） → 验证通过
   ↓
   🔧 重构优化（Refactor） → 改进代码质量
   ↓
3️⃣ 下一功能
```

### 时间盒分配

| 阶段 | 目标时间 | 检查点 |
|--------|----------|---------|----------|
| 编写测试 | 5-10分钟 | 测试是否编译 |
| 编写实现 | 10-20分钟 | 测试是否通过 |
| 重构优化 | 5-15分钟 | 测试是否通过 |
| 文档更新 | 5分钟 | 文档是否同步 |

**总周期**：25-50分钟 / 每个小功能

---

## 📚 参考资料

### EP21 TDD相关文档

- [EP19 TDD开发计划](../../ep19/docs/03_development_plans/EP19_TDD开发计划.md) - 基础TDD标准
- [EP20 TDD实施标准](../../ep20/docs/02_implementation_standards/EP20_TDD实施标准.md) - 扩展标准

### 外部资源

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Mockito Documentation](https://javadoc.io/doc/org/mockito/mockito/latest/org/mockito/Mockito.html)
- [Test Driven Development by Example](https://martinfowler.com/bliki/TestDrivenDevelopment)
- [Growing Object-Oriented Software, Guided by Tests](https://www.amazon.com/Growing-Object-Oriented-Software-Guided-Tests-Steve-Freeman/dp/0201634817)

---

**文档版本**: 1.0
**创建日期**: 2026-01-14
**适用范围**: EP21模块所有开发和测试活动
**维护者**: EP21模块维护团队
**审核要求**: 需要明确测试意图、确保代码质量、达到覆盖率目标、建立重构安全网
