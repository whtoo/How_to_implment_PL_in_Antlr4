# EP20 TDD测试用例编写指南

## 测试用例设计策略

### 1. 测试分层架构

#### 单元测试层
- **目标**: 测试单个类或方法
- **范围**: 方法级别
- **示例**: `testBinaryExprIRGeneration()`

#### 集成测试层
- **目标**: 测试组件间交互
- **范围**: 类级别
- **示例**: `testASTToIRConversion()`

#### 端到端测试层
- **目标**: 测试完整编译流程
- **范围**: 系统级别
- **示例**: `testFullCompilationPipeline()`

### 2. 测试数据设计模式

#### 2.1 参数化测试模板
```java
@ParameterizedTest
@MethodSource("expressionProvider")
void testExpressionIRGeneration(String expression, String expectedIR) {
    // Arrange
    ASTNode ast = parseExpression(expression);
    
    // Act
    IRNode ir = irBuilder.visit(ast);
    
    // Assert
    assertThat(ir.toString()).isEqualTo(expectedIR);
}

static Stream<Arguments> expressionProvider() {
    return Stream.of(
        Arguments.of("1 + 2", "t0 = 1 + 2"),
        Arguments.of("a * b", "t1 = a * b"),
        Arguments.of("x - y + z", "t2 = x - y; t3 = t2 + z")
    );
}
```

#### 2.2 边界值测试
```java
@Test
void testIntegerOverflow() {
    // 测试整型边界值
    testExpression("2147483647 + 1", "t0 = -2147483648");
    testExpression("-2147483648 - 1", "t0 = 2147483647");
}
```

#### 2.3 异常情况测试
```java
@Test
void testDivisionByZero() {
    assertThrows(ArithmeticException.class, 
        () -> evaluate("1 / 0"));
}
```

### 3. 测试夹具设计

#### 3.1 基础测试夹具
```java
public class IRTestFixture {
    protected IRBuilder irBuilder;
    protected SymbolTable symbolTable;
    
    @BeforeEach
    void setUp() {
        symbolTable = new SymbolTable();
        irBuilder = new IRBuilder(symbolTable);
    }
}
```

#### 3.2 复杂场景夹具
```java
public class CFGTestFixture extends IRTestFixture {
    protected CFGBuilder cfgBuilder;
    
    @BeforeEach
    void setUpCFG() {
        super.setUp();
        cfgBuilder = new CFGBuilder();
    }
    
    protected BasicBlock createTestBlock(List<IRNode> instructions) {
        BasicBlock block = new BasicBlock();
        instructions.forEach(block::addInstruction);
        return block;
    }
}
```

## 测试用例实现模板

### 1. AST节点测试模板

#### 1.1 表达式节点测试
```java
package org.teachfx.antlr4.ep20.ast.expr;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep20.ast.ASTNode;
import static org.assertj.core.api.Assertions.*;

@DisplayName("表达式节点测试")
class ExpressionNodeTest {
    
    @Test
    @DisplayName("应正确创建字面量表达式节点")
    void testLiteralExprCreation() {
        // Arrange
        int value = 42;
        LiteralExprNode literal = new LiteralExprNode(value);
        
        // Act & Assert
        assertThat(literal.getValue()).isEqualTo(value);
        assertThat(literal.getType()).isEqualTo(Type.INT);
    }
    
    @Test
    @DisplayName("应正确创建二元表达式节点")
    void testBinaryExprCreation() {
        // Arrange
        ExprNode left = new LiteralExprNode(1);
        ExprNode right = new LiteralExprNode(2);
        BinaryExprNode binary = new BinaryExprNode(left, right, "+");
        
        // Act & Assert
        assertThat(binary.getLeft()).isEqualTo(left);
        assertThat(binary.getRight()).isEqualTo(right);
        assertThat(binary.getOperator()).isEqualTo("+");
    }
}
```

#### 1.2 语句节点测试
```java
@Test
@DisplayName("应正确构建if语句的AST")
void testIfStatementAST() {
    // Arrange
    ExprNode condition = new BinaryExprNode(
        new IdentifierNode("x"), 
        new LiteralExprNode(0), 
        ">"
    );
    StmtNode thenStmt = new ReturnStmtNode(new LiteralExprNode(1));
    StmtNode elseStmt = new ReturnStmtNode(new LiteralExprNode(0));
    
    // Act
    IfStmtNode ifStmt = new IfStmtNode(condition, thenStmt, elseStmt);
    
    // Assert
    assertThat(ifStmt.getCondition()).isEqualTo(condition);
    assertThat(ifStmt.getThenBranch()).isEqualTo(thenStmt);
    assertThat(ifStmt.getElseBranch()).isEqualTo(elseStmt);
}
```

### 2. IR生成测试模板

#### 2.1 表达式IR测试
```java
package org.teachfx.antlr4.ep20.ir;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep20.ir.expr.*;

@DisplayName("表达式IR生成测试")
class ExpressionIRTest extends IRTestFixture {
    
    @Test
    @DisplayName("应生成正确的二元表达式IR")
    void testBinaryExpressionIR() {
        // Arrange
        BinaryExprNode ast = new BinaryExprNode(
            new IdentifierNode("a"), 
            new IdentifierNode("b"), 
            "+"
        );
        
        // Act
        BinExpr ir = (BinExpr) irBuilder.visit(ast);
        
        // Assert
        assertThat(ir.getOperator()).isEqualTo("+");
        assertThat(ir.getLeft()).isInstanceOf(FrameSlot.class);
        assertThat(ir.getRight()).isInstanceOf(FrameSlot.class);
    }
    
    @ParameterizedTest
    @CsvSource({
        "1 + 2 + 3, t0 = 1 + 2; t1 = t0 + 3",
        "a * b + c, t0 = a * b; t1 = t0 + c",
        "a + b * c, t0 = b * c; t1 = a + t0"
    })
    @DisplayName("应正确处理表达式优先级")
    void testExpressionPrecedence(String expr, String expected) {
        // Arrange
        ASTNode ast = parseExpression(expr);
        
        // Act
        List<IRNode> ir = irBuilder.visit(ast);
        
        // Assert
        assertThat(formatIR(ir)).isEqualTo(expected);
    }
}
```

### 3. 控制流图测试模板

#### 3.1 基本块测试
```java
package org.teachfx.antlr4.ep20.cfg;

import org.junit.jupiter.api.*;
import java.util.*;

@DisplayName("控制流图构建测试")
class ControlFlowGraphTest extends CFGTestFixture {
    
    @Test
    @DisplayName("应正确构建if语句的CFG")
    void testIfStatementCFG() {
        // Arrange
        String code = """
            if (x > 0) {
                return 1;
            } else {
                return 0;
            }
            """;
        
        // Act
        ASTNode ast = parseCode(code);
        IRNode ir = irBuilder.visit(ast);
        CFG cfg = cfgBuilder.build(ir);
        
        // Assert
        assertThat(cfg.getBasicBlocks()).hasSize(4); // entry, then, else, exit
        assertThat(cfg.getEntryBlock().getSuccessors()).hasSize(2);
    }
    
    @Test
    @DisplayName("应正确识别循环结构")
    void testLoopStructure() {
        // Arrange
        String code = """
            while (i < 10) {
                i = i + 1;
            }
            """;
        
        // Act
        CFG cfg = buildCFG(code);
        
        // Assert
        assertThat(cfg.hasLoop()).isTrue();
        assertThat(cfg.getBackEdges()).hasSize(1);
    }
}
```

### 4. 代码生成测试模板

#### 4.1 虚拟机指令测试
```java
package org.teachfx.antlr4.ep20.codegen;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("虚拟机代码生成测试")
class CodeGenerationTest {
    
    private CymbolAssembler assembler;
    
    @BeforeEach
    void setUp() {
        assembler = new CymbolAssembler();
    }
    
    @Test
    @DisplayName("应生成正确的加载常量指令")
    void testLoadConstant() {
        // Arrange
        LiteralExpr literal = new LiteralExpr(42);
        
        // Act
        assembler.visit(literal);
        
        // Assert
        assertThat(assembler.getInstructions())
            .containsExactly("iconst 42");
    }
    
    @Test
    @DisplayName("应生成正确的函数调用指令")
    void testFunctionCall() {
        // Arrange
        CallExpr call = new CallExpr("print", 
            List.of(new LiteralExpr("hello")));
        
        // Act
        assembler.visit(call);
        
        // Assert
        assertThat(assembler.getInstructions())
            .containsSequence(
                "ldc \"hello\"",
                "call print(args:1)"
            );
    }
}
```

## 测试用例命名规范

### 命名格式
```
[测试方法]_[场景]_[期望结果]
```

### 示例
- `testAddition_WhenPositiveNumbers_ShouldReturnSum`
- `testDivision_WhenDivideByZero_ShouldThrowException`
- `testVariableDeclaration_WhenDuplicateName_ShouldReportError`

### 场景分类
- **正常场景**: `WhenValidInput`
- **边界场景**: `WhenBoundaryValue`
- **异常场景**: `WhenInvalidInput`
- **错误场景**: `WhenErrorCondition`

## 测试数据管理

### 1. 测试资源文件结构
```
src/test/resources/
├── ast/
│   ├── expressions/
│   ├── statements/
│   └── programs/
├── ir/
│   ├── expected/
│   └── actual/
├── cfg/
│   ├── graphs/
│   └── optimizations/
└── codegen/
    ├── instructions/
    └── programs/
```

### 2. 测试数据生成工具
```java
public class TestDataGenerator {
    
    public static String generateSimpleExpression() {
        return "1 + 2 * 3";
    }
    
    public static String generateComplexProgram() {
        return """
            int factorial(int n) {
                if (n <= 1) return 1;
                return n * factorial(n - 1);
            }
            """;
    }
}
```

## 测试执行策略

### 1. 分层执行
```bash
# 快速测试（单元测试）
mvn test -Dtest=*Test -DfailIfNoTests=false

# 集成测试
mvn test -Dtest=*IntegrationTest -DfailIfNoTests=false

# 端到端测试
mvn test -Dtest=*EndToEndTest -DfailIfNoTests=false
```

### 2. 并行执行配置
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

## 测试维护指南

### 1. 测试重构原则
- **DRY原则**: 避免重复代码
- **单一职责**: 每个测试只验证一个概念
- **可读性**: 使用描述性名称
- **可维护性**: 模块化测试夹具

### 2. 测试审查清单
- [ ] 测试名称是否清晰
- [ ] 测试数据是否合适
- [ ] 断言是否充分
- [ ] 测试是否独立
- [ ] 是否有适当的注释

### 3. 测试演进策略
- **增量添加**: 新功能必须有测试
- **回归保护**: 修复bug时添加测试
- **性能监控**: 定期运行性能测试
- **覆盖率检查**: 每次构建检查覆盖率

## 最佳实践总结

1. **测试先行**: 在实现功能之前编写测试
2. **小步快跑**: 每个测试验证一个小功能
3. **持续集成**: 每次提交都运行测试
4. **及时重构**: 保持测试代码质量
5. **文档同步**: 测试即文档，保持同步更新