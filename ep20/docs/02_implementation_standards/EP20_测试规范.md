
# EP20寄存器分配测试策略文档

## 1. 测试概述

本文档定义EP20编译器寄存器分配模块的完整测试策略，包括单元测试、集成测试、性能测试和回归测试方案。

## 2. 测试目标

### 2.1 功能正确性目标
- 验证图着色算法正确性
- 确保溢出处理机制正常工作
- 验证代码生成器适配正确
- 保证向后兼容性

### 2.2 性能目标
- 编译时间增加不超过20%
- 内存访问指令减少30%以上
- 内存使用控制在合理范围内

### 2.3 质量目标
- 核心算法100%测试覆盖率
- 关键路径集成测试覆盖
- 边界条件和错误场景全面测试

## 3. 测试环境

### 3.1 硬件环境
```yaml
测试环境配置:
  - 开发环境: 8核CPU, 16GB内存
  - 测试环境: 4核CPU, 8GB内存  
  - 生产环境: 16核CPU, 32GB内存

性能测试专用:
  - 隔离的性能测试服务器
  - 监控和 profiling 工具
  - 基准测试数据集
```

### 3.2 软件环境
```yaml
依赖组件:
  - JDK 17+
  - JUnit 5
  - AssertJ
  - JaCoCo (代码覆盖率)
  - JMH (微基准测试)
  - VisualVM (性能分析)

构建工具:
  - Maven 3.6+
  - 持续集成: GitHub Actions
  - 代码质量: SonarQube
```

## 4. 测试层次结构

### 4.1 单元测试策略

#### 4.1.1 冲突图测试
```java
public class ConflictGraphTest {
    
    @Test
    void testAddAndRemoveNodes() {
        ConflictGraph graph = new ConflictGraph();
        Operand op1 = new FrameSlot(1);
        Operand op2 = new FrameSlot(2);
        
        graph.addConflict(op1, op2);
        assertEquals(1, graph.getDegree(op1));
        assertEquals(1, graph.getDegree(op2));
        
        graph.removeNode(op1);
        assertEquals(0, graph.getDegree(op2));
    }
    
    @Test
    void testGraphVisualization() {
        ConflictGraph graph = createComplexGraph();
        String graphviz = graph.toGraphviz();
        assertThat(graphviz).contains("digraph");
        assertThat(graphviz).contains("->");
    }
}
```

#### 4.1.2 图着色算法测试
```java
public class GraphColoringTest {
    
    @Test
    void testSimpleColoring() {
        ConflictGraph graph = createSimpleGraph();
        ColoringStrategy strategy = new GreedyColoringStrategy();
        
        Map<Operand, Integer> coloring = strategy.colorGraph(graph, 3);
        assertValidColoring(graph, coloring, 3);
    }
    
    @Test
    void testColoringFailure() {
        ConflictGraph graph = createCompleteGraph(10);
        ColoringStrategy strategy = new GreedyColoringStrategy();
        
        assertThrows(GraphColoringException.class, () -> {
            strategy.colorGraph(graph, 3);
        });
    }
}
```

### 4.2 集成测试策略

#### 4.2.1 端到端编译测试
```java
public class EndToEndAllocationTest {
    
    @Test
    void testCompleteAllocationFlow() {
        // 从源代码到目标代码的完整流程
        String sourceCode = "int main() { int a = 1; int b = 2; return a + b; }";
        
        Compiler compiler = new Compiler();
        CompilationResult result = compiler.compile(sourceCode);
        
        assertThat(result.getOutput()).contains("rload");
        assertThat(result.getMetrics().getSpillCount()).isZero();
    }
    
    @Test
    void testAllocationWithSpilling() {
        // 测试溢出处理
        String sourceCode = createLargeVariableCode(20); // 超过寄存器数量
        
        Compiler compiler = new Compiler();
        CompilationResult result = compiler.compile(sourceCode);
        
        assertThat(result.getMetrics().getSpillCount()).isPositive();
        assertThat(result.getOutput()).contains("load"); // 包含内存访问
    }
}
```

#### 4.2.2 回归测试
```java
public class RegressionTest {
    
    @ParameterizedTest
    @ValueSource(strings = {
        "test1.cym", "test2.cym", "test3.cym" // EP20现有测试用例
    })
    void testBackwardCompatibility(String testFile) {
        // 确保现有功能不受影响
        Compiler compiler = new Compiler();
        CompilationResult oldResult = compiler.compileWithoutAllocation(testFile);
        CompilationResult newResult = compiler.compileWithAllocation(testFile);
        
        // 功能应该一致
        assertThat(newResult.getOutput()).isEqualTo(oldResult.getOutput());
    }
}
```

## 5. 性能测试策略

### 5.1 微基准测试
```java
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class AllocationBenchmark {
    
    private ConflictGraph graph;
    private List<IRNode> irNodes;
    
    @Setup
    public void setup() {
        graph = createRealisticGraph();
        irNodes = generateTestIR();
    }
    
    @Benchmark
    public void benchmarkGraphBuilding() {
        buildConflictGraph(irNodes);
    }
    
    @Benchmark
    public void benchmarkColoring() {
        colorGraph(graph, 8);
    }
    
    @Benchmark
    public void benchmarkFullAllocation() {
        RegisterAllocator allocator = new DefaultRegisterAllocator();
        allocator.allocate(irNodes, 8);
    }
}
```

### 5.2 宏观性能测试
```java
public class MacroPerformanceTest {
    
    @Test
    void testCompilationTimeImpact() {
        // 测试编译时间影响
        Compiler compiler = new Compiler();
        List<CompilationResult> results = new ArrayList<>();
        
        for (String testFile : getPerformanceTestFiles()) {
            long startTime = System.currentTimeMillis();
            CompilationResult result = compiler.compile(testFile);
            long endTime = System.currentTimeMillis();
            
            result.setCompilationTime(endTime - startTime);
            results.add(result);
        }
        
        // 编译时间增加不应超过20%
        double timeIncrease = calculateTimeIncrease(results);
        assertThat(timeIncrease).isLessThanOrEqualTo(0.2);
    }
    
    @Test
    void testMemoryAccessReduction() {
        // 测试内存访问减少效果
        Compiler compiler = new Compiler();
        int totalReduction = 0;
        
        for (String testFile : getTestFiles()) {
            CompilationResult withoutAlloc = compiler.compileWithoutAllocation(testFile);
            CompilationResult withAlloc = compiler.compileWithAllocation(testFile);
            
            int reduction = calculateMemoryAccessReduction(
                withoutAlloc.getInstructionCount(),
                withAlloc.getInstructionCount());
            totalReduction += reduction;
        }
        
        // 平均减少应超过30%
        double avgReduction = totalReduction / (double) getTestFiles().size();
        assertThat(avgReduction).isGreaterThanOrEqualTo(0.3);
    }
}
```

## 6. 边界和错误测试

### 6.1 边界条件测试
```java
public class BoundaryTest {
    
    @Test
    void testSingleVariable() {
        // 单变量测试
        String sourceCode = "int main() { return 42; }";
        CompilationResult result = compileWithAllocation(sourceCode);
        assertThat(result.getMetrics().getSpillCount()).isZero();
    }
    
    @Test
    void testMaximumVariables() {
        // 最大变量数测试
        String sourceCode = createMaxVariablesCode(100);
        CompilationResult result = compileWithAllocation(sourceCode);
        assertThat(result.getMetrics().getSpillCount()).isPositive();
    }
    
    @Test
    void testZeroRegisters() {
        // 零寄存器测试（应回退到栈模式）
        Compiler compiler = new Compiler();
        compiler.setRegisterCount(0);
        
        CompilationResult result = compiler.compile("int main() { return 0; }");
        assertThat(result.getOutput()).doesNotContain("rload");
    }
}
```

### 6.2 错误场景测试
```java
public class ErrorHandlingTest {
    
    @Test
    void testInvalidIRInput() {
        RegisterAllocator allocator = new DefaultRegisterAllocator();
        assertThrows(InvalidIREexception.class, () -> {
            allocator.allocate(null, 8);
        });
    }
    
    @Test
    void testNegativeRegisterCount() {
        RegisterAllocator allocator = new DefaultRegisterAllocator();
        assertThrows(IllegalArgumentException.class, () -> {
            allocator.allocate(createTestIR(), -1);
        });
    }
    
    @Test
    void testAllocationFailureRecovery() {
        // 测试分配失败时的回退机制
        Compiler compiler = new Compiler();
        compiler.setRegisterCount(1); // 强制分配失败
        
        CompilationResult result = compiler.compile(createComplexCode());
        assertThat(result.isSuccess()).isTrue(); // 应该成功回退
        assertThat(result.getOutput()).contains("load"); // 使用栈模式
    }
}
```

## 7. 测试数据管理

### 7.1 测试用例分类
```yaml
测试类别:
  - 单元测试: 核心算法组件
  - 集成测试: 模块间交互
  - 性能测试: 编译和执行性能
  - 回归测试: 现有功能验证
  - 边界测试: 极端情况验证
  - 错误测试: 异常处理验证

测试优先级:
  - P0: 核心功能正确性
  - P1: 性能和质量指标
  - P2: 边界和错误场景
  - P3: 优化和增强功能
```

### 7.2 测试数据生成
```java
public class TestDataGenerator {
    
    public static List<IRNode> generateTestIR(int variableCount, 
                                             int instructionCount) {
        List<IRNode> irNodes = new ArrayList<>();
        // 生成测试IR指令
        return irNodes;
    }
    
    public static ConflictGraph generateTestGraph(int nodeCount, 
                                                 double edgeDensity) {
        ConflictGraph graph = new ConflictGraph();
        // 生成测试冲突图
        return graph;
    }
    
    public static String generateSourceCode(int variableCount,
                                          int statementCount) {
        // 生成测试源代码
        return "int main() { ... }";
    }
}
```

## 8. 测试执行策略

### 8.1 本地开发测试
```bash
# 运行所有单元测试
mvn test -Dtest=*RegisterAllocation*Test

# 运行性能测试
mvn test -Dtest=*Benchmark

# 生成覆盖率报告
mvn jacoco:report
```

### 8.2 持续集成测试
```yaml
# GitHub Actions 配置
name: Register Allocation Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Run Unit Tests
      run: mvn test -Dtest=*RegisterAllocation*Test
    - name: Run Integration Tests
      run: mvn test -Dtest=*IntegrationTest
    - name: Generate Coverage Report
      run: mvn jacoco:report
```

### 8.3 性能回归测试
```bash
# 定期性能回归测试
#!/bin/bash
# 运行性能基准测试
mvn test -Dtest=PerformanceTest

# 比较当前结果与基线
python compare_performance.py current_results.json baseline_results.json

# 如果性能下降超过阈值，失败构建
if [ $? -ne 0 ]; then
    echo "Performance regression detected!"
    exit 1
fi
```

## 9. 质量指标监控

### 9.1 测试覆盖率要求
```yaml
覆盖率目标:
  - 总体覆盖率: >= 90%
  - 核心算法: >= 95%
  - 关键路径: 100%
  - 错误处理: >= 85%

覆盖率监控:
  - 每次构建生成覆盖率报告
  - 覆盖率下降时失败构建
  - 定期审查未覆盖代码
```

### 9.2 性能指标监控
```yaml
性能指标:
  - 编译时间: < 20% 增加
  - 内存使用: < 50% 增加
  - 内存访问减少: >= 30%
  - 溢出变量比例: < 10%

监控频率:
  - 每次提交: 核心指标
  - 每日: 完整性能测试
  - 每周: 性能趋势分析
```

## 10. 测试报告和文档

### 10.1 测试报告格式
```java
/**
 * 测试结果报告
 */
public class TestReport {
    private String testName;
    private TestStatus status;
    private long executionTime;
    private String errorMessage;
    private Map<String, Object> metrics;
    private String stackTrace;
    
    public enum TestStatus { PASSED, FAILED, SKIPPED, ERROR }
}
```

### 10.2 自动化报告生成
```bash
# 生成HTML测试报告
mvn surefire-report:report

# 生成性能报告
java -jar benchmarks.jar -rf json -rff performance.json

# 生成质量报告
sonar-scanner -Dsonar.projectKey=ep20-register-allocation
```

## 11. 总结

本测试策略文档为EP20寄存器分配模块提供了全面的测试方案，涵盖从单元测试到性能测试的各个层面。通过系统化的测试设计和严格的质