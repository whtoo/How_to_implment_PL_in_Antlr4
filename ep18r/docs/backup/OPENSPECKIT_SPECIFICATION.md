# EP18R OpenSpecKit 规范

> **版本**: 2.0.0
> **状态**: 正式发布
> **许可证**: MIT
> **维护者**: EP18R 开发团队
> **参考文档**: [EP18R_ABI_设计文档.md](EP18R_ABI_设计文档.md), [EP18R_核心设计文档.md](EP18R_核心设计文档.md)

## 概述

EP18R OpenSpecKit 定义了基于 ANTLR4 构建寄存器虚拟机的架构和实现标准。本规范为 EP18R 模块提供了一套完整的开发框架，聚焦于教育清晰度和生产就绪质量，同时与 EP18R ABI 规范严格保持一致。

## 🎯 设计理念

### 核心原则
- **教育优先**: 每个设计决策都优先考虑学习价值
- **生产质量**: 采用行业标准实践和全面测试
- **ANTLR4集成**: 与 ANTLR4 解析器生成无缝集成
- **寄存器架构**: 简洁高效的寄存器虚拟机设计
- **RISC-V启发**: 借鉴成熟的 RISC-V 架构概念
- **ABI一致性**: 严格遵循 EP18R ABI 规范，确保二进制兼容性

### 质量标准
- **100%测试覆盖率**: 所有代码必须具有全面的测试覆盖
- **TDD方法论**: 测试驱动开发是强制要求
- **文档先行**: 设计文档优先于实现
- **同行评审**: 所有变更都需要架构评审
- **性能基准**: 可衡量的性能目标
- **ABI合规性**: 所有实现必须通过 ABI 一致性测试

## 🏗️ 架构规范

### 寄存器架构

#### 寄存器文件组织
EP18R 提供 16 个 32 位通用寄存器（r0-r15），每个寄存器都有特定的 ABI 名称和用途，严格遵循 [EP18R ABI 设计文档](EP18R_ABI_设计文档.md) 的约定。

```
寄存器文件 (32位 × 16寄存器) - 符合 EP18R ABI 规范
├── r0 (zero): 硬连线为零寄存器，写入无效
├── r1 (ra): 返回地址（兼容性用途）/临时值，调用者保存
├── r2 (a0): 函数参数1/返回值，调用者保存
├── r3 (a1): 函数参数2/临时值，调用者保存
├── r4 (a2): 函数参数3/临时值，调用者保存
├── r5 (a3): 函数参数4/临时值，调用者保存
├── r6 (a4): 函数参数5/临时值，调用者保存
├── r7 (a5): 函数参数6/临时值，调用者保存
├── r8 (s0): 保存寄存器1，被调用者保存
├── r9 (s1): 保存寄存器2，被调用者保存
├── r10 (s2): 保存寄存器3，被调用者保存
├── r11 (s3): 保存寄存器4，被调用者保存
├── r12 (s4): 保存寄存器5，被调用者保存
├── r13 (sp): 栈指针，被调用者保存
├── r14 (fp): 帧指针，被调用者保存
└── r15 (lr): 链接寄存器，调用者保存
```

#### 寄存器访问规则
- **r0 (zero)**: 只读，始终返回 0
- **调用者保存寄存器 (Caller-saved)**: ra (r1), a0-a5 (r2-r7), lr (r15) - 调用者在调用前保存（如需要）
- **被调用者保存寄存器 (Callee-saved)**: s0-s4 (r8-r12), sp (r13), fp (r14) - 被调用者使用前保存，退出前恢复
- **参数传递**: 前6个整数参数通过 a0-a5 (r2-r7) 传递
- **返回值**: 单个返回值通过 a0 (r2) 寄存器返回
- **特殊寄存器**: sp (r13) 栈指针，fp (r14) 帧指针，lr (r15) 链接寄存器

### 指令集架构

EP18R 采用 32 位固定长度指令，支持三种指令格式，与 [EP18R 核心设计文档](EP18R_核心设计文档.md) 中定义的 42 条指令保持一致。

#### 指令格式

**R类型（寄存器-寄存器运算）**
```
格式: opcode rd, rs1, rs2
位域: [31:26] 操作码 (6位)
      [25:21] 目标寄存器 rd (5位)
      [20:16] 源寄存器1 rs1 (5位)
      [15:11] 源寄存器2 rs2 (5位)
      [10:0]  保留位 (11位)
```

**I类型（立即数/内存访问）**
```
格式: opcode rd, rs1, immediate
位域: [31:26] 操作码 (6位)
      [25:21] 目标寄存器 rd (5位)
      [20:16] 源寄存器 rs1 (5位)
      [15:0]  立即数 immediate (16位，符号扩展至32位)
```

**J类型（跳转指令）**
```
格式: opcode address
位域: [31:26] 操作码 (6位)
      [25:0]  跳转地址 address (26位)
```

#### 指令类别（基于 EP18R 42 条指令）

**算术运算指令** (操作码 1-4, 16-19, 22)
```
add  rd, rs1, rs2    // 整数加法: rd = rs1 + rs2
sub  rd, rs1, rs2    // 整数减法: rd = rs1 - rs2
mul  rd, rs1, rs2    // 整数乘法: rd = rs1 * rs2
div  rd, rs1, rs2    // 整数除法: rd = rs1 / rs2
neg  rd, rs1         // 整数取负: rd = -rs1
fadd rd, rs1, rs2    // 浮点加法: rd = rs1 + rs2
fsub rd, rs1, rs2    // 浮点减法: rd = rs1 - rs2
fmul rd, rs1, rs2    // 浮点乘法: rd = rs1 * rs2
fdiv rd, rs1, rs2    // 浮点除法: rd = rs1 / rs2
itof rd, rs1         // 整数转浮点: rd = (float)rs1
```

**逻辑运算指令** (操作码 12-15)
```
not  rd, rs1         // 按位取反: rd = ~rs1
and  rd, rs1, rs2    // 按位与: rd = rs1 & rs2
or   rd, rs1, rs2    // 按位或: rd = rs1 | rs2
xor  rd, rs1, rs2    // 按位异或: rd = rs1 ^ rs2
```

**比较运算指令** (操作码 5-11, 20-21)
```
slt  rd, rs1, rs2    // 小于设置: rd = (rs1 < rs2) ? 1 : 0
sle  rd, rs1, rs2    // 小于等于设置: rd = (rs1 ≤ rs2) ? 1 : 0
sgt  rd, rs1, rs2    // 大于设置: rd = (rs1 > rs2) ? 1 : 0
sge  rd, rs1, rs2    // 大于等于设置: rd = (rs1 ≥ rs2) ? 1 : 0
seq  rd, rs1, rs2    // 等于设置: rd = (rs1 == rs2) ? 1 : 0
sne  rd, rs1, rs2    // 不等于设置: rd = (rs1 != rs2) ? 1 : 0
flt  rd, rs1, rs2    // 浮点小于: rd = (rs1 < rs2) ? 1 : 0
feq  rd, rs1, rs2    // 浮点等于: rd = (rs1 == rs2) ? 1 : 0
```

**内存访问指令** (操作码 28-37)
```
li   rd, immediate       // 加载整数立即数: rd = immediate
lc   rd, immediate       // 加载字符立即数: rd = immediate
lf   rd, pool_index      // 加载浮点常量: rd = pool[pool_index]
ls   rd, pool_index      // 加载字符串常量: rd = pool[pool_index]
lw   rd, base, offset    // 加载字: rd = memory[base + offset]
sw   rs, base, offset    // 存储字: memory[base + offset] = rs
lw_g rd, offset          // 全局加载: rd = memory[GBASE + offset]
sw_g rs, offset          // 全局存储: memory[GBASE + offset] = rs
lw_f rd, offset          // 字段加载: rd = memory[obj_ptr + offset]
sw_f rs, offset          // 字段存储: memory[obj_ptr + offset] = rs
```

**控制流指令** (操作码 23-27, 41-42)
```
call target_address      // 函数调用: 压栈返回地址，PC = target
ret                      // 函数返回: 从调用栈弹出返回地址，PC = 返回地址
j    target_address      // 无条件跳转: PC = target
jt   rs, target_address  // 条件为真跳转: if (rs != 0) PC = target
jf   rs, target_address  // 条件为假跳转: if (rs == 0) PC = target
mov  rd, rs              // 寄存器移动: rd = rs
halt                     // 停止执行
```

**特殊指令** (操作码 38-40)
```
print rs                 // 打印寄存器值: print(rs)
struct rd, size          // 分配结构体: rd = allocate_struct(size)
null rd                  // 加载空指针: rd = NULL
```

### 内存架构

#### 内存布局
EP18R 采用简化的内存模型，与 [EP18R 核心设计文档](EP18R_核心设计文档.md) 中定义的内存布局保持一致。

```
EP18R 内存布局（简化模型）
0x00000000 ┌─────────────────┐
           │   代码区        │ 存储字节码指令
0x10000000 ├─────────────────┤
           │   常量池        │ 存储浮点、字符串常量
0x20000000 ├─────────────────┤
           │   全局数据区    │ 存储全局变量
0x30000000 ├─────────────────┤
           │   堆区          │ 动态分配内存（结构体）
0x40000000 ├─────────────────┤
           │   栈区          │ 函数调用栈（向下增长）
0x50000000 └─────────────────┘
```

#### 内存访问规则
- **字对齐**: 所有内存访问必须 4 字节对齐
- **小端序**: 字节顺序为小端序（Little Endian）
- **原子操作**: 单个内存操作是原子的
- **缓存一致性**: 简单的顺序一致性模型
- **栈帧访问**: 通过帧指针（FP）相对寻址访问局部变量，偏移量遵循 ABI 栈帧布局规范

### 调用约定

本部分严格遵循 [EP18R ABI 设计文档](EP18R_ABI_设计文档.md) 的规范。

#### 函数调用协议

**调用者责任**:
1. **保存调用者保存寄存器**: ra (r1), a0-a5 (r2-r7), lr (r15)（如需要）
2. **准备参数**:
   - 前6个整数参数放入寄存器 a0-a5 (r2-r7)
   - 第7+个参数从右向左压入栈中
3. **执行CALL指令**: 调用函数
4. **获取返回值**: 从 a0 (r2) 寄存器读取返回值

**被调用者责任**:
1. **保存被调用者保存寄存器**: 如果使用 s0-s4 (r8-r12), sp (r13), fp (r14)，必须在函数入口保存
2. **建立栈帧**: 如果需要局部变量或栈传递参数，分配栈帧空间
3. **执行函数体**: 执行实际功能
4. **设置返回值**: 将返回值存入 a0 (r2) 寄存器
5. **恢复保存的寄存器**: 恢复所有修改的被调用者保存寄存器
6. **执行RET指令**: 返回调用者

#### 栈帧布局（向下增长）

```
高地址
+-------------------+ ← 调用者栈帧结束
|   调用者保存区域   |   （可选，由调用者管理）
+-------------------+
|   参数7+          |   fp + 16 + 4*(n-7)
|   ...             |
|   参数8           |   fp + 20
|   参数7           |   fp + 16
+-------------------+
|   返回地址         |   fp + 12  (存储在调用栈中)
+-------------------+
|   旧帧指针(fp)     |   fp + 8   (fp旧值)
+-------------------+
|   保存寄存器s4     |   fp + 4   (r12)
|   保存寄存器s3     |   fp + 0   (r11)
|   保存寄存器s2     |   fp - 4   (r10)
|   保存寄存器s1     |   fp - 8   (r9)
|   保存寄存器s0     |   fp - 12  (r8)
+-------------------+
|   局部变量n       |   fp - 16 - 4*(n-1)
|   ...             |
|   局部变量2       |   fp - 20
|   局部变量1       |   fp - 16
+-------------------+
|   临时空间         |   （用于表达式求值等）
+-------------------+
低地址               ← sp (栈指针)
```

## 💻 实现规范

### 项目结构

#### 包组织结构
EP18R 模块采用实际的项目结构，与源代码布局保持一致。

```
org.teachfx.antlr4.ep18r/
├── stackvm/                      # 栈虚拟机核心实现
│   ├── RegisterVMInterpreter.java    # 虚拟机主解释器
│   ├── RegisterBytecodeDefinition.java # 指令集定义（42条指令）
│   ├── RegisterByteCodeAssembler.java  # 汇编器（集成ANTLR4）
│   ├── RegisterDisAssembler.java       # 反汇编器
│   ├── StackFrame.java             # 栈帧管理（调用栈支持）
│   ├── LabelSymbol.java            # 标签符号表（前向引用处理）
│   ├── CallingConventionUtils.java # 调用约定工具类（ABI支持）
│   ├── StackOffsets.java           # 栈偏移量定义（ABI栈帧布局）
│   ├── ArithmeticExecutors.java    # 算术运算执行器
│   ├── ComparisonExecutors.java    # 比较运算执行器
│   ├── ControlFlowExecutors.java   # 控制流执行器
│   ├── FunctionSymbol.java         # 函数符号
│   ├── ErrorCode.java              # 错误码定义
│   └── ExecutionContext.java       # 执行上下文
├── parser/                       # ANTLR4解析器
│   ├── VMAssemblerLexer.java     # 汇编器词法分析器（自动生成）
│   ├── VMAssemblerParser.java    # 汇编器语法分析器（自动生成）
│   ├── VMAssemblerBaseVisitor.java # 基础访问者（自动生成）
│   ├── VMAssemblerVisitor.java   # 访问者接口（自动生成）
│   └── VMAssemblerListener.java  # 监听器接口（自动生成）
├── gc/                           # 垃圾回收子系统
│   ├── GarbageCollector.java     # 垃圾回收器接口
│   ├── ReferenceCountingGC.java  # 引用计数GC实现
│   ├── GCObjectHeader.java       # GC对象头
│   └── GCStats.java              # GC统计信息
├── abi/                          # ABI测试（测试目录）
│   ├── ABIComplianceTestSuite.java # ABI一致性测试套件
│   ├── ABIDebugTest.java         # ABI调试测试
│   └── ABISimpleTest.java        # ABI简单测试
└── test/                         # 测试套件
    ├── unit/                     # 单元测试
    ├── integration/              # 集成测试
    └── performance/              # 性能测试
```

#### 命名约定

**类和接口**
- 使用帕斯卡命名法（PascalCase）: `RegisterVMInterpreter`
- 使用描述性后缀: `Instruction`, `Manager`, `Visitor`, `Executor`
- 接口名称应为名词或形容词: `Instruction`, `Executable`, `GarbageCollector`

**方法和函数**
- 使用驼峰命名法（camelCase）: `executeInstruction`
- 使用动词-名词组合: `getRegisterValue`, `setMemoryAddress`, `allocateStackFrame`
- 布尔方法应以 `is` 或 `has` 开头: `isValidRegister`, `hasOverflow`, `isMarked`

**变量和字段**
- 使用驼峰命名法: `registerFile`, `instructionPointer`, `framePointer`
- 常量使用大写蛇形命名法（UPPER_SNAKE_CASE）: `MAX_REGISTERS`, `DEFAULT_MEMORY_SIZE`
- 避免缩写，除非是众所周知的术语: `ip` 表示指令指针，`sp` 表示栈指针

**包名**
- 使用反向域名表示法: `org.teachfx.antlr4.ep18r`
- 保持包名简短有意义
- 使用单数名词: `stackvm`, `parser`, `gc` 而不是 `stackvms`, `parsers`

### 设计模式

#### 强制使用的模式

**访问者模式** (ANTLR4 集成)
```java
public class InstructionVisitor extends RegisterVMBaseVisitor<Instruction> {
    @Override
    public Instruction visitRTypeInstruction(RTypeInstructionContext ctx) {
        // Implementation
    }
}
```

**策略模式** (指令执行)
```java
public interface InstructionStrategy {
    void execute(InstructionContext context);
}

public class AddStrategy implements InstructionStrategy {
    @Override
    public void execute(InstructionContext context) {
        // ADD 指令实现
    }
}
```

**建造者模式** (虚拟机配置)
```java
public class VMConfig {
    private final int memorySize;
    private final int stackSize;

    private VMConfig(Builder builder) {
        this.memorySize = builder.memorySize;
        this.stackSize = builder.stackSize;
    }

    public static class Builder {
        private int memorySize = 1024 * 1024; // 默认 1MB
        private int stackSize = 64 * 1024;    // 默认 64KB

        public Builder memorySize(int size) {
            this.memorySize = size;
            return this;
        }

        public VMConfig build() {
            return new VMConfig(this);
        }
    }
}
```

**工厂模式** (指令创建)
```java
public class InstructionFactory {
    public static Instruction createInstruction(String opcode) {
        switch (opcode.toUpperCase()) {
            case "ADD": return new AddInstruction();
            case "SUB": return new SubInstruction();
            // ...
            default: throw new IllegalArgumentException("未知操作码: " + opcode);
        }
    }
}
```

### 异常处理

#### 异常层次结构
```java
public class VMException extends Exception {
    private final int errorCode;
    private final String instruction;
    
    public VMException(String message, int errorCode, String instruction) {
        super(message);
        this.errorCode = errorCode;
        this.instruction = instruction;
    }
}

public class InvalidInstructionException extends VMException {
    public InvalidInstructionException(String instruction) {
        super("Invalid instruction: " + instruction, 1001, instruction);
    }
}

public class RegisterAccessException extends VMException {
    public RegisterAccessException(int registerNumber) {
        super("Invalid register access: r" + registerNumber, 2001, null);
    }
}

public class MemoryAccessException extends VMException {
    public MemoryAccessException(int address, String operation) {
        super("Invalid memory " + operation + " at address: 0x" + 
              Integer.toHexString(address), 3001, null);
    }
}
```

#### Error Codes
- **1000-1999**: Instruction errors
- **2000-2999**: Register access errors
- **3000-3999**: Memory access errors
- **4000-4999**: Runtime errors
- **5000-5999**: System errors

### Logging Standards

#### Log Levels
```java
public enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
}
```

#### Logging Format
```
[YYYY-MM-DD HH:mm:ss.SSS] [LEVEL] [CLASS] MESSAGE
```

#### Required Log Points
- VM initialization and shutdown
- Instruction execution (DEBUG level)
- Memory allocation/deallocation
- Exception occurrences
- Performance-critical operations

## 🧪 Testing Specification

### Test-Driven Development (TDD)

#### TDD Cycle
1. **Red**: Write failing test first
2. **Green**: Write minimal code to pass test
3. **Refactor**: Improve code quality while maintaining tests

#### Test Structure
```java
@Test
@DisplayName("Should correctly execute ADD instruction")
void testAddInstruction() {
    // Given
    VMInterpreter vm = new VMInterpreter();
    vm.setRegister(1, 10);
    vm.setRegister(2, 20);
    
    // When
    vm.executeInstruction("ADD r3, r1, r2");
    
    // Then
    assertEquals(30, vm.getRegister(3));
    assertDoesNotThrow(() -> vm.executeInstruction("ADD r3, r1, r2"));
}
```

### Test Categories

#### Unit Tests
- **Coverage**: Individual classes and methods
- **Naming**: `{ClassName}Test.{methodName}{Scenario}Test`
- **Isolation**: Mock external dependencies
- **Speed**: Must execute in < 100ms

#### Integration Tests
- **Coverage**: Component interactions
- **Naming**: `{Feature}IntegrationTest.{scenario}Test`
- **Database**: Use test databases
- **Speed**: Must execute in < 1s

#### Performance Tests
- **Benchmarks**: Instruction execution speed
- **Memory**: Memory usage validation
- **Scalability**: Large program execution
- **Metrics**: Performance regression detection

### Test Data Management

#### Test Fixtures
```java
public class VMTestFixtures {
    public static final String SIMPLE_ADD_PROGRAM = """
        ADD r1, r2, r3
        ST [0x1000], r1
        RET r0
        """;
    
    public static final String FIBONACCI_PROGRAM = """
        # Fibonacci sequence calculation
        MOV r1, #0      # First Fibonacci number
        MOV r2, #1      # Second Fibonacci number
        MOV r3, #10     # Counter
        
    loop:
        ADD r4, r1, r2  # Next Fibonacci number
        MOV r1, r2      # Shift numbers
        MOV r2, r4
        SUB r3, r3, #1  # Decrement counter
        BNE r3, #0, loop
        RET r0
        """;
}
```

#### Property-Based Testing
```java
@Property
void registerValueShouldBePreservedAfterStoreAndLoad(@ForAll int value) {
    VMInterpreter vm = new VMInterpreter();
    int address = 0x1000;
    
    vm.setRegister(1, value);
    vm.executeInstruction("ST [" + address + "], r1");
    vm.setRegister(1, 0); // Clear register
    vm.executeInstruction("LD r2, [" + address + "]");
    
    assertEquals(value, vm.getRegister(2));
}
```

### Code Coverage Requirements

#### Minimum Coverage
- **Line Coverage**: 95%
- **Branch Coverage**: 90%
- **Method Coverage**: 100%
- **Class Coverage**: 100%

#### Coverage Exclusions
- Generated code (ANTLR4 parsers)
- Simple getters/setters
- Logging statements
- Main method entry points

## 📚 Documentation Specification

### Design Documentation

#### Architecture Decision Records (ADRs)
```markdown
# ADR-001: Register-Based Architecture Choice

## Status
Accepted

## Context
We need to choose between stack-based and register-based VM architecture for educational purposes.

## Decision
We will implement a register-based VM architecture inspired by RISC-V.

## Rationale
- Better performance for educational demonstrations
- Clearer mapping to real processor architectures
- Easier to explain instruction execution
- More intuitive for students with assembly background

## Consequences
- More complex instruction decoding
- Requires register allocation strategies
- Larger instruction size
- More complex calling conventions
```

#### API Documentation
```java
/**
 * Executes a register-based instruction in the virtual machine.
 * 
 * @param instruction The instruction to execute, must be in format "OPCODE rd, rs1, rs2"
 * @return The number of cycles taken to execute the instruction
 * @throws InvalidInstructionException if the instruction format is invalid
 * @throws RegisterAccessException if register numbers are out of range
 * @throws MemoryAccessException if memory operations fail
 * 
 * @example
 * <pre>{@code
 * VMInterpreter vm = new VMInterpreter();
 * int cycles = vm.executeInstruction("ADD r1, r2, r3");
 * System.out.println("Executed in " + cycles + " cycles");
 * }</pre>
 * 
 * @since 1.0.0
 * @see Instruction
 * @see RegisterFile
 */
public int executeInstruction(String instruction) 
    throws InvalidInstructionException, RegisterAccessException, MemoryAccessException {
    // Implementation
}
```

### Code Comments

#### Class-Level Comments
```java
/**
 * Represents the register file in the EP18R virtual machine.
 * 
 * <p>This class manages the 16 general-purpose registers (r0-r15) and provides
 * thread-safe access to register values. Register r0 is hard-wired to zero
 * and cannot be modified.</p>
 * 
 * <p>The register file implements the RISC-V ABI naming convention:</p>
 * <ul>
 *   <li>r0 (zero): Hard-wired zero</li>
 *   <li>r1 (ra): Return address</li>
 *   <li>r2 (sp): Stack pointer</li>
 *   <li>r3 (gp): Global pointer</li>
 *   <li>...</li>
 * </ul>
 * 
 * @author EP18R Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VMInterpreter
 * @see Instruction
 */
public class RegisterFile {
    // Implementation
}
```

#### Method-Level Comments
```java
/**
 * Loads a value from memory into the specified register.
 * 
 * <p>This method performs a little-endian 32-bit load from the specified
 * memory address. The address must be 4-byte aligned and within valid
 * memory bounds.</p>
 * 
 * @param register The destination register number (0-15)
 * @param address The memory address to load from
 * @throws MemoryAccessException if the address is invalid or unaligned
 * @throws RegisterAccessException if the register number is invalid
 * 
 * @implNote This method uses atomic memory operations to ensure
 *           consistency in multi-threaded environments.
 */
private void loadFromMemory(int register, int address) 
    throws MemoryAccessException, RegisterAccessException {
    // Implementation
}
```

### Version Control Documentation

#### Commit Message Format
```
type(scope): subject

body

footer
```

**Types**: feat, fix, docs, style, refactor, test, chore
**Scopes**: core, instruction, memory, parser, test, docs

**Example**:
```
feat(instruction): add MUL instruction implementation

- Implement 32-bit signed multiplication
- Add comprehensive unit tests
- Update instruction documentation
- Verify RISC-V compatibility

Closes #123
```

#### Branch Naming
```
feature/EP18R-123-add-multiplication-instruction
bugfix/EP18R-456-fix-memory-alignment-issue
docs/EP18R-789-update-api-documentation
```

## 🔧 Development Workflow

### Development Environment Setup

#### Required Tools
- **JDK**: OpenJDK 11 or higher
- **ANTLR4**: Version 4.9.3 or higher
- **Maven**: Version 3.6.0 or higher
- **IDE**: IntelliJ IDEA or Eclipse with ANTLR4 plugin

#### Project Initialization
```bash
# Clone repository
git clone https://github.com/teachfx/ep18r.git
cd ep18r

# Generate ANTLR4 parser
mvn antlr4:antlr4

# Run tests
mvn test

# Build project
mvn package
```

### Quality Gates

#### Pre-commit Checks
```bash
#!/bin/bash
# Pre-commit hook

# Run tests
mvn test
if [ $? -ne 0 ]; then
    echo "Tests failed. Commit aborted."
    exit 1
fi

# Check code coverage
mvn jacoco:check
if [ $? -ne 0 ]; then
    echo "Code coverage below threshold. Commit aborted."
    exit 1
fi

# Run static analysis
mvn spotbugs:check
if [ $? -ne 0 ]; then
    echo "Static analysis issues found. Commit aborted."
    exit 1
fi
```

#### Continuous Integration
```yaml
# .github/workflows/ci.yml
name: CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java: [11, 17, 21]
    
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK ${{ matrix.java }}
      uses: actions/setup-java@v3
      with:
        java-version: ${{ matrix.java }}
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run tests
      run: mvn test
    
    - name: Generate test report
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
```

## 📊 Performance Benchmarks

### Baseline Performance

#### Instruction Execution Speed
```
Target Performance (per instruction type):
├── R-Type: < 50ns
├── I-Type: < 40ns
├── J-Type: < 30ns
├── Memory: < 100ns
└── Branch: < 60ns
```

#### Memory Performance
```
Target Memory Performance:
├── Register Access: < 5ns
├── L1 Cache Hit: < 20ns
├── Memory Access: < 100ns
└── Page Fault: < 1ms
```

### Benchmark Suite
```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class InstructionBenchmark {
    
    @Benchmark
    public void benchmarkAddInstruction() {
        // Benchmark ADD instruction execution
    }
    
    @Benchmark
    public void benchmarkMemoryAccess() {
        // Benchmark memory load/store operations
    }
    
    @Benchmark
    public void benchmarkFunctionCall() {
        // Benchmark function call/return overhead
    }
}
```

## 🔍 Monitoring and Debugging

### Logging Configuration
```xml
<!-- logback.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/ep18r.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/ep18r.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="FILE"/>
    </root>
    
    <logger name="org.teachfx.antlr4.ep18r" level="DEBUG"/>
</configuration>
```

### Debug Features
```java
public class VMDebugger {
    private final VMInterpreter vm;
    private final boolean stepMode;
    private final Set<Integer> breakpoints;
    
    public void step() {
        Instruction current = vm.getCurrentInstruction();
        System.out.println("Executing: " + current);
        System.out.println("Registers: " + vm.getRegisterState());
        System.out.println("Memory: " + vm.getMemoryState());
        
        if (stepMode || breakpoints.contains(vm.getInstructionPointer())) {
            waitForUserInput();
        }
    }
}
```

## 🔐 Security Considerations

### Input Validation
```java
public class InputValidator {
    public static void validateInstruction(String instruction) {
        if (instruction == null || instruction.trim().isEmpty()) {
            throw new InvalidInstructionException("Instruction cannot be null or empty");
        }
        
        if (instruction.length() > MAX_INSTRUCTION_LENGTH) {
            throw new InvalidInstructionException("Instruction too long");
        }
        
        if (!INSTRUCTION_PATTERN.matcher(instruction).matches()) {
            throw new InvalidInstructionException("Invalid instruction format");
        }
    }
}
```

### Memory Protection
```java
public class MemoryProtection {
    private final BitSet protectedPages;
    
    public void validateMemoryAccess(int address, int size, AccessType type) {
        if (isProtected(address, size)) {
            throw new MemoryAccessException(address, type);
        }
        
        if (address < 0 || address + size > MAX_ADDRESS) {
            throw new MemoryAccessException(address, type);
        }
        
        if (address % 4 != 0) {
            throw new MemoryAlignmentException(address);
        }
    }
}
```

## 🚀 Deployment

### Production Deployment
```dockerfile
FROM openjdk:11-jre-slim

COPY target/ep18r-*.jar /app/ep18r.jar
COPY config/production.properties /app/config/

WORKDIR /app

USER nobody

ENTRYPOINT ["java", "-jar", "ep18r.jar", "--config", "config/production.properties"]
```

### Performance Tuning
```bash
#!/bin/bash
# JVM Performance Tuning

java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=100 \
     -XX:G1HeapRegionSize=16m \
     -XX:+UseStringDeduplication \
     -Xms1g -Xmx4g \
     -jar ep18r.jar
```

## 📋 Compliance Checklist

### Pre-release Checklist
- [ ] All tests pass (100% success rate)
- [ ] Code coverage >= 95%
- [ ] No critical/static analysis issues
- [ ] Performance benchmarks meet targets
- [ ] Documentation is complete and accurate
- [ ] Security review completed
- [ ] API documentation generated
- [ ] Changelog updated
- [ ] Version number incremented

### Post-release Checklist
- [ ] Monitor production metrics
- [ ] Verify deployment success
- [ ] Update documentation links
- [ ] Announce release
- [ ] Schedule next iteration review

## 🤝 Contributing

### Contributor Guidelines
1. Fork the repository
2. Create a feature branch
3. Write tests first (TDD)
4. Implement the feature
5. Ensure all tests pass
6. Update documentation
7. Submit pull request

### Code Review Process
1. Automated checks (CI/CD)
2. Peer review (minimum 2 approvals)
3. Architecture review (for major changes)
4. Performance review (if applicable)
5. Security review (if applicable)

## 📞 Support

### Communication Channels
- **Issues**: GitHub Issues
- **Discussions**: GitHub Discussions
- **Documentation**: Project Wiki
- **Email**: ep18r-support@teachfx.org

### Support SLA
- **Critical Issues**: 4 hours
- **High Priority**: 1 business day
- **Normal Priority**: 3 business days
- **Low Priority**: 1 week

---

*本文档是一个动态更新的文档，将随着项目的发展而更新。最后更新: 2025年12月19日*