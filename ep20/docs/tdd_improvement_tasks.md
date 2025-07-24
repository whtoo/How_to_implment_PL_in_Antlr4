# EP20 TDD驱动改进任务总览

## 📋 项目概述

基于EP20编译器架构演进成果，制定完整的TDD（测试驱动开发）驱动改进计划，确保代码质量和架构稳定性。

## 🎯 核心目标

- **测试覆盖率**: 从13个测试扩展到35+个测试
- **代码质量**: 达到90%以上语句覆盖率
- **架构稳定**: 建立回归测试体系
- **持续集成**: 自动化测试流程

## 📅 实施时间线

### 第1周：基础框架搭建
- **Day 1-2**: AST层测试增强
- **Day 3-5**: IR层测试构建
- **Day 6-7**: CFG层测试扩展

### 第2周：高级功能测试
- **Day 8-9**: 代码生成测试
- **Day 10**: 集成测试与优化

## 📊 任务分解

### 🔴 高优先级任务（必须完成）

#### AST层测试（Phase 1）
- [ ] **AST-001**: 字面量表达式节点测试
  - 测试文件: [`LiteralExprNodeTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/ast/expr/LiteralExprNodeTest.java)
  - 测试内容: 整型、字符串、布尔值字面量
  - 预期测试: 5个测试用例

- [ ] **AST-002**: 二元表达式节点测试
  - 测试文件: [`BinaryExprNodeTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/ast/expr/BinaryExprNodeTest.java)
  - 测试内容: 算术、比较、逻辑运算
  - 预期测试: 8个测试用例

- [ ] **AST-003**: 语句节点测试
  - 测试文件: [`IfStmtNodeTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/ast/stmt/IfStmtNodeTest.java)
  - 测试内容: if、while、return语句
  - 预期测试: 6个测试用例

#### IR层测试（Phase 2）
- [ ] **IR-001**: IR构建器基础测试
  - 测试文件: [`IRBuilderTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/ir/IRBuilderTest.java)
  - 测试内容: AST到IR转换
  - 预期测试: 10个测试用例

- [ ] **IR-002**: 地址化测试
  - 测试文件: [`AddressingTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/ir/AddressingTest.java)
  - 测试内容: 栈帧位置、全局变量
  - 预期测试: 6个测试用例

- [ ] **IR-003**: 三地址码生成测试
  - 测试文件: [`ThreeAddressCodeTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/ir/ThreeAddressCodeTest.java)
  - 测试内容: 表达式线性化
  - 预期测试: 8个测试用例

### 🟡 中优先级任务（优先完成）

#### CFG层测试（Phase 3）
- [ ] **CFG-001**: 控制流图构建测试
  - 测试文件: [`ControlFlowGraphTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/cfg/ControlFlowGraphTest.java)
  - 测试内容: if、while、函数调用
  - 预期测试: 12个测试用例

- [ ] **CFG-002**: 基本块优化测试
  - 测试文件: [`BasicBlockOptimizationTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/cfg/BasicBlockOptimizationTest.java)
  - 测试内容: 空块消除、跳转优化
  - 预期测试: 8个测试用例

- [ ] **CFG-003**: 数据流分析测试
  - 测试文件: [`DataFlowAnalysisTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/cfg/DataFlowAnalysisTest.java)
  - 测试内容: 活性分析、到达定义
  - 预期测试: 6个测试用例

#### 代码生成测试（Phase 4）
- [ ] **CG-001**: 虚拟机指令测试
  - 测试文件: [`CymbolAssemblerTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/codegen/CymbolAssemblerTest.java)
  - 测试内容: 指令生成正确性
  - 预期测试: 10个测试用例

- [ ] **CG-002**: 寄存器分配测试
  - 测试文件: [`RegisterAllocationTest.java`](ep20/src/test/java/org/teachfx/antlr4/ep20/codegen/RegisterAllocationTest.java)
  - 测试内容: 寄存器分配算法
  - 预期测试: 5个测试用例

### 🟢 低优先级任务（后续完善）

#### 符号表测试（Phase 5）
- [ ] **SYM-001**: 作用域测试
- [ ] **SYM-002**: 类型检查测试
- [ ] **SYM-003**: 符号解析测试

## 🏗️ 测试结构

### 目录结构
```
ep20/src/test/java/
├── org/teachfx/antlr4/ep20/
│   ├── ast/
│   │   ├── expr/
│   │   │   ├── LiteralExprNodeTest.java
│   │   │   ├── BinaryExprNodeTest.java
│   │   │   └── IdentifierNodeTest.java
│   │   └── stmt/
│   │       ├── IfStmtNodeTest.java
│   │       ├── WhileStmtNodeTest.java
│   │       └── ReturnStmtNodeTest.java
│   ├── ir/
│   │   ├── IRBuilderTest.java
│   │   ├── AddressingTest.java
│   │   └── ThreeAddressCodeTest.java
│   ├── cfg/
│   │   ├── ControlFlowGraphTest.java
│   │   ├── BasicBlockOptimizationTest.java
│   │   └── DataFlowAnalysisTest.java
│   └── codegen/
│       ├── CymbolAssemblerTest.java
│       └── RegisterAllocationTest.java
```

### 测试模板
每个测试类遵循以下模板：
```java
@DisplayName("组件测试")
class ComponentTest {
    
    @BeforeEach
    void setUp() {
        // 初始化测试环境
    }
    
    @Test
    @DisplayName("应正确执行基本功能")
    void testBasicFunctionality() {
        // Arrange
        // Act
        // Assert
    }
    
    @ParameterizedTest
    @MethodSource("testDataProvider")
    void testWithMultipleInputs(String input, String expected) {
        // 参数化测试
    }
    
    static Stream<Arguments> testDataProvider() {
        return Stream.of(
            Arguments.of("input1", "expected1"),
            Arguments.of("input2", "expected2")
        );
    }
}
```

## 🔄 开发流程

### 1. 红-绿-重构循环
1. **编写测试** (红)
2. **运行测试** (失败)
3. **实现功能** (绿)
4. **重构代码** (重构)
5. **运行测试** (通过)

### 2. 每日开发流程
```bash
# 1. 获取最新代码
git pull origin main

# 2. 运行现有测试
mvn test

# 3. 实现新测试
# 编辑测试文件

# 4. 运行新测试（应失败）
mvn test -Dtest=NewTest

# 5. 实现功能代码
# 编辑源文件

# 6. 运行所有测试
mvn test

# 7. 提交代码
git add .
git commit -m "feat: add test for XXX"
git push origin feature/xxx
```

## 📈 进度跟踪

### 测试覆盖率仪表板
| 模块 | 当前测试 | 目标测试 | 覆盖率 | 状态 |
|------|----------|----------|--------|------|
| AST层 | 1 | 8 | 90% | 🔄 |
| IR层 | 0 | 12 | 95% | ⏳ |
| CFG层 | 7 | 15 | 90% | ⏳ |
| 代码生成 | 2 | 10 | 85% | ⏳ |
| 总计 | 13 | 35+ | 90% | ⏳ |

### 每日进度更新
- [ ] **Day 1**: AST字面量测试
- [ ] **Day 2**: AST表达式测试
- [ ] **Day 3**: IR构建器基础
- [ ] **Day 4**: 地址化测试
- [ ] **Day 5**: 三地址码测试
- [ ] **Day 6**: CFG构建测试
- [ ] **Day 7**: 优化测试
- [ ] **Day 8**: 数据流分析
- [ ] **Day 9**: 代码生成测试
- [ ] **Day 10**: 集成测试与总结

## 🧪 测试执行命令

### 快速测试
```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -Dtest=*ast*

# 运行特定测试类
mvn test -Dtest=LiteralExprNodeTest

# 运行特定测试方法
mvn test -Dtest=LiteralExprNodeTest#testIntegerLiteralValue
```

### 覆盖率检查
```bash
# 生成覆盖率报告
mvn jacoco:report

# 查看HTML报告
open target/site/jacoco/index.html

# 设置覆盖率阈值
mvn jacoco:check -Djacoco.haltOnFailure=true
```

### 性能测试
```bash
# 运行性能基准测试
mvn test -Dtest=*Benchmark*

# 生成性能报告
mvn surefire-report:report
```

## 🚨 质量门控

### 代码质量检查
- [ ] **测试通过率**: 100%
- [ ] **语句覆盖率**: ≥90%
- [ ] **分支覆盖率**: ≥85%
- [ ] **复杂度**: ≤10
- [ ] **代码风格**: 符合规范

### 审查清单
- [ ] 测试命名清晰
- [ ] 测试数据充分
- [ ] 断言完整
- [ ] 异常处理
- [ ] 边界条件
- [ ] 性能考虑

## 📞 支持资源

### 文档链接
- [📖 TDD改进计划](docs/tdd_improvement_tasks.md)
- [🧪 测试用例指南](docs/tdd_test_case_guide.md)
- [⚙️ 实施步骤](docs/tdd_implementation_steps.md)
- [📊 EP20架构文档](ep20/docs/ep20-improvements-summary.md)

### 开发工具
- **IDE**: IntelliJ IDEA / Eclipse
- **构建工具**: Maven
- **测试框架**: JUnit 5 + AssertJ
- **覆盖率**: JaCoCo
- **CI/CD**: GitHub Actions

### 联系方式
- **技术负责人**: EP20架构团队
- **代码仓库**: [EP20 Compiler](https://github.com/ep20/compiler)
- **问题跟踪**: GitHub Issues

---

## 🚀 开始实施

1. **环境准备**: 按照[实施步骤](docs/tdd_implementation_steps.md)配置环境
2. **选择任务**: 从Phase 1开始，按优先级顺序执行
3. **编写测试**: 使用提供的测试模板
4. **提交代码**: 遵循Git工作流程
5. **更新进度**: 每日更新进度表

**记住**: 每次只处理一个任务，确保测试通过后再进行下一个任务！