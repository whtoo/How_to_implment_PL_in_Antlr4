# EP18R TDD重构计划

**基于专业代码审查报告的测试驱动重构方案**

---

## 概述

本文档提供了一套完整的测试驱动开发(TDD)重构计划，基于专业的代码审查结果，系统性地改进EP18R寄存器虚拟机的代码质量。遵循"红-绿-重构"原则，确保每个重构步骤都有测试保护。

**核心理念**: 先写测试，再重构代码，确保功能正确性

---

## 目录

1. [重构原则](#1-重构原则)
2. [重构阶段规划](#2-重构阶段规划)
3. [阶段1：代码清理](#3-阶段1代码清理)
4. [阶段2：配置管理](#4-阶段2配置管理)
5. [阶段3：指令执行重构](#5-阶段3指令执行重构)
6. [阶段4：异常体系](#6-阶段4异常体系)
7. [阶段5：性能优化](#7-阶段5性能优化)
8. [执行指南](#8-执行指南)
9. [验收标准](#9-验收标准)

---

## 1. 重构原则

### 1.1 TDD循环

```
每轮重构遵循TDD循环：
┌─────────────┐
│ 1. 写测试   │ → 明确需求，定义预期行为
└─────────────┘
       ↓
┌─────────────┐
│ 2. 运行测试 │ → 验证测试正确性（应失败）
└─────────────┘
       ↓
┌─────────────┐
│ 3. 重构代码 │ → 小步重构，逐步改进
└─────────────┘
       ↓
┌─────────────┐
│ 4. 运行测试 │ → 验证重构正确性（应通过）
└─────────────┘
       ↓
   下一轮重构
```

### 1.2 重构纪律

1. **小步快跑**: 每次只改一个明显的问题
2. **测试先行**: 没有测试不重构
3. **随时回滚**: 测试失败立即回滚
4. **代码评审**: 重要修改需评审
5. **文档更新**: 及时更新文档

---

## 2. 重构阶段规划

### 2.1 总体时间线

| 阶段 | 任务 | 预计时间 | 优先级 |
|------|------|----------|--------|
| 阶段1 | 代码清理（删除弃用类） | 1天 | P0 |
| 阶段2 | 配置管理统一 | 2天 | P0 |
| 阶段3 | 指令执行重构 | 5天 | P1 |
| 阶段4 | 异常体系统一 | 2天 | P1 |
| 阶段5 | 性能优化 | 3天 | P2 |

**总预计时间**: 2-3周

### 2.2 风险评估

| 风险 | 影响 | 概率 | 应对策略 |
|------|------|------|----------|
| 测试覆盖不全 | 高 | 中 | 增量测试，分步验证 |
| 回归错误 | 高 | 低 | 完整测试套件 |
| 性能下降 | 中 | 低 | 基准测试监控 |
| 时间超预算 | 中 | 中 | 优先级排序，MVP优先 |

---

## 3. 阶段1：代码清理

### 3.1 任务目标

删除或归档弃用代码，减少代码库噪音，提升可维护性。

### 3.2 具体任务

#### 任务1.1: 归档弃用类

**文件列表**:
- `CymbolStackVM.java`
- `CymbolRegisterVM.java`
- `VMInterpreter.java`

**执行步骤**:

```bash
# 1. 创建deprecated目录
mkdir -p ep18r/src/main/java/deprecated
mkdir -p ep18r/src/test/java/deprecated

# 2. 移动弃用文件
mv ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/CymbolStackVM.java \
   ep18r/src/main/java/deprecated/
mv ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/CymbolRegisterVM.java \
   ep18r/src/main/java/deprecated/
mv ep18r/src/main/java/org/teachfx/antlr4/ep18r/VMInterpreter.java \
   ep18r/src/main/java/deprecated/

# 3. 移动相关测试
mv ep18r/src/test/java/org/teachfx/antlr4/ep18r/VMInterpreterTest.java \
   ep18r/src/test/java/deprecated/
```

**测试验证**:
```java
@Test
@DisplayName("验证弃用类已归档")
void testDeprecatedClassesArchived() {
    // 确保弃用类不再被编译
    Path deprecatedDir = Paths.get("src/main/java/deprecated");
    assertThat(deprecatedDir).exists();

    // 确保主源码目录中没有弃用类
    Path mainDir = Paths.get("src/main/java/org/teachfx/antlr4/ep18r/stackvm");
    assertThat(mainDir.resolve("CymbolStackVM.java")).doesNotExist();
    assertThat(mainDir.resolve("CymbolRegisterVM.java")).doesNotExist();
}
```

#### 任务1.2: 清理导入依赖

**当前问题**:
```java
// 导入已删除的类
import org.teachfx.antlr4.ep18r.stackvm.CymbolStackVM; // ❌
import org.teachfx.antlr4.ep18r.VMInterpreter; // ❌
```

**执行步骤**:
1. 搜索所有导入已删除类的文件
2. 删除或替换这些导入
3. 编译验证

**测试验证**:
```bash
# 编译检查
mvn clean compile -pl ep18r

# 应该成功，无错误
```

#### 任务1.3: 统一命名规范

**检查项目**:
- 包名：`org.teachfx.antlr4.ep18r` ✅ 正确
- 类名：`PascalCase` ✅ 正确
- 方法名：`camelCase` ✅ 正确
- 常量名：`UPPER_SNAKE_CASE` ✅ 正确
- 局部变量：`camelCase` ✅ 正确

**微调建议**:
```java
// 统一错误消息格式
// 建议格式：[组件名] 操作失败: 原因
throw new IllegalArgumentException(
    "[RegisterVM] Invalid register number: " + regNum);
```

### 3.3 验收标准

- [ ] 所有弃用类已归档到`deprecated/`目录
- [ ] 项目编译无错误
- [ ] 所有现有测试通过
- [ ] 代码库行数减少>30%

---

## 4. 阶段2：配置管理

### 4.1 任务目标

创建统一的`VMConfig`类，替代硬编码配置参数。

### 4.2 测试设计

#### 测试2.1: VMConfig构建测试

```java
@Test
@DisplayName("VMConfig应该能正确创建")
void testVMConfigCreation() {
    VMConfig config = new VMConfig.Builder()
        .heapSize(2048 * 1024)
        .localsSize(2048)
        .maxCallStackDepth(2048)
        .maxExecutionSteps(2_000_000)
        .build();

    assertThat(config.getHeapSize()).isEqualTo(2048 * 1024);
    assertThat(config.getLocalsSize()).isEqualTo(2048);
    assertThat(config.getMaxCallStackDepth()).isEqualTo(2048);
    assertThat(config.getMaxExecutionSteps()).isEqualTo(2_000_000);
}

@Test
@DisplayName("VMConfig应该使用默认值")
void testVMConfigDefaults() {
    VMConfig config = new VMConfig.Builder().build();

    assertThat(config.getHeapSize()).isEqualTo(1024 * 1024);
    assertThat(config.getLocalsSize()).isEqualTo(1024);
    assertThat(config.getMaxCallStackDepth()).isEqualTo(1024);
    assertThat(config.getMaxExecutionSteps()).isEqualTo(1_000_000);
}
```

#### 测试2.2: 配置验证测试

```java
@Test
@DisplayName("VMConfig应该拒绝无效参数")
void testVMConfigValidation() {
    assertThatThrownBy(() -> new VMConfig.Builder()
        .heapSize(0)
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("heapSize must be positive");

    assertThatThrownBy(() -> new VMConfig.Builder()
        .maxExecutionSteps(-1)
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("maxExecutionSteps must be positive");
}
```

### 4.3 重构步骤

#### 步骤2.1: 创建VMConfig类

**位置**: `ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/VMConfig.java`

```java
package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 虚拟机配置类
 * 统一管理所有可配置参数
 */
public final class VMConfig {
    // 默认配置常量
    public static final int DEFAULT_HEAP_SIZE = 1024 * 1024; // 1MB
    public static final int DEFAULT_LOCALS_SIZE = 1024;
    public static final int DEFAULT_MAX_CALL_STACK_DEPTH = 1024;
    public static final int DEFAULT_MAX_EXECUTION_STEPS = 1_000_000;

    private final int heapSize;
    private final int localsSize;
    private final int maxCallStackDepth;
    private final int maxExecutionSteps;

    private VMConfig(Builder builder) {
        validatePositive(builder.heapSize, "heapSize");
        validatePositive(builder.localsSize, "localsSize");
        validatePositive(builder.maxCallStackDepth, "maxCallStackDepth");
        validatePositive(builder.maxExecutionSteps, "maxExecutionSteps");

        this.heapSize = builder.heapSize;
        this.localsSize = builder.localsSize;
        this.maxCallStackDepth = builder.maxCallStackDepth;
        this.maxExecutionSteps = builder.maxExecutionSteps;
    }

    private void validatePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                name + " must be positive, got: " + value);
        }
    }

    // Getters
    public int getHeapSize() { return heapSize; }
    public int getLocalsSize() { return localsSize; }
    public int getMaxCallStackDepth() { return maxCallStackDepth; }
    public int getMaxExecutionSteps() { return maxExecutionSteps; }

    // Builder模式
    public static class Builder {
        private int heapSize = DEFAULT_HEAP_SIZE;
        private int localsSize = DEFAULT_LOCALS_SIZE;
        private int maxCallStackDepth = DEFAULT_MAX_CALL_STACK_DEPTH;
        private int maxExecutionSteps = DEFAULT_MAX_EXECUTION_STEPS;

        public Builder heapSize(int size) {
            this.heapSize = size;
            return this;
        }

        public Builder localsSize(int size) {
            this.localsSize = size;
            return this;
        }

        public Builder maxCallStackDepth(int depth) {
            this.maxCallStackDepth = depth;
            return this;
        }

        public Builder maxExecutionSteps(int steps) {
            this.maxExecutionSteps = steps;
            return this;
        }

        public VMConfig build() {
            return new VMConfig(this);
        }
    }
}
```

#### 步骤2.2: 修改RegisterVMInterpreter

**修改前**:
```java
public class RegisterVMInterpreter {
    private int[] heap = new int[1024 * 1024]; // 硬编码
    private int[] locals = new int[1024];      // 硬编码
    private StackFrame[] callStack = new StackFrame[1024]; // 硬编码
    private static final int MAX_EXECUTION_STEPS = 1000000; // 硬编码
}
```

**修改后**:
```java
public class RegisterVMInterpreter {
    private final VMConfig config;
    private final int[] heap;
    private final int[] locals;
    private final StackFrame[] callStack;
    private final int maxExecutionSteps;

    public RegisterVMInterpreter(VMConfig config) {
        this.config = config;
        this.heap = new int[config.getHeapSize()];
        this.locals = new int[config.getLocalsSize()];
        this.callStack = new StackFrame[config.getMaxCallStackDepth()];
        this.maxExecutionSteps = config.getMaxExecutionSteps();
    }
}
```

**测试验证**:
```java
@Test
@DisplayName("RegisterVMInterpreter应该使用配置创建")
void testRegisterVMInterpreterUsesConfig() {
    VMConfig config = new VMConfig.Builder()
        .heapSize(2048 * 1024)
        .localsSize(2048)
        .maxCallStackDepth(512)
        .maxExecutionSteps(2_000_000)
        .build();

    RegisterVMInterpreter vm = new RegisterVMInterpreter(config);

    assertThat(vm.getHeapSize()).isEqualTo(2048 * 1024);
    assertThat(vm.getLocalsSize()).isEqualTo(2048);
    assertThat(vm.getMaxCallStackDepth()).isEqualTo(512);
    assertThat(vm.getMaxExecutionSteps()).isEqualTo(2_000_000);
}
```

#### 步骤2.3: 更新调用者代码

**需要更新的位置**:
- `RegisterVMTestBase.java`
- `InfiniteLoopFixTest.java`
- `SimpleVerificationTest.java`

**更新示例**:
```java
// 修改前
RegisterVMInterpreter interpreter = new RegisterVMInterpreter();

// 修改后
VMConfig config = new VMConfig.Builder()
    .heapSize(1024 * 1024)
    .build();
RegisterVMInterpreter interpreter = new RegisterVMInterpreter(config);
```

### 4.4 验收标准

- [ ] `VMConfig`类创建并通过所有测试
- [ ] `RegisterVMInterpreter`使用配置而非硬编码
- [ ] 所有测试更新并通过
- [ ] 代码编译无错误

---

## 5. 阶段3：指令执行重构

### 5.1 任务目标

将646行的`executeInstruction`方法拆分为多个小方法，减少代码重复，提升可维护性。

### 5.2 测试设计

#### 测试3.1: 算术指令测试

```java
@Test
@DisplayName("所有算术指令应该正确执行")
void testAllArithmeticInstructions() {
    // 测试数据：[opcode, operand1, operand2, expected]
    Object[][] testData = {
        {INSTR_ADD, 10, 20, 30},
        {INSTR_SUB, 20, 10, 10},
        {INSTR_MUL, 6, 7, 42},
        {INSTR_DIV, 20, 4, 5},
        {INSTR_AND, 12, 10, 8},   // 1100 & 1010 = 1000
        {INSTR_OR, 12, 10, 14},   // 1100 | 1010 = 1110
        {INSTR_XOR, 12, 10, 6},   // 1100 ^ 1010 = 0110
    };

    for (Object[] data : testData) {
        int opcode = (Integer) data[0];
        int val1 = (Integer) data[1];
        int val2 = (Integer) data[2];
        int expected = (Integer) data[3];

        String program = createArithmeticTestProgram(opcode, val1, val2);
        loadAndExecute(program);

        assertThat(interpreter.getRegister(3))
            .as("Opcode %s with operands %d and %d", opcode, val1, val2)
            .isEqualTo(expected);
    }
}

private String createArithmeticTestProgram(int opcode, int val1, int val2) {
    String opName = getOpcodeName(opcode);
    return String.format("""
        .def main: args=0, locals=0
            li r1, %d
            li r2, %d
            %s r3, r1, r2
            halt
        """, val1, val2, opName);
}
```

#### 测试3.2: 比较指令测试

```java
@Test
@DisplayName("所有比较指令应该正确执行")
void testAllComparisonInstructions() {
    // 测试小于指令
    assertComparison(INSTR_SLT, 10, 20, 1);  // 10 < 20 = true
    assertComparison(INSTR_SLT, 20, 10, 0);  // 20 < 10 = false

    // 测试大于指令
    assertComparison(INSTR_SGT, 20, 10, 1);  // 20 > 10 = true
    assertComparison(INSTR_SGT, 10, 20, 0);  // 10 > 20 = false

    // 测试等于指令
    assertComparison(INSTR_SEQ, 10, 10, 1);  // 10 == 10 = true
    assertComparison(INSTR_SEQ, 10, 20, 0);  // 10 == 20 = false

    // 测试不等于指令
    assertComparison(INSTR_SNE, 10, 20, 1);  // 10 != 20 = true
    assertComparison(INSTR_SNE, 10, 10, 0);  // 10 != 10 = false
}

private void assertComparison(int opcode, int val1, int val2, int expected) {
    String opName = getOpcodeName(opcode);
    String program = String.format("""
        .def main: args=0, locals=0
            li r1, %d
            li r2, %d
            %s r3, r1, r2
            halt
        """, val1, val2, opName);

    loadAndExecute(program);
    assertThat(interpreter.getRegister(3))
        .as("Comparison %s(%d, %d)", opName, val1, val2)
        .isEqualTo(expected);
}
```

### 5.3 重构策略

#### 策略3.1: 提取指令执行器接口

**创建**: `InstructionExecutor.java`

```java
package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 指令执行器接口
 * 每个指令类型对应一个实现类
 */
@FunctionalInterface
public interface InstructionExecutor {
    /**
     * 执行指令
     * @param operand 指令操作数
     * @param context 执行上下文
     * @throws Exception 执行异常
     */
    void execute(int operand, ExecutionContext context) throws Exception;
}
```

#### 策略3.2: 创建执行上下文

**创建**: `ExecutionContext.java`

```java
package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 执行上下文
 * 封装寄存器、内存等执行环境
 */
public class ExecutionContext {
    private final RegisterVMInterpreter vm;
    private final int[] registers;
    private final int programCounter;

    public ExecutionContext(RegisterVMInterpreter vm, int programCounter) {
        this.vm = vm;
        this.registers = vm.getRegisters();
        this.programCounter = programCounter;
    }

    // 寄存器访问
    public int getRegister(int regNum) {
        validateRegister(regNum);
        return registers[regNum];
    }

    public void setRegister(int regNum, int value) {
        validateRegister(regNum);
        if (regNum != 0) { // r0是只读的
            registers[regNum] = value;
        }
    }

    private void validateRegister(int regNum) {
        if (regNum < 0 || regNum >= RegisterBytecodeDefinition.NUM_REGISTERS) {
            throw new IllegalArgumentException(
                "Invalid register number: " + regNum);
        }
    }

    // 操作数提取
    public int extractRd(int operand) {
        return (operand >> 21) & 0x1F;
    }

    public int extractRs1(int operand) {
        return (operand >> 16) & 0x1F;
    }

    public int extractRs2(int operand) {
        return (operand >> 11) & 0x1F;
    }

    public int extractImm16(int operand) {
        int imm = operand & 0xFFFF;
        if ((imm & 0x8000) != 0) {
            imm |= 0xFFFF0000;
        }
        return imm;
    }

    public int extractImm26(int operand) {
        int imm = operand & 0x3FFFFFF;
        if ((imm & 0x2000000) != 0) {
            imm |= 0xFC000000;
        }
        return imm;
    }

    // 内存访问
    public int readMemory(int address) {
        return vm.readMemory(address);
    }

    public void writeMemory(int address, int value) {
        vm.writeMemory(address, value);
    }

    // 堆访问
    public int readHeap(int address) {
        return vm.readHeap(address);
    }

    public void writeHeap(int address, int value) {
        vm.writeHeap(address, value);
    }

    // 跳转控制
    public void setJumpTarget(int target) {
        vm.setJumpTarget(target);
    }

    public boolean didJump() {
        return vm.didJump();
    }

    // 异常处理
    public void throwException(Exception e) throws Exception {
        throw e;
    }

    // Getters
    public VMConfig getConfig() {
        return vm.getConfig();
    }

    public int getProgramCounter() {
        return programCounter;
    }
}
```

#### 策略3.3: 实现具体指令执行器

**创建**: `ArithmeticExecutors.java`

```java
package org.teachfx.antlr4.ep18r.stackvm;

import java.util.Map;
import java.util.function.BinaryOperator;

/**
 * 算术指令执行器集合
 */
public class ArithmeticExecutors {

    // 算术运算映射
    public static final Map<Integer, BinaryOperator<Integer>> ARITHMETIC_OPS = Map.of(
        RegisterBytecodeDefinition.INSTR_ADD, (a, b) -> a + b,
        RegisterBytecodeDefinition.INSTR_SUB, (a, b) -> a - b,
        RegisterBytecodeDefinition.INSTR_MUL, (a, b) -> a * b,
        RegisterBytecodeDefinition.INSTR_DIV, (a, b) -> {
            if (b == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return a / b;
        }
    );

    // 逻辑运算映射
    public static final Map<Integer, BinaryOperator<Integer>> LOGICAL_OPS = Map.of(
        RegisterBytecodeDefinition.INSTR_AND, (a, b) -> a & b,
        RegisterBytecodeDefinition.INSTR_OR, (a, b) -> a | b,
        RegisterBytecodeDefinition.INSTR_XOR, (a, b) -> a ^ b
    );

    // 比较运算映射
    public static final Map<Integer, ComparisonOperation> COMPARISON_OPS = Map.of(
        RegisterBytecodeDefinition.INSTR_SLT, (a, b) -> a < b ? 1 : 0,
        RegisterBytecodeDefinition.INSTR_SLE, (a, b) -> a <= b ? 1 : 0,
        RegisterBytecodeDefinition.INSTR_SGT, (a, b) -> a > b ? 1 : 0,
        RegisterBytecodeDefinition.INSTR_SGE, (a, b) -> a >= b ? 1 : 0,
        RegisterBytecodeDefinition.INSTR_SEQ, (a, b) -> a == b ? 1 : 0,
        RegisterBytecodeDefinition.INSTR_SNE, (a, b) -> a != b ? 1 : 0
    );

    @FunctionalInterface
    interface ComparisonOperation {
        int compare(int a, int b);
    }
}
```

#### 策略3.4: 重构executeInstruction方法

**修改前** (646行):
```java
private void executeInstruction(int opcode, int operand) throws Exception {
    switch (opcode) {
        case INSTR_ADD: {
            int rd = extractRd(operand);
            int rs1 = extractRs1(operand);
            int rs2 = extractRs2(operand);
            int val1 = getRegister(rs1);
            int val2 = getRegister(rs2);
            setRegister(rd, val1 + val2);
            break;
        }
        case INSTR_SUB: {
            int rd = extractRd(operand);
            int rs1 = extractRs1(operand);
            int rs2 = extractRs2(operand);
            int val1 = getRegister(rs1);
            int val2 = getRegister(rs2);
            setRegister(rd, val1 - val2);
            break;
        }
        // ... 重复40次
    }
}
```

**修改后** (约100行):
```java
private void executeInstruction(int opcode, int operand) throws Exception {
    ExecutionContext context = new ExecutionContext(this, programCounter);

    InstructionExecutor executor = EXECUTORS.get(opcode);
    if (executor == null) {
        throw new UnsupportedOperationException(
            "Unsupported opcode: " + opcode);
    }

    executor.execute(operand, context);
}

// 指令执行器映射
private static final Map<Integer, InstructionExecutor> EXECUTORS = Map.of(
    // 算术运算
    INSTR_ADD, ArithmeticExecutors.ADD,
    INSTR_SUB, ArithmeticExecutors.SUB,
    INSTR_MUL, ArithmeticExecutors.MUL,
    INSTR_DIV, ArithmeticExecutors.DIV,
    // 比较运算
    INSTR_SLT, ComparisonExecutors.SLT,
    INSTR_SLE, ComparisonExecutors.SLE,
    // ... 其他指令
);

// 在ArithmeticExecutors类中添加具体执行器
public static final InstructionExecutor ADD = (operand, context) -> {
    int rd = context.extractRd(operand);
    int rs1 = context.extractRs1(operand);
    int rs2 = context.extractRs2(operand);
    int val1 = context.getRegister(rs1);
    int val2 = context.getRegister(rs2);
    context.setRegister(rd, val1 + val2);
};
```

### 5.4 验收标准

- [ ] `executeInstruction`方法拆分为多个小方法
- [ ] 代码重复率从30%降低到5%以下
- [ ] 所有指令执行测试通过
- [ ] 代码行数减少20%

---

## 6. 阶段4：异常体系

### 6.1 任务目标

统一异常类型，提供清晰的错误信息，便于调试和错误处理。

### 6.2 测试设计

#### 测试4.1: 异常类型测试

```java
@Test
@DisplayName("除零错误应该抛出正确的异常")
void testDivisionByZeroThrowsCorrectException() {
    String program = createDivisionProgram(10, 0);

    assertThatThrownBy(() -> loadAndExecute(program))
        .isInstanceOf(VMExecutionException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIVISION_BY_ZERO)
        .hasFieldOrPropertyWithValue("pc", is(greaterThanOrEqualTo(0)))
        .extracting(Exception::getMessage)
        .asString()
        .contains("Division by zero");
}

@Test
@DisplayName("无效操作码应该抛出正确的异常")
void testInvalidOpcodeThrowsCorrectException() {
    // 通过直接构造字节码测试
    byte[] invalidBytecode = createBytecodeWithOpcode(999);

    RegisterVMInterpreter vm = createVM();
    vm.loadCode(invalidBytecode);

    assertThatThrownBy(vm::exec)
        .isInstanceOf(VMExecutionException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_OPCODE)
        .extracting(Exception::getMessage)
        .asString()
        .contains("Invalid opcode");
}
```

#### 测试4.2: 异常链测试

```java
@Test
@DisplayName("异常应该保持原始原因")
void testExceptionChaining() {
    Exception cause = new ArithmeticException("Original cause");

    VMExecutionException exception = new VMExecutionException(
        "Test message", 100, ErrorCode.DIVISION_BY_ZERO, cause);

    assertThat(exception.getCause()).isEqualTo(cause);
    assertThat(exception.getMessage()).contains("Test message");
}
```

### 6.3 重构步骤

#### 步骤4.1: 创建异常基类

```java
package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 虚拟机异常基类
 */
public abstract class VMException extends RuntimeException {
    protected final int pc;
    protected final ErrorCode errorCode;

    protected VMException(String message, int pc, ErrorCode errorCode) {
        super(message);
        this.pc = pc;
        this.errorCode = errorCode;
    }

    protected VMException(String message, int pc, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.pc = pc;
        this.errorCode = errorCode;
    }

    public int getPc() {
        return pc;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("[%s] PC=%d: %s",
            errorCode.getCode(), pc, getMessage());
    }
}
```

#### 步骤4.2: 创建具体异常类

```java
package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 执行异常
 */
public class VMExecutionException extends VMException {
    public VMExecutionException(String message, int pc, ErrorCode errorCode) {
        super(message, pc, errorCode);
    }

    public VMExecutionException(String message, int pc, ErrorCode errorCode, Throwable cause) {
        super(message, pc, errorCode, cause);
    }
}

/**
 * 内存访问异常
 */
public class VMMemoryException extends VMException {
    public VMMemoryException(String message, int pc, int address) {
        super(message, pc, ErrorCode.INVALID_ADDRESS);
        addContextData("address", address);
    }

    private void addContextData(String key, int value) {
        // 可以添加更多上下文信息
    }
}
```

#### 步骤4.3: 更新异常抛出位置

**示例: 除零错误**
```java
// 修改前
case INSTR_DIV: {
    int rd = extractRd(operand);
    int rs1 = extractRs1(operand);
    int rs2 = extractRs2(operand);
    int divisor = getRegister(rs2);
    if (divisor == 0) {
        throw new ArithmeticException("Division by zero");
    }
    setRegister(rd, getRegister(rs1) / divisor);
    break;
}

// 修改后
case INSTR_DIV: {
    int rd = context.extractRd(operand);
    int rs1 = context.extractRs1(operand);
    int rs2 = context.extractRs2(operand);
    int divisor = context.getRegister(rs2);
    if (divisor == 0) {
        throw new VMExecutionException(
            "Division by zero at PC=" + context.getProgramCounter(),
            context.getProgramCounter(),
            ErrorCode.DIVISION_BY_ZERO);
    }
    context.setRegister(rd, context.getRegister(rs1) / divisor);
    break;
}
```

### 6.4 验收标准

- [ ] 所有异常类型统一为`VMException`体系
- [ ] 异常包含PC和ErrorCode信息
- [ ] 所有异常测试通过
- [ ] 错误信息清晰可读

---

## 7. 阶段5：性能优化

### 7.1 任务目标

在不破坏功能的前提下，提升虚拟机执行性能。

### 7.2 测试设计

#### 测试7.1: 性能基准测试

```java
@Test
@DisplayName("简单循环性能测试")
void testSimpleLoopPerformance() {
    String program = generateLoopProgram(10000);

    long startTime = System.nanoTime();
    loadAndExecute(program);
    long endTime = System.nanoTime();
    long durationMs = (endTime - startTime) / 1_000_000;

    // 10000次循环应该在100ms内完成
    assertThat(durationMs).isLessThan(100);

    System.out.printf("执行10000次循环耗时: %dms (%.2f ns/次)%n",
        durationMs, (endTime - startTime) / 10000.0);
}

@Test
@DisplayName("递归计算性能测试")
void testRecursiveCalculationPerformance() {
    String program = generateFibProgram(15);

    long startTime = System.nanoTime();
    loadAndExecute(program);
    long endTime = System.nanoTime();
    long durationMs = (endTime - startTime) / 1_000_000;

    // fib(15)应该在合理时间内完成
    assertThat(durationMs).isLessThan(1000);
    assertThat(interpreter.getRegister(1)).isEqualTo(987);
}
```

#### 测试7.2: 内存使用测试

```java
@Test
@DisplayName("内存使用应该在限制内")
void testMemoryUsage() {
    // 分配大量内存
    String program = generateMemoryAllocationProgram(1000);

    Runtime runtime = Runtime.getRuntime();
    long usedBefore = runtime.totalMemory() - runtime.freeMemory();

    assertThatCodeOf(() -> loadAndExecute(program))
        .doesNotThrow();

    long usedAfter = runtime.totalMemory() - runtime.freeMemory();
    long diff = usedAfter - usedBefore;

    // 内存增长应该在合理范围内（例如<10MB）
    assertThat(diff).isLessThan(10 * 1024 * 1024);
}
```

### 7.3 优化策略

#### 策略7.1: 预解码缓存

```java
public class PreDecodedCache {
    private final PreDecodedInstruction[] cache;
    private final int size;
    private int nextIndex = 0;

    public PreDecodedCache(int size) {
        this.size = size;
        this.cache = new PreDecodedInstruction[size];
    }

    public synchronized PreDecodedInstruction get(int pc) {
        int index = (pc / 4) % size;
        return cache[index];
    }

    public synchronized void put(int pc, PreDecodedInstruction instruction) {
        int index = (pc / 4) % size;
        cache[index] = instruction;
    }
}

public class PreDecodedInstruction {
    public final int opcode;
    public final int rd, rs1, rs2;
    public final int immediate;

    public PreDecodedInstruction(int instructionWord) {
        this.opcode = (instructionWord >> 26) & 0x3F;
        this.rd = (instructionWord >> 21) & 0x1F;
        this.rs1 = (instructionWord >> 16) & 0x1F;
        this.rs2 = (instructionWord >> 11) & 0x1F;
        this.immediate = instructionWord & 0xFFFF;
    }
}
```

#### 策略7.2: 指令内联

```java
// 对于高频指令，可以内联执行逻辑
private static final InstructionExecutor INSTR_LI_FAST = (operand, context) -> {
    int rd = (operand >> 21) & 0x1F;
    int imm = operand & 0xFFFF;
    if ((imm & 0x8000) != 0) {
        imm |= 0xFFFF0000;
    }
    if (rd != 0) {
        context.getRegisters()[rd] = imm;
    }
};
```

#### 策略7.3: 边界检查优化

```java
public class OptimizedBoundsCheck {
    private static final boolean ENABLE_BOUNDS_CHECKS = true;

    public static void checkRegister(int regNum) {
        if (ENABLE_BOUNDS_CHECKS) {
            if (regNum < 0 || regNum >= 16) {
                throw new VMExecutionException(
                    "Invalid register: " + regNum,
                    -1, ErrorCode.INVALID_REGISTER);
            }
        }
    }
}
```

### 7.4 验收标准

- [ ] 10000次循环执行时间<100ms
- [ ] fib(15)计算时间<1000ms
- [ ] 内存使用<10MB
- [ ] 所有功能测试通过

---

## 8. 执行指南

### 8.1 每日执行流程

#### Day 1-3: 阶段1 (代码清理)
```bash
# 每天开始
git checkout -b refactoring/cleanup

# 执行任务
# - 归档弃用类
# - 清理导入依赖
# - 统一命名规范

# 运行测试
mvn test -pl ep18r

# 提交代码
git add .
git commit -m "refactor(ep18r): clean up deprecated classes

- Move CymbolStackVM to deprecated/
- Move CymbolRegisterVM to deprecated/
- Move VMInterpreter to deprecated/
- Remove unused imports"

# 推送并创建PR
git push origin refactoring/cleanup
```

#### Day 4-5: 阶段2 (配置管理)
```bash
git checkout -b refactoring/config

# 创建VMConfig类
# 修改RegisterVMInterpreter
# 更新测试

# 运行测试
mvn test -pl ep18r

# 提交
git commit -m "refactor(ep18r): introduce VMConfig for unified configuration

- Create VMConfig class with builder pattern
- Replace hardcoded values with config parameters
- Update all test classes to use config"
```

#### Day 6-10: 阶段3 (指令执行重构)
```bash
git checkout -b refactoring/instruction-execution

# 创建指令执行器接口
# 创建执行上下文
# 拆分executeInstruction方法

# 增量测试
# 每次拆分几个指令就运行测试

# 提交
git commit -m "refactor(ep18r): refactor instruction execution with strategy pattern

- Introduce InstructionExecutor interface
- Create ExecutionContext for register/memory access
- Split executeInstruction into smaller methods"
```

### 8.2 质量检查清单

每次提交前检查:
- [ ] 所有单元测试通过
- [ ] 代码编译无警告
- [ ] 代码覆盖率≥85%
- [ ] 性能测试通过
- [ ] 文档已更新

### 8.3 回滚计划

如果测试失败:
```bash
# 1. 查看失败信息
mvn test -pl ep18r -Dtest=FailingTest

# 2. 回滚到上一个工作版本
git checkout HEAD~1

# 3. 分析失败原因
git diff HEAD~1 HEAD --name-only

# 4. 修复问题后重新提交
```

---

## 9. 验收标准

### 9.1 代码质量指标

| 指标 | 重构前 | 重构后 | 目标 |
|------|--------|--------|------|
| 代码行数 | ~3000 | ~2400 | -20% |
| 代码重复率 | 30% | 5% | -25% |
| 方法平均长度 | 50行 | 20行 | -60% |
| 圈复杂度 | 高 | 中等 | -40% |
| 测试覆盖率 | 85% | 90% | +5% |

### 9.2 功能验收

- [ ] 所有现有功能正常工作
- [ ] 新增配置管理功能正常
- [ ] 异常处理更加清晰
- [ ] 性能无明显下降
- [ ] 内存使用合理

### 9.3 文档验收

- [ ] API文档完整
- [ ] 架构设计文档更新
- [ ] 重构日志记录
- [ ] 迁移指南编写

### 9.4 最终验证

运行完整测试套件:
```bash
# 编译
mvn clean compile -pl ep18r

# 测试
mvn test -pl ep18r

# 性能测试
mvn test -pl ep18r -Dtest=PerformanceTest

# 覆盖率
mvn jacoco:report -pl ep18r

# 验收
open target/site/jacoco/index.html
```

---

**计划制定者**: Claude Code
**最后更新**: 2025-12-16
**版本**: v1.0
