# EP20 TDD实施步骤指南

## 概述

本文档提供基于TDD的EP20编译器改进的具体实施步骤，按照优先级和依赖关系组织，确保测试驱动开发的顺利进行。

## 🚀 实施路线图

### 阶段0：环境准备（0.5天）

#### 0.1 环境检查
```bash
# 检查Java版本
java -version

# 检查Maven
mvn -version

# 检查测试框架
mvn test -Dtest=BasicBlockTest
```

#### 0.2 依赖配置
```xml
<!-- 确保pom.xml包含 -->
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.9.3</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.24.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 阶段1：AST层测试实现（2天）

#### 1.1 表达式节点测试（第1天上午）
```java
// 创建: src/test/java/org/teachfx/antlr4/ep20/ast/expr/LiteralExprNodeTest.java
@Test
void testIntegerLiteralValue() {
    LiteralExprNode literal = new LiteralExprNode(42);
    assertThat(literal.getValue()).isEqualTo(42);
    assertThat(literal.getType()).isEqualTo(Type.INT);
}

@Test
void testStringLiteralValue() {
    LiteralExprNode literal = new LiteralExprNode("hello");
    assertThat(literal.getValue()).isEqualTo("hello");
    assertThat(literal.getType()).isEqualTo(Type.STRING);
}
```

#### 1.2 二元表达式测试（第1天下午）
```java
// 创建: src/test/java/org/teachfx/antlr4/ep20/ast/expr/BinaryExprNodeTest.java
@ParameterizedTest
@CsvSource({
    "1, +, 2, 3",
    "5, -, 3, 2",
    "4, *, 5, 20",
    "10, /, 2, 5"
})
void testBinaryExpressionEvaluation(int left, String op, int right, int expected) {
    BinaryExprNode expr = new BinaryExprNode(
        new LiteralExprNode(left),
        new LiteralExprNode(right),
        op
    );
    
    // 验证AST结构
    assertThat(expr.getLeft()).isInstanceOf(LiteralExprNode.class);
    assertThat(expr.getOperator()).isEqualTo(op);
    assertThat(expr.getRight()).isInstanceOf(LiteralExprNode.class);
}
```

#### 1.3 语句节点测试（第2天）
```java
// 创建: src/test/java/org/teachfx/antlr4/ep20/ast/stmt/IfStmtNodeTest.java
@Test
void testIfStatementStructure() {
    // 构建: if (x > 0) { return 1; }
    ExprNode condition = new BinaryExprNode(
        new IdentifierNode("x"),
        new LiteralExprNode(0),
        ">"
    );
    StmtNode thenStmt = new ReturnStmtNode(new LiteralExprNode(1));
    
    IfStmtNode ifStmt = new IfStmtNode(condition, thenStmt, null);
    
    assertThat(ifStmt.getCondition()).isEqualTo(condition);
    assertThat(ifStmt.getThenBranch()).isEqualTo(thenStmt);
    assertThat(ifStmt.getElseBranch()).isNull();
}
```

### 阶段2：IR层测试实现（3天）

#### 2.1 IR构建器测试（第3天）
```java
// 创建: src/test/java/org/teachfx/antlr4/ep20/ir/IRBuilderTest.java
@Test
void testSimpleAssignment() {
    // 输入: int x = 5;
    VarDeclNode decl = new VarDeclNode(
        Type.INT,
        "x",
        new LiteralExprNode(5)
    );
    
    // 期望的IR: x@0 = 5
    List<IRNode> ir = irBuilder.visit(decl);
    
    assertThat(ir).hasSize(1);
    assertThat(ir.get(0).toString()).isEqualTo("x@0 = 5");
}
```

#### 2.2 地址化测试（第4天）
```java
@Test
void testFrameSlotAllocation() {
    // 测试栈帧位置分配
    SymbolTable symbols = new SymbolTable();
    symbols.define("x", Type.INT);
    symbols.define("y", Type.INT);
    
    FrameSlot xSlot = new FrameSlot(symbols.resolve("x").getSlot());
    FrameSlot ySlot = new FrameSlot(symbols.resolve("y").getSlot());
    
    assertThat(xSlot.getOffset()).isEqualTo(0);
    assertThat(ySlot.getOffset()).isEqualTo(1);
}
```

#### 2.3 三地址码测试（第5天）
```java
@Test
void testThreeAddressCodeGeneration() {
    // 输入: a = b + c * d
    AssignmentNode assign = new AssignmentNode(
        new IdentifierNode("a"),
        new BinaryExprNode(
            new IdentifierNode("b"),
            new BinaryExprNode(
                new IdentifierNode("c"),
                new IdentifierNode("d"),
                "*"
            ),
            "+"
        )
    );
    
    List<IRNode> ir = irBuilder.visit(assign);
    
    // 期望:
    // t0 = c * d
    // t1 = b + t0
    // a = t1
    assertThat(ir).hasSize(3);
    assertThat(ir.get(0).toString()).matches("t\\d+ = c \\* d");
    assertThat(ir.get(1).toString()).matches("t\\d+ = b \\+ t\\d+");
    assertThat(ir.get(2).toString()).isEqualTo("a = t\\d+");
}
```

### 阶段3：CFG层测试实现（3天）

#### 3.1 基本块构建测试（第6天）
```java
// 扩展: BasicBlockTest.java
@Test
void testIfStatementBasicBlocks() {
    // 代码: if (x > 0) { print(1); } else { print(0); }
    String code = loadTestFile("if_statement.cymbol");
    ASTNode ast = parseCode(code);
    IRNode ir = irBuilder.visit(ast);
    
    List<BasicBlock> blocks = cfgBuilder.build(ir);
    
    // 验证基本块结构
    assertThat(blocks).hasSize(4); // entry, condition, then, else
    
    BasicBlock conditionBlock = blocks.get(1);
    assertThat(conditionBlock.getLastInstr()).isInstanceOf(CJMP.class);
    
    CJMP cjmp = (CJMP) conditionBlock.getLastInstr();
    assertThat(cjmp.getThenTarget()).isEqualTo(blocks.get(2));
    assertThat(cjmp.getElseTarget()).isEqualTo(blocks.get(3));
}
```

#### 3.2 优化测试（第7天）
```java
@Test
void testEmptyBlockElimination() {
    // 测试空基本块消除
    List<BasicBlock> original = List.of(
        createBlock(new Label("L1"), new JMP("L3")),
        createBlock(new Label("L2")), // 空块
        createBlock(new Label("L3"), new Return())
    );
    
    List<BasicBlock> optimized = optimizer.eliminateEmptyBlocks(original);
    
    assertThat(optimized).hasSize(2);
    assertThat(optimized.get(0).getLastInstr())
        .extracting(instr -> ((JMP) instr).getTarget())
        .isEqualTo("L3");
}
```

#### 3.3 数据流分析测试（第8天）
```java
@Test
void testLivenessAnalysis() {
    // 简单程序:
    // t0 = 1
    // t1 = 2
    // t2 = t0 + t1
    BasicBlock block = createBlock(
        new Assign(new Temp("t0"), new Literal(1)),
        new Assign(new Temp("t1"), new Literal(2)),
        new Assign(new Temp("t2"), new BinExpr("+", new Temp("t0"), new Temp("t1")))
    );
    
    LivenessAnalysis analysis = new LivenessAnalysis();
    Map<String, Set<String>> liveVars = analysis.analyze(block);
    
    // t0和t1在第三条指令时是活的
    assertThat(liveVars.get("inst2")).contains("t0", "t1");
}
```

### 阶段4：代码生成测试实现（2天）

#### 4.1 简单指令测试（第9天）
```java
@Test
void testLoadConstant() {
    // IR: t0 = 42
    Assign assign = new Assign(new Temp("t0"), new Literal(42));
    
    assembler.visit(assign);
    
    assertThat(assembler.getInstructions())
        .containsExactly("iconst 42");
}

@Test
void testVariableAccess() {
    // IR: t0 = x@0
    Assign assign = new Assign(
        new Temp("t0"), 
        new FrameSlot(0)
    );
    
    assembler.visit(assign);
    
    assertThat(assembler.getInstructions())
        .containsExactly("iload 0");
}
```

#### 4.2 函数调用测试（第10天）
```java
@Test
void testFunctionCallCodeGeneration() {
    // IR: call print(1, 2)
    Call call = new Call("print", 
        List.of(new Literal(1), new Literal(2))
    );
    
    assembler.visit(call);
    
    assertThat(assembler.getInstructions())
        .containsExactly(
            "iconst 1",
            "iconst 2",
            "call print(args:2)"
        );
}
```

## 📊 测试执行计划

### 每日测试执行
```bash
# 运行当天新增测试
mvn test -Dtest=*Test#test* -DfailIfNoTests=false

# 运行所有测试确保无回归
mvn test

# 生成覆盖率报告
mvn jacoco:report
```

### 周度回顾
1. **覆盖率检查**: 确保达到目标覆盖率
2. **性能测试**: 运行性能基准测试
3. **回归测试**: 验证所有历史测试通过
4. **代码审查**: 审查测试代码质量

## 🔍 调试技巧

### 1. 测试调试
```java
@Test
@Disabled("调试特定问题")
void debugSpecificIssue() {
    // 使用日志输出调试信息
    Logger logger = LoggerFactory.getLogger(getClass());
    
    ASTNode ast = parseCode("复杂代码");
    logger.debug("AST: {}", ast);
    
    IRNode ir = irBuilder.visit(ast);
    logger.debug("IR: {}", ir);
    
    // 设置断点调试
    assertThat(ir).isNotNull();
}
```

### 2. 可视化调试
```java
@Test
void visualizeCFG() {
    CFG cfg = buildCFG(testCode);
    
    // 生成Mermaid图
    String mermaid = cfg.toMermaid();
    Files.write(Paths.get("target/cfg.mmd"), mermaid.getBytes());
}
```

## 🎯 质量门控

### 1. 预提交检查
```bash
#!/bin/bash
# .git/hooks/pre-commit

# 运行测试
mvn test

# 检查覆盖率
mvn jacoco:check

# 代码风格检查
mvn checkstyle:check
```

### 2. CI/CD配置
```yaml
# .github/workflows/test.yml
name: Test
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '17'
      - run: mvn test
      - run: mvn jacoco:report
```

## 📈 进度跟踪

### 每日进度表
| 日期 | 完成任务 | 测试数 | 覆盖率 | 备注 |
|------|----------|--------|--------|------|
| Day1 | AST字面量测试 | 5 | 15% | - |
| Day2 | AST语句测试 | 8 | 25% | - |
| Day3 | IR构建测试 | 12 | 40% | - |
| Day4 | 地址化测试 | 15 | 55% | - |
| Day5 | 三地址码测试 | 18 | 65% | - |
| Day6 | CFG构建测试 | 22 | 75% | - |
| Day7 | 优化测试 | 25 | 80% | - |
| Day8 | 数据流测试 | 28 | 85% | - |
| Day9 | 代码生成测试 | 32 | 90% | - |
| Day10| 集成测试 | 35 | 95% | 完成 |

### 里程碑检查
- [ ] **M1**: AST层测试完成（第2天）
- [ ] **M2**: IR层测试完成（第5天）
- [ ] **M3**: CFG层测试完成（第8天）
- [ ] **M4**: 代码生成测试完成（第10天）
- [ ] **M5**: 集成测试完成（第10天）

## 🚨 风险缓解

### 1. 时间风险
- **风险**: 测试实现时间超出预期
- **缓解**: 优先实现P0测试，逐步完善

### 2. 复杂度风险
- **风险**: 某些组件测试过于复杂
- **缓解**: 使用mock对象简化依赖

### 3. 维护风险
- **风险**: 测试代码难以维护
- **缓解**: 建立测试编码规范，定期重构

## 📝 完成标准

### 功能完成标准
- [ ] 所有P0测试通过
- [ ] 代码覆盖率达到90%
- [ ] 测试执行时间<30秒
- [ ] 无回归缺陷
- [ ] 文档更新完成

### 质量完成标准
- [ ] 测试代码审查通过
- [ ] 性能基准测试通过
- [ ] 安全扫描通过
- [ ] 代码风格检查通过

---

**开始实施**: 按照上述步骤逐步执行，每完成一个阶段更新进度表，确保按计划完成EP20的TDD改进。