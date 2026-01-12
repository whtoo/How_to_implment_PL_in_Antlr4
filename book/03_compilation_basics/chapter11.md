# 第11章：虚拟机设计与垃圾回收

## 11.1 本章概述

本章将带你深入编译器后端的执行引擎,实现一个完整的栈式虚拟机和引用计数垃圾回收系统。这是编译器流水线的最后一环,负责执行前面章节生成的字节码,并自动管理程序运行时的内存分配与回收。你在整个编译器流水线中的位置是:**IR生成 → 优化 → 字节码生成 → [虚拟机执行]** ← 当前位置

## 11.2 动机与真实场景

想象你在一家云计算公司工作,正在开发一个Serverless函数计算平台。开发者上传的代码会被编译成字节码并在隔离的沙箱环境中执行。系统每天需要处理数百万次函数调用,每调用一次都可能创建大量临时对象。

如果没有高效的虚拟机和垃圾回收系统,你会面临这些真实痛点:
- **内存泄漏**: 一次函数调用后未正确回收对象,导致长期运行后OOM(Out of Memory)
- **性能瓶颈**: 频繁的内存分配和回收影响响应速度
- **复杂错误管理**: 栈溢出、除零错误、内存越界等异常难以追踪
- **调试困难**: 无法追踪函数调用链和内存使用情况

通过本章学习,你将掌握如何设计一个既有高性能又有可靠错误处理的执行引擎,这正是现代编程语言运行时的核心能力。

## 11.3 人类工程师线：技术与实现

### 11.3.1 核心概念

#### 1. 栈式虚拟机执行模型

栈式虚拟机(Stack-based VM)使用操作数栈来计算表达式,与寄存器虚拟机(Register-based VM)形成对比。想象你在用计算器算 `(3 + 5) * 2`:

```
[图1：栈式计算过程]
步骤1: 压入3         [3]
步骤2: 压入5         [3, 5]
步骤3: 执行ADD        [8]   (弹出3和5,计算8,压入结果)
步骤4: 压入2         [8, 2]
步骤5: 执行MUL        [16]  (弹出8和2,计算16,压入结果)
```

**关键要点**:
- 操作数栈是LIFO(后进先出)数据结构,`push()`和`pop()`都是O(1)操作
- 每条指令从栈顶弹出所需操作数,计算后将结果压回栈
- 不需要指定操作数位置,简化了指令编码

#### 2. 指令获取-译码-执行循环

虚拟机的核心是无限循环,不断从内存取指令、译码、执行:

```
[图2：取指-译码-执行循环]
while (running) {
    1. FETCH: instruction = memory[PC]      // 从程序计数器位置取指令
    2. DECODE: opcode = instruction >> 24 // 提取操作码(高8位)
    3. DECODE: operand = instruction & 0xFFFFFF // 提取操作数(低24位)
    4. EXECUTE: dispatch(opcode)          // 根据操作码执行对应操作
    5. UPDATE: PC++                        // 程序计数器前进到下一条指令
}
```

**比喻**: 这就像一个精简的CPU,只是用软件模拟:
- **Fetch(取指)**: 从指令缓存"读"下一条要做什么
- **Decode(译码)**: 分析这条指令是"加法"还是"跳转",需要什么操作数
- **Execute(执行)**: 真正干活——加减乘除或改变PC

#### 3. 引用计数垃圾回收原理

引用计数(Reference Counting)是最直观的GC算法。想象图书馆的借阅系统:

```
[图3：引用计数生命周期]
对象创建: refCount = 1 (分配者持有引用)
变量赋值: refCount++    (新的引用增加计数)
变量清空: refCount--    (引用减少)
refCount == 0: 立即回收 (没人引用了,可以放回书架)
```

**核心机制**:
- 每个对象头部存储`refCount`(引用计数)
- `push()`一个对象引用时:`refCount++`
- `pop()`一个对象引用时:`refCount--`
- `refCount == 0`时:对象可被立即回收

**优点**: 实时回收(零停顿)、实现简单  
**缺点**: 无法处理循环引用(如:A引用B,B引用A)

### 11.3.2 与仓库EP的对应关系

本章对应`ep18/`模块,核心组件组织如下:

```
ep18/src/main/java/org/teachfx/antlr4/ep18/
├── stackvm/              # 虚拟机引擎
│   ├── CymbolStackVM.java         # 主虚拟机引擎
│   ├── ByteCodeAssembler.java      # 字节码汇编器
│   ├── BytecodeDefinition.java     # 指令定义
│   ├── StackFrame.java            # 栈帧管理
│   ├── VMConfig.java              # 虚拟机配置
│   └── StructValue.java           # 结构体运行时表示
├── gc/                   # 垃圾回收系统
│   ├── ReferenceCountingGC.java   # 引用计数GC实现
│   ├── GarbageCollector.java      # GC接口
│   ├── GCObjectHeader.java        # GC对象头
│   └── GCStats.java             # GC统计信息
└── parser/               # ANTLR4生成的汇编解析器
    └── VMAssembler.g4           # VM汇编语法
```

**关键类职责**:

1. **CymbolStackVM**(主引擎)
   - 管理操作数栈(`stack[]`)和栈指针(`stackPointer`)
   - 维护指令缓存(`instructionCache[]`)和程序计数器(`programCounter`)
   - 管理调用栈(`callStack[]`)和帧指针(`framePointer`)
   - 集成垃圾回收器(`GarbageCollector`)

2. **ReferenceCountingGC**(GC实现)
   - 使用`TreeMap<Integer, FreeBlock>`管理空闲链表
   - 对象分配时应用首次适应(First-Fit)算法
   - 引用计数归零时自动触发回收
   - 自动合并相邻空闲块减少碎片

3. **ByteCodeAssembler**(字节码汇编)
   - 继承ANTLR4的`VMAssemblerBaseListener`
   - 将汇编代码(如`iconst 5`)编译成32位字节码
   - 处理前向引用和标签解析
   - 管理常量池和全局变量

**编译器流水线集成**:
```
前端(EP1-EP12): 词法+语法+语义分析
          ↓
中间表示(EP13-EP16): AST+符号表+类型检查
          ↓
优化与代码生成(EP17-EP21): CFG+IR+优化
          ↓
字节码生成(EP18的ByteCodeAssembler)
          ↓
虚拟机执行(EP18的CymbolStackVM) ← 你在这里
```

### 11.3.3 实战流程

#### 步骤1: 构建EP18模块

```bash
# 进入EP18目录
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep18

# 清理并编译
mvn clean compile

# 预期输出:
# [INFO] BUILD SUCCESS
# [INFO] Total time: X.XXX s
```

#### 步骤2: 运行虚拟机核心测试

```bash
# 运行虚拟机单元测试
mvn test -Dtest=CymbolStackVMTest

# 观察输出,重点看:
# - 算术指令测试(IADD, ISUB, IMUL, IDIV)
# - 比较指令测试(ILT, IGT, IEQ, INE)
# - 逻辑指令测试(IAND, IOR, IXOR)
# - 控制流指令测试(BR, BRT, BRF)
# - 函数调用测试(CALL, RET)
```

**预期测试结果**:
```
[INFO] Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

如果测试失败,检查:
1. 是否所有依赖都已正确下载(`mvn clean install`从根目录)
2. Java版本是否为21+ (`java -version`)
3. ANTLR4生成代码是否正确(删除`target/generated-sources/`重新编译)

#### 步骤3: 手工编写并执行VM汇编代码

创建文件`src/main/resources/simple.vm`:

```vm
# 简单加法程序
iconst 5
iconst 3
iadd
print
halt
```

执行这段汇编代码:

```bash
# 方式1: 使用VMInterpreter
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.VMInterpreter" \
    -Dexec.args="simple.vm"

# 方式2: 先汇编再执行
java -cp target/classes org.teachfx.antlr4.ep18.stackvm.ByteCodeAssembler \
    simple.vm simple.cvm

# 查看生成的字节码
hexdump -C simple.cvm | head -20

# 使用CymbolStackVM直接执行字节码
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.VMRunner"
```

**预期输出**:
```
8
```

#### 步骤4: 测试函数调用

创建文件`src/main/resources/function_call.vm`:

```vm
# 函数调用示例: 5 + 3
iconst 5
iconst 3
call add
print
halt

add:
    load 0    ; 加载第一个参数
    load 1    ; 加载第二个参数
    iadd      ; 相加
    ret       ; 返回结果
```

执行并观察调用栈行为:

```bash
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.VMInterpreter" \
    -Dexec.args="-trace function_call.vm"
```

**预期输出(启用trace模式)**:
```
[TRACE] PC=0: iconst 5  -> stack: [5]
[TRACE] PC=5: iconst 3  -> stack: [5, 3]
[TRACE] PC=10: call add  -> push frame, PC=20
[TRACE] PC=20: load 0    -> stack: [5, 3, 5]
[TRACE] PC=25: load 1    -> stack: [5, 3, 5, 3]
[TRACE] PC=30: iadd      -> stack: [5, 3, 8]
[TRACE] PC=35: ret       -> pop frame, result=8
8
```

#### 步骤5: 测试垃圾回收

创建文件`src/main/resources/gc_test.vm`:

```vm
# 创建多个结构体并测试GC
struct 2        ; 创建2字段结构体A
store 0        ; 保存引用到局部变量0
struct 2        ; 创建2字段结构体B
store 1        ; 保存引用到局部变量1

# 修改结构体A的字段
load 0
iconst 10
fstore 0       ; A.field0 = 10
iconst 20
fstore 1       ; A.field1 = 20

# 覆盖局部变量0,A的引用计数减少到0
iconst 0
store 0        ; 触发回收A

# 此时结构体A应已被回收
halt
```

执行并启用GC调试:

```bash
cd ep18
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.stackvm.CymbolStackVM"
```

**在代码中启用GC调试**(修改VMConfig):

```java
VMConfig config = VMConfig.builder()
    .setDebugMode(true)           // 启用调试日志
    .setEnableGC(true)            // 启用GC
    .setGcHeapSize(1024 * 1024) // 1MB堆
    .build();
```

**预期输出(调试模式)**:
```
[GC] Initialized reference-counting GC with heap size: 1048576
[GC] executeStruct called: nfields=2
[GC] executeStruct: allocated structId=1, isAlive=true
[GC] PUSH incrementRef(1)
[GC] executeStruct called: nfields=2
[GC] executeStruct: allocated structId=2, isAlive=true
[GC] PUSH incrementRef(2)
[GC] POP decrementRef(1)
[GC] collectObject:回收 structId=1 (16 bytes)
[GC] PUSH incrementRef(0)  ; null引用,不增加计数
```

#### 步骤6: 性能基准测试

运行JMH性能基准:

```bash
cd ep18
mvn clean install

# 运行GC性能测试
mvn test -Dtest=GCPerformanceBenchmark

# 预期输出:
# [INFO] Result "org.teachfx.antlr4.ep18.gc.performance.GCPerformanceBenchmark.testAllocationSpeed":
# [INFO]   50000.234 ± 1234.567  ops/s [Average]
# [INFO]   (min=45000.123, avg=50000.234, max=55000.345, p95=53000.123)
```

#### 故障排查指南

**问题1: 栈溢出错误(`VMStackOverflowException`)**
- 原因: 递归过深或无限递归
- 解决: 检查递归终止条件,增加栈大小(`VMConfig.setStackSize()`)

**问题2: 除零错误(`VMDivisionByZeroException`)**
- 原因: IDIV或FDIV指令的分母为0
- 解决: 在除法前检查分母,或添加try-catch处理

**问题3: 垃圾回收未释放内存**
- 原因: 引用计数泄漏(忘记`decrementRef`)
- 解决: 在`pop()`和`store`时正确减少引用计数

**问题4: 字节码编码错误**
- 原因: 大端序处理错误或符号扩展错误
- 解决: 使用`InstructionEncoder`工具类验证编码

## 11.4 AI协作线:Context Engineering视角

### 11.4.1 上下文设计

为了让AI高效完成虚拟机和GC任务,你需要提供完整的上下文:

#### 必需源码文件(按优先级排序):

1. **虚拟机核心**
   - `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/CymbolStackVM.java` - 主引擎实现
   - `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/BytecodeDefinition.java` - 指令集定义
   - `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/VMConfig.java` - 配置系统

2. **垃圾回收系统**
   - `ep18/src/main/java/org/teachfx/antlr4/ep18/gc/ReferenceCountingGC.java` - GC实现
   - `ep18/src/main/java/org/teachfx/antlr4/ep18/gc/GarbageCollector.java` - GC接口
   - `ep18/src/main/java/org/teachfx/antlr4/ep18/gc/GCObjectHeader.java` - 对象头定义

3. **支持组件**
   - `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/StackFrame.java` - 栈帧管理
   - `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/StructValue.java` - 结构体表示

4. **测试与示例**
   - `ep18/src/test/java/org/teachfx/antlr4/ep18/CymbolStackVMTest.java` - 核心测试
   - `ep18/src/test/java/org/teachfx/antlr4/ep18/gc/GarbageCollectorTest.java` - GC测试
   - `ep18/src/main/resources/*.vm` - VM汇编示例

#### 设计文档(可选但推荐):

- `ep18/README.md` - EP18架构概览
- `ep18/docs/垃圾回收机制实现.md` - GC详细设计
- `AGENTS.md` - 代码风格和构建系统规范

#### 上下文组织说明:

**为什么选择这些文件?**
1. **CymbolStackVM**是虚拟机主入口,包含完整的取指-译码-执行循环,是理解整体架构的关键
2. **ReferenceCountingGC**展示了引用计数算法的完整实现,包括空闲链表管理和块合并逻辑
3. **测试文件**提供了边界情况覆盖(如除零、栈溢出、GC触发条件),这些对AI理解正确行为至关重要

**上下文完整性检查**:
- ✅ 指令编码格式(32位统一编码)
- ✅ 栈操作语义(`push`/`pop`时自动管理引用计数)
- ✅ 函数调用ABI(参数传递、返回值、栈帧管理)
- ✅ GC触发条件(分配失败时自动触发`collect()`)
- ✅ 异常处理(除零、溢出、栈下溢)

### 11.4.2 Prompt模板

#### 模板A: 实现新指令(功能实现)

```markdown
任务: 在CymbolStackVM中实现新的字节码指令

目标指令: IREM(整数取模)
指令格式: 32位固定编码,高8位为操作码,低24位为操作数
操作数: 无(仅从操作数栈弹出操作数)

行为规范:
1. 从操作数栈弹出两个int值: b = pop(), a = pop()
2. 检查除零: if (b == 0) throw VMDivisionByZeroException(PC-1, "IREM")
3. 计算结果: result = a % b
4. 将结果压入栈: push(result)

参考实现:
- 参照executeIDiv()方法的实现模式
- 参考"CymbolStackVM.java"第667-674行的IDIV指令
- 复用相同的除零检查逻辑

约束条件:
1. 必须在CymbolStackVM中添加executeIRem()方法
2. 必须在executeInstructionLegacy()的switch语句中添加case分支
3. 必须在BytecodeDefinition中添加INSTR_IREM常量
4. 异常信息必须包含PC位置和指令名称
5. 代码风格必须遵循AGENTS.md规范(注释、命名、异常处理)

测试用例:
输入字节码: [encodeInstruction(INSTR_ICONST, 17), 
            encodeInstruction(INSTR_ICONST, 5),
            encodeInstruction(INSTR_IREM),
            encodeInstruction(INSTR_HALT)]
预期输出: 2 (17 % 5 = 2)

期望输出格式:
1. executeIRem()方法的完整Java代码(带中文注释)
2. executeInstructionLegacy()中新增的case分支代码
3. BytecodeDefinition.INSTR_IREM常量定义
4. 对应的测试方法(JUnit 5 + AssertJ)

上下文文件:
- CymbolStackVM.java
- BytecodeDefinition.java
- CymbolStackVMTest.java
```

#### 模板B: 优化GC性能(优化实现)

```markdown
任务: 优化ReferenceCountingGC的内存分配性能

现状分析:
- 当前使用First-Fit算法(findFreeBlock),时间复杂度O(n)
- 空闲块列表用TreeMap存储,但查找仍需遍历所有块
- 频繁的小对象分配导致大量碎片

优化方向:
1. 实现"最佳适应"(Best-Fit)算法替代First-Fit
2. 添加内存块分级系统(如:小块<256B,中块<4KB,大块>4KB)
3. 实现"分裂合并"策略: 分配时分裂大块,回收时合并相邻块

实现要求:
1. 在ReferenceCountingGC中添加BlockSize枚举:
   enum BlockSize { SMALL, MEDIUM, LARGE }
2. 为每个BlockSize级别维护独立的TreeMap:
   private TreeMap<Integer, FreeBlock> smallBlocks;
   private TreeMap<Integer, FreeBlock> mediumBlocks;
   private TreeMap<Integer, FreeBlock> largeBlocks;
3. 重写allocate()方法:
   - 根据size选择合适的BlockSize级别
   - 在对应级别的TreeMap中查找"最佳"块(>=size且最小的)
   - 如果当前级别找不到,向上搜索其他级别
4. 重写addFreeBlock()方法:
   - 根据size将回收的块放入对应级别
   - 跨级别合并(如: 小块+小块合并成中块)
5. 保持向后兼容: 修改不影响现有测试

性能目标:
- 小对象分配速度提升2倍以上(通过JMH基准验证)
- 碎片率降低30%(通过GCStats.getFragmentationRate()测量)

参考上下文:
- 参考当前findFreeBlock()的实现(第125-132行)
- 参考addFreeBlock()的合并逻辑(第212-236行)
- 参考GCPerformanceBenchmark的性能测试方法

期望输出格式:
1. BlockSize枚举完整定义
2. 三个级别的TreeMap初始化代码
3. 优化后的allocate()方法(带详细中文注释)
4. 优化后的addFreeBlock()方法
5. 性能对比测试代码(JMH基准)
6. 迁移指南(如何从First-Fit切换到Best-Fit)
```

### 11.4.3 AI应该做/不该做

#### ✅ AI允许做的事情:

1. **实现明确界定的功能模块**
   - 添加新的字节码指令(如`IREM`, `FSQRT`, `POW`)
   - 实现新的GC算法(如标记-清除、复制收集器)
   - 添加调试支持(断点、单步执行、内存dump)

2. **生成测试用例和辅助代码**
   - 为新指令编写单元测试(覆盖边界情况)
   - 生成性能基准测试(JMH)
   - 创建集成测试(端到端执行流程)

3. **优化特定算法实现**
   - 优化GC的空闲块查找(从O(n)到O(log n))
   - 优化指令分发机制(从switch到策略模式)
   - 添加热点内联(高频指令的内联版本)

4. **生成代码注释和文档**
   - 为复杂算法添加详细注释(如空闲块合并逻辑)
   - 生成API文档(Javadoc)
   - 编写使用示例和最佳实践

5. **重构代码结构**
   - 将巨型方法拆分成小函数(如executeInstructionLegacy)
   - 提取重复代码为工具方法(如extractOperand, checkBounds)
   - 应用设计模式(策略模式、工厂模式)

#### ❌ AI禁止做的事情:

1. **大规模重构目录结构**
   - 不要重命名`stackvm/`目录为`engine/`
   - 不要将`gc/`包移动到`stackvm/gc/`
   - 保持包结构稳定,避免破坏下游依赖

2. **修改核心接口定义**
   - 不要修改`GarbageCollector`接口的方法签名
   - 不要删除`StackFrame`的公共方法
   - 接口变更必须经过Review,确保向后兼容

3. **删除测试用例或降低测试覆盖率**
   - 不要删除边界测试(除零、栈溢出)
   - 不要禁用性能基准测试
   - 修改代码后必须保证所有现有测试通过

4. **破坏现有EP模块边界**
   - 不要让EP18直接依赖EP21的代码
   - 不要在EP18中实现SSA优化(这是EP21的职责)
   - 每个EP的职责要清晰,避免跨模块耦合

5. **硬编码魔术数字**
   - 不要用`heapSize = 1048576`,应该用`VMConfig.getHeapSize()`
   - 不要用`INSTR_ICONST = 29`,应该用`BytecodeDefinition.INSTR_ICONST`
   - 所有配置项必须来自VMConfig,提高可配置性

### 11.4.4 验证与回滚策略

在接受AI的修改前,必须执行以下验证步骤:

#### 验证步骤清单:

**1. 编译验证**
```bash
cd ep18
mvn clean compile

# 检查点:
# - 无编译错误
# - 无警告(特别是unchecked cast, deprecation)
# - ANTLR4生成代码正常(target/generated-sources/)
```

**2. 单元测试验证**
```bash
# 运行所有测试
mvn test

# 运行特定测试套件
mvn test -Dtest=CymbolStackVMTest
mvn test -Dtest=GarbageCollectorTest
mvn test -Dtest=InstructionFormat32Test

# 检查点:
# - 所有测试通过(Failures: 0, Errors: 0)
# - 测试覆盖率未下降(jacoco-maven-plugin)
# - 无跳过的测试(Skipped: 0)
```

**3. 手工检查关键点**

**虚拟机功能检查**:
- [ ] 所有算术指令正确(IADD, ISUB, IMUL, IDIV)
- [ ] 所有比较指令正确(ILT, IGT, IEQ, INE)
- [ ] 函数调用返回正确(参数传递、返回值)
- [ ] 栈操作正确(push/pop/peek)

**GC功能检查**:
- [ ] 对象分配时引用计数=1
- [ ] 对象引用增加时引用计数递增
- [ ] 对象引用减少时引用计数递减
- [ ] 引用计数归零时对象被回收
- [ ] 空闲块自动合并(检查GC日志)

**错误处理检查**:
- [ ] 除零时抛出VMDivisionByZeroException
- [ ] 栈溢出时抛出VMStackOverflowException
- [ ] 内存越界时抛出VMMemoryAccessException
- [ ] 异常信息包含PC位置和指令名称

**4. 性能回归检查**
```bash
# 运行性能基准
mvn test -Dtest=GCPerformanceBenchmark
mvn test -Dtest=InstructionExecutionBenchmark

# 检查点:
# - 分配速度未降低(ops/s >= 基准值 * 0.9)
# - 指令执行速度未降低
# - GC暂停时间未增加
```

**5. 集成测试验证**
```bash
# 运行端到端测试
mvn test -Dtest=CymbolStackVMGCIntegrationTest

# 检查点:
# - 复杂程序执行正确
# - GC在真实场景中正常工作
# - 无内存泄漏(运行多次后堆使用稳定)
```

#### 回滚策略:

**场景1: 编译失败**
```bash
# 查看AI修改的文件
git status

# 暂存所有修改(保存AI的工作用于后续学习)
git stash save "AI attempt: add IREM instruction"

# 恢复到修改前状态
git stash pop --index  # 只恢复暂存区,保留工作区
# 或
git checkout -- .      # 完全恢复

# 仔细分析AI的代码,手动修复错误后重新提交
```

**场景2: 测试失败**
```bash
# 查看失败的测试
mvn test -Dtest=CymbolStackVMTest 2>&1 | grep -A 20 "FAILURE"

# 分析失败原因:
cat target/surefire-reports/org.teachfx.antlr4.ep18.CymbolStackVMTest.txt

# 选项1: 回退AI修改
git checkout -- ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/CymbolStackVM.java

# 选项2: 保存AI工作,手工修复
git stash
# 手动修复代码...
git stash pop
```

**场景3: 性能回归**
```bash
# 运行基准对比
git diff HEAD~1 ep18/src/main/java/org/teachfx/antlr4/ep18/gc/ReferenceCountingGC.java

# 如果性能下降超过20%,回滚:
git revert <commit-hash>
# 或
git reset --hard <commit-hash>

# 分析AI的优化思路,应用到性能测试后再提交
```

**快速恢复命令**:
```bash
# 1. 查看最近3次提交
git log --oneline -3

# 2. 回滚到指定提交
git reset --hard <commit-hash>

# 3. 或者创建新分支保存AI工作
git checkout -b save-ai-work
git add .
git commit -m "Save AI work for analysis"

# 4. 回到主分支继续
git checkout main
```

**保存AI修改用于学习**:
```bash
# 导出AI生成的代码
git diff HEAD > ai-changes.patch

# 或导出完整文件
git show HEAD:ep18/src/main/java/org/teachfx.antlr4/ep18/stackvm/CymbolStackVM.java > ai-version.java

# 分析AI的思路,提取有价值的部分
# 手动集成到代码中,确保所有测试通过
```

## 11.5 练习题

### 练习1: 实现浮点数指令(手工实现版)

**任务**: 为CymbolStackVM添加浮点数开方指令`FSQRT`

**要求**:
1. 在`BytecodeDefinition`中添加`INSTR_FSQRT = 43`
2. 在`CymbolStackVM`中实现`executeFsqrt()`方法:
   - 从栈顶弹出一个`float`值
   - 计算其平方根(`Math.sqrt()`)
   - 将结果压回栈
3. 在`executeInstructionLegacy()`中添加case分支
4. 编写测试用例验证:
   - `FSQRT 4.0`应返回`2.0`
   - `FSQRT 9.0`应返回`3.0`
   - `FSQRT 0.0`应返回`0.0`
   - `FSQRT -1.0`应返回`NaN`(检查`Float.isNaN()`)

**解题思路提示**:
- 参考`executeFadd()`的实现,学习如何处理浮点数操作
- 使用`Float.intBitsToFloat()`和`Float.floatToIntBits()`进行类型转换
- 注意栈操作顺序:`float a = pop(); push(sqrt(a));`

---

### 练习2: 实现条件跳转指令优化(AI协作版)

**任务**: 优化`BRT`和`BRF`指令,支持前向标签解析

**要求**:
1. 设计上下文:提供`ByteCodeAssembler.java`和测试用例
2. 编写Prompt:让AI实现前向引用的标签解析
3. 验证AI输出:
   - 标签在指令前定义也能正确跳转
   - 所有现有测试通过
4. 编写新测试用例验证:
   ```vm
   brt forward_label
   iconst 1  ; 这段代码应被跳过
   halt
   
   forward_label:
       iconst 2
       halt
   ```

**解题思路提示**:
- 参考`LabelSymbol`的`isForwardRef`字段
- 参考`getLabelAddress()`中的前向引用处理逻辑
- 使用`LabelSymbol.resolveForwardReferences(code)`解析标签

---

### 练习3: 优化GC的空闲块查找(手工实现版)

**任务**: 将`ReferenceCountingGC`的`findFreeBlock()`从O(n)优化到O(log n)

**要求**:
1. 当前`findFreeBlock()`使用`for`循环遍历所有空闲块
2. 优化为使用`TreeMap.ceilingEntry()`或`TreeMap.higherEntry()`
3. 验证优化效果:
   - 创建1万个小对象(每个16字节)
   - 释放一半
   - 重新分配1万个对象
   - 比较优化前后的时间(使用`System.nanoTime()`)

**解题思路提示**:
- `TreeMap`的键是偏移量,不是大小
- 可以维护一个按大小排序的辅助数据结构
- 或者在分配时按大小分段:小块(<256B)、中块(<4KB)、大块(>=4KB)

---

### 练习4: 实现结构体字段的边界检查(AI协作版)

**任务**: 为`FLOAD`和`FSTORE`指令添加字段偏移量边界检查

**要求**:
1. 设计上下文:提供`CymbolStackVM.java`的`executeFload()`和`executeFstore()`方法
2. 编写Prompt:让AI实现以下功能:
   - 检查`fieldOffset`是否在结构体字段数量范围内
   - 如果越界,抛出`VMMemoryAccessException`并提示字段数量
3. 验证AI输出:
   - 正常访问字段时无异常
   - 越界访问时抛出正确异常
   - 错误信息包含字段偏移量、结构体ID、字段数量

**解题思路提示**:
- 参考`executeLoad()`的边界检查实现
- 使用`StructValue.getFieldCount()`获取字段数量
- 异常信息示例:`"Field offset 5 out of bounds for struct with 3 fields"`

---

### 练习5: 实现调用栈深度限制(手工实现版)

**任务**: 为CymbolStackVM添加最大调用栈深度限制

**要求**:
1. 在`VMConfig`中添加`maxCallStackDepth`配置(默认值1000)
2. 在`executeCall()`中检查:
   ```java
   if (framePointer >= config.getMaxCallStackDepth()) {
       throw new VMStackOverflowException("Maximum call stack depth exceeded", 
                                       programCounter, "CALL");
   }
   ```
3. 编写测试用例:
   - 正常递归(深度<限制)应正常执行
   - 超过限制的递归应抛出`VMStackOverflowException`
4. 实现可配置:允许通过VMConfig调整限制值

**解题思路提示**:
- 参考`push()`中的栈溢出检查
- 使用`VMConfig.getMaxCallStackDepth()`获取限制
- 测试用例:递归斐波那契,深度超过1000时验证异常

---

### 练习6: 实现GC统计日志(AI协作版)

**任务**: 为`GCStats`添加详细的垃圾回收日志功能

**要求**:
1. 设计上下文:提供`GCStats.java`和`ReferenceCountingGC.java`
2. 编写Prompt:让AI实现以下功能:
   - 记录每次GC的详细信息(时间戳、对象数、回收内存、耗时)
   - 提供日志输出方法(格式化输出,支持JSON和文本)
   - 集成到`ReferenceCountingGC.collect()`中
3. 验证AI输出:
   - 日志信息完整且准确
   - 不影响GC性能
   - 支持通过VMConfig控制日志级别(DEBUG, INFO, OFF)

**解题思路提示**:
- 使用`java.time.Instant`记录时间戳
- 使用`String.format()`或`StringBuilder`构建日志
- 考虑使用Log4j2日志框架(已有依赖)
- JSON输出可使用`Jackson`或`Gson`(需添加依赖)

## 11.6 本章小结与下一章预告

### 本章关键收获

**技术能力**:
1. **虚拟机架构设计**: 理解了取指-译码-执行循环、栈式执行模型、函数调用ABI
2. **字节码设计与编码**: 掌握了32位统一指令格式、大端序编码、操作数符号扩展
3. **垃圾回收实现**: 学会了引用计数算法、空闲链表管理、内存碎片处理
4. **内存管理**: 理解了栈帧管理、操作数栈、堆内存、全局变量访问

**工程实践**:
1. **测试驱动开发**: 学会了如何为虚拟机编写单元测试、集成测试、性能基准
2. **错误处理**: 掌握了异常分类(除零、溢出、越界)和调试支持(断点、单步、trace)
3. **配置管理**: 理解了如何设计灵活的配置系统(VMConfig.Builder模式)
4. **性能优化**: 学会了如何分析和优化虚拟机性能(JMH基准、热点分析)

**AI协作能力**:
1. **上下文设计**: 学会了如何组织源码、测试、文档提供给AI
2. **Prompt工程**: 掌握了功能实现、优化、测试生成的Prompt模板
3. **验证策略**: 理解了编译验证、测试验证、性能回归检查的完整流程
4. **回滚机制**: 学会了如何保存AI工作、手工修复、Git回滚

### 这些收获将在下一章如何被使用

**EP19(IR生成)将使用**:
- **字节码格式**: EP19的三地址码将转换为EP18的字节码
- **指令语义**: IR的运算符(ADD, SUB, MUL, DIV)映射到字节码指令
- **函数调用**: IR的CALL/RET节点将生成EP18的CALL/RET指令

**EP20(CFG优化)将使用**:
- **基本块划分**: CFG的基本块对应字节码的连续指令序列
- **控制流优化**: 基本块的分支跳转优化直接生成BR/BRT/BRF指令
- **寄存器分配**: 临时变量映射到LOAD/STORE指令的局部变量索引

**EP21(SSA与TRO)将使用**:
- **SSA变量**: φ函数将转换为局部变量赋值和加载
- **尾递归优化**: 尾调用将转换为JMP指令(避免创建新栈帧)
- **数据流分析**: 活变量分析指导局部变量的生命周期管理

### 你现在站在哪

**编译器流水线位置**:
```
词法分析 → 语法分析 → 语义分析 → AST构建 → 类型检查
    ↓
符号表解析 → IR生成(三地址码) ← 你在这里(EP18)
    ↓
控制流图构建 → 数据流分析 → 优化Pass(SSA, TRO等)
    ↓
代码生成 → 字节码汇编 → 虚拟机执行 ← 你在这里
```

**核心组件掌握度**:
- ✅ 虚拟机引擎(CymbolStackVM): 完全理解
- ✅ 字节码汇编(ByteCodeAssembler): 完全理解
- ✅ 垃圾回收(ReferenceCountingGC): 完全理解
- ✅ 栈帧管理(StackFrame): 完全理解
- ✅ 异常处理(VMException体系): 完全理解

### 下一章预告

**第12章: 中间表示(IR)生成与优化**

你将学习:
1. **三地址码设计**: 如何将AST转换为线性化的中间表示
2. **SSA(静态单赋值)形式**: 如何消除中间变量的歧义
3. **IR优化Pass**: 常量折叠、死代码消除、公共子表达式消除
4. **IR到字节码的转换**: 如何将优化后的IR生成可执行字节码

**与本章的衔接**:
- EP18的字节码指令将成为EP19 IR生成的目标指令
- EP18的函数调用ABI将指导EP19的CALL/RET节点生成
- EP18的内存布局(堆、栈、全局变量)将约束EP19的地址分配

**准备好了吗?** 下一章将带你进入编译器优化的核心领域,将AST转换为更高效、更易优化的中间表示!

<task_metadata>
session_id: ses_44d447749ffe9pq5BaqqYWhSdT
</task_metadata>