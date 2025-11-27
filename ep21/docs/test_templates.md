# EP21 测试模板和示例

本文档提供P0阶段需要补充的测试用例模板，用于快速搭建测试框架。

---

## 1. AST节点测试模板

### 1.1 BinaryExprNodeTest.java
```java
package org.teachfx.antlr4.ep21.test.ast;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ast.expr.BinaryExprNode;
import org.teachfx.antlr4.ep21.ast.expr.IntExprNode;
import org.teachfx.antlr4.ep21.symtab.type.BuiltInTypeSymbol;
import static org.junit.jupiter.api.Assertions.*;

class BinaryExprNodeTest {
    
    @Test
    void testBinaryExprCreation() {
        // Arrange
        IntExprNode left = new IntExprNode(10);
        IntExprNode right = new IntExprNode(20);
        BinaryExprNode.OpType op = BinaryExprNode.OpType.ADD;
        
        // Act
        BinaryExprNode expr = new BinaryExprNode(op, left, right);
        
        // Assert
        assertEquals(op, expr.getOpType());
        assertEquals(left, expr.getLhs());
        assertEquals(right, expr.getRhs());
    }
    
    @Test
    void testBinaryExprTypePropagation() {
        // Arrange
        IntExprNode left = new IntExprNode(10);
        left.setType(BuiltInTypeSymbol.intType);
        
        IntExprNode right = new IntExprNode(20);
        right.setType(BuiltInTypeSymbol.intType);
        
        BinaryExprNode expr = new BinaryExprNode(BinaryExprNode.OpType.ADD, left, right);
        
        // Act
        expr.setType(BuiltInTypeSymbol.intType);
        
        // Assert
        assertEquals(BuiltInTypeSymbol.intType, expr.getType());
    }
    
    @Test
    void testBinaryExprLocation() {
        // Arrange
        IntExprNode left = new IntExprNode(10);
        IntExprNode right = new IntExprNode(20);
        BinaryExprNode expr = new BinaryExprNode(BinaryExprNode.OpType.SUB, left, right);
        
        // Act & Assert
        assertNotNull(expr.getLocation());
    }
}
```

### 1.2 FuncDeclNodeTest.java
```java
package org.teachfx.antlr4.ep21.test.ast;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep21.ast.decl.VarDeclListNode;
import org.teachfx.antlr4.ep21.ast.stmt.BlockStmtNode;
import org.teachfx.antlr4.ep21.symtab.type.BuiltInTypeSymbol;
import static org.junit.jupiter.api.Assertions.*;

class FuncDeclNodeTest {
    
    @Test
    void testFuncDeclCreation() {
        // Arrange & Act
        FuncDeclNode func = new FuncDeclNode(
            BuiltInTypeSymbol.intType,
            "testFunc",
            new VarDeclListNode(),
            new BlockStmtNode()
        );
        
        // Assert
        assertEquals("testFunc", func.getFuncName());
        assertEquals(BuiltInTypeSymbol.intType, func.getRetType());
        assertNotNull(func.getBody());
    }
    
    @Test
    void testFuncDeclWithParams() {
        // Arrange
        VarDeclListNode params = new VarDeclListNode();
        // 添加参数...
        
        // Act
        FuncDeclNode func = new FuncDeclNode(
            BuiltInTypeSymbol.voidType,
            "main",
            params,
            new BlockStmtNode()
        );
        
        // Assert
        assertNotNull(func.getParams());
    }
}
```

---

## 2. IR生成测试模板

### 2.1 IRGenerationTest.java
```java
package org.teachfx.antlr4.ep21.test.ir;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.expr.ConstVal;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;
import static org.junit.jupiter.api.Assertions.*;

class IRGenerationTest {
    
    @Test
    void testConstantGeneration() {
        // Arrange
        int value = 42;
        
        // Act
        ConstVal constVal = new ConstVal(value);
        
        // Assert
        assertEquals(value, constVal.getIntVal());
        assertTrue(constVal.isInt());
    }
    
    @Test
    void testBinaryOpIR() {
        // Arrange
        Operand left = new ConstVal(10);
        Operand right = new ConstVal(20);
        OperatorType op = OperatorType.ADD;
        
        // Act
        BinExpr binExpr = new BinExpr(op, left, right);
        
        // Assert
        assertEquals(op, binExpr.getOpType());
        assertEquals(left, binExpr.getLhs());
        assertEquals(right, binExpr.getRhs());
    }
    
    @Test
    void testAssignIR() {
        // Arrange
        Operand target = new VarSlot("x");
        Operand source = new ConstVal(100);
        
        // Act
        Assign assign = Assign.with(target, source);
        
        // Assert
        assertEquals(target, assign.getLhs());
        assertEquals(source, assign.getRhs());
    }
}
```

### 2.2 BasicBlockTest.java
```java
package org.teachfx.antlr4.ep21.test.ir;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.symtab.type.BuiltInTypeSymbol;
import static org.junit.jupiter.api.Assertions.*;

class BasicBlockTest {
    
    @Test
    void testBasicBlockCreation() {
        // Arrange & Act
        BasicBlock<IRNode> block = new BasicBlock<>(1);
        
        // Assert
        assertEquals(1, block.getBlockId());
        assertTrue(block.getIRNodes().isEmpty());
    }
    
    @Test
    void testBasicBlockWithInstructions() {
        // Arrange
        BasicBlock<IRNode> block = new BasicBlock<>(1);
        Label label = new Label("L1", 1);
        Assign assign = Assign.with(new Operand("x"), new ConstVal(10));
        
        // Act
        block.addIRNode(label);
        block.addIRNode(assign);
        
        // Assert
        assertEquals(2, block.getIRNodes().size());
    }
    
    @Test
    void testBasicBlockMerge() {
        // Arrange
        BasicBlock<IRNode> block1 = new BasicBlock<>(1);
        BasicBlock<IRNode> block2 = new BasicBlock<>(2);
        
        block1.addIRNode(new Assign(...));
        block2.addIRNode(new Assign(...));
        
        // Act
        block1.mergeNearBlock(block2);
        
        // Assert
        assertTrue(block2.getIRNodes().isEmpty());
    }
}
```

---

## 3. CFG构建测试模板

### 3.1 CFGBuilderTest扩展方案
```java
@Test
void testSimpleSequentialCFG() {
    // 构建简单的顺序代码CFG
    // 验证基本块划分和边关系
}

@Test
void testIfStatementCFG() {
    // 测试if语句的CFG构建
    // 验证条件分支的两条边
}

@Test
void testWhileLoopCFG() {
    // 测试while循环的CFG构建
    // 验证回边的存在
}

@Test
void testEmptyBasicBlockElimination() {
    // 测试空基本块消除优化
}
```

### 3.2 ControlFlowAnalysisTest扩展
```java
@Test
void testConstantPropagation() {
    // 测试常量传播优化
    // 验证常量被正确替换
}

@Test
void testUnreachableCodeElimination() {
    // 测试不可达代码消除
    // 验证死代码被移除
}
```

---

## 4. 符号表测试模板

### 4.1 SymbolTableTest.java
```java
package org.teachfx.antlr4.ep21.test.symtab;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.symtab.scope.*;
import org.teachfx.antlr4.ep21.symtab.symbol.*;
import org.teachfx.antlr4.ep21.symtab.type.BuiltInTypeSymbol;
import static org.junit.jupiter.api.Assertions.*;

class SymbolTableTest {
    
    @Test
    void testGlobalScope() {
        // Arrange
        GlobalScope globalScope = new GlobalScope();
        VariableSymbol var = new VariableSymbol("globalVar", BuiltInTypeSymbol.intType);
        
        // Act
        globalScope.define(var);
        
        // Assert
        assertNotNull(globalScope.resolve("globalVar"));
        assertEquals(var, globalScope.resolve("globalVar"));
    }
    
    @Test
    void testLocalScopeNesting() {
        // Arrange
        GlobalScope global = new GlobalScope();
        LocalScope local1 = new LocalScope(global);
        LocalScope local2 = new LocalScope(local1);
        
        VariableSymbol var = new VariableSymbol("var", BuiltInTypeSymbol.intType);
        
        // Act
        local2.define(var);
        
        // Assert
        assertEquals(var, local2.resolve("var"));
        assertNotNull(local2.resolve("var")); // 向上查找
    }
    
    @Test
    void testMethodSymbol() {
        // Arrange
        MethodSymbol method = new MethodSymbol("testFunc", 
            BuiltInTypeSymbol.intType, null);
        
        // Act
        method.addParameter(new VariableSymbol("x", BuiltInTypeSymbol.intType));
        method.addParameter(new VariableSymbol("y", BuiltInTypeSymbol.intType));
        
        // Assert
        assertEquals(2, method.getParameters().size());
    }
}
```

---

## 5. 集成测试模板

### 5.1 EndToEndCompilationTest.java
```java
package org.teachfx.antlr4.ep21.test.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.teachfx.antlr4.ep21.Compiler;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class EndToEndCompilationTest {
    
    @Test
    void testSimpleProgramCompilation(@TempDir Path tempDir) 
        throws IOException {
        // Arrange
        String sourceCode = """
            int main() {
                int x = 10;
                int y = 20;
                return x + y;
            }
            """;
        
        Path sourceFile = tempDir.resolve("test.cym");
        try (FileWriter writer = new FileWriter(sourceFile.toFile())) {
            writer.write(sourceCode);
        }
        
        // Act
        String[] args = {sourceFile.toString()};
        Compiler.main(args);
        
        // Assert
        // 验证生成的汇编文件
        Path outputFile = tempDir.resolve("test.s");
        assertTrue(outputFile.toFile().exists());
    }
    
    @Test
    void testControlFlowProgram(@TempDir Path tempDir) 
        throws IOException {
        // Arrange
        String sourceCode = """
            int factorial(int n) {
                if (n <= 1) {
                    return 1;
                } else {
                    return n * factorial(n - 1);
                }
            }
            
            int main() {
                return factorial(5);
            }
            """;
        
        Path sourceFile = tempDir.resolve("factorial.cym");
        try (FileWriter writer = new FileWriter(sourceFile.toFile())) {
            writer.write(sourceCode);
        }
        
        // Act
        String[] args = {"-O2", sourceFile.toString()}; // 带优化
        Compiler.main(args);
        
        // Assert
        Path outputFile = tempDir.resolve("factorial.s");
        assertTrue(outputFile.toFile().exists());
        // 可以验证优化是否生效
    }
}
```

---

## 6. 数据流分析测试模板

### 6.1 LiveVariableAnalyzerTest.java
```java
package org.teachfx.antlr4.ep21.test.analysis;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.analysis.dataflow.LiveVariableAnalyzer;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LiveVariableAnalyzerTest {
    
    @Test
    void testSimpleLiveVariableAnalysis() {
        // 构建简单的CFG
        // 变量x在定义后被使用
        // Setup: x = 10; y = x + 5;
        
        // 验证x在第二条指令中是活跃的
    }
    
    @Test
    void testControlFlowLiveVariables() {
        // 测试控制流中的活跃变量
        // if (x > 0) { y = x; } else { z = x; }
        // 验证x在分支前后都是活跃的
    }
}
```

### 6.2 SSAGraphTest.java
```java
package org.teachfx.antlr4.ep21.test.analysis;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.analysis.ssa.SSAGraph;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import static org.junit.jupiter.api.Assertions.*;

class SSAGraphTest {
    
    @Test
    void testSSABuilding() {
        // 构建CFG
        CFG<IRNode> cfg = buildTestCFG();
        
        // 构建SSA
        SSAGraph ssaGraph = new SSAGraph(cfg);
        ssaGraph.buildSSA();
        
        // 验证Φ函数插入
        // 验证变量版本号
    }
    
    @Test
    void testSSARenaming() {
        // 测试变量重命名
        // 验证版本栈的正确管理
    }
}
```

---

## 7. 快速测试清单

### 7.1 测试文件创建顺序

**第1周（P0优先级）：**
- [ ] AST节点测试（10个类）
- [ ] IR生成测试（8个类）
- [ ] CFG测试（扩展现有）
- [ ] 符号表测试（5个类）
- [ ] 1个集成测试

**第2-3周（补充）：**
- [ ] 数据流分析测试
- [ ] SSA测试
- [ ] 优化器测试
- [ ] 性能基准测试
- [ ] 更多集成测试

### 7.2 测试数据生成

```bash
#!/bin/bash
# 快速生成测试类骨架

# AST测试
for class in BinaryExprNode FuncDeclNode VarDeclNode IfStmtNode WhileStmtNode; do
    testClass="${class}Test"
    cat > "src/test/java/org/teachfx/antlr4/ep21/test/ast/${testClass}.java" <<EOF
package org.teachfx.antlr4.ep21.test.ast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ${class}Test {
    
    @Test
    void test${class}Creation() {
        // TODO: 实现测试
        assertTrue(true);
    }
}
EOF
    echo "创建测试: ${testClass}"
done
```

### 7.3 覆盖率目标

```java
// pom.xml中添加Jacoco插件配置
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
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
    </executions>
</plugin>
```

**执行覆盖率检查：**
```bash
mvn clean test
mvn jacoco:report
open target/site/jacoco/index.html
```

**覆盖率目标：**
- P0阶段：60%
- P1阶段：70%
- P2阶段：80%

---

## 8. 测试最佳实践

### 8.1 命名规范
- 测试类：`被测试类名Test`
- 测试方法：`test被测试方法场景`
- 示例：`testBinaryOpIR()`

### 8.2 组织结构
```
Arrange: 准备测试数据和对象
Act: 执行被测试的操作
Assert: 验证结果
```

### 8.3 测试原则
1. **独立性**：每个测试应该独立运行
2. **可重复**：结果不依赖外部环境
3. **小粒度**：测试单个功能点
4. **有意义的断言**：验证重要行为

### 8.4 测试分类
- **单元测试**：测试单个类或方法（占70%）
- **集成测试**：测试多个组件协作（占20%）
- **端到端测试**：测试完整流程（占10%）

---

## 9. 执行计划

### 9.1 第1周测试任务
1. **Day 1-2**: AST节点测试
2. **Day 3-4**: IR生成测试
3. **Day 5**: CFG和符号表测试
4. **Day 6-7**: 集成测试和覆盖率检查

**目标**：60%覆盖率，100+测试用例

### 9.2 使用JUnit 5特性
```java
// 参数化测试示例
@ParameterizedTest
@ValueSource(ints = {1, 2, 3, 4, 5})
void testWithParameters(int argument) {
    // 使用不同的参数运行多次
}

// 测试套件组织
@Tag("fast")
class FastTests {
    // 快速测试
}

@Tag("slow")
class SlowTests {
    // 慢速测试
}
```

---

**文档版本**: 1.0  
**创建日期**: 2025-11-27  
**说明**: 本文为P0阶段提供测试模板，可根据需要扩展
