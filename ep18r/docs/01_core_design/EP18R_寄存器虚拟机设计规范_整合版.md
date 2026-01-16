# EP18R 寄存器虚拟机核心设计文档

## 概述

EP18R是基于寄存器架构的独立虚拟机模块，采用寄存器指令集和优化的执行模型，专注于寄存器架构的性能优势。它是EP18栈式虚拟机的寄存器版本实现，使用16个通用寄存器（r0-r15）和42条寄存器指令。

## 目录
1. [虚拟机架构设计](#1-虚拟机架构设计)
2. [寄存器指令集规范](#2-寄存器指令集规范)
3. [执行引擎设计](#3-执行引擎设计)
4. [寄存器分配策略](#4-寄存器分配策略)
5. [汇编器与反汇编器](#5-汇编器与反汇编器)
6. [内存管理与垃圾回收](#6-内存管理与垃圾回收)
7. [Struct统一设计](#7-struct统一设计)

---

## 1. 虚拟机架构设计

### 1.1 整体架构

EP18R采用寄存器架构，核心组件包括：

```
RegisterVMInterpreter (主控制器)
├── RegisterBytecodeDefinition (指令集定义)
│   ├── 42条寄存器指令 (操作码1-42)
│   └── 三种指令格式 (R/I/J类型)
├── RegisterByteCodeAssembler (汇编器)
│   ├── 前向引用处理
│   └── 指令编码生成
├── RegisterDisAssembler (反汇编器)
├── StackFrame (栈帧管理)
│   └── 调用栈支持 (最多1024层嵌套)
└── LabelSymbol (标签符号表)
```

### 1.2 寄存器架构

#### 寄存器定义
- **r0**: 零寄存器（恒为0）
- **r1-r12**: 通用目的寄存器
- **r13**: SP (栈指针)
- **r14**: FP (帧指针)
- **r15**: LR (链接寄存器，存储返回地址)

#### 寄存器文件特性
- **位宽**: 32位整数/浮点统一寄存器
- **数量**: 16个物理寄存器
- **寻址**: 5位寄存器编号（0-15）
- **特殊寄存器**: r0硬连线到0，r13-r15有特殊用途

### 1.3 指令格式

所有指令均为32位固定长度，支持三种格式：

#### R类型（寄存器-寄存器运算）
```
 31        26 25     21 20     16 15     11 10         0
┌────────────┬─────────┬─────────┬─────────┬─────────────┐
│   opcode   │    rd   │   rs1   │   rs2   │   unused    │
│    (6)     │   (5)   │   (5)   │   (5)   │    (11)     │
└────────────┴─────────┴─────────┴─────────┴─────────────┘
```
- **用途**: 算术运算、逻辑运算、比较运算
- **示例**: `add r3, r1, r2` (r3 = r1 + r2)

#### I类型（立即数/内存访问）
```
 31        26 25     21 20     16 15                    0
┌────────────┬─────────┬─────────┬───────────────────────┐
│   opcode   │    rd   │   rs1   │      immediate        │
│    (6)     │   (5)   │   (5)   │         (16)          │
└────────────┴─────────┴─────────┴───────────────────────┘
```
- **用途**: 立即数加载、内存访问、条件跳转
- **示例**: `li r1, 100` (r1 = 100), `lw r2, r14, 8` (r2 = memory[FP+8])

#### J类型（跳转指令）
```
 31        26 25                                         0
┌────────────┬───────────────────────────────────────────┐
│   opcode   │               address                     │
│    (6)     │                  (26)                     │
└────────────┴───────────────────────────────────────────┘
```
- **用途**: 无条件跳转、函数返回
- **示例**: `j 0x1000` (跳转到地址0x1000), `ret` (从调用栈返回)

### 1.4 函数调用机制

EP18R采用**调用栈**机制替代简单的链接寄存器，支持任意深度嵌套调用：

1. **调用者**将参数存入寄存器r1-rn
2. 执行`call target`指令：
   - 将返回地址压入**调用栈**（数组结构，支持1024层嵌套）
   - 同时保存到LR (r15)以保持向后兼容
   - 跳转到目标函数地址
3. **被调用者**：
   - 执行函数体
   - 函数执行结果存入r1
   - 执行`ret`指令，从调用栈弹出返回地址并跳转
4. 支持任意深度的函数嵌套调用

#### 返回地址管理
- **调用栈**: `callStack[0..1023]`数组，每个元素包含返回地址
- **栈指针**: `framePointer`指向当前栈帧，初始为-1
- **兼容性**: 同时维护LR寄存器，兼容仅使用r15的代码

---

## 2. 寄存器指令集规范

### 2.1 操作码分配表

| 操作码 | 指令 | 格式 | 操作数类型 | 语义描述 |
|--------|------|------|------------|----------|
| 1 | add | R | rd, rs1, rs2 | 整数加法：rd = rs1 + rs2 |
| 2 | sub | R | rd, rs1, rs2 | 整数减法：rd = rs1 - rs2 |
| 3 | mul | R | rd, rs1, rs2 | 整数乘法：rd = rs1 * rs2 |
| 4 | div | R | rd, rs1, rs2 | 整数除法：rd = rs1 / rs2 |
| 5 | slt | R | rd, rs1, rs2 | 小于设置：rd = (rs1 < rs2) ? 1 : 0 |
| 6 | sle | R | rd, rs1, rs2 | 小于等于设置：rd = (rs1 ≤ rs2) ? 1 : 0 |
| 7 | sgt | R | rd, rs1, rs2 | 大于设置：rd = (rs1 > rs2) ? 1 : 0 |
| 8 | sge | R | rd, rs1, rs2 | 大于等于设置：rd = (rs1 ≥ rs2) ? 1 : 0 |
| 9 | seq | R | rd, rs1, rs2 | 等于设置：rd = (rs1 == rs2) ? 1 : 0 |
| 10 | sne | R | rd, rs1, rs2 | 不等于设置：rd = (rs1 != rs2) ? 1 : 0 |
| 11 | neg | R | rd, rs1 | 整数取负：rd = -rs1 |
| 12 | not | R | rd, rs1 | 按位取反：rd = ~rs1 |
| 13 | and | R | rd, rs1, rs2 | 按位与：rd = rs1 & rs2 |
| 14 | or | R | rd, rs1, rs2 | 按位或：rd = rs1 \| rs2 |
| 15 | xor | R | rd, rs1, rs2 | 按位异或：rd = rs1 ^ rs2 |
| 16 | fadd | R | rd, rs1, rs2 | 浮点加法：rd = rs1 + rs2 |
| 17 | fsub | R | rd, rs1, rs2 | 浮点减法：rd = rs1 - rs2 |
| 18 | fmul | R | rd, rs1, rs2 | 浮点乘法：rd = rs1 * rs2 |
| 19 | fdiv | R | rd, rs1, rs2 | 浮点除法：rd = rs1 / rs2 |
| 20 | flt | R | rd, rs1, rs2 | 浮点小于：rd = (rs1 < rs2) ? 1 : 0 |
| 21 | feq | R | rd, rs1, rs2 | 浮点等于：rd = (rs1 == rs2) ? 1 : 0 |
| 22 | itof | R | rd, rs1 | 整数转浮点：rd = (float)rs1 |
| 23 | call | J | immediate | 函数调用：压栈返回地址；PC = target |
| 24 | ret | J | - | 函数返回：从调用栈弹出返回地址；PC = 返回地址 |
| 25 | j | J | immediate | 无条件跳转：PC = target |
| 26 | jt | I | rs1, immediate | 条件为真跳转：if (rs1 != 0) PC = target |
| 27 | jf | I | rs1, immediate | 条件为假跳转：if (rs1 == 0) PC = target |
| 28 | li | I | rd, immediate | 加载整数立即数：rd = immediate |
| 29 | lc | I | rd, immediate | 加载字符立即数：rd = immediate |
| 30 | lf | I | rd, pool_index | 加载浮点常量：rd = pool[pool_index] |
| 31 | ls | I | rd, pool_index | 加载字符串常量：rd = pool[pool_index] |
| 32 | lw | I | rd, base, offset | 加载字：rd = memory[base + offset] |
| 33 | sw | I | rs, base, offset | 存储字：memory[base + offset] = rs |
| 34 | lw_g | I | rd, offset | 全局加载：rd = memory[GBASE + offset] |
| 35 | sw_g | I | rs, offset | 全局存储：memory[GBASE + offset] = rs |
| 36 | lw_f | I | rd, offset | 字段加载：rd = memory[obj_ptr + offset] |
| 37 | sw_f | I | rs, offset | 字段存储：memory[obj_ptr + offset] = rs |
| 38 | print | I | rs | 打印寄存器值：print(rs) |
| 39 | struct | I | rd, size | 分配结构体：rd = allocate_struct(size) |
| 40 | null | I | rd | 加载空指针：rd = NULL |
| 41 | mov | R | rd, rs1 | 寄存器移动：rd = rs1 |
| 42 | halt | J | - | 停止执行 |

### 2.2 重要修复记录

#### 条件跳转指令编码修复（2025-12-16）
**问题**: jt/jf指令的条件寄存器错误地放入rd字段（bits 25-21），但解释器从rs1字段（bits 20-16）读取。

**修复**: 在`RegisterByteCodeAssembler.addOperand()`中添加特殊处理：
```java
boolean isConditionalJump = currentInstruction.equals("jt") || currentInstruction.equals("jf");
if (isConditionalJump && currentOperandIndex == 0) {
    // jt/jf特殊处理: 第一个操作数是条件寄存器，放入rs1字段
    currentInstructionWord |= (value & 0x1F) << 16;  // rs1字段
}
```

#### 前向引用处理增强
**问题**: 汇编器需要处理前向引用（标签或函数在引用时尚未定义）。

**解决方案**:
- 在`LabelSymbol`中记录前向引用及指令类型（I类型/J类型）
- I类型：修补低16位立即数字段（bits 15-0）
- J类型：修补低26位地址字段（bits 25-0）
- 函数定义时自动创建同名标签，解析所有前向引用

---

## 3. 执行引擎设计

### 3.1 核心组件

- **RegisterVMInterpreter**: 虚拟机主类，协调各组件，管理执行状态
  - 负责取指、解码、执行循环
  - 管理调用栈 (`callStack[0..1023]`)
  - 管理程序计数器和执行步骤
  - 包含 `StackFrame[] callStack` 字段和 `int framePointer`

### 3.2 执行循环

```java
while (running) {
    // 1. 检查调试器（断点、单步）
    checkDebugger();

    // 2. 取指
    int instruction = fetchInstruction(programCounter);

    // 3. 解码
    InstructionInfo info = decodeInstruction(instruction);

    // 4. 执行前检查（权限、资源）
    if (!preExecuteCheck(info)) {
        handleExecutionError(info);
        continue;
    }

    // 5. 执行指令
    executeInstruction(info);

    // 6. 更新程序计数器（除非跳转指令已修改）
    updateProgramCounter(info);

    // 7. 更新统计信息
    updateStatistics(info);

    // 8. 检查停止条件
    if (shouldHalt()) {
        running = false;
    }
}
```

### 3.3 指令处理流水线

#### 阶段1：取指 (Fetch)
- 从指令缓存读取32位指令
- 指令缓存大小可配置（默认1024条指令）
- 支持指令预取，减少内存访问延迟

#### 阶段2：解码 (Decode)
- 解析操作码（bits[31:26]）
- 根据指令格式解析操作数
  - R类型：rd, rs1, rs2
  - I类型：rd, rs1, immediate
  - J类型：address
- 符号扩展立即数（16位→32位）

#### 阶段3：执行 (Execute)
- **算术运算**: 访问寄存器文件，执行ALU操作
- **内存访问**: 计算有效地址，加载/存储数据
- **控制流**: 更新程序计数器，处理跳转
- **特殊操作**: 系统调用、调试操作

#### 阶段4：写回 (Writeback)
- 将结果写回目标寄存器
- 更新条件标志寄存器（如有）
- 提交内存写入（如有）

### 3.4 程序计数器管理

#### 跳转标志机制
```java
private boolean didJump = false;

// 在cpu()循环中：
if (!didJump) {
    programCounter += 4;  // 只有在没有跳转的情况下才自动增加PC
}
didJump = false; // 重置跳转标志
```

#### 循环检测机制
```java
// 循环检测和安全机制
private static final int MAX_EXECUTION_STEPS = 1000000; // 最大执行步数
private int executionSteps = 0;

// 在cpu()循环中：
if (executionSteps++ > MAX_EXECUTION_STEPS) {
    throw new RuntimeException("Maximum execution steps exceeded. Possible infinite loop detected at PC=" + programCounter);
}
```

### 3.5 异常处理机制

#### 异常类型
| 异常码 | 异常名称 | 触发条件 |
|--------|----------|----------|
| 1 | 非法操作码 | 操作码不在1-42范围内 |
| 2 | 寄存器越界 | 寄存器编号不在0-15范围内 |
| 3 | 内存越界 | 访问地址超出有效内存范围 |
| 4 | 除零错误 | 整数除法除数为0 |
| 5 | 栈溢出 | 栈指针超出栈边界 |
| 6 | 栈下溢 | 栈指针低于栈底 |
| 7 | 空指针访问 | 访问空指针指向的内存 |

#### 异常处理流程
1. **异常检测**: 在执行阶段检测异常条件
2. **异常触发**: 设置异常码和异常PC
3. **上下文保存**: 保存当前寄存器状态和栈帧
4. **异常分发**: 根据异常码调用对应处理例程
5. **恢复或终止**: 尝试恢复执行或终止虚拟机

---

## 4. 寄存器分配策略

### 4.1 简单固定分配算法

#### 算法概述
简单固定分配是一种贪心算法，按变量声明顺序线性分配寄存器，当寄存器不足时报告错误。这是寄存器分配的最简形式，适合教育目的和初始实现。

#### 分配规则
1. **局部变量分配**: 按声明顺序分配r1, r2, r3, ...
2. **临时值分配**: 从下一个可用寄存器开始分配
3. **参数接收**: 前4个参数存入r1-r4，超出部分通过栈传递
4. **返回值分配**: 返回值始终使用r1

#### 算法流程
```java
public class SimpleRegisterAllocator {
    private int nextReg = 1;  // 下一个可用寄存器 (r1开始)
    private Map<String, Integer> varToReg = new HashMap<>();
    private Map<String, Integer> varToStackOffset = new HashMap<>();
    private int stackOffset = 0;

    // 为局部变量分配寄存器
    public int allocateLocal(String varName) {
        if (nextReg <= 12) {  // r1-r12可用
            int reg = nextReg++;
            varToReg.put(varName, reg);
            return reg;
        } else {
            // 寄存器不足，溢出到栈
            stackOffset -= 4;  // 每个变量4字节
            varToStackOffset.put(varName, stackOffset);
            return -1;  // 表示栈分配
        }
    }
}
```

### 4.2 调用约定

EP18R采用仿照RISC-V风格的应用程序二进制接口（ABI）规范。完整的ABI规范参见[EP18R_ABI_设计文档.md](EP18R_ABI_设计文档.md)。

#### 目标ABI规范

##### 参数传递规则
1. **前6个参数**: 通过寄存器a0-a5 (r2-r7)传递
2. **第7+个参数**: 通过栈传递，偏移量 `fp + 16 + 4*(n-7)`
3. **结构体参数**: 通过栈传递（传递指针）

##### 返回值规则
1. **基本类型**: 通过寄存器a0 (r2)返回
2. **结构体**: 通过栈返回（调用者预留空间，传递指针）

##### 寄存器保存责任

**调用者保存寄存器 (Caller-saved)**:
- **寄存器**: ra (r1), a0-a5 (r2-r7), lr (r15)
- **责任**: 调用者在调用前保存这些寄存器的值（如果需要）
- **特点**: 被调用者可以自由使用，无需保存/恢复
- **用途**: 传递参数、存储临时计算结果

**被调用者保存寄存器 (Callee-saved)**:
- **寄存器**: s0-s4 (r8-r12), sp (r13), fp (r14)
- **责任**: 被调用者如果使用这些寄存器，必须在函数入口保存，在函数出口恢复
- **特点**: 值在函数调用间保持不变
- **用途**: 存储需要在函数调用间保持的局部变量、栈和帧指针

#### 当前实现差异

当前实现与目标ABI规范存在一些差异以保持向后兼容性：

1. **返回值寄存器**: 使用r1而非a0 (r2)作为返回值寄存器
2. **参数传递**: 支持前6个参数通过r2-r7传递（与规范一致）
3. **寄存器保存**: CALL指令自动保存r2-r7，RET指令恢复r2-r7
4. **栈帧布局**: 使用简化的栈帧结构，尚未实现标准栈帧布局

详细差异参见ABI文档的[附录D：当前实现与ABI规范的差异](EP18R_ABI_设计文档.md#附录d当前实现与abi规范的差异)。

### 4.3 寄存器溢出处理

#### 溢出触发条件
1. **寄存器不足**: 局部变量数量超过可用通用寄存器数量（12个）
2. **大结构体**: 结构体变量无法放入单个寄存器
3. **复杂表达式**: 表达式求值需要大量临时寄存器

#### 溢出策略

##### 策略1：栈溢出 (Stack Spilling)
- 将寄存器值保存到栈内存
- 需要时从栈内存重新加载
- 适用于不频繁访问的变量

##### 策略2：内存变量 (Memory Variables)
- 直接将变量分配在栈上，不占用寄存器
- 通过FP相对寻址访问
- 适用于大数组或结构体

#### 溢出实现
```java
public class SpillManager {
    // 将寄存器溢出到栈
    public void spillRegister(int reg, int stackOffset) {
        // 生成存储指令: sw reg, fp, offset
        emitStore(reg, RegisterFile.FP, stackOffset);
    }

    // 从栈加载到寄存器
    public void reloadRegister(int reg, int stackOffset) {
        // 生成加载指令: lw reg, fp, offset
        emitLoad(reg, RegisterFile.FP, stackOffset);
    }

    // 分配栈槽位
    public int allocateStackSlot() {
        currentOffset -= 4;  // 每个槽位4字节
        return currentOffset;
    }
}
```

---

## 5. 汇编器与反汇编器

### 5.1 汇编器设计

#### 整体架构
```
汇编代码 (.asm文件)
    ↓
汇编器 (RegisterByteCodeAssembler) - 集成式实现
    ├── 词法分析 (ANTLR4 VMAssemblerLexer)
    ├── 语法分析 (ANTLR4 VMAssemblerParser)
    ├── 语义检查（集成在汇编器内）
    ├── 代码生成（集成在汇编器内）
    └── 错误报告（基础错误检查）
    ↓
寄存器字节码 (.vm文件)
```

#### 汇编语言语法

##### 语法文件结构
```antlr4
grammar VMAssembler;

program
    :   (globalVariable | globals)?
        (functionDeclaration | instr | label | NEWLINE)+
    ;

// 全局变量声明
globalVariable : '.global' type=ID name=ID NEWLINE;

// 全局数据空间声明
globals : NEWLINE* '.globals' intVal=INT NEWLINE;

// 函数声明
functionDeclaration
    :   '.def' name=ID ':' 'args' '=' a=INT ',' 'locals' '=' lo=INT NEWLINE
    ;

// 指令
instr
    :   op=ID NEWLINE
    |   op=ID a=temp NEWLINE
    |   op=ID a=temp ',' b=temp NEWLINE
    |   op=ID a=temp ',' b=temp ',' c=temp NEWLINE
    ;
```

##### 汇编程序示例
```assembly
# 简单寄存器汇编程序示例
.globals 1024

.def main: args=0, locals=2
    li r1, 10          # 加载立即数10到r1
    li r2, 20          # 加载立即数20到r2
    add r3, r1, r2     # r3 = r1 + r2
    print r3           # 打印r3的值
    halt               # 停止执行

.def add_func: args=2, locals=0
    add r1, r1, r2     # r1 = r1 + r2 (参数在r1, r2)
    ret                # 返回，结果在r1
```

### 5.2 反汇编器设计

#### 32位指令格式反汇编

反汇编器必须按照设计规范的32位指令格式进行解码：

**指令格式定义**：
- 所有指令均为32位（4字节）固定长度
- 操作码位于 bits 31-26（6位）
- 根据指令类型，操作数字段分布在不同位置

#### 反汇编流程

1. **读取指令字**: 从字节码中读取4字节（大端序）组成32位指令字
2. **提取操作码**: 从指令字的 bits 31-26 提取操作码（`instructionWord >> 26 & 0x3F`）
3. **指令识别**: 根据操作码查找指令定义
4. **操作数解码**: 根据指令格式（R/I/J）从指令字中提取操作数字段
5. **文本生成**: 格式化输出指令名称和操作数

#### 核心类设计

```java
public class RegisterDisAssembler {
    protected byte[] code;
    protected int codeSize;
    protected Object[] constPool;
    protected RegisterBytecodeDefinition.Instruction[] instructions;

    public RegisterDisAssembler(byte[] code, int codeSize, Object[] constPool) {
        this.code = code;
        this.codeSize = codeSize;
        this.constPool = constPool;
        this.instructions = RegisterBytecodeDefinition.instructions;
    }

    // 反汇编单条指令
    public int disassembleInstruction(int ip) {
        // 1. 读取完整的32位指令字（大端序）
        int instructionWord = ((code[ip] & 0xFF) << 24) |
                          ((code[ip + 1] & 0xFF) << 16) |
                          ((code[ip + 2] & 0xFF) << 8) |
                          (code[ip + 3] & 0xFF);
        ip += 4;

        // 2. 从指令字中提取操作码（bits 31-26）
        int opcode = (instructionWord >> 26) & 0x3F;

        // 3. 获取指令定义
        RegisterBytecodeDefinition.Instruction instr = instructions[opcode];

        // 4. 根据指令格式解码操作数
        int rd = 0, rs1 = 0, rs2 = 0, imm = 0;
        switch (instr.getFormat()) {
            case FORMAT_R:
                rd = (instructionWord >> 21) & 0x1F;
                rs1 = (instructionWord >> 16) & 0x1F;
                rs2 = (instructionWord >> 11) & 0x1F;
                break;
            case FORMAT_I:
                rd = (instructionWord >> 21) & 0x1F;
                rs1 = (instructionWord >> 16) & 0x1F;
                imm = instructionWord & 0xFFFF;
                break;
            case FORMAT_J:
                imm = instructionWord & 0x3FFFFFF;
                break;
        }

        // 5. 格式化输出
        // ...
    }
}
```

#### 关键修复记录

**修复日期**: 2026-01-16
**问题**: 反汇编器使用错误的解码方式
- **错误实现**: 将32位指令拆分为"1字节操作码 + 4字节操作数"（总共5字节）
- **正确实现**: 将32位指令作为一个整体，从bits 31-26提取操作码

**修复前后对比**:

| 方面 | 修复前（错误） | 修复后（正确） |
|------|----------------|----------------|
| 读取方式 | 先读1字节opcode，再读4字节operand | 一次性读取4字节instructionWord |
| 操作码提取 | `code[ip] & 0xFF` | `(instructionWord >> 26) & 0x3F` |
| 总字节数 | 5字节 | 4字节 |
| 指令对齐 | 不对齐，导致反汇编错误 | 正确对齐 |
| 测试结果 | 测试失败（opcode解析错误） | 测试通过（正确识别指令） |

#### 指令格式解码示例

**R类型示例** (`add r3, r1, r2`):
- 指令字: `0x04611000`
- opcode: `(0x04611000 >> 26) & 0x3F = 1` (add)
- rd: `(0x04611000 >> 21) & 0x1F = 3` (r3)
- rs1: `(0x04611000 >> 16) & 0x1F = 1` (r1)
- rs2: `(0x04611000 >> 11) & 0x1F = 2` (r2)
- 反汇编输出: `add r3, r1, r2`

**I类型示例** (`li r1, 100`):
- 指令字: `0x01C00064`
- opcode: `(0x01C00064 >> 26) & 0x3F = 28` (li)
- rd: `(0x01C00064 >> 21) & 0x1F = 1` (r1)
- imm: `0x01C00064 & 0xFFFF = 100`
- 反汇编输出: `li r1, 100`

**J类型示例** (`j 0x1000`):
- 指令字: `0x04001000`
- opcode: `(0x04001000 >> 26) & 0x3F = 25` (j)
- imm: `0x04001000 & 0x3FFFFFF = 0x1000`
- 反汇编输出: `j 4096`

#### 指令解码器
```java
public class InstructionDecoder {
    // 解码单个指令
    public String decode(Instruction instruction) {
        int opcode = instruction.getOpcode();
        InstructionInfo info = RegisterBytecodeDefinition.getInstruction(opcode);

        if (info == null) {
            return String.format(".word 0x%08x  # 未知指令",
                instruction.getEncoding());
        }

        // 根据指令格式解码操作数
        switch (info.getFormat()) {
            case FORMAT_R:
                return decodeRType(instruction, info);
            case FORMAT_I:
                return decodeIType(instruction, info);
            case FORMAT_J:
                return decodeJType(instruction, info);
            default:
                return info.getName();
        }
    }
}
```

---

## 6. 内存管理与垃圾回收

### 6.1 内存布局

```
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

### 6.2 栈帧结构

```
┌─────────────────┐ 高地址
│   调用者帧      │
├─────────────────┤
│   返回地址      │ ← FP+8
├─────────────────┤
│   旧FP          │ ← FP+4
├─────────────────┤
│   局部变量n     │ ← FP-4*(n+1)
│   ...           │
│   局部变量1     │ ← FP-8
├─────────────────┤
│   参数m         │ ← FP+12+4*(m-1)
│   ...           │
│   参数1         │ ← FP+12
├─────────────────┤
│   临时空间      │
└─────────────────┘ 低地址
```

### 6.3 垃圾回收架构

#### 总体架构
```
GarbageCollector (接口)
├── ReferenceCountingGC (引用计数GC)
├── MarkSweepGC (标记-清除GC)
├── IncrementalGC (增量GC)
├── ConcurrentGC (并发GC)
└── GenerationalGC (分代GC)
```

#### 核心接口设计

##### GC对象接口
```java
public interface GCObject {
    // 引用计数管理
    int getRefCount();
    void incrementRef();
    void decrementRef();

    // 标记支持
    boolean isMarked();
    void setMarked(boolean marked);

    // 清理回调
    void onGC();
}
```

##### 垃圾回收器接口
```java
public interface GarbageCollector {
    // 对象分配
    int allocate(int size);
    int allocateObject(ObjectData data);

    // 引用管理
    void incrementRef(int objectId);
    void decrementRef(int objectId);

    // 垃圾回收
    void collect();
    void collect(GCTypes.CollectionType type);

    // 统计信息
    GCStatistics getStatistics();
    GCHeapInfo getHeapInfo();

    // 配置
    void setConfig(GCConfig config);
    GCConfig getConfig();
}
```

### 6.4 引用计数GC实现

#### 核心设计
```java
public class ReferenceCountingGC implements GarbageCollector {
    // 堆管理
    private final byte[] heap;
    private int heapSize;
    private int heapUsed;

    // 对象管理
    private final Map<Integer, GCObjectHeader> objects;
    private final AtomicInteger nextObjectId;
    private final Queue<Integer> freeObjectIds;

    // 循环引用检测
    private final CycleDetector cycleDetector;
    private final Set<Integer> markedForCollection;

    // 统计信息
    private final GCStatistics statistics;
}
```

#### 对象头结构
```java
public class GCObjectHeader {
    // 基本信息
    public final int objectId;
    public final int size;
    public final GCObjectType type;

    // 引用计数
    public volatile int refCount;

    // 标记信息
    public volatile boolean marked;
    public volatile boolean inCycle;

    // 链接信息
    public GCObjectHeader next;
    public int forwardAddress;
}
```

#### 引用计数管理
```java
@Override
public void incrementRef(int objectId) {
    GCObjectHeader header = objects.get(objectId);
    if (header != null) {
        int oldCount = header.refCount;
        header.refCount++;

        statistics.recordRefIncrement(oldCount, header.refCount);

        // 检查是否从0变为非0（复活对象）
        if (oldCount == 0 && header.refCount > 0) {
            unmarkObject(objectId);
        }
    }
}

@Override
public void decrementRef(int objectId) {
    GCObjectHeader header = objects.get(objectId);
    if (header != null) {
        int oldCount = header.refCount;
        header.refCount--;

        statistics.recordRefDecrement(oldCount, header.refCount);

        // 检查引用计数是否变为0
        if (header.refCount == 0) {
            // 对象可能需要被回收
            if (!header.marked) {
                collectObject(objectId);
            }
        }
    }
}
```

### 6.5 标记-清除GC实现

#### 标记阶段
```java
// 启动标记过程
public void mark() {
    // 1. 重置标记
    markedObjects.clear();

    // 2. 标记根对象（栈、寄存器、全局变量等）
    markRoots();

    // 3. 深度优先标记
    while (!markStack.isEmpty()) {
        int objectId = markStack.poll();
        markObject(objectId);
    }

    statistics.recordMarkPhase();
}

// 标记单个对象
private void markObject(int objectId) {
    if (markedObjects.get(objectId)) {
        return; // 已经标记过
    }

    GCObjectHeader header = getObjectHeader(objectId);
    if (header == null) {
        return; // 对象不存在
    }

    // 1. 标记对象
    markedObjects.set(objectId);

    // 2. 标记对象的所有引用
    Set<Integer> references = getObjectReferences(objectId);
    for (int refId : references) {
        if (!markedObjects.get(refId)) {
            markStack.push(refId);
        }
    }

    statistics.recordObjectMarked();
}
```

---

## 7. Struct统一设计

### 7.1 问题背景

EP18模块存在两个不同的struct实现：
1. `CymbolStackVM` - 使用int[]堆存储struct，每个字段一个整数槽位
2. `VMInterpreter` - 使用`StructSpace`对象（Object[] fields）存储struct

### 7.2 统一设计目标

1. **统一内存表示**: 建立一致的struct内存模型
2. **保持向后兼容**: 现有测试必须全部通过
3. **增强类型安全**: 添加字段类型信息
4. **支持嵌套struct**: 允许struct包含其他struct
5. **实现字段映射**: 支持字段名到偏移的映射

### 7.3 技术方案

采用**统一运行时表示**模式，创建`StructValue`作为ep18中结构体的唯一运行时表示：

```
统一结构体运行时表示（StructValue）
        │
        ├── CymbolStackVM适配层（结构体ID映射）
        │       └── 保持int[]栈和堆，内部使用StructValue表
        │
        └── VMInterpreter适配层（直接对象引用）
                └── 保持Object[]操作数栈，直接使用StructValue
```

### 7.4 StructValue设计

```java
public class StructValue {
    private final Object[] fields;
    private StructType type;  // 初始可为null，阶段2填充类型信息

    public StructValue(int fieldCount) {
        this.fields = new Object[fieldCount];
        this.type = null;
    }

    public StructValue(StructType type) {
        this.type = type;
        this.fields = new Object[type.getFieldCount()];
    }

    // 基于偏移量的访问（保持兼容性）
    public Object getField(int offset) {
        return fields[offset];
    }

    public void setField(int offset, Object value) {
        fields[offset] = value;
    }

    // 基于字段名的访问（未来扩展）
    public Object getField(String name) {
        if (type == null) throw new IllegalStateException("No type information");
        Integer offset = type.getFieldOffset(name);
        return fields[offset];
    }
}
```

### 7.5 实现步骤

#### 阶段1：统一结构体表示（3-4天）
**目标**: 创建StructValue作为统一运行时表示

1. **创建StructValue类**
2. **修改VMInterpreter使用StructValue**
3. **修改CymbolStackVM适配StructValue**
4. **统一测试验证**

#### 阶段2：集成ep20类型系统（2-3天）
**目标**: 引入类型信息，为类型安全做准备

1. **复制类型系统组件**
2. **增强ByteCodeAssembler**
3. **增强StructValue类型感知**
4. **测试类型系统集成**

#### 阶段3：功能增强（2-3天）
**目标**: 实现类型安全、嵌套支持和字段映射

1. **类型安全字段访问**
2. **嵌套结构体支持**
3. **字段名映射支持**
4. **全面测试**

---

## 8. 性能优化

### 8.1 指令缓存
- **缓存大小**: 可配置（默认1024条指令）
- **缓存策略**: 直接映射缓存
- **缓存失效**: 当PC跳转到缓存外地址时失效

### 8.2 热点检测
- 统计指令执行频率
- 识别热点代码段（循环、频繁调用函数）
- 为JIT编译做准备（未来扩展）

### 8.3 寄存器转发
- 解决RAW（读后写）数据冒险
- 在执行阶段直接将结果转发给解码阶段
- 减少流水线停顿

### 8.4 分支预测
- **简单预测**: 总是预测不跳转
- **预测错误惩罚**: 清空流水线，从正确地址重新取指

---

## 9. 调试支持

### 9.1 断点管理
- 支持代码地址断点
- 支持条件断点（寄存器值条件）
- 断点命中时暂停执行，进入调试模式

### 9.2 单步执行
- 支持指令级单步执行
- 支持函数级单步执行（step over/into/out）
- 单步执行时显示寄存器状态变化

### 9.3 执行跟踪
- 记录指令执行历史
- 记录寄存器值变化历史
- 记录内存访问历史
- 支持回放执行轨迹

### 9.4 调试接口
```java
public interface Debugger {
    // 断点操作
    void setBreakpoint(int address);
    void clearBreakpoint(int address);

    // 执行控制
    void step();        // 单步执行
    void continue();    // 继续执行
    void pause();       // 暂停执行

    // 状态查询
    RegisterSnapshot getRegisters();
    MemorySnapshot getMemory(int address, int size);
    StackTrace getStackTrace();

    // 跟踪控制
    void startTrace();
    ExecutionTrace stopTrace();
}
```

---

## 10. 测试策略

### 10.1 单元测试
- 指令解码正确性测试
- 指令执行正确性测试
- 寄存器文件功能测试
- 内存管理功能测试

### 10.2 集成测试
- 完整程序执行测试
- 函数调用测试
- 异常处理测试
- 调试功能测试

### 10.3 性能测试
- 指令执行速度基准测试
- 内存访问性能测试
- 与栈式虚拟机的性能对比

### 10.4 验证结果
- **VMInterpreterTest**: 6/6测试通过 ✅
- **SimpleVerificationTest**: 2/2测试通过
- **GarbageCollectorTest**: 17/17测试通过

---

## 11. 附录

### 11.1 相关源文件
- `RegisterVMInterpreter.java`: 虚拟机主类
- `RegisterBytecodeDefinition.java`: 指令集定义
- `RegisterByteCodeAssembler.java`: 汇编器
- `RegisterDisAssembler.java`: 反汇编器
- `StackFrame.java`: 栈帧实现
- `LabelSymbol.java`: 标签符号表

### 11.2 配置参数
- `instructionCacheSize`: 指令缓存大小（默认1024）
- `stackSize`: 栈大小（默认65536字节）
- `heapSize`: 堆大小（默认131072字节）
- `maxSteps`: 最大执行步骤（默认1000000）

### 11.3 性能调优建议
1. 调整指令缓存大小以匹配工作集
2. 使用寄存器转发减少数据冒险
3. 优化热点代码的指令序列
4. 合理设置栈和堆大小，避免频繁扩展

---

**文档版本**: v1.0
**最后更新**: 2025-12-16
**制定者**: Claude Code
