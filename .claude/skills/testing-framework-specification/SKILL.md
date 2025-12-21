---
name: testing-framework-specification
description: 定义EP项目的标准化测试框架规范，基于JUnit 5 + AssertJ + Mockito + JaCoCo。包括测试配置、命名规范、结构标准和覆盖率要求，适用于所有EP模块的测试开发。
allowed-tools: Read, Grep, Glob, Bash, Write, Edit
---

# 测试框架规范 Skill

**文档版本**: v1.0
**创建日期**: 2025-12-21
**适用范围**: 所有 EP 模块测试开发

---

## 📋 概述

本 Skill 定义了 EP 项目的标准化测试框架，基于 JUnit 5 + AssertJ + Mockito + JaCoCo，确保测试代码的质量、一致性和可维护性。

---

## 🛠️ 测试框架配置

### Maven 依赖

```xml
<!-- 父 POM 测试依赖配置 -->
<properties>
    <junit.version>5.11.3</junit.version>
    <assertj.version>3.27.0</assertj.version>
    <mockito.version>5.8.0</mockito.version>
    <jacoco.version>0.8.12</jacoco.version>
</properties>

<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-params</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>${assertj.version}</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>${mockito.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <version>${mockito.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### JaCoCo 覆盖率配置

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <phase>test</phase>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.85</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## 📝 测试编写规范

### 测试命名规则

```java
// 格式: test{被测场景}_{期望结果}_when{条件}
// 或者: test{被测方法}_{场景}_should{期望行为}

✅ 好示例:
@Test
void testIAddInstructionReturnsSum_whenStackHasTwoIntegers() { ... }

@Test
void testTypeCheckerRejectsInvalidBinaryOperation_whenOperandsAreIncompatible() { ... }

@Test
void testBytecodeEmissionFails_whenInvalidInstruction() { ... }

❌ 不好示例:
@Test
void test1() { ... }  // 无意义名称

@Test
void testMethod() { ... }  // 太通用
```

### 测试结构 (Given-When-Then)

```java
@Test
@DisplayName("应正确执行 IADD 指令")
void testIAddInstruction() {
    // Given - 准备测试数据和上下文
    OperandStack stack = new OperandStack(10);
    stack.push(10);
    stack.push(20);
    ExecutionContext context = new ExecutionContext(stack);
    IAddInstruction instruction = new IAddInstruction();
    
    // When - 执行被测操作
    instruction.execute(context);
    
    // Then - 验证结果
    assertThat(stack.pop()).isEqualTo(30);
    assertThat(stack.size()).isEqualTo(0);
}
```

### 使用 AssertJ 断言

```java
// ✅ 推荐: AssertJ 的流畅断言
assertThat(result)
    .isNotNull()
    .isEqualTo(expected)
    .matches(r -> r.getStatus() == Status.SUCCESS);

assertThat(list)
    .isNotEmpty()
    .hasSize(3)
    .containsExactly("a", "b", "c");

assertThat(exception)
    .isInstanceOf(VMException.class)
    .hasMessageContaining("invalid instruction");

// ❌ 避免: JUnit 的旧式断言
assertEquals(expected, result);  // 顺序易错
assertTrue(list.isEmpty());       // 错误信息不明确
```

---

## 🎯 测试类型定义

### 1. 单元测试 (Unit Tests)

**目标**: 验证单个类或方法的正确性

```java
@Tag("unit")
class InstructionTest {
    @Test
    @DisplayName("加法指令应正确计算两个整数之和")
    void testAddInstruction() {
        // Given
        AddInstruction add = new AddInstruction();
        ExecutionContext ctx = createContext();
        ctx.push(5);
        ctx.push(3);
        
        // When
        add.execute(ctx);
        
        // Then
        assertThat(ctx.pop()).isEqualTo(8);
    }
}
```

**命名约定**: `{ClassName}Test.java`
**位置**: `src/test/java/{package}/`
**覆盖率要求**: ≥90%

### 2. 集成测试 (Integration Tests)

**目标**: 验证模块间协作的正确性

```java
@Tag("integration")
class CompilerPipelineTest {
    @Test
    @DisplayName("完整编译流程应能处理包含循环和函数调用的程序")
    void testFullCompilationPipeline() {
        // Given
        String source = loadTestProgram("fibonacci.cymbol");
        Compiler compiler = createCompiler();
        
        // When
        CompilationResult result = compiler.compile(source);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getBytecode()).isNotNull();
        assertThat(result.getDiagnostics()).isEmpty();
    }
}
```

**命名约定**: `{Feature}IntegrationTest.java`
**位置**: `src/test/java/{package}/integration/`
**覆盖率要求**: 关键路径 100%

### 3. 端到端测试 (End-to-End Tests)

**目标**: 验证完整系统功能

```java
@Tag("e2e")
class EndToEndTest {
    @Test
    @DisplayName("应能编译并执行斐波那契程序")
    void testFibonacciProgramExecution() {
        // Given
        Path program = Paths.get("test-programs/fibonacci.csymbol");
        
        // When
        ExecutionResult result = executeProgram(program);
        
        // Then
        assertThat(result.getExitCode()).isEqualTo(0);
        assertThat(result.getOutput()).contains("55");
    }
}
```

**命名约定**: `{Feature}E2ETest.java`
**位置**: `src/test/java/{package}/e2e/`

---

## 🔧 测试工具和方法

### 参数化测试

```java
@ParameterizedTest
@ValueSource(ints = {0, 1, 10, 100, Integer.MAX_VALUE})
@DisplayName("数据栈应能处理各种大小的值")
void testStackPushPop(int value) {
    // Given
    OperandStack stack = new OperandStack(1000);
    
    // When
    stack.push(value);
    int result = stack.pop();
    
    // Then
    assertThat(result).isEqualTo(value);
}

@ParameterizedTest
@CsvSource({
    "5, 3, ADD, 8",
    "10, 7, SUB, 3",
    "4, 6, MUL, 24",
    "12, 4, DIV, 3"
})
@DisplayName("二元运算指令应正确计算结果")
void testBinaryInstruction(int a, int b, OpCode op, int expected) {
    // ...
}
```

### Mock 和 Stub

```java
class TypeCheckerTest {
    @Mock
    private SymbolTable symbolTable;
    
    @Mock
    private ErrorReporter errorReporter;
    
    @InjectMocks
    private TypeChecker typeChecker;
    
    @Test
    void testUndefinedTypeReporting() {
        // Given
        when(symbolTable.resolve("UndefinedType"))
            .thenReturn(null);
        
        // When
        typeChecker.check(node);
        
        // Then
        verify(errorReporter).reportError(
            eq(ErrorType.UNDEFINED_TYPE),
            eq("Unknown type: UndefinedType")
        );
    }
}
```

### 使用 Test Fixtures

```java
public class VMTestFixtures {
    public static final String SIMPLE_ADD_PROGRAM = """
        iconst 5
        iconst 3
        iadd
        halt
    """;
    
    public static final String FACTORIAL_PROGRAM = """
        .def factorial: args=1, locals=1
            load 0
            iconst 1
            if_icmple base_case
            ; recursive case
            load 0
            iconst 1
            isub
            call factorial
            load 0
            imul
            ret
        base_case:
            iconst 1
            ret
    """;
}
```

---

## 📊 测试覆盖要求

### 模块覆盖矩阵

| 模块类型 | 行覆盖率 | 分支覆盖率 | 方法覆盖率 | 注解 |
|----------|----------|------------|------------|------|
| 核心引擎 | ≥90% | ≥85% | 100% | 类型检查、IR生成 |
| 指令实现 | ≥95% | ≥90% | 100% | 每条指令都需测试 |
| 栈帧管理 | ≥90% | ≥85% | 100% | 溢出/下溢边界 |
| 内存管理 | ≥85% | ≥80% | 100% | GC、分配策略 |
| 优化算法 | ≥90% | ≥85% | 100% | 精确验证效果 |
| 异常处理 | ≥95% | ≥90% | 100% | 所有错误路径 |
| **工具类** | ≥80% | ≥75% | 100% | 辅助功能 |
| **配置类** | ≥70% | ≥65% | 100% | 简单逻辑 |

### 测试编写检查清单

#### 创建测试类时检查:
- [ ] 测试类与被测类同名 + "Test" 后缀
- [ ] 添加了 `@Tag` 注解 (unit/integration/e2e)
- [ ] 使用 `@DisplayName` 提供中文描述
- [ ] 测试方法名符合规范 (test{场景}_{期望}_when{条件})

#### 编写测试方法时检查:
- [ ] 遵循 Given-When-Then 结构
- [ ] 使用 AssertJ 断言 (不使用 JUnit 旧式断言)
- [ ] 每个测试只测试一个关注点
- [ ] 边界条件有单独测试
- [ ] 异常测试使用 `assertThrows`

#### 提交前检查:
- [ ] 所有测试通过 (`mvn test`)
- [ ] 覆盖率达标 (`mvn jacoco:check`)
- [ ] 没有编译警告
- [ ] 代码格式化 (4空格缩进)

---

## 🔍 常见测试场景模式

### 测试异常场景

```java
@Test
@DisplayName("除零操作应抛出 DivisionByZeroException")
void testDivisionByZeroThrowsException() {
    // Given
    ExecutionContext ctx = createContext();
    ctx.push(10);
    ctx.push(0);  // 除数为零
    
    // When & Then
    DivisionByZeroException exception = assertThrows(
        DivisionByZeroException.class,
        () -> new IDivInstruction().execute(ctx, 0)
    );
    
    assertThat(exception.getPC()).isEqualTo(ctx.getPC());
    assertThat(exception.getMessage()).contains("Division by zero");
}
```

### 测试性能边界

```java
@Test
@DisplayName("算法应能在 100ms 内处理 10,000 行代码")
void testPerformanceWithLargeProgram() {
    // Given
    String largeProgram = generateLargeProgram(10000);
    IRGenerator generator = new IRGenerator();
    
    // When
    long start = System.currentTimeMillis();
    IR ir = generator.generate(largeProgram);
    long duration = System.currentTimeMillis() - start;
    
    // Then
    assertThat(duration).isLessThan(100);
    assertThat(ir.getInstructionCount()).isGreaterThan(0);
}
```

### 测试并发安全

```java
@Test
@DisplayName("符号表应线程安全")
void testSymbolTableThreadSafety() throws InterruptedException {
    // Given
    SymbolTable table = new SymbolTable();
    int threadCount = 10;
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    // When
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
        final int id = i;
        Thread t = new Thread(() -> {
            table.defineSymbol("var" + id, Type.INT);
            latch.countDown();
        });
        threads.add(t);
        t.start();
    }
    
    latch.await();
    
    // Then
    for (int i = 0; i < threadCount; i++) {
        assertThat(table.lookup("var" + id)).isNotNull();
    }
}
```

---

## 📦 测试数据管理

### 测试资源目录结构

```
src/test/resources/
├── valid-programs/          # 有效的测试程序
│   ├── simple/              # 简单程序
│   ├── complex/             # 复杂程序
│   └── edge-cases/          # 边界情况
├── invalid-programs/        # 无效的测试程序
│   ├── syntax-errors/       # 语法错误
│   ├── semantic-errors/     # 语义错误
│   └── type-errors/         # 类型错误
└── performance/             # 性能测试程序
    ├── small/
    ├── medium/
    └── large/
```

### 测试资源加载

```java
public class TestResourceLoader {
    /**
     * 加载测试程序
     */
    public static String loadProgram(String name) {
        try {
            Path path = Paths.get("src/test/resources/valid-programs/" + name);
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load program: " + name, e);
        }
    }
}
```

---

## 🎓 测试最佳实践

### 1. 测试优先 (TDD)

```
红 (Red) → 绿 (Green) → 重构 (Refactor)
   ↓          ↓           ↓
写失败测试 → 最小实现 → 优化代码
```

### 2. 测试金字塔

```
        端到端测试 (10%)
            ↑
            |
      集成测试 (30%)
            ↑
            |
      单元测试 (60%)  ← 基础要稳固
```

### 3. FIRST 原则

- **F**ast: 测试要快 (毫秒级)
- **I**solated: 测试相互独立
- **R**epeatable: 可重复执行
- **S**elf-validating: 自动化验证
- **T**imely: 及时编写 (TDD)

### 4. 避免的测试坏味道

❌ **测试不独立**
```java
private static sharedVariable;  // 不要在测试间共享状态
```

❌ **测试有逻辑**
```java
@Test
void testWithLogic() {
    if (condition) {  // 测试分支 = 坏味道
        // ...
    }
}
```

❌ **依赖执行顺序**
```java
@Test
void testA() { /* 设置状态 */ }

@Test
depend void testB() { /* 依赖 testA */ }  // 不要这样做
```

✅ **正确做法**: 每个测试自给自足，明确准备数据

---

## 🔧 CI/CD 集成

### GitHub Actions 配置

```yaml
name: Test and Coverage

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Run tests
        run: mvn clean test
      
      - name: Check coverage
        run: mvn jacoco:check
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml
```

---

**版本**: v1.0  
**适用范围**: 所有 EP 项目的测试开发  
**维护**: 由测试框架负责人统一更新  
**最后更新**: 2025-12-21 
