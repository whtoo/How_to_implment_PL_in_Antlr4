# EP18 OpenSpecKit 规范

> **版本**: 2.0.0
> **状态**: 正式发布
> **许可证**: MIT
> **维护者**: EP18 开发团队
> **参考文档**: [EP18_ABI_设计文档.md](EP18_ABI_设计文档.md), [EP18_核心设计文档.md](EP18_核心设计文档.md)

## 概述

EP18 OpenSpecKit 定义了基于 ANTLR4 构建堆栈式虚拟机的架构和实现标准。本规范为 EP18 模块提供了一套完整的开发框架，聚焦于教育清晰度和生产就绪质量，同时与 EP18 ABI 规范严格保持一致。

## 🎯 设计理念

### 核心原则
- **教育优先**: 每个设计决策都优先考虑学习价值
- **生产质量**: 采用行业标准实践和全面测试
- **ANTLR4集成**: 与 ANTLR4 解析器生成无缝集成
- **堆栈架构**: 简洁高效的堆栈式虚拟机设计
- **JVM启发**: 借鉴成熟的 JVM 堆栈架构概念
- **ABI一致性**: 严格遵循 EP18 ABI 规范，确保二进制兼容性

### 质量标准
- **100%测试覆盖率**: 所有代码必须具有全面的测试覆盖
- **TDD方法论**: 测试驱动开发是强制要求
- **文档先行**: 设计文档优先于实现
- **同行评审**: 所有变更都需要架构评审
- **性能基准**: 可衡量的性能目标
- **ABI合规性**: 所有实现必须通过 ABI 一致性测试

## 🏗️ 架构规范

### 堆栈架构

#### 运行时数据区域
EP18 堆栈式虚拟机采用基于堆栈的执行模型，主要包含以下运行时数据区域：

```
运行时数据区域 - 符合 EP18 ABI 规范
├── 操作数栈 (Operand Stack)
│   ├── 大小: 可配置（默认 1024 个槽位）
│   ├── 每个槽位: 32位（整数/浮点/引用统一表示）
│   └── 操作: push, pop, dup, swap 等
├── 局部变量区 (Local Variables)
│   ├── 每个栈帧独立的局部变量数组
│   ├── 索引访问: 0 到 n-1
│   └── 支持整数、浮点、引用类型
├── 堆内存 (Heap Memory)
│   ├── 动态分配的内存区域
│   ├── 用于结构体、数组等复合对象
│   └── 垃圾回收支持
├── 调用栈 (Call Stack)
│   ├── 栈帧链表，支持嵌套调用
│   ├── 每个栈帧包含: 返回地址、局部变量、操作数栈状态
│   └── 最大深度可配置（默认 1024）
└── 常量池 (Constant Pool)
    ├── 存储编译期常量（字符串、浮点数等）
    └── 运行时只读访问
```

#### 栈帧结构
每个栈帧包含以下组件：
1. **局部变量数组**: 存储方法的局部变量
2. **操作数栈**: 用于表达式求值的栈
3. **返回地址**: 方法返回后继续执行的地址
4. **动态链接**: 指向调用者栈帧的引用
5. **方法引用**: 当前执行的方法信息

### 指令集架构

EP18 采用 32 位固定长度指令，支持三种指令格式，与 EP18 核心设计文档中定义的 42 条指令保持一致。

#### 指令格式

**S类型（栈操作指令）**
```
格式: opcode
位域: [31:26] 操作码 (6位)
      [25:0]  保留位 (26位)
```
示例: `iadd`, `isub`, `imul`, `idiv`, `fadd`, `fsub`

**I类型（立即数/索引操作）**
```
格式: opcode operand
位域: [31:26] 操作码 (6位)
      [25:0]  操作数 operand (26位，符号扩展至32位)
```
示例: `iconst`, `load`, `store`, `br`, `brt`, `brf`

**M类型（内存/方法操作）**
```
格式: opcode index1 index2
位域: [31:26] 操作码 (6位)
      [25:13] 索引1 index1 (13位)
      [12:0]  索引2 index2 (13位)
```
示例: `call`, `struct`, `fload`, `fstore`

#### 指令类别（基于 EP18 42 条指令）

**算术运算指令** (操作码 1-4, 16-19, 22)
```
iadd        // 整数加法: b=pop(), a=pop(), push(a+b)
isub        // 整数减法: b=pop(), a=pop(), push(a-b)
imul        // 整数乘法: b=pop(), a=pop(), push(a*b)
idiv        // 整数除法: b=pop(), a=pop(), push(a/b)
fadd        // 浮点加法: b=pop(), a=pop(), push(a+b)
fsub        // 浮点减法: b=pop(), a=pop(), push(a-b)
fmul        // 浮点乘法: b=pop(), a=pop(), push(a*b)
fdiv        // 浮点除法: b=pop(), a=pop(), push(a/b)
itof        // 整数转浮点: a=pop(), push((float)a)
```

**比较运算指令** (操作码 5-11, 20-21)
```
ilt         // 整数小于: b=pop(), a=pop(), push(a<b?1:0)
ile         // 整数小于等于: b=pop(), a=pop(), push(a<=b?1:0)
igt         // 整数大于: b=pop(), a=pop(), push(a>b?1:0)
ige         // 整数大于等于: b=pop(), a=pop(), push(a>=b?1:0)
ieq         // 整数等于: b=pop(), a=pop(), push(a==b?1:0)
ine         // 整数不等于: b=pop(), a=pop(), push(a!=b?1:0)
flt         // 浮点小于: b=pop(), a=pop(), push(a<b?1:0)
feq         // 浮点等于: b=pop(), a=pop(), push(a==b?1:0)
```

**逻辑运算指令** (操作码 12-15)
```
ineg        // 整数取负: a=pop(), push(-a)
inot        // 逻辑非: a=pop(), push(a==0?1:0)
iand        // 按位与: b=pop(), a=pop(), push(a&b)
ior         // 按位或: b=pop(), a=pop(), push(a|b)
ixor        // 按位异或: b=pop(), a=pop(), push(a^b)
```

**控制流指令** (操作码 23-27, 42)
```
call        // 函数调用: 保存返回地址，跳转到目标地址
ret         // 函数返回: 恢复返回地址，跳转回调用者
br          // 无条件跳转: PC = target
brt         // 条件为真跳转: if (pop() != 0) PC = target
brf         // 条件为假跳转: if (pop() == 0) PC = target
halt        // 停止执行
```

**内存访问指令** (操作码 28-37)
```
iconst      // 加载整数常量: push(constant)
cconst      // 加载字符常量: push(constant)
fconst      // 加载浮点常量: push(pool[pool_index])
sconst      // 加载字符串常量: push(pool[pool_index])
load        // 加载局部变量: push(locals[index])
store       // 存储局部变量: value=pop(), locals[index]=value
gload       // 全局加载: push(heap[GBASE+offset])
gstore      // 全局存储: value=pop(), heap[GBASE+offset]=value
fload       // 字段加载: structRef=pop(), push(structRef[offset])
fstore      // 字段存储: value=pop(), structRef=pop(), structRef[offset]=value
```

**特殊指令** (操作码 38-41)
```
print       // 打印栈顶值: print(pop())
struct      // 分配结构体: size=pop(), push(allocate_struct(size))
null        // 加载空指针: push(0)
pop         // 弹出栈顶值: pop()
```

### 内存架构

#### 内存布局
EP18 采用简化的内存模型，与 EP18 核心设计文档中定义的内存布局保持一致。

```
EP18 内存布局（简化模型）
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
- **栈帧访问**: 通过局部变量索引访问局部变量，通过操作数栈进行数据交换

### 调用约定

本部分严格遵循 EP18 ABI 设计文档的规范。

#### 函数调用协议

**调用者责任**:
1. **准备参数**: 将参数压入操作数栈（从左到右）
2. **执行CALL指令**: 调用函数
3. **获取返回值**: 从操作数栈顶部获取返回值

**被调用者责任**:
1. **建立栈帧**: 分配局部变量空间
2. **执行函数体**: 执行实际功能
3. **设置返回值**: 将返回值压入操作数栈顶部
4. **执行RET指令**: 返回调用者

#### 栈帧布局

```
高地址
+-------------------+ ← 调用者栈帧结束
|   调用者操作数栈   |   （参数已弹出）
+-------------------+
|   返回地址         |   （存储在调用栈中）
+-------------------+
|   局部变量n       |   locals[n-1]
|   ...             |
|   局部变量1       |   locals[0]
+-------------------+
|   操作数栈         |   （用于表达式求值）
+-------------------+
低地址               ← 当前栈帧基址
```

## 💻 实现规范

### 项目结构

#### 包组织结构
EP18 模块采用实际的项目结构，与源代码布局保持一致。

```
org.teachfx.antlr4.ep18/
├── stackvm/                      # 栈虚拟机核心实现
│   ├── CymbolStackVM.java        # 虚拟机主引擎
│   ├── BytecodeDefinition.java   # 指令集定义（42条指令）
│   ├── ByteCodeAssembler.java    # 汇编器（集成ANTLR4）
│   ├── DisAssembler.java         # 反汇编器
│   ├── StackFrame.java           # 栈帧管理（调用栈支持）
│   ├── LabelSymbol.java          # 标签符号表（前向引用处理）
│   ├── FunctionSymbol.java       # 函数符号
│   ├── StructValue.java          # 统一结构体表示
│   ├── VMConfig.java             # 虚拟机配置
│   ├── VMStats.java              # 性能统计
│   └── exceptions/               # 异常类
│       ├── VMException.java
│       ├── VMDivisionByZeroException.java
│       └── VMStackOverflowException.java
├── parser/                       # ANTLR4解析器
│   ├── VMAssemblerLexer.java     # 汇编器词法分析器（自动生成）
│   ├── VMAssemblerParser.java    # 汇编器语法分析器（自动生成）
│   ├── VMAssemblerBaseVisitor.java # 基础访问者（自动生成）
│   └── VMAssemblerVisitor.java   # 访问者接口（自动生成）
├── gc/                           # 垃圾回收子系统
│   ├── GarbageCollector.java     # 垃圾回收器接口
│   ├── ReferenceCountingGC.java  # 引用计数GC实现
│   ├── GCObjectHeader.java       # GC对象头
│   └── GCStats.java              # GC统计信息
├── symtab/                       # 符号表和类型系统
│   ├── scope/                    # 作用域管理
│   ├── symbol/                   # 符号定义
│   └── type/                     # 类型系统
└── test/                         # 测试套件
    ├── unit/                     # 单元测试
    ├── integration/              # 集成测试
    └── performance/              # 性能测试
```

#### 命名约定

**类和接口**
- 使用帕斯卡命名法（PascalCase）: `CymbolStackVM`
- 使用描述性后缀: `Instruction`, `Manager`, `Visitor`, `Executor`
- 接口名称应为名词或形容词: `Instruction`, `Executable`, `GarbageCollector`

**方法和函数**
- 使用驼峰命名法（camelCase）: `executeInstruction`
- 使用动词-名词组合: `getStackValue`, `setLocalVariable`, `allocateStackFrame`
- 布尔方法应以 `is` 或 `has` 开头: `isStackEmpty`, `hasOverflow`, `isMarked`

**变量和字段**
- 使用驼峰命名法: `operandStack`, `programCounter`, `framePointer`
- 常量使用大写蛇形命名法（UPPER_SNAKE_CASE）: `MAX_STACK_SIZE`, `DEFAULT_MEMORY_SIZE`
- 避免缩写，除非是众所周知的术语: `pc` 表示程序计数器, `sp` 表示栈指针

**包名**
- 使用反向域名表示法: `org.teachfx.antlr4.ep18`
- 保持包名简短有意义
- 使用单数名词: `stackvm`, `parser`, `gc` 而不是 `stackvms`, `parsers`

### 设计模式

#### 强制使用的模式

**访问者模式** (ANTLR4 集成)
```java
public class InstructionVisitor extends VMAssemblerBaseVisitor<Instruction> {
    @Override
    public Instruction visitIConstInstruction(IConstInstructionContext ctx) {
        // Implementation
    }
}
```

**策略模式** (指令执行)
```java
public interface InstructionStrategy {
    void execute(ExecutionContext context);
}

public class AddStrategy implements InstructionStrategy {
    @Override
    public void execute(ExecutionContext context) {
        // IADD 指令实现
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
            case "IADD": return new IAddInstruction();
            case "ISUB": return new ISubInstruction();
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

public class StackOverflowException extends VMException {
    public StackOverflowException(int stackSize) {
        super("Stack overflow, size: " + stackSize, 2001, null);
    }
}

public class DivisionByZeroException extends VMException {
    public DivisionByZeroException(String instruction) {
        super("Division by zero: " + instruction, 3001, instruction);
    }
}
```

#### 错误代码
- **1000-1999**: 指令错误
- **2000-2999**: 栈访问错误
- **3000-3999**: 算术运算错误
- **4000-4999**: 内存访问错误
- **5000-5999**: 系统错误

### 日志标准

#### 日志级别
```java
public enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
}
```

#### 日志格式
```
[YYYY-MM-DD HH:mm:ss.SSS] [级别] [类名] 消息
```

#### 必需的日志点
- 虚拟机初始化和关闭
- 指令执行（DEBUG级别）
- 内存分配/释放
- 异常发生
- 性能关键操作

## 🧪 测试规范

### 测试驱动开发 (TDD)

#### TDD循环
1. **红色**: 首先编写失败的测试
2. **绿色**: 编写最小代码通过测试
3. **重构**: 在保持测试通过的同时改进代码质量

#### 测试结构
```java
@Test
@DisplayName("应该正确执行IADD指令")
void testIAddInstruction() {
    // 准备
    CymbolStackVM vm = new CymbolStackVM();
    vm.push(10);
    vm.push(20);

    // 执行
    vm.executeInstruction("iadd");

    // 验证
    assertEquals(30, vm.pop());
    assertDoesNotThrow(() -> vm.executeInstruction("iadd"));
}
```

### 测试类别

#### 单元测试
- **覆盖范围**: 单个类和方法
- **命名**: `{ClassName}Test.{methodName}{Scenario}Test`
- **隔离**: 模拟外部依赖
- **速度**: 必须在100毫秒内执行完成

#### 集成测试
- **覆盖范围**: 组件交互
- **命名**: `{Feature}IntegrationTest.{scenario}Test`
- **数据库**: 使用测试数据库
- **速度**: 必须在1秒内执行完成

#### 性能测试
- **基准测试**: 指令执行速度
- **内存**: 内存使用验证
- **可扩展性**: 大型程序执行
- **指标**: 性能回归检测

### 测试数据管理

#### 测试夹具
```java
public class VMTestFixtures {
    public static final String SIMPLE_ADD_PROGRAM = """
        iconst 10
        iconst 20
        iadd
        print
        halt
        """;

    public static final String FIBONACCI_PROGRAM = """
        # 斐波那契数列计算
        .def fibonacci: args=1, locals=2
            iconst 0
            store 0      # a = 0
            iconst 1
            store 1      # b = 1
            load 0       # 加载 a
            load 1       # 加载 b
        loop:
            iadd         # c = a + b
            load 1
            store 0      # a = b
            store 1      # b = c
            # 循环逻辑...
            ret
        """;
}
```

#### 基于属性的测试
```java
@Property
void stackValueShouldBePreservedAfterStoreAndLoad(@ForAll int value) {
    CymbolStackVM vm = new CymbolStackVM();
    int localIndex = 0;

    vm.push(value);
    vm.executeInstruction("store " + localIndex);
    vm.push(0); // 清空栈
    vm.executeInstruction("load " + localIndex);

    assertEquals(value, vm.pop());
}
```

### 代码覆盖率要求

#### 最低覆盖率
- **行覆盖率**: 95%
- **分支覆盖率**: 90%
- **方法覆盖率**: 100%
- **类覆盖率**: 100%

#### 覆盖率排除项
- 生成的代码（ANTLR4解析器）
- 简单的getter/setter方法
- 日志语句
- 主方法入口点

## 📚 文档规范

### 设计文档

#### 架构决策记录 (ADRs)
```markdown
# ADR-001: 基于栈的架构选择

## 状态
已接受

## 背景
我们需要在教育目的下选择基于栈还是基于寄存器的虚拟机架构。

## 决策
我们将实现一个受JVM启发的基于栈的虚拟机架构。

## 理由
- 更简单的指令集和执行模型
- 对学生来说更容易理解
- 与表达式求值的天然映射
- 更有利于编译器理论教学

## 后果
- 更多的内存访问（栈操作）
- 复杂表达式的代码体积更大
- 更简单的编译器实现
```

#### API文档
```java
/**
 * 在虚拟机中执行基于栈的指令。
 *
 * @param instruction 要执行的指令
 * @return 执行该指令所花费的周期数
 * @throws InvalidInstructionException 如果指令格式无效
 * @throws StackOverflowException 如果栈操作超出限制
 * @throws DivisionByZeroException 如果发生除零错误
 *
 * @示例
 * <pre>{@code
 * CymbolStackVM vm = new CymbolStackVM();
 * int cycles = vm.executeInstruction("iadd");
 * System.out.println("在 " + cycles + " 个周期内执行完成");
 * }</pre>
 *
 * @since 1.0.0
 * @see Instruction
 * @see OperandStack
 */
public int executeInstruction(String instruction)
    throws InvalidInstructionException, StackOverflowException, DivisionByZeroException {
    // 实现
}
```

### 代码注释

#### 类级注释
```java
/**
 * 表示EP18虚拟机中的操作数栈。
 *
 * <p>此类管理用于表达式求值的操作数栈，并提供线程安全的栈操作访问。
 * 栈使用32位槽位，可以存储整数、浮点数或对象引用。</p>
 *
 * <p>操作数栈遵循LIFO（后进先出）语义：</p>
 * <ul>
 *   <li>push(value): 将值添加到栈顶</li>
 *   <li>pop(): 移除并返回栈顶值</li>
 *   <li>peek(): 不移除的情况下返回栈顶值</li>
 * </ul>
 *
 * @作者 EP18开发团队
 * @版本 1.0.0
 * @自从 1.0.0
 * @参见 CymbolStackVM
 * @参见 StackFrame
 */
public class OperandStack {
    // 实现
}
```

#### 方法级注释
```java
/**
 * 将值压入操作数栈。
 *
 * <p>此方法将32位值添加到操作数栈的顶部。
 * 如果栈已满，则抛出StackOverflowException。</p>
 *
 * @param value 要压入的值（32位整数、浮点位或引用）
 * @throws StackOverflowException 如果栈已满
 *
 * @implNote 此方法使用边界检查确保栈安全。
 *           在存储值之后栈指针会递增。
 */
private void push(int value) throws StackOverflowException {
    // 实现
}
```

### 版本控制文档

#### 提交消息格式
```
type(scope): 主题

正文

页脚
```

**类型**: feat, fix, docs, style, refactor, test, chore
**范围**: core, instruction, memory, parser, test, docs

**示例**:
```
feat(instruction): 添加FMUL指令实现

- 实现32位浮点乘法
- 添加全面的单元测试
- 更新指令文档
- 验证IEEE 754合规性

关闭 #123
```

#### 分支命名
```
feature/EP18-123-添加浮点乘法
bugfix/EP18-456-修复栈溢出检测
docs/EP18-789-更新API文档
```

## 🔧 开发工作流程

### 开发环境设置

#### 必需工具
- **JDK**: OpenJDK 11或更高版本
- **ANTLR4**: 4.9.3或更高版本
- **Maven**: 3.6.0或更高版本
- **IDE**: IntelliJ IDEA或带ANTLR4插件的Eclipse

#### 项目初始化
```bash
# 克隆仓库
git clone https://github.com/teachfx/ep18.git
cd ep18

# 生成ANTLR4解析器
mvn antlr4:antlr4

# 运行测试
mvn test

# 构建项目
mvn package
```

### 质量门禁

#### 提交前检查
```bash
#!/bin/bash
# 提交前钩子

# 运行测试
mvn test
if [ $? -ne 0 ]; then
    echo "测试失败。提交中止。"
    exit 1
fi

# 检查代码覆盖率
mvn jacoco:check
if [ $? -ne 0 ]; then
    echo "代码覆盖率低于阈值。提交中止。"
    exit 1
fi

# 运行静态分析
mvn spotbugs:check
if [ $? -ne 0 ]; then
    echo "发现静态分析问题。提交中止。"
    exit 1
fi
```

#### 持续集成
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
    - name: 设置JDK ${{ matrix.java }}
      uses: actions/setup-java@v3
      with:
        java-version: ${{ matrix.java }}
        distribution: 'temurin'

    - name: 缓存Maven依赖
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

    - name: 运行测试
      run: mvn test

    - name: 生成测试报告
      run: mvn jacoco:report

    - name: 上传覆盖率到Codecov
      uses: codecov/codecov-action@v3
```

## 📊 性能基准

### 基线性能

#### 指令执行速度
```
目标性能（每类指令）:
├── 栈操作: < 30ns
├── 算术运算: < 40ns
├── 控制流: < 50ns
├── 内存访问: < 100ns
└── 函数调用: < 200ns
```

#### 内存性能
```
目标内存性能:
├── 栈压入/弹出: < 20ns
├── 局部变量访问: < 30ns
├── 堆分配: < 200ns
└── 垃圾回收: < 1ms每MB
```

### 基准测试套件
```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class InstructionBenchmark {

    @Benchmark
    public void benchmarkIAddInstruction() {
        // 基准测试IADD指令执行
    }

    @Benchmark
    public void benchmarkMemoryAccess() {
        // 基准测试内存加载/存储操作
    }

    @Benchmark
    public void benchmarkFunctionCall() {
        // 基准测试函数调用/返回开销
    }
}
```

## 🔍 监控和调试

### 日志配置
```xml
<!-- logback.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/ep18.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/ep18.%d{yyyy-MM-dd}.log</fileNamePattern>
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

    <logger name="org.teachfx.antlr4.ep18" level="DEBUG"/>
</configuration>
```

### 调试功能
```java
public class VMDebugger {
    private final CymbolStackVM vm;
    private final boolean stepMode;
    private final Set<Integer> breakpoints;

    public void step() {
        Instruction current = vm.getCurrentInstruction();
        System.out.println("执行中: " + current);
        System.out.println("栈: " + vm.getStackState());
        System.out.println("局部变量: " + vm.getLocalVariables());

        if (stepMode || breakpoints.contains(vm.getProgramCounter())) {
            waitForUserInput();
        }
    }
}
```

## 🔐 安全考虑

### 输入验证
```java
public class InputValidator {
    public static void validateInstruction(String instruction) {
        if (instruction == null || instruction.trim().isEmpty()) {
            throw new InvalidInstructionException("指令不能为空或为null");
        }

        if (instruction.length() > MAX_INSTRUCTION_LENGTH) {
            throw new InvalidInstructionException("指令太长");
        }

        if (!INSTRUCTION_PATTERN.matcher(instruction).matches()) {
            throw new InvalidInstructionException("无效的指令格式");
        }
    }
}
```

### 内存保护
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

## 🚀 部署

### 生产环境部署
```dockerfile
FROM openjdk:11-jre-slim

COPY target/ep18-*.jar /app/ep18.jar
COPY config/production.properties /app/config/

WORKDIR /app

USER nobody

ENTRYPOINT ["java", "-jar", "ep18.jar", "--config", "config/production.properties"]
```

### 性能调优
```bash
#!/bin/bash
# JVM性能调优

java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=100 \
     -XX:G1HeapRegionSize=16m \
     -XX:+UseStringDeduplication \
     -Xms1g -Xmx4g \
     -jar ep18.jar
```

## 📋 合规检查清单

### 发布前检查清单
- [ ] 所有测试通过（100%成功率）
- [ ] 代码覆盖率 >= 95%
- [ ] 无关键/静态分析问题
- [ ] 性能基准达到目标
- [ ] 文档完整准确
- [ ] 安全审查完成
- [ ] API文档已生成
- [ ] 更新日志已更新
- [ ] 版本号已递增

### 发布后检查清单
- [ ] 监控生产指标
- [ ] 验证部署成功
- [ ] 更新文档链接
- [ ] 发布公告
- [ ] 安排下次迭代审查

## 🤝 贡献指南

### 贡献者指南
1. Fork仓库
2. 创建功能分支
3. 首先编写测试（TDD）
4. 实现功能
5. 确保所有测试通过
6. 更新文档
7. 提交Pull Request

### 代码审查流程
1. 自动化检查（CI/CD）
2. 同行审查（最少2个批准）
3. 架构审查（重大变更）
4. 性能审查（如适用）
5. 安全审查（如适用）

## 📞 支持

### 沟通渠道
- **问题**: GitHub Issues
- **讨论**: GitHub Discussions
- **文档**: 项目Wiki
- **邮件**: ep18-support@teachfx.org

### 支持SLA
- **关键问题**: 4小时
- **高优先级**: 1个工作日
- **普通优先级**: 3个工作日
- **低优先级**: 1周

---

*本文档是一个动态更新的文档，将随着项目的发展而更新。最后更新: 2025年12月19日*